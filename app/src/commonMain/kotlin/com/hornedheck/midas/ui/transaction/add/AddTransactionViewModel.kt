package com.hornedheck.midas.ui.transaction.add

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.CategorySource
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.IRuleMatcher
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import midas.app.generated.resources.Res
import midas.app.generated.resources.error_amount_zero
import midas.app.generated.resources.error_description_required
import midas.app.generated.resources.error_invalid_amount
import midas.app.generated.resources.error_save_transaction_failed
import midas.app.generated.resources.error_update_transaction_failed
import org.koin.core.annotation.InjectedParam
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Clock

class AddTransactionViewModel(
    @InjectedParam private val transactionId: Long?,
    private val transactionsRepo: ITransactionsRepo,
    private val categoriesRepo: ICategoriesRepo,
    private val ruleMatcher: IRuleMatcher,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddTransactionState(
            form = AddTransactionFormData(
                date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            )
        )
    )
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    init {
        val form = _state.value.form
        viewModelScope.launch {
            snapshotFlow { form.amountState.text.toString() }
                .drop(1)
                .collectLatest {
                    updateForm { copy(amountError = null) }
                }
        }
        viewModelScope.launch {
            snapshotFlow { form.descriptionState.text.toString() }
                .drop(1)
                .collectLatest {
                    updateForm { copy(descriptionError = null) }
                }
        }
        loadCategories()
        transactionId?.let { loadTransaction(it) }
    }

    @Suppress("MagicNumber")
    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            runCatching { transactionsRepo.getTransactionById(id) }
                .onSuccess { details ->
                    details?.let { d ->
                        val current = _state.value
                        val isBusy = current.saveStatus is SaveStatus.Loading
                            || current.saveStatus is SaveStatus.Success
                        if (isBusy) return@let
                        val isExpense = d.amountCents < 0
                        val absAmount = abs(d.amountCents)
                        val amountText =
                            "${absAmount / 100}.${(absAmount % 100).toString().padStart(2, '0')}"
                        val form = current.form
                        form.amountState.edit { replace(0, length, amountText) }
                        form.descriptionState.edit { replace(0, length, d.description) }
                        form.notesState.edit { replace(0, length, d.notes.orEmpty()) }
                        updateForm {
                            copy(
                                isExpense = isExpense,
                                date = d.date,
                                selectedCategoryId = d.categoryId,
                                categorySource = d.categorySource,
                            )
                        }
                    }
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoriesRepo.getCategories().collect { categories ->
                updateForm {
                    copy(categories = categories.map { c -> CategoryOption(c.id, c.name) })
                }
            }
        }
    }

    private fun updateForm(transform: AddTransactionFormData.() -> AddTransactionFormData) {
        _state.update { current ->
            if (current.saveStatus is SaveStatus.Success) current
            else current.copy(form = current.form.transform())
        }
    }

    fun updateIsExpense(isExpense: Boolean) {
        updateForm { copy(isExpense = isExpense) }
    }

    fun updateDate(date: LocalDate) {
        updateForm { copy(date = date) }
    }

    fun updateCategory(categoryId: Long?) {
        updateForm { copy(selectedCategoryId = categoryId, categorySource = CategorySource.MANUAL) }
    }

    fun setAutoCategory() {
        updateForm { copy(selectedCategoryId = null, categorySource = CategorySource.AUTO) }
    }

    fun clearSaveStatus() {
        _state.update { it.copy(saveStatus = SaveStatus.Idle) }
    }

    fun save() {
        val current = _state.value
        if (current.saveStatus is SaveStatus.Loading || current.saveStatus is SaveStatus.Success || !validate()) return
        val form = _state.value.form
        val amountCents = parseAmountToCents(form.amountState.text.toString()) ?: return
        viewModelScope.launch {
            _state.update { it.copy(saveStatus = SaveStatus.Loading) }
            runCatching {
                val signedAmount = if (form.isExpense) -amountCents else amountCents
                val notes = form.notesState.text.toString().ifBlank { null }
                val description = form.descriptionState.text.toString().trim()

                val categorySource = form.categorySource
                val (resolvedCategoryId, resolvedSource) = if (categorySource != CategorySource.MANUAL) {
                    val matched = runCatching {
                        ruleMatcher.match(description, signedAmount)
                    }.getOrNull()
                    matched to CategorySource.AUTO
                } else {
                    form.selectedCategoryId to CategorySource.MANUAL
                }
                transactionsRepo.upsertTransaction(
                    id = transactionId,
                    date = form.date,
                    amountCents = signedAmount,
                    description = description,
                    categoryId = resolvedCategoryId,
                    notes = notes,
                    categorySource = resolvedSource,
                )
            }.onSuccess {
                _state.update { it.copy(saveStatus = SaveStatus.Success) }
            }.onFailure {
                val errorRes = if (transactionId != null) {
                    Res.string.error_update_transaction_failed
                } else {
                    Res.string.error_save_transaction_failed
                }
                _state.update { it.copy(saveStatus = SaveStatus.Error(errorRes)) }
            }
        }
    }

    private fun validate(): Boolean {
        val form = _state.value.form
        val amountCents = parseAmountToCents(form.amountState.text.toString())
        val descriptionError =
            if (form.descriptionState.text.isBlank()) Res.string.error_description_required else null
        val amountError = when (amountCents) {
            null -> Res.string.error_invalid_amount
            0L -> Res.string.error_amount_zero
            else -> null
        }
        updateForm { copy(descriptionError = descriptionError, amountError = amountError) }
        return descriptionError == null && amountError == null
    }

    @Suppress("MagicNumber")
    private fun parseAmountToCents(text: String): Long? {
        val trimmed = text.trim().takeIf { it.isNotEmpty() } ?: return null
        val dotIndex = trimmed.indexOf('.')
        return if (dotIndex != -1) {
            trimmed.substring(0, min(dotIndex + 3, trimmed.length))
                .padEnd(dotIndex + 3, '0')
                .replace(".", "")
                .toLongOrNull()
        } else {
            trimmed.toLongOrNull()?.let { it * 100 }
        }
    }
}


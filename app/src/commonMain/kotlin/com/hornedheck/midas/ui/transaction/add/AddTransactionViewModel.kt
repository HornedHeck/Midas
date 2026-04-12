package com.hornedheck.midas.ui.transaction.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import midas.app.generated.resources.Res
import midas.app.generated.resources.error_amount_zero
import midas.app.generated.resources.error_description_required
import midas.app.generated.resources.error_invalid_amount
import midas.app.generated.resources.error_save_transaction_failed
import midas.app.generated.resources.error_update_transaction_failed
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Clock

class AddTransactionViewModel(
    private val transactionId: Long?,
    private val transactionsRepo: ITransactionsRepo,
    private val categoriesRepo: ICategoriesRepo,
) : ViewModel() {

    private val _state = MutableStateFlow<AddTransactionState>(
        AddTransactionState.Editing(
            form = AddTransactionFormData(
                date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            )
        )
    )
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    init {
        loadCategories()
        transactionId?.let { loadTransaction(it) }
    }

    @Suppress("MagicNumber")
    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            runCatching { transactionsRepo.getTransactionById(id) }
                .onSuccess { details ->
                    details?.let { d ->
                        val isExpense = d.amountCents < 0
                        val absAmount = abs(d.amountCents)
                        val amountText =
                            "${absAmount / 100}.${(absAmount % 100).toString().padStart(2, '0')}"
                        updateEditing {
                            copy(
                                isExpense = isExpense,
                                amountText = amountText,
                                description = d.description,
                                date = d.datetime.date,
                                originalTime = d.datetime.time,
                                selectedCategoryId = d.categoryId,
                                notes = d.notes.orEmpty(),
                            )
                        }
                    }
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            runCatching { categoriesRepo.getCategories() }
                .onSuccess { categories ->
                    updateEditing {
                        copy(categories = categories.map { c -> CategoryOption(c.id, c.name) })
                    }
                }
        }
    }

    private fun updateEditing(transform: AddTransactionFormData.() -> AddTransactionFormData) {
        _state.update { current ->
            when (current) {
                is AddTransactionState.Editing -> current.copy(form = current.form.transform())
                is AddTransactionState.Saving -> current.copy(form = current.form.transform())
                is AddTransactionState.Saved -> current
            }
        }
    }

    fun updateIsExpense(isExpense: Boolean) {
        updateEditing { copy(isExpense = isExpense) }
    }

    fun updateAmount(amountText: String) {
        updateEditing { copy(amountText = amountText, amountError = null) }
    }

    fun updateDescription(description: String) {
        updateEditing { copy(description = description, descriptionError = null) }
    }

    fun updateDate(date: LocalDate) {
        updateEditing { copy(date = date) }
    }

    fun updateCategory(categoryId: String?) {
        updateEditing { copy(selectedCategoryId = categoryId) }
    }

    fun updateNotes(notes: String) {
        updateEditing { copy(notes = notes) }
    }

    fun clearSaveError() {
        updateEditing { copy(saveError = null) }
    }

    fun clearSaved() {
        val current = _state.value
        if (current is AddTransactionState.Saved) {
            _state.value = AddTransactionState.Editing(current.form)
        }
    }

    // TODO refactor this and state to get rid of 2 fields to store datetime
    fun save() {
        val current = _state.value
        if (current !is AddTransactionState.Editing) return
        if (!validate(current)) return
        val form = current.form
        val amountCents = parseAmountToCents(form.amountText) ?: return
        viewModelScope.launch {
            _state.value = AddTransactionState.Saving(form)
            runCatching {
                val time = form.originalTime ?: Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).time
                val datetime = LocalDateTime(form.date, time)
                val signedAmount = if (form.isExpense) -amountCents else amountCents
                val notes = form.notes.ifBlank { null }
                val description = form.description.trim()
                if (transactionId != null) {
                    transactionsRepo.updateTransaction(
                        id = transactionId,
                        datetime = datetime,
                        amountCents = signedAmount,
                        description = description,
                        categoryId = form.selectedCategoryId,
                        notes = notes,
                    )
                } else {
                    transactionsRepo.addTransaction(
                        datetime = datetime,
                        amountCents = signedAmount,
                        description = description,
                        categoryId = form.selectedCategoryId,
                        notes = notes,
                    )
                }
            }.onSuccess {
                _state.value = AddTransactionState.Saved(form)
            }.onFailure {
                val errorRes = if (transactionId != null) {
                    Res.string.error_update_transaction_failed
                } else {
                    Res.string.error_save_transaction_failed
                }
                _state.value = AddTransactionState.Editing(form.copy(saveError = errorRes))
            }
        }
    }

    private fun validate(current: AddTransactionState.Editing): Boolean {
        val form = current.form
        val amountCents = parseAmountToCents(form.amountText)
        val descriptionError =
            if (form.description.isBlank()) Res.string.error_description_required else null
        val amountError = when (amountCents) {
            null -> Res.string.error_invalid_amount
            0L -> Res.string.error_amount_zero
            else -> null
        }
        _state.value = AddTransactionState.Editing(
            form.copy(descriptionError = descriptionError, amountError = amountError)
        )
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
                .toLong()
        } else {
            trimmed.toLong() * 100
        }
    }
}



package com.hornedheck.midas.ui.transaction.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
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

    private val _state = MutableStateFlow(
        AddTransactionFormState(
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        )
    )
    val state: StateFlow<AddTransactionFormState> = _state.asStateFlow()

    // TODO explain to Copilot how to write Event pipes
    private val _savedEvent = Channel<Unit>(Channel.CONFLATED)
    val savedEvent: Flow<Unit> = _savedEvent.receiveAsFlow()

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
                        _state.update { current ->
                            current.copy(
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
                    _state.update {
                        it.copy(categories = categories.map { c ->
                            CategoryOption(c.id, c.name)
                        })
                    }
                }
        }
    }

    fun updateIsExpense(isExpense: Boolean) {
        _state.update { it.copy(isExpense = isExpense) }
    }

    fun updateAmount(amountText: String) {
        _state.update { it.copy(amountText = amountText, amountError = null) }
    }

    fun updateDescription(description: String) {
        _state.update { it.copy(description = description, descriptionError = null) }
    }

    fun updateDate(date: LocalDate) {
        _state.update { it.copy(date = date) }
    }

    fun updateCategory(categoryId: String?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun clearError() {
        _state.update { it.copy(generalError = null) }
    }

    // TODO refactor this and state to get rid of 2 fields to store datetime
    fun save() {
        if (!validate()) return
        val amountCents = parseAmountToCents(_state.value.amountText) ?: return
        with(_state.value) {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                runCatching {
                    val time = originalTime ?: Clock.System
                        .now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).time
                    val datetime = LocalDateTime(date, time)
                    val signedAmount = if (isExpense) -amountCents else amountCents
                    val notes = notes.ifBlank { null }
                    val description = description.trim()
                    if (transactionId != null) {
                        transactionsRepo.updateTransaction(
                            id = transactionId,
                            datetime = datetime,
                            amountCents = signedAmount,
                            description = description,
                            categoryId = selectedCategoryId,
                            notes = notes,
                        )
                    } else {
                        transactionsRepo.addTransaction(
                            datetime = datetime,
                            amountCents = signedAmount,
                            description = description,
                            categoryId = selectedCategoryId,
                            notes = notes,
                        )
                    }
                }.onSuccess {
                    _savedEvent.send(Unit)
                }.onFailure {
                    val errorRes = if (transactionId != null) {
                        Res.string.error_update_transaction_failed
                    } else {
                        Res.string.error_save_transaction_failed
                    }
                    _state.update { it.copy(isLoading = false, generalError = errorRes) }
                }
            }
        }
    }

    private fun validate(): Boolean {
        val current = _state.value
        val amountCents = parseAmountToCents(current.amountText)
        val descriptionError =
            if (current.description.isBlank()) Res.string.error_description_required else null
        val amountError = when (amountCents) {
            null -> Res.string.error_invalid_amount
            0L -> Res.string.error_amount_zero
            else -> null
        }
        _state.update { it.copy(descriptionError = descriptionError, amountError = amountError) }
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

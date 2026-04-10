package com.hornedheck.midas.ui.transaction.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.parseAmountToCents
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
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AddTransactionViewModel(
    private val transactionsRepo: ITransactionsRepo,
    private val categoriesRepo: ICategoriesRepo,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddTransactionFormState(
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        )
    )
    val state: StateFlow<AddTransactionFormState> = _state.asStateFlow()

    private val _savedEvent = Channel<Unit>(Channel.CONFLATED)
    val savedEvent: Flow<Unit> = _savedEvent.receiveAsFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            runCatching { categoriesRepo.getCategories() }
                .onSuccess { categories ->
                    _state.update { it.copy(categories = categories.map { c -> CategoryOption(c.id, c.name) }) }
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

    @Suppress("ReturnCount")
    fun save() {
        val current = _state.value
        var hasError = false

        if (current.description.isBlank()) {
            _state.update { it.copy(descriptionError = "Description is required") }
            hasError = true
        }

        val amountCents = parseAmountToCents(current.amountText)
        if (amountCents == null) {
            _state.update { it.copy(amountError = "Invalid amount") }
            hasError = true
        } else if (amountCents == 0L) {
            _state.update { it.copy(amountError = "Amount must not be zero") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val datetime = LocalDateTime(current.date, now.time)
                val signedAmount = if (current.isExpense) -(amountCents!!) else amountCents!!
                transactionsRepo.addTransaction(
                    datetime = datetime,
                    amountCents = signedAmount,
                    description = current.description.trim(),
                    categoryId = current.selectedCategoryId,
                    notes = current.notes.ifBlank { null },
                )
            }.onSuccess {
                _savedEvent.send(Unit)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, generalError = e.message ?: "Failed to save transaction") }
            }
        }
    }
}

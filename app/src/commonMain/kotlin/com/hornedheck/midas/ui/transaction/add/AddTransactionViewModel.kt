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
    private val transactionsRepo: ITransactionsRepo,
    private val categoriesRepo: ICategoriesRepo,
) : ViewModel() {

    private var transactionId: Long? = null

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

    @Suppress("MagicNumber")
    fun init(id: Long?) {
        if (transactionId == null && id != null) {
            transactionId = id
            viewModelScope.launch {
                runCatching { transactionsRepo.getTransactionById(id) }
                    .onSuccess { details ->
                        details?.let { d ->
                            val isExpense = d.amountCents < 0
                            val absAmount = abs(d.amountCents)
                            val amountText = "${absAmount / 100}.${(absAmount % 100).toString().padStart(2, '0')}"
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

    @Suppress("ReturnCount", "CognitiveComplexMethod")
    fun save() {
        val current = _state.value
        var hasError = false

        if (current.description.isBlank()) {
            _state.update { it.copy(descriptionError = Res.string.error_description_required) }
            hasError = true
        }

        val amountCents = parseAmountToCents(current.amountText)
        if (amountCents == null) {
            _state.update { it.copy(amountError = Res.string.error_invalid_amount) }
            hasError = true
        } else if (amountCents == 0L) {
            _state.update { it.copy(amountError = Res.string.error_amount_zero) }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val time = current.originalTime ?: now.time
                val datetime = LocalDateTime(current.date, time)
                val signedAmount = if (current.isExpense) -(amountCents!!) else amountCents!!
                val currentId = transactionId
                if (currentId != null) {
                    transactionsRepo.updateTransaction(
                        id = currentId,
                        datetime = datetime,
                        amountCents = signedAmount,
                        description = current.description.trim(),
                        categoryId = current.selectedCategoryId,
                        notes = current.notes.ifBlank { null },
                    )
                } else {
                    transactionsRepo.addTransaction(
                        datetime = datetime,
                        amountCents = signedAmount,
                        description = current.description.trim(),
                        categoryId = current.selectedCategoryId,
                        notes = current.notes.ifBlank { null },
                    )
                }
            }.onSuccess {
                _savedEvent.send(Unit)
            }.onFailure { _ ->
                val errorRes = if (transactionId != null) {
                    Res.string.error_update_transaction_failed
                } else {
                    Res.string.error_save_transaction_failed
                }
                _state.update { it.copy(isLoading = false, generalError = errorRes) }
            }
        }
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


package com.jw.railstatistics.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jw.railstatistics.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TicketViewModel(
    private val repository: TicketRepository
) : ViewModel() {

    private val _tickets = MutableStateFlow<List<TicketRecord>>(emptyList())
    val tickets: StateFlow<List<TicketRecord>> = _tickets.asStateFlow()

    private val _selectedYear = MutableStateFlow("")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()

    private val _statistics = MutableStateFlow(TicketStatistics(0, 0.0, 0.0, 0.0, 0, 0.0, 0))
    val statistics: StateFlow<TicketStatistics> = _statistics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _csvImportResult = MutableStateFlow<CSVImportResult?>(null)
    val csvImportResult: StateFlow<CSVImportResult?> = _csvImportResult.asStateFlow()

    init {
        loadTickets()
        updateStatistics()
    }

    fun loadTickets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllTickets().collect { ticketList ->
                    _tickets.value = ticketList
                    updateStatistics()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading tickets: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedYear(year: String) {
        _selectedYear.value = year
        loadFilteredTickets()
    }

    private fun loadFilteredTickets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val year = _selectedYear.value
                repository.getFilteredTickets(year.takeIf { it.isNotEmpty() }).collect { ticketList ->
                    _tickets.value = ticketList
                    updateStatistics()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading filtered tickets: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateStatistics() {
        viewModelScope.launch {
            try {
                val year = _selectedYear.value
                val stats = repository.calculateStatistics(year.takeIf { it.isNotEmpty() })
                _statistics.value = stats
            } catch (e: Exception) {
                _errorMessage.value = "Error calculating statistics: ${e.message}"
            }
        }
    }

    fun addTicket(ticket: TicketRecord) {
        viewModelScope.launch {
            try {
                repository.insertTicket(ticket)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error adding ticket: ${e.message}"
            }
        }
    }

    fun updateTicket(ticket: TicketRecord) {
        viewModelScope.launch {
            try {
                repository.updateTicket(ticket)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error updating ticket: ${e.message}"
            }
        }
    }

    fun deleteTicket(ticket: TicketRecord) {
        viewModelScope.launch {
            try {
                repository.deleteTicket(ticket)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting ticket: ${e.message}"
            }
        }
    }

    fun importCSV(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (tickets, errors) = repository.parseCSV(uri)
                if (tickets.isNotEmpty()) {
                    repository.insertTickets(tickets)
                    _csvImportResult.value = CSVImportResult.Success(tickets.size, errors)
                } else {
                    _csvImportResult.value = CSVImportResult.Error(errors.joinToString("\n"))
                }
            } catch (e: Exception) {
                _csvImportResult.value = CSVImportResult.Error("Error importing CSV: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun exportCSV(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.exportCSV(_tickets.value, uri)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error exporting CSV: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearCSVImportResult() {
        _csvImportResult.value = null
    }

    fun getTicketsByReturnGroup(returnGroupId: String): List<TicketRecord> {
        return _tickets.value.filter { it.returnGroupID == returnGroupId }
    }

    fun getNewestTicket(): TicketRecord? {
        return _tickets.value.maxByOrNull { ticket ->
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                dateFormat.parse("${ticket.outboundDate} ${ticket.outboundTime}")?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }
}

// Data class for CSV import results
sealed class CSVImportResult {
    data class Success(val importedCount: Int, val errors: List<String>) : CSVImportResult()
    data class Error(val message: String) : CSVImportResult()
}

// ViewModel Factory
class TicketViewModelFactory(
    private val repository: TicketRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TicketViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TicketViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
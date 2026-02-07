package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Employer
import com.example.workertracking.repository.EmployerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EmployersViewModel(
    private val employerRepository: EmployerRepository
) : ViewModel() {
    
    private val _employers = MutableStateFlow<List<Employer>>(emptyList())
    val employers: StateFlow<List<Employer>> = _employers.asStateFlow()
    
    private val _employerProfits = MutableStateFlow<Map<Long, Double>>(emptyMap())

    private val _employerIncomes = MutableStateFlow<Map<Long, Double>>(emptyMap())

    private val _employerExpenses = MutableStateFlow<Map<Long, Double>>(emptyMap())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadEmployers()
    }
    
    private fun loadEmployers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                employerRepository.getAllEmployers().collectLatest { employerList ->
                    _employers.value = employerList
                    loadEmployerFinancials(employerList)
                    _isLoading.value = false
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadEmployerFinancials(employers: List<Employer>) {
        val profits = mutableMapOf<Long, Double>()
        val incomes = mutableMapOf<Long, Double>()
        val expenses = mutableMapOf<Long, Double>()

        employers.forEach { employer ->
            try {
                profits[employer.id] = employerRepository.getTotalProfitFromEmployer(employer.id)
                incomes[employer.id] = employerRepository.getTotalIncomeFromEmployer(employer.id)
                expenses[employer.id] = employerRepository.getTotalExpensesFromEmployer(employer.id)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                profits[employer.id] = 0.0
                incomes[employer.id] = 0.0
                expenses[employer.id] = 0.0
            }
        }
        _employerProfits.value = profits
        _employerIncomes.value = incomes
        _employerExpenses.value = expenses
    }
    
    fun deleteEmployer(employer: Employer) {
        viewModelScope.launch {
            try {
                employerRepository.deleteEmployer(employer)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
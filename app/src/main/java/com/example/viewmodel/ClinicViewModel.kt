package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Patient
import com.example.data.model.ProfessionalProfile
import com.example.data.model.Receipt
import com.example.data.repository.ClinicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    RECEIPTS,
    PATIENTS,
    FINANCIAL,
    SETTINGS,
    CREATE_RECEIPT
}

class ClinicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClinicRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ClinicRepository(
            database.patientDao(),
            database.receiptDao(),
            database.professionalProfileDao()
        )
    }

    // App Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.RECEIPTS)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // PIN Security Lock
    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    // Professional Profile
    val profile: StateFlow<ProfessionalProfile> = repository.professionalProfile
        .map { it ?: ProfessionalProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfessionalProfile()
        )

    fun onPinDigit(digit: String) {
        if (_pinInput.value.length < 4) {
            val updated = _pinInput.value + digit
            _pinInput.value = updated
            _pinError.value = false

            if (updated.length == 4) {
                verifyPin(updated)
            }
        }
    }

    fun onPinDelete() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _pinError.value = false
        }
    }

    private fun verifyPin(entered: String) {
        val correctPin = profile.value.pinCode.ifBlank { "1234" }
        if (entered == correctPin) {
            _isLocked.value = false
            _pinInput.value = ""
            _pinError.value = false
        } else {
            _pinError.value = true
            _pinInput.value = ""
        }
    }

    fun lockApp() {
        _pinInput.value = ""
        _pinError.value = false
        _isLocked.value = true
    }

    fun unlockDirectly() {
        _isLocked.value = false
    }

    // Receipts & Search / Filters
    val allReceipts: StateFlow<List<Receipt>> = repository.allReceipts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _receiptSearchQuery = MutableStateFlow("")
    val receiptSearchQuery: StateFlow<String> = _receiptSearchQuery.asStateFlow()

    private val _receiptFilterStatus = MutableStateFlow("TODOS") // TODOS, PAGO, PENDENTE
    val receiptFilterStatus: StateFlow<String> = _receiptFilterStatus.asStateFlow()

    val filteredReceipts: StateFlow<List<Receipt>> = combine(
        allReceipts,
        _receiptSearchQuery,
        _receiptFilterStatus
    ) { receipts, query, filter ->
        receipts.filter { receipt ->
            val matchesQuery = query.isBlank() ||
                    receipt.patientName.contains(query, ignoreCase = true) ||
                    receipt.receiptNumber.contains(query, ignoreCase = true) ||
                    receipt.patientCpf.contains(query, ignoreCase = true)

            val matchesStatus = when (filter) {
                "PAGO" -> receipt.paymentStatus.equals("PAGO", ignoreCase = true)
                "PENDENTE" -> receipt.paymentStatus.equals("PENDENTE", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setReceiptSearch(query: String) {
        _receiptSearchQuery.value = query
    }

    fun setReceiptFilter(filter: String) {
        _receiptFilterStatus.value = filter
    }

    // Patients & Search
    val allPatients: StateFlow<List<Patient>> = repository.allPatients
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _patientSearchQuery = MutableStateFlow("")
    val patientSearchQuery: StateFlow<String> = _patientSearchQuery.asStateFlow()

    val filteredPatients: StateFlow<List<Patient>> = combine(
        allPatients,
        _patientSearchQuery
    ) { patients, query ->
        if (query.isBlank()) {
            patients
        } else {
            patients.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.cpf.contains(query, ignoreCase = true) ||
                        it.phone.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setPatientSearch(query: String) {
        _patientSearchQuery.value = query
    }

    // Receipt in Creation
    private val _preselectedPatient = MutableStateFlow<Patient?>(null)
    val preselectedPatient: StateFlow<Patient?> = _preselectedPatient.asStateFlow()

    fun startReceiptForPatient(patient: Patient) {
        _preselectedPatient.value = patient
        _currentScreen.value = AppScreen.CREATE_RECEIPT
    }

    fun clearPreselectedPatient() {
        _preselectedPatient.value = null
    }

    // Preview Receipt Dialog
    private val _previewReceipt = MutableStateFlow<Receipt?>(null)
    val previewReceipt: StateFlow<Receipt?> = _previewReceipt.asStateFlow()

    fun showReceiptPreview(receipt: Receipt) {
        _previewReceipt.value = receipt
    }

    fun closeReceiptPreview() {
        _previewReceipt.value = null
    }

    // CRUD Operations
    fun saveReceipt(
        patient: Patient,
        serviceDescription: String,
        sessionQuantity: Int,
        unitPrice: Double,
        discount: Double,
        paymentMethod: String,
        paymentStatus: String,
        notes: String,
        sessionDates: String,
        customDate: String? = null,
        onComplete: (Receipt) -> Unit
    ) {
        viewModelScope.launch {
            val nextNumber = repository.generateNextReceiptNumber()
            val formattedDate = customDate ?: SimpleDateFormat("dd - MMM - yyyy", Locale("pt", "BR")).format(Date())
            val total = (sessionQuantity * unitPrice) - discount

            val newReceipt = Receipt(
                receiptNumber = nextNumber,
                patientId = patient.id,
                patientName = patient.name,
                patientCpf = patient.cpf,
                patientPhone = patient.phone,
                issueDate = formattedDate,
                serviceDescription = serviceDescription.ifBlank { "Sessão de Psicanálise Clínica" },
                sessionQuantity = sessionQuantity,
                unitPrice = unitPrice,
                discount = discount,
                totalAmount = if (total > 0) total else 0.0,
                paymentMethod = paymentMethod,
                paymentStatus = paymentStatus,
                notes = notes,
                sessionDates = sessionDates
            )

            val id = repository.insertReceipt(newReceipt)
            val insertedReceipt = newReceipt.copy(id = id)
            _preselectedPatient.value = null
            _currentScreen.value = AppScreen.RECEIPTS
            onComplete(insertedReceipt)
        }
    }

    fun toggleReceiptPaymentStatus(receipt: Receipt) {
        viewModelScope.launch {
            val newStatus = if (receipt.paymentStatus.equals("PAGO", ignoreCase = true)) "PENDENTE" else "PAGO"
            repository.updateReceipt(receipt.copy(paymentStatus = newStatus))
        }
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            repository.deleteReceipt(receipt)
            if (_previewReceipt.value?.id == receipt.id) {
                _previewReceipt.value = null
            }
        }
    }

    fun savePatient(
        id: Long = 0,
        name: String,
        cpf: String,
        phone: String,
        email: String,
        address: String,
        defaultSessionValue: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val patient = Patient(
                id = id,
                name = name,
                cpf = cpf,
                phone = phone,
                email = email,
                address = address,
                defaultSessionValue = defaultSessionValue,
                notes = notes
            )
            if (id == 0L) {
                repository.insertPatient(patient)
            } else {
                repository.updatePatient(patient)
            }
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient)
        }
    }

    fun updateProfile(
        name: String,
        title: String,
        cpf: String,
        register: String,
        phone: String,
        email: String,
        pixKey: String,
        address: String,
        signatureName: String,
        pinCode: String
    ) {
        viewModelScope.launch {
            val updated = profile.value.copy(
                name = name,
                title = title,
                cpf = cpf,
                professionalRegister = register,
                phone = phone,
                email = email,
                pixKey = pixKey,
                address = address,
                signatureName = signatureName,
                pinCode = pinCode.ifBlank { "1234" }
            )
            repository.updateProfile(updated)
        }
    }
}

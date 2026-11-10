package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.PatientDao
import com.example.data.dao.ProfessionalProfileDao
import com.example.data.dao.ReceiptDao
import com.example.data.model.Patient
import com.example.data.model.ProfessionalProfile
import com.example.data.model.Receipt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Patient::class, Receipt::class, ProfessionalProfile::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun professionalProfileDao(): ProfessionalProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "psico_recibos_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(
                            database.patientDao(),
                            database.receiptDao(),
                            database.professionalProfileDao()
                        )
                    }
                }
            }

            suspend fun populateDatabase(
                patientDao: PatientDao,
                receiptDao: ReceiptDao,
                profileDao: ProfessionalProfileDao
            ) {
                // 1. Initial Professional Profile
                profileDao.insertOrUpdate(
                    ProfessionalProfile(
                        id = 1,
                        name = "Laudiceia Mendonça",
                        title = "Psicanalista Clínica",
                        cpf = "123.456.789-00",
                        professionalRegister = "Reg. Psicanálise 4829/SP",
                        phone = "(11) 98765-4321",
                        email = "laudiceia@psic.com",
                        pixKey = "laudiceia.pix@email.com",
                        address = "Av. Paulista, 1000 - Cj. 82 - Bela Vista, São Paulo - SP",
                        signatureName = "Céia Mendonça",
                        pinCode = "1234"
                    )
                )

                // 2. Initial Sample Patients
                val p1Id = patientDao.insertPatient(
                    Patient(
                        name = "Mariana Alves Costa",
                        cpf = "321.654.987-11",
                        phone = "(11) 99123-4567",
                        email = "mariana.alves@email.com",
                        address = "Rua Augusta, 450 - Consolação, São Paulo - SP",
                        defaultSessionValue = 200.0,
                        notes = "Atendimento semanal às terças-feiras às 16h. Foco em ansiedade e desenvolvimento profissional."
                    )
                )

                val p2Id = patientDao.insertPatient(
                    Patient(
                        name = "Carlos Eduardo Silveira",
                        cpf = "789.123.456-22",
                        phone = "(11) 98456-7890",
                        email = "carlos.silveira@email.com",
                        address = "Alameda Santos, 820 - Jardins, São Paulo - SP",
                        defaultSessionValue = 180.0,
                        notes = "Atendimento quinzenal às quintas-feiras às 18h."
                    )
                )

                val p3Id = patientDao.insertPatient(
                    Patient(
                        name = "Beatriz Helena Ramos",
                        cpf = "456.789.012-33",
                        phone = "(11) 97654-3210",
                        email = "beatriz.ramos@email.com",
                        address = "Rua Oscar Freire, 120 - Cerqueira César, São Paulo - SP",
                        defaultSessionValue = 220.0,
                        notes = "Psicanálise de orientação lacaniana. Preferência por recibos mensais para o IRPF."
                    )
                )

                // 3. Initial Sample Receipts
                receiptDao.insertReceipt(
                    Receipt(
                        receiptNumber = "REC-2026-001",
                        patientId = p1Id,
                        patientName = "Mariana Alves Costa",
                        patientCpf = "321.654.987-11",
                        patientPhone = "(11) 99123-4567",
                        issueDate = "05 - Nov - 2026",
                        serviceDescription = "Sessão de Psicanálise Clínica",
                        sessionQuantity = 4,
                        unitPrice = 200.0,
                        discount = 0.0,
                        totalAmount = 800.0,
                        paymentMethod = "PIX",
                        paymentStatus = "PAGO",
                        notes = "Sessões realizadas nos dias: 03, 10, 17 e 24 de Outubro/2026.",
                        sessionDates = "03, 10, 17, 24 Out"
                    )
                )

                receiptDao.insertReceipt(
                    Receipt(
                        receiptNumber = "REC-2026-002",
                        patientId = p2Id,
                        patientName = "Carlos Eduardo Silveira",
                        patientCpf = "789.123.456-22",
                        patientPhone = "(11) 98456-7890",
                        issueDate = "04 - Nov - 2026",
                        serviceDescription = "Atendimento Psicoterápico Individual",
                        sessionQuantity = 2,
                        unitPrice = 180.0,
                        discount = 0.0,
                        totalAmount = 360.0,
                        paymentMethod = "Transferência",
                        paymentStatus = "PAGO",
                        notes = "Sessões quinzenais de Outubro/2026.",
                        sessionDates = "08 e 22 Out"
                    )
                )

                receiptDao.insertReceipt(
                    Receipt(
                        receiptNumber = "REC-2026-003",
                        patientId = p3Id,
                        patientName = "Beatriz Helena Ramos",
                        patientCpf = "456.789.012-33",
                        patientPhone = "(11) 97654-3210",
                        issueDate = "06 - Nov - 2026",
                        serviceDescription = "Sessão de Psicanálise Clínica",
                        sessionQuantity = 1,
                        unitPrice = 220.0,
                        discount = 0.0,
                        totalAmount = 220.0,
                        paymentMethod = "PIX",
                        paymentStatus = "PENDENTE",
                        notes = "Sessão realizada no dia 05/11. Aguardando confirmação do comprovante PIX.",
                        sessionDates = "05 Nov"
                    )
                )
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.CreateReceiptScreen
import com.example.ui.screens.FinancialScreen
import com.example.ui.screens.PatientsScreen
import com.example.ui.screens.ReceiptsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ClinicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: ClinicViewModel = viewModel()) {
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val pinInput by viewModel.pinInput.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    if (isLocked) {
        PinLockScreen(
            pinLength = pinInput.length,
            isError = pinError,
            onDigitClick = { viewModel.onPinDigit(it) },
            onDeleteClick = { viewModel.onPinDelete() },
            onQuickUnlock = { viewModel.unlockDirectly() }
        )
    } else {
        // Back navigation when inside secondary screens
        if (currentScreen != AppScreen.RECEIPTS) {
            BackHandler {
                if (currentScreen == AppScreen.CREATE_RECEIPT) {
                    viewModel.clearPreselectedPatient()
                }
                viewModel.navigateTo(AppScreen.RECEIPTS)
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (currentScreen != AppScreen.CREATE_RECEIPT) {
                    BottomNavBar(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                when (currentScreen) {
                    AppScreen.RECEIPTS -> ReceiptsScreen(viewModel = viewModel)
                    AppScreen.CREATE_RECEIPT -> CreateReceiptScreen(viewModel = viewModel)
                    AppScreen.PATIENTS -> PatientsScreen(viewModel = viewModel)
                    AppScreen.FINANCIAL -> FinancialScreen(viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

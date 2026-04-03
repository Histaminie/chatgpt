package com.example.expensetracker

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.components.BottomNavBar
import com.example.expensetracker.ui.navigation.NavGraph
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                val navController = rememberNavController()
                val viewModel: ExpenseViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                val createFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
                    if (uri == null) return@rememberLauncherForActivityResult
                    CoroutineScope(Dispatchers.IO).launch {
                        val csv = viewModel.exportCsv()
                        context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                    }
                }

                val openFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                    if (uri == null) return@rememberLauncherForActivityResult
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                        viewModel.importCsv(it.readText())
                    }
                }

                LaunchedEffect(state.message) {
                    state.message?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearMessage()
                    }
                }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = { BottomNavBar(navController) },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    floatingActionButton = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { createFile.launch("expenses_export.csv") }) { Text("Export CSV") }
                            Button(onClick = { openFile.launch(arrayOf("text/*")) }) { Text("Restore") }
                        }
                    }
                ) { innerPadding ->
                    NavGraph(navController = navController, modifier = Modifier.padding(innerPadding), viewModel = viewModel)
                }
            }
        }
    }
}

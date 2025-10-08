package com.example.practica1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis
import androidx.lifecycle.lifecycleScope
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.delay
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                DemoScreen(
                    runSequential = {
                        // ¡ATENCIÓN! Esto corre en el hilo principal y CONGELA la UI.
                        var result = 0
                        val elapsed = measureTimeMillis {
                            result = longPrimeCount(100_000)
                        }
                        "Secuencial (bloquea UI): primes=$result en ${elapsed} ms"
                    },
                    runOnThread = { onDone ->
                        Thread {
                            var result = 0
                            val elapsed = measureTimeMillis {
                                result = longPrimeCount(400_000) // súbelo un poco si hace falta
                            }

                            // Pausa opcional para que el spinner sea visible (demo)
                            try { Thread.sleep(1000) } catch (_: InterruptedException) {}

                            runOnUiThread {
                                onDone("Thread (concurrente): primes=$result en ${elapsed} ms")
                            }
                        }.start()
                    },
                    runWithCoroutines = { onDone ->
                        lifecycleScope.launch(Dispatchers.Default) {
                            var result = 0
                            val elapsed = measureTimeMillis {
                                result = longPrimeCount(400_000) // súbelo para ver el spinner
                            }

                            // Pausa opcional de demostración
                            delay(700)

                            withContext(Dispatchers.Main) {
                                onDone("Coroutine (concurrente): primes=$result en ${elapsed} ms")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DemoScreen(
    runSequential: () -> String,
    runOnThread: (onDone: (String) -> Unit) -> Unit,
    runWithCoroutines: (onDone: (String) -> Unit) -> Unit
) {
    var status by remember { mutableStateOf("Listo. Elige un modo de ejecución.") }
    var isBusy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = status, style = MaterialTheme.typography.titleMedium)

        if (isBusy) {
            CircularProgressIndicator()
        }

        // 1) SECuENCIAL: BLOQUEA LA UI
        Button(
            onClick = {
                isBusy = true
                status = "Ejecutando SECuENCIAL (bloquea UI)..."

                // Truco: Posteamos el trabajo pesado al siguiente frame
                // para que la UI pinte el spinner y luego se congele.
                Handler(Looper.getMainLooper()).post {
                    val msg = runSequential() // <- sigue en el hilo principal (bloquea)
                    isBusy = false
                    status = msg
                }
            },
            enabled = !isBusy
        ) {
            Text("Secuencial (bloquea UI)")
        }

        // 2) CONCURRENTE con THREAD
        Button(
            onClick = {
                isBusy = true
                status = "Ejecutando en Thread..."
                runOnThread { msg ->
                    isBusy = false
                    status = msg
                }
            },
            enabled = !isBusy
        ) {
            Text("Concurrente: Thread")
        }

        // 3) CONCURRENTE con COROUTINES
        Button(
            onClick = {
                isBusy = true
                status = "Ejecutando con Coroutines..."
                runWithCoroutines { msg ->
                    isBusy = false
                    status = msg
                }
            },
            enabled = !isBusy
        ) {
            Text("Concurrente: Coroutines")
        }
    }
}

package com.example.practica1

/**
 * Algoritmo CPU-bound sencillo: cuenta primos hasta N.
 * Sube o baja N si en tu equipo corre demasiado rápido/lento.
 */
fun longPrimeCount(n: Int): Int {
    var count = 0
    for (i in 2..n) {
        var prime = true
        var d = 2
        while (d * d <= i) {
            if (i % d == 0) { prime = false; break }
            d++
        }
        if (prime) count++
    }
    return count
}

package br.com.arlesonsilva.geratimes.Utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class DateTimeUtils {

    @SuppressLint("SimpleDateFormat")
    fun dateAtual(): String? {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        return currentDate
    }

    @SuppressLint("SimpleDateFormat")
    fun horaAtual(): String? {
        val sdf = SimpleDateFormat("HH:ss")
        val currentHour = sdf.format(Date())
        return currentHour
    }
}
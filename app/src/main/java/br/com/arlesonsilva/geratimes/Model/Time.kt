package br.com.arlesonsilva.geratimes.Model

data class Time(val id: Int, val nome: String, val data: String, val jogador: ArrayList<JogadorTime>, val racha_id: Int)
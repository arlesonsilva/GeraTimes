package br.com.arlesonsilva.geratimes.Fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import br.com.arlesonsilva.geratimes.Adapter.ConfirmadoAdapter
import br.com.arlesonsilva.geratimes.Adapter.JogadorTimeAdapter
import br.com.arlesonsilva.geratimes.DBHelper.database
import br.com.arlesonsilva.geratimes.Model.Confirmado
import br.com.arlesonsilva.geratimes.Model.JogadorTime
import br.com.arlesonsilva.geratimes.Model.Racha
import br.com.arlesonsilva.geratimes.Model.Time
import br.com.arlesonsilva.geratimes.R
import br.com.arlesonsilva.geratimes.Utils.DateTimeUtils
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil

class SorteioFragment : Fragment() {

    private var dateAtual: String? = null
    private var btnSorteio: FloatingActionButton? = null
    private var btnShare: FloatingActionButton? = null
    private lateinit var listView: ListView
    private var listConfirmado = ArrayList<Confirmado>()
    private var listTimeSorteio = ArrayList<Time>()
    private var listJogadorTime = ArrayList<JogadorTime>()
    private var adapter: ConfirmadoAdapter? = null
    private var adapterTime: JogadorTimeAdapter? = null
    private var racha: Racha? = null
    lateinit var empty: TextView
    private var idRacha: Int = 0
    private var timeBola: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_sorteio, container, false)

        empty = view.findViewById(android.R.id.empty)
        listView = view.findViewById(R.id.list_confirmados)
        adapter = ConfirmadoAdapter(view.context, listConfirmado)
        adapterTime = JogadorTimeAdapter(view.context, listTimeSorteio)
        btnSorteio = view.findViewById<FloatingActionButton>(R.id.btn_sorteio)
        btnShare = view.findViewById<FloatingActionButton>(R.id.btn_share)
        idRacha = arguments!!.getInt("idRacha")
        dateAtual = DateTimeUtils().dateAtual()

        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_time WHERE racha_id = ?", arrayOf(idRacha.toString()))
            if (cursor.count > 0) {
                selectTimeJogadorDB()
            }else{
                selectJogadorConfirmadoDB()
            }
        }

        btnSorteio!!.setOnClickListener {
            if (listConfirmado.size == 0 && listTimeSorteio.size == 0) {
                toast("Nenhum jogador confirmado para o sorteio.")
            }else if(listConfirmado.size == 0 && listTimeSorteio.size > 0) {
                //dialogSortearNovamente()
                toast("Sorteio já realizado!")
            }else {
                dialogSorteioTimes()
            }
        }

        btnShare!!.setOnClickListener{
            var message = "Sorteio de time(s) do racha ${racha!!.nome} realizado no dia ${dateAtual} \n"
            for (t in listTimeSorteio) {
                message = message + "\n" + t.nome
                for (j in t.jogador) {
                    message = message + "\n" + j.nome
                }
            }
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, message)
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent,"Compartilhar com: "))
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        selectRachaDB()
        selectTimeBolaDB()
    }

//    fun dialogSortearNovamente() {
//        val dialogBuilder = AlertDialog.Builder(view!!.context)
//        dialogBuilder.setTitle("Atenção")
//        dialogBuilder.setMessage("Deseja realizar o sorteio novamente?")
//        dialogBuilder.setPositiveButton("Sim") { _, _ ->
//            context!!.database.use { delete("tb_time",null,null) }
//            context!!.database.use { delete("tb_jogador_time",null,null) }
//            dialogSorteioTimes()
//        }
//        dialogBuilder.setNegativeButton("Não") { _, _ ->
//            return@setNegativeButton
//        }
//        val alertDialog = dialogBuilder.create()
//        alertDialog.show()
//    }

    fun dialogSorteioTimes() {
        val dialogBuilder = AlertDialog.Builder(view!!.context)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.add_sorteio_dialog, null)
        var numero = view.findViewById<EditText>(R.id.numero_jogador)

        numero.setText(racha!!.nu_jogadores_time.toString())

        dialogBuilder.setView(view)
        dialogBuilder.setTitle("Jogadores por time")
        dialogBuilder.setMessage("\nEste é o número de jogadores por time sem o goleiro para o racha ${racha!!.nome}?")
        dialogBuilder.setPositiveButton("Sortear") { _, _ ->
            when {
                numero.length() == 0 -> context!!.toast("Campo número de jogadores obrigatório")
                numero.text.toString().toInt() == 0 -> context!!.toast("Campo número de jogadores deve ser maior que 0")
                else -> sorteioTimes(listConfirmado, numero.text.toString().toInt())
            }
        }
        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            return@setNegativeButton
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun sorteioTimes(listConfirmado: ArrayList<Confirmado>, nuJogadoresTime: Int) {
        val list = listConfirmado
        val numberOfGroups = ceil(list.size.toDouble() / nuJogadoresTime.toDouble()).toInt()
        getRandomElement(list, nuJogadoresTime, numberOfGroups)
        selectTimeJogadorDB()
    }

    private fun getRandomElement(list: MutableList<Confirmado>, numberOfElements: Int, numberOfGroups: Int) /*: List<String>*/ {
        var idTime: Long = 0
        val rand = Random()
        for (i in 1 .. numberOfGroups) {
            if (timeBola && i == 1) {
                context!!.database.use {
                    idTime = insert("tb_time",
                        "nome" to "Time ${i} - Começa com a bola",
                        "data" to dateAtual,
                        "racha_id" to idRacha
                    )
                }
            }else {
                context!!.database.use {
                    idTime = insert("tb_time",
                        "nome" to "Time ${i}",
                        "data" to dateAtual,
                        "racha_id" to idRacha
                    )
                }
            }
            for (j in 0 until numberOfElements) {
                if (list.count() > 0) {
                    val randomIndex = rand.nextInt(list.size)
                    insertJogadorTimeDB(
                        list[randomIndex].nome,
                        idTime.toInt(),
                        idRacha
                    )
                    list.removeAt(randomIndex)
                }
            }
        }
        return
    }

    fun insertJogadorTimeDB(nome: String, idTime: Int, idRacha: Int) {
        context!!.database.use {
            insert("tb_jogador_time",
                "nome" to nome,
                "time_id" to idTime,
                "racha_id" to idRacha
            )
        }
    }

    private fun selectTimeBolaDB() {
        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_configuracao WHERE id = 2", null)
            if (cursor.moveToFirst()) {
                do {
                    timeBola = cursor.getString(2)!!.toBoolean()
                    Log.i("selectTimeBolaDB",cursor.getString(1) + timeBola)
                } while (cursor.moveToNext())
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun selectTimeJogadorDB() {
        val listTimeSorteio2 = ArrayList<Time>()
        val listJogadorTime2 = ArrayList<JogadorTime>()
        listTimeSorteio.clear()
        listJogadorTime.clear()
        listTimeSorteio2.clear()
        listJogadorTime2.clear()
        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_time WHERE racha_id = ?" , arrayOf(idRacha.toString()))
            if (cursor.moveToFirst()) {
                do {
                    listJogadorTime.clear()
                    val cursorJ = rawQuery("SELECT * FROM tb_jogador_time WHERE time_id = ? AND racha_id = ?",
                        arrayOf(cursor.getInt(0).toString(), idRacha.toString())
                    )
                    if (cursorJ.moveToFirst()) {
                        do {
                            listJogadorTime.add(
                                JogadorTime(
                                    cursorJ.getInt(0),
                                    cursorJ.getString(1),
                                    cursorJ.getInt(2),
                                    cursorJ.getInt(3)
                                )
                            )
                        } while (cursorJ.moveToNext())
                    }
                    cursorJ.close()

                    listTimeSorteio.add(
                        Time(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            listJogadorTime.filter { jogadorTime-> jogadorTime.time_id == cursor.getInt(0) } as ArrayList<JogadorTime>,
                            cursor.getInt(3)
                        )
                    )
                } while (cursor.moveToNext())
            }else {
                empty.visibility = View.VISIBLE
                listView.emptyView = empty
            }
            cursor.close()

        }
        btnShare!!.visibility = View.VISIBLE
        listView.adapter = adapterTime
        adapterTime!!.notifyDataSetChanged()
    }

    private fun selectRachaDB() {
        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_racha WHERE id = ?" , arrayOf(idRacha.toString()))
            if (cursor.moveToFirst()) {
                do {
                    racha =
                        Racha(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2)!!.toBoolean(),
                            cursor.getInt(3),
                            cursor.getString(4),
                            cursor.getString(5)
                        )
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun selectJogadorConfirmadoDB() {
        Log.i("selectJogador","selectJogadorConfirmadoDB")
        listConfirmado.clear()
        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_jogador WHERE racha_id = ? AND pago = 'true' AND goleiro = 'false' ORDER BY nome", arrayOf(idRacha.toString()))
            Log.i("selectJogador",cursor.count.toString())
            if (cursor.moveToFirst()) {
                do {
                    listConfirmado.add(
                        Confirmado(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(3)!!.toBoolean(),
                            idRacha
                        )
                    )
                } while (cursor.moveToNext())
            }else {
                empty.visibility = View.VISIBLE
                listView.emptyView = empty
            }
            cursor.close()
        }
        btnSorteio!!.visibility = View.VISIBLE
        listView.adapter = adapter
        adapter!!.notifyDataSetChanged()
    }

}

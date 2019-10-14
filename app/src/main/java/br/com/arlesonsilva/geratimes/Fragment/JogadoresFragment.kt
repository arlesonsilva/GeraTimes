package br.com.arlesonsilva.geratimes.Fragment

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import br.com.arlesonsilva.geratimes.Adapter.JogadorAdapter
import br.com.arlesonsilva.geratimes.DBHelper.database
import br.com.arlesonsilva.geratimes.Model.Jogador
import br.com.arlesonsilva.geratimes.R
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.update
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.support.v4.toast

class JogadoresFragment : Fragment() {

    private var txtContador: TextView? = null
    private var switch: Switch? = null
    private var btnAddJogador: Button? = null
    private lateinit var listView: ListView //SwipeMenuListView
    private var listJogador = ArrayList<Jogador>()
    private var adapter: JogadorAdapter? = null
    lateinit var empty: TextView
    private var idRacha: Int = 0
    private var inclusao: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_jogadores, container, false)

        txtContador = view.findViewById(R.id.txtContador)
        switch = view.findViewById(R.id.switchPagoTodos)
        empty = view.findViewById(android.R.id.empty)
        listView = view.findViewById(R.id.list_jogadores)
        adapter = JogadorAdapter(view.context, listJogador)
        btnAddJogador = view.findViewById(R.id.add_jogador)
        listView.adapter = adapter
        idRacha = arguments!!.getInt("idRacha")

        switch!!.setOnCheckedChangeListener { _, _ ->
            updateTodosPagoDB(switch!!.isChecked)
        }

        btnAddJogador!!.setOnClickListener {
            dialogAddJogador()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        selectJogadorDB()
        selectInclusaoDB()
    }

    fun dialogAddJogador() {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.add_jogador_dialog, null)
        val nome = view.findViewById<EditText>(R.id.edtNome)
        val pago = view.findViewById<Switch>(R.id.swtPago)
        val goleiro = view.findViewById<Switch>(R.id.swtGoleiro)

        dialogBuilder.setTitle("Adicionar jogador")
        dialogBuilder.setView(view)
        dialogBuilder.setPositiveButton("Salvar",null)
        dialogBuilder.setNegativeButton("Cancelar", null)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val cancelButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        goleiro.onCheckedChange { _, _ ->
            pago.isChecked = goleiro.isChecked
        }

        positiveButton.setOnClickListener {
            if(nome.text.trim().isEmpty()) {
                toast("Campo nome obrigatório")
            }else {
                insertJogadorDB(
                    nome.text.toString(),
                    pago.isChecked,
                    goleiro.isChecked,
                    alertDialog
                )
            }
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

    }

    private fun insertJogadorDB(
        nome: String,
        pago: Boolean,
        goleiro: Boolean,
        alertDialog: AlertDialog) {
        context!!.database.use {
            insert("tb_jogador",
                "nome" to nome,
                "goleiro" to goleiro.toString(),
                "pago" to pago.toString(),
                "racha_id" to idRacha
            )
        }
        selectJogadorDB()
        alertDialog.dismiss()
        toast("Jogador ${nome} inserido com sucesso")
        if (inclusao) {
            dialogAddJogador()
        }
    }

    private fun selectInclusaoDB() {
        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_configuracao WHERE id = 1", null)
            if (cursor.moveToFirst()) {
                do {
                    inclusao = cursor.getString(2)!!.toBoolean()
                } while (cursor.moveToNext())
            }
        }
    }

    fun verificaTodosPago() {
        context!!.database.use {
            val cursor = rawQuery("SELECT count(*) FROM tb_jogador WHERE racha_id = ? AND goleiro = 'false'", arrayOf(idRacha.toString()))
            if (cursor.moveToFirst()) {
                do {
                    val nJogadores = cursor.getInt(0)
                    Log.i("verificaTodosPago","${nJogadores}")
                    if (nJogadores > 0) {
                        val cursor2 = rawQuery("SELECT count(*) FROM tb_jogador WHERE racha_id = ? AND pago = 'true' AND goleiro = 'false'", arrayOf(idRacha.toString()))
                        if (cursor2.moveToFirst()) {
                            do {
                                val nJogadoresPG = cursor2.getInt(0)
                                Log.i("verificaTodosPago","${nJogadores} ${nJogadoresPG}")
                                if (nJogadores == nJogadoresPG) {
                                    switch!!.isChecked = true
                                }else if (nJogadores == 0) {
                                    switch!!.isChecked = false
                                }
                            } while (cursor2.moveToNext())
                        }else {
                            switch!!.isChecked = false
                        }
                        cursor2.close()
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    fun updateTodosPagoDB(status: Boolean) {
        context!!.database.use {
            update("tb_jogador",
                "pago" to status.toString())
                .whereSimple("goleiro != 'true' AND racha_id = ${idRacha}" )
                .exec()
        }
        selectJogadorDB()
    }

    fun selectJogadorDB() {
        listJogador.clear()
        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_jogador WHERE racha_id = ? ORDER BY nome", arrayOf(idRacha.toString()))
            if (cursor.moveToFirst()) {
                do {
                    listJogador.add(
                        Jogador(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2)!!.toBoolean(),
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

        selectCountJogadorPagoDB()
        verificaTodosPago()
        listView.adapter = adapter
        adapter!!.notifyDataSetChanged()
    }

    fun selectCountJogadorPagoDB() {
        context!!.database.use {
            val cursor = rawQuery("SELECT count(*) FROM tb_jogador WHERE racha_id = ? AND pago = 'true' AND goleiro = 'false'", arrayOf(idRacha.toString()))
            if (cursor.moveToFirst()) {
                do {
                    txtContador!!.text = "Jogadores pagos: " + cursor.getString(0)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

}

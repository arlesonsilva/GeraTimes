package br.com.arlesonsilva.geratimes.Acitivity

import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.widget.*
import br.com.arlesonsilva.geratimes.Adapter.RachaAdapter
import br.com.arlesonsilva.geratimes.Model.Racha
import br.com.arlesonsilva.geratimes.R
import br.com.hapvida.desospofflinehap.DBHelper.database
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.*

class RachaActivity : AppCompatActivity() {

    private var btnAddRacha: FloatingActionButton? = null
    private lateinit var listView: ListView
    private var listRacha = ArrayList<Racha>()
    private var adapter: RachaAdapter? = null
    lateinit var empty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_racha)

        empty = findViewById(android.R.id.empty)
        btnAddRacha = findViewById<FloatingActionButton>(R.id.add_racha)
        listView = findViewById(R.id.list_rachas)
        adapter = RachaAdapter(this@RachaActivity, listRacha)

        selectRachaDB()

        btnAddRacha!!.setOnClickListener {
            dialogAddRacha()
        }

    }

    fun selectRachaDB() {
        listRacha.clear()
        database.use {
            val cursor = rawQuery("SELECT * FROM tb_racha", null)
            if (cursor.moveToFirst()) {
                do {
                    listRacha.add(
                        Racha(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2)!!.toBoolean(),
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

        listView.adapter = adapter
        adapter!!.notifyDataSetChanged()
    }

    fun insertRachaDB(nome: String, status: Boolean, numero: String) {
        database.use {
            insert("tb_racha",
                "nome" to nome,
                "status" to status.toString(),
                "jogadores_por_time" to numero
            )
        }
        toast("Racha ${nome} inserido com sucesso")
        selectRachaDB()
    }

    fun dialogAddRacha() {
        val dialogBuilder = AlertDialog.Builder(this@RachaActivity)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.add_racha_dialog, null)
        var nome = view.findViewById<EditText>(R.id.edtNome)
        var status = view.findViewById<Switch>(R.id.swtStatus)
        var numero = view.findViewById<EditText>(R.id.edtNJogadores)

        dialogBuilder.setTitle("Adicionar racha")
        dialogBuilder.setView(view)

        dialogBuilder.setPositiveButton("Salvar") { _, _ ->
            if(nome.text.trim().isEmpty()) {
                toast("Campo nome obrigatório")
            }else if (numero.length() == 0) {
                toast("Campo número de jogadores obrigatório")
            }else if (numero.text.toString().toInt() == 0) {
                toast("Campo número de jogadores deve ser maior que 0")
            }else {
                insertRachaDB(
                    nome.text.toString(),
                    status.isChecked,
                    numero.text.toString()
                )
            }
        }

        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            return@setNegativeButton
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

}

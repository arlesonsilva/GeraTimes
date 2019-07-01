package br.com.arlesonsilva.geratimes.Fragment

import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import br.com.arlesonsilva.geratimes.Adapter.JogadorAdapter
import br.com.arlesonsilva.geratimes.Model.Jogador
import br.com.arlesonsilva.geratimes.R
import br.com.hapvida.desospofflinehap.DBHelper.database
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.update
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.support.v4.toast

class JogadoresFragment : Fragment() {

    private var txtContador: TextView? = null
    private var switch: Switch? = null
    private var btnAddJogador: FloatingActionButton? = null
    private lateinit var listView: SwipeMenuListView
    private var listJogador = ArrayList<Jogador>()
    private var adapter: JogadorAdapter? = null
    lateinit var empty: TextView
    private var idRacha: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.fragment_jogadores, container, false)

        txtContador = view.findViewById(R.id.txtContador)
        switch = view.findViewById(R.id.switchPagoTodos)
        empty = view.findViewById(android.R.id.empty)
        listView = view.findViewById(R.id.list_jogadores)
        adapter = JogadorAdapter(view.context, listJogador)
        btnAddJogador = view.findViewById<FloatingActionButton>(R.id.add_jogador)
        listView.adapter = adapter
        idRacha = arguments!!.getInt("idRacha")

        selectJogadorDB()

        val creator = SwipeMenuCreator { menu ->
            val editItem = SwipeMenuItem(
                view.context
            )
            editItem.background = ColorDrawable(
                Color.rgb(
                    0xC9,
                    0xC9,
                    0xCE
                )
            )
            editItem.width = 150
            editItem.setIcon(R.drawable.ic_mode_edit_black_24dp)
            menu.addMenuItem(editItem)

            val deleteItem = SwipeMenuItem(
                view.context
            )
            deleteItem.background = ColorDrawable(
                Color.rgb(
                    0xF9,
                    0x3F,
                    0x25
                )
            )
            deleteItem.width = 150
            deleteItem.setIcon(R.drawable.ic_delete_black_24dp)
            menu.addMenuItem(deleteItem)
        }

        listView.setMenuCreator(creator)

        listView.setOnMenuItemClickListener { position, menu, index ->
            var jogador = listJogador[position]
            when (index) {
                0 -> {
                    dialogEditarJogador(jogador)
                }
                1 -> {
                    deleteJogadorDB(jogador)
                }
            }
            false
        }

        switch!!.onCheckedChange { buttonView, isChecked ->
            updateTodosPagoDB(switch!!.isChecked)
            selectJogadorDB()
        }

        btnAddJogador!!.setOnClickListener {
            dialogAddJogador()
        }

        return view
    }

    fun dialogAddJogador() {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.add_jogador_dialog, null)
        val nome = view.findViewById<EditText>(R.id.edtNome)
        val pago = view.findViewById<Switch>(R.id.swtPago)
        val goleiro = view.findViewById<Switch>(R.id.swtGoleiro)

        goleiro.onCheckedChange { buttonView, isChecked ->
            pago.isChecked = goleiro.isChecked
        }

        dialogBuilder.setTitle("Adicionar jogador")
        dialogBuilder.setView(view)
        dialogBuilder.setPositiveButton("Salvar") { _, _ ->
            if(nome.text.trim().isEmpty()) {
                toast("Campo nome obrigatório")
            }else {
                insertJogadorDB(
                    nome.text.toString(),
                    pago.isChecked,
                    goleiro.isChecked
                )
            }
        }

        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            return@setNegativeButton
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    fun dialogEditarJogador(jogador: Jogador) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.add_jogador_dialog, null)
        val nome = view.findViewById<EditText>(R.id.edtNome)
        val pago = view.findViewById<Switch>(R.id.swtPago)
        val goleiro = view.findViewById<Switch>(R.id.swtGoleiro)

        nome.setText(jogador.nome)
        pago.isChecked = jogador.pago
        goleiro.isChecked = jogador.goleiro

        goleiro.onCheckedChange { buttonView, isChecked ->
            pago.isChecked = goleiro.isChecked
        }

        dialogBuilder.setTitle("Editar jogador")
        dialogBuilder.setView(view)
        dialogBuilder.setPositiveButton("Salvar") { _, _ ->
            if(nome.text.trim().isEmpty()) {
                toast("Campo nome obrigatório")
            }else {
                val jogador =
                    Jogador(
                        jogador.id,
                        nome.text.toString(),
                        goleiro.isChecked,
                        pago.isChecked,
                        idRacha
                )
                updateJogadorDB(jogador)
            }
        }

        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            return@setNegativeButton
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    fun insertJogadorDB(nome: String, pago: Boolean, goleiro: Boolean) {

        context!!.database.use {
            insert("tb_jogador",
                "nome" to nome,
                "goleiro" to goleiro.toString(),
                "pago" to pago.toString(),
                "racha_id" to idRacha
            )
        }

        toast("Jogador ${nome} inserido com sucesso")
        selectJogadorDB()
    }

    fun verificaTodosPago() {
        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_jogador WHERE racha_id = ? AND pago = 'false' AND goleiro = 'false'", arrayOf(idRacha.toString()))
            switch!!.isChecked = cursor.count <= 0
            cursor.close()
        }
    }

    fun updateJogadorDB(jogador: Jogador) {
        context!!.database.use {
            update("tb_jogador",
                "nome" to jogador.nome,
                "goleiro" to jogador.goleiro.toString(),
                "pago" to jogador.pago.toString(),
                "racha_id" to jogador.racha_id)
                .whereSimple("id = ?", jogador.id.toString())
                .exec()
        }

        toast("Jogador ${jogador.nome} editado com sucesso")
        selectJogadorDB()
    }

    fun updateTodosPagoDB(status: Boolean) {
        context!!.database.use {
            update("tb_jogador",
                "pago" to status.toString())
                //.whereSimple()
                .exec()
        }
    }

    fun deleteJogadorDB(jogador: Jogador) {
        context!!.database.use {
            delete("tb_jogador", "id = {id}", "id" to jogador.id)
        }
        toast("Jogador ${jogador.nome} excluído com sucesso")
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

        verificaTodosPago()
        selectCountJogadorPagoDB()
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

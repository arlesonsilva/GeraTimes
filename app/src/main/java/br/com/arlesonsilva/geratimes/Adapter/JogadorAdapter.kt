package br.com.arlesonsilva.geratimes.Adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import br.com.arlesonsilva.geratimes.Acitivity.RachaNavigationActivity
import br.com.arlesonsilva.geratimes.DBHelper.database
import br.com.arlesonsilva.geratimes.Fragment.JogadoresFragment
import br.com.arlesonsilva.geratimes.Model.Jogador
import br.com.arlesonsilva.geratimes.R
import org.jetbrains.anko.*
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.update
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange

class JogadorAdapter (private val context: Context, private val dataSource: ArrayList<Jogador>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = inflater.inflate(R.layout.list_item_jogador, parent, false)

        val jogador = getItem(position) as Jogador
        val nome = view.findViewById(R.id.nome) as TextView
        val pago = view.findViewById(R.id.pago) as Switch
        val goleiro = view.findViewById(R.id.goleiro) as Switch
        val rachaId = jogador.racha_id

        if (jogador.goleiro) {
            pago.visibility = View.INVISIBLE
            goleiro.visibility = View.VISIBLE
        }else {
            pago.visibility = View.VISIBLE
            goleiro.visibility = View.INVISIBLE
        }

        nome.text = jogador.nome
        pago.isChecked = jogador.pago
        goleiro.isChecked = jogador.goleiro

        pago.onCheckedChange { _, _ ->
            updateJogadorDB(
                jogador.id,
                jogador.nome,
                pago.isChecked,
                goleiro.isChecked,
                rachaId
            )
        }

        goleiro.onCheckedChange { _, _ ->
            updateJogadorDB(
                jogador.id,
                jogador.nome,
                pago.isChecked,
                goleiro.isChecked,
                rachaId
            )
        }

        view.setOnLongClickListener {
            val opcoes = listOf("Editar", "Excluir")
            val opc_editar = 0
            val opc_excluir = 1

            context.selector("Escolha uma opção", opcoes) { _, position ->
                when (position){
                    opc_editar -> {
                        dialogEditarJogador(jogador)
                    }
                    opc_excluir ->{
                        context.alert("Tem certeza que deseja excluir o jogador ${jogador.nome}?", "Atenção") {
                            yesButton {
                                deleteJogadorDB(jogador)
                            }
                            noButton {
                                //Ação caso escolheu a opção NAO
                            }
                        }.show()
                    }
                }
            }

            return@setOnLongClickListener true
        }

        return view!!
    }

    fun deleteJogadorDB(jogador: Jogador) {
        context.database.use {
            delete("tb_jogador", "id = {id}", "id" to jogador.id)
        }
        context.toast("Jogador ${jogador.nome} excluído com sucesso")
        selectJogadorDB()
    }

    fun dialogEditarJogador(jogador: Jogador) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = context.layoutInflater
        val view = inflater.inflate(R.layout.add_jogador_dialog, null)
        val nome = view.findViewById<EditText>(R.id.edtNome)
        val pago = view.findViewById<Switch>(R.id.swtPago)
        val goleiro = view.findViewById<Switch>(R.id.swtGoleiro)
        val rachaId = jogador.racha_id
        nome.setText(jogador.nome)
        pago.isChecked = jogador.pago
        goleiro.isChecked = jogador.goleiro
        goleiro.onCheckedChange { _, _ ->
            pago.isChecked = goleiro.isChecked
        }
        dialogBuilder.setTitle("Editar jogador")
        dialogBuilder.setView(view)
        dialogBuilder.setPositiveButton("Salvar") { _, _ ->
            if(nome.text.trim().isEmpty()) {
                context.toast("Campo nome obrigatório")
            }else {
                val jogador =
                    Jogador(
                        jogador.id,
                        nome.text.toString(),
                        goleiro.isChecked,
                        pago.isChecked,
                        rachaId
                    )
                updateJogadorDB(jogador.id,jogador.nome,jogador.pago,jogador.goleiro,jogador.racha_id)
            }
        }
        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            return@setNegativeButton
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    fun updateJogadorDB(id: Int, nome: String, pago: Boolean, goleiro: Boolean, idRacha: Int) {
        context.database.use {
            update("tb_jogador",
                "nome" to nome,
                "pago" to pago.toString(),
                "goleiro" to goleiro.toString())
                .whereSimple("id = ?", id.toString())
                .exec()
        }

        context.toast("Jogador ${nome} editado com sucesso")
        selectJogadorDB()
    }

    fun selectJogadorDB() {
        val listJogador = ArrayList<Jogador>()
        listJogador.clear()
        context.database.use {
            val cursor = rawQuery("SELECT * FROM tb_jogador ORDER BY nome", null)
            if (cursor.moveToFirst()) {
                do {
                    listJogador.add(
                        Jogador(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2)!!.toBoolean(),
                            cursor.getString(3)!!.toBoolean(),
                            cursor.getInt(4)
                        )
                    )
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        dataSource.clear()
        dataSource.addAll(listJogador)
        this.notifyDataSetChanged()
    }

}
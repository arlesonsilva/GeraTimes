package br.com.arlesonsilva.geratimes.Adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import br.com.arlesonsilva.geratimes.Acitivity.RachaActivity
import br.com.arlesonsilva.geratimes.Acitivity.RachaNavigationActivity
import br.com.arlesonsilva.geratimes.Model.Jogador
import br.com.arlesonsilva.geratimes.Model.Racha
import br.com.arlesonsilva.geratimes.R
import br.com.hapvida.desospofflinehap.DBHelper.database
import org.jetbrains.anko.*
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.update
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange

class RachaAdapter (private val context: Context, private val dataSource: ArrayList<Racha>): BaseAdapter() {

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

        val view = inflater.inflate(R.layout.list_item_racha, parent, false)
        val nome = view.findViewById(R.id.nome) as TextView
        val status = view.findViewById(R.id.pago) as Switch
        val racha = getItem(position) as Racha

        nome.text = racha.nome
        status.isChecked = racha.status

        status.onCheckedChange { buttonView, isChecked ->
            updateRachaDB(
                racha.id,
                status.isChecked
            )
        }

        view.setOnClickListener {
            if (status.isChecked) {
                context.startActivity<RachaNavigationActivity>("idRacha" to racha.id)
            }else {
                context.toast("Racha inativo")
            }
        }

        view.setOnLongClickListener {
            val opcoes = listOf("Editar", "Excluir")
            val opc_editar = 0
            val opc_excluir = 1

            context.selector("Escolha uma opção", opcoes) { dialogInterface, position ->
                when (position){
                    opc_editar -> {
                        dialogEditRacha(racha)
                    }
                    opc_excluir ->{
                        context.alert("Tem certeza que deseja excluir o racha ${racha.nome}?", "Atenção"){
                            yesButton {
                                deleteRachaDB(racha)
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

        return view
    }

    fun dialogEditRacha(racha: Racha) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = context.layoutInflater
        val view = inflater.inflate(R.layout.add_racha_dialog, null)
        var nome = view.findViewById<EditText>(R.id.edtNome)
        var status = view.findViewById<Switch>(R.id.swtStatus)
        var numero = view.findViewById<EditText>(R.id.edtNJogadores)

        dialogBuilder.setTitle("Adicionar racha")
        dialogBuilder.setView(view)

        nome.setText(racha.nome)
        status.isChecked = racha.status
        numero.setText(racha.nu_jogadores_time.toString())

        dialogBuilder.setPositiveButton("Salvar") { _, _ ->
            if(nome.text.trim().isEmpty()) {
                context.toast("Campo nome obrigatório")
            }else if (numero.length() == 0) {
                context.toast("Campo número de jogadores obrigatório")
            }else if (numero.text.toString().toInt() == 0) {
                context.toast("Campo número de jogadores deve ser maior que 0")
            }else {
                editRachaDB(
                    Racha(
                        racha.id,
                        nome.text.toString(),
                        status.isChecked,
                        numero.text.toString().toInt()
                    )
                )
            }
        }

        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            return@setNegativeButton
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    fun editRachaDB(racha: Racha) {
        context.database.use {
            update("tb_racha",
                "nome" to racha.nome,
                "status" to racha.status.toString(),
                "jogadores_por_time" to racha.nu_jogadores_time)
                    .whereSimple("id = ?", racha.id.toString())
                    .exec()
        }
        context.toast("Racha ${racha.nome} editado com sucesso")
        selectRachaDB()
    }

    fun selectRachaDB() {
        val listRacha = ArrayList<Racha>()
        listRacha.clear()
        context.database.use {
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
            }
            cursor.close()
        }
        dataSource.clear()
        dataSource.addAll(listRacha)
        this.notifyDataSetChanged()
    }

    fun deleteRachaDB(racha: Racha) {
        context.database.use {
            delete("tb_racha", "id = {id}", "id" to racha.id)
            delete("tb_jogador","racha_id = {id}","id" to racha.id)
            delete("tb_time","racha_id = {id}","id" to racha.id)
            delete("tb_jogador_time","racha_id = {id}","id" to racha.id)
        }
        context.toast("Racha ${racha.nome} excluído com sucesso")
        selectRachaDB()
    }

    fun updateRachaDB(id: Int, status: Boolean) {
        context.database.use {
            update("tb_racha", "status" to status.toString())
                .whereSimple("id = ?", id.toString())
                .exec()
        }
    }

}
package br.com.arlesonsilva.geratimes.Adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import br.com.arlesonsilva.geratimes.Acitivity.RachaNavigationActivity
import br.com.arlesonsilva.geratimes.DBHelper.database
import br.com.arlesonsilva.geratimes.Model.Racha
import br.com.arlesonsilva.geratimes.R
import org.jetbrains.anko.*
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.update
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import java.util.*
import kotlin.collections.ArrayList
import android.view.inputmethod.InputMethodManager

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
        val horario = view.findViewById(R.id.horario) as TextView
        val dSemana = view.findViewById(R.id.dSemana) as TextView
        val status = view.findViewById(R.id.pago) as Switch
        val racha = getItem(position) as Racha

        nome.text = racha.nome
        horario.text = racha.horario
        status.isChecked = racha.status
        dSemana.text = racha.dia_semana
            .replace("[", "")
            .replace("]", "")
            .replace("1", "Dom")
            .replace("2", "Seg")
            .replace("3", "Ter")
            .replace("4", "Qua")
            .replace("5", "Qui")
            .replace("6", "Sex")
            .replace("7", "Sáb")

        status.onCheckedChange { _, _ ->
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

            context.selector("Escolha uma opção", opcoes) { _, position ->
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

    private fun closeKeyboard(editText: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    private fun dialogEditRacha(racha: Racha) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = context.layoutInflater
        val view = inflater.inflate(R.layout.add_racha_dialog, null)
        val nome = view.findViewById<EditText>(R.id.edtNome)
        val status = view.findViewById<Switch>(R.id.swtStatus)
        val numero = view.findViewById<EditText>(R.id.edtNJogadores)
        val horario = view.findViewById<EditText>(R.id.edtHorario)
        val segunda = view.findViewById<CheckBox>(R.id.cbSegunda)
        val terca = view.findViewById<CheckBox>(R.id.cbTerca)
        val quarta = view.findViewById<CheckBox>(R.id.cbQuarta)
        val quinta = view.findViewById<CheckBox>(R.id.cbQuinta)
        val sexta = view.findViewById<CheckBox>(R.id.cbSexta)
        val sabado = view.findViewById<CheckBox>(R.id.cbSabado)
        val domingo = view.findViewById<CheckBox>(R.id.cbDomingo)
        val calendar = Calendar.getInstance()
        val dSemana = ArrayList<String>()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        dialogBuilder.setTitle("Adicionar racha")
        dialogBuilder.setView(view)
        dialogBuilder.setPositiveButton("Salvar",null)
        dialogBuilder.setNegativeButton("Cancelar", null)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val cancelButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        nome.setText(racha.nome)
        status.isChecked = racha.status
        numero.setText(racha.nu_jogadores_time.toString())
        horario.setText(racha.horario)

        domingo.isChecked = racha.dia_semana.contains("1")
        segunda.isChecked = racha.dia_semana.contains("2")
        terca.isChecked = racha.dia_semana.contains("3")
        quarta.isChecked = racha.dia_semana.contains("4")
        quinta.isChecked = racha.dia_semana.contains("5")
        sexta.isChecked = racha.dia_semana.contains("6")
        sabado.isChecked = racha.dia_semana.contains("7")

        dSemana.add(racha.dia_semana.replace("[","").replace("]",""))
        Log.i("dialogEditRacha",dSemana.toString())

        horario.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val timeSetListener = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, h, m ->
                    horario.setText("$h:$m")
                }, hour, minute, true)
                timeSetListener.show()
            }
            closeKeyboard(horario)
        }
        domingo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("1")
                Log.i("dialogEditRacha",dSemana.toString())
            }else {
                dSemana.remove("1")
                Log.i("dialogEditRacha",dSemana.toString())
            }
        }
        segunda.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("2")
                Log.i("dialogEditRacha",dSemana.toString())
            }else {
                dSemana.remove("2")
                Log.i("dialogEditRacha",dSemana.toString())
            }
        }
        terca.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("3")
                Log.i("dialogEditRacha",dSemana.toString())
            }else {
                dSemana.remove("3")
                Log.i("dialogEditRacha",dSemana.toString())
            }
        }
        quarta.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("4")
                Log.i("dialogEditRacha",dSemana.toString())
            }else {
                dSemana.remove("4")
                Log.i("dialogEditRacha",dSemana.toString())
            }
        }
        quinta.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("5")
                Log.i("dialogEditRacha",dSemana.toString())
            }else {
                dSemana.remove("5")
                Log.i("dialogEditRacha",dSemana.toString())
            }
        }
        sexta.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("6")
                Log.i("dialogEditRacha",dSemana.toString())
            }else {
                dSemana.remove("6")
                Log.i("dialogEditRacha",dSemana.toString())
            }
        }
        sabado.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("7")
                Log.i("dialogEditRacha",dSemana.toString())
            }else {
                dSemana.remove("7")
                Log.i("dialogEditRacha",dSemana.toString())
            }
        }

        positiveButton.setOnClickListener {
            when {
                nome.text.trim().isEmpty() -> context.toast("Campo nome obrigatório")
                numero.length() == 0 -> context.toast("Campo número de jogadores obrigatório")
                numero.text.toString().toInt() == 0 -> context.toast("Campo número de jogadores deve ser maior que 0")
                horario.text.isEmpty() -> context.toast("Campo horário do racha obrigatório")
                !segunda.isChecked &&
                        !terca.isChecked &&
                        !quarta.isChecked &&
                        !quinta.isChecked &&
                        !sexta.isChecked &&
                        !sabado.isChecked &&
                        !domingo.isChecked  -> context.toast("Campo dias da semana do racha obrigatório")
                else ->
                    editRachaDB(
                        Racha(
                            racha.id,
                            nome.text.toString(),
                            status.isChecked,
                            numero.text.toString().toInt(),
                            horario.text.toString(),
                            dSemana.toList().toString()
                        ),
                        alertDialog
                    )
            }
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

    }

    fun editRachaDB(racha: Racha, alertDialog: AlertDialog) {
        context.database.use {
            update("tb_racha",
                "nome" to racha.nome,
                "status" to racha.status.toString(),
                "jogadores_por_time" to racha.nu_jogadores_time,
                "horario" to racha.horario,
                "dia_semana" to racha.dia_semana)
                    .whereSimple("id = ?", racha.id.toString())
                    .exec()
        }
        alertDialog.dismiss()
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
                            cursor.getString(cursor.getColumnIndex("nome")),
                            cursor.getString(cursor.getColumnIndex("status"))!!.toBoolean(),
                            cursor.getInt(cursor.getColumnIndex("jogadores_por_time")),
                            cursor.getString(cursor.getColumnIndex("horario")),
                            cursor.getString(cursor.getColumnIndex("dia_semana"))
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
package br.com.arlesonsilva.geratimes.Adapter

import br.com.arlesonsilva.geratimes.Model.Confirmado
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import br.com.arlesonsilva.geratimes.R
import br.com.hapvida.desospofflinehap.DBHelper.database
import org.jetbrains.anko.db.update
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange

class ConfirmadoAdapter (private val context: Context, private val dataSource: ArrayList<Confirmado>): BaseAdapter() {

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

        val rowView = inflater.inflate(R.layout.list_item_confirmado, parent, false)

        val confirmado = getItem(position) as Confirmado
        val nome = rowView.findViewById(R.id.nome) as TextView
        //val checkBox = rowView.findViewById(R.id.confirmado) as CheckBox

        nome.text = confirmado.nome

//        checkBox.onCheckedChange { buttonView, isChecked ->
//            //if (!checkBox.isChecked) {
//                dialogDesconfirmarJogador(confirmado,checkBox)
//            //}
//        }

        return rowView
    }

    fun dialogDesconfirmarJogador(confirmado: Confirmado, checkBox: CheckBox) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("Atenção")
        dialogBuilder.setMessage("Se desconfirmar o jogador ${confirmado.nome} ele não irá partircipar do sorteio")
        dialogBuilder.setPositiveButton("Ok") { _, _ ->
            updateJogadorDB(
                confirmado.id,
                false
            )
            checkBox.isChecked = false
        }
        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            updateJogadorDB(
                confirmado.id,
                true
            )
            checkBox.isChecked = true
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    fun updateJogadorDB(id: Int, pago: Boolean) {
        context.database.use {
            update("tb_jogador", "pago" to pago.toString())
                .whereSimple("id = ?", id.toString())
                .exec()
        }
        this.notifyDataSetChanged()
    }

}
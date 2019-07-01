package br.com.arlesonsilva.geratimes.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Switch
import android.widget.TextView
import br.com.arlesonsilva.geratimes.Acitivity.RachaNavigationActivity
import br.com.arlesonsilva.geratimes.Fragment.JogadoresFragment
import br.com.arlesonsilva.geratimes.Model.Jogador
import br.com.arlesonsilva.geratimes.R
import br.com.hapvida.desospofflinehap.DBHelper.database
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

        val rowView = inflater.inflate(R.layout.list_item_jogador, parent, false)

        val jogador = getItem(position) as Jogador
        val nome = rowView.findViewById(R.id.nome) as TextView
        val pago = rowView.findViewById(R.id.pago) as Switch
        val goleiro = rowView.findViewById(R.id.goleiro) as Switch
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

        pago.onCheckedChange { buttonView, isChecked ->
            updateJogadorDB(
                jogador.id,
                pago.isChecked,
                goleiro.isChecked,
                rachaId
            )
        }

        goleiro.onCheckedChange { buttonView, isChecked ->
            updateJogadorDB(
                jogador.id,
                pago.isChecked,
                goleiro.isChecked,
                rachaId
            )
        }

        return rowView
    }

    fun updateJogadorDB(id: Int, pago: Boolean, goleiro: Boolean, idRacha: Int) {
        context.database.use {
            update("tb_jogador",
                "pago" to pago.toString(),
                "goleiro" to goleiro.toString())
                .whereSimple("id = ?", id.toString())
                .exec()
        }

        callFragment(JogadoresFragment(), idRacha)
    }

    fun callFragment(fragment: Fragment, idRacha: Int) {
        val bundle = Bundle()
        bundle.putInt("idRacha",idRacha)
        fragment.arguments = bundle
        val fm = (context as RachaNavigationActivity).supportFragmentManager
        val ft = fm!!.beginTransaction()
        ft.replace(R.id.fragment_content, fragment)
        ft.commit()
    }

}
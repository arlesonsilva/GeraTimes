package br.com.arlesonsilva.geratimes.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import br.com.arlesonsilva.geratimes.Model.Time
import br.com.arlesonsilva.geratimes.R

class JogadorTimeAdapter (private val context: Context, private val dataSource: ArrayList<Time>): BaseAdapter() {

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

        val rowView = inflater.inflate(R.layout.list_item_time, parent, false)

        val time = getItem(position) as Time
        val idTime = time.id
        val nome = rowView.findViewById(R.id.nome) as TextView
        val jogador = time.jogador.filter { time.id == idTime }
        var nomejogadores = ""

        for (j in jogador) {
            nomejogadores = nomejogadores + "\n" + j.nome
        }

        nome.text = time.nome + "\n" + nomejogadores

        return rowView
    }

}
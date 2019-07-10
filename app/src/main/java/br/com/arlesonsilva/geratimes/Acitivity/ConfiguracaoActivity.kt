package br.com.arlesonsilva.geratimes.Acitivity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import br.com.arlesonsilva.geratimes.R
import android.view.MenuItem
import android.widget.CheckBox
import br.com.arlesonsilva.geratimes.DBHelper.database
import br.com.arlesonsilva.geratimes.Model.Configuracao
import org.jetbrains.anko.db.*
import org.jetbrains.anko.toast

class ConfiguracaoActivity : AppCompatActivity() {

    private var inclusao: CheckBox? = null
    private var timeA: CheckBox? = null
    private var configuracoes = ArrayList<Configuracao>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracao)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //createTableConfig()

        inclusao = findViewById(R.id.check_inclusao)
        timeA = findViewById(R.id.check_timeA)

        selectConfiguracoes()

        for (c in configuracoes) {
            when (c.id) {
                inclusao!!.tag.toString().toInt() -> {
                    inclusao!!.isChecked = c.ativo
                }
                timeA!!.tag.toString().toInt() -> {
                    timeA!!.isChecked = c.ativo
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_action_config, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_save -> {
                capturaConfig()
                insertConfiguracoesDB(configuracoes)
                return true
            }
        }
        return super.onOptionsItemSelected(item)

    }

    private fun capturaConfig() {
        configuracoes.clear()
        configuracoes.add(
            Configuracao(
                inclusao!!.tag.toString().toInt(),
                inclusao!!.text as String,
                inclusao!!.isChecked
            )
        )
        configuracoes.add(
            Configuracao(
                timeA!!.tag.toString().toInt(),
                timeA!!.text as String,
                timeA!!.isChecked
            )
        )
    }

    private fun selectConfiguracoes() {
        configuracoes.clear()
        database.use {
            val cursor = rawQuery("SELECT * FROM tb_configuracao ORDER BY id ASC", null)
            if (cursor.moveToFirst()) {
                do {
                    configuracoes.add(
                        Configuracao(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2)!!.toBoolean()
                        )
                    )
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    private fun insertConfiguracoesDB(configuracao: ArrayList<Configuracao>) {
        for (c in configuracao) {
            database.use {
                val cursor = rawQuery("SELECT * FROM tb_configuracao WHERE id = ?", arrayOf(c.id.toString()))
                if (cursor.moveToFirst()) {
                    database.use {
                        update("tb_configuracao",
                            "ativo" to c.ativo.toString())
                            .whereSimple("id = ?", c.id.toString())
                            .exec()
                    }
                }else {
                    Log.i("insertConfiguracoesDB", "insiro")
                    database.use {
                        insert("tb_configuracao",
                            "item" to c.item,
                            "ativo" to c.ativo.toString()
                        )
                    }
                }
                cursor.close()
            }
        }
        toast("Configurações salvo com sucesso")
        selectConfiguracoes()
    }

}

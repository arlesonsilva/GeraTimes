package br.com.arlesonsilva.geratimes.Acitivity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
    private var inclusao: Boolean = false
    private val PERMISSION_RESULT_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_racha)

        empty = findViewById(android.R.id.empty)
        btnAddRacha = findViewById<FloatingActionButton>(R.id.add_racha)
        listView = findViewById(R.id.list_rachas)
        adapter = RachaAdapter(this@RachaActivity, listRacha)

        btnAddRacha!!.setOnClickListener {
            dialogAddRacha()
        }

        checkPermissions()
    }

    override fun onResume() {
        super.onResume()

        selectRachaDB()
        selectInclusaoDB()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_RESULT_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    // Permissão foi concedida, já é possível usufruir da funcionalidade
                } else if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
                        !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        TODO("VERSION.SDK_INT < M")
                        return
                    }
                ) {
                    // Usuário marcou a caixa "não perguntar novamente"
                    // Mostrar uma dialog explicando a importância do app ter acesso a funcionalidade
                    showAlertConfig()
                } else {
                    // Usuário negou acesso à permissão
                    // Bloquear trecho que utilizava a funcionalidade ou informar o usuário da necessidade de ter acesso à funcionalidade
                    showAlertPermission()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity<ConfiguracaoActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkPermissions() {
        ActivityCompat.requestPermissions(
            this@RachaActivity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_RESULT_CODE)
    }

    private fun showAlertPermission() {
        AlertDialog.Builder(this@RachaActivity).apply {
            setTitle("Precisa de permissão")
            setMessage("Algumas permissões são necessárias para executar tarefas no app Gera Times.")
            setPositiveButton("Sim") { d, i ->
                // Se o usuário quiser, requere novamente permissão à funcionalidade
                ActivityCompat.requestPermissions(
                    this@RachaActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_RESULT_CODE
                )
            }
            setNegativeButton("Não") { d, i -> d.dismiss() }
        }.show()
    }

    private fun showAlertConfig() {
        AlertDialog.Builder(this).apply {
            setTitle("Precisa de permissão")
            setMessage("Algumas permissões são necessárias para executar tarefas no app Gera Times, é preciso que acesse as configurações do sistema e conceda as permissões necessárias.")
            setPositiveButton("Ir para as configurações") { d, i ->
                // Cria intent para a tela de detalhes do app onde é possível o usuário conceder permissão à funcionalidade
                val appSettings = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", this@RachaActivity.packageName, null)
                }
                context.startActivity(appSettings)
            }
            setNegativeButton("Não") { d, i -> d.dismiss() }
        }.show()
    }

    private fun selectRachaDB() {
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

    private fun selectInclusaoDB() {
        database.use {
            val cursor = rawQuery("SELECT * FROM tb_configuracao WHERE id = 1", null)
            if (cursor.moveToFirst()) {
                do {
                    inclusao = cursor.getString(2)!!.toBoolean()
                    Log.i("selectInclusaoDB",cursor.getString(1) + inclusao)
                } while (cursor.moveToNext())
            }
        }
    }

    private fun insertRachaDB(nome: String, status: Boolean, numero: String) {
        database.use {
            insert("tb_racha",
                "nome" to nome,
                "status" to status.toString(),
                "jogadores_por_time" to numero
            )
        }
        selectRachaDB()
        if (inclusao) {
            dialogAddRacha()
        }else {
            toast("Racha ${nome} inserido com sucesso")
        }
    }

    private fun dialogAddRacha() {
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


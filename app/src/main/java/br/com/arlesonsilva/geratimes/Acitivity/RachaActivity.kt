package br.com.arlesonsilva.geratimes.Acitivity

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import br.com.arlesonsilva.geratimes.Adapter.RachaAdapter
import br.com.arlesonsilva.geratimes.DBHelper.database
import br.com.arlesonsilva.geratimes.Model.Racha
import br.com.arlesonsilva.geratimes.R
import org.jetbrains.anko.db.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList

class RachaActivity : AppCompatActivity() {

    private var btnAddRacha: Button? = null
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
        btnAddRacha = findViewById(R.id.add_racha)
        listView = findViewById(R.id.list_rachas)
        adapter = RachaAdapter(this@RachaActivity, listRacha)

        btnAddRacha!!.setOnClickListener {
            dialogAddRacha()
        }

        createTableConfig()
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
            R.id.action_alarm -> {
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                startActivity(intent)
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
            setPositiveButton("Sim") { _, _ ->
                // Se o usuário quiser, requere novamente permissão à funcionalidade
                ActivityCompat.requestPermissions(
                    this@RachaActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_RESULT_CODE
                )
            }
            setNegativeButton("Não") { d, _ -> d.dismiss() }
        }.show()
    }

    private fun showAlertConfig() {
        AlertDialog.Builder(this).apply {
            setTitle("Precisa de permissão")
            setMessage("Algumas permissões são necessárias para executar tarefas no app Gera Times, é preciso que acesse as configurações do sistema e conceda as permissões necessárias.")
            setPositiveButton("Ir para as configurações") { _, _ ->
                // Cria intent para a tela de detalhes do app onde é possível o usuário conceder permissão à funcionalidade
                val appSettings = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", this@RachaActivity.packageName, null)
                }
                context.startActivity(appSettings)
            }
            setNegativeButton("Não") { d, _ -> d.dismiss() }
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
                            cursor.getString(cursor.getColumnIndex("nome")),
                            cursor.getString(cursor.getColumnIndex("status"))!!.toBoolean(),
                            cursor.getInt(cursor.getColumnIndex("jogadores_por_time")),
                            cursor.getString(cursor.getColumnIndex("horario")),
                            cursor.getString(cursor.getColumnIndex("dia_semana"))
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
                } while (cursor.moveToNext())
            }
        }
    }

    private fun createTableConfig() {
        database.use {
            createTable(
                "tb_configuracao", true,
                "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                "item" to TEXT,
                "ativo" to TEXT
            )
        }
    }

    private fun insertRachaDB(
        nome: String,
        horario: String,
        dSemana: String,
        status: Boolean,
        numero: String,
        alertDialog: AlertDialog) {
        database.use {
            insert("tb_racha",
                "nome" to nome,
                "horario" to horario,
                "dia_semana" to dSemana,
                "status" to status.toString(),
                "jogadores_por_time" to numero
            )
        }
        val str = horario
        val delimiter = ":"
        val parts = str.split(delimiter)
        val hora = parts[0].toInt()
        val minuto = parts[1].toInt()
        createAlarm("Alarme racha ${nome}",hora, minuto, dSemana)
        selectRachaDB()
        alertDialog.dismiss()
        //if (inclusao) {
            //dialogAddRacha()
        //}else {
            toast("Racha ${nome} inserido com sucesso")
        //}
    }

    private fun createAlarm(message: String, hour: Int, minutes: Int, dSemana: String) {
        val dias = dSemana
            .replace("[", "")
            .replace("]", "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val intent= Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                .putExtra(AlarmClock.EXTRA_DAYS, arrayListOf(dias.toInt()))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        } else {
            val intent= Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    private fun dialogAddRacha() {
        val dialogBuilder = AlertDialog.Builder(this@RachaActivity)
        val inflater = this.layoutInflater
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

        horario.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val timeSetListener = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, h, m ->
                    horario.setText("$h:$m")
                }, hour, minute, true)
                timeSetListener.show()
            }
        }
        domingo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("1")
            }else {
                dSemana.remove("1")
            }
        }
        segunda.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("2")
            }else {
                dSemana.remove("2")
            }
        }
        terca.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("3")
            }else {
                dSemana.remove("3")
            }
        }
        quarta.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("4")
            }else {
                dSemana.remove("4")
            }
        }
        quinta.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("5")
            }else {
                dSemana.remove("5")
            }
        }
        sexta.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("6")
            }else {
                dSemana.remove("6")
            }
        }
        sabado.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dSemana.add("7")
            }else {
                dSemana.remove("7")
            }
        }

        positiveButton.setOnClickListener {
            when {
                nome.text.isEmpty() -> toast("Campo nome obrigatório")
                numero.length() == 0 -> toast( "Campo número de jogadores obrigatório")
                numero.text.toString().toInt() == 0 -> toast("Campo número de jogadores deve ser maior que 0")
                horario.text.isEmpty() -> toast("Campo horário do racha obrigatório")
                !segunda.isChecked &&
                    !terca.isChecked &&
                    !quarta.isChecked &&
                    !quinta.isChecked &&
                    !sexta.isChecked &&
                    !sabado.isChecked &&
                    !domingo.isChecked  -> toast("Campo dias da semana do racha obrigatório")

                else -> insertRachaDB(
                    nome.text.toString(),
                    horario.text.toString(),
                    dSemana.toList().toString(),
                    status.isChecked,
                    numero.text.toString(),
                    alertDialog
                )
            }
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

    }

}


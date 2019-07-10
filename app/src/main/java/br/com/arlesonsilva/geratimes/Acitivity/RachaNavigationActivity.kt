package br.com.arlesonsilva.geratimes.Acitivity

import android.app.AlertDialog
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import br.com.arlesonsilva.geratimes.DBHelper.database
import br.com.arlesonsilva.geratimes.Fragment.EmpateFragment
import br.com.arlesonsilva.geratimes.Fragment.JogadoresFragment
import br.com.arlesonsilva.geratimes.Fragment.SorteioFragment
import br.com.arlesonsilva.geratimes.Model.Racha
import br.com.arlesonsilva.geratimes.R
import org.jetbrains.anko.db.update

class RachaNavigationActivity : AppCompatActivity() {

    var idRacha: Int = 0
    private var fm: FragmentManager? =  null
    private var ft: FragmentTransaction? =  null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_racha_navigation)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        idRacha = intent.getIntExtra("idRacha",0)
        selectRachaDB(idRacha)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val f= supportFragmentManager.findFragmentById(R.id.fragment_content)
        if (f == null) {
            callFragment(JogadoresFragment())
        }

    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_jogadores -> {
                callFragment(JogadoresFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_soteio_times -> {
                callFragment(SorteioFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_empate_times -> {
                callFragment(EmpateFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onBackPressed() {
        database.use {
            val cursor = rawQuery("SELECT * FROM tb_jogador WHERE racha_id = ? AND pago = 'true' AND goleiro = 'false'", arrayOf(idRacha.toString()))
            if (cursor.count > 0) {
                dialogZerarJogadorPago()
            } else {
                finish()
            }
        }
        return
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun dialogZerarJogadorPago() {
        val dialogBuilder = AlertDialog.Builder(this@RachaNavigationActivity)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.obs_restaurar_dialog, null)
        dialogBuilder.setView(view)
        dialogBuilder.setTitle("Atenção")
        dialogBuilder.setMessage("\nDeseja restaurar o status inicial do racha ${title}?")
        dialogBuilder.setPositiveButton("Sim") { _, _ ->
            updateJogadorPago()
            database.use { delete("tb_time",null,null) }
            database.use { delete("tb_jogador_time",null,null) }
            finish()
        }
        dialogBuilder.setNegativeButton("Não") { _, _ ->
            finish()
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun updateJogadorPago() {
        database.use {
            update("tb_jogador",
                "pago" to "false")
                .whereSimple("racha_id = ?", idRacha.toString())
                .exec()
        }
    }

    fun callFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putInt("idRacha",idRacha)
        fragment.arguments = bundle
        fm = supportFragmentManager
        ft = fm!!.beginTransaction()
        ft!!.replace(R.id.fragment_content, fragment)
        ft!!.commit()
    }

    fun selectRachaDB(id: Int) {
        database.use {
            val cursor = rawQuery("SELECT * FROM tb_racha WHERE id = ?", arrayOf(id.toString()))
            if (cursor.moveToFirst()) {
                do {
                    Racha(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2)!!.toBoolean(),
                        cursor.getInt(3)
                    )
                    title = cursor.getString(1)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

}

package br.com.arlesonsilva.geratimes.Fragment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import br.com.arlesonsilva.geratimes.Model.Racha
import br.com.arlesonsilva.geratimes.R
import br.com.arlesonsilva.geratimes.Utils.DateTimeUtils
import br.com.arlesonsilva.geratimes.Utils.ScreenshotUtils
import br.com.hapvida.desospofflinehap.DBHelper.database
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.yesButton
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class EmpateFragment : Fragment() {

    private var dateAtual: String? = null
    private var horaAtual: String? = null
    private var nomeRacha: String?= null
    private var rootContent: ConstraintLayout? = null
    private var spnTime1: Spinner? = null
    private var spnTime2: Spinner? = null
    private var tgbTime1: ToggleButton? = null
    private var tgbTime2: ToggleButton? = null
    private var time1: String? = null
    private var time2: String? = null
    private var tvtResultado: TextView? = null
    private var btnSortear: FloatingActionButton? = null
    private var imgResultado: ImageView? = null
    private var adapter1: ArrayAdapter<String>? = null
    private var adapter2: ArrayAdapter<String>? = null
    private var imageCara: Drawable? = null
    private var imageCoroa: Drawable? = null
    private var idRacha: Int = 0
    private var list_times1 = ArrayList<String>()
    private var list_times2 = ArrayList<String>()
    private var FULL: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_empate, container, false)

        idRacha = arguments!!.getInt("idRacha")
        dateAtual = DateTimeUtils().dateAtual()
        rootContent = view.findViewById(R.id.root_content)
        spnTime1 = view.findViewById(R.id.spn_time1)
        spnTime2 = view.findViewById(R.id.spn_time2)
        tgbTime1 = view.findViewById(R.id.tgb_time1)
        tgbTime2 = view.findViewById(R.id.tgb_time2)
        tvtResultado = view.findViewById(R.id.txv_resultado)
        btnSortear = view.findViewById(R.id.btn_play)
        imgResultado = view.findViewById(R.id.img_resultado)
        imageCara = resources.getDrawable(R.drawable.ic_tag_faces_black_24dp)
        imageCoroa = resources.getDrawable(R.drawable.ic_attach_money_black_24dp)

        adapter1 = ArrayAdapter(context, android.R.layout.simple_spinner_item, list_times1)
        adapter1!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnTime1!!.adapter = adapter1

        adapter2 = ArrayAdapter(context, android.R.layout.simple_spinner_item, list_times2)
        adapter2!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnTime2!!.adapter = adapter2

        spnTime1!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                time1 = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>){
            }
        }

        spnTime2!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                time2 = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>){
            }
        }

        tgbTime1!!.onCheckedChange { buttonView, isChecked ->
            tgbTime2!!.isChecked = !tgbTime1!!.isChecked
            if (isChecked) {
                tgbTime1!!.setCompoundDrawablesWithIntrinsicBounds(null, imageCara, null, null)
            }else {
                tgbTime1!!.setCompoundDrawablesWithIntrinsicBounds(null, imageCoroa, null, null)
            }
        }

        tgbTime2!!.onCheckedChange { buttonView, isChecked ->
            tgbTime1!!.isChecked = !tgbTime2!!.isChecked
            if (isChecked) {
                tgbTime2!!.setCompoundDrawablesWithIntrinsicBounds(null, imageCara, null, null)
            }else {
                tgbTime2!!.setCompoundDrawablesWithIntrinsicBounds(null, imageCoroa, null, null)
            }
        }

        btnSortear!!.setOnClickListener {
            when {
                tvtResultado!!.text.isNotEmpty() -> {
                    alert("Cara ou Coroa já realizado, deseja realizar outro?", "Atenção") {
                        yesButton {

                            tgbTime1!!.isEnabled = true
                            tgbTime2!!.isEnabled = true
                            spnTime1!!.isEnabled = true
                            spnTime2!!.isEnabled = true
                            tvtResultado!!.text = ""
                            imgResultado!!.visibility = View.INVISIBLE
                            selectTimeDB()

                            spnTime1!!.setSelection(adapter1!!.getPosition("Selecione um time"))
                            spnTime2!!.setSelection(adapter2!!.getPosition("Selecione um time"))

                        }
                        noButton {}
                    }.show()
                    return@setOnClickListener
                }
                time1!! == "Selecione um time" -> {
                    toast("Selecione o primeiro time")
                    return@setOnClickListener
                }
                time2!! == "Selecione um time" -> {
                    toast("Selecione o outro time")
                    return@setOnClickListener
                }
                time1!! == time2!! -> {
                    toast("Os time não podem ser iguais")
                    return@setOnClickListener
                }
                else -> {

                    tgbTime1!!.isEnabled = false
                    tgbTime2!!.isEnabled = false
                    spnTime1!!.isEnabled = false
                    spnTime2!!.isEnabled = false

                    val numero = Random().nextInt(2)
                    val palpite1 = tgbTime1!!.isChecked
                    val palpite2 = tgbTime2!!.isChecked

                    if (numero == 0) {
                        imgResultado!!.setImageResource(R.drawable.cara)
                        imgResultado!!.visibility = View.VISIBLE
                    } else {
                        imgResultado!!.setImageResource(R.drawable.coroa)
                        imgResultado!!.visibility = View.VISIBLE
                    }

                    if (palpite1 && numero == 0) {
                        tvtResultado!!.text = "${time1} ganhou!"
                    }else if (!palpite1 && numero == 1) {
                        tvtResultado!!.text = "${time1} ganhou!"
                    }else if (palpite2 && numero == 0) {
                        tvtResultado!!.text = "${time2} ganhou!"
                    }else if (!palpite2 && numero == 1) {
                        tvtResultado!!.text = "${time2} ganhou!"
                    } else {
                        tvtResultado!!.text = "Não sei quem ganhou"
                    }

                    takeScreenshot(FULL)

                }
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        selectTimeDB()
        selectRachaDB(idRacha)
    }

    private fun selectTimeDB() {
        list_times1.clear()
        list_times2.clear()

        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_time WHERE racha_id = ?" , arrayOf(idRacha.toString()))
            list_times1.add(
                "Selecione um time"
            )
            list_times2.add(
                "Selecione um time"
            )
            if (cursor.moveToFirst()) {
                do {
                    list_times1.add(
                        cursor.getString(1).replace("- Começa com a bola","")
                    )
                    list_times2.add(
                        cursor.getString(1).replace("- Começa com a bola","")
                    )
                } while (cursor.moveToNext())
            }else {
                list_times1.clear()
                list_times2.clear()

                list_times1.add(
                    "Selecione um time"
                )
                list_times2.add(
                    "Selecione um time"
                )
            }
            cursor.close()
        }

        adapter1!!.notifyDataSetChanged()
        adapter2!!.notifyDataSetChanged()

    }

    private fun selectRachaDB(id: Int) {
        context!!.database.use {
            val cursor = rawQuery("SELECT * FROM tb_racha WHERE id = ?", arrayOf(id.toString()))
            if (cursor.moveToFirst()) {
                do {
                    Racha(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2)!!.toBoolean(),
                        cursor.getInt(3)
                    )
                    nomeRacha = cursor.getString(1)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    private fun dialogShareCaraCoroa(b: Bitmap,file: File) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.share_caracorao_dialog, null)
        val image = view.findViewById<ImageView>(R.id.resultado)
        val txt = tvtResultado!!.text
        image.setImageBitmap(b)

        dialogBuilder.setTitle("Compartilhar")
        dialogBuilder.setMessage("\n${txt}\nDeseja compartilhar o resultado?")
        dialogBuilder.setView(view)

        dialogBuilder.setPositiveButton("Compartilhar") { _, _ ->
            shareScreenshot(file)
        }
        dialogBuilder.setNegativeButton("Cancelar") { _, _ ->
            return@setNegativeButton
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun takeScreenshot(screenshotType: Int) {
        var b: Bitmap? = null

        when (screenshotType) {
            FULL ->
                b = ScreenshotUtils().getScreenShot(rootContent!!)
        }

        if (b != null) {
            val saveFile = ScreenshotUtils().getMainDirectoryName(context!!) //get the path to save screenshot
            val file = ScreenshotUtils().store(
                b,
                "screenshot$screenshotType.jpg",
                saveFile
            )
            dialogShareCaraCoroa(b,file)
        }else {
            toast(R.string.screenshot_take_failed)
        }
    }

    private fun shareScreenshot(file: File) {
        //val uri = Uri.fromFile(file)//Convert file path into Uri for sharing
        horaAtual = DateTimeUtils().horaAtual()
        val uri = FileProvider.getUriForFile(
            context!!,
            "br.com.arlesonsilva.geratimes.provider",
            file
        )
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "Resultado cara ou coroa entre os ${time1} x ${time2} do racha ${nomeRacha} no dia ${dateAtual} as ${horaAtual}")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)))
    }

}

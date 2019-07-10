package br.com.arlesonsilva.geratimes.DBHelper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

val Context.database: GeraTimesDatabase
    get() = GeraTimesDatabase.getInstance(applicationContext)


class GeraTimesDatabase(context: Context) : ManagedSQLiteOpenHelper(ctx = context , name = "geraTimes.db",  version = 1) {

    companion object {
        private var instance: GeraTimesDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): GeraTimesDatabase {
            if (instance == null) {
                instance = GeraTimesDatabase(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {

        db.createTable("tb_racha",true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE,
            "nome" to TEXT,
            "status" to TEXT,
            "jogadores_por_time" to INTEGER
        )

        db.createTable("tb_jogador",true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE,
            "nome" to TEXT,
            "goleiro" to TEXT,
            "pago" to TEXT,
            "racha_id" to INTEGER
        )

        db.createTable("tb_time",true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE,
            "nome" to TEXT,
            "data" to TEXT,
            "racha_id" to INTEGER
        )

        db.createTable("tb_jogador_time",true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE,
            "nome" to TEXT,
            "time_id" to INTEGER,
            "racha_id" to INTEGER
        )

        db.createTable("tb_configuracao",true,
            "id" to INTEGER + PRIMARY_KEY + UNIQUE,
            "item" to TEXT,
            "ativo" to TEXT
        )

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable("geraTimes.db", ifExists = true)
    }

}
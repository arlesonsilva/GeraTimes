package br.com.arlesonsilva.geratimes.Acitivity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import br.com.arlesonsilva.geratimes.R
import org.jetbrains.anko.startActivity

class SplashScreenActivity : AppCompatActivity() {

    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 3000

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            startActivity<RachaActivity>()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
    }

    public override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }

}

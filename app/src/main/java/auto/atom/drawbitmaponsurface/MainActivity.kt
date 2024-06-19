package auto.atom.drawbitmaponsurface

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var replyHandler: Handler
    private var serviceMessenger: Messenger? = null
    private var bound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            serviceMessenger = Messenger(service)
            bound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            serviceMessenger = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)
        surfaceView.holder.addCallback(this)

        replyHandler = Handler(Looper.getMainLooper(), Handler.Callback { msg ->
            when (msg.what) {
                MyService.MSG_UPDATE_UI -> {
                    val bitmap = msg.data.getParcelable<Bitmap>("bitmap")
                    drawBitmapOnSurface(bitmap)
                    true
                }
                else -> false
            }
        })

        findViewById<Button>(R.id.button).setOnClickListener {
            if (bound) {
                val msg = Message.obtain(null, MyService.MSG_DRAW_CANVAS)
                msg.replyTo = Messenger(replyHandler)
                serviceMessenger?.send(msg)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MyService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Surface created
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Surface changed
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Surface destroyed
    }

    private fun drawBitmapOnSurface(bitmap: Bitmap?) {
        val canvas = surfaceView.holder.lockCanvas()
        if (canvas != null && bitmap != null) {
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }
}
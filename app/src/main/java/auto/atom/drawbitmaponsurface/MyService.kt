package auto.atom.drawbitmaponsurface

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.*

class MyService : Service() {

    private lateinit var serviceHandler: Handler
    private lateinit var serviceLooper: Looper
    private val messenger = Messenger(IncomingHandler())

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        serviceLooper = thread.looper
        serviceHandler = Handler(serviceLooper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return messenger.binder
    }

    inner class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_DRAW_CANVAS -> {
                    val replyMessenger = msg.replyTo
                    val bitmap = createBitmapWithDrawing()
                    val bundle = Bundle().apply {
                        putParcelable("bitmap", bitmap)
                    }
                    val replyMsg = Message.obtain(null, MSG_UPDATE_UI).apply {
                        data = bundle
                    }
                    replyMessenger.send(replyMsg)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun createBitmapWithDrawing(): Bitmap {
        val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 5f
        }
        canvas.drawColor(Color.WHITE)
        canvas.drawLine(0f, 0f, 500f, 500f, paint)
        canvas.drawCircle(250f, 250f, 100f, paint)
        return bitmap
    }

    companion object {
        const val MSG_DRAW_CANVAS = 1
        const val MSG_UPDATE_UI = 2
    }
}
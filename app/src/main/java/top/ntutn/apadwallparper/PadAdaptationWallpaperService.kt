package top.ntutn.apadwallparper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.core.net.toUri

class PadAdaptationWallpaperService : WallpaperService() {
    companion object {
        const val ACTION_WALLPAPER_UPDATE = "top.ntutn.apadwallparper.update"
        const val KEY_BITMAP_H_URI = "bitmap_h_uri"
        const val KEY_BITMAP_V_URI = "bitmap_v_uri"
    }

    override fun onCreateEngine(): Engine? = PadWallpaperEngine(this)

    inner class PadWallpaperEngine(private val context: Context) : Engine() {
        private var bitmapH: Bitmap? = null
            set(value) {
                field = value
                if (value != null) {
                    bitmapHRect.set(0, 0, value.width, value.height)
                }
            }
        private var bitmapV: Bitmap? = null
            set(value) {
                field = value
                if (value != null) {
                    bitmapVRect.set(0, 0, value.width, value.height)
                }
            }

        private val bitmapHRect = Rect()
        private val bitmapVRect = Rect()
        private val drawingRect = Rect()
        private val wallpaperChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                refreshImage()
            }
        }

        @Volatile
        private var surfaceValid = false
        private lateinit var scope: CoroutineScope

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            scope = CoroutineScope(Dispatchers.Main.immediate)
            val filter = IntentFilter().also {
                it.addAction(ACTION_WALLPAPER_UPDATE)
            }
            ContextCompat.registerReceiver(
                context,
                wallpaperChangeReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        private suspend fun loadBitmap(resourceCode: Int? = null, uri: Uri? = null): Bitmap {
            require(resourceCode != null || uri != null)
            return suspendCoroutine<Bitmap> { continuation ->
                val callback = object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        continuation.resume(resource.copy(Bitmap.Config.ARGB_8888, false))
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                    }
                }
                if (resourceCode != null) {
                    Glide.with(context)
                        .asBitmap()
                        .load(resourceCode)
                        .into(callback)
                } else {
                    Glide.with(context)
                        .asBitmap()
                        .load(uri)
                        .into(callback)
                }
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            surfaceValid = true
        }


        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
//            if (width > height == desiredMinimumWidth > desiredMinimumHeight) {
//                drawingRect.set(0, 0, desiredMinimumWidth, desiredMinimumHeight)
//            } else {
//                drawingRect.set(0, 0, desiredMinimumHeight, desiredMinimumWidth)
//            }
            drawingRect.set(0, 0, width, height) // fixme 壁纸拉伸问题解决
            refreshImage()
        }

        override fun onDesiredSizeChanged(desiredWidth: Int, desiredHeight: Int) {
            super.onDesiredSizeChanged(desiredWidth, desiredHeight)
            refreshImage()
        }

        private fun refreshImage() {
            scope.launch {
                val hUri = MMKV.defaultMMKV().decodeString(KEY_BITMAP_H_URI)
                val vUri = MMKV.defaultMMKV().decodeString(KEY_BITMAP_V_URI)
                bitmapH = if (hUri != null && hUri.isNotBlank()) {
                    loadBitmap(uri = hUri.toUri())
                } else {
                    loadBitmap(resourceCode = R.drawable.image_h)
                }
                bitmapV = if (vUri != null && vUri.isNotBlank()) {
                    loadBitmap(uri = vUri.toUri())
                } else {
                    loadBitmap(resourceCode = R.drawable.image_v)
                }
                drawImage()
            }
        }

        private fun drawImage() {
            if (!surfaceValid) {
                return
            }
            if (bitmapV == null || bitmapH == null) {
                return
            }
            val holder = surfaceHolder ?: return
            val frameRect = holder.surfaceFrame
            if (frameRect.let { it.width() <= 0 || it.height() <= 0 }) {
                return
            }

            val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                holder.lockHardwareCanvas()
            } else {
                holder.lockCanvas()
            } ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                canvas.drawColor(Color.TRANSPARENT, BlendMode.CLEAR)
            } else {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            }
            if (frameRect.width() > frameRect.height()) {
                bitmapH?.let { canvas.drawBitmap(it, bitmapHRect, drawingRect, null) }
            } else {
                bitmapV?.let { canvas.drawBitmap(it, bitmapVRect, drawingRect , null) }
            }
            holder.unlockCanvasAndPost(canvas)
        }

        override fun onDestroy() {
            scope.cancel()
            surfaceValid = false
            context.unregisterReceiver(wallpaperChangeReceiver)
            super.onDestroy()
        }
    }
}
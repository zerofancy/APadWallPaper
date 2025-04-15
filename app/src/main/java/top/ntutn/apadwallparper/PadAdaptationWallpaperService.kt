package top.ntutn.apadwallparper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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
import kotlin.math.max

class PadAdaptationWallpaperService : WallpaperService() {
    companion object {
        const val ACTION_WALLPAPER_UPDATE = "top.ntutn.apadwallparper.update"
        const val KEY_BITMAP_H_URI = "bitmap_h_uri"
        const val KEY_BITMAP_V_URI = "bitmap_v_uri"
    }

    override fun onCreateEngine(): Engine? = PadWallpaperEngine(this)

    inner class PadWallpaperEngine(private val context: Context) : Engine() {
        private var bitmapH: Bitmap? = null
        private var bitmapV: Bitmap? = null

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
            setOffsetNotificationsEnabled(false)
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
            if (!surfaceValid || bitmapV == null || bitmapH == null) return

            val holder = surfaceHolder ?: return
            val frameRect = holder.surfaceFrame
            if (frameRect.width() <= 0 || frameRect.height() <= 0) return

            val canvas = holder.lockCanvas() ?: return
            try {
                // 清除画布
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                // 选择目标位图
                val targetBitmap = if (frameRect.width() > frameRect.height()) bitmapH else bitmapV
                targetBitmap?.let { bitmap ->
                    // 计算缩放比例
                    val scale = max(
                        frameRect.width().toFloat() / bitmap.width,
                        frameRect.height().toFloat() / bitmap.height
                    )

                    // 计算缩放后的尺寸
                    val scaledWidth = bitmap.width * scale
                    val scaledHeight = bitmap.height * scale

                    // 计算偏移量（居中显示）
                    val dx = (frameRect.width() - scaledWidth) / 2
                    val dy = (frameRect.height() - scaledHeight) / 2

                    // 创建缩放矩阵
                    val matrix = Matrix().apply {
                        postScale(scale, scale)
                        postTranslate(dx, dy)
                    }

                    // 绘制位图
                    canvas.drawBitmap(bitmap, matrix, null)
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }


        override fun onDestroy() {
            scope.cancel()
            surfaceValid = false
            context.unregisterReceiver(wallpaperChangeReceiver)
            super.onDestroy()
        }
    }
}
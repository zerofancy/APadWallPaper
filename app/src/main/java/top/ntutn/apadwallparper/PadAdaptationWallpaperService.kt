package top.ntutn.apadwallparper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class PadAdaptationWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine? = PadWallpaperEngine(this)

    inner class PadWallpaperEngine(private val context: Context) : Engine() {
        private var bitmapH: Bitmap? = null
        private var bitmapV: Bitmap? = null

        private val bitmapHRect = Rect()
        private val bitmapVRect = Rect()
        private val desiredRect = Rect()

        @Volatile
        private var surfaceValid = false

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            Glide.with(context)
                .asBitmap()
                .load(R.drawable.image_h)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        bitmapH = resource
                        bitmapHRect.set(0, 0, resource.width, resource.height)
                        drawImage()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        bitmapH = null
                    }
                })
            Glide.with(context)
                .asBitmap()
                .load(R.drawable.image_v)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        bitmapV = resource
                        bitmapVRect.set(0, 0, resource.width, resource.height)

                        drawImage()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        bitmapV = null
                    }
                })
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            surfaceValid = true
            drawImage()
            desiredRect.set(0, 0, desiredMinimumWidth, desiredMinimumHeight)
        }


        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            drawImage()
        }

        override fun onDesiredSizeChanged(desiredWidth: Int, desiredHeight: Int) {
            super.onDesiredSizeChanged(desiredWidth, desiredHeight)
            desiredRect.set(0, 0, desiredWidth, desiredHeight)
            drawImage()
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
                bitmapH?.let { canvas.drawBitmap(it, bitmapHRect, desiredRect, null) }
            } else {
                bitmapV?.let { canvas.drawBitmap(it, bitmapVRect, desiredRect , null) }
            }
            holder.unlockCanvasAndPost(canvas)
        }

        override fun onDestroy() {
            surfaceValid = false
            super.onDestroy()
        }
    }
}
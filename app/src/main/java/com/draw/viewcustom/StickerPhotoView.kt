package com.draw.viewcustom

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.draw.R
import kotlin.math.atan2
import kotlin.math.hypot

class StickerPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private lateinit var stickerImageView: AppCompatImageView
    private lateinit var borderView: RelativeLayout
    private lateinit var deleteButton: AppCompatImageView
    private lateinit var flipButton: AppCompatImageView
    private lateinit var transformButton: AppCompatImageView

    private var lastX = 0f
    private var lastY = 0f
    private var initialDistance = 0f
    private var initialRotation = 0f
    private var currentScale = 1f

    private var isTouchingSticker = false
    private val hideBorderHandler = Handler(Looper.getMainLooper())
    private val hideBorderRunnable = Runnable { borderView.isVisible = false }

    init {
        // Initialize the border view
        borderView = RelativeLayout(context).apply {
            background = createBorderDrawable()
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            isVisible = false
        }
        addView(borderView)
        initStickerView()
    }

    private fun createBorderDrawable(): ShapeDrawable {
        return ShapeDrawable(RectShape()).apply {
            paint.color = Color.DKGRAY
            paint.strokeWidth = 5f // Đổi độ dày đường viền nếu cần
            paint.style = Paint.Style.STROKE
        }
    }

    private fun initStickerView() {
        stickerImageView = AppCompatImageView(context).apply {
            // Đặt hình ảnh cho ImageView
            setImageResource(R.drawable.img_freestyle) // Thay bằng hình ảnh bạn muốn
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE // Tùy chỉnh cách hình ảnh hiển thị trong view
        }
        addView(stickerImageView)

        // Các nút điều khiển (xóa, lật, thay đổi kích thước)
        deleteButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_delete) // Thay bằng icon bạn muốn
            layoutParams = LayoutParams(30, 30).apply {
                addRule(ALIGN_PARENT_TOP, TRUE)
                addRule(ALIGN_PARENT_END, TRUE)
            }
            setOnClickListener { removeSticker() }
        }
        borderView.addView(deleteButton)

        flipButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_flip) // Thay bằng icon bạn muốn
            layoutParams = LayoutParams(30, 30).apply {
                addRule(ALIGN_PARENT_TOP, TRUE)
                addRule(CENTER_HORIZONTAL, TRUE)
            }
            setOnClickListener { flipSticker() }
        }
        borderView.addView(flipButton)

        transformButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_resize) // Thay bằng icon bạn muốn
            layoutParams = LayoutParams(30, 30).apply {
                addRule(ALIGN_PARENT_BOTTOM, TRUE)
                addRule(ALIGN_PARENT_END, TRUE)
            }
            setOnTouchListener { _, event -> handleTransform(event) }
        }
        borderView.addView(transformButton)
    }

    private fun flipSticker() {
        stickerImageView.scaleX *= -1 // Lật ngang ảnh
    }

    private fun removeSticker() {
        this.visibility = View.GONE
    }

    private fun handleTransform(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialRotation = getAngle(event.rawX, event.rawY)
                lastX = event.rawX
                lastY = event.rawY
                initialDistance = getDistance(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val newDistance = getDistance(event)
                val scaleFactor = newDistance / initialDistance
                if (scaleFactor > 0.5f && scaleFactor < 2f) {
                    currentScale = scaleFactor
                    stickerImageView.scaleX = currentScale
                    stickerImageView.scaleY = currentScale
                    updateBorderSize()
                }

                val newAngle = getAngle(event.rawX, event.rawY)
                val rotationDelta = newAngle - initialRotation
                stickerImageView.rotation += rotationDelta
                initialRotation = newAngle

                lastX = event.rawX
                lastY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
            }
        }
        return true
    }

    private fun getDistance(event: MotionEvent): Float {
        val dx = event.rawX - stickerImageView.x - (stickerImageView.width * stickerImageView.scaleX / 2)
        val dy = event.rawY - stickerImageView.y - (stickerImageView.height * stickerImageView.scaleY / 2)
        return hypot(dx.toDouble(), dy.toDouble()).toFloat()
    }

    private fun getAngle(x: Float, y: Float): Float {
        val dx = x - stickerImageView.x
        val dy = y - stickerImageView.y
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    private fun updateBorderSize() {
        // Cập nhật kích thước viền khi thay đổi kích thước hoặc xoay
        borderView.layoutParams = LayoutParams(
            (stickerImageView.width * stickerImageView.scaleX).toInt(),
            (stickerImageView.height * stickerImageView.scaleY).toInt()
        ).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        }
    }
}

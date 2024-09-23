package com.draw.viewcustom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.draw.R
import kotlin.math.atan2
import kotlin.math.hypot

class StickerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private var text: String = "Sticker"
    private lateinit var stickerTextView: TextView
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
        stickerTextView = TextView(context).apply {
            text = this@StickerTextView.text // Sử dụng giá trị của thuộc tính text
            textSize = 24f // Kích thước chữ
            setTextColor(Color.BLACK) // Màu chữ
            setBackgroundColor(Color.TRANSPARENT) // Nền trong suốt
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            gravity = Gravity.CENTER
        }
        addView(stickerTextView)

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

    fun updateText(newText: String) {
        text = newText
        stickerTextView.text = newText
        updateBorderSize() // Cập nhật kích thước khung viền khi thay đổi nội dung
    }

    private fun updateBorderSize() {
        val textWidth = stickerTextView.paint.measureText(stickerTextView.text.toString())
        val textHeight = stickerTextView.paint.fontMetrics.run { bottom - top }
        val padding = 15 // Khoảng cách 15dp mỗi bên

        val newWidth = (textWidth * stickerTextView.scaleX + padding).toInt()
        val newHeight = (textHeight * stickerTextView.scaleY + padding).toInt()

        borderView.layoutParams = LayoutParams(newWidth, newHeight).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        }

        // Cập nhật vị trí của các nút điều chỉnh
        updateButtonPositions()
        applyTransformToBorder()
    }

    private fun updateButtonPositions() {
        val buttonSize = 30
        val borderPadding = -5 // Khoảng cách âm từ viền

        deleteButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_TOP, TRUE)
            addRule(ALIGN_PARENT_END, TRUE)
            setMargins(borderPadding, borderPadding, borderPadding, borderPadding) // Đặt margin âm để nút nằm trên viền
        }

        flipButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_TOP, TRUE)
            addRule(CENTER_HORIZONTAL, TRUE)
            setMargins(0, borderPadding, 0, borderPadding) // Đặt margin âm để nút nằm trên viền
        }

        transformButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_BOTTOM, TRUE)
            addRule(ALIGN_PARENT_END, TRUE)
            setMargins(borderPadding, 0, borderPadding, borderPadding) // Đặt margin âm để nút nằm trên viền
        }
    }

    private fun applyTransformToBorder() {
        borderView.pivotX = stickerTextView.width * stickerTextView.scaleX / 2
        borderView.pivotY = stickerTextView.height * stickerTextView.scaleY / 2
        borderView.rotation = stickerTextView.rotation
        borderView.scaleX = stickerTextView.scaleX
        borderView.scaleY = stickerTextView.scaleY
    }

    private fun flipSticker() {
        stickerTextView.scaleX *= -1
        updateBorderSize() // Cập nhật kích thước khung viền khi lật
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
                    stickerTextView.scaleX = currentScale
                    stickerTextView.scaleY = currentScale
                    updateBorderSize() // Cập nhật kích thước khung viền khi thay đổi kích thước
                }

                val newAngle = getAngle(event.rawX, event.rawY)
                val rotationDelta = newAngle - initialRotation
                stickerTextView.rotation += rotationDelta
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
        val dx = event.rawX - stickerTextView.x - (stickerTextView.width * stickerTextView.scaleX / 2)
        val dy = event.rawY - stickerTextView.y - (stickerTextView.height * stickerTextView.scaleY / 2)
        return hypot(dx.toDouble(), dy.toDouble()).toFloat()
    }

    private fun getAngle(x: Float, y: Float): Float {
        val dx = x - stickerTextView.x
        val dy = y - stickerTextView.y
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isTouchWithinSticker(event)) {
                        isTouchingSticker = true
                        lastX = event.rawX
                        lastY = event.rawY
                        borderView.isVisible = true
                        hideBorderHandler.removeCallbacks(hideBorderRunnable)
                    } else {
                        isTouchingSticker = false
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isTouchingSticker) {
                        val dx = event.rawX - lastX
                        val dy = event.rawY - lastY
                        stickerTextView.translationX += dx
                        stickerTextView.translationY += dy
                        borderView.translationX += dx
                        borderView.translationY += dy
                        lastX = event.rawX
                        lastY = event.rawY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isTouchingSticker) {
                        hideBorderHandler.postDelayed(hideBorderRunnable, 2000)
                    }
                }
            }
        }
        return true
    }

    private fun isTouchWithinSticker(event: MotionEvent): Boolean {
        val stickerRect = Rect()
        stickerTextView.getHitRect(stickerRect)
        return stickerRect.contains(event.x.toInt(), event.y.toInt())
    }

    fun getStickerBitmap(): Bitmap {
        // Vẽ StickerTextView lên bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}
package com.kwanhee.cameraxsample

import android.content.Context
import android.os.Bundle
import android.view.View.MeasureSpec
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.kwanhee.camera.CameraXFactory
import com.kwanhee.camera.CameraXProvider
import com.kwanhee.cameraxsample.databinding.ActivityCameraxBinding

class CameraXActivity() : AppCompatActivity() {
    private lateinit var binding: ActivityCameraxBinding
    private lateinit var cameraX: CameraXProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraX = CameraXFactory.create()
        binding = ActivityCameraxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInset()

        initCameraSetting()
        initEvent()
    }

    private fun initCameraSetting() {
        binding.cameraView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        binding.cameraView.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.cameraView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                cameraX.startCamera(
                    this@CameraXActivity,
                    binding.cameraView.measuredWidth.dpToPx(this@CameraXActivity).toInt(),
                    binding.cameraView.measuredHeight.dpToPx(this@CameraXActivity).toInt()
                )
            }
        })
        cameraX.initialize(this, binding.cameraView)
    }

    private fun initEvent() {
        handleCaptureButton()
    }

    private fun handleCaptureButton() {
        binding.buttonCapture.setOnClickListener {
            Toast.makeText(this, "찰칵! 😎", Toast.LENGTH_SHORT).show()
            cameraX.takePhoto()
        }
    }

    private fun applyWindowInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)


            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.isAppearanceLightStatusBars = false // 상태바 아이콘 흰색
                controller.isAppearanceLightNavigationBars = false // 하단바 아이콘 흰색
            }

            WindowInsetsCompat.CONSUMED
        }
    }
}

private fun Int.dpToPx(context: Context) = this / context.resources.displayMetrics.density
private fun Float.dpToPx(context: Context) = this / context.resources.displayMetrics.density
package com.kwanhee.camera

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.text.SimpleDateFormat
import java.util.Locale

class CameraXImpl() : CameraXProvider {

    /**
     * [preview] : 미리보기 객체
     * [cameraProviderFuture] : 카메라의 수명주기를 수명주기 소유자에 바인딩에 사용되는 객체
     * [cameraProvider] : 카메라의 수명주기를 수명주기 소유자에 바인딩에 사용되는 객체
     * [context] : 액티비티 컨텍스트
     * [imageCapture] : 사진 촬영 객체
     */
    private lateinit var preview: Preview
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var context: Context
    private lateinit var imageCapture: ImageCapture
    private lateinit var resolutionSelector: ResolutionSelector

    override fun initialize(context: Context, previewView: PreviewView) {
        this.context = context
//        resolutionSelector = ResolutionSelector.Builder()
//            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
//            .build()
        this.preview =
            Preview.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//                .setResolutionSelector(resolutionSelector)
                .build()
                .also { it.surfaceProvider = previewView.surfaceProvider }
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()

        imageCapture = ImageCapture.Builder()
//            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//            .setResolutionSelector(resolutionSelector)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) // 캡처 모드 기본 값 : CAPTURE_MODE_MINIMIZE_LATENCY (퀄리티)
            .setFlashMode(ImageCapture.FLASH_MODE_OFF) // 플래시 모드
            .build()

        val orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation: Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

    }

    override fun startCamera(
        lifecycleOwner: LifecycleOwner,
        width: Int,
        height: Int
    ) {
        cameraProviderFuture.addListener({
            // 후면 카메를 기본값으로 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 리바인딩하기 전에 모든 바인딩 해제
                cameraProvider.unbindAll()

                val viewPort = ViewPort.Builder(Rational(width, height), imageCapture.targetRotation).build()
                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture)
                    .setViewPort(viewPort)
                    .build()

                // 새로운 미리보기 카메라에 바인딩
                cameraProvider.bindToLifecycle(
                    lifecycleOwner = lifecycleOwner,
                    cameraSelector = cameraSelector,
                    useCaseGroup,
                )
            } catch (exception: Exception) { // 앱 초점이 맞지않을 때 예외가 발생할 수 있음
                Log.e("로그", "Failed: ", exception)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    override fun takePhoto() {
        // 수정 가능한 imageCapture UseCase 에 대한 안정적 참조 얻기(imageCapture 얻어 오기 전에 사진 촬영 시, return)
        ::imageCapture.isInitialized.let { if (!it) return }

        // 타임 스탬프 이름
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.KOREAN)
            .format(System.currentTimeMillis())

        // MediaStore 항목 생성
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // 파일과 메타데이터를 포함한 출력 옵션 객체를 생성 (원하는 출력 방식을 지정할 수 있음)
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()


//        imageCapture.takePicture(
//            ContextCompat.getMainExecutor(context),
//            object: ImageCapture.OnImageCapturedCallback() {
//                override fun onCaptureStarted() {
//                    super.onCaptureStarted()
//                }
//
//                override fun onCaptureSuccess(image: ImageProxy) {
//                    super.onCaptureSuccess(image)
//                    image.toBitmap()
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    super.onError(exception)
//                }
//
//                override fun onCaptureProcessProgressed(progress: Int) {
//                    super.onCaptureProcessProgressed(progress)
//                }
//
//                override fun onPostviewBitmapAvailable(bitmap: Bitmap) {
//                    super.onPostviewBitmapAvailable(bitmap)
//                }
//            }
//        )

        // 사진이 촬영된 후 트리거되는 사진 촬영 리스너
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${outputFileResults.savedUri}"

                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.e("로그", msg)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("로그", "capture failed: ${exception.message}")
                }
            }
        )
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}
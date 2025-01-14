package com.kwanhee.cameraxsample

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.kwanhee.cameraxsample.ui.theme.CameraXSampleTheme

class MainActivity : ComponentActivity() {
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

    private val permissionsCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false) {
                    permissionGranted = false
                }
            }
            if (!permissionGranted) {
                // 권한 허용하지 않을 때
                Toast.makeText(baseContext, "Permission request Denied 😨", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // 권한 허용 시
                Toast.makeText(baseContext, "Permission request Granted 🎊", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionsCameraLauncher.launch(REQUIRED_PERMISSIONS)

        setContent {
            CameraXSampleTheme {
                Scaffold { innerPadding ->
                    MainScreen(
                        modifier = Modifier
                            .padding(innerPadding),
                        onStartCamera = {
                            if (allPermissionsGranted()) {
                                startCameraXActivity()
                            } else {
                                permissionsCameraLauncher.launch(REQUIRED_PERMISSIONS)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun startCameraXActivity() {
        val intent = Intent(this, CameraXActivity::class.java)
        activityLauncher.launch(intent)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}


@Composable
fun MainScreen(
    modifier: Modifier,
    onStartCamera: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onStartCamera
        ) {
            Text(
                text = "CameraX Activity"
            )
        }
    }
}
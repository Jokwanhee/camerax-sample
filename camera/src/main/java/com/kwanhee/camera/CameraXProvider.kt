package com.kwanhee.camera

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

interface CameraXProvider {
    fun initialize(context: Context, previewView: PreviewView)
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        width: Int, height: Int
    )
    fun takePhoto()
}
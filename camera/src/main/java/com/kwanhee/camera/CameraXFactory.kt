package com.kwanhee.camera

object CameraXFactory {
    fun create(): CameraXProvider = CameraXImpl()
}
package com.mikn.camera_sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.io.File
import java.util.concurrent.Executors

private const val REQUEST_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isTrainDataExists()
        viewFinder = findViewById(R.id.view_finder)
        if (allPermissionsGranted()) {
            viewFinder.post {
                startCamera()
            }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS
            )
        }

        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private fun isTrainDataExists(): Unit {
        val target_url =
            "https://raw.githubusercontent.com/tesseract-ocr/tessdata_best/master/eng.traineddata"
        val httpasync = target_url.httpGet().response { request, response, result ->
            when (result) {
                is Result.Success -> {
                    val data = result.get()
                    if (!File(applicationContext.filesDir.toString() + "/tessdata/").mkdir()) {
                        Log.d("Don't Exist", "files/tessdata")
                    }
                    if (!File(applicationContext.filesDir.toString() + "/tessdata/eng.traineddata").createNewFile()) {
                        Log.d("Don't exist", "tessdata/eng.trainddata")
                        File(applicationContext.filesDir.toString() + "/tessdata/eng.traineddata").writeBytes(
                            data
                        )
                    }
                }
                is Result.Failure -> {
                    Log.e("Error:", "Unable Start to Download File")
                }
            }
        }

        httpasync.join()
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView

    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640, 480))
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)
            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)

        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
            val savedPath =
                (applicationContext.cacheDir.toString()) + "/" + (System.currentTimeMillis()
                    .toString()) + ".jpg";
            val file = File(savedPath)
            imageCapture.takePicture(file, executor,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        val msg = "Photo capture failed: $message"
                        Log.e("CameraXApp", msg, cause)
                        viewFinder.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        Log.d("CameraXApp", msg)
                        viewFinder.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                        val intent = Intent(applicationContext, DisplayImageActivity::class.java)
                        intent.putExtra("path", savedPath)
                        startActivity(intent)
                    }
                })
        }
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        val rotationDegress = when (viewFinder.display.rotation) {
//            Surface.ROTATION_0 -> 0
//            Surface.ROTATION_90 -> 90
//            Surface.ROTATION_180 -> 180
//            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegress.toFloat(), centerX, centerY)

        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            }
        } else {
            Toast.makeText(
                this,
                "Permissions aren't granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}

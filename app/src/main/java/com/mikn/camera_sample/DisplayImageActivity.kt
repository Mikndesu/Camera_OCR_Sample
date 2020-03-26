package com.mikn.camera_sample

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_display_image.*
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class DisplayImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_image)
        capturedView.visibility = View.INVISIBLE
        intent = this.intent
        val savedPath = intent.getStringExtra("path")
        var inputStream : InputStream
        try {
            inputStream = FileInputStream(savedPath)
            val bitMap = BitmapFactory.decodeStream(inputStream)
            val imageWidth = bitMap.width
            val imageHeight = bitMap.height
            var matrix = Matrix()
            matrix.setRotate(90F, ((imageWidth/2).toFloat()), (imageHeight/2).toFloat())
            val bitMap2 = Bitmap.createBitmap(bitMap, 0, 0, imageWidth, imageHeight, matrix, true)
            capturedView.setImageBitmap(bitMap2)
            capturedView.visibility = View.VISIBLE
            inputStream.close()
        } catch (e:FileNotFoundException) {
            Log.d("isImageExists", "Image File isn't exists at $savedPath")
        }
        moveTess.setOnClickListener {
            val intent = Intent(applicationContext, TesseractActivity::class.java)
            intent.putExtra("path",savedPath)
            startActivity(intent)
        }
    }
}

package com.mikn.camera_sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class TesseractActivity : AppCompatActivity() {

    private lateinit var savedPath:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tesseract)
        savedPath = this.intent.getStringExtra("path")
    }

    inner class tessOCR : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {
            val baseApi = TessBaseAPI()
            baseApi.init(applicationContext.filesDir.toString()+"/", "eng")
            val inputStream: InputStream
            val bitmap: Bitmap
            try {
                inputStream = FileInputStream(savedPath)
                bitmap = BitmapFactory.decodeStream(inputStream)
                baseApi.setImage(bitmap)
                val recognizedText = baseApi.utF8Text
                baseApi.end()
                println(recognizedText)
                inputStream.close()
            } catch (e: FileNotFoundException) {
                Log.d("isImageExists", "Image File isn't exists at $savedPath")
            }
            return null
        }


    }
}

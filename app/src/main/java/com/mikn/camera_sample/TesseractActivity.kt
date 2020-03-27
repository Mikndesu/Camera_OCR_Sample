package com.mikn.camera_sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.android.synthetic.main.activity_tesseract.*
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class TesseractActivity : AppCompatActivity() {

    private lateinit var savedPath:String
    private lateinit var tessResult:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tesseract)
        savedPath = this.intent.getStringExtra("path")
        tessOCR().execute()
    }

    inner class tessOCR : AsyncTask<Void, Void, String>() {
        override fun onPreExecute() {
            tessResultView.text = "In Progress..."
        }
        override fun doInBackground(vararg params: Void?): String? {
            val baseApi = TessBaseAPI()
            baseApi.init(applicationContext.filesDir.toString()+"/", "eng")
            val inputStream: InputStream
            val bitMap: Bitmap
            try {
                inputStream = FileInputStream(savedPath)
                bitMap = BitmapFactory.decodeStream(inputStream)
                val imageWidth = bitMap.width
                val imageHeight = bitMap.height
                var matrix = Matrix()
                matrix.setRotate(90F, ((imageWidth/2).toFloat()), (imageHeight/2).toFloat())
                val bitMap2 = Bitmap.createBitmap(bitMap, 0, 0, imageWidth, imageHeight, matrix, true)
                baseApi.setImage(bitMap2)
                tessResult = baseApi.utF8Text
                baseApi.end()
                inputStream.close()
            } catch (e: FileNotFoundException) {
                Log.d("isImageExists", "Image File isn't exists at $savedPath")
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            tessResultView.text = tessResult
        }

    }
}

package com.example.mybyk

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class scanner : AppCompatActivity() {

    private lateinit var qrIV: ImageView
    private lateinit var msgEdt: EditText
    private lateinit var generateQRBtn: Button

    private lateinit var bitmap: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        qrIV = findViewById(R.id.idIVQrcode)
        msgEdt = findViewById(R.id.idEdt)
        generateQRBtn = findViewById(R.id.idBtnGenerateQR)

        generateQRBtn.setOnClickListener {
            val inputText = msgEdt.text.toString().trim()
            if (TextUtils.isEmpty(inputText)) {
                Toast.makeText(this, "Enter your message", Toast.LENGTH_SHORT).show()
            } else {
                generateQRCode(inputText)
            }
        }
    }

    private fun generateQRCode(text: String) {
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.GREEN else Color.WHITE)
                }
            }
            qrIV.setImageBitmap(bitmap)
            Toast.makeText(this, "QR Code Generated Successfully", Toast.LENGTH_SHORT).show()
            findViewById<EditText>(R.id.idEdt).text.clear()
        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show()
        }
    }
}

package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class CreateShiftActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_edit)



        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnBack.setOnClickListener {
            val submitFlag = false

            if(submitFlag){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
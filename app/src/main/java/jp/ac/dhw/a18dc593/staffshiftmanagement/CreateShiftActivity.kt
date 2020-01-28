package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class CreateShiftActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_edit)



        val btnShiftEditBack = findViewById<Button>(R.id.btnShiftEditBack)

        btnShiftEditBack.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            var submitFlag = false

            val userName = findViewById<TextView>(R.id.txtUserName).text.toString()
            val attendDate = findViewById<TextView>(R.id.txtAttendDate).text.toString()
            val attendTime = findViewById<TextView>(R.id.txtAttendTime).text.toString()
            val endTime = findViewById<TextView>(R.id.txtEndTime).text.toString()
            val memo = findViewById<TextView>(R.id.txtMemo).text.toString()

            if(TextUtils.isEmpty(attendDate)){
                Toast.makeText(baseContext, "出勤日を入力してください。",
                    Toast.LENGTH_SHORT).show()
            } else if(TextUtils.isEmpty(attendTime)){
                Toast.makeText(baseContext, "出勤時間を入力してください。",
                    Toast.LENGTH_SHORT).show()
            } else if(TextUtils.isEmpty(endTime)){
                Toast.makeText(baseContext, "終了時間を入力してください。",
                    Toast.LENGTH_SHORT).show()
            } else {
                submitFlag = true
            }

            if(submitFlag){


                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
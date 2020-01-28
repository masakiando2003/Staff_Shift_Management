package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateShiftActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    private val myPREFERENCES = "MyPrefs"
    var sharedpreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_edit)

        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)

        if(sharedpreferences!!.contains("loginUserName")){
            var loginUserStr = findViewById<TextView>(R.id.txtUserName)
            loginUserStr.text = sharedpreferences!!.getString("loginUserName",null)
        }

        val btnShiftEditBack = findViewById<Button>(R.id.btnShiftEditBack)

        btnShiftEditBack.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnShiftEditSubmit = findViewById<Button>(R.id.btnShiftEditSubmit)

        btnShiftEditSubmit.setOnClickListener {
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
                Toast.makeText(baseContext, "シフトを登録しています。少々お待ちください...",
                    Toast.LENGTH_SHORT).show()
                databaseReference = FirebaseDatabase.getInstance().reference

                val shiftData = ShiftModel(attendTime, endTime, memo)
                var shiftDateArr = attendDate.split("/")
                val shiftDate = shiftDateArr.joinToString("")
                databaseReference.child("shift_list").child(shiftDate).child(userName).setValue(shiftData)
                Toast.makeText(baseContext, "シフトを登録しました。",
                    Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
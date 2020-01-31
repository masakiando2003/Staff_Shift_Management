package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ShiftDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ShiftDetailActivity"
    }

    private lateinit var shiftDetailRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var shiftDetailListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_detail)

        val userName = intent!!.getStringExtra("userName")
        val shiftDateFormatted =
            intent!!.getStringExtra("shiftDateFormatted")
        val shiftDate = intent!!.getStringExtra("shiftDate")
        val txtUserName = findViewById<TextView>(R.id.txtUserName)
        txtUserName.text = userName
        val txtAttendDate = findViewById<TextView>(R.id.txtAttendDate)
        txtAttendDate.text = shiftDateFormatted
        if(userName != null && !TextUtils.isEmpty(userName) &&
            shiftDateFormatted != null && !TextUtils.isEmpty(shiftDateFormatted) &&
            shiftDate != null && !TextUtils.isEmpty(shiftDate)){
            databaseReference = FirebaseDatabase.getInstance().reference
            shiftDetailRef = databaseReference.child("shift_list")
                .child(shiftDate).child(userName)
            shiftDetailListener = object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "Number of messages for shiftDetailListener: " +
                            "${dataSnapshot.childrenCount}")
                    dataSnapshot.children.forEach { child ->
                        val field = child.key.toString()
                        val value = child.value.toString()
                        when{
                            (field == "attendTime") -> {
                                val txtAttendTime = findViewById<TextView>(R.id.txtAttendTime)
                                txtAttendTime.text = value
                            }
                            (field == "endTime") -> {
                                val txtEndTime = findViewById<TextView>(R.id.txtEndTime)
                                txtEndTime.text = value
                            }
                            (field == "memo") -> {
                                val txtMemo = findViewById<TextView>(R.id.txtMemo)
                                txtMemo.text = value
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "shiftDetailListener:onCancelled: ${error.message}")
                }
            }
            shiftDetailRef.addValueEventListener(shiftDetailListener)
        } else {
            Toast.makeText(this, "ユーザーの出勤データを取得出来ません。" +
                    "シフトリストから再選択してください。",
                Toast.LENGTH_LONG).show()
        }

        val btnShiftDetailBack = findViewById<Button>(R.id.btnShiftDetailBack)

        btnShiftDetailBack.setOnClickListener {
            val intentBack = Intent(this, ShiftListActivity::class.java)
            intentBack.putExtra("shiftDateFormatted",
                shiftDateFormatted)
            intentBack.putExtra("shiftDate", shiftDate)
            startActivity(intentBack)
        }
    }

}
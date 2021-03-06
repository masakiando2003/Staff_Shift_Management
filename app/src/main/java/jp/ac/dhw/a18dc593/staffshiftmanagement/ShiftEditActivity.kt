package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.*
import ir.drax.netwatch.NetWatch
import ir.drax.netwatch.cb.NetworkChangeReceiver_navigator
import java.text.SimpleDateFormat
import java.util.*

class ShiftEditActivity : FragmentActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    companion object {
        private const val TAG = "ShiftEditActivity"
    }

    private var timePickerID: String? = null

    private lateinit var shiftEditRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var shiftEditListener: ValueEventListener
    private lateinit var userListReference: DatabaseReference
    private lateinit var userListListener: ValueEventListener

    private val myPREFERENCES = "MyPrefs"
    private var mySharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_edit)

        NetWatch.builder(this)
            .setIcon(R.drawable.ic_signal_wifi_off_black_12dp)
            .setCallBack(object : NetworkChangeReceiver_navigator {
                override fun onConnected(source: Int) {
                }
                override fun onDisconnected() {
                    Toast.makeText(this@ShiftEditActivity,
                        "ネットワークにアクセス出来ません。" +
                                "コネクションをチェックしてください。", Toast.LENGTH_SHORT).show()
                }
            })
            .setNotificationCancelable(false)
            .build()

        val oldUserName = intent!!.getStringExtra("userName")
        val shiftDateFormatted = intent!!.getStringExtra("shiftDateFormatted")
        val shiftDate = intent!!.getStringExtra("shiftDate")
        val txtAttendDate = findViewById<TextView>(R.id.txtAttendDate)
        txtAttendDate.text = shiftDateFormatted

        mySharedPreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!mySharedPreferences!!.contains("email")){
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }
        val loginUserRole = mySharedPreferences!!.getString("loginUserRole",
            null)

        if(oldUserName != null && !TextUtils.isEmpty(oldUserName) &&
            shiftDateFormatted != null && !TextUtils.isEmpty(shiftDateFormatted) &&
            shiftDate != null && !TextUtils.isEmpty(shiftDate)){


            val userNameArr = arrayListOf<String>()
            databaseReference = FirebaseDatabase.getInstance().reference
            userListReference = databaseReference.child("users")
            userListListener = object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "Number of messages for userListListener: " +
                                "${dataSnapshot.childrenCount}"
                    )
                    dataSnapshot.children.forEach { child ->
                        Log.d(TAG,
                            "Key: ${child.key.toString()}, Value: ${child.value.toString()}")
                        val user = child.key.toString()
                        userNameArr.add(user)
                    }
                    val userNameSpinner = findViewById<Spinner>(R.id.spnUserName)
                    val adapter =
                        ArrayAdapter(
                            this@ShiftEditActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            userNameArr
                        )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    userNameSpinner!!.adapter = adapter
                    userNameSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parentView: AdapterView<*>,
                                                        selectedItemView: View,
                                                        position: Int, id: Long) {
                            }

                            override fun onNothingSelected(parentView: AdapterView<*>) {
                            }
                        }
                    val spinnerPosition = adapter.getPosition(oldUserName)
                    userNameSpinner.setSelection(spinnerPosition)
                    if(loginUserRole != "admin"){
                        userNameSpinner.isEnabled = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "userListListener:onCancelled: ${error.message}")
                }
            }
            userListReference.addValueEventListener(userListListener)

            databaseReference = FirebaseDatabase.getInstance().reference
            shiftEditRef = databaseReference.child("shift_list")
                .child(shiftDate).child(oldUserName)
            shiftEditListener = object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(
                        TAG, "Number of messages for shiftEditListener: " +
                                "${dataSnapshot.childrenCount}")
                    dataSnapshot.children.forEach { child ->
                        val field = child.key.toString()
                        val value = child.value.toString()
                        Log.d(TAG, "Field: $field, Value: $value")
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
            shiftEditRef.addValueEventListener(shiftEditListener)
        } else {
            Toast.makeText(this, "ユーザーの出勤データを取得出来ません。" +
                    "シフトリストから再選択してください。",
                Toast.LENGTH_LONG).show()
        }

        val btnShiftEditBack = findViewById<Button>(R.id.btnShiftEditBack)

        btnShiftEditBack.setOnClickListener {
            val intent = Intent(this, ShiftListActivity::class.java)
            intent.putExtra("shiftDateFormatted",
                shiftDateFormatted)
            intent.putExtra("shiftDate", shiftDate)
            startActivity(intent)
        }

        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        btnSubmit.text = "更新"

        btnSubmit.setOnClickListener {
            var submitFlag = false

            val userName = findViewById<Spinner>(R.id.spnUserName).selectedItem.toString()
            val attendDate = findViewById<TextView>(R.id.txtAttendDate).text.toString()
            val attendTime = findViewById<TextView>(R.id.txtAttendTime).text.toString()
            val endTime = findViewById<TextView>(R.id.txtEndTime).text.toString()
            val memo = findViewById<TextView>(R.id.txtMemo).text.toString()

            val timeSDF = SimpleDateFormat("hh:mm", Locale.JAPAN)
            val inTime: Date = timeSDF.parse(attendTime)
            val outTime: Date = timeSDF.parse(endTime)
            Log.d(TAG, "inTime: $inTime, outTime: $outTime")

            when{
                (TextUtils.isEmpty(attendDate)) -> {
                    Toast.makeText(baseContext, "出勤日を入力してください。",
                        Toast.LENGTH_SHORT).show()
                }
                (TextUtils.isEmpty(attendTime)) -> {
                    Toast.makeText(baseContext, "出勤時間を入力してください。",
                        Toast.LENGTH_SHORT).show()
                }
                (TextUtils.isEmpty(endTime)) -> {
                    Toast.makeText(baseContext, "終了時間を入力してください。",
                        Toast.LENGTH_SHORT).show()
                }
                (outTime.before(inTime)) -> {
                    Toast.makeText(baseContext, "終了時間は出勤時間の後に入力してください。",
                        Toast.LENGTH_SHORT).show()
                }
                else -> {
                    submitFlag = true
                }
            }

            when (submitFlag){
                true -> {
                    databaseReference = FirebaseDatabase.getInstance().reference
                    val editShiftData = ShiftModel(attendTime, endTime, memo)
                    val editSiftDateArr = attendDate.split("/")
                    val editShiftDate = editSiftDateArr.joinToString("")
                    if(oldUserName != userName){
                        databaseReference.child("shift_list").child(shiftDate)
                                .child(oldUserName).removeValue()
                        databaseReference.child("shift_list").child(editShiftDate)
                            .child(userName).setValue(editShiftData)
                    } else {
                        if(shiftDate != editShiftDate){
                            databaseReference.child("shift_list").child(shiftDate)
                                .child(userName).removeValue()
                        }
                        databaseReference.child("shift_list").child(editShiftDate)
                            .child(userName).setValue(editShiftData)
                    }
                    Toast.makeText(
                        baseContext, "シフトを更新しました。",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, ShiftListActivity::class.java)
                    intent.putExtra("shiftDateFormatted",
                        shiftDateFormatted)
                    intent.putExtra("shiftDate", shiftDate)
                    startActivity(intent)
                }
            }
        }

        val btnShiftDelete = findViewById<Button>(R.id.btnShiftDelete)
        btnShiftDelete.visibility = View.VISIBLE
        btnShiftDelete.setOnClickListener { AlertDialog.Builder(this@ShiftEditActivity)
            .setTitle("注意!")
            .setMessage(oldUserName+"の出勤データを削除してもよろしいでしょうか?")
            .setPositiveButton("OK") { _, _ ->
                databaseReference.child("shift_list")
                    .child(shiftDate)
                    .child(oldUserName).removeValue()
                Toast.makeText(this@ShiftEditActivity,
                    "ユーザー: $oldUserName の出勤データを削除しました",
                    Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ShiftEditActivity,
                    ShiftListActivity::class.java)
                intent.putExtra("shiftDateFormatted",
                    shiftDateFormatted)
                intent.putExtra("shiftDate", shiftDate)
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show() }
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val monthOfYearStr = when(monthOfYear < 10){
            true -> "0${(monthOfYear+1)}"
            false -> (monthOfYear+1).toString()
        }
        val dayOfMonthStr = when(dayOfMonth < 10){
            true -> "0$dayOfMonth"
            false -> dayOfMonth.toString()
        }
        val dateStr = String.format(Locale.JAPAN, "%s/%s/%s", year, monthOfYearStr,
            dayOfMonthStr)
        val attendDate = findViewById<TextView>(R.id.txtAttendDate)
        attendDate.text = dateStr
    }

    fun showDatePickerDialog(@Suppress("UNUSED_PARAMETER")view: View) {
        val newFragment = ShiftEditDatePick()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val hourOfDayStr = when(hourOfDay < 10){
            true -> "0$hourOfDay"
            false -> hourOfDay.toString()
        }
        val minuteStr = when(minute < 10){
            true -> "0$minute"
            false -> minute.toString()
        }
        val timeStr = String.format(Locale.JAPAN, "%s:%s", hourOfDayStr, minuteStr)
        when(timePickerID){
            "txtAttendTime" -> {
                val txtAttendDateView = findViewById<TextView>(R.id.txtAttendTime)
                txtAttendDateView.text = timeStr
            }
            "txtEndTime" -> {
                val txtEndDateView = findViewById<TextView>(R.id.txtEndTime)
                txtEndDateView.text = timeStr
            }
        }
    }

    fun showTimePickerDialogForAttendTime(@Suppress("UNUSED_PARAMETER")view: View) {
        timePickerID = "txtAttendTime"
        val newFragment = ShiftEditTimePick()
        newFragment.show(supportFragmentManager, "timePicker")
    }

    fun showTimePickerDialogForEndTime(@Suppress("UNUSED_PARAMETER")view: View) {
        timePickerID = "txtEndTime"
        val newFragment = ShiftEditTimePick()
        newFragment.show(supportFragmentManager, "timePicker")
    }

}
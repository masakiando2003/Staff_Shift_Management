package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.fragment.app.FragmentActivity
import java.util.*

class ShiftDateActivity : FragmentActivity(), DatePickerDialog.OnDateSetListener {

    companion object {
        private const val TAG = "ShiftDateActivity"
    }

    private val myPREFERENCES = "MyPrefs"
    private var sharedReferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_date)

        sharedReferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!sharedReferences!!.contains("email")){
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }

        val newFragment = ShiftListDatePick()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val monthOfYearStr = when(monthOfYear < 10){
            true -> "0${(monthOfYear+1)}"
            false -> (monthOfYear+1).toString()
        }
        val dayOfMonthStr = when(dayOfMonth < 10){
            true -> "0$dayOfMonth"
            false -> dayOfMonth.toString()
        }
        val shiftDateFormattedStr = String.format(Locale.JAPAN, "%s/%s/%s",
            year, monthOfYearStr, dayOfMonthStr)
        val shiftDateStr = String.format(Locale.JAPAN, "%s%s%s", year,
            monthOfYearStr, dayOfMonthStr)
        Log.d(TAG,
            "shiftDateFormattedStf: $shiftDateFormattedStr, shiftDateStf: $shiftDateStr")
        val intent = Intent(this@ShiftDateActivity, ShiftListActivity::class.java)
        intent.putExtra("shiftDateFormatted", shiftDateFormattedStr)
        intent.putExtra("shiftDate", shiftDateStr)
        startActivity(intent)
    }
}
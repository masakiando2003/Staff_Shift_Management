package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*


class CreateShiftTimePick : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    // Bundle sould be nullable, Bundle?
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // to initialize a Calender instance
        val c = Calendar.getInstance()

        // at the first, to get the system current hour and minute
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        return TimePickerDialog(
            activity,
            activity as CreateShiftActivity?,
            hour,
            minute,
            true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        //
    }
}
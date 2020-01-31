package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class ShiftEditDatePick : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            context!!,
            activity as ShiftEditActivity?,
            year,
            month,
            day)
    }

    override fun onDateSet(view: android.widget.DatePicker, year: Int,
                           monthOfYear: Int, dayOfMonth: Int) {
    }
}
package jp.ac.dhw.a18dc593.staffshiftmanagement

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ShiftModel(
    val attendTime: String?,
    val endTime: String?,
    val memo: String?
)
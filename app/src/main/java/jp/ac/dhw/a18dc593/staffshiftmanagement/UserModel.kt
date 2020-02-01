package jp.ac.dhw.a18dc593.staffshiftmanagement

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserModel(
    val name: String?,
    val email: String?,
    val password: String?,
    val role: String?,
    val avatarBase64: String?
)
package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class UserRegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserRegister"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit)
    }
}
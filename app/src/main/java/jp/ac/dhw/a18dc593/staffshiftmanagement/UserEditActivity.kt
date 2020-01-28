package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

class UserEditActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserEdit"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit)
    }
}
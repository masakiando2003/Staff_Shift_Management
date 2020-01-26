package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LogInActivity : AppCompatActivity() {

    val myPREFERENCES = "MyPrefs"
    val emailKey = "email"
    val passwordKey = "password"
    var sharedpreferences: SharedPreferences? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedpreferences!!.edit()
        editor.clear()
        editor.apply()

        auth = FirebaseAuth.getInstance()

        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.txtCompanyEmail).text.toString()
            val password = findViewById<EditText>(R.id.txtPassword).text.toString()

            if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
                            val editor2 = sharedpreferences!!.edit()

                            editor2.putString(emailKey, email)
                            editor2.putString(passwordKey, password)
                            editor2.apply()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "E-メール、パスワードの入力に誤りがあります。",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(baseContext, "E-メールを入力してください。",
                        Toast.LENGTH_SHORT).show()
                } else if(TextUtils.isEmpty(password)){
                    Toast.makeText(baseContext, "パスワードを入力してください。",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}
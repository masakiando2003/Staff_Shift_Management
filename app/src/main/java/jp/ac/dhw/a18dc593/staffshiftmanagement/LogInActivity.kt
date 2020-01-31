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
import com.google.firebase.database.*


class LogInActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val myPREFERENCES = "MyPrefs"
    private val emailKey = "email"
    private val passwordKey = "password"
    private val loginUserName = "loginUserName"
    private val loginUserRole = "loginUserRole"
    private var mySharedPreferences: SharedPreferences? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var userInfoRef: DatabaseReference
    private lateinit var dbRef: DatabaseReference
    private lateinit var userInfoListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        mySharedPreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        val editor = mySharedPreferences!!.edit()
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
                            Log.d(TAG, "signInWithEmail:success")
                            Toast.makeText(this,
                                "ログイン中です。少々お待ちください...",
                                Toast.LENGTH_SHORT).show()

                            // アカウントの情報をsession(sharedPreferences)に保存する
                            mySharedPreferences = getSharedPreferences(myPREFERENCES,
                                Context.MODE_PRIVATE)
                            val editor2 = mySharedPreferences!!.edit()

                            editor2.putString(emailKey, email)
                            editor2.putString(passwordKey, password)
                            dbRef = FirebaseDatabase.getInstance().reference
                            userInfoRef = dbRef.child("users")
                            userInfoListener = object : ValueEventListener {

                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    Log.d(TAG, "Number of messages: " +
                                            "${dataSnapshot.childrenCount}")
                                    dataSnapshot.children.forEach { child ->
                                        if(child.child("email").value.toString() == email){
                                            editor2.putString(loginUserName, child.key)
                                            editor2.putString(loginUserRole,
                                                child.child("role").value.toString())
                                        }
                                    }
                                    editor2.apply()
                                    val intent = Intent(this@LogInActivity,
                                        MainActivity::class.java)
                                    startActivity(intent)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(TAG, "messages:onCancelled: ${error.message}")
                                }
                            }
                            userInfoRef.addValueEventListener(userInfoListener)
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(baseContext,
                                "E-メール、パスワードの入力に誤りがあります。",
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
}
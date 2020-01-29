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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class LogInActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    val myPREFERENCES = "MyPrefs"
    val emailKey = "email"
    val passwordKey = "password"
    val loginUserName = "loginUserName"
    val loginUserRole = "loginUserRole"
    var sharedpreferences: SharedPreferences? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var UserInfoRef: DatabaseReference
    private lateinit var dbRef: DatabaseReference
    private lateinit var UserInfoListener: ValueEventListener

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
                            Toast.makeText(this, "ログイン中です。少々お待ちください...",
                                Toast.LENGTH_SHORT).show()

                            // アカウントの情報をsession(sharedPreferences)に保存する
                            sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
                            val editor2 = sharedpreferences!!.edit()

                            editor2.putString(emailKey, email)
                            editor2.putString(passwordKey, password)
                            dbRef = FirebaseDatabase.getInstance().reference
                            UserInfoRef = dbRef.child("users")
                            UserInfoListener = object : ValueEventListener {

                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    // New data at this path. This method will be called after every change in the
                                    // data at this path or a subpath.

                                    Log.d(TAG, "Number of messages: ${dataSnapshot.childrenCount}")
                                    dataSnapshot.children.forEach { child ->
                                        if(child.child("email").value.toString() == email){
                                            editor2.putString(loginUserName, child.key)
                                            editor2.putString(loginUserRole, child.child("role").value.toString())
                                        }
                                    }
                                    editor2.apply()
                                    val intent = Intent(this@LogInActivity, MainActivity::class.java)
                                    startActivity(intent)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Could not successfully listen for data, log the error
                                    Log.e(TAG, "messages:onCancelled: ${error.message}")
                                }
                            }
                            UserInfoRef.addValueEventListener(UserInfoListener)
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
}
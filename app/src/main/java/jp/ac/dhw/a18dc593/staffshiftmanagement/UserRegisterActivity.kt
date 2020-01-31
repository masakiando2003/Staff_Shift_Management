package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserRegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserRegisterActivity"
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit)

        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val btnUserRegisterSubmit = findViewById<Button>(R.id.btnUserRegisterSubmit)

        btnUserRegisterSubmit.setOnClickListener {
            var submitFlag = false

            val userName = findViewById<TextView>(R.id.txtUserName).text.toString()
            val userEmail = findViewById<TextView>(R.id.txtUserEmail).text.toString()
            val userPassword = findViewById<TextView>(R.id.txtUserPassword).text.toString()
            var userRole = findViewById<Spinner>(R.id.spnRole).selectedItem.toString()
            when {
                (userRole.equals("一般ユーザー", false)) ->{
                    userRole = "user"
                }
                else -> {
                    userRole = "admin"
                }
            }
            when{
                (TextUtils.isEmpty(userName)) ->{
                    Toast.makeText(baseContext, "ユーザー名を入力してください。",
                        Toast.LENGTH_SHORT).show()
                } (TextUtils.isEmpty(userEmail)) ->{
                    Toast.makeText(baseContext, "メールアドレスを入力してください。",
                        Toast.LENGTH_SHORT).show()
                } (TextUtils.isEmpty(userPassword)) ->{
                    Toast.makeText(baseContext, "パスワードを入力してください。",
                        Toast.LENGTH_SHORT).show()
                } else -> {
                    submitFlag = true
                }
            }

            if(submitFlag){
                Toast.makeText(baseContext, "登録しています。少々お待ちください...",
                    Toast.LENGTH_SHORT).show()

                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val userData = UserModel(userName, userEmail, userPassword, userRole)
                            databaseReference.child("users").child(userName).setValue(userData)
                            Log.d(TAG, "registerUserToDatabase:success")

                            Toast.makeText(baseContext, "登録完成しました。",
                                Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "ユーザーが存在しています。" +
                                    "別のメールアドレスで登録してください。",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        val btnUserEditBack = findViewById<Button>(R.id.btnUserEditBack)

        btnUserEditBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
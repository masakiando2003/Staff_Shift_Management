package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    val myPREFERENCES = "MyPrefs"
    var sharedpreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!sharedpreferences!!.contains("email")){
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }
        if(sharedpreferences!!.contains("loginUserName")){
            var loginUserStr = findViewById<TextView>(R.id.loginUser)
            loginUserStr.text = "ログインユーザー: "+sharedpreferences!!.getString("loginUserName",null)
        }

        auth = FirebaseAuth.getInstance()

        val layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        layoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        mainMenuRecyclerView.layoutManager = layoutManager

        val adapter = MainMenuItemsAdapter(this, Supplier.menu_items)
        mainMenuRecyclerView.adapter = adapter

        val itemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            this,
            androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
        )
        mainMenuRecyclerView.addItemDecoration(itemDecoration)

    }

    public override fun onStart() {
        super.onStart()
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        // [END signin_anonymously]
    }

    companion object {
        private const val TAG = "AnonymousAuth"
    }
}

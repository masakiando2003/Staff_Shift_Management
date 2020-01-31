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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var auth: FirebaseAuth

    private val myPREFERENCES = "MyPrefs"
    private var mySharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mySharedPreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!mySharedPreferences!!.contains("email")){
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }
        if(mySharedPreferences!!.contains("loginUserName")){
            val loginUserStr = findViewById<TextView>(R.id.loginUser)
            val loginUserName =
                mySharedPreferences!!.getString("loginUserName",null)?.toString()
            val displayUserText = "ログインユーザー: $loginUserName"
            loginUserStr.text = displayUserText
        }

        auth = FirebaseAuth.getInstance()

        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val userEmail = mySharedPreferences!!.getString("email", null)?.toString()

        db.collection("users").whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    for(fields in document.data){
                        Log.w(TAG, "Field Data: $fields")
                        val fieldData = fields.toString().split("=")
                        if(fieldData[0] == "userName"){
                            val userNameStr = "ログインユーザー: "+fieldData[1]
                            val txtLoginUser = findViewById<TextView>(R.id.loginUser)
                            txtLoginUser.text = userNameStr
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
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
                    Log.d(TAG, "signInAnonymously:success")
                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext,
                        "認証出来ません。もう一度ログインしてください。.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

}

package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*


class CompanyDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CompanyInfoActivity"
    }

    private val myPREFERENCES = "MyPrefs"
    private var sharedpreferences: SharedPreferences? = null

    private lateinit var companyInfoRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var companyInfoListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.company_detail)

        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!sharedpreferences!!.contains("email")){
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }

        databaseReference = FirebaseDatabase.getInstance().reference

        companyInfoRef = databaseReference.child("company_info")
        companyInfoListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "Number of messages in Company Info: " +
                        "${dataSnapshot.childrenCount}")
                dataSnapshot.children.forEach { child ->
                    val field = child.key.toString()
                    val value = child.value.toString()
                    when {
                        (field == "CompanyName") -> {
                            val companyNameField =
                                findViewById<TextView>(R.id.txtCompanyName)
                            companyNameField.text = value
                        }
                        (field == "CompanyEmail") -> {
                            val companyEmailField =
                                findViewById<TextView>(R.id.txtCompanyEmail)
                            companyEmailField.text = value
                        }
                        (field == "CompanyTel") -> {
                            val companyTelField =
                                findViewById<TextView>(R.id.txtCompanyTel)
                            companyTelField.text = value
                        }
                        (field == "CompanyDesc") -> {
                            val companyDescField =
                                findViewById<TextView>(R.id.txtCompanyDesc)
                            companyDescField.text = value
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "messages:onCancelled: ${error.message}")
            }
        }
        companyInfoRef.addValueEventListener(companyInfoListener)

        val btnCompanyDetailBack = findViewById<Button>(R.id.btnCompanyDetailBack)

        btnCompanyDetailBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
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
        private const val TAG = "CompanyInfo"
    }

    val myPREFERENCES = "MyPrefs"
    var sharedpreferences: SharedPreferences? = null

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
                // New data at this path. This method will be called after every change in the
                // data at this path or a subpath.

                Log.d(TAG, "Number of messages: ${dataSnapshot.childrenCount}")
                dataSnapshot.children.forEach { child ->
                    if(child.key.toString() == "CompanyName"){
                        val companyNameField = findViewById<TextView>(R.id.txtCompanyName)
                        companyNameField.text = child.value.toString()
                    }
                    else if(child.key.toString() == "CompanyEmail"){
                        val companyEmailField = findViewById<TextView>(R.id.txtCompanyEmail)
                        companyEmailField.text = child.value.toString()
                    }
                    else if(child.key.toString() == "CompanyTel"){
                        val companyTelField = findViewById<TextView>(R.id.txtCompanyTel)
                        companyTelField.text = child.value.toString()
                    }
                    else if(child.key.toString() == "CompanyDesc"){
                        val companyDescField = findViewById<TextView>(R.id.txtCompanyDesc)
                        companyDescField.text = child.value.toString()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Could not successfully listen for data, log the error
                Log.e(TAG, "messages:onCancelled: ${error.message}")
            }
        }
        companyInfoRef.addValueEventListener(companyInfoListener)

        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
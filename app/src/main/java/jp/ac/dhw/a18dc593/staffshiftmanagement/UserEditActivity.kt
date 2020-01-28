package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class UserEditActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserEdit"
    }

    private lateinit var userDetailRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userDetailListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit)

        val intent: Intent = getIntent()
        val userName = intent.getStringExtra("userName")
        if(userName != null && !TextUtils.isEmpty(userName)){
            databaseReference = FirebaseDatabase.getInstance().reference
            userDetailRef = databaseReference.child("users").child(userName)
            userDetailListener = object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // New data at this path. This method will be called after every change in the
                    // data at this path or a subpath.
                    Log.d(TAG, "Number of messages: ${dataSnapshot.childrenCount}")
                    dataSnapshot.children.forEach { child ->
                        if(child.key.toString() == "email"){
                            var userEmail = findViewById<TextView>(R.id.txtUserEmail)
                            userEmail.text = child.value.toString()
                        } else if(child.key.toString() == "name"){
                            var userEmail = findViewById<TextView>(R.id.txtUserName)
                            userEmail.text = child.value.toString()
                        } else if(child.key.toString() == "password"){
                            var userEmail = findViewById<TextView>(R.id.txtUserPassword)
                            userEmail.text = child.value.toString()
                        } else if(child.key.toString() == "role"){
                            val userRole = findViewById<Spinner>(R.id.spnRole)
                            val role: String?
                            if(child.value.toString() == "admin"){
                                role = "管理者"
                            } else {
                                role = "一般ユーザー"
                            }
                            val adapter =
                                ArrayAdapter.createFromResource(
                                    this@UserEditActivity,
                                    R.array.role_array,
                                    android.R.layout.simple_spinner_item
                                )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            userRole.adapter = adapter
                            val spinnerPosition = adapter.getPosition(role)
                            userRole.setSelection(spinnerPosition)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Could not successfully listen for data, log the error
                    Log.e(TAG, "messages:onCancelled: ${error.message}")
                }
            }
            userDetailRef.addValueEventListener(userDetailListener)
        } else {
            Toast.makeText(this, "ユーザー取得出来ません。ユーザーから再選択してください。",
                Toast.LENGTH_LONG).show()
        }

        val btnUserEditBack = findViewById<Button>(R.id.btnUserEditBack)

        btnUserEditBack.setOnClickListener {
            val intentBack = Intent(this, UserListActivity::class.java)
            startActivity(intentBack)
        }
    }
}
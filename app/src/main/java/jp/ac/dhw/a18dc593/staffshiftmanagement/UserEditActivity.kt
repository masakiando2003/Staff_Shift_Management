package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*


class UserEditActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserEditActivity"
    }

    private lateinit var userDetailRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userDetailListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit)

        val userName = intent!!.getStringExtra("userName")
        val oldUserName = intent!!.getStringExtra("userName")
        if(userName != null && !TextUtils.isEmpty(userName)){
            databaseReference = FirebaseDatabase.getInstance().reference
            userDetailRef = databaseReference.child("users").child(userName)
            userDetailListener = object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "Number of messages for userDetailListener: " +
                            "${dataSnapshot.childrenCount}")
                    dataSnapshot.children.forEach { child ->
                        val field = child.key.toString()
                        val value = child.value.toString()
                        when {
                            (field == "email") -> {
                                val userEmail =
                                    findViewById<TextView>(R.id.txtUserEmail)
                                userEmail.text = child.value.toString()
                            }
                            (field == "name") -> {
                                val userNameDisplay =
                                    findViewById<TextView>(R.id.txtUserName)
                                userNameDisplay.text = child.value.toString()
                            }
                            (field == "password") -> {
                                val userPassword =
                                    findViewById<TextView>(R.id.txtUserPassword)
                                userPassword.text = child.value.toString()
                            }
                            (field == "role") -> {
                                val userRole = findViewById<Spinner>(R.id.spnRole)
                                val role: String?
                                when{
                                    (value == "admin") ->{
                                        role = "管理者"
                                    } else -> {
                                        role = "一般ユーザー"
                                    }
                                }
                                val adapter =
                                    ArrayAdapter.createFromResource(
                                        this@UserEditActivity,
                                        R.array.role_array,
                                        android.R.layout.simple_spinner_item
                                    )
                                adapter.setDropDownViewResource(
                                    android.R.layout.simple_spinner_dropdown_item
                                )
                                userRole.adapter = adapter
                                val spinnerPosition = adapter.getPosition(role)
                                userRole.setSelection(spinnerPosition)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Could not successfully listen for data, log the error
                    Log.e(TAG, "userDetailListener:onCancelled: ${error.message}")
                }
            }
            userDetailRef.addValueEventListener(userDetailListener)
        } else {
            Toast.makeText(this, "ユーザー取得出来ません。" +
                    "ユーザーから再選択してください。",
                Toast.LENGTH_LONG).show()
        }

        val btnUserEdit = findViewById<Button>(R.id.btnUserRegisterSubmit)
        btnUserEdit.text = "更新"
        btnUserEdit.setOnClickListener {
            Toast.makeText(
                this, "更新しています。少々お待ちください...",
                Toast.LENGTH_SHORT
            ).show()
            val editUserName = findViewById<TextView>(R.id.txtUserName).text.toString()
            val editUserEmail = findViewById<TextView>(R.id.txtUserEmail).text.toString()
            val editUserPassword =
                findViewById<TextView>(R.id.txtUserPassword).text.toString()
            var editUserRole = findViewById<Spinner>(R.id.spnRole).selectedItem.toString()
            when {
                (editUserRole == "一般ユーザー") -> {
                    editUserRole = "user"
                } else -> {
                    editUserRole = "admin"
                }
            }
            val userData = UserModel(userName, editUserEmail, editUserPassword, editUserRole)

            if(editUserName != oldUserName){
                databaseReference.child("users")
                    .child(editUserName).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val userCount = p0.childrenCount
                            if(userCount > 0){
                                Toast.makeText(this@UserEditActivity,
                                    "ユーザー: "+editUserName+"はデータベースに存在しています",
                                    Toast.LENGTH_LONG).show()
                            }
                            else {
                                databaseReference.child("users")
                                    .child(oldUserName).removeValue()
                                databaseReference.child("users")
                                    .child(editUserName).setValue(userData)
                                Toast.makeText(this@UserEditActivity,
                                    "ユーザー: "+editUserName+"を更新しました",
                                    Toast.LENGTH_SHORT).show()
                                val intentBack = Intent(this@UserEditActivity,
                                    UserListActivity::class.java)
                                startActivity(intentBack)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "userCountListener:onCancelled: ${error.message}")
                        }
                })
            } else{
                databaseReference.child("users").child(editUserName).setValue(userData)
                Toast.makeText(this, "ユーザー: "+editUserName+"を更新しました",
                    Toast.LENGTH_SHORT).show()
                val intentBack = Intent(this, UserListActivity::class.java)
                startActivity(intentBack)
            }
        }

        val btnUserDelete = findViewById<Button>(R.id.btnUserDelete)
        btnUserDelete.visibility = View.VISIBLE
        btnUserDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("注意!")
                .setMessage(userName+"を削除してもよろしいでしょうか?")
                .setPositiveButton("OK") { _, _ ->
                    databaseReference.child("users").child(oldUserName).removeValue()
                    Toast.makeText(this, "ユーザー: $oldUserName を削除しました",
                        Toast.LENGTH_SHORT).show()
                    val intentBack = Intent(this, UserListActivity::class.java)
                    startActivity(intentBack)
                }
                .setNegativeButton("No", null)
                .show()
        }

        val btnUserEditBack = findViewById<Button>(R.id.btnUserEditBack)

        btnUserEditBack.setOnClickListener {
            val intentBack = Intent(this, UserListActivity::class.java)
            startActivity(intentBack)
        }
    }
}
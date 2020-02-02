package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import ir.drax.netwatch.NetWatch
import ir.drax.netwatch.cb.NetworkChangeReceiver_navigator
import java.util.*

class UserDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserDetailActivity"
    }

    private lateinit var userDetailRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userDetailListener: ValueEventListener
    private var avatarBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_detail)

        NetWatch.builder(this)
            .setIcon(R.drawable.ic_signal_wifi_off_black_12dp)
            .setCallBack(object : NetworkChangeReceiver_navigator {
                override fun onConnected(source: Int) {
                }
                override fun onDisconnected() {
                    Toast.makeText(this@UserDetailActivity,
                        "ネットワークにアクセス出来ません。" +
                                "コネクションをチェックしてください。", Toast.LENGTH_SHORT).show()
                }
            })
            .setNotificationCancelable(false)
            .build()

        val userName = intent!!.getStringExtra("userName")
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
                            (field == "avatarBase64") -> {
                                avatarBase64 = value
                                val avatarBytes = Base64.getDecoder().decode(avatarBase64)
                                val decodedImage = BitmapFactory.decodeByteArray(avatarBytes,
                                    0, avatarBytes.size)
                                val imgUserAvatar = findViewById<ImageView>(R.id.imgUserAvatar)
                                imgUserAvatar.setImageBitmap(decodedImage)
                            }
                            (field == "email") -> {
                                val userEmail =
                                    findViewById<TextView>(R.id.txtUserDetailEmail)
                                userEmail.text = value
                            }
                            (field == "name") -> {
                                val userNameDisplay =
                                    findViewById<TextView>(R.id.txtUserDetailName)
                                userNameDisplay.text = value
                            }
                            (field == "password") ->{
                                val userPassword =
                                    findViewById<TextView>(R.id.txtUserDetailPassword)
                                userPassword.text = value
                            }
                            (field == "role") -> {
                                val userRole =
                                    findViewById<TextView>(R.id.txtUserDetailRole)
                                Log.d(TAG, "role: $value")
                                when(value.equals("admin", false)){
                                    true -> userRole.text = "管理者"
                                    else ->  userRole.text = "一般ユーザー"
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "userDetailListener:onCancelled: ${error.message}")
                }
            }
            userDetailRef.addValueEventListener(userDetailListener)
        } else {
            Toast.makeText(this,
                "ユーザー取得出来ません。ユーザーから再選択してください。",
                Toast.LENGTH_LONG).show()
        }

        val btnUserDetailBack = findViewById<Button>(R.id.btnUserDetailBack)

        btnUserDetailBack.setOnClickListener {
            val intentBack = Intent(this, UserListActivity::class.java)
            startActivity(intentBack)
        }
    }
}
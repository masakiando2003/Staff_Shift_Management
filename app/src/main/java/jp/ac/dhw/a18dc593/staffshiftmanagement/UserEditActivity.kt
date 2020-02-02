package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import ir.drax.netwatch.NetWatch
import ir.drax.netwatch.cb.NetworkChangeReceiver_navigator
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class UserEditActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserEditActivity"
        private const val IMAGE_DIRECTORY = "/SSM"
    }

    private lateinit var userDetailRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userDetailListener: ValueEventListener
    private val gallery = 1
    private val camera = 2
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private var avatarBase64: String? = null
    private lateinit var auth: FirebaseAuth
    private var oldUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit)

        NetWatch.builder(this)
            .setIcon(R.drawable.ic_signal_wifi_off_black_12dp)
            .setCallBack(object : NetworkChangeReceiver_navigator {
                override fun onConnected(source: Int) {
                }
                override fun onDisconnected() {
                    Toast.makeText(this@UserEditActivity,
                        "ネットワークにアクセス出来ません。" +
                                "コネクションをチェックしてください。", Toast.LENGTH_SHORT).show()
                }
            })
            .setNotificationCancelable(false)
            .build()

        val btnSelectImage = findViewById<Button>(R.id.btnSelectAvatar)
        val btnRemoveImage = findViewById<Button>(R.id.btnRemoveAvatar)

        btnSelectImage!!.setOnClickListener { showPictureDialog() }
        btnRemoveImage!!.setOnClickListener {
            val imgUserAvatar = findViewById<ImageView>(R.id.imgUserAvatar)
            imgUserAvatar!!.setImageDrawable(null)
            avatarBase64 = null
        }

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
                                    findViewById<TextView>(R.id.txtUserEmail)
                                userEmail.text = value
                                oldUserEmail = value
                            }
                            (field == "name") -> {
                                val userNameDisplay =
                                    findViewById<TextView>(R.id.txtUserName)
                                userNameDisplay.text = value
                            }
                            (field == "password") -> {
                                val userPassword =
                                    findViewById<TextView>(R.id.txtUserPassword)
                                userPassword.text = child.value.toString()
                            }
                            (field == "role") -> {
                                val userRole = findViewById<Spinner>(R.id.spnRole)
                                val role: String? =
                                    if (value.equals("admin", false)){
                                            "管理者"
                                    } else {
                                        "一般ユーザー"
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
            editUserRole = when(editUserRole == "一般ユーザー") {
                true -> "user"
                false -> "admin"
            }
            val userData = UserModel(userName, editUserEmail, editUserPassword,
                editUserRole, avatarBase64)

            if(editUserName != oldUserName){
                databaseReference.child("users")
                    .child(editUserName).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            val userCount = p0.childrenCount
                            if(userCount > 0){
                                Toast.makeText(this@UserEditActivity,
                                    "ユーザー: $editUserName はデータベースに存在しています",
                                    Toast.LENGTH_LONG).show()
                            }
                            else {
                                databaseReference.child("users")
                                    .child(oldUserName).removeValue()
                                databaseReference.child("users")
                                    .child(editUserName).setValue(userData)
                                Log.d(TAG, "editUserEmail: $editUserEmail," +
                                        " oldUserEmail: $oldUserEmail")
                                if(editUserEmail != oldUserEmail){
                                    auth = FirebaseAuth.getInstance()
                                    auth.createUserWithEmailAndPassword(editUserEmail,
                                        editUserPassword)
                                        .addOnCompleteListener { task2->
                                            if (task2.isSuccessful) {
                                                Log.d(TAG,
                                                    "Create new user in FireBase " +
                                                            "successfully!!")
                                                val currentUser = auth.currentUser
                                                Log.d(TAG,
                                                    "Current User Email: " +
                                                            "${currentUser!!.email}")
                                                currentUser.delete()
                                                    .addOnCompleteListener { task3->
                                                        if (task3.isSuccessful) {
                                                            Log.d(TAG,
                                                                "Delete old user " +
                                                                        "successfully!!")
                                                            databaseReference
                                                                .child("users")
                                                                .child(editUserName)
                                                                .setValue(userData)
                                                            Toast.makeText(
                                                                applicationContext,
                                                                "ユーザー:  $editUserName " +
                                                                        "を更新しました",
                                                                Toast.LENGTH_SHORT).show()
                                                            val intentBack = Intent(
                                                                applicationContext,
                                                                UserListActivity::class.java)
                                                            startActivity(intentBack)
                                                        } else {
                                                            Log.d(TAG,
                                                                "Delete old user " +
                                                                        "failure...")
                                                        }
                                                    }
                                            }
                                            else{
                                                Log.d(TAG, "Create new user failure...")
                                            }
                                        }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "userCountListener:onCancelled: ${error.message}")
                        }
                })
            } else{
                if(editUserEmail != oldUserEmail){
                    auth = FirebaseAuth.getInstance()
                    auth.createUserWithEmailAndPassword(editUserEmail, editUserPassword)
                        .addOnCompleteListener { task2->
                            if (task2.isSuccessful) {
                                Log.d(TAG, "Create new user in FireBase successfully!!")
                                val currentUser = auth.currentUser
                                Log.d(TAG, "Current User Email: ${currentUser!!.email}")
                                currentUser.delete()
                                    .addOnCompleteListener { task3->
                                        if (task3.isSuccessful) {
                                            Log.d(TAG, "Delete old user successfully!!")
                                            databaseReference.child("users")
                                                .child(editUserName).setValue(userData)
                                            Toast.makeText(this,
                                                "ユーザー: $editUserName を更新しました",
                                                Toast.LENGTH_SHORT).show()
                                            val intentBack = Intent(
                                                this, UserListActivity::class.java)
                                            startActivity(intentBack)
                                        } else {
                                            Log.d(TAG, "Delete old user failure...")
                                        }
                                    }
                            }
                            else{
                                Log.d(TAG, "Create new user failure...")
                            }
                        }
                }
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

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("選択してください")
        val pictureDialogItems = arrayOf("ギャラリーから選択", "カメラで撮影")
        pictureDialog.setItems(pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, gallery)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, camera)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "requestCode: $requestCode, data.data: ${data?.data}")

        if (requestCode == gallery)
        {
            if (data != null)
            {
                val contentURI = data.data
                try
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,
                        contentURI)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG, 100,
                        byteArrayOutputStream)
                    avatarBase64 = Base64.getEncoder()
                        .encodeToString(byteArrayOutputStream.toByteArray())
                    val resizedBitmap = resizeBitmap(bitmap,250,250)
                    val imgUserAvatar = findViewById<ImageView>(R.id.imgUserAvatar)
                    imgUserAvatar!!.setImageBitmap(resizedBitmap)

                }
                catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@UserEditActivity,
                        "アバターの選択失敗した...", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (requestCode == camera)
        {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            val byteArrayOutputStream = ByteArrayOutputStream()
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100,
                byteArrayOutputStream)
            avatarBase64 = Base64.getEncoder()
                .encodeToString(byteArrayOutputStream.toByteArray())
            val resizedThumbnail = resizeBitmap(thumbnail,400,400)
            val imgUserAvatar = findViewById<ImageView>(R.id.imgUserAvatar)
            imgUserAvatar!!.setImageBitmap(resizedThumbnail)
            saveImage(thumbnail)
        }
    }

    private fun saveImage(myBitmap: Bitmap):String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        avatarBase64 = Base64.getEncoder()
            .encodeToString(bytes.toByteArray())
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
        )
        Log.d(TAG,wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists())
        {

            wallpaperDirectory.mkdirs()
        }

        try
        {
            Log.d(TAG,wallpaperDirectory.toString())
            val f = File(wallpaperDirectory, ((Calendar.getInstance()
                .timeInMillis).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this,
                arrayOf(f.path),
                arrayOf("image/jpeg"), null)
            fo.close()
            Log.d(TAG, "ファイルを保存した::--->" + f.absolutePath)

            return f.absolutePath
        }
        catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }

    override fun onStart() {
        super.onStart()
        if (hasNoPermissions()) {
            requestPermission()
        }
    }

    private fun hasNoPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions,0)
    }

    private fun resizeBitmap(bitmap: Bitmap, width:Int, height:Int): Bitmap {
        return Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            false
        )
    }
}
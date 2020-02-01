package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class UserRegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserRegisterActivity"
        private const val IMAGE_DIRECTORY = "/SSM"
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val gallery = 1
    private val camera = 2
    private val permissions = arrayOf(Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private var avatarBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit)

        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val btnSelectImage = findViewById<Button>(R.id.btnSelectAvatar)
        val btnRemoveImage = findViewById<Button>(R.id.btnRemoveAvatar)

        btnSelectImage!!.setOnClickListener { showPictureDialog() }
        btnRemoveImage!!.setOnClickListener {
            val imgUserAvatar = findViewById<ImageView>(R.id.imgUserAvatar)
            imgUserAvatar!!.setImageDrawable(null)
            avatarBase64 = null
        }

        val btnUserRegisterSubmit = findViewById<Button>(R.id.btnUserRegisterSubmit)

        btnUserRegisterSubmit.setOnClickListener {
            var submitFlag = false

            val userName = findViewById<TextView>(R.id.txtUserName).text.toString()
            val userEmail = findViewById<TextView>(R.id.txtUserEmail).text.toString()
            val userPassword = findViewById<TextView>(R.id.txtUserPassword).text.toString()
            var userRole = findViewById<Spinner>(R.id.spnRole).selectedItem.toString()
            userRole = when(userRole.equals("一般ユーザー", false)) {
                true -> "user"
                false ->  "admin"
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
                            val userData = UserModel(userName, userEmail, userPassword,
                                userRole, avatarBase64)
                            databaseReference.child("users")
                                .child(userName).setValue(userData)
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
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                        byteArrayOutputStream)
                    avatarBase64 = Base64.getEncoder()
                        .encodeToString(byteArrayOutputStream.toByteArray())
                    val resizedBitmap = resizeBitmap(bitmap,250,250)
                    val imgUserAvatar = findViewById<ImageView>(R.id.imgUserAvatar)
                    imgUserAvatar!!.setImageBitmap(resizedBitmap)

                }
                catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@UserRegisterActivity,
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
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
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

    private fun resizeBitmap(bitmap:Bitmap, width:Int, height:Int):Bitmap{
        return Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            false
        )
    }
}
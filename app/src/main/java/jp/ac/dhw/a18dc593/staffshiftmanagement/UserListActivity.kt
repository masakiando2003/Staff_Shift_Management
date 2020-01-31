package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.io.Serializable

class UserListActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "UserListActivity"
    }

    private val myPREFERENCES = "MyPrefs"
    private var mySharedPreferences: SharedPreferences? = null

    private lateinit var userListRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userListListener: ValueEventListener
    
    private var userRecyclerListView: RecyclerView? = null
    private var userRecyclerAdapter: UserListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_list)

        Toast.makeText(this@UserListActivity, "少々お待ちください...", 
            Toast.LENGTH_SHORT).show()

        userRecyclerListView = findViewById(R.id.rvUserList)
        val userDataItems = arrayListOf<UserListItem>()

        mySharedPreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!mySharedPreferences!!.contains("email")){
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }

        databaseReference = FirebaseDatabase.getInstance().reference

        userListRef = databaseReference.child("users")
        userListListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "Number of messages: " +
                        "${dataSnapshot.childrenCount}")
                dataSnapshot.children.forEach { child ->
                    userRecyclerAdapter = null
                    userRecyclerListView!!.adapter = userRecyclerAdapter

                    val parentUserDataItem = UserListItem()
                    parentUserDataItem.userName=child.key!!.toString()

                    val userActionItems = arrayListOf<UserActionItem>()
                    var userActionItem: UserActionItem
                    userActionItem = UserActionItem()
                    userActionItem.actionName=parentUserDataItem.userName+"のデータを見る"
                    userActionItems.add(userActionItem)
                    if(mySharedPreferences!!.contains("loginUserRole") &&
                        mySharedPreferences!!.getString("loginUserRole", null)?.
                            toString() == "admin") {
                        userActionItem = UserActionItem()
                        userActionItem.actionName=parentUserDataItem.userName+"を編集する"
                        userActionItems.add(userActionItem)
                        userActionItem = UserActionItem()
                        userActionItem.actionName=parentUserDataItem.userName+"を削除する"
                        userActionItems.add(userActionItem)
                    }
                    parentUserDataItem.userActionItems=userActionItems
                    userDataItems.add(parentUserDataItem)
                }
                val layoutManager = LinearLayoutManager(this@UserListActivity)
                userRecyclerListView!!.layoutManager = layoutManager
                userRecyclerAdapter = UserListAdapter(this@UserListActivity,
                    userDataItems)
                userRecyclerListView!!.addItemDecoration(
                    DividerItemDecoration(
                        userRecyclerListView!!.context, layoutManager.orientation
                    )
                )
                userRecyclerListView!!.adapter = userRecyclerAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "messages:onCancelled: ${error.message}")
            }
        }
        userListRef.addValueEventListener(userListListener)

        val btnUserListBack = findViewById<Button>(R.id.btnUserListBack)

        btnUserListBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    private inner class UserListAdapter(internal var context: Context,
                                        internal var mData: List<UserListItem>) : 
        RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.user_list_adapter, parent, 
                    false)
            return UserListViewHolder(view)
        }

        override fun onBindViewHolder(holder:UserListViewHolder, position: Int) {
            val userListItem = mData[position]
            holder.userName.text = userListItem.userName
            val noOfChildTextViews = holder.linearLayoutChildItems.childCount
            val noOfChild = userListItem.userActionItems!!.size
            if (noOfChild < noOfChildTextViews) {
                for (index in noOfChild until noOfChildTextViews) {
                    val currentTextView =
                        holder.linearLayoutChildItems.getChildAt(index) as TextView
                    currentTextView.visibility = View.GONE
                }
            }
            for (textViewIndex in 0 until noOfChild) {
                val currentTextView =
                    holder.linearLayoutChildItems.getChildAt(textViewIndex) as TextView
                currentTextView.text = userListItem.userActionItems!![textViewIndex].actionName
            }
        }

        override fun getItemCount(): Int {
            return mData.size
        }

        inner class UserListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            internal var userName: TextView = itemView.findViewById(R.id.userName)
            val linearLayoutChildItems: LinearLayout  = itemView.findViewById(R.id.ll_child_items)

            init {
                linearLayoutChildItems.visibility = View.GONE
                var intMaxNoOfChild = 0
                for (index in mData.indices) {
                    val intMaxSizeTemp = mData[index].userActionItems!!.size
                    if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp
                }
                for (indexView in 0 until intMaxNoOfChild) {
                    val textView = TextView(context)
                    textView.id = indexView
                    textView.gravity = Gravity.START
                    textView.setPadding(80, 60, 0, 60)
                    /*textView.background =
                        ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_down_black_24dp)*/
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textView.setOnClickListener(this)
                    linearLayoutChildItems.addView(textView, layoutParams)
                }
                userName.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                if (view.id == R.id.userName) {
                    if (linearLayoutChildItems.visibility == View.VISIBLE) {
                        linearLayoutChildItems.visibility = View.GONE
                    } else {
                        linearLayoutChildItems.visibility = View.VISIBLE
                    }
                } else {
                    val textViewClicked = view as TextView
                    val chosenAction = textViewClicked.text.toString()
                    if(chosenAction.contains("見る")){
                        val userArr = chosenAction.split("の")
                        val userName = userArr[0]
                        val intent = Intent(this@UserListActivity,
                            UserDetailActivity::class.java)
                        intent.putExtra("userName", userName)
                        startActivity(intent)
                    }
                    if(chosenAction.contains("編集")){
                        val userArr = chosenAction.split("を")
                        val userName = userArr[0]
                        val intent = Intent(this@UserListActivity,
                            UserEditActivity::class.java)
                        intent.putExtra("userName", userName)
                        startActivity(intent)
                    }
                    else if(chosenAction.contains("削除")){
                        val userArr = chosenAction.split("を")
                        val userName = userArr[0]
                        AlertDialog.Builder(context)
                            .setTitle("注意!")
                            .setMessage(userName+"を削除してもよろしいでしょうか?")
                            .setPositiveButton("OK") { _, _ ->
                                databaseReference.child("users")
                                    .child(userName).removeValue()
                                Toast.makeText(context, "ユーザー: $userName を削除しました",
                                    Toast.LENGTH_SHORT).show()
                                val intentRefresh = Intent(context, UserListActivity::class.java)
                                startActivity(intentRefresh)
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                }
            }
        }
    }

    private inner class UserListItem : Serializable {
        var userName: String? = null
        var userActionItems: ArrayList<UserActionItem>? = null
    }

    private inner class UserActionItem : Serializable {
        var actionName: String? = null
    }

}
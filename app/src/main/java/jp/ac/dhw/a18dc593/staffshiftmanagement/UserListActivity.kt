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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.io.Serializable;

class UserListActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "UserList"
    }


    val myPREFERENCES = "MyPrefs"
    var sharedpreferences: SharedPreferences? = null

    private lateinit var companyInfoRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var companyInfoListener: ValueEventListener
    
    private var userRecyclerListView: RecyclerView? = null
    private var userRecyclerAdapter: userListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_list)

        Toast.makeText(this@UserListActivity, "少々お待ちください...", Toast.LENGTH_SHORT).show()

        userRecyclerListView = findViewById(R.id.rvUserList) as RecyclerView
        val UserDataItems = arrayListOf<UserListItem>()
        val UserActionItems = arrayListOf<UserActionItem>()

        var UserActionItem: UserActionItem
        UserActionItem = UserActionItem()
        UserActionItem.actionName="編集"
        UserActionItems.add(UserActionItem)
        UserActionItem = UserActionItem()
        UserActionItem.actionName="削除"
        UserActionItems.add(UserActionItem)

        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!sharedpreferences!!.contains("email")){
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }

        databaseReference = FirebaseDatabase.getInstance().reference

        companyInfoRef = databaseReference.child("users")
        companyInfoListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // New data at this path. This method will be called after every change in the
                // data at this path or a subpath.

                Log.d(UserListActivity.TAG, "Number of messages: ${dataSnapshot.childrenCount}")
                dataSnapshot.children.forEach { child ->
                    val ParentUserDataItem: UserListItem
                    ParentUserDataItem = UserListItem()
                    ParentUserDataItem.userName=child.key!!.toString()

                    ParentUserDataItem.UserActionItems=UserActionItems
                    UserDataItems.add(ParentUserDataItem)
                }
                val layoutManager = LinearLayoutManager(this@UserListActivity)
                userRecyclerListView!!.setLayoutManager(layoutManager)
                userRecyclerAdapter = userListAdapter(this@UserListActivity, UserDataItems)
                userRecyclerListView!!.addItemDecoration(DividerItemDecoration(userRecyclerListView!!.getContext(), layoutManager.orientation))
                userRecyclerListView!!.setAdapter(userRecyclerAdapter)
            }

            override fun onCancelled(error: DatabaseError) {
                // Could not successfully listen for data, log the error
                Log.e(UserListActivity.TAG, "messages:onCancelled: ${error.message}")
            }
        }
        companyInfoRef.addValueEventListener(companyInfoListener)

        val btnUserListBack = findViewById<Button>(R.id.btnUserListBack)

        btnUserListBack.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    private inner class userListAdapter(internal var context: Context, internal var mData: List<UserListItem>) : RecyclerView.Adapter<userListAdapter.myViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): userListAdapter.myViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.user_list_adapter, parent, false)
            return myViewHolder(view)
        }

        override fun onBindViewHolder(holder: userListAdapter.myViewHolder, position: Int) {
            val UserListItem = mData[position]
            holder.userName.text = UserListItem.userName
            val noOfChildTextViews = holder.linearLayout_childItems.childCount
            val noOfChild = UserListItem.UserActionItems!!.size
            if (noOfChild < noOfChildTextViews) {
                for (index in noOfChild until noOfChildTextViews) {
                    val currentTextView =
                        holder.linearLayout_childItems.getChildAt(index) as TextView
                    currentTextView.visibility = View.GONE
                }
            }
            for (textViewIndex in 0 until noOfChild) {
                val currentTextView =
                    holder.linearLayout_childItems.getChildAt(textViewIndex) as TextView
                currentTextView.setText(UserListItem.UserActionItems!![textViewIndex].actionName)
            }
        }

        override fun getItemCount(): Int {
            return mData.size
        }

        inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            internal var userName: TextView
            val linearLayout_childItems: LinearLayout

            init {
                userName = itemView.findViewById(R.id.userName)
                linearLayout_childItems = itemView.findViewById(R.id.ll_child_items)
                linearLayout_childItems.visibility = View.GONE
                var intMaxNoOfChild = 0
                for (index in mData.indices) {
                    val intMaxSizeTemp = mData[index].UserActionItems!!.size
                    if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp
                }
                for (indexView in 0 until intMaxNoOfChild) {
                    val textView = TextView(context)
                    textView.id = indexView
                    textView.gravity = Gravity.LEFT
                    textView.setPadding(80, 60, 0, 60)
                    /*textView.background =
                        ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_down_black_24dp)*/
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textView.setOnClickListener(this)
                    linearLayout_childItems.addView(textView, layoutParams)
                }
                userName.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                if (view.getId() == R.id.userName) {
                    if (linearLayout_childItems.visibility == View.VISIBLE) {
                        linearLayout_childItems.visibility = View.GONE
                    } else {
                        linearLayout_childItems.visibility = View.VISIBLE
                    }
                } else {
                    val textViewClicked = view as TextView
                    Toast.makeText(context, "Parent: " + textViewClicked.text.toString(), Toast.LENGTH_SHORT
                    ).show()
                    val chosenAction = textViewClicked.text.toString()
                    if(chosenAction == "編集"){

                    }
                    else if(chosenAction == "削除"){

                    }
                }
            }
        }
    }

    private inner class UserListItem : Serializable {
        var userName: String? = null
        var UserActionItems: ArrayList<UserActionItem>? = null
    }

    private inner class UserActionItem : Serializable {
        var actionName: String? = null
    }

}
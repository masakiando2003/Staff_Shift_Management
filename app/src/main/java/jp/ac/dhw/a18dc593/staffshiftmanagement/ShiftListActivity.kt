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
import java.io.Serializable

class ShiftListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ShiftList"
    }


    val myPREFERENCES = "MyPrefs"
    var sharedpreferences: SharedPreferences? = null

    private lateinit var shiftListRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var shiftListListener: ValueEventListener

    private var shiftRecyclerListView: RecyclerView? = null
    private var shiftListRecyclerAdapter: shiftListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_list)

        Toast.makeText(this@ShiftListActivity, "少々お待ちください...", Toast.LENGTH_SHORT).show()

        shiftRecyclerListView = findViewById(R.id.rvShiftList) as RecyclerView
        val ShiftDataItems = arrayListOf<ShiftListItem>()
        //val UserDataItems = arrayListOf<UserListItem>()
        val ShiftActionItems = arrayListOf<ShiftActionItem>()

        var ShiftActionItem: ShiftActionItem
        ShiftActionItem = ShiftActionItem()
        ShiftActionItem.actionName="編集"
        ShiftActionItems.add(ShiftActionItem)
        ShiftActionItem = ShiftActionItem()
        ShiftActionItem.actionName="削除"
        ShiftActionItems.add(ShiftActionItem)

        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!sharedpreferences!!.contains("email")){
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }

        databaseReference = FirebaseDatabase.getInstance().reference

        shiftListRef = databaseReference.child("shift_list")
        shiftListListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // New data at this path. This method will be called after every change in the
                // data at this path or a subpath.

                Log.d(TAG, "Number of messages: ${dataSnapshot.childrenCount}")
                dataSnapshot.children.forEach { child ->
                    Log.d(TAG, "${child!!.key} => ${child!!.value}")

                    val ParentUserDataItem: ShiftListItem
                    ParentUserDataItem = ShiftListItem()
                    ParentUserDataItem.date=child.key!!.toString()

                    ParentUserDataItem.ShiftActionItems=ShiftActionItems
                    ShiftDataItems.add(ParentUserDataItem)
                }
                val layoutManager = LinearLayoutManager(this@ShiftListActivity)
                shiftRecyclerListView!!.setLayoutManager(layoutManager)
                shiftListRecyclerAdapter = shiftListAdapter(this@ShiftListActivity, ShiftDataItems)
                shiftRecyclerListView!!.addItemDecoration(DividerItemDecoration(shiftRecyclerListView!!.getContext(), layoutManager.orientation))
                shiftRecyclerListView!!.setAdapter(shiftListRecyclerAdapter)
            }

            override fun onCancelled(error: DatabaseError) {
                // Could not successfully listen for data, log the error
                Log.e(TAG, "messages:onCancelled: ${error.message}")
            }
        }
        shiftListRef.addValueEventListener(shiftListListener)

        val btnShiftListBack = findViewById<Button>(R.id.btnShiftListBack)

        btnShiftListBack.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    private inner class shiftListAdapter(internal var context: Context, internal var mData: List<ShiftListItem>) : RecyclerView.Adapter<shiftListAdapter.myViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): shiftListAdapter.myViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.shift_list_adapter, parent, false)
            return myViewHolder(view)
        }

        override fun onBindViewHolder(holder: shiftListAdapter.myViewHolder, position: Int) {
            val ShiftListItem = mData[position]
            holder.date.text = ShiftListItem.date
            val noOfChildTextViews = holder.linearLayout_childItems.childCount
            val noOfChild = ShiftListItem.ShiftActionItems!!.size
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
                currentTextView.setText(ShiftListItem.ShiftActionItems!![textViewIndex].actionName)
            }
        }

        override fun getItemCount(): Int {
            return mData.size
        }

        inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            internal var date: TextView
            val linearLayout_childItems: LinearLayout

            init {
                date = itemView.findViewById(R.id.txtShiftDate)
                linearLayout_childItems = itemView.findViewById(R.id.llShiftListUsers)
                linearLayout_childItems.visibility = View.GONE
                var intMaxNoOfChild = 0
                for (index in mData.indices) {
                    val intMaxSizeTemp = mData[index].ShiftActionItems!!.size
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
                date.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                if (view.getId() == R.id.txtShiftDate) {
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

    private inner class ShiftListItem : Serializable {
        var date: String? = null
        var ShiftActionItems: ArrayList<ShiftActionItem>? = null
    }

    /*
    private inner class UserListItem : Serializable {
        var userName: String? = null
        var ShiftActionItems: ArrayList<ShiftActionItem>? = null
    }*/

    private inner class ShiftActionItem : Serializable {
        var actionName: String? = null
    }
}
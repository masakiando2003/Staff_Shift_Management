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
        private const val TAG = "ShiftListActivity"
    }
    
    private val myPREFERENCES = "MyPrefs"
    var sharedpreferences: SharedPreferences? = null

    private lateinit var shiftListRef: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var shiftListListener: ValueEventListener

    private var shiftRecyclerListView: RecyclerView? = null
    private var shiftListRecyclerAdapter: ShiftListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shift_list)

        Toast.makeText(this@ShiftListActivity, "少々お待ちください...", Toast.LENGTH_SHORT).show()

        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!sharedpreferences!!.contains("email")){
            val loginIntent = Intent(this, LogInActivity::class.java)
            startActivity(loginIntent)
        }

        shiftRecyclerListView = findViewById(R.id.rvShiftList)
        val shiftDataItems = arrayListOf<ShiftListItem>()
        val shiftActionItems = arrayListOf<ShiftActionItem>()

        databaseReference = FirebaseDatabase.getInstance().reference

        shiftListRef = databaseReference.child("shift_list")
        shiftListListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "Number of messages: ${dataSnapshot.childrenCount}")
                dataSnapshot.children.forEach { child ->
                    shiftListRecyclerAdapter = null
                    shiftRecyclerListView!!.adapter = shiftListRecyclerAdapter

                    val parentShiftDataItem = ShiftListItem()
                    parentShiftDataItem.date=child.key!!.toString()

                    val userActionItems = arrayListOf<ShiftActionItem>()
                    var shiftActionItem = ShiftActionItem()
                    shiftActionItem.actionName=parentShiftDataItem.userName+"のデータを見る"
                    userActionItems.add(shiftActionItem)
                    if(sharedpreferences!!.contains("loginUserRole") && 
                        sharedpreferences!!.getString("loginUserRole", null)?.toString() == "admin") {
                        shiftActionItem = ShiftActionItem()
                        shiftActionItem.actionName=parentShiftDataItem.userName+"を編集する"
                        userActionItems.add(shiftActionItem)
                        shiftActionItem = ShiftActionItem()
                        shiftActionItem.actionName=parentShiftDataItem.userName+"を削除する"
                        userActionItems.add(shiftActionItem)
                    }

                    parentShiftDataItem.shiftActionItems=shiftActionItems
                    shiftDataItems.add(parentShiftDataItem)
                }
                val layoutManager = LinearLayoutManager(this@ShiftListActivity)
                shiftRecyclerListView!!.setLayoutManager(layoutManager)
                shiftListRecyclerAdapter = ShiftListAdapter(this@ShiftListActivity, shiftDataItems)
                shiftRecyclerListView!!.addItemDecoration(DividerItemDecoration(shiftRecyclerListView!!.getContext(), layoutManager.orientation))
                shiftRecyclerListView!!.setAdapter(shiftListRecyclerAdapter)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "messages:onCancelled: ${error.message}")
            }
        }
        shiftListRef.addValueEventListener(shiftListListener)

        val btnShiftListBack = findViewById<Button>(R.id.btnShiftListBack)

        btnShiftListBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    private inner class ShiftListAdapter(internal var context: Context, internal var mData: List<ShiftListItem>) : RecyclerView.Adapter<ShiftListAdapter.myViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.shift_list_adapter, parent, false)
            return myViewHolder(view)
        }

        override fun onBindViewHolder(holder: myViewHolder, position: Int) {
            val shiftListItem = mData[position]
            holder.date!!.text = shiftListItem.date
            val noOfChildTextViews = holder.lLayoutChildItems!!.childCount
            val noOfChild = shiftListItem.shiftActionItems!!.size
            if (noOfChild < noOfChildTextViews) {
                for (index in noOfChild until noOfChildTextViews) {
                    val currentTextView =
                        holder.lLayoutChildItems.getChildAt(index) as TextView
                    currentTextView.visibility = View.GONE
                }
            }
            for (textViewIndex in 0 until noOfChild) {
                val currentTextView =
                    holder.lLayoutChildItems.getChildAt(textViewIndex) as TextView
                currentTextView.text= shiftListItem.shiftActionItems!![textViewIndex].actionName
            }
        }

        override fun getItemCount(): Int {
            return mData.size
        }

        inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            internal var date: TextView? = itemView.findViewById(R.id.txtShiftDate)
            val lLayoutChildItems: LinearLayout? = itemView.findViewById(R.id.llShiftListUsers)

            init {
                lLayoutChildItems!!.visibility = View.GONE
                var intMaxNoOfChild = 0
                for (index in mData.indices) {
                    val intMaxSizeTemp = mData[index].shiftActionItems!!.size
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
                    lLayoutChildItems.addView(textView, layoutParams)
                }
                date!!.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                if (view.id == R.id.txtShiftDate) {
                    if (lLayoutChildItems!!.visibility == View.VISIBLE) {
                        lLayoutChildItems.visibility = View.GONE
                    } else {
                        lLayoutChildItems.visibility = View.VISIBLE
                    }
                } else {
                    val textViewClicked = view as TextView
                    Toast.makeText(context, "Parent: " + textViewClicked.text.toString(), Toast.LENGTH_SHORT
                    ).show()
                    val chosenAction = textViewClicked.text.toString()
                    when{
                        (chosenAction.contains("編集"))->{

                        }
                        (chosenAction.contains("削除")) ->{

                        }
                    }
                }
            }
        }
    }

    private inner class ShiftListItem : Serializable {
        var date: String? = null
        var userName: String? = null
        var shiftActionItems: ArrayList<ShiftActionItem>? = null
    }
    private inner class ShiftActionItem : Serializable {
        var actionName: String? = null
    }
}
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

        Toast.makeText(this@ShiftListActivity, "少々お待ちください...",
            Toast.LENGTH_SHORT).show()

        val shiftDateFormatted = intent!!.getStringExtra("shiftDateFormatted")
        val shiftDate = intent!!.getStringExtra("shiftDate")
        Log.d(TAG, "shiftDateFormatted: $shiftDateFormatted, shiftDate: $shiftDate")
        if(shiftDate == null && shiftDateFormatted == null){
            Toast.makeText(this@ShiftListActivity,
                "選択した日付が不明です。もう一度選択してください。",
                Toast.LENGTH_SHORT).show()
            val redirectIntent = Intent(this, ShiftDateActivity::class.java)
            startActivity(redirectIntent)
        } else {
            sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
            if(!sharedpreferences!!.contains("email")){
                val loginIntent = Intent(this, LogInActivity::class.java)
                startActivity(loginIntent)
            }

            val txtSelectedDate = findViewById<TextView>(R.id.txtSelectedShiftList)
            txtSelectedDate.text = ("選択した日付: $shiftDateFormatted")

            shiftRecyclerListView = findViewById(R.id.rvShiftList)
            val shiftDataItems = arrayListOf<ShiftListItem>()

            databaseReference = FirebaseDatabase.getInstance().reference

            shiftListRef = databaseReference.child("shift_list").child(shiftDate)
            shiftListListener = object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "Number of messages: ${dataSnapshot.childrenCount}")
                    if(dataSnapshot.childrenCount < 1){
                        Toast.makeText(this@ShiftListActivity,
                            "選択した日付に出勤データがありません。" +
                                    "別の日付を選択してください。",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        dataSnapshot.children.forEach { child ->
                            shiftListRecyclerAdapter = null
                            shiftRecyclerListView!!.adapter = shiftListRecyclerAdapter

                            val parentShiftDataItem = ShiftListItem()
                            parentShiftDataItem.userName=child.key!!.toString()

                            val shiftActionItems = arrayListOf<ShiftActionItem>()
                            var shiftActionItem = ShiftActionItem()
                            shiftActionItem.actionName=parentShiftDataItem.userName+
                                    "の出勤データを見る"
                            shiftActionItems.add(shiftActionItem)
                            if(sharedpreferences!!.contains("loginUserRole") &&
                                sharedpreferences!!.getString("loginUserRole",
                                    null)?.toString() == "admin") {
                                shiftActionItem = ShiftActionItem()
                                shiftActionItem.actionName=parentShiftDataItem.userName+
                                        "の出勤データを編集する"
                                shiftActionItems.add(shiftActionItem)
                                shiftActionItem = ShiftActionItem()
                                shiftActionItem.actionName=parentShiftDataItem.userName+
                                        "の出勤データを削除する"
                                shiftActionItems.add(shiftActionItem)
                            }

                            parentShiftDataItem.shiftActionItems=shiftActionItems
                            shiftDataItems.add(parentShiftDataItem)
                        }
                        val layoutManager = LinearLayoutManager(this@ShiftListActivity)
                        shiftRecyclerListView!!.layoutManager = layoutManager
                        shiftListRecyclerAdapter = ShiftListAdapter(this@ShiftListActivity,
                            shiftDataItems)
                        shiftRecyclerListView!!.addItemDecoration(
                            DividerItemDecoration(shiftRecyclerListView!!.context,
                                layoutManager.orientation)
                        )
                        shiftRecyclerListView!!.adapter = shiftListRecyclerAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "messages:onCancelled: ${error.message}")
                }
            }
            shiftListRef.addValueEventListener(shiftListListener)

            val btnShiftListBack = findViewById<Button>(R.id.btnShiftListBack)

            btnShiftListBack.setOnClickListener {
                val redirectIntent = Intent(this, MainActivity::class.java)
                startActivity(redirectIntent)
            }
        }
    }


    private inner class ShiftListAdapter(internal var context: Context,
                                         internal var mData: List<ShiftListItem>) :
        RecyclerView.Adapter<ShiftListAdapter.ShiftListViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftListViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.shift_list_adapter, parent,
                    false)
            return ShiftListViewHolder(view)
        }

        override fun onBindViewHolder(holder: ShiftListViewHolder, position: Int) {
            val shiftListItem = mData[position]
            holder.staffName!!.text = shiftListItem.userName
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

        inner class ShiftListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            internal var staffName: TextView? = itemView.findViewById(R.id.txtStaffName)
            val lLayoutChildItems: LinearLayout? = itemView.findViewById(R.id.llShiftActions)

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
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textView.setOnClickListener(this)
                    lLayoutChildItems.addView(textView, layoutParams)
                }
                staffName!!.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                if (view.id == R.id.txtStaffName) {
                    if (lLayoutChildItems!!.visibility == View.VISIBLE) {
                        lLayoutChildItems.visibility = View.GONE
                    } else {
                        lLayoutChildItems.visibility = View.VISIBLE
                    }
                } else {
                    val textViewClicked = view as TextView
                    val chosenAction = textViewClicked.text.toString()
                    when{
                        (chosenAction.contains("見る"))->{
                            val userArr = chosenAction.split("の")
                            val userName = userArr[0]
                            val intent = Intent(this@ShiftListActivity,
                                ShiftDetailActivity::class.java)
                            intent.putExtra("userName", userName)
                            startActivity(intent)
                        }
                        (chosenAction.contains("編集"))->{
                            val userArr = chosenAction.split("の")
                            val userName = userArr[0]
                            val intent = Intent(this@ShiftListActivity,
                                ShiftEditActivity::class.java)
                            intent.putExtra("userName", userName)
                            startActivity(intent)
                        }
                        (chosenAction.contains("削除")) ->{
                            val userArr = chosenAction.split("の")
                            val userName = userArr[0]
                            val shiftDateFormatted =
                                intent!!.getStringExtra("shiftDateFormatted")
                            val shiftDate = intent!!.getStringExtra("shiftDate")
                            AlertDialog.Builder(context)
                                .setTitle("注意!")
                                .setMessage(userName+"の出勤データを削除してもよろしいでしょうか?")
                                .setPositiveButton("OK") { _, _ ->
                                    databaseReference.child("shift_list")
                                        .child(shiftDate)
                                        .child(userName).removeValue()
                                    Toast.makeText(context,
                                        "ユーザー: $userName の出勤データを削除しました",
                                        Toast.LENGTH_SHORT).show()
                                    val intentRefresh = Intent(context,
                                        ShiftListActivity::class.java)
                                    intentRefresh.putExtra("shiftDateFormatted",
                                        shiftDateFormatted)
                                    intentRefresh.putExtra("shiftDate", shiftDate)
                                    startActivity(intentRefresh)
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private inner class ShiftListItem : Serializable {
        var userName: String? = null
        var shiftActionItems: ArrayList<ShiftActionItem>? = null
    }
    private inner class ShiftActionItem : Serializable {
        var actionName: String? = null
    }
}

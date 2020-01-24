package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Context
import android.content.Intent
import android.os.Debug
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_list_item.view.*

class MainMenuItemsAdapter(val context:Context, val menuItems: List<MainMenuItems>) : androidx.recyclerview.widget.RecyclerView.Adapter<MainMenuItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.setData(menuItem, position)
    }

    inner class MyViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){

        var currentItem : MainMenuItems? = null;
        var currentItemPosition: Int = 0;

        init {
            itemView.setOnClickListener {
                var intent : Intent? = null
                Toast.makeText(context, currentItem.toString(), Toast.LENGTH_LONG).show()
                if(currentItem.toString().contains("出勤シフト登録")){
                    intent = Intent(context, CreateShiftActivity::class.java)
                } else if(currentItem.toString().contains("会社情報")){
                    intent = Intent(context, CompanyDetailActivity::class.java)
                } else if(currentItem.toString().contains("アカウント設定")) {
                    intent = Intent(context, AccountSettingActivity::class.java)
                } else if(currentItem.toString().contains("ログアウト")) {
                    intent = Intent(context, LogoutActivity::class.java)
                }
                if(intent != null){
                    context.startActivity(intent)
                }
            }
        }

        fun setData(mainMainItems: MainMenuItems?, pos: Int){
            itemView.txvMenuTitle.text = mainMainItems!!.title

            this.currentItem = mainMainItems
            this.currentItemPosition = pos
        }
    }
}
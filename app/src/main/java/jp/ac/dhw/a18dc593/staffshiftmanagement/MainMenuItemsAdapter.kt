package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
                if(currentItem.toString().contains("出勤シフト一覧")){
                    intent = Intent(context, ShiftListActivity::class.java)
                } else if(currentItem.toString().contains("出勤シフト登録")){
                    intent = Intent(context, CreateShiftActivity::class.java)
                } else if(currentItem.toString().contains("会社情報")){
                    intent = Intent(context, CompanyDetailActivity::class.java)
                } else if(currentItem.toString().contains("ユーザーリスト")){
                    intent = Intent(context, UserListActivity::class.java)
                } else if(currentItem.toString().contains("ユーザー登録")) {
                    intent = Intent(context, UserRegisterActivity::class.java)
                } else if(currentItem.toString().contains("ログアウト")) {
                    intent = Intent(context, LogInActivity::class.java)
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
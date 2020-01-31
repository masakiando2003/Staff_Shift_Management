package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.menu_list_item.view.*

class MainMenuItemsAdapter(val context:Context, private val menuItems: List<MainMenuItems>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<MainMenuItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_list_item, parent,
            false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.setData(menuItem, position)
    }

    inner class MyViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){

        private  var currentItem : MainMenuItems? = null
        private var currentItemPosition: Int = 0

        init {
            itemView.setOnClickListener {
                var intent : Intent? = null
                val currentMenuItem = currentItem.toString()
                when {
                    (currentMenuItem.contains("出勤シフト一覧")) ->{
                        intent = Intent(context, ShiftDateActivity::class.java)
                    }
                    (currentMenuItem.contains("出勤シフト登録")) -> {
                        intent = Intent(context, ShiftRegisterActivity::class.java)
                    }
                    (currentMenuItem.contains("会社情報")) -> {
                        intent = Intent(context, CompanyDetailActivity::class.java)
                    }
                    (currentMenuItem.contains("ユーザーリスト")) -> {
                        intent = Intent(context, UserListActivity::class.java)
                    }
                    (currentMenuItem.contains("ユーザー登録")) -> {
                        intent = Intent(context, UserRegisterActivity::class.java)
                    }
                    (currentItem.toString().contains("ログアウト")) -> {
                        intent = Intent(context, LogInActivity::class.java)
                    }
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
package jp.ac.dhw.a18dc593.staffshiftmanagement

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val myPREFERENCES = "MyPrefs"
    val emailKey = "emailKey"
    var sharedpreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE)
        if(!sharedpreferences!!.contains("email")){

        }

        val layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        layoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        mainMenuRecyclerView.layoutManager = layoutManager

        val adapter = MainMenuItemsAdapter(this, Supplier.menu_items)
        mainMenuRecyclerView.adapter = adapter

        val itemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            this,
            androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
        )
        mainMenuRecyclerView.addItemDecoration(itemDecoration)

    }
}

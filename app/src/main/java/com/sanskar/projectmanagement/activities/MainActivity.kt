package com.sanskar.projectmanagement.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.sanskar.projectmanagement.R
import com.sanskar.projectmanagement.adapters.BoardItemsAdapter
import com.sanskar.projectmanagement.firebase.FirestoreClass
import com.sanskar.projectmanagement.models.Board
import com.sanskar.projectmanagement.models.User
import com.sanskar.projectmanagement.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var create_board_fab: FloatingActionButton
    private lateinit var mUserName: String
    private lateinit var boards_list: RecyclerView
    private lateinit var no_boards_available: TextView
    private lateinit var mBoardList: ArrayList<Board>

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }
    @SuppressLint("ResourceType", "InflateParams", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        navigationView = findViewById(R.id.navView)
        navigationView.setNavigationItemSelectedListener(this)
        drawerLayout = findViewById(R.id.drawer_layout)
        create_board_fab = findViewById(R.id.fab_create_board)
        boards_list = findViewById(R.id.rv_board_list)
        no_boards_available = findViewById(R.id.tv_no_boards_available)

        setUpActionBar()

        FirestoreClass().loadUserData(this, true)

        create_board_fab.setOnClickListener {
            val intent = Intent(this, CreateBoard::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }


    private fun setUpActionBar(){
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.nav_menu)
        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.my_profile -> {
                val intent = Intent(this, MyProfile::class.java)
                startActivityForResult(intent, MY_PROFILE_REQUEST_CODE)
                Toast.makeText(this, "My Profile", Toast.LENGTH_SHORT).show()
            }
            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, introActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                Handler().postDelayed({finish()}, 1000)
            }
        }
        Handler().postDelayed({item.isChecked = false
            drawerLayout.closeDrawer(GravityCompat.START)}, 1000)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }
        else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardsList(this)
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        showProgressDialog("Please Wait")
        val navHeaderView = navigationView.getHeaderView(0)
        val profilePic: CircleImageView = navHeaderView.findViewById(R.id.profile_pic)
        val user_name: TextView = navHeaderView.findViewById(R.id.tv_username)
        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(profilePic)
        user_name.text = user.name
        mUserName = user.name

        if(readBoardsList){
            FirestoreClass().getBoardsList(this)
        }
        else{
            hideProgressDialog()
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>){
        mBoardList = boardsList
        hideProgressDialog()
        if(boardsList.size>0){
            boards_list.visibility = View.VISIBLE
            no_boards_available.visibility = View.GONE

            boards_list.layoutManager = LinearLayoutManager(this)
            boards_list.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            boards_list.adapter = adapter

            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            boards_list.visibility = View.GONE
            no_boards_available.visibility = View.VISIBLE
        }
    }
}
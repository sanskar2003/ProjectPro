package com.sanskar.projectmanagement.activities

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sanskar.projectmanagement.R
import com.sanskar.projectmanagement.adapters.MemberListItemsAdapter
import com.sanskar.projectmanagement.firebase.FirestoreClass
import com.sanskar.projectmanagement.models.Board
import com.sanskar.projectmanagement.models.User
import com.sanskar.projectmanagement.utils.Constants

class MembersActivity : BaseActivity() {
    private lateinit var mBoardDetails: Board
    private lateinit var members_list_rv: RecyclerView
    private lateinit var mAssignedMembers: ArrayList<User>
    private var anyChangesMade: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        members_list_rv = findViewById(R.id.rv_members_list)
        setUpActionBar()
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        showProgressDialog("Please Wait")
        FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    private fun setUpActionBar(){
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_member_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.back_btn_white)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUpMembersList(list: ArrayList<User>){
        mAssignedMembers = list
        hideProgressDialog()
        members_list_rv.layoutManager = LinearLayoutManager(this)
        members_list_rv.setHasFixedSize(true)
        val adapter = MemberListItemsAdapter(this, list)
        members_list_rv.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add_btn).setOnClickListener {
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            if(email.isNotEmpty()){
                showProgressDialog("Please Wait")
                if(!mBoardDetails.assignedTo.contains(email)) {
                    FirestoreClass().getMemberDetails(this, email)
                }else{
                    hideProgressDialog()
                    Toast.makeText(this, "Member is already added!", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            else{
                Toast.makeText(this, "Please enter members email address.", Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel_btn).setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun memberDetails(user: User){
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails, user)
    }

    fun memberAssignSuccess(user: User){
        mAssignedMembers.add(user)
        anyChangesMade = true
        setUpMembersList(mAssignedMembers)
    }

    override fun onBackPressed() {
        if(anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
}
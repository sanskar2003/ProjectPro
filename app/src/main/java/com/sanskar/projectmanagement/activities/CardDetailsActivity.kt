package com.sanskar.projectmanagement.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.sanskar.projectmanagement.R
import com.sanskar.projectmanagement.dialogs.LabelColorListDialog
import com.sanskar.projectmanagement.firebase.FirestoreClass
import com.sanskar.projectmanagement.models.Board
import com.sanskar.projectmanagement.models.Card
import com.sanskar.projectmanagement.models.Task
import com.sanskar.projectmanagement.utils.Constants
import org.w3c.dom.Text
import java.text.FieldPosition

class CardDetailsActivity : BaseActivity() {
    private lateinit var mBoardDetails: Board
    private var mTaskListPosition: Int = -1
    private var mCardPosition: Int = -1
    private lateinit var name_card_details_et: EditText
    private lateinit var update_btn: TextView
    private lateinit var select_label_color_tv: TextView
    private var mSelectedColor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        getIntentData()
        setUpActionBar()

        name_card_details_et = findViewById(R.id.et_name_card_details)
        update_btn = findViewById(R.id.tv_update_card_details)
        select_label_color_tv = findViewById(R.id.tv_select_label_color)

        name_card_details_et.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        name_card_details_et.setSelection(name_card_details_et.text.toString().length)

        update_btn.setOnClickListener {
            if(name_card_details_et.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                Toast.makeText(this, "Please enter a card name!", Toast.LENGTH_SHORT).show()
            }
        }

        select_label_color_tv.setOnClickListener {
            labelColorsListDialog()
        }

    }

    private fun setUpActionBar(){
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_card_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.back_btn_white)
        actionBar?.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card -> {
                deleteCard()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails(){
        val card = Card(name_card_details_et.text.toString(), mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo, mSelectedColor)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog("Please Wait")
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun deleteCard(){
        val cardList:ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete ${cardList[mCardPosition].name}.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()
            showProgressDialog("Please Wait")
            cardList.removeAt(mCardPosition)

            val taskList: ArrayList<Task> = mBoardDetails.taskList
            taskList.removeAt(taskList.size-1)

            taskList[mTaskListPosition].cards = cardList

            FirestoreClass().addUpdateTaskList(this, mBoardDetails)
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")
        return colorsList
    }

    private fun setColor(){
        select_label_color_tv.text = ""
        select_label_color_tv.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorsListDialog(){
        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object: LabelColorListDialog(
            this,
            colorsList,
            "Select Label Color"){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }
}
package com.sanskar.projectmanagement.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sanskar.projectmanagement.R
import com.sanskar.projectmanagement.firebase.FirestoreClass
import com.sanskar.projectmanagement.models.Board
import com.sanskar.projectmanagement.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException


class CreateBoard : BaseActivity() {
    private var selectedImageFileUri: Uri? = null
    private lateinit var board_pic: CircleImageView
    private lateinit var mUserName: String
    private lateinit var boardName: EditText
    private lateinit var createBoardBtn: TextView
    private lateinit var adView: AdView
    private lateinit var adRequest: AdRequest

    private var mBoardImageURL: String = ""

    @SuppressLint("MissingInflatedId", "VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        board_pic = findViewById(R.id.create_board_pic)
        boardName = findViewById(R.id.board_name)
        createBoardBtn = findViewById(R.id.createBtn)

        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView)
        adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        setUpActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        board_pic.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }
            else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        createBoardBtn.setOnClickListener {
            if(selectedImageFileUri!=null){
                uploadBoardImage()
            }
            else{
                showProgressDialog("Please Wait")
                createBoard()
            }
        }
    }

    private fun setUpActionBar(){
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_create_board_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }
        }else{
            Toast.makeText(this, "You just denied the permission for storage. You can also allow it from settings", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            selectedImageFileUri = data.data

            try {
                Glide.with(this)
                    .load(selectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(board_pic)
            }catch (e: IOException){
                e.printStackTrace()
            }

        }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun createBoard(){
        val assignedUserList: ArrayList<String> = ArrayList()
        assignedUserList.add(getCurrentUID())

        var board = Board(boardName.text.toString(), mBoardImageURL, mUserName, assignedUserList)

        FirestoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage(){
        showProgressDialog("Please Wait")
        if(selectedImageFileUri != null){
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference
                .child("BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(this, selectedImageFileUri))
            storageRef.putFile(selectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    mBoardImageURL = uri.toString()
                    createBoard()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
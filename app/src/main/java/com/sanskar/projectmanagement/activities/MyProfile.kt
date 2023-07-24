package com.sanskar.projectmanagement.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.sanskar.projectmanagement.models.User
import com.sanskar.projectmanagement.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.util.PriorityQueue
import java.util.Scanner

class MyProfile : BaseActivity() {
    private lateinit var email_profile: EditText
    private lateinit var name_profile: EditText
    private lateinit var phone_profile: EditText
    private lateinit var profile_picture: CircleImageView
    private lateinit var btn_update: TextView
    private lateinit var adView: AdView
    private lateinit var adRequest: AdRequest

    private var selectedImageFileUri: Uri? = null
    private var profileImageURL: String = ""
    private lateinit var userDetails: User

    companion object{

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        email_profile = findViewById(R.id.profile_email)
        name_profile = findViewById(R.id.profile_name)
        phone_profile = findViewById(R.id.profile_phone)
        profile_picture = findViewById(R.id.profile_pic_myProfile)
        btn_update = findViewById(R.id.updateBtn)

        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView)
        adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        setUpActionBar()

        FirestoreClass().loadUserData(this)

        profile_picture.setOnClickListener {
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

        btn_update.setOnClickListener {
            if(selectedImageFileUri != null){
                uploadUserImager()
            }else{
                showProgressDialog("Please Wait")
                updateUserProfileData()
            }
        }
    }

    private fun setUpActionBar(){
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_profile_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.back_btn_white)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataInUI(user: User) {
        userDetails = user
        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(profile_picture)

        name_profile.setText(user.name)
        email_profile.setText(user.email)
        email_profile.setTextColor(Color.parseColor("#808080"))
        if(user.mobile != 0L){
            phone_profile.setText(user.mobile.toString())
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
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(profile_picture)
            }catch (e: IOException){
                e.printStackTrace()
            }

        }
    }

    private fun uploadUserImager(){
        showProgressDialog("Please Wait")
        if(selectedImageFileUri != null){
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference
                .child("USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(this, selectedImageFileUri))
            storageRef.putFile(selectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    profileImageURL = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        Toast.makeText(this, "Your Profile Updated!", Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()

        var anyChangesMade = false
        if(profileImageURL.isNotEmpty() && profileImageURL != userDetails.image){
            userHashMap[Constants.IMAGE] = profileImageURL
            anyChangesMade = true
        }
        if(name_profile.text.toString() != userDetails.name){
            userHashMap[Constants.NAME] = name_profile.text.toString()
            anyChangesMade = true
        }
        if(phone_profile.text.toString() != userDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = phone_profile.text.toString().toLong()
            anyChangesMade = true
        }
        if(anyChangesMade) {
            FirestoreClass().updateUserProfileData(this, userHashMap)
        }
        else{
            Toast.makeText(this, "Not changes made!", Toast.LENGTH_SHORT).show()
        }
        hideProgressDialog()
    }
}

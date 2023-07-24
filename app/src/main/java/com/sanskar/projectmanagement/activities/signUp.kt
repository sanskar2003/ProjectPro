package com.sanskar.projectmanagement.activities

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanskar.projectmanagement.R
import com.sanskar.projectmanagement.firebase.FirestoreClass
import com.sanskar.projectmanagement.models.User

class signUp : BaseActivity() {
    private lateinit var back_btn: LinearLayout
    private lateinit var signupToSignin: TextView
    private lateinit var signUpbtn: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var su_name: EditText
    private lateinit var su_email: EditText
    private lateinit var su_password: EditText
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        back_btn = findViewById(R.id.backBtnLayout)
        signupToSignin = findViewById(R.id.signuptosignin)
        signUpbtn = findViewById(R.id.signUpBtn)
        su_name = findViewById(R.id.name_input)
        su_email = findViewById(R.id.email_input)
        su_password = findViewById(R.id.password_input)
        auth = Firebase.auth

        back_btn.setOnClickListener {
            finish()
        }

        signupToSignin.setOnClickListener {
            val intent = Intent(this, signIn::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            Handler().postDelayed({ finish()}, 1000)
        }

        signUpbtn.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser(){
        val name: String = su_name.text.toString().trim{ it <= ' ' }
        val email: String = su_email.text.toString().trim{ it <= ' ' }
        val password: String = su_password.text.toString().trim{ it <= ' ' }

        if(validateForm(name, email, password)){
            showProgressDialog("Please Wait")
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                    task ->
                    hideProgressDialog()
                    if(task.isSuccessful){
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    }
                    else{
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when{
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(this, "You have successfully registered!",
            Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        Handler().postDelayed({ finish()}, 1000)
    }
}
package com.sanskar.projectmanagement.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sanskar.projectmanagement.activities.CardDetailsActivity
import com.sanskar.projectmanagement.activities.CreateBoard
import com.sanskar.projectmanagement.activities.MainActivity
import com.sanskar.projectmanagement.activities.MembersActivity
import com.sanskar.projectmanagement.activities.MyProfile
import com.sanskar.projectmanagement.activities.TaskListActivity
import com.sanskar.projectmanagement.activities.signIn
import com.sanskar.projectmanagement.activities.signUp
import com.sanskar.projectmanagement.models.Board
import com.sanskar.projectmanagement.models.User
import com.sanskar.projectmanagement.utils.Constants

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: signUp, userInfo: User){
        mFireStore.collection(Constants.Users)
            .document(getCurrUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                Log.i("signUp error", "Error writing user data!")
            }
    }

    fun loadUserData(activity: Activity, readBoardsList: Boolean = false){
        mFireStore.collection(Constants.Users)
            .document(getCurrUserId())
            .get()
            .addOnSuccessListener {
                document ->
                val loggedInUser = document.toObject(User::class.java)!!
                when(activity){
                    is signIn -> {
                        activity.userSignInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfile -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }.addOnFailureListener {
                e ->
                when(activity){
                    is signIn -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
            }
    }

    fun getCurrUserId(): String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun updateUserProfileData(activity: MyProfile, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.Users)
            .document(getCurrUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                activity.profileUpdateSuccess()
            }.addOnFailureListener{
                exception ->
                Toast.makeText(activity, "Error when updating the profile!", Toast.LENGTH_LONG).show()
            }
    }

    fun createBoard(activity: CreateBoard, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created successfully!", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Log.e("Board creation error", "Error creating board!", exception)
            }
    }

    fun getBoardsList(activity: MainActivity){
        Toast.makeText(activity, "Updated!!", Toast.LENGTH_SHORT).show()
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrUserId())
            .get()
            .addOnSuccessListener {
                document ->
                val boardList: ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardsListToUI(boardList)
            }.addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e("Board List error", "Error listing board!", exception)
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document ->
                val board = document.toObject(Board::class.java)
                board!!.documentId = document.id
                activity.boardDetails(board)

            }.addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e("Get Board Details error", "Error getting board details!", exception)
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                when(activity){
                    is TaskListActivity -> {
                        activity.addUpdateTaskListSuccess()
                    }
                    is CardDetailsActivity -> {
                        activity.addUpdateTaskListSuccess()
                    }
                }
            }.addOnFailureListener {
                    exception ->
                when(activity){
                    is TaskListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is CardDetailsActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("Update Task Details error", "Error updating task details!", exception)
            }
    }

    fun getAssignedMembersListDetails(activity: MembersActivity, assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.Users)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                val userList: ArrayList<User> = ArrayList()
                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }
                activity.setUpMembersList(userList)
            }.addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e("Member List error", "Error getting members!", exception)
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        mFireStore.collection(Constants.Users)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document ->
                if(document.documents.size>0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }.addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e("Member search error", "Error searching members!", exception)
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener{
                    exception ->
                activity.hideProgressDialog()
                Toast.makeText(activity, "Error when updating the members!", Toast.LENGTH_LONG).show()
            }
    }

}
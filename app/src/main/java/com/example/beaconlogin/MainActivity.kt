package com.example.beaconlogin

import android.Manifest
import android.app.AlertDialog

import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


import kotlinx.android.synthetic.main.activity_main.Button_Logout
import kotlinx.android.synthetic.main.activity_main.TextField_Password
import kotlinx.android.synthetic.main.activity_main.TextField_username
import kotlinx.android.synthetic.main.activity_main.TextView_status
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference



    private val PERMISSION_REQUEST_COARSE_LOCATION = 1

    companion object {
        public var mBeaconManager: BeaconManager?=null
        lateinit var BeaconNamespaceID: String
        lateinit var BeaconInstanceID: String
        lateinit var loginbut:Button

        private var mInstanceActivity: MainActivity?=null
        fun getmInstanceActivity(): MainActivity? {
            return mInstanceActivity
        }
        var loginFlag=false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("This app needs location access")
                builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_COARSE_LOCATION)
                }
                builder.show()
            }
        }

        mInstanceActivity=this

        mBeaconManager = BeaconManager.getInstanceForApplication(baseContext)
        // Detect the main Eddystone-UID frame:
        mBeaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))

        setContentView(R.layout.activity_main)

        TextView_status.visibility= GONE
        Button_Logout.visibility= GONE
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        loginbut = findViewById(R.id.Button_Login)
        loginbut.isEnabled = false
        loginbut.setOnClickListener { loginActivity() }
        Button_Logout.setOnClickListener { signOut() }
        TextField_username.setOnFocusChangeListener { _, b->fetchBeaconID(b) }
    }

    private fun loginActivity(){

        val Username=TextField_username.text.toString().trim()+"@idrbt.ac.in"
        val Pass=TextField_Password.text.toString()

        if(!validateForm()){
            return
        }


        auth.signInWithEmailAndPassword(Username,Pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",Toast.LENGTH_SHORT).show()
                }

            }

    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = TextField_username.text.toString()
        if (TextUtils.isEmpty(email)) {
            TextField_username.error = "Required."
            valid = false
        } else {
            TextField_username.error = null
        }

        val password = TextField_Password.text.toString()
        if (TextUtils.isEmpty(password)) {
            TextField_Password.error = "Required."
            valid = false
        } else {
            TextField_Password.error = null
        }

        return valid
    }

    private fun updateUI(usr:FirebaseUser?){

        if (usr != null) {
            TextView_status.text = "Login Successful.\n Welcome "+usr.displayName
            TextView_status.visibility= VISIBLE

            TextField_Password.visibility = GONE
            TextField_username.visibility = GONE
            loginbut.visibility = GONE

            loginFlag=true
            Button_Logout.visibility= VISIBLE
        }

    }


        fun signOut() {
            auth.signOut()

            TextView_status.visibility = GONE


            TextField_Password.visibility = VISIBLE
            TextField_username.visibility = VISIBLE
            loginbut.visibility = VISIBLE
            loginbut.isEnabled = false

            Button_Logout.visibility = GONE


            val region =
                Region("Unlock-Region", Identifier.parse(BeaconNamespaceID), Identifier.parse(BeaconInstanceID), null)
            mBeaconManager?.stopRangingBeaconsInRegion(region)
            mBeaconManager?.removeAllMonitorNotifiers()
            loginFlag=false
        }

    private fun fetchBeaconID(b:Boolean){

        if(b)
        {
            mBeaconManager?.removeAllRangeNotifiers()
            mBeaconManager?.removeAllMonitorNotifiers()

        }

        else {

            val uname = TextField_username.text.toString()

            //Toast.makeText(baseContext, uname, Toast.LENGTH_SHORT).show()

            val IDListener = object: ValueEventListener {
               override fun onDataChange(dataSnapshot: DataSnapshot) {

                   var BeaconNamespace:String = dataSnapshot.value.toString()


                   if(BeaconNamespace == "null")
                   {
                       Toast.makeText(baseContext, "Invalid UserName",Toast.LENGTH_SHORT).show()
                       loginbut.isEnabled = false
                   }
                   else
                   {

                        BeaconNamespaceID= BeaconNamespace.substringBefore(",")
                        BeaconInstanceID=BeaconNamespace.substringAfter(",")
                        Toast.makeText(baseContext, BeaconNamespaceID,Toast.LENGTH_SHORT).show()
                        var beaconMonitor=BeaconMonitor()

                        beaconMonitor.onResume()
                   }

               }

               override fun onCancelled(databaseError: DatabaseError) {
                  Toast.makeText(baseContext,"Data Fetch Failed",Toast.LENGTH_SHORT).show()
                   println("loadPost:onCancelled ${databaseError.toException()}")

               }
           }
           database.child("BeaconIDs").child(uname).addListenerForSingleValueEvent(IDListener)

        }
    }

}


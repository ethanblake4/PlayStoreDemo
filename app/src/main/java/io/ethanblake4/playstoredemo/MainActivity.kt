package io.ethanblake4.playstoredemo

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.AccountPicker
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getAppsButton.setOnClickListener {
            pickUserAccount()
        }
    }

    private fun pickUserAccount() {
        val intent = AccountPicker.newChooseAccountIntent(
            null, null, arrayOf("com.google"), true, null, null, null, null
        )
        // check if play-services are installed
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (ConnectionResult.SUCCESS == result) {
            startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT)
        } else {
            // display user friendly error message
            GoogleApiAvailability.getInstance().showErrorDialogFragment(this, result, REQUEST_CODE_PICK_ACCOUNT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT && resultCode == Activity.RESULT_OK) {
            data?.let { accountInfo ->
                getAuthToken(this, accountInfo.getStringExtra(AccountManager.KEY_ACCOUNT_NAME))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ token ->
                        if (token == null) {
                            Toast.makeText(this, "Token is null", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Got token $token", Toast.LENGTH_LONG).show()

                        }
                    }, { err ->
                        Log.e("MainActivity", "Error getting auth token", err)
                        Toast.makeText(this, "Error getting auth token", Toast.LENGTH_LONG).show()
                    })
            }
        }
    }

    private fun getAuthToken(activity: Activity, userEmail: String) = Observable.fromCallable {

        val accountManager = AccountManager.get(activity)
        val userAccount = Account(userEmail, "com.google")

        val options = Bundle().apply {
            putBoolean("suppressProgressScreen", true)
        }

        return@fromCallable accountManager
            .getAuthToken(userAccount, "androidmarket", options, activity, null, null)
            ?.result?.getString("authtoken")
    }

    companion object {
        const val REQUEST_CODE_PICK_ACCOUNT = 442
    }
}

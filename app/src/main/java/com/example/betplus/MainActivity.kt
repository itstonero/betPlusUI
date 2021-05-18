package com.example.betplus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.betplus.models.*
import com.example.betplus.models.views.MainViewModel
import com.example.betplus.services.BetPlusAPI
import com.example.betplus.services.Repo
import retrofit2.Call
import retrofit2.Response

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    lateinit var viewModel:MainViewModel;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        findViewById<TextView>(R.id.text_auth_username).apply {
            this.text = viewModel.email
            this.doOnTextChanged { text, _, _, _ -> viewModel.email = text.toString() }
        }
        findViewById<Button>(R.id.button_login).setOnClickListener{ initiateLogin(it) }
    }

    private fun initiateLogin(view: View){
        findViewById<TextView>(R.id.text_auth_username).text?.apply {
            if(this.toString().isEmpty())
            {
                Toast.makeText(view.context, "Enter a valid email address", Toast.LENGTH_LONG).show()
            }else{
                val apiResponse = Repo.betPlusAPI.signIn(AuthRequest(this.toString()))
                apiResponse?.enqueue(object : retrofit2.Callback<AuthResponse?> {
                    override fun onResponse(call: Call<AuthResponse?>, response: Response<AuthResponse?>) {
                        if(response.code() == 200)
                        {
                            (response.body() as AuthResponse)?.apply {
                                BetPlusAPI.SITE_TOKEN = this.token
                                BetPlusAPI.SITE_USERNAME = this.username
                                openDashboard();
                            }
                        }else
                        {
                            Toast.makeText(view.context, response.message(), Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse?>, t: Throwable) {
                        Toast.makeText(view.context, "Failed to LogIn", Toast.LENGTH_LONG).show()
                    }

                })
            }
        }
    }

    private fun openDashboard() {
        var dashboardIntent = Intent(this, DashboardActivity::class.java)
        startActivity(dashboardIntent)
    }

}
package com.example.betplus.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.betplus.models.SlipRequest
import com.example.betplus.models.SlipResponse
import com.example.betplus.models.SlipUpgradeRequest
import com.example.betplus.services.BetPlusAPI
import com.example.betplus.services.Repo
import retrofit2.Call
import retrofit2.Response

private const val TAG = "HomeViewModel"
class HomeViewModel : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    private val _slip = MutableLiveData<SlipResponse>();
    val errorMessage: LiveData<String> = _errorMessage
    val slip: LiveData<SlipResponse> = _slip
    var amountType = 0;

    fun retrieveSlip(){
        Log.d(TAG, "RETRIEVING SLIP")
        val apiService = Repo.betPlusAPI.retrieveSlip(BetPlusAPI.SITE_USERNAME)
        apiService?.enqueue(object : retrofit2.Callback<SlipResponse?> {
            override fun onResponse(call: Call<SlipResponse?>, response: Response<SlipResponse?>) {
                if(response.code() == 200)
                {
                    (response.body() as SlipResponse)?.apply { _slip.value = this }
                }else
                {
                    _errorMessage.value = response.message().toString()
                }
            }

            override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                _errorMessage.value = "Failed To Retrieve Slip Information"
            }
        })
    }

    fun updateSlip(){
        Log.d(TAG, "UPDATING SLIP")
        val apiService = Repo.betPlusAPI.updateSlip(BetPlusAPI.SITE_USERNAME)
        apiService?.enqueue(object : retrofit2.Callback<SlipResponse?> {
            override fun onResponse(call: Call<SlipResponse?>, response: Response<SlipResponse?>) {
                if(response.code() == 200)
                {
                    (response.body() as SlipResponse)?.apply { _slip.value = this }
                }else
                {
                    _errorMessage.value = response.message().toString()
                }
            }

            override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                _errorMessage.value = "Failed To Retrieve Slip Information"
            }
        })
    }

    fun upgradeSlip() {
        Log.d(TAG, "UPGRADING SLIP")
        val apiService = Repo.betPlusAPI.upgradeSlip(BetPlusAPI.SITE_USERNAME, SlipUpgradeRequest("5.01"))
        apiService?.enqueue(object : retrofit2.Callback<SlipResponse?> {
            override fun onResponse(call: Call<SlipResponse?>, response: Response<SlipResponse?>) {
                if(response.code() == 200)
                {
                    (response.body() as SlipResponse)?.apply { _slip.value = this }
                }else
                {
                    _errorMessage.value = response.message().toString()
                }
            }

            override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                _errorMessage.value = "Failed To Retrieve Slip Information"
            }
        })
    }

    fun createSlip(slipRequest: SlipRequest): Boolean{
        val apiCreate = Repo.betPlusAPI.createSlip(BetPlusAPI.SITE_USERNAME, slipRequest)
        apiCreate?.enqueue(object : retrofit2.Callback<SlipResponse?> {
            override fun onResponse(call: Call<SlipResponse?>, response: Response<SlipResponse?>) {
                if(response.code() == 200)
                {
                    (response.body() as SlipResponse)?.apply {
                        _slip.value = this
                    }
                }else
                {
                    _errorMessage.value = response.message()
                }
            }

            override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                _errorMessage.value = ("Failed To Create Slip")
            }
        })

        return true
    }

    fun reverseSlip(): Boolean {
        Log.d(TAG, "REVERSING SLIP")
        val apiService = Repo.betPlusAPI.reverseSlip(BetPlusAPI.SITE_USERNAME)
        apiService?.enqueue(object : retrofit2.Callback<SlipResponse?> {
            override fun onResponse(call: Call<SlipResponse?>, response: Response<SlipResponse?>) {
                if(response.code() == 200)
                {
                    (response.body() as SlipResponse)?.apply { _slip.value = this }
                }else
                {
                    _errorMessage.value = response.message().toString()
                }
            }

            override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                _errorMessage.value = "Failed To Retrieve Slip Information"
            }
        })
        return true
    }
}
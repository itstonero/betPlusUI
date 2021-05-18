package com.example.betplus.ui.gallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.betplus.models.Fixture
import com.example.betplus.services.BetPlusAPI
import com.example.betplus.services.Repo
import retrofit2.Call
import retrofit2.Response

private const val TAG = "GalleryViewModel"
class GalleryViewModel : ViewModel() {

    private val _allFixtures = MutableLiveData<List<Fixture>>()
    val allFixtures:LiveData<List<Fixture>> = _allFixtures;

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getRegisteredMatches() {
        Log.d(TAG, "Retreiving Registered Matches")
        val apiResponse = Repo.betPlusAPI.getRegisteredFixtures(BetPlusAPI.SITE_USERNAME)
        apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
            override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                _errorMessage.value = response.message()
                if(response.code() == 200)
                {
                    (response.body() as List<Fixture>)?.apply {
                        _allFixtures.value = this
                    }
                }
            }

            override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                _errorMessage.value = ("Failed To Retreive All Games")
            }

        })
    }

    fun removeSingleGame(fixture:Fixture) {
        Log.d(TAG, "Removing Single Game")
        val apiResponse = Repo.betPlusAPI.deleteFixture(BetPlusAPI.SITE_USERNAME, fixture)
        apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
            override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                _errorMessage.value = response.message()
                if(response.code() == 200)
                {
                    _allFixtures.value = _allFixtures.value?.filter { f -> f != fixture }
                }
            }

            override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                _errorMessage.value = ("Failed To Retreive All Games")
            }

        })
    }

    fun updateSingleGame(fixture:Fixture) {
        Log.d(TAG, "Updating Single Game")
        val apiResponse = Repo.betPlusAPI.updateFixture(BetPlusAPI.SITE_USERNAME, fixture)
        apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
            override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                _errorMessage.value = response.message()
            }

            override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                _errorMessage.value = ("Failed To Retreive All Games")
            }

        })
    }
}
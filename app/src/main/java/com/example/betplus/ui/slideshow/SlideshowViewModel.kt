package com.example.betplus.ui.slideshow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.betplus.models.Fixture
import com.example.betplus.services.BetPlusAPI
import com.example.betplus.services.Repo
import com.example.betplus.ui.gallery.AlarmReceiver
import retrofit2.Call
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "SlideshowViewModel"
class SlideshowViewModel : ViewModel() {

    private val _allFixtures = MutableLiveData<List<Fixture>>()
    val allFixtures:LiveData<List<Fixture>> = _allFixtures;

    private val _matchingFixtures = MutableLiveData<List<Fixture>>()
    val matchingFixtures:LiveData<List<Fixture>> = _matchingFixtures;

    private val _selectedFixture = MutableLiveData<ArrayList<Fixture>>().apply { this.value = ArrayList<Fixture>() }
    val selectedFixture: LiveData<ArrayList<Fixture>> = _selectedFixture

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    //private val dbHandler = GameDB()
    fun getAllMatches() {
        Log.d(TAG, "Retrieving all Matches")
        if(allFixtures.value == null) {
            val apiResponse = Repo.betPlusAPI.getAvailableFixture(BetPlusAPI.SITE_USERNAME)
            apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
                override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                    _text.value = response.message()
                    if(response.code() == 200)
                    {
                        (response.body() as List<Fixture>)?.apply {
                            _allFixtures.value = this
                            _matchingFixtures.value = this
                        }
                    }
                }

                override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                    _text.value = ("Failed To Retreive All Games")
                }

            })
        }
    }

    fun toggleFixture(fixture: Fixture) {
        if(_selectedFixture.value?.contains(fixture) !!)
        {
            _selectedFixture.value?.remove(fixture)
        }else{
            _selectedFixture.value?.add(fixture)
        }
    }

    fun findMatches(searchParam: String){
        _matchingFixtures.value = _allFixtures.value?.filter {
            it.away.lowercase().contains(searchParam.lowercase()) || it.home.lowercase().contains(searchParam.lowercase())
        }
    }

    fun registerGames(allowClearing: Boolean, context: Context){
        Log.d(TAG, "Register Games and Allow Clearing => $allowClearing")
        var apiResponse:Call<List<Fixture?>?>?

        if(allowClearing)
            apiResponse= Repo.betPlusAPI.registerFixtures(BetPlusAPI.SITE_USERNAME, selectedFixture.value?.toList()!!)
        else
            apiResponse = Repo.betPlusAPI.updateFixtures(BetPlusAPI.SITE_USERNAME, selectedFixture.value?.toList()!!)

        apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
            override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                _text.value = response.message()
                if(response.code() == 200) {
                    _selectedFixture.value?.clear()

                    (response.body() as List<Fixture>)?.apply {
                        _text.value = "${this.size} Matches Registered"
                        this.forEach { fixture ->
                            fixture.time.split(":").let {
                                Date().apply { hours = it[0].toInt(); minutes = it[1].toInt() }
                                    .let {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            val intent =
                                                Intent(
                                                    context.applicationContext,
                                                    AlarmReceiver::class.java
                                                )
                                            intent.putExtra(
                                                AlarmReceiver.NF_TEAMS,
                                                "${fixture.home} Vs. ${fixture.away}"
                                            )
                                            intent.putExtra(
                                                AlarmReceiver.NF_SCHEDULE,
                                                "${fixture.suggestion} - ${fixture.country} - ${fixture.tournament}"
                                            )
                                            intent.putExtra(AlarmReceiver.NF_ID, fixture.fixtureId)

                                            val cal = Calendar.getInstance()
                                            val alarmTime = cal.apply { time = it }.timeInMillis
                                            val alarmManager =
                                                context.applicationContext.getSystemService(
                                                    Context.ALARM_SERVICE
                                                ) as AlarmManager
                                            val pendingIntent = PendingIntent.getBroadcast(
                                                context,
                                                fixture.fixtureId.toInt(),
                                                intent,
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                            )
                                            alarmManager.set(
                                                AlarmManager.RTC_WAKEUP,
                                                alarmTime,
                                                pendingIntent
                                            )
                                            Toast.makeText(
                                                context,
                                                "Notification SET",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else
                                            Toast.makeText(
                                                context,
                                                "Only on Android N and Above",
                                                Toast.LENGTH_LONG
                                            ).show()
                                    }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                _text.value = ("Failed To Register Matches")
            }

        })
    }
}
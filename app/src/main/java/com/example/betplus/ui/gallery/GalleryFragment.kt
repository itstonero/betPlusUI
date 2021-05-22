package com.example.betplus.ui.gallery

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.betplus.R
import com.example.betplus.databinding.FragmentGalleryBinding
import com.example.betplus.models.Fixture
import com.example.betplus.models.views.DashboardViewModel
import com.example.betplus.services.BetPlusAPI
import com.example.betplus.services.Repo
import retrofit2.Call
import retrofit2.Response
import java.util.*


private const val TAG = "GalleryFragment"
class GalleryFragment : Fragment() {

    private val sharedViewModel:DashboardViewModel by activityViewModels()
    private var _binding: FragmentGalleryBinding? = null
    private lateinit var recyclerAdapter: GalleryAdapter
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sharedViewModel.selectedMatches.observe(viewLifecycleOwner, { fixtures ->
            binding.recyclerGames.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false)
            fixtures.forEach{ fixture ->  alarmSetStatus(fixture) }
            recyclerAdapter = GalleryAdapter(fixtures.sortedBy { fixture -> fixture.time.split(":").let{  (it[0].toInt()* 60 + it[1].toInt()) } }, this)
            binding.recyclerGames.adapter = recyclerAdapter
        })

        if(sharedViewModel.selectedMatches.value == null) getRegisteredMatches()

        return root
    }


    fun alarmSetStatus(fixture: Fixture):Boolean {
        val alarmIsUp = PendingIntent.getBroadcast(
            context, fixture.fixtureId.toInt(), Intent(requireContext(), AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)
        return (alarmIsUp != null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun scheduleNotification(fixture: Fixture){
        Log.d(TAG, "Preparing Notification at ${fixture.time}")
        fixture.time.split(":").let {
            Date().apply { hours = it[0].toInt(); minutes = it[1].toInt() }.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val intent = Intent(requireContext().applicationContext, AlarmReceiver::class.java)
                    intent.putExtra(AlarmReceiver.NF_TEAMS, "${fixture.home} Vs. ${fixture.away}")
                    intent.putExtra(AlarmReceiver.NF_SCHEDULE, "${fixture.suggestion} - ${fixture.country} - ${fixture.tournament}")
                    intent.putExtra(AlarmReceiver.NF_ID, fixture.fixtureId)

                    val cal = Calendar.getInstance()
                    val alarmManager = requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val pendingIntent = PendingIntent.getBroadcast(requireContext(), fixture.fixtureId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, cal.apply { time = it }.timeInMillis, pendingIntent)
                    sharedViewModel.updateSelectedMatches(sharedViewModel.selectedMatches.value?.map { it -> it } ?: listOf())
                    Toast.makeText(context, "Notification for ${cal.time.hours}:${cal.time.minutes}", Toast.LENGTH_LONG).show()
                } else
                    Toast.makeText(context, "Only on Android N and Above", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cancelNotification(fixture: Fixture){
        val intent = Intent(requireContext().applicationContext, AlarmReceiver::class.java)
        intent.putExtra(AlarmReceiver.NF_TEAMS, "${fixture.home} Vs. ${fixture.away}")
        intent.putExtra(AlarmReceiver.NF_SCHEDULE, "${fixture.suggestion} - ${fixture.country} - ${fixture.tournament}")
        intent.putExtra(AlarmReceiver.NF_ID, fixture.fixtureId)

        val alarmManager = requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(context, fixture.fixtureId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

    fun modifyGame(fixture: Fixture) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fixture_options, null, false))
        dialog.findViewById<Button>(R.id.button_fixture_notify).setOnClickListener{
            scheduleNotification(fixture)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.button_fixture_remove).setOnClickListener{
            removeSingleGame(fixture)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.button_fixture_un_notify).setOnClickListener{
            cancelNotification(fixture)
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(600, 400)
    }

    private fun getRegisteredMatches() {
        Log.d(TAG, "Retrieving Registered Matches")
        val apiResponse = Repo.betPlusAPI.getRegisteredFixtures(BetPlusAPI.SITE_USERNAME)
        apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
            override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                if(response.code() == 200)
                {
                    (response.body() as List<Fixture>?)?.apply {
                        sharedViewModel.updateSelectedMatches(this)
                    }
                }else
                    Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                Toast.makeText(context, "Failed To Retrieve Fixtures", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun removeSingleGame(fixture:Fixture) {
        Log.d(TAG, "Removing Single Game")
        val apiResponse = Repo.betPlusAPI.deleteFixture(BetPlusAPI.SITE_USERNAME, fixture)
        apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
            override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                if(response.code() == 200)
                {
                    (response.body() as List<Fixture>)?.apply {
                        sharedViewModel.updateSelectedMatches(this)
                    }
                }else
                    Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                Toast.makeText(context, "Failed To Remove Fixture", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun updateSingleGame(fixture:Fixture) {
        Log.d(TAG, "Updating Single Game")
        val apiResponse = Repo.betPlusAPI.updateFixture(BetPlusAPI.SITE_USERNAME, fixture)
        apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
            override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                if(response.code() == 200)
                {
                    (response.body() as List<Fixture>)?.apply {
                       sharedViewModel.updateSelectedMatches(this)
                    }
                }else
                    Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                Toast.makeText(context, "Failed To Update Fixture", Toast.LENGTH_LONG).show()
            }

        })
    }
}
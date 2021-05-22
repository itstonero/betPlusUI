package com.example.betplus.ui.slideshow

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.betplus.R
import com.example.betplus.databinding.FragmentSlideshowBinding
import com.example.betplus.models.Fixture
import com.example.betplus.models.views.DashboardViewModel
import com.example.betplus.services.BetPlusAPI
import com.example.betplus.services.Repo
import com.example.betplus.ui.gallery.AlarmReceiver
import retrofit2.Call
import retrofit2.Response
import java.util.*


private const val TAG = "SlideshowFragment"
class SlideshowFragment : Fragment() {

    private val sharedViewModel:DashboardViewModel by activityViewModels()
    private var _binding: FragmentSlideshowBinding? = null
    private lateinit var recyclerAdapter: SlideShowAdapter

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.textSlideshowSearch.doOnTextChanged{ text, _, _, _ ->   findMatches(text.toString())}
        binding.buttonRegisterGame.setOnClickListener { loadUp() }
        sharedViewModel.searchResult.observe(viewLifecycleOwner, Observer {
            binding.recyclerSlideshow.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false)
            recyclerAdapter = SlideShowAdapter(it, this)
            binding.recyclerSlideshow.adapter = recyclerAdapter
        })
        getAllMatches()
        return root
    }

    fun toggleFixture(fixture: Fixture){
        if(sharedViewModel.chosenGames.contains(fixture)){
            sharedViewModel.chosenGames.remove(fixture)
            recyclerAdapter.notifyDataSetChanged()
        }else
            makeSuggestion(fixture)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun isSelected(fixture: Fixture): Boolean {
        return sharedViewModel.chosenGames.contains(fixture)
    }

    private fun loadUp(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Register Matches")
        builder.setMessage("Clear Previous Matches")
        builder.setPositiveButton(
            "Clear"
        ) { _, _ -> registerGames(true);}
        builder.setNegativeButton(
            "Retain"
        ) { _, _ -> registerGames(false);}

        builder.create().show()
    }

    private fun makeSuggestion(fixture: Fixture){
        val options = arrayOf("Home Wins", "Away Wins", "Home Win (-1.5AH)", "Away Win (-1.5AH)", "Home Win (-2.5AH)", "Away Win (-2.5AH)")

        val builder  = AlertDialog.Builder(context)
        builder.setTitle("Make Suggestion")
        builder.setItems(options, DialogInterface.OnClickListener { _, which ->
            fixture.suggestion = options[which]
            sharedViewModel.chosenGames.add(fixture)
            recyclerAdapter.notifyDataSetChanged()
            Toast.makeText(context, fixture.suggestion, Toast.LENGTH_LONG).show()
        })
        builder.show()
    }

    private fun getAllMatches() {
        if(sharedViewModel.allMatches.value == null) {
            val apiResponse = Repo.betPlusAPI.getAvailableFixture(BetPlusAPI.SITE_USERNAME)
            apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
                override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                    if(response.code() == 200)
                    {
                        (response.body() as List<Fixture>)?.apply {
                            sharedViewModel.updateAllMatches(this)
                            sharedViewModel.updateSearchResult(this)
                        }
                    }else
                        Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
                }

                override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                    Toast.makeText(context, "Failed to retrieve all games", Toast.LENGTH_LONG).show()
                }

            })
        }
    }

    private fun findMatches(searchParam: String){
        sharedViewModel.updateSearchResult(sharedViewModel.allMatches.value?.filter {
            it.away.lowercase().contains(searchParam.lowercase()) || it.home.lowercase().contains(searchParam.lowercase())
        } ?: listOf())
    }

    private fun registerGames(allowClearing: Boolean){
        var apiResponse: Call<List<Fixture?>?>?  = if(allowClearing)
            Repo.betPlusAPI.registerFixtures(BetPlusAPI.SITE_USERNAME, sharedViewModel.chosenGames.toList())
        else
            Repo.betPlusAPI.updateFixtures(BetPlusAPI.SITE_USERNAME, sharedViewModel.chosenGames.toList())

        sharedViewModel.chosenGames.clear()
        sharedViewModel.updateSearchResult(sharedViewModel.allMatches.value ?: listOf())

        apiResponse?.enqueue(object : retrofit2.Callback<List<Fixture?>?> {
            override fun onResponse(call: Call<List<Fixture?>?>, response: Response<List<Fixture?>?>) {
                if(response.code() == 200) {
                    (response.body() as List<Fixture>)?.apply {
                        sharedViewModel.updateSelectedMatches(this)
                        this.forEach { fixture ->
                            fixture.time.split(":").let {
                                Date().apply { hours = it[0].toInt(); minutes = it[1].toInt() }.let {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        val intent = Intent(requireContext().applicationContext, AlarmReceiver::class.java)
                                        intent.putExtra(AlarmReceiver.NF_TEAMS, "${fixture.home} Vs. ${fixture.away}")
                                        intent.putExtra(AlarmReceiver.NF_SCHEDULE, "${fixture.suggestion} - ${fixture.country} - ${fixture.tournament}")
                                        intent.putExtra(AlarmReceiver.NF_ID, fixture.fixtureId)

                                        val cal = Calendar.getInstance()
                                        val alarmTime = cal.apply { time = it }.timeInMillis
                                        val alarmManager = requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        val pendingIntent = PendingIntent.getBroadcast(context, fixture.fixtureId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                                        Toast.makeText(context, "Notification SET", Toast.LENGTH_LONG).show()
                                    } else
                                        Toast.makeText(context, "Only on Android N and Above", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        this@SlideshowFragment.findNavController().navigate(R.id.nav_gallery)
                    }
                }else
                    Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<Fixture?>?>, t: Throwable) {
                Toast.makeText(context, "Failed to Load Games", Toast.LENGTH_LONG).show()
            }

        })


    }
}
package com.example.betplus.ui.gallery

import android.app.*
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.betplus.databinding.FragmentGalleryBinding
import com.example.betplus.models.Fixture
import java.util.*


private const val TAG = "GalleryFragment"
class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null
    private lateinit var recycler_adapter: GalleryAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        galleryViewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        galleryViewModel.allFixtures.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "Received ${it.size} Matches")
            binding.recyclerGames.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false)
            recycler_adapter = GalleryAdapter(it, this)
            binding.recyclerGames.adapter = recycler_adapter
        })

        if(galleryViewModel.allFixtures.value == null) {
            galleryViewModel.getRegisteredMatches()
        }

        return root
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

                    val alarmManager = requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().apply { time = it }.timeInMillis, pendingIntent)
                } else
                    Toast.makeText(context, "Only on Android N and Above", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun modifyGame(fixture: Fixture) {
        scheduleNotification(fixture)
    }
}
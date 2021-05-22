package com.example.betplus.ui.gallery

import android.app.*
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.betplus.R
import com.example.betplus.databinding.FragmentGalleryBinding
import com.example.betplus.models.Fixture
import java.util.*


private const val TAG = "GalleryFragment"
class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null
    private lateinit var recyclerAdapter: GalleryAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        galleryViewModel.errorMessage.observe(viewLifecycleOwner, Observer { Toast.makeText(context, it, Toast.LENGTH_LONG).show() })
        galleryViewModel.allFixtures.observe(viewLifecycleOwner, Observer {
            binding.recyclerGames.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false)
            it.forEach{ fixture ->  alarmSetStatus(fixture) }
            recyclerAdapter = GalleryAdapter(it.sortedBy { fixture -> fixture.time.split(":").let{  (it[0].toInt()* 60 + it[1].toInt()) } }, this)
            binding.recyclerGames.adapter = recyclerAdapter
        })
        galleryViewModel.getRegisteredMatches()

        return root
    }


    private fun alarmSetStatus(fixture: Fixture) {
        fixture.suggestion.replace("**", "")

        val alarmIsUp = PendingIntent.getBroadcast(
            context, fixture.fixtureId.toInt(), Intent(requireContext(), AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)

        if(alarmIsUp != null) fixture.suggestion = "** ${fixture.suggestion} **"
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
                    Toast.makeText(context, "Notification for ${cal.time.hours}:${cal.time.minutes}", Toast.LENGTH_LONG).show()
                } else
                    Toast.makeText(context, "Only on Android N and Above", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cancelNotification(fixture: Fixture){
        val alarmManager = requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        PendingIntent.getBroadcast(requireContext(),
            fixture.fixtureId.toInt(),
            Intent(requireContext().applicationContext, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
            ?.apply { alarmManager.cancel(this) }
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
            galleryViewModel.removeSingleGame(fixture)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.button_fixture_un_notify).setOnClickListener{
            cancelNotification(fixture)
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(600, 400)
    }
}
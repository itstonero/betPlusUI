package com.example.betplus.ui.gallery

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.betplus.R
import com.example.betplus.models.Fixture
import com.example.betplus.ui.slideshow.SlideshowFragment

class GalleryAdapter (private val dataSet: List<Fixture>, private val selected: GalleryFragment) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val teamText: TextView = view.findViewById<TextView>(R.id.text_slide_adapter_team)
        val tournamentText: TextView = view.findViewById<TextView>(R.id.text_slide_adapter_tournament)
        val timeText: TextView = view.findViewById<TextView>(R.id.text_slide_adapter_time)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fixture_list_adapter, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.teamText.text = "${dataSet[position].home} vs ${dataSet[position].away}"
        viewHolder.tournamentText.text = dataSet[position].suggestion
        viewHolder.timeText.text = "${dataSet[position].tournament} (${dataSet[position].country})"
        if(!dataSet[position].suggestion.contains("**"))
            viewHolder.view.setBackgroundColor(Color.LTGRAY)
        viewHolder.view.setOnClickListener{ selected.modifyGame(dataSet[position]) }
    }

    override fun getItemCount() = dataSet.size

}

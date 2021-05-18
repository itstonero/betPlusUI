package com.example.betplus.ui.slideshow

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.betplus.R
import com.example.betplus.models.Fixture
import org.w3c.dom.Text

class SlideShowAdapter(val dataSet: List<Fixture>, val selected: SlideshowFragment) : RecyclerView.Adapter<SlideShowAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val teamText:TextView
        val tournamentText: TextView
        val timeText: TextView

        init {
            // Define click listener for the ViewHolder's View.
            teamText = view.findViewById<TextView>(R.id.text_slide_adapter_team)
            tournamentText = view.findViewById<TextView>(R.id.text_slide_adapter_tournament)
            timeText = view.findViewById<TextView>(R.id.text_slide_adapter_time)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fixture_list_adapter, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.teamText.text = "${dataSet[position].home} vs ${dataSet[position].away}"
        viewHolder.tournamentText.text = "${dataSet[position].tournament} (${dataSet[position].country})"
        viewHolder.timeText.text = dataSet[position].time

        viewHolder.view.setOnClickListener{ selected.toggleFixture(dataSet[position]) }

        if(selected.isSelected(dataSet[position])){
            viewHolder.view.setBackgroundColor(Color.GREEN)
        }else{
            viewHolder.view.setBackgroundColor(Color.parseColor("#FF9100"))
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

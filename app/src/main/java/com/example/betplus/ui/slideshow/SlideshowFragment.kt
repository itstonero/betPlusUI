package com.example.betplus.ui.slideshow

import android.R
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.betplus.databinding.FragmentSlideshowBinding
import com.example.betplus.models.Fixture


private const val TAG = "SlideshowFragment"
class SlideshowFragment : Fragment() {

    private lateinit var slideshowViewModel: SlideshowViewModel
    private var _binding: FragmentSlideshowBinding? = null
    private lateinit var recycler_adapter: SlideShowAdapter
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root


        slideshowViewModel.text.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        binding.textSlideshowSearch.doOnTextChanged{ text, _, _, _ ->   slideshowViewModel.findMatches(text.toString())}
        binding.buttonRegisterGame.setOnClickListener { loadUp() }

        slideshowViewModel.matchingFixtures.observe(viewLifecycleOwner, Observer {
            binding.recyclerSlideshow.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false)
            recycler_adapter = SlideShowAdapter(it, this)
            binding.recyclerSlideshow.adapter = recycler_adapter
        })

        if(slideshowViewModel.allFixtures.value.isNullOrEmpty()){
            slideshowViewModel.getAllMatches()
        }

        return root
    }

    public fun toggleFixture(fixture: Fixture){
        slideshowViewModel.toggleFixture(fixture)
        recycler_adapter.notifyDataSetChanged()
        if(isSelected(fixture))  makeSuggestion(fixture)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun isSelected(fixture: Fixture): Boolean {
        return slideshowViewModel.selectedFixture.value?.contains(fixture) !!
    }

    fun loadUp(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Register Matches")
        builder.setMessage("Clear Previous Matches")
        builder.setPositiveButton(
            "Clear"
        ) { dialog, which -> slideshowViewModel.registerGames(true)}
        builder.setNegativeButton(
            "Retain"
        ) { dialog, which -> slideshowViewModel.registerGames(false)}

        builder.create().show()
    }

    fun makeSuggestion(fixture: Fixture){
        val options = arrayOf("Home Wins", "Away Wins", "Home Win (-1.5AH)", "Away Win (-1.5AH)", "Home Win (-2.5AH)", "Away Win (-2.5AH)")

        val builder  = AlertDialog.Builder(context)
        builder.setTitle("Make Suggestion")
        builder.setItems(options, DialogInterface.OnClickListener { _, which ->
            fixture.suggestion = options[which]
            Toast.makeText(context, fixture.suggestion, Toast.LENGTH_LONG).show()
        })
        builder.show()
    }
}
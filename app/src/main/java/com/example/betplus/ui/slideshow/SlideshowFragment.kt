package com.example.betplus.ui.slideshow

import android.app.AlertDialog
import android.content.DialogInterface
import android.opengl.Visibility
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
    private lateinit var recyclerAdapter: SlideShowAdapter
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        slideshowViewModel = ViewModelProvider(this).get(SlideshowViewModel::class.java)
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        slideshowViewModel.text.observe(viewLifecycleOwner, Observer { Toast.makeText(context, it, Toast.LENGTH_LONG).show() })
        binding.textSlideshowSearch.doOnTextChanged{ text, _, _, _ ->   slideshowViewModel.findMatches(text.toString())}
        binding.buttonRegisterGame.setOnClickListener { loadUp() }
        binding.textSlideshowSearch.visibility = View.INVISIBLE
        slideshowViewModel.matchingFixtures.observe(viewLifecycleOwner, Observer { loadRecyclerView(it) })
        slideshowViewModel.getAllMatches()

        return root
    }

    public fun toggleFixture(fixture: Fixture){
        slideshowViewModel.toggleFixture(fixture)
        recyclerAdapter.notifyDataSetChanged()
        if(isSelected(fixture))  makeSuggestion(fixture)
    }

    private fun loadRecyclerView(allFixtures: List<Fixture>){
        if(binding.recyclerSlideshow.adapter == null){
            binding.recyclerSlideshow.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false)
            recyclerAdapter = SlideShowAdapter(allFixtures, this)
            binding.recyclerSlideshow.adapter = recyclerAdapter
        }else{
            recyclerAdapter.notifyDataSetChanged()
        }

        binding.textSlideshowSearch.visibility = View.VISIBLE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun isSelected(fixture: Fixture): Boolean {
        return slideshowViewModel.selectedFixture.value?.contains(fixture) !!
    }

    private fun loadUp(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Register Matches")
        builder.setMessage("Clear Previous Matches")
        builder.setPositiveButton(
            "Clear"
        ) { _, _ -> slideshowViewModel.registerGames(true, requireContext());}
        builder.setNegativeButton(
            "Retain"
        ) { _, _ -> slideshowViewModel.registerGames(false, requireContext());}

        builder.create().show()
    }

    private fun makeSuggestion(fixture: Fixture){
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
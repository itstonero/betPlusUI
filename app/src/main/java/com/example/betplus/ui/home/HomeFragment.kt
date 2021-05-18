package com.example.betplus.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.betplus.R
import com.example.betplus.databinding.FragmentHomeBinding
import com.example.betplus.models.SlipRequest

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.textHomeRetries.setOnClickListener { homeViewModel.updateSlip() }
        binding.textHomeRetries.setOnLongClickListener{ homeViewModel.reverseSlip() }
        binding.textHomeProgress.setOnClickListener { homeViewModel.upgradeSlip() }
        binding.textHomeProgress.setOnLongClickListener { homeViewModel.createSlip(SlipRequest(5, 5, 2000, 2000, "Begins")) }
        binding.textHomeAdviceAmount.setOnClickListener {
            when(homeViewModel.amountType){
                0 -> {
                    homeViewModel.amountType = 1
                    binding.textHomeAdviceAmount.text = "NGN ${homeViewModel.slip.value?.balanceAmount}"
                    binding.textHomeAmountType.text = "BALANCE AMOUNT"
                }
                1 -> {
                    homeViewModel.amountType = 2
                    binding.textHomeAdviceAmount.text = "NGN ${homeViewModel.slip.value?.bonusAmount}"
                    binding.textHomeAmountType.text = "BONUS AMOUNT"
                }
                else ->{
                    homeViewModel.amountType = 0
                    binding.textHomeAdviceAmount.text = "NGN ${homeViewModel.slip.value?.adviceAmount}"
                    binding.textHomeAmountType.text = "ADVICE AMOUNT"
                }
            }
        }

        homeViewModel.slip.observe(viewLifecycleOwner, Observer {
            binding.textHomeAdviceAmount.text = "NGN ${it.adviceAmount}"
            binding.textHomeAmountType.text = "Advice Amount"
            binding.textHomeProgress.text = "${it.progress} / ${it.progressLimit}"
            binding.textHomeRetries.text = "${it.retries} / ${it.retriesLimit}"
            binding.progressLoadingSlips.visibility = View.INVISIBLE
        })

        homeViewModel.errorMessage.observe(viewLifecycleOwner, Observer{
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        if(homeViewModel.slip.value == null) {
            homeViewModel.retrieveSlip()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
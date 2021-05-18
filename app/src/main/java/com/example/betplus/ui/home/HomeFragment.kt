package com.example.betplus.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.betplus.R
import com.example.betplus.databinding.FragmentHomeBinding
import com.example.betplus.models.SlipRequest
import com.example.betplus.services.BetPlusAPI


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

        Toast.makeText(context, "LoggedIn as ${BetPlusAPI.SITE_USERNAME}", Toast.LENGTH_LONG).show()

        binding.textHomeRetries.setOnClickListener { homeViewModel.updateSlip() }
        binding.textHomeRetries.setOnLongClickListener{ homeViewModel.reverseSlip() }
        binding.textHomeProgress.setOnClickListener { upgradeSlip() }
        binding.textHomeProgress.setOnLongClickListener { createSlip(); true }
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

        if(homeViewModel.slip.value == null) homeViewModel.retrieveSlip()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun upgradeSlip(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Win ODD")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Confirm",
            DialogInterface.OnClickListener { _, _ ->
                if(input.text.toString().isNotEmpty())
                    homeViewModel.upgradeSlip(input.text.toString()) }
        )

        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()

    }

    private fun createSlip(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_slip, null, false))
        dialog.findViewById<Button>(R.id.button_confirm_create_slip).setOnClickListener{
            val trial = dialog.findViewById<TextView>(R.id.text_create_trials).text
            val amount = dialog.findViewById<TextView>(R.id.text_create_total_amount).text
            val totalOdd = dialog.findViewById<TextView>(R.id.text_create_total_odds).text
            val odd = dialog.findViewById<TextView>(R.id.text_create_odd).text
            trial.let { it ->
                if(it.isNullOrEmpty()) Toast.makeText(context, "Enter Valid Trials", Toast.LENGTH_LONG).show()
                else
                    amount.let { it ->
                        if(it.isNullOrEmpty()) Toast.makeText(context, "Enter Valid Amount", Toast.LENGTH_LONG).show()
                        else
                            totalOdd.let { it->
                                if(it.isNullOrEmpty()) Toast.makeText(context, "Enter Valid Total Odds", Toast.LENGTH_LONG).show()
                                else
                                    amount.let { it->
                                        if(it.isNullOrEmpty()) Toast.makeText(context, "Enter Valid Total Odds", Toast.LENGTH_LONG).show()
                                        else {
                                            homeViewModel.createSlip(
                                                SlipRequest(
                                                    trial.toString().toDouble(),
                                                    odd.toString().toDouble(),
                                                    totalOdd.toString().toDouble(),
                                                    amount.toString().toDouble(),
                                                    "On God"
                                                )
                                            )
                                        }
                                    }
                            }
                    }
            }
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.button_cancel_create_slip).setOnClickListener{ dialog.dismiss()  }
        dialog.show()

        dialog.getWindow()?.let {
            val layoutDimension = WindowManager.LayoutParams()
            it.setLayout(layoutDimension.width, layoutDimension.height)
        }
    }
}
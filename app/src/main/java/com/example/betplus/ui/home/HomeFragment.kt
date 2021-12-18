package com.example.betplus.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.betplus.R
import com.example.betplus.databinding.FragmentHomeBinding
import com.example.betplus.models.SlipRequest
import com.example.betplus.models.SlipResponse
import com.example.betplus.models.SlipUpgradeRequest
import com.example.betplus.models.views.DashboardViewModel
import com.example.betplus.services.BetPlusAPI
import com.example.betplus.services.Repo
import retrofit2.Call
import retrofit2.Response


private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private val sharedViewModel: DashboardViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.textHomeRetries.setOnClickListener { updateSlip() }
        binding.textHomeRetries.setOnLongClickListener{ reverseSlip() }
        binding.textHomeProgress.setOnClickListener { upgradeSlip() }
        binding.textHomeProgress.setOnLongClickListener { createSlip() }
        binding.textHomeAdviceAmount.setOnClickListener { loadAmount()}
        binding.textHomeAdviceAmount.setOnLongClickListener { createSlip() }

        sharedViewModel.slipInfo.observe(viewLifecycleOwner, Observer { loadParameters() })

        if(sharedViewModel.slipInfo.value == null) initializeSlip()

        return root
    }

    private fun initializeSlip() {
        Log.i(TAG, "Initializing Slip")
        val apiService = Repo.betPlusAPI.retrieveSlip(BetPlusAPI.SITE_USERNAME)
        apiService?.enqueue(object : retrofit2.Callback<SlipResponse?> {
            override fun onResponse(call: Call<SlipResponse?>, response: Response<SlipResponse?>) {
                if(response.code() == 200)
                {
                    (response.body() as SlipResponse)?.apply {
                        sharedViewModel.updateSlipInfo(this)
                        Toast.makeText(context, "Loaded Slip Successfully", Toast.LENGTH_LONG).show()
                    }
                }else
                {
                    Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                Toast.makeText(context, "Failed To Load Slip", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadParameters(){
        sharedViewModel.slipInfo.value?.let {
            binding.textHomeProgress.text = "${it.progress} / ${it.progressLimit}"
            binding.textHomeRetries.text = "${it.retries} / ${it.retriesLimit}"
            binding.progressLoadingSlips.visibility = View.INVISIBLE
        }
        sharedViewModel.amountType = 2;
        loadAmount()
    }

    private fun loadAmount() {
        sharedViewModel.slipInfo.value?.let {
            when(sharedViewModel.amountType){
                0 -> {
                    sharedViewModel.amountType = 1
                    binding.textHomeAdviceAmount.text = "NGN ${it.balanceAmount}"
                    binding.textHomeAmountType.text = "BALANCE AMOUNT"
                }
                1 -> {
                    sharedViewModel.amountType = 2
                    binding.textHomeAdviceAmount.text = "NGN ${it.bonusAmount}"
                    binding.textHomeAmountType.text = "BONUS AMOUNT"
                }
                else ->{
                    sharedViewModel.amountType = 0
                    binding.textHomeAdviceAmount.text = "NGN ${it.adviceAmount}"
                    binding.textHomeAmountType.text = "ADVICE AMOUNT (${it.adviceOdd})"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun upgradeSlip(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Win ODD")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Confirm") { _, _ ->
            if (input.text.toString().isNotEmpty()) {
                val apiService = Repo.betPlusAPI.upgradeSlip(
                    BetPlusAPI.SITE_USERNAME,
                    SlipUpgradeRequest(input.text.toString())
                )
                apiService?.enqueue(object : retrofit2.Callback<SlipResponse?> {
                    override fun onResponse(
                        call: Call<SlipResponse?>,
                        response: Response<SlipResponse?>
                    ) {
                        if (response.code() == 200) {
                            (response.body() as SlipResponse)?.apply {
                                sharedViewModel.updateSlipInfo(
                                    this
                                )
                            }
                        } else {
                            Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                        Toast.makeText(context, "Failed to Upgrade Slip", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()

    }

    private fun createSlip():Boolean{
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
                                            val newSlip = SlipRequest(trial.toString().toDouble(), odd.toString().toDouble(), totalOdd.toString().toDouble(), amount.toString().toDouble(), "On God")
                                            val apiCreate = Repo.betPlusAPI.createSlip(BetPlusAPI.SITE_USERNAME, newSlip)
                                            apiCreate?.enqueue(object : retrofit2.Callback<SlipResponse?> {
                                                override fun onResponse(call: Call<SlipResponse?>, response: Response<SlipResponse?>) {
                                                    if(response.code() == 200)
                                                    {
                                                        (response.body() as SlipResponse)?.apply {
                                                            sharedViewModel.updateSlipInfo(this)
                                                        }
                                                    }else
                                                    {
                                                        Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
                                                    }
                                                }

                                                override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                                                    Toast.makeText(context, "Failed To Create Slip", Toast.LENGTH_LONG).show()
                                                }
                                            })
                                        }
                                    }
                            }
                    }
            }
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.button_cancel_create_slip).setOnClickListener{ dialog.dismiss()  }
        dialog.show()

        dialog.window?.let {
            val layoutDimension = WindowManager.LayoutParams()
            it.setLayout(layoutDimension.width, layoutDimension.height)
        }
        return true
    }

    private fun updateSlip(){
        Log.d(TAG, "UPDATING SLIP")
        val apiService = Repo.betPlusAPI.updateSlip(BetPlusAPI.SITE_USERNAME)
        apiService?.enqueue(object : retrofit2.Callback<SlipResponse?> {
            override fun onResponse(call: Call<SlipResponse?>, response: Response<SlipResponse?>) {
                if(response.code() == 200)
                {
                    (response.body() as SlipResponse)?.apply { sharedViewModel.updateSlipInfo(this) }
                }else
                {
                    Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                Toast.makeText(context, "Failed To Update Slip", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun reverseSlip(): Boolean {
        Log.d(TAG, "REVERSING SLIP")
        val apiService = Repo.betPlusAPI.reverseSlip(BetPlusAPI.SITE_USERNAME)
        apiService?.enqueue(object : retrofit2.Callback<SlipResponse?> {
            override fun onResponse(call: Call<SlipResponse?>, response: Response<SlipResponse?>) {
                if(response.code() == 200)
                {
                    (response.body() as SlipResponse)?.apply { sharedViewModel.updateSlipInfo(this) }
                }else
                {
                    Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SlipResponse?>, t: Throwable) {
                Toast.makeText(context, "Failed To Reverse Slip", Toast.LENGTH_LONG).show()
            }
        })
        return true
    }
}
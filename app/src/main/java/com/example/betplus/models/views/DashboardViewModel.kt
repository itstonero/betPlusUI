package com.example.betplus.models.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.betplus.models.Fixture
import com.example.betplus.models.SlipResponse

class DashboardViewModel : ViewModel() {

    var amountType = 0
    val chosenGames = ArrayList<Fixture>()

    private var _allMatches = MutableLiveData<List<Fixture>>()
    val allMatches: LiveData<List<Fixture>> = _allMatches;

    private var _selectedMatches = MutableLiveData<List<Fixture>>()
    val selectedMatches: LiveData<List<Fixture>> = _selectedMatches;

    private var _searchResult = MutableLiveData<List<Fixture>>()
    val searchResult: LiveData<List<Fixture>> = _searchResult;

    private var _slipInfo = MutableLiveData<SlipResponse>();
    val slipInfo: LiveData<SlipResponse> = _slipInfo;

    fun updateAllMatches(fixtures: List<Fixture>){
        _allMatches.value = fixtures
    }

    fun updateSelectedMatches(fixtures: List<Fixture>){
        _selectedMatches.value = fixtures
    }

    fun updateSlipInfo(slipResponse: SlipResponse){
        _slipInfo.value = slipResponse
    }

    fun updateSearchResult(fixtures: List<Fixture>){
        _searchResult.value = fixtures
    }
}
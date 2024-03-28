package com.just.scanner.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.just.scanner.home.HomeViewModel

class ViewModelFactory() : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) : T {
        return when{
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel() as T
            else -> throw IllegalArgumentException("ViewModelClass not found")
        }
    }

}
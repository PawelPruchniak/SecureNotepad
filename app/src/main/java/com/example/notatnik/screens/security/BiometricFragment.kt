package com.example.notatnik.screens.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.notatnik.R
import com.example.notatnik.databinding.BiometricFragmentBinding

class BiometricFragment : Fragment() {

    private lateinit var biometricViewModel: BiometricViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Setting binding
        val binding: BiometricFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.biometric_fragment, container, false)

        binding.viewModel = biometricViewModel
        binding.lifecycleOwner = this

        return binding.root
    }


}
package com.example.notatnik.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.notatnik.databinding.TitleFragmentBinding

class TitleFragment : Fragment() {

    private lateinit var viewModel: TitleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = TitleFragmentBinding.inflate(inflater)
        viewModel = ViewModelProvider(this).get(TitleViewModel::class.java)
        binding.viewModel = viewModel

        return binding.root
    }
}
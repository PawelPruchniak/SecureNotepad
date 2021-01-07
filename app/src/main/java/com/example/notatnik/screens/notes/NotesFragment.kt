package com.example.notatnik.screens.notes

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.notatnik.R
import com.example.notatnik.database.NotesDatabase
import com.example.notatnik.databinding.NotesFragmentBinding

class NotesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Setting binding
        val binding: NotesFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.notes_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = NotesDatabase.getInstance(application).notesDatabaseDao

        val arguments =  NotesFragmentArgs.fromBundle(requireArguments())

        val viewModelFactory = NotesViewModelFactory(dataSource, application, arguments.passwordArg, arguments.status, arguments.cipher)
        val notesViewModel = ViewModelProvider(this, viewModelFactory).get(
            NotesViewModel::class.java
        )
        binding.viewModel = notesViewModel
        binding.lifecycleOwner = this



        // Event zapisujący notatkę po kliknięciu przyciusku
        notesViewModel.eventSaveButtonClicked.observe(viewLifecycleOwner, { isTrue ->
            if (isTrue) {
                hideKeyboard()
                val noteText: String = binding.noteTextView.text.toString()
                if(notesViewModel.noteString.value != noteText){
                    notesViewModel.saveNewNote(noteText)
                }
                notesViewModel.onEventSaveButtonClickedComplete()
            }
        })


        // Event obserwujący zmienną w której są zaszyfrowane dane
        notesViewModel.noteDatabase.observe(viewLifecycleOwner, { note ->
            if(note != null){
                //notesViewModel.initializeNote()
                println("The noteEncrypted was successfully loaded")
            }
            else{
                println("Note is null")
            }
        })
        return binding.root
    }


    // FUNKCJE DO UKRYCIA KLAWIATURY PO KLIKNIECIU PRZYCISKU
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
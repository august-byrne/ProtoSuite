package com.example.protosuite.ui.notes

import com.example.protosuite.data.db.NotesDatabase
import com.example.protosuite.data.repositories.NoteRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val notesModule = module {

    // single instance of NoteRepository
    //single { NoteRepository(NotesDatabase.getInstance(androidContext()).noteDao) }


    single { NotesDatabase.getInstance(androidContext()).noteDao } // registers noteDao
    factory { NoteRepository(get()) } // get NotesDao from singleton ^^




    // MyViewModel ViewModel
    viewModel { NoteViewModel(get()) }







}

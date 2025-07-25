package com.zitsav.memoir.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.zitsav.memoir.layout.HomeScreen
import com.zitsav.memoir.repository.HomeRepository
import com.zitsav.memoir.viewmodel.HomeViewModel
import com.zitsav.memoir.data.entry.EntryRepositoryImpl

class HomeActivity : ComponentActivity() {
    private lateinit var viewModel: HomeViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //todo: change it and add it to an env later on
        val dbPass = "password"

        val homeRepository = HomeRepository(EntryRepositoryImpl(this, dbPass))
        viewModel = HomeViewModel(homeRepository)

        setContent {
            HomeScreen(
                userName = "Utsav",
                notes = viewModel.entries,
                onAddNoteClick = {
                    val intent = Intent(this, CreateOrEditNoteActivity::class.java)
                    startActivity(intent)
                },
                onNoteClick = {
                    val intent = Intent(this, CreateOrEditNoteActivity::class.java)
                    intent.putExtra("ENTRY_ID", it.id)
                    startActivity(intent)
                }
            )
        }
    }
}
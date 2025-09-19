package com.einkscreensaver.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.einkscreensaver.app.R
import com.einkscreensaver.app.data.repository.SettingsRepository
import com.einkscreensaver.app.databinding.ActivityFontSelectionBinding
import com.einkscreensaver.app.ui.adapter.FontSelectionAdapter
import kotlinx.coroutines.launch

class FontSelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFontSelectionBinding
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var fontAdapter: FontSelectionAdapter
    
    private val fontPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectCustomFont(it)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFontSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        settingsRepository = SettingsRepository(this)
        
        setupToolbar()
        setupRecyclerView()
        loadFonts()
    }
    
    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select Font"
    }
    
    private fun setupRecyclerView() {
        fontAdapter = FontSelectionAdapter { font ->
            selectFont(font)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FontSelectionActivity)
            adapter = fontAdapter
        }
    }
    
    private fun loadFonts() {
        val fonts = listOf(
            "Default",
            "Monospace",
            "Sans Serif",
            "Serif",
            "Condensed",
            "Light",
            "Thin",
            "Custom Font File"
        )
        fontAdapter.submitList(fonts)
    }
    
    private fun selectFont(font: String) {
        lifecycleScope.launch {
            try {
                if (font == "Custom Font File") {
                    fontPickerLauncher.launch("*/*")
                } else {
                    settingsRepository.updateClockFont(font)
                    finish()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun selectCustomFont(uri: Uri) {
        lifecycleScope.launch {
            try {
                settingsRepository.updateCustomFontPath(uri.toString())
                settingsRepository.updateClockFont("Custom")
                finish()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.font_selection_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_clear_font -> {
                clearFont()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun clearFont() {
        lifecycleScope.launch {
            try {
                settingsRepository.updateClockFont("Default")
                settingsRepository.updateCustomFontPath("")
                finish()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
package com.einkscreensaver.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.einkscreensaver.app.R
import com.einkscreensaver.app.data.repository.SettingsRepository
import com.einkscreensaver.app.databinding.ActivityImageSelectionBinding
import com.einkscreensaver.app.ui.adapter.ImageSelectionAdapter
import kotlinx.coroutines.launch

class ImageSelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityImageSelectionBinding
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var imageAdapter: ImageSelectionAdapter
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadImages()
        } else {
            Toast.makeText(this, "Permission required to access images", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectImage(it)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        settingsRepository = SettingsRepository(this)
        
        setupToolbar()
        setupRecyclerView()
        checkPermissionAndLoadImages()
    }
    
    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select Custom Image"
    }
    
    private fun setupRecyclerView() {
        imageAdapter = ImageSelectionAdapter { imageUri ->
            selectImage(imageUri)
        }
        
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@ImageSelectionActivity, 3)
            adapter = imageAdapter
        }
    }
    
    private fun checkPermissionAndLoadImages() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadImages()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    private fun loadImages() {
        lifecycleScope.launch {
            try {
                val images = getImagesFromGallery()
                imageAdapter.submitList(images)
            } catch (e: Exception) {
                Toast.makeText(this@ImageSelectionActivity, "Error loading images", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private suspend fun getImagesFromGallery(): List<Uri> {
        val images = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("image/jpeg", "image/png")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                images.add(contentUri)
            }
        }
        
        return images
    }
    
    private fun selectImage(imageUri: Uri) {
        lifecycleScope.launch {
            try {
                settingsRepository.updateCustomImagePath(imageUri.toString())
                Toast.makeText(this@ImageSelectionActivity, "Image selected successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ImageSelectionActivity, "Error selecting image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_selection_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_pick_image -> {
                imagePickerLauncher.launch("image/*")
                true
            }
            R.id.action_clear_image -> {
                clearImage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun clearImage() {
        lifecycleScope.launch {
            try {
                settingsRepository.updateCustomImagePath("")
                Toast.makeText(this@ImageSelectionActivity, "Image cleared", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ImageSelectionActivity, "Error clearing image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
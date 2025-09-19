package com.einkscreensaver.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.einkscreensaver.app.BuildConfig
import com.einkscreensaver.app.R
import com.einkscreensaver.app.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAboutBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupContent()
    }
    
    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.about_title)
    }
    
    private fun setupContent() {
        binding.apply {
            tvAppName.text = getString(R.string.app_name)
            tvVersion.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"
            tvDescription.text = getString(R.string.description)
            tvFeatures.text = getString(R.string.features_description)
            
            btnGitHub.setOnClickListener {
                openGitHub()
            }
            
            btnRateApp.setOnClickListener {
                rateApp()
            }
            
            btnShareApp.setOnClickListener {
                shareApp()
            }
        }
    }
    
    private fun openGitHub() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yourusername/eink-screensaver"))
        startActivity(intent)
    }
    
    private fun rateApp() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}"))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${packageName}"))
            startActivity(webIntent)
        }
    }
    
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this E-Ink ScreenSaver app: https://play.google.com/store/apps/details?id=${packageName}")
        startActivity(Intent.createChooser(shareIntent, "Share App"))
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.about_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_privacy_policy -> {
                openPrivacyPolicy()
                true
            }
            R.id.action_terms_of_service -> {
                openTermsOfService()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun openPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://yourwebsite.com/privacy-policy"))
        startActivity(intent)
    }
    
    private fun openTermsOfService() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://yourwebsite.com/terms-of-service"))
        startActivity(intent)
    }
}
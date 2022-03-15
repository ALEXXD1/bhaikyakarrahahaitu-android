package com.oddlyspaced.bkkrht.ui.activity

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.oddlyspaced.bkkrht.R
import com.oddlyspaced.bkkrht.databinding.ActivityMainBinding
import com.oddlyspaced.bkkrht.service.CurrentAppService
import com.oddlyspaced.bkkrht.ui.adapter.AppListAdapter
import com.oddlyspaced.bkkrht.util.SharedPreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val prefManager by lazy { SharedPreferenceManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appAdapter = AppListAdapter(applicationContext, prefManager.readAppsList())
        binding.rvApps.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = appAdapter
            setItemViewCacheSize(100)
        }

        binding.fabSave.setOnClickListener {
            prefManager.saveAppsList(appAdapter.getCheckedApps())
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 1000L)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isServiceEnabled()) {
            promptServiceOff()
        }
    }

    private fun promptServiceOff() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.alert_title))
            setMessage(getString(R.string.alert_message))
            setPositiveButton(getString(R.string.alert_enable)) { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
            setCancelable(false)
        }.show()
    }

    private fun isServiceEnabled(): Boolean {
        val am = applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (service in enabledServices) {
            if (service.resolveInfo.serviceInfo.name.contains(CurrentAppService::class.qualifiedName.toString())) {
                return true
            }
        }
        return false
    }
}
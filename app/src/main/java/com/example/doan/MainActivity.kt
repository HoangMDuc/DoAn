package com.example.doan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.doan.databinding.ActivityMainBinding
import com.example.doan.repository.KeysRepository
import com.example.doan.utils.IMAGE_MEDIA
import com.example.doan.utils.getLockedFileRootPath
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var slideNavView: NavigationView
    @RequiresApi(Build.VERSION_CODES.R)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d("Activity", "Permission ${Environment.isExternalStorageManager()}")
    }

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        slideNavView = binding.navView
        drawerLayout = binding.drawerLayout
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        slideNavView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.login_fragment, R.id.register_fragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }

                else -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
            }

        }

        if (!checkPermission(READ_IMAGE_PERMISSION)) {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.READ_MEDIA_AUDIO), 1)
        }


        val root = getExternalFilesDirs(null)
        Log.d("Tag", getLockedFileRootPath(applicationContext, IMAGE_MEDIA))

        if (root != null) {
            for (r in root) {
                val files = r.listFiles()
                Log.d("External root", r.absolutePath)
                if (files != null) {
                    for (f in files) {
                        Log.d("external", f.absolutePath)
                    }
                }
            }

        }

        Log.d("Activity", Environment.getExternalStorageDirectory().absolutePath)
        //Log.d("Activity", getLockedFilePath(applicationContext, null))
        Log.d("Activity", getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS)[0].absolutePath)
        Log.d("Activity", "${checkPermission(Manifest.permission.READ_MEDIA_VIDEO)}")
        Log.d("Activity", "${checkPermission(Manifest.permission.READ_MEDIA_IMAGES)}")
        Log.d("Activity", "${checkPermission(Manifest.permission.READ_MEDIA_AUDIO)}")
//        if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
//        }

        Log.d("AC",Environment.isExternalStorageManager().toString())
        if(Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

            startActivityForResult(intent, 22)
        }

        KeysRepository(application).test1("")
    }


    override fun onSupportNavigateUp(): Boolean {

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
//    }

    /**
     * A native method that is implemented by the 'doan' native library,
     * which is packaged with this application.
     */


    companion object {
        // Used to load the 'doan' library on application startup.
        init {
            System.loadLibrary("doan")
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val READ_IMAGE_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val READ_VIDEO_PERMISSION = Manifest.permission.READ_MEDIA_VIDEO
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val READ_AUDIO_PERMISSION = Manifest.permission.READ_MEDIA_AUDIO

    }

    private fun requestPermission() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 22) {
            Log.d("Activity", "OK1")
        }
    }

    private fun checkPermission(permissions: String): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            permissions
        ) == PackageManager.PERMISSION_GRANTED
    }



}
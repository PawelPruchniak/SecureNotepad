package com.example.notatnik

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.notatnik.databinding.ActivityMainBinding
import net.sqlcipher.database.SQLiteDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // SQLCipher
        SQLiteDatabase.loadLibs(this)
        val databaseFile = getDatabasePath("demo.db")
        if(databaseFile.exists()) databaseFile.delete()
        databaseFile.mkdirs()
        databaseFile.delete()
        val database = SQLiteDatabase.openOrCreateDatabase(databaseFile, "test123", null)
        database.execSQL("create table t1(a, b)")
        database.execSQL("insert into t1(a, b) values(?, ?)",
            arrayOf<Any>("one for the money", "two for the show")
        )
        // SQLCipher

        setupNavigation()
    }

    override fun onSupportNavigateUp()
            = navigateUp(findNavController(R.id.nav_host_fragment), binding.drawerLayout)

    private fun setupNavigation() {
        // first find the nav controller
        val navController = findNavController(R.id.nav_host_fragment)

        setSupportActionBar(binding.toolbar)

        // then setup the action bar, tell it about the DrawerLayout
        setupActionBarWithNavController(navController, binding.drawerLayout)

        // finally setup the left drawer (called a NavigationView)
        binding.navigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
            val toolBar = supportActionBar ?: return@addOnDestinationChangedListener
            when(destination.id) {
                R.id.titleFragment -> {
                    toolBar.setDisplayShowTitleEnabled(true)
                }
                else -> {
                    toolBar.setDisplayShowTitleEnabled(true)
                }
            }
        }
    }
}
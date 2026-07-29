package com.carloshinojosa.idealistachallenge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.carloshinojosa.idealistachallenge.databinding.ActivityMainBinding
import com.carloshinojosa.idealistachallenge.list.ListingNavigator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ListingNavigator {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun navigateToDetail(propertyId: String) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.navigate(
            R.id.action_listing_to_detail,
            bundleOf("propertyId" to propertyId)
        )
    }
}

package com.vinn.vhike

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.vinn.vhike.ui.navigation.AppNavigation
import com.vinn.vhike.ui.theme.MHikeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MHikeTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
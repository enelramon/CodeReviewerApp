package com.sagrd.codereviewerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sagrd.codereviewerapp.ui.navigation.CodeReviewerNavHost
import com.sagrd.codereviewerapp.ui.theme.CodeReviewerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeReviewerAppTheme {
                CodeReviewerNavHost()
            }
        }
    }
}
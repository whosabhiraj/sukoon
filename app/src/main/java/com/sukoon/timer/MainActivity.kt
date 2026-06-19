package com.sukoon.timer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukoon.timer.timer.TimerEngine
import com.sukoon.timer.ui.SukoonApp
import com.sukoon.timer.ui.theme.SukoonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SukoonTheme {
                val vm: TimerEngine = viewModel()
                SukoonApp(vm)
            }
        }
    }
}

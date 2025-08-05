package ru.kokoroyume.tasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ru.kokoroyume.tasks.ui.TasksApp
import ru.kokoroyume.tasks.ui.theme.TasksTheme
import ru.kokoroyume.tasks.viewmodel.TasksViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: TasksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TasksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TasksApp(viewModel = viewModel)
                }
            }
        }
    }
}
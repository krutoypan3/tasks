package ru.kokoroyume.tasks.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.kokoroyume.tasks.viewmodel.TasksViewModel
import ru.kokoroyume.tasks.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksApp(viewModel: TasksViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nodes by viewModel.nodes.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentParentId by viewModel.currentParentId.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Граф целей") },
            actions = {
                IconButton(onClick = {
                    viewModel.setViewMode(
                        if (uiState.viewMode == ViewMode.LIST) ViewMode.MIND_MAP else ViewMode.LIST
                    )
                }) {
                    Icon(
                        imageVector = if (uiState.viewMode == ViewMode.LIST) Icons.Default.AccountBox else Icons.Default.List,
                        contentDescription = "Switch view"
                    )
                }

                IconButton(onClick = { viewModel.showAddDialog(true, currentParentId) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add node")
                }
            }
        )

        // Navigation breadcrumb (если не в корне)
        if (currentParentId != null) {
            Row(
                modifier = Modifier.padding(16.dp)
            ) {
                TextButton(onClick = { viewModel.navigateToNode(null) }) {
                    Text("← Вернуться в начало")
                }
            }
        }

        // Content
        when (uiState.viewMode) {
            ViewMode.LIST -> {
                TasksListView(
                    nodes = nodes,
                    onNodeClick = { node -> viewModel.navigateToNode(node.node.id) },
                    onToggleStatus = { node -> viewModel.toggleNodeStatus(node.node) },
                    onDeleteNode = { node -> viewModel.deleteNode(node.node.id) },
                    onAddChild = { node -> viewModel.showAddDialog(true, node.node.id) }
                )
            }
            ViewMode.MIND_MAP -> {
                TasksMindMapView(
                    nodes = nodes,
                    onNodeClick = { node -> viewModel.navigateToNode(node.node.id) }
                )
            }
        }
    }

    // Add Node Dialog
    if (uiState.showAddDialog) {
        AddNodeDialog(
            onDismiss = { viewModel.showAddDialog(false) },
            onConfirm = { name, description ->
                viewModel.addNode(name, description, uiState.addDialogParentId)
                viewModel.showAddDialog(false)
            }
        )
    }
}
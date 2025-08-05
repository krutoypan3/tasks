package ru.kokoroyume.tasks.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.kokoroyume.tasks.model.NodeStatus
import ru.kokoroyume.tasks.model.TreeNodeWithProgress

@Composable
fun TasksListView(
    nodes: List<TreeNodeWithProgress>,
    onNodeClick: (TreeNodeWithProgress) -> Unit,
    onToggleStatus: (TreeNodeWithProgress) -> Unit,
    onDeleteNode: (TreeNodeWithProgress) -> Unit,
    onAddChild: (TreeNodeWithProgress) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nodes) { nodeWithProgress ->
            TreeNodeCard(
                nodeWithProgress = nodeWithProgress,
                onNodeClick = onNodeClick,
                onToggleStatus = onToggleStatus,
                onDeleteNode = onDeleteNode,
                onAddChild = onAddChild
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeNodeCard(
    nodeWithProgress: TreeNodeWithProgress,
    onNodeClick: (TreeNodeWithProgress) -> Unit,
    onToggleStatus: (TreeNodeWithProgress) -> Unit,
    onDeleteNode: (TreeNodeWithProgress) -> Unit,
    onAddChild: (TreeNodeWithProgress) -> Unit
) {
    val node = nodeWithProgress.node

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNodeClick(nodeWithProgress) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Icon
                IconButton(onClick = { onToggleStatus(nodeWithProgress) }) {
                    when (node.status) {
                        NodeStatus.PENDING -> Icon(Icons.Default.ThumbUp, contentDescription = "Pending")
                        NodeStatus.IN_PROGRESS -> Icon(Icons.Default.MoreVert, contentDescription = "In Progress", tint = MaterialTheme.colorScheme.primary)
                        NodeStatus.COMPLETED -> Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = Color.Green)
                    }
                }

                // Node Name
                Text(
                    text = node.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (node.status == NodeStatus.COMPLETED) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )

                // Actions
                Row {
                    IconButton(onClick = { onAddChild(nodeWithProgress) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add child")
                    }
                    IconButton(onClick = { onDeleteNode(nodeWithProgress) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            // Description
            if (node.description.isNotEmpty()) {
                Text(
                    text = node.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 48.dp, top = 4.dp)
                )
            }

            // Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = nodeWithProgress.progress / 100f,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${nodeWithProgress.progress.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Child count
            if (nodeWithProgress.childCount > 0) {
                Text(
                    text = "${nodeWithProgress.childCount} subtasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 48.dp, top = 4.dp)
                )
            }
        }
    }
}
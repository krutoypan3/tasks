package ru.kokoroyume.tasks.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.kokoroyume.tasks.model.NodeStatus
import ru.kokoroyume.tasks.model.TreeNodeWithProgress
import kotlin.math.*

// Данные для узла в mind map
data class MindMapNode(
    val nodeWithProgress: TreeNodeWithProgress,
    var position: Offset,
    val radius: Float = 60f,
    var isDragging: Boolean = false
)

@Composable
fun TasksMindMapView(
    nodes: List<TreeNodeWithProgress>,
    onNodeClick: (TreeNodeWithProgress) -> Unit
) {
    val density = LocalDensity.current

    // Состояние для позиций узлов
    var mindMapNodes by remember { mutableStateOf<List<MindMapNode>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // Инициализируем позиции узлов при изменении данных
    LaunchedEffect(nodes, canvasSize) {
        if (canvasSize != androidx.compose.ui.geometry.Size.Zero && nodes.isNotEmpty()) {
            mindMapNodes = calculateNodePositions(nodes, canvasSize, density.density)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Обработка клика по узлам
                        mindMapNodes.find { node ->
                            val distance = sqrt(
                                (tapOffset.x - node.position.x).pow(2) +
                                        (tapOffset.y - node.position.y).pow(2)
                            )
                            distance <= node.radius
                        }?.let { clickedNode ->
                            onNodeClick(clickedNode.nodeWithProgress)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            mindMapNodes = mindMapNodes.map { node ->
                                val distance = sqrt(
                                    (offset.x - node.position.x).pow(2) +
                                            (offset.y - node.position.y).pow(2)
                                )
                                if (distance <= node.radius) {
                                    node.copy(isDragging = true)
                                } else {
                                    node.copy(isDragging = false)
                                }
                            }
                        },
                        onDragEnd = {
                            mindMapNodes = mindMapNodes.map { it.copy(isDragging = false) }
                        },
                        onDrag = { _, dragAmount ->
                            mindMapNodes = mindMapNodes.map { node ->
                                if (node.isDragging) {
                                    node.copy(
                                        position = Offset(
                                            (node.position.x + dragAmount.x).coerceIn(
                                                node.radius,
                                                canvasSize.width - node.radius
                                            ),
                                            (node.position.y + dragAmount.y).coerceIn(
                                                node.radius,
                                                canvasSize.height - node.radius
                                            )
                                        )
                                    )
                                } else {
                                    node
                                }
                            }
                        }
                    )
                }
        ) {
            canvasSize = size
            drawMindMap(mindMapNodes, nodes, density.density)
        }

        // Легенда
        MindMapLegend(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun MindMapLegend(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Legend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            LegendItem(color = Color(0xFF4CAF50), text = "Completed")
            LegendItem(color = Color(0xFFFFC107), text = "In Progress")
            LegendItem(color = Color(0xFFF44336), text = "Not Started")
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

// Отрисовка связей и узлов
fun DrawScope.drawMindMap(
    nodes: List<MindMapNode>,
    allNodes: List<TreeNodeWithProgress>,
    density: Float
) {
    val nodeMap = nodes.associateBy { it.nodeWithProgress.node.id }

    // Рисуем связи
    for (node in allNodes) {
        node.node.parentId?.let { parentId ->
            val from = nodeMap[parentId]
            val to = nodeMap[node.node.id]
            if (from != null && to != null) {
                drawLine(
                    color = Color.Gray,
                    start = from.position,
                    end = to.position,
                    strokeWidth = 2f
                )
            }
        }
    }

    // Рисуем узлы
    for (node in nodes) {
        val statusColor = when (node.nodeWithProgress.node.status) {
            NodeStatus.PENDING -> Color(0xFFF44336)
            NodeStatus.IN_PROGRESS -> Color(0xFFFFC107)
            NodeStatus.COMPLETED -> Color(0xFF4CAF50)
        }

        drawCircle(
            color = statusColor,
            radius = node.radius,
            center = node.position
        )

        drawContext.canvas.nativeCanvas.apply {
            drawText(
                node.nodeWithProgress.node.name,
                node.position.x,
                node.position.y - node.radius - 10,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 32f * density
                    isAntiAlias = true
                }
            )
        }
    }
}


// Простая раскладка узлов по кругу
fun calculateNodePositions(
    nodes: List<TreeNodeWithProgress>,
    canvasSize: androidx.compose.ui.geometry.Size,
    density: Float
): List<MindMapNode> {
    val centerX = canvasSize.width / 2
    val centerY = canvasSize.height / 2
    val radius = min(canvasSize.width, canvasSize.height) / 3
    val angleStep = 2 * PI / nodes.size

    return nodes.mapIndexed { index, node ->
        val angle = angleStep * index
        val x = (centerX + radius * cos(angle)).toFloat()
        val y = (centerY + radius * sin(angle)).toFloat()
        MindMapNode(
            nodeWithProgress = node,
            position = Offset(x, y),
            radius = 60f * density
        )
    }
}
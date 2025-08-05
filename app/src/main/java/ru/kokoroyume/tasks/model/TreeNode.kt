package ru.kokoroyume.tasks.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "tree_nodes")
@Serializable
data class TreeNode(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val status: NodeStatus = NodeStatus.PENDING,
    val parentId: String? = null,
    val color: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val x: Float = 0f,
    val y: Float = 0f
)

enum class NodeStatus {
    PENDING, IN_PROGRESS, COMPLETED
}

data class TreeNodeWithProgress(
    val node: TreeNode,
    val progress: Float,
    val childCount: Int
)
package ru.kokoroyume.tasks.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.kokoroyume.tasks.database.TasksDao
import ru.kokoroyume.tasks.model.NodeStatus
import ru.kokoroyume.tasks.model.TreeNode
import ru.kokoroyume.tasks.model.TreeNodeWithProgress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepository @Inject constructor(
    private val dao: TasksDao
) {
    fun getAllNodes(): Flow<List<TreeNode>> = dao.getAllNodes()

    fun getRootNodes(): Flow<List<TreeNodeWithProgress>> =
        dao.getAllNodes().map { nodes ->
            nodes.filter { it.parentId == null }
                .map { node ->
                    TreeNodeWithProgress(
                        node = node,
                        progress = calculateProgress(node, nodes),
                        childCount = countChildren(node.id, nodes)
                    )
                }
        }

    fun getChildNodes(parentId: String): Flow<List<TreeNodeWithProgress>> =
        dao.getAllNodes().map { nodes ->
            nodes.filter { it.parentId == parentId }
                .map { node ->
                    TreeNodeWithProgress(
                        node = node,
                        progress = calculateProgress(node, nodes),
                        childCount = countChildren(node.id, nodes)
                    )
                }
        }

    suspend fun insertNode(node: TreeNode) = dao.insertNode(node)

    suspend fun updateNode(node: TreeNode) = dao.updateNode(node)

    suspend fun deleteNode(nodeId: String) {
        val allNodes = dao.getAllNodes()
        // Рекурсивно удаляем все дочерние узлы
        deleteNodeAndChildren(nodeId)
    }

    private suspend fun deleteNodeAndChildren(nodeId: String) {
        // Получаем текущий снимок всех узлов
        getAllNodes().first().let { allNodes ->
            // Находим всех детей текущего узла
            val children = allNodes.filter { it.parentId == nodeId }

            // Рекурсивно удаляем детей
            children.forEach { child ->
                deleteNodeAndChildren(child.id)
            }
        }

        // Удаляем сам узел
        dao.deleteNodeById(nodeId)
    }

    private fun calculateProgress(node: TreeNode, allNodes: List<TreeNode>): Float {
        val children = allNodes.filter { it.parentId == node.id }

        if (children.isEmpty()) {
            return when (node.status) {
                NodeStatus.COMPLETED -> 100f
                NodeStatus.IN_PROGRESS -> 50f
                NodeStatus.PENDING -> 0f
            }
        }

        val totalProgress = children.sumOf { child ->
            calculateProgress(child, allNodes).toDouble()
        }

        return (totalProgress / children.size).toFloat()
    }

    private fun countChildren(nodeId: String, allNodes: List<TreeNode>): Int {
        val directChildren = allNodes.filter { it.parentId == nodeId }
        return directChildren.size + directChildren.sumOf { child ->
            countChildren(child.id, allNodes)
        }
    }

    suspend fun exportToJson(): String {
        // Реализация экспорта в JSON
        return "TODO: Export implementation"
    }

    suspend fun importFromJson(json: String) {
        // Реализация импорта из JSON
        // TODO: Parse JSON and insert nodes
    }
}
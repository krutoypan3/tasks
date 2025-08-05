package ru.kokoroyume.tasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.kokoroyume.tasks.model.NodeStatus
import ru.kokoroyume.tasks.model.TreeNode
import ru.kokoroyume.tasks.model.TreeNodeWithProgress
import ru.kokoroyume.tasks.repository.TasksRepository
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TasksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _currentParentId = MutableStateFlow<String?>(null)
    val currentParentId: StateFlow<String?> = _currentParentId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val nodes: Flow<List<TreeNodeWithProgress>> = _currentParentId.flatMapLatest { parentId ->
        if (parentId == null) {
            repository.getRootNodes()
        } else {
            repository.getChildNodes(parentId)
        }
    }

    fun addNode(name: String, description: String = "", parentId: String? = null) {
        viewModelScope.launch {
            val node = TreeNode(
                name = name,
                description = description,
                parentId = parentId ?: _currentParentId.value
            )
            repository.insertNode(node)
        }
    }

    fun updateNode(nodeId: String, name: String? = null, description: String? = null, status: NodeStatus? = null) {
        viewModelScope.launch {
            // Получаем текущий узел из репозитория
            repository.getAllNodes().first().find { it.id == nodeId }?.let { existingNode ->
                val updatedNode = existingNode.copy(
                    name = name ?: existingNode.name,
                    description = description ?: existingNode.description,
                    status = status ?: existingNode.status
                )
                repository.updateNode(updatedNode)
            }
        }
    }

    fun deleteNode(nodeId: String) {
        viewModelScope.launch {
            repository.deleteNode(nodeId)
        }
    }

    fun toggleNodeStatus(node: TreeNode) {
        val newStatus = when (node.status) {
            NodeStatus.PENDING -> NodeStatus.IN_PROGRESS
            NodeStatus.IN_PROGRESS -> NodeStatus.COMPLETED
            NodeStatus.COMPLETED -> NodeStatus.PENDING
        }

        viewModelScope.launch {
            repository.updateNode(node.copy(status = newStatus))
        }
    }

    fun navigateToNode(nodeId: String?) {
        _currentParentId.value = nodeId
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun showAddDialog(show: Boolean, parentId: String? = null) {
        _uiState.value = _uiState.value.copy(
            showAddDialog = show,
            addDialogParentId = parentId
        )
    }
}

data class TasksUiState(
    val viewMode: ViewMode = ViewMode.LIST,
    val showAddDialog: Boolean = false,
    val addDialogParentId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class ViewMode {
    LIST, MIND_MAP
}
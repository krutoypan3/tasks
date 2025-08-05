package ru.kokoroyume.tasks.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.kokoroyume.tasks.model.TreeNode

@Dao
interface TasksDao {
    @Query("SELECT * FROM tree_nodes ORDER BY createdAt ASC")
    fun getAllNodes(): Flow<List<TreeNode>>

    @Query("SELECT * FROM tree_nodes WHERE parentId IS NULL ORDER BY createdAt ASC")
    fun getRootNodes(): Flow<List<TreeNode>>

    @Query("SELECT * FROM tree_nodes WHERE parentId = :parentId ORDER BY createdAt ASC")
    fun getChildNodes(parentId: String): Flow<List<TreeNode>>

    @Query("SELECT * FROM tree_nodes WHERE id = :id")
    suspend fun getNodeById(id: String): TreeNode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: TreeNode)

    @Update
    suspend fun updateNode(node: TreeNode)

    @Delete
    suspend fun deleteNode(node: TreeNode)

    @Query("DELETE FROM tree_nodes WHERE id = :nodeId")
    suspend fun deleteNodeById(nodeId: String)

    @Query("DELETE FROM tree_nodes")
    suspend fun deleteAllNodes()
}
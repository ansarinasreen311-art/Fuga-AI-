package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomAgent
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomAgentDao {
    @Query("SELECT * FROM custom_agents ORDER BY isDefault DESC, id ASC")
    fun getAllAgents(): Flow<List<CustomAgent>>

    @Query("SELECT * FROM custom_agents WHERE isActive = 1 LIMIT 1")
    fun getActiveAgent(): Flow<CustomAgent?>

    @Query("SELECT * FROM custom_agents WHERE id = :id")
    suspend fun getAgentById(id: Long): CustomAgent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: CustomAgent): Long

    @Update
    suspend fun updateAgent(agent: CustomAgent)

    @Query("UPDATE custom_agents SET isActive = 0")
    suspend fun deactivateAllAgents()

    @Query("UPDATE custom_agents SET isActive = 1 WHERE id = :id")
    suspend fun setActiveAgent(id: Long)

    @Query("DELETE FROM custom_agents WHERE id = :id AND isDefault = 0")
    suspend fun deleteAgentById(id: Long)
}

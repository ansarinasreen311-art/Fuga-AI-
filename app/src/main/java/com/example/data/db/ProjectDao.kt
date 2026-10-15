package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GeneratedProject
import com.example.data.model.ProjectType
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM generated_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<GeneratedProject>>

    @Query("SELECT * FROM generated_projects WHERE type = :type ORDER BY createdAt DESC")
    fun getProjectsByType(type: ProjectType): Flow<List<GeneratedProject>>

    @Query("SELECT * FROM generated_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): GeneratedProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: GeneratedProject): Long

    @Update
    suspend fun updateProject(project: GeneratedProject)

    @Query("DELETE FROM generated_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}

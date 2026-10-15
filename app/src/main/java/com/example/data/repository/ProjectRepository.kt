package com.example.data.repository

import com.example.data.db.ProjectDao
import com.example.data.model.GeneratedProject
import com.example.data.model.ProjectType
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<GeneratedProject>> = dao.getAllProjects()
    val appProjects: Flow<List<GeneratedProject>> = dao.getProjectsByType(ProjectType.ANDROID_APP)
    val webProjects: Flow<List<GeneratedProject>> = dao.getProjectsByType(ProjectType.WEBSITE)

    suspend fun saveProject(project: GeneratedProject): Long = dao.insertProject(project)

    suspend fun updateProject(project: GeneratedProject) = dao.updateProject(project)

    suspend fun deleteProject(id: Long) = dao.deleteProjectById(id)

    suspend fun getProject(id: Long): GeneratedProject? = dao.getProjectById(id)
}

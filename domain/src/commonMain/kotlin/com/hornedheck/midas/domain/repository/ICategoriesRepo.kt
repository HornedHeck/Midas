package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface ICategoriesRepo {
    fun getCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun addCategory(name: String, color: Int): Category
    suspend fun updateCategory(id: Long, name: String, color: Int)
    suspend fun deleteCategory(id: Long)
}

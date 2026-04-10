package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.Category

interface ICategoriesRepo {
    suspend fun getCategories(): List<Category>
}

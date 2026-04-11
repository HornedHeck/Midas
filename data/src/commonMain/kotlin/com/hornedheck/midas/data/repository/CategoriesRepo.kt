package com.hornedheck.midas.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.hornedheck.midas.db.Database
import com.hornedheck.midas.domain.model.Category
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class CategoriesRepo(
    private val db: Database,
    private val ioContext: CoroutineContext,
) : ICategoriesRepo {

    override suspend fun getCategories(): List<Category> = withContext(ioContext) {
        db.categoryQueries.selectAll { id, name, color ->
            Category(id = id, name = name, color = color)
        }.awaitAsList()
    }
}

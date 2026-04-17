package com.hornedheck.midas.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hornedheck.midas.db.Database
import com.hornedheck.midas.domain.model.Category
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class CategoriesRepo(
    private val db: Database,
    private val ioContext: CoroutineContext,
) : ICategoriesRepo {

    override fun getCategories(): Flow<List<Category>> =
        db.categoryQueries
            .selectAll { id, name, color ->
                Category(id = id, name = name, color = color)
            }
            .asFlow()
            .mapToList(ioContext)

    override suspend fun getCategoryById(id: Long): Category? = withContext(ioContext) {
        db.categoryQueries.selectById(id) { cId, name, color ->
            Category(id = cId, name = name, color = color)
        }.awaitAsOneOrNull()
    }

    override suspend fun addCategory(name: String, color: Int): Category = withContext(ioContext) {
        db.categoryQueries.transactionWithResult {
            db.categoryQueries.insert(name = name, color = color)
            val id = db.categoryQueries.lastInsertId().executeAsOne()
            Category(id = id, name = name, color = color)
        }
    }

    override suspend fun updateCategory(id: Long, name: String, color: Int): Unit =
        withContext(ioContext) {
            db.categoryQueries.updateById(name = name, color = color, id = id)
        }

    override suspend fun deleteCategory(id: Long) = withContext(ioContext) {
        db.categoryQueries.transaction {
            db.entryQueries.nullOutCategoryId(categoryId = id)
            db.categoryQueries.deleteById(id = id)
        }
    }
}

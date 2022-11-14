package edu.uwaterloo.cs.data

import edu.uwaterloo.cs.data.TodoCategories.clientDefault
import edu.uwaterloo.cs.todo.lib.TodoCategoryModel
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class TodoCategory(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TodoCategory>(TodoCategories)

    var name by TodoCategories.name
    var favoured by TodoCategories.favoured
    var modifiedTime: LocalDateTime by TodoCategories.modifiedTime
        .clientDefault { Clock.System.now().epochSeconds }
        .transform(
            { it.toInstant(TimeZone.currentSystemDefault()).epochSeconds },
            { Instant.fromEpochSeconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) }
        )
    val users by User via TodoCategoryOwnerships
    val items by TodoItem referrersOn TodoItems.categoryId

    fun toModel(): TodoCategoryModel = TodoCategoryModel(id.value, name, favoured, modifiedTime)
}
package edu.uwaterloo.cs.data

import edu.uwaterloo.cs.data.TodoCategories.clientDefault
import edu.uwaterloo.cs.todo.lib.ItemImportance
import edu.uwaterloo.cs.todo.lib.TodoItemModel
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class TodoItem(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TodoItem>(TodoItems)

    var categoryId: UUID by TodoItems.categoryId.transform(
        { EntityID(it, TodoCategories) },
        { it.value }
    )
    var name by TodoItems.name
    var description by TodoItems.description
    var favoured by TodoItems.favoured
    var importance: ItemImportance by TodoItems.importance.transform(
        { it.ordinal },
        { ItemImportance.values()[it] }
    )
    var modifiedTime: LocalDateTime by TodoItems.modifiedTime
        .clientDefault { Clock.System.now().epochSeconds }
        .transform(
            { it.toInstant(TimeZone.currentSystemDefault()).epochSeconds },
            { Instant.fromEpochSeconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) }
        )
    var deadline: LocalDate? by TodoItems.deadline.transform(
        { it?.toEpochDays() },
        { if (it === null) null else LocalDate.fromEpochDays(it) }
    )

    val users by User via TodoItemOwnerships

    fun toModel(): TodoItemModel =
        TodoItemModel(id.value, categoryId, name, description, favoured, importance, deadline, modifiedTime)
}
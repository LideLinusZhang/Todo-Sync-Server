package edu.uwaterloo.cs.data

import edu.uwaterloo.cs.data.TodoCategories.clientDefault
import edu.uwaterloo.cs.todo.lib.TodoCategoryModel
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class TodoCategory(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TodoCategory>(TodoCategories)

    var uniqueId by TodoCategories.uniqueId.clientDefault { UUID.randomUUID() }
    var name by TodoCategories.name
    var favoured by TodoCategories.favoured
    var modifiedTime: LocalDateTime by TodoCategories.modifiedTime
        .clientDefault { Clock.System.now().epochSeconds }
        .transform(
            { it.toInstant(TimeZone.currentSystemDefault()).epochSeconds },
            { Instant.fromEpochSeconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) }
        )

    fun toModel(): TodoCategoryModel = TodoCategoryModel(uniqueId, name, favoured, modifiedTime)
}
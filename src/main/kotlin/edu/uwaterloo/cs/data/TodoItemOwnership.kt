package edu.uwaterloo.cs.data

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TodoItemOwnership(id: EntityID<Int>): IntEntity(id)  {
    companion object : IntEntityClass<TodoItemOwnership>(TodoItemOwnerships)

    val userId by TodoItemOwnerships.userId
    val itemUniqueId by TodoItemOwnerships.itemUniqueId
}
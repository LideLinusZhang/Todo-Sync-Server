package edu.uwaterloo.cs.data

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TodoCategoryOwnership(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TodoCategoryOwnership>(TodoCategoryOwnerships)

    var userId by TodoCategoryOwnerships.userId
    var categoryUniqueId by TodoCategoryOwnerships.categoryUniqueId
}
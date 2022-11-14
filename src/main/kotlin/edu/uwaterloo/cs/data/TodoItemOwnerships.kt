package edu.uwaterloo.cs.data

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object TodoItemOwnerships : Table(name = "TodoItemOwnerships") {
    val user = reference("UserId", Users, onDelete = ReferenceOption.CASCADE)
    val item = reference("Item", TodoItems, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(user, item)
}
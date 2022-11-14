package edu.uwaterloo.cs.data

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object TodoCategoryOwnerships : Table(name = "TodoCategoryOwnerships") {
    val user = reference("User", Users, onDelete = ReferenceOption.CASCADE)
    val category = reference("Category", TodoCategories, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(user, category)
}
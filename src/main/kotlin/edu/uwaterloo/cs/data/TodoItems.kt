package edu.uwaterloo.cs.data

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object TodoItems : UUIDTable(name = "TodoItems", columnName = "Id") {
    val categoryId: Column<UUID> = uuid("categoryId").index()
        .references(TodoCategories.id, onDelete = ReferenceOption.CASCADE)
    val name: Column<String> = text("Name")
    val description: Column<String> = text("Description")
    val favoured: Column<Boolean> = bool("Favoured")
    val importance: Column<Int> = integer("Importance")
    val deadline: Column<Int?> = integer("Deadline").nullable()
    val modifiedTime: Column<Long> = long("ModifiedTime").index()
}
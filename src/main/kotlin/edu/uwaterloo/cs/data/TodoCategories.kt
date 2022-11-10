package edu.uwaterloo.cs.data

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column

object TodoCategories : UUIDTable(name = "TodoCategories", columnName = "Id") {
    val name: Column<String> = text("Name").uniqueIndex()
    val favoured: Column<Boolean> = bool("Favoured")
    val modifiedTime: Column<Long> = long("ModifiedTime")
}
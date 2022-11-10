package edu.uwaterloo.cs.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object TodoCategoryOwnerships : IntIdTable(name = "TodoCategoryOwnerships", columnName = "Id") {
    val userId: Column<Int> = integer("UserId").index()
    val categoryUniqueId: Column<UUID> = uuid("categoryUniqueId").index()
}
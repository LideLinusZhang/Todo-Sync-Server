package edu.uwaterloo.cs.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.util.UUID

object TodoItemOwnerships: IntIdTable(name = "TodoItemOwnerships", columnName = "Id") {
    val userId: Column<Int> = integer("UserId").index()
    val itemUniqueId: Column<UUID> = uuid("itemUniqueId").index()
}
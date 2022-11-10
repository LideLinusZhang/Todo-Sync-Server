package edu.uwaterloo.cs.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Users : IntIdTable(name = "Users", columnName = "Id") {
    val name: Column<String> = text("Name").uniqueIndex()
    val hashedPassword: Column<String> = text("HashedPassword")
}
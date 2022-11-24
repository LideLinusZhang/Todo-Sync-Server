package edu.uwaterloo.cs.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

object Users : IntIdTable(name = "Users", columnName = "Id") {
    val name: Column<String> = text("Name")
    val byOAuth: Column<Boolean> = bool("ByOAuth")
    val hashedPassword: Column<ExposedBlob?> = blob("HashedPassword").nullable()

    init {
        uniqueIndex(name, byOAuth)
    }
}
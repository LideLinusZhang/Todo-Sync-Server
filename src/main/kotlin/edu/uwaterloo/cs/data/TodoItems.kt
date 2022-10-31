package data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object TodoItems: IntIdTable(name = "TodoItems", columnName = "Id") {
    val uniqueId: Column<UUID> = uuid("UniqueId").uniqueIndex()
    val name: Column<String> = text("Name")
    val description: Column<String> = text("Description")
    val importance: Column<Int> = integer("Importance")
    val deadline: Column<Int?> = integer("Deadline").nullable()
    val modifiedTime: Column<Long> = long("ModifiedTime").index()
    var categoryId: Column<UUID> = uuid("categoryId")
}
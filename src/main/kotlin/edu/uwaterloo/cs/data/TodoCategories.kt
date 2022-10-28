package data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object TodoCategories: IntIdTable(name = "TodoCategories", columnName = "Id") {
    val uniqueId: Column<UUID> = uuid("UniqueId").uniqueIndex()
    val name: Column<String> = text("Name").uniqueIndex()
    val favoured: Column<Boolean> = bool("Favoured")
    val modifiedTime: Column<Long> = long("ModifiedTime")
}
package data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.Connection

object DataFactory {
    init {
        connect()
        createSchema()
    }

    private fun connect() {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:h2:./h2data"
        config.driverClassName = "org.h2.Driver"
        config.validate()

        Database.connect(HikariDataSource(config))
    }

    private fun createSchema() {
        org.jetbrains.exposed.sql.transactions.transaction {
            SchemaUtils.createMissingTablesAndColumns(TodoCategories, TodoItems)
        }
    }

    suspend fun <T> transaction(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}
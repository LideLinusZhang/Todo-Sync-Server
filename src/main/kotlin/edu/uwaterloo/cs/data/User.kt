package edu.uwaterloo.cs.data

import io.ktor.server.auth.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

class User(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var hashedPassword: ByteArray by Users.hashedPassword.transform({ ExposedBlob(it) }, { it.bytes })

    val categories by TodoCategory via TodoCategoryOwnerships
    val items by TodoItem via TodoItemOwnerships
}
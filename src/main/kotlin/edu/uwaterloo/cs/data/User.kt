package edu.uwaterloo.cs.data

import io.ktor.server.auth.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var hashedPassword: ByteArray by Users.hashedPassword.transform(
        { String(it, Charsets.UTF_8) },
        { it.toByteArray(Charsets.UTF_8) })
}
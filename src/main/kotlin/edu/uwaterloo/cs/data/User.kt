package edu.uwaterloo.cs.data

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    val name by Users.name
    var hashedPassword by Users.hashedPassword
}
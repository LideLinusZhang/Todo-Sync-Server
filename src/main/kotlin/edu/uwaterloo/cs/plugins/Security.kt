package edu.uwaterloo.cs.plugins

import edu.uwaterloo.cs.data.DataFactory
import edu.uwaterloo.cs.data.User
import edu.uwaterloo.cs.data.Users
import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureSecurity() {
    install(Authentication) {
        digest("auth-digest") {
            realm = edu.uwaterloo.cs.todo.lib.realm
            digestProvider { userName, _ ->
                DataFactory.transaction {
                    User.find { Users.name eq userName }.firstOrNull()?.hashedPassword
                }
            }
        }
    }
}

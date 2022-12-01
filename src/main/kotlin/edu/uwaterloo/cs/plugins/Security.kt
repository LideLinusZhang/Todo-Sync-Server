package edu.uwaterloo.cs.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import edu.uwaterloo.cs.data.DataFactory
import edu.uwaterloo.cs.data.User
import edu.uwaterloo.cs.data.Users
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.not

fun Application.configureSecurity() {
    install(Authentication) {
        digest("auth-digest") {
            realm = edu.uwaterloo.cs.todo.lib.realm
            digestProvider { userName, _ ->
                DataFactory.transaction {
                    User.find { Users.name eq userName and not(Users.byOAuth) }.firstOrNull()?.hashedPassword
                }
            }
        }
        jwt("auth-jwt") {
            realm = edu.uwaterloo.cs.todo.lib.realm
            verifier(JWT.require(Algorithm.HMAC256(System.getenv("JWT_SECRET"))).build())
            validate { credential ->
                if (!credential.payload.getClaim("username").asString().isNullOrEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

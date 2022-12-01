package edu.uwaterloo.cs

import edu.uwaterloo.cs.plugins.configureRouting
import edu.uwaterloo.cs.plugins.configureSecurity
import edu.uwaterloo.cs.plugins.configureSerialization
import edu.uwaterloo.cs.plugins.configureSwagger
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSecurity()
        configureSerialization()
        configureRouting()
        configureSwagger()
    }.start(wait = true)
}

fun ApplicationCall.getUserName(): String {
    return when (val principal = principal<Principal>()) {
        is UserIdPrincipal -> principal.name
        is JWTPrincipal -> principal.payload.getClaim("username").asString()
        else -> String()
    }
}
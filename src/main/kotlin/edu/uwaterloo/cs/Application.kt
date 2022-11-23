package edu.uwaterloo.cs

import edu.uwaterloo.cs.plugins.configureRouting
import edu.uwaterloo.cs.plugins.configureSecurity
import edu.uwaterloo.cs.plugins.configureSerialization
import edu.uwaterloo.cs.plugins.configureSwagger
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

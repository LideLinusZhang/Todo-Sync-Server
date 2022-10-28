package edu.uwaterloo.cs

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import edu.uwaterloo.cs.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSecurity()
        configureSerialization()
        configureRouting()
    }.start(wait = true)
}

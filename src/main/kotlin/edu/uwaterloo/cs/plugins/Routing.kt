package edu.uwaterloo.cs.plugins

import edu.uwaterloo.cs.routes.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.locations.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {
    routing {
        categoryRouting()
        itemRouting()
    }
}

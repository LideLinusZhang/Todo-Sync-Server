package edu.uwaterloo.cs.plugins

import edu.uwaterloo.cs.routes.categoryRouting
import edu.uwaterloo.cs.routes.itemRouting
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        categoryRouting()
        itemRouting()
    }
}

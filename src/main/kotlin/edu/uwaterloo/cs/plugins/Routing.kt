package edu.uwaterloo.cs.plugins

import edu.uwaterloo.cs.routes.categoryRouting
import edu.uwaterloo.cs.routes.itemRouting
import edu.uwaterloo.cs.routes.userRouting
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        categoryRouting()
        itemRouting()
        userRouting()
    }
}

package edu.uwaterloo.cs.plugins

import edu.uwaterloo.cs.routes.categoryRouting
import edu.uwaterloo.cs.routes.itemRouting
import edu.uwaterloo.cs.routes.userRouting
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(httpClient: HttpClient) {
    routing {
        categoryRouting()
        itemRouting()
        userRouting(httpClient)
    }
}

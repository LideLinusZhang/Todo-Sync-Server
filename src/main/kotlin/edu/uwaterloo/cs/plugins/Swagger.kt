package edu.uwaterloo.cs.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.server.application.*

fun Application.configureSwagger() {
    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger-ui"
            forwardRoot = true
        }
        info {
            title = "Todo RESTful APIs"
            version = "latest"
            description = "RESTful APIs currently supported by the Todo Server."
        }
        server {
            url = "http://localhost:8080"
            description = "Todo Server"
        }
    }
}
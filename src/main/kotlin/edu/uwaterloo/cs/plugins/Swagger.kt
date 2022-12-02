package edu.uwaterloo.cs.plugins

import edu.uwaterloo.cs.host
import edu.uwaterloo.cs.port
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.http.*
import io.ktor.server.application.*

fun Application.configureSwagger() {
    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger-ui"
            forwardRoot = false
        }
        info {
            title = "Todo RESTful APIs"
            version = "latest"
            description = "RESTful APIs currently supported by the Todo Server."
        }
        server {
            url = URLBuilder(host = host, port = port).buildString()
            description = "Todo Server"
        }
    }
}
package edu.uwaterloo.cs.routes

import edu.uwaterloo.cs.data.DataFactory
import edu.uwaterloo.cs.data.User
import edu.uwaterloo.cs.data.Users
import edu.uwaterloo.cs.todo.lib.UserModel
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRouting() {
    route("/user") {
        post("/signup", {
            description = "Create an account on the server."
            request { body<UserModel>() }
            response {
                HttpStatusCode.BadRequest to { description = "Request JSON is in incorrect format." }
                HttpStatusCode.Created to { description = "Successful request." }
                HttpStatusCode.Conflict to { description = "User with the same name already exists." }
            }
        }) {
            val userModel: UserModel

            try {
                userModel = call.receive()
            } catch (_: ContentTransformationException) {
                return@post call.respondText("Request JSON is in incorrect format.", status = HttpStatusCode.BadRequest)
            }

            if (DataFactory.transaction { User.find { Users.name eq userModel.name }.empty() }) {
                DataFactory.transaction {
                    User.new {
                        name = userModel.name
                        hashedPassword = userModel.hashedPassword
                    }
                }
                return@post call.respondText("Signed up successfully.", status = HttpStatusCode.Created)
            } else return@post call.respondText(
                "User with the same name already exists.",
                status = HttpStatusCode.Conflict
            )
        }
        authenticate("auth-digest") {
            post("/change_password", {
                description = "Change the user's passowrd after successful authentication."
                request { body<ByteArray>() }
                response {
                    HttpStatusCode.Unauthorized to { description = "User credential is incorrect." }
                    HttpStatusCode.Created to { description = "Successful request." }
                }
            }) {
                val newHashedPassword: ByteArray = call.receive()
                val principal = call.principal<UserIdPrincipal>()!!
                val user = User.find { Users.name eq principal.name }.notForUpdate().first()

                DataFactory.transaction { user.hashedPassword = newHashedPassword }
                return@post call.respondText("Password changed successfully.", status = HttpStatusCode.OK)
            }
            post("/login", { description = "Endpoint for OAuth, not yet implemented." }) {
                call.respondText("Not yet implemented", status = HttpStatusCode.NotImplemented)
            }
        }
    }
}
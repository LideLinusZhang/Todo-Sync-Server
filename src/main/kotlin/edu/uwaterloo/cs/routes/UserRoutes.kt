package edu.uwaterloo.cs.routes

import edu.uwaterloo.cs.data.DataFactory
import edu.uwaterloo.cs.data.User
import edu.uwaterloo.cs.data.Users
import edu.uwaterloo.cs.todo.lib.UserModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRouting() {
    route("/user") {
        post("/signup") {
            val userModel: UserModel

            try {
                userModel = call.receive()
            } catch (_: ContentTransformationException) {
                return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
            }

            if (DataFactory.transaction { User.find { Users.name eq userModel.name } }.empty()) {
                DataFactory.transaction {
                    User.new {
                        name = userModel.name
                        hashedPassword = userModel.hashedPassword
                    }
                }
                return@post call.respondText("Signed up successfully", status = HttpStatusCode.Created)
            } else return@post call.respondText(
                "User with the same name already exists",
                status = HttpStatusCode.Conflict
            )
        }
        authenticate("auth-digest") {
            post("/change_password") {
                val newHashedPassword: ByteArray = call.receive()
                val principal = call.principal<User>()!!

                DataFactory.transaction { User.findById(principal.id)?.hashedPassword = newHashedPassword }
                return@post call.respondText("Password changed successfully", status = HttpStatusCode.OK)
            }
            post("/login") {

            }
        }
    }
}
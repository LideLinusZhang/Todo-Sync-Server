package edu.uwaterloo.cs.routes

import edu.uwaterloo.cs.data.DataFactory
import edu.uwaterloo.cs.data.TodoCategory
import edu.uwaterloo.cs.data.TodoCategoryOwnerships
import edu.uwaterloo.cs.data.User
import edu.uwaterloo.cs.todo.lib.TodoCategoryModel
import edu.uwaterloo.cs.todo.lib.TodoCategoryModificationModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import java.util.*

fun Route.categoryRouting() {
    route("/category") {
        authenticate("auth-digest") {
            get {
                val principal = call.principal<User>()!!

                DataFactory.transaction {
                    val user = User.findById(principal.id)!!
                    call.respond(user.categories.notForUpdate().map { it.toModel() })
                }
            }
            post {
                val todoCategoryModel: TodoCategoryModel
                val principal = call.principal<User>()!!

                try {
                    todoCategoryModel = call.receive()
                } catch (_: ContentTransformationException) {
                    return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.findById(principal.id)!!

                    if (user.categories.any { it.name == todoCategoryModel.name }) {
                        call.respondText(
                            "Category with the same name already exist.",
                            status = HttpStatusCode.Conflict
                        )
                    } else {
                        val newCategory = TodoCategory.new(todoCategoryModel.uniqueId) {
                            name = todoCategoryModel.name
                            favoured = todoCategoryModel.favoured
                        }

                        TodoCategoryOwnerships.insert {
                            it[category] = newCategory.id
                            it[TodoCategoryOwnerships.user] = user.id
                        }

                        call.respondText("Category added successfully.", status = HttpStatusCode.Created)
                    }
                }
            }
            post("{?id}") {
                val todoCategoryModel: TodoCategoryModificationModel
                val uniqueId: UUID
                val principal = call.principal<User>()!!

                try {
                    todoCategoryModel = call.receive()
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: RuntimeException) {
                    return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.findById(principal.id)!!
                    val existingCategory = user.categories.find { it.id.value == uniqueId }

                    if (existingCategory === null) {
                        call.respondText(
                            "Category with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    } else if (existingCategory.modifiedTime > todoCategoryModel.modifiedTime) {
                        call.respondText(
                            "Category on the server is more recent",
                            status = HttpStatusCode.NotModified
                        )
                    } else {
                        existingCategory.name = todoCategoryModel.name ?: existingCategory.name
                        existingCategory.favoured = todoCategoryModel.favoured ?: existingCategory.favoured
                        existingCategory.modifiedTime = todoCategoryModel.modifiedTime

                        call.respondText("Category modified successfully.", status = HttpStatusCode.Accepted)
                    }
                }
            }
            delete("{?id}") {
                val uniqueId: UUID
                val principal = call.principal<User>()!!

                try {
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: IllegalArgumentException) {
                    return@delete call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.findById(principal.id)!!
                    val existingCategory = user.categories.find { it.id.value == uniqueId }

                    if (existingCategory === null) {
                        call.respondText(
                            "Item with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    } else {
                        existingCategory.delete()

                        call.respondText(
                            "Category and associated items deleted successfully.",
                            status = HttpStatusCode.OK
                        )
                    }
                }
            }
        }
    }
}
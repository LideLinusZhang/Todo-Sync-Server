package edu.uwaterloo.cs.routes

import edu.uwaterloo.cs.data.*
import edu.uwaterloo.cs.getUserName
import edu.uwaterloo.cs.todo.lib.TodoCategoryModel
import edu.uwaterloo.cs.todo.lib.TodoCategoryModificationModel
import io.github.smiley4.ktorswaggerui.dsl.delete
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import java.util.*

fun Route.categoryRouting() {
    authenticate("auth-digest", "auth-jwt") {
        route("/category") {
            get({
                description = "Obtain all todo categories that belong to a user."
                response {
                    HttpStatusCode.OK to {
                        description = "A list of all todo categories that belong to the user"
                        body<List<TodoCategoryModel>>()
                    }
                    HttpStatusCode.Unauthorized to { description = "User credential is incorrect." }
                }
            }) {
                val username = call.getUserName()

                DataFactory.transaction {
                    val user = User.find { Users.name eq username }.notForUpdate().first()
                    call.respond(user.categories.notForUpdate().map { it.toModel() })
                }
            }
            post("/add", {
                description = "Add a todo category to a user."
                request { body<TodoCategoryModel>() }
                response {
                    HttpStatusCode.Created to { description = "Successful Request" }
                    HttpStatusCode.Unauthorized to { description = "User credential is incorrect." }
                    HttpStatusCode.BadRequest to { description = "The request JSON is in incorrect format." }
                    HttpStatusCode.Conflict to { description = "Category with the same name already exist." }
                }
            }) {
                val todoCategoryModel: TodoCategoryModel
                val username = call.getUserName()

                try {
                    todoCategoryModel = call.receive()
                } catch (_: Exception) {
                    return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq username }.notForUpdate().first()

                    if (user.categories.any { it.name == todoCategoryModel.name }) {
                        call.respondText(
                            "Category with the same name already exist",
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

                        call.respondText("Category added successfully", status = HttpStatusCode.Created)
                    }
                }
            }
            post("/modify{?id}", {
                description = "Modify an existing todo category of a user."
                request { body<TodoCategoryModificationModel>() }
                response {
                    HttpStatusCode.Created to { description = "Successful Request" }
                    HttpStatusCode.Unauthorized to { description = "User credential is incorrect." }
                    HttpStatusCode.BadRequest to { description = "The request JSON is in incorrect format." }
                    HttpStatusCode.NotModified to { description = "Category on the server is more recent." }
                }
            }) {
                val todoCategoryModel: TodoCategoryModificationModel
                val uniqueId: UUID
                val username = call.getUserName()

                try {
                    todoCategoryModel = call.receive()
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: Exception) {
                    return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq username }.notForUpdate().first()
                    val associatedCategory = user.categories.notForUpdate().find { it.id.value == uniqueId }

                    if (associatedCategory === null) {
                        call.respondText(
                            "Category with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    } else if (associatedCategory.modifiedTime > todoCategoryModel.modifiedTime) {
                        call.respondText(
                            "Category on the server is more recent",
                            status = HttpStatusCode.NotModified
                        )
                    } else {
                        val categoryToModify = TodoCategory.findById(associatedCategory.id)!!

                        categoryToModify.name = todoCategoryModel.name ?: categoryToModify.name
                        categoryToModify.favoured = todoCategoryModel.favoured ?: categoryToModify.favoured
                        categoryToModify.modifiedTime = todoCategoryModel.modifiedTime

                        call.respondText("Category modified successfully", status = HttpStatusCode.Accepted)
                    }
                }
            }
            delete("/delete{?id}", {
                description = "Delete an existing todo category of a user."
                request { pathParameter<UUID>("id") { description = "Unique ID of the category" } }
                response {
                    HttpStatusCode.OK to { description = "Successful Request" }
                    HttpStatusCode.Unauthorized to { description = "User credential is incorrect." }
                    HttpStatusCode.BadRequest to {
                        description = "The unique ID is in incorrect format or does not exist."
                    }
                }
            }) {
                val uniqueId: UUID
                val username = call.getUserName()

                try {
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: Exception) {
                    return@delete call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq username }.notForUpdate().first()
                    val associatedCategory = user.categories.notForUpdate().find { it.id.value == uniqueId }

                    if (associatedCategory === null) {
                        call.respondText(
                            "Category with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    } else {
                        val categoryToDelete = TodoCategory.findById(associatedCategory.id)!!
                        categoryToDelete.delete()

                        call.respondText(
                            "Category and associated items deleted successfully",
                            status = HttpStatusCode.OK
                        )
                    }
                }
            }
        }
    }
}
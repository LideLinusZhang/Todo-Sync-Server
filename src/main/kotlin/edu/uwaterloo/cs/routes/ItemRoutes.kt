package edu.uwaterloo.cs.routes

import edu.uwaterloo.cs.data.*
import edu.uwaterloo.cs.getUserName
import edu.uwaterloo.cs.todo.lib.TodoItemModel
import edu.uwaterloo.cs.todo.lib.TodoItemModificationModel
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

fun Route.itemRouting() {
    authenticate("auth-digest", "auth-jwt") {
        route("/item") {
            get("{categoryUniqueId?}", {
                description = "Obtain all todo items that belong to a user and under a given category."
                response {
                    HttpStatusCode.OK to {
                        description = "A list of all todo items that belong to the user and under the category"
                        body<List<TodoItemModel>>()
                    }
                    HttpStatusCode.Unauthorized to { description = "User credential is incorrect." }
                    HttpStatusCode.BadRequest to {
                        description =
                            "Category with the provided unique ID does not exist," +
                                    " or the unique ID is in incorrect format."
                    }
                }
                request { pathParameter<UUID>("categoryUniqueId") }
            }) {
                val username = call.getUserName()
                val uniqueId: UUID

                try {
                    uniqueId = UUID.fromString(call.parameters["categoryUniqueId"])
                } catch (_: Exception) {
                    return@get call.respondText("Unique ID is in incorrect format", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq username }.notForUpdate().first()
                    val category = user.categories.notForUpdate().find { it.id.value == uniqueId }

                    if (category === null)
                        call.respondText(
                            "Category with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    else
                        call.respond(category.items.notForUpdate().map { it.toModel() })
                }
            }
            post("/modify{id?}", {
                description = "Modify an existing todo category of a user."
                request { body<TodoItemModificationModel>() }
                response {
                    HttpStatusCode.Created to { description = "Successful Request" }
                    HttpStatusCode.Unauthorized to { description = "User credential is incorrect." }
                    HttpStatusCode.BadRequest to {
                        description =
                            "The request JSON or unique ID is in incorrect format, or the unique ID does not exist."
                    }
                    HttpStatusCode.NotModified to { description = "Item on the server is more recent." }
                }
            }) {
                val uniqueId: UUID
                val itemModificationModel: TodoItemModificationModel
                val username = call.getUserName()

                try {
                    itemModificationModel = call.receive()
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: Exception) {
                    return@post call.respondText(
                        "Unique ID or request JSON is in incorrect format",
                        status = HttpStatusCode.BadRequest
                    )
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq username }.notForUpdate().first()
                    val associatedItem = user.items.notForUpdate().find { it.id.value == uniqueId }

                    if (associatedItem === null)
                        call.respondText(
                            "Item with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    else if (associatedItem.modifiedTime > itemModificationModel.modifiedTime)
                        call.respondText("Item on the server is more recent", status = HttpStatusCode.NotModified)
                    else {
                        val itemToModify = TodoItem.findById(associatedItem.id)!!

                        itemToModify.name = itemModificationModel.name ?: itemToModify.name
                        itemToModify.deadline = itemModificationModel.deadline ?: itemToModify.deadline
                        itemToModify.description = itemModificationModel.description ?: itemToModify.description
                        itemToModify.importance = itemModificationModel.importance ?: itemToModify.importance
                        itemToModify.favoured = itemModificationModel.favoured ?: itemToModify.favoured
                        itemToModify.modifiedTime = itemModificationModel.modifiedTime

                        call.respondText("Item modified successfully", status = HttpStatusCode.Accepted)
                    }

                }
            }
            delete("/delete{id?}", {
                description = "Delete an existing todo item of a user."
                request { pathParameter<UUID>("id") { description = "Unique ID of the item" } }
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
                    val associatedItem = user.items.notForUpdate().find { it.id.value == uniqueId }

                    if (associatedItem === null)
                        call.respondText(
                            "Item with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    else {
                        val itemToDelete = TodoItem.findById(associatedItem.id)!!

                        itemToDelete.delete()
                        call.respondText("Item deleted successfully", status = HttpStatusCode.OK)
                    }
                }
            }
            post("/add", {
                description = "Add a todo item to a user."
                request { body<TodoItemModel>() }
                response {
                    HttpStatusCode.Created to { description = "Successful Request" }
                    HttpStatusCode.Unauthorized to { description = "User credential is incorrect." }
                    HttpStatusCode.BadRequest to {
                        description =
                            "The request JSON is in incorrect format, " +
                                    "or the category with the provided unique ID does not exist."
                    }
                }
            }) {
                val todoItemModel: TodoItemModel
                val username = call.getUserName()

                try {
                    todoItemModel = call.receive()
                } catch (_: Exception) {
                    return@post call.respondText(
                        "The request JSON is in incorrect format.",
                        status = HttpStatusCode.BadRequest
                    )
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq username }.notForUpdate().first()
                    val category = user.categories.find { it.id.value == todoItemModel.categoryId }

                    if (category === null) {
                        call.respondText(
                            "Category with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    } else {
                        TodoItem.new(todoItemModel.uniqueId) {
                            name = todoItemModel.name
                            description = todoItemModel.description
                            importance = todoItemModel.importance
                            favoured = todoItemModel.favoured
                            categoryId = todoItemModel.categoryId
                            modifiedTime = todoItemModel.modifiedTime
                            deadline = todoItemModel.deadline
                        }
                        TodoItemOwnerships.insert {
                            it[TodoItemOwnerships.user] = user.id
                            it[item] = todoItemModel.uniqueId
                        }
                    }
                }

                call.respondText("Item added successfully", status = HttpStatusCode.Created)
            }
        }
    }
}
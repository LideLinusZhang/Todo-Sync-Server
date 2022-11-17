package edu.uwaterloo.cs.routes

import edu.uwaterloo.cs.data.*
import edu.uwaterloo.cs.todo.lib.TodoItemModel
import edu.uwaterloo.cs.todo.lib.TodoItemModificationModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import java.util.*

fun Route.itemRouting() {
    authenticate("auth-digest") {
        route("/item") {
            get("{categoryUniqueId?}") {
                val principal = call.principal<UserIdPrincipal>()!!
                val uniqueId: UUID

                try {
                    uniqueId = UUID.fromString(call.parameters["categoryUniqueId"])
                } catch (_: Exception) {
                    return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
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
            post("/modify{id?}") {
                val uniqueId: UUID
                val itemModificationModel: TodoItemModificationModel
                val principal = call.principal<UserIdPrincipal>()!!

                try {
                    itemModificationModel = call.receive()
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: Exception) {
                    return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
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
            delete("/delete{id?}") {
                val uniqueId: UUID
                val principal = call.principal<UserIdPrincipal>()!!

                try {
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: Exception) {
                    return@delete call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
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
            post("/add") {
                val todoItemModel: TodoItemModel
                val principal = call.principal<UserIdPrincipal>()!!

                try {
                    todoItemModel = call.receive()
                } catch (_: Exception) {
                    return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
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

                call.respondText("Category added successfully", status = HttpStatusCode.Created)
            }
        }
    }
}
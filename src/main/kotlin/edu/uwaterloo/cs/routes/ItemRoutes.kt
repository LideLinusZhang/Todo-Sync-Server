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
                } catch (_: IllegalArgumentException) {
                    return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
                    val category = user.categories.find { it.id.value == uniqueId }

                    if (category === null)
                        call.respondText(
                            "Category with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    else
                        call.respond(category.items.notForUpdate().map { it.toModel() })
                }
            }
            post {
                val todoItemModel: TodoItemModel
                val principal = call.principal<UserIdPrincipal>()!!

                try {
                    todoItemModel = call.receive()
                } catch (_: ContentTransformationException) {
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

                call.respondText("Category added successfully.", status = HttpStatusCode.Created)
            }
            post("{id?}") {
                val uniqueId: UUID
                val todoItemModel: TodoItemModificationModel
                val principal = call.principal<UserIdPrincipal>()!!

                try {
                    todoItemModel = call.receive()
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: RuntimeException) {
                    return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
                    val existingItem = user.items.find { it.id.value == uniqueId }

                    if (existingItem === null)
                        call.respondText(
                            "Item with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    else if (existingItem.modifiedTime > todoItemModel.modifiedTime)
                        call.respondText("Item on the server is more recent", status = HttpStatusCode.NotModified)
                    else {
                        existingItem.name = todoItemModel.name ?: existingItem.name
                        existingItem.deadline = todoItemModel.deadline ?: existingItem.deadline
                        existingItem.description = todoItemModel.description ?: existingItem.description
                        existingItem.importance = todoItemModel.importance ?: existingItem.importance
                        existingItem.favoured = todoItemModel.favoured ?: existingItem.favoured
                        existingItem.modifiedTime = todoItemModel.modifiedTime

                        call.respondText("Item modified successfully.", status = HttpStatusCode.Accepted)
                    }

                }
            }
            delete("{id?}") {
                val uniqueId: UUID
                val principal = call.principal<UserIdPrincipal>()!!

                try {
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: IllegalArgumentException) {
                    return@delete call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
                    val existingItem = user.items.find { it.id.value == uniqueId }

                    if (existingItem === null)
                        call.respondText(
                            "Item with the provided unique ID does not exist",
                            status = HttpStatusCode.BadRequest
                        )
                    else {
                        existingItem.delete()
                        call.respondText("Item deleted successfully.", status = HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}
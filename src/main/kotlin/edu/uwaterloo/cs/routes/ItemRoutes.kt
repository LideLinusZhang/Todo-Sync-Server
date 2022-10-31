package edu.uwaterloo.cs.routes

import edu.uwaterloo.cs.data.DataFactory
import edu.uwaterloo.cs.data.TodoCategories
import edu.uwaterloo.cs.data.TodoItem
import edu.uwaterloo.cs.data.TodoItems
import edu.uwaterloo.cs.todo.lib.TodoItemModel
import edu.uwaterloo.cs.todo.lib.TodoItemModificationModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.select
import java.util.*

fun Route.itemRouting() {
    route("/item") {
        get("categoryUniqueId={id?}") {
            val uniqueId: UUID

            try {
                uniqueId = UUID.fromString(call.parameters["id"])
            } catch (_: IllegalArgumentException) {
                return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
            }

            DataFactory.transaction {
                if (TodoCategories.select { TodoCategories.uniqueId eq uniqueId }.empty())
                    call.respondText(
                        "Category with the provided unique ID does not exist",
                        status = HttpStatusCode.BadRequest
                    )
                else
                    call.respond(TodoItem.find { TodoItems.categoryId eq uniqueId }.notForUpdate().map { it.toModel() })
            }
        }
        post("categoryUniqueId={id?}") {
            val todoItemModel: TodoItemModel

            try {
                todoItemModel = call.receive()
            } catch (_: ContentTransformationException) {
                return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
            }

            DataFactory.transaction {
                TodoItem.new {
                    name = todoItemModel.name
                    description = todoItemModel.description
                    importance = todoItemModel.importance
                    categoryId = todoItemModel.categoryId
                    modifiedTime = todoItemModel.modifiedTime
                    deadline = todoItemModel.deadline
                }
            }

            call.respondText("Category added successfully.", status = HttpStatusCode.Created)
        }
        post("{id?}") {
            val uniqueId: UUID
            val todoItemModel: TodoItemModificationModel

            try {
                todoItemModel = call.receive()
                uniqueId = UUID.fromString(call.parameters["id"])
            } catch (_: RuntimeException) {
                return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
            }

            DataFactory.transaction {
                val existingItem = TodoItem.find { TodoItems.uniqueId eq uniqueId }.firstOrNull()

                if (existingItem === null)
                    call.respondText(
                        "Item with the provided unique ID does not exist",
                        status = HttpStatusCode.BadRequest
                    )
                else {
                    if (existingItem.modifiedTime > todoItemModel.modifiedTime)
                        call.respondText("Item on the server is more recent", status = HttpStatusCode.NotModified)
                    else {
                        existingItem.name = todoItemModel.name ?: existingItem.name
                        existingItem.deadline = todoItemModel.deadline ?: existingItem.deadline
                        existingItem.description = todoItemModel.description ?: existingItem.description
                        existingItem.importance = todoItemModel.importance ?: existingItem.importance
                        existingItem.modifiedTime = todoItemModel.modifiedTime

                        call.respondText("Item modified successfully.", status = HttpStatusCode.Accepted)
                    }
                }
            }
        }
        delete("{id?}") {
            val uniqueId: UUID

            try {
                uniqueId = UUID.fromString(call.parameters["id"])
            } catch (_: IllegalArgumentException) {
                return@delete call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
            }

            DataFactory.transaction {
                val existingItem = TodoItem.find { TodoItems.uniqueId eq uniqueId }.firstOrNull()

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
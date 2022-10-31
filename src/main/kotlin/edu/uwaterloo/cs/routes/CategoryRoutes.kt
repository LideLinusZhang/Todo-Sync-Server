package edu.uwaterloo.cs.routes

import edu.uwaterloo.cs.data.*
import edu.uwaterloo.cs.todo.lib.TodoCategoryModel
import edu.uwaterloo.cs.todo.lib.TodoCategoryModificationModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.select
import java.util.*

fun Route.categoryRouting() {
    route("/category") {
        get {
            DataFactory.transaction {
                call.respond(TodoCategory.all().notForUpdate().map { it.toModel() })
            }
        }
        post {
            val todoCategoryModel: TodoCategoryModel

            try {
                todoCategoryModel = call.receive()
            } catch (_: ContentTransformationException) {
                return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
            }

            DataFactory.transaction {
                if (!TodoCategories.select { TodoCategories.name eq todoCategoryModel.name }.empty())
                    call.respondText("Category with the same name already exist.", status = HttpStatusCode.Conflict)
                else {
                    TodoCategory.new {
                        name = todoCategoryModel.name
                        favoured = todoCategoryModel.favoured
                        uniqueId = todoCategoryModel.uniqueId
                    }

                    call.respondText("Category added successfully.", status = HttpStatusCode.Created)
                }
            }
        }
        post("{?id}") {
            val todoCategoryModel: TodoCategoryModificationModel
            val uniqueId: UUID

            try {
                todoCategoryModel = call.receive()
                uniqueId = UUID.fromString(call.parameters["id"])
            } catch (_: RuntimeException) {
                return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
            }

            DataFactory.transaction {
                val existingCategory = TodoCategory.find { TodoItems.uniqueId eq uniqueId }.firstOrNull()

                if (existingCategory === null)
                    call.respondText(
                        "Category with the provided unique ID does not exist",
                        status = HttpStatusCode.BadRequest
                    )
                else {
                    if (existingCategory.modifiedTime > todoCategoryModel.modifiedTime)
                        call.respondText("Category on the server is more recent", status = HttpStatusCode.NotModified)
                    else {
                        existingCategory.name = todoCategoryModel.name ?: existingCategory.name
                        existingCategory.favoured = todoCategoryModel.favoured ?: existingCategory.favoured
                        existingCategory.modifiedTime = todoCategoryModel.modifiedTime

                        call.respondText("Category modified successfully.", status = HttpStatusCode.Accepted)
                    }
                }
            }
        }
        delete("{?id}") {
            val uniqueId: UUID

            try {
                uniqueId = UUID.fromString(call.parameters["id"])
            } catch (_: IllegalArgumentException) {
                return@delete call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
            }

            DataFactory.transaction {
                val existingCategory = TodoCategory.find { TodoCategories.uniqueId eq uniqueId }.firstOrNull()

                if (existingCategory === null)
                    call.respondText(
                        "Item with the provided unique ID does not exist",
                        status = HttpStatusCode.BadRequest
                    )
                else {
                    val existingItem = TodoItem.find { TodoItems.categoryId eq uniqueId }

                    existingItem.forEach { it.delete() }
                    existingCategory.delete()

                    call.respondText("Category and associated items deleted successfully.", status = HttpStatusCode.OK)
                }
            }
        }
    }
}
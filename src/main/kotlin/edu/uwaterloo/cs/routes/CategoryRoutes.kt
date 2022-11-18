package edu.uwaterloo.cs.routes

import edu.uwaterloo.cs.data.*
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
    authenticate("auth-digest") {
        route("/category") {
            get {
                val principal = call.principal<UserIdPrincipal>()!!

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
                    call.respond(user.categories.notForUpdate().map { it.toModel() })
                }
            }
            post("/add") {
                val todoCategoryModel: TodoCategoryModel
                val principal = call.principal<UserIdPrincipal>()!!

                try {
                    todoCategoryModel = call.receive()
                } catch (_: Exception) {
                    return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()

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
            post("/modify{?id}") {
                val todoCategoryModel: TodoCategoryModificationModel
                val uniqueId: UUID
                val principal = call.principal<UserIdPrincipal>()!!

                try {
                    todoCategoryModel = call.receive()
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: Exception) {
                    return@post call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
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

            delete("/delete{?id}") {
                val uniqueId: UUID
                val principal = call.principal<UserIdPrincipal>()!!

                try {
                    uniqueId = UUID.fromString(call.parameters["id"])
                } catch (_: Exception) {
                    return@delete call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                }

                DataFactory.transaction {
                    val user = User.find { Users.name eq principal.name }.notForUpdate().first()
                    val associatedCategory = user.categories.notForUpdate().find { it.id.value == uniqueId }

                    if (associatedCategory === null) {
                        call.respondText(
                            "Item with the provided unique ID does not exist",
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
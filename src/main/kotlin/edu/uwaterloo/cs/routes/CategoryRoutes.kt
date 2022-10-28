package edu.uwaterloo.cs.routes

import data.DataFactory
import data.TodoCategory
import edu.uwaterloo.cs.todo.lib.TodoCategoryModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRouting() {
    route("/category") {
         get {
             DataFactory.transaction {
                 call.respond(TodoCategory.all().notForUpdate().map { it.toModel() })
             }
         }
        post {
            val todoCategoryModel = call.receive<TodoCategoryModel>()
            DataFactory.transaction {
                TodoCategory.new {
                    name = todoCategoryModel.name
                    favoured = todoCategoryModel.favoured
                    uniqueId = todoCategoryModel.uniqueId
                }
            }
            call.respondText("Category added successfully.", status = HttpStatusCode.Created)
        }
    }
}
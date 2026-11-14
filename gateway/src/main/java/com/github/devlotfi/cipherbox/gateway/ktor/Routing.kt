package com.github.devlotfi.cipherbox.gateway.ktor

import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.routing
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.Serializable

@Serializable
data class Todo(
  val id: Int,
  val title: String,
  val completed: Boolean
)

@Serializable
data class ErrorResponse(
  val message: String
)

// Dummy in-memory data
private val todos = listOf(
  Todo(id = 1, title = "Buy milk", completed = false),
  Todo(id = 2, title = "Write Ktor server", completed = true),
  Todo(id = 3, title = "Document the API", completed = false)
)

@OptIn(ExperimentalKtorApi::class)
fun Application.configureRouting() {
  routing {
    // GET /todos -> list of todos
    get("/todos") {
      call.respond(todos)
    }.describe {
      tag("Todos")
      operationId = "listTodos"
      summary = "List all todos"
      description = "Returns every todo currently stored in memory."
      responses {
        HttpStatusCode.OK {
          description = "All todos"
          schema = jsonSchema<List<Todo>>()
        }
      }
    }

    // GET /todos/{id} -> one todo, or 400 / 404
    get("/todos/{id}") {
      val id = call.parameters["id"]?.toIntOrNull()
      if (id == null) {
        call.respond(HttpStatusCode.BadRequest, ErrorResponse("id must be an integer"))
        return@get
      }

      val todo = todos.find { it.id == id }
      if (todo == null) {
        call.respond(HttpStatusCode.NotFound, ErrorResponse("Todo $id not found"))
      } else {
        call.respond(todo)
      }
    }.describe {
      tag("Todos")
      operationId = "getTodo"
      summary = "Get a todo by id"
      description = "Returns a single todo, or an error if the id is invalid or unknown."
      parameters {
        path("id") {
          description = "Todo id (integer)"
        }
      }
      responses {
        HttpStatusCode.OK {
          description = "The requested todo"
          schema = jsonSchema<Todo>()
        }
        HttpStatusCode.BadRequest {
          description = "The id is not an integer"
          schema = jsonSchema<ErrorResponse>()
        }
        HttpStatusCode.NotFound {
          description = "No todo with this id"
          schema = jsonSchema<ErrorResponse>()
        }
      }
    }
  }
}
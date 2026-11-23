package com.github.devlotfi.cipherbox.gateway.ktor

import com.github.devlotfi.cipherbox.shared.transport.packet.TransportPacket
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.Serializable

@Serializable
data class TestResponse(
  val status: String = "OK"
)

@OptIn(ExperimentalKtorApi::class)
fun Application.configureRouting(
  onTransportPacket: (TransportPacket.DataTor) -> Unit,
) {
  routing {
    get("/test") {
      call.respond(TestResponse())
    }.describe {
      operationId = "test"
      responses {
        HttpStatusCode.OK {
          schema = jsonSchema<TestResponse>()
        }
      }
    }

    post("/send") {
      val body = try {
        call.receive<TransportPacket.DataTor>()
      } catch (e: Exception) {
        call.respond(
          HttpStatusCode.BadRequest,
          "Invalid packet"
        )
        return@post
      }
      println("Received data: ${body}")
      onTransportPacket(body)
      call.respond(TestResponse())
    }.describe {
      operationId = "send"
      requestBody {
        required = true
        schema = jsonSchema<TransportPacket.DataTor>()
      }
      responses {
        HttpStatusCode.OK {
          schema = jsonSchema<TestResponse>()
        }
      }
    }
  }
}
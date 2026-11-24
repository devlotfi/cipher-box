package com.github.devlotfi.cipherbox.gateway.ktor

import com.github.devlotfi.cipherbox.shared.global.JsonInstances
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

@Serializable
data class SendBody(
  val rawTransportPacket: String
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
        call.receive<SendBody>()
      } catch (e: Exception) {
        call.respond(
          HttpStatusCode.BadRequest,
          "Invalid packet"
        )
        return@post
      }
      println("Received data: ${body}")

      val transportPacket = try {
        JsonInstances.jsonInstance.decodeFromString<TransportPacket>(body.rawTransportPacket)
      } catch (e: Exception) {
        call.respond(
          HttpStatusCode.BadRequest,
          "Invalid packet"
        )
        return@post
      }
      if (transportPacket !is TransportPacket.DataTor) {
        call.respond(
          HttpStatusCode.BadRequest,
          "Invalid packet"
        )
        return@post
      }

      onTransportPacket(transportPacket)
      call.respond(TestResponse())
    }.describe {
      operationId = "send"
      requestBody {
        required = true
        schema = jsonSchema<SendBody>()
      }
      responses {
        HttpStatusCode.OK {
          schema = jsonSchema<TestResponse>()
        }
      }
    }
  }
}
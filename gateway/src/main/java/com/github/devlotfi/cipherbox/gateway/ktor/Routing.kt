package com.github.devlotfi.cipherbox.gateway.ktor

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
  val data: String
)

@OptIn(ExperimentalKtorApi::class)
fun Application.configureRouting(
  sendViaBluetooth: (String) -> Boolean,
  gatewayKeyHash: String,
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
      if (!call.hasValidApiKey(gatewayKeyHash)) {
        call.respond(
          HttpStatusCode.Unauthorized,
          "Invalid or missing API key"
        )
        return@post
      }

      val body = call.receive<SendBody>()
      println("Received data: ${body.data}")
      val result = sendViaBluetooth(body.data)
      if (result) {
        call.respond(HttpStatusCode.InternalServerError)
      } else {
        call.respond(TestResponse())
      }
    }.describe {
      operationId = "send"
      security {
        requirement("GatewayKeyAuth")
      }
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
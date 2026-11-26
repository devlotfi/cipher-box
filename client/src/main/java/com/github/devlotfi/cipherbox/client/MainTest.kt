package com.github.devlotfi.cipherbox.client

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.devlotfi.cipherbox.client.services.ClientForegroundService
import com.github.devlotfi.cipherbox.shared.crypto.SHAUtils
import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.transport.packet.Level2CipherPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import java.util.UUID

val testData = """
    {"type":"DATA","requestId":"069a7019-8d2c-417a-a7db-f50b56de6f7b","url":"alice/url","transportPacket":{"mlKemData":{"encapsulation":"MSV5vl+0GrFdo4LHp8UqCvgMq14flfYv3GkSswg2RhifxqyuJHoZuTOo2l7prMO8oJu9r6aG2bNnrUWDLN0TavrH5s96XrrbtijHkB4ex2mn216LknkmeOQGLgLtHpYDpfpSeC2BSa1PoaPyoLkvdEU6Miyp9iajjvvztSc7ocmd9otsvT9OQKkEVEu3bK0YkxOMpWOaQ8nBv7zrEpkXIBjzUaZUbY4HjQtfeao6NxKNPBxMyL0J1QmY3q+Desqjt3jNNHDohfjVg3jAHg05W6MAIA1tmtrpUaqs8dd6GVpfeE9oiRUmjmzvKfrP39EY/o+VOmCVNoZkVf6COQ1WSPd1mVxGkPxu+ac3O4eplCOKMb19jTeG5Cf8aKyBOTW2AqWoBIQa8b3P9nKiA8V7zSm/IEc6lIBh83BjrmnG06rhk4jBmkaz1PtLDwoC596TW65Hk8v6t9bFDmp2WvuOwOp/kgftCO+6pHrnsII/MvtuK/5dCFqvDtYHNw2pON4rx/XvRdhTjwzMMgAfKe1E3nUXuCoUEebKvr8SLQuD1Hlzx67hsvrhjxt8i7VWKHFRYnM+6K18AaIrx0bgru2fyunakw5C4rup4MY9d+nFBFIZZYRYoUlbk+4hEGLjJofkSNijeSTAMFfKrnzpGJBXMNv/V2rkj1OT9OkhGx+r0U5D0UmvxL9dLgjyhUUwVysVF9ssPy0YVZ9+23piOJgv2Oofp/fUIX2LFXRTBbTdLHSONp2HKEMommtYOc6p0ndCmU5xTmqVWBqN9v/rdSx3kRh37/kTfhB8ltn4fGS0hNDAUSO0Tno4CI4eitMh2TNeMZlRuRFpu08Or7MblVzWtWjEefE6Gbs2VqS6YbKUS28HzXzgXex1Rr7OBxFXEilL4TjQppXTfcUd3IwLFHGoaznixRA29BB/NHZp15Abi/A02IsWoNvnbrJThMv6zqD5VAghKrV2+vaRsw1sGgnoYqaxurPUpT1U/tie4ruT4Kukxnz3rFESWwrlgz222pBGaG0x00lDkKdP6xf1PWKVC5WZ21MPD0rYkncF4nECXYBjwoV4eyIeos6F60yb3bzQGAeJFJBab9s/oF4OsTEsk59bt+NiuiMbAKgBrwWBR1WsmCqgx+hlN4KJmKX9yi/xyB74Gik4ivqhk18jrtkqmcJMh78V3S0TxKCx9MAff+9nkYrw5H0XKwBAaxJdGTh+cPJXwBfMrk/4t+pq6DNzIrTL4OqXq9dLPcqSVAQ5Mx9vN0fMux0LHP0PpsBsi34ln7+kH9+Z0RXIEZ0d9kRFx8JC06vatytmCmM7L+Wz3ZkfbYotu6qJ6VwdCPC2jhTmvnNJNI5/a59EIbv1V0F3QZT0od5tZ8ifBIunwm+d4JlyaIlxE50FsNS6CYuilVKoHJof0qoZ2r/bD/oHaAWH8xfvCAgEF/iV8H8BDTRjJzE="},"hkdfData":{"salt":"M9bYWY3MAB6jHSJ3X5RFDNf1brXCS8z7k/E+Glfb+mA=","info":"message-transport-packet"},"aesGcmData":{"ciphertext":"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","iv":"AsXAuAQzz+fEsGEW"}}}
""".trimIndent()

@Composable
fun MainTest(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { println(it) }

  Column(
    modifier = modifier.fillMaxSize()
  ) {
    Text("busy: ${ClientForegroundService.isBusy}")
    Text("status: ${ClientForegroundService.usbStatus}")
    Text("connected: ${ClientForegroundService.isUsbConnected}")

    Button(onClick = {
      permissionLauncher.launch(
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
      )
    }) {
      Text("permissions")
    }

    Button(onClick = {
      val intent = Intent(context, ClientForegroundService::class.java)
        .setAction(ClientForegroundService.Actions.START.toString())
      context.startService(intent)
    }) {
      Text("start")
    }

    Button(onClick = {
      val intent = Intent(context, ClientForegroundService::class.java)
        .setAction(ClientForegroundService.Actions.RESTART.toString())
      context.startService(intent)
    }) {
      Text("restart")
    }

    Button(onClick = {
      val intent = Intent(context, ClientForegroundService::class.java)
        .setAction(ClientForegroundService.Actions.STOP.toString())
      context.startService(intent)
    }) {
      Text("stop")
    }

    Button(onClick = {
      val outboundGatewayPacket = OutboundGatewayPacket.DataMqtt(
        topic = "ffedbf40-01fb-46c9-bb58-1df1c6eb62f6",
        requestId = UUID.randomUUID().toString(),
        level2CipherPacket = Level2CipherPacket(
          mlKemData = Level2CipherPacket.MLKEMData(
            encapsulation = testData
          ),
          hkdfData = Level2CipherPacket.HKDFData(
            salt = "test",
            info = "test"
          ),
          aesGcmData = Level2CipherPacket.AESGCMData(
            ciphertext = "lol",
            iv = "lol"
          )
        )
      )
      val rawOutboundGatewayPacket =
        JsonInstances.jsonInstance.encodeToString<OutboundGatewayPacket>(outboundGatewayPacket)
      println("Payload: $rawOutboundGatewayPacket")
      println("Payload hash: ${SHAUtils.hash(outboundGatewayPacket.level2CipherPacket.mlKemData.encapsulation)}")
      ClientForegroundService.send(rawOutboundGatewayPacket)
    }) {
      Text("send mqtt")
    }

    Button(onClick = {
      val outboundGatewayPacket = OutboundGatewayPacket.DataTor(
        url = "l75kbbvh6yxbpp643pxoec4iwjfknybinxris7tv3v7wwp6zea3lkkad.onion",
        requestId = UUID.randomUUID().toString(),
        level2CipherPacket = Level2CipherPacket(
          mlKemData = Level2CipherPacket.MLKEMData(
            encapsulation = testData
          ),
          hkdfData = Level2CipherPacket.HKDFData(
            salt = "test",
            info = "test"
          ),
          aesGcmData = Level2CipherPacket.AESGCMData(
            ciphertext = "lol",
            iv = "lol"
          )
        )
      )
      val rawOutboundGatewayPacket =
        JsonInstances.jsonInstance.encodeToString<OutboundGatewayPacket>(outboundGatewayPacket)
      println("Payload: $rawOutboundGatewayPacket")
      println("Payload hash: ${SHAUtils.hash(outboundGatewayPacket.level2CipherPacket.mlKemData.encapsulation)}")
      ClientForegroundService.send(rawOutboundGatewayPacket)
    }) {
      Text("send tor")
    }
  }
}
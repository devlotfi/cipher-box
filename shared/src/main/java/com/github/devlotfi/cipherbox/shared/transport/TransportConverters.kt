package com.github.devlotfi.cipherbox.shared.transport

import com.github.devlotfi.cipherbox.shared.transport.packet.InboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.TransportPacket

fun OutboundGatewayPacket.DataMqtt.toTransportPacket(topic: String): TransportPacket.DataMqtt {
  return TransportPacket.DataMqtt(
    level2CipherPacket = level2CipherPacket,
    requestId = requestId,
    ackTopic = topic
  )
}

fun TransportPacket.DataMqtt.toInboundGatewayPacket(): InboundGatewayPacket.Data {
  return InboundGatewayPacket.Data(
    level2CipherPacket = level2CipherPacket,
    requestId = requestId,
  )
}

fun TransportPacket.Ack.toInboundGatewayPacket(): InboundGatewayPacket.Ack {
  return InboundGatewayPacket.Ack(
    requestId = requestId,
    success = true
  )
}

fun TransportPacket.DataTor.toInboundGatewayPacket(): InboundGatewayPacket.Data {
  return InboundGatewayPacket.Data(
    level2CipherPacket = level2CipherPacket,
    requestId = requestId,
  )
}
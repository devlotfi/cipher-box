package com.github.devlotfi.cipherbox.gateway.services

import android.util.Log
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

/**
 * Keeps an MQTT 5 connection over TLS to one broker alive, using the HiveMQ client's own
 * machinery: automatic reconnect with exponential backoff (this also retries a failed first
 * connect), lifecycle listeners and the client state. No thread of our own.
 *
 * The topic is subscribed once, after the first successful connect. When the connection comes
 * back the client restores the subscription itself if the broker lost the session, and the
 * global publishes callback registered before connecting keeps receiving messages.
 *
 * start() and stop() do not block, but like the other gateway classes they should be called from
 * one thread at a time. send() can be called from any thread.
 */
class GatewayMqtt(
  private val onConnectedChanged: (Boolean) -> Unit,
  private val onMessage: (topic: String, payload: ByteArray) -> Unit,
) {
  /** Everything needed to connect. username and password are optional. */
  data class Config(
    val host: String,
    val port: Int,
    val topic: String,
    val username: String? = null,
    val password: String? = null,
  ) {
    // Never print the password
    override fun toString() = "Config(host=$host, port=$port, topic=$topic, username=$username)"
  }

  @Volatile
  private var session: Session? = null

  val currentConfig: Config? get() = session?.config

  /** True from start() until stop(), whatever state the connection is in. */
  val isAlive: Boolean get() = session != null

  /**
   * The HiveMQ client's own state: DISCONNECTED, CONNECTING, CONNECTED,
   * DISCONNECTED_RECONNECT (waiting for the next attempt) or CONNECTING_RECONNECT.
   * Null when not started.
   */
  val state: MqttClientState? get() = session?.client?.state

  /** Connected to the broker and the topic subscription has been confirmed. */
  val isConnected: Boolean
    get() = session?.let { it.subscribed && it.client.state.isConnected } == true

  fun start(config: Config) {
    stop()
    session = Session(config).also { it.open() }
  }

  fun stop() {
    val s = session ?: return
    session = null
    s.close()
    onConnectedChanged(false)
  }

  /**
   * Publishes with QoS 1 and waits for the broker's PUBACK (at most SEND_TIMEOUT_S seconds).
   * Returns false right away if the client is not connected: the HiveMQ client would otherwise
   * queue the message and deliver it after the reconnect, which is not wanted for a gateway.
   * Also false if the topic is invalid, the broker refused the message or no PUBACK came in time.
   */
  fun send(topic: String, payload: ByteArray): Boolean {
    val c = session?.client ?: return false // read the CURRENT client on every call
    if (!c.state.isConnected) return false
    return try {
      val result = c.publishWith()
        .topic(topic)
        .payload(payload)
        .qos(MqttQos.AT_LEAST_ONCE)
        .send()
        .get(SEND_TIMEOUT_S, TimeUnit.SECONDS)
      !result.error.isPresent
    } catch (e: Exception) {
      Log.w(TAG, "Send to $topic failed: ${e.message}")
      false
    }
  }

  fun send(topic: String, payload: String): Boolean = send(topic, payload.toByteArray())

  /**
   * One client and its flags. A new Session is created on every start(), so the listeners of an
   * old, stopped client can never touch the state of a new one.
   */
  private inner class Session(val config: Config) {
    @Volatile
    private var closed = false

    @Volatile
    var subscribed = false
      private set

    @Volatile
    private var reported = false

    // Only used to retry a refused subscribe; its thread starts on first use
    private val retry = Executors.newSingleThreadScheduledExecutor()

    val client: Mqtt5AsyncClient = build()

    private fun build(): Mqtt5AsyncClient {
      var builder: Mqtt5ClientBuilder = Mqtt5Client.builder()
        // Kept by the client across reconnects
        .identifier("cipherbox-gateway-${UUID.randomUUID().toString().take(8)}")
        .serverHost(config.host)
        .serverPort(config.port)
        .sslWithDefaultConfig() // TLS with the system trust store and hostname verification
        .automaticReconnect()
        .initialDelay(1, TimeUnit.SECONDS)
        .maxDelay(RECONNECT_MAX_DELAY_S, TimeUnit.SECONDS)
        .applyAutomaticReconnect()
        .addConnectedListener { onConnected() }
        .addDisconnectedListener { context -> onDisconnected(context) }

      if (config.username != null) {
        val auth = Mqtt5SimpleAuth.builder()
          .username(config.username)
          .also { b -> config.password?.let { b.password(it.toByteArray()) } }
          .build()
        builder = builder.simpleAuth(auth)
      }

      return builder.buildAsync()
    }

    fun open() {
      // Registered BEFORE connecting and subscribing, so no message can be missed. It stays
      // registered for the life of the client, across reconnects.
      client.publishes(MqttGlobalPublishFilter.ALL) { publish ->
        try {
          onMessage(publish.topic.toString(), publish.payloadAsBytes)
        } catch (e: Exception) {
          Log.e(TAG, "onMessage failed", e)
        }
      }

      // The result is ignored on purpose: the listeners below follow every connect, failed
      // attempt and reconnect, and automatic reconnect keeps retrying a failed first connect.
      client.connectWith()
        .keepAlive(KEEP_ALIVE_S)
        .send()
    }

    fun close() {
      closed = true
      retry.shutdownNow()
      // Source USER: the client does not reconnect after this
      client.disconnect().whenComplete { _, _ -> }
    }

    // Called for the first connect AND for every reconnect
    private fun onConnected() {
      if (closed) {
        // stop() came while a reconnect was already scheduled
        client.disconnect()
        return
      }
      Log.i(TAG, "Connected to ${config.host}:${config.port}")

      if (subscribed) {
        // A reconnect: the client restores the subscription itself if the session was lost
        report(true)
      } else {
        subscribe()
      }
    }

    private fun onDisconnected(context: MqttClientDisconnectedContext) {
      Log.w(TAG, "Disconnected (${context.source}): ${context.cause.message}")
      report(false)
      // Stop the automatic reconnect for a client that was stopped
      if (closed) context.reconnector.reconnect(false)
    }

    private fun subscribe() {
      client.subscribeWith()
        .topicFilter(config.topic)
        .qos(MqttQos.AT_LEAST_ONCE)
        .send()
        .whenComplete { subAck, error ->
          if (closed) return@whenComplete

          if (error == null && subAck.reasonCodes.none { it.isError }) {
            subscribed = true
            report(client.state.isConnected)
          } else {
            Log.e(
              TAG,
              "Subscribe to ${config.topic} failed: ${error?.message ?: subAck.reasonCodes}"
            )
            // Connection lost during the subscribe: the next connect subscribes again.
            // Connection still up (broker refused): try again later.
            if (client.state.isConnected) scheduleSubscribeRetry()
          }
        }
    }

    private fun scheduleSubscribeRetry() {
      try {
        retry.schedule({
          if (!closed && !subscribed && client.state.isConnected) subscribe()
        }, SUBSCRIBE_RETRY_DELAY_S, TimeUnit.SECONDS)
      } catch (_: RejectedExecutionException) {
        // closed in the meantime
      }
    }

    private fun report(connected: Boolean) {
      if (closed || reported == connected) return
      reported = connected
      onConnectedChanged(connected)
    }
  }

  private companion object {
    const val TAG = "GatewayMqtt"
    const val KEEP_ALIVE_S = 30
    const val RECONNECT_MAX_DELAY_S = 30L
    const val SUBSCRIBE_RETRY_DELAY_S = 5L
    const val SEND_TIMEOUT_S = 10L
  }
}
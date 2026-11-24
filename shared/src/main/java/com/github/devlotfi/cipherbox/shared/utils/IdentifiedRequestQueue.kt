package com.github.devlotfi.cipherbox.shared.utils

import com.github.devlotfi.cipherbox.shared.transport.packet.IdentifiedRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class IdentifiedRequestQueue<T : IdentifiedRequest> {
  private companion object {
    const val RECENT_IDS_LIMIT = 10
  }

  private val mutex = Mutex()
  private val queue = ArrayDeque<T>()

  // Wake-up signal for consumers. CONFLATED: multiple signals collapse into one,
  // consumers always re-check the real queue after waking.
  private val signal = Channel<Unit>(Channel.CONFLATED)

  // Insertion-ordered, so the oldest id is the first element.
  private val recentRequestIds = LinkedHashSet<String>()

  /** @return `true` if enqueued, `false` if its requestId is a recent duplicate. */
  suspend fun enqueue(request: T): Boolean {
    val added = mutex.withLock {
      if (!recentRequestIds.add(request.requestId)) {
        return@withLock false
      }

      if (recentRequestIds.size > RECENT_IDS_LIMIT) {
        val iterator = recentRequestIds.iterator()
        iterator.next()
        iterator.remove()
      }

      queue.addLast(request)
      true
    }

    if (added) signal.trySend(Unit)
    return added
  }

  /** Removes and returns the head only if its requestId equals [requestId], otherwise `null`. */
  suspend fun dequeue(requestId: String): T? {
    val removed = mutex.withLock {
      val head = queue.firstOrNull() ?: return@withLock null
      if (head.requestId == requestId) queue.removeFirst() else null
    }
    if (removed != null && mutex.withLock { queue.isNotEmpty() }) signal.trySend(Unit)
    return removed
  }

  suspend fun dequeue(): T? = mutex.withLock { queue.removeFirstOrNull() }

  /** Suspends until an item is available, then removes and returns the head. */
  suspend fun dequeueAwait(): T {
    while (true) {
      val item = mutex.withLock { queue.removeFirstOrNull() }
      if (item != null) {
        // If more items remain, re-arm the signal for the next consumer/iteration.
        if (mutex.withLock { queue.isNotEmpty() }) signal.trySend(Unit)
        return item
      }
      signal.receive()
    }
  }

  /**
   * Suspends until a new item is enqueued or [timeoutMs] elapses.
   * @return `true` if woken by an enqueue, `false` on timeout.
   * Does not remove anything from the queue.
   */
  suspend fun awaitEnqueue(timeoutMs: Long): Boolean =
    withTimeoutOrNull(timeoutMs.milliseconds) { signal.receive() } != null

  /** Discards any pending wake-up signal, so [awaitEnqueue] only reacts to enqueues from now on. */
  fun drainSignal() {
    signal.tryReceive()
  }

  /** Returns the head (next to be dequeued) without removing it, or `null` if empty. */
  suspend fun peek(): T? = mutex.withLock { queue.firstOrNull() }

  suspend fun size(): Int = mutex.withLock { queue.size }

  suspend fun isEmpty(): Boolean = mutex.withLock { queue.isEmpty() }

  suspend fun clear(clearRecentIds: Boolean = false) = mutex.withLock {
    queue.clear()
    if (clearRecentIds) recentRequestIds.clear()
  }
}
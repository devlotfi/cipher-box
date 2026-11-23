package com.github.devlotfi.cipherbox.shared.utils

import com.github.devlotfi.cipherbox.shared.transport.packet.IdentifiedRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class IdentifiedRequestQueue<T : IdentifiedRequest> {
  private companion object {
    const val RECENT_IDS_LIMIT = 10
  }

  private val mutex = Mutex()
  private val queue = ArrayDeque<T>()

  // Insertion-ordered, so the oldest id is the first element.
  private val recentRequestIds = LinkedHashSet<String>()

  /** @return `true` if enqueued, `false` if its requestId is a recent duplicate. */
  suspend fun enqueue(request: T): Boolean = mutex.withLock {
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

  /** Removes and returns the head only if its requestId equals [requestId], otherwise `null`. */
  suspend fun dequeue(requestId: String): T? = mutex.withLock {
    val head = queue.firstOrNull() ?: return@withLock null
    if (head.requestId == requestId) queue.removeFirst() else null
  }

  suspend fun dequeue(): T? = mutex.withLock {
    val head = queue.firstOrNull() ?: return@withLock null
    return@withLock queue.removeFirst()
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
/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tetherfi.server.proxy.session.tcp

import androidx.annotation.CheckResult
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.joinTo
import io.ktor.utils.io.writeFully

private const val LINE_ENDING = "\r\n"

internal suspend fun talk(input: ByteReadChannel, output: ByteWriteChannel) {
  //    KtorDefaultPool.useInstance { buffer ->
  //      while (isActive) {
  //        val array = buffer.array()
  //        val size = input.readAvailable(array)
  //        if (size < 0) {
  //          break
  //        }
  //
  //        output.writeFully(array, 0, size)
  //      }
  //    }

  // Should be faster than parsing byte buffers raw
  input.joinTo(output, closeOnEnd = true)
}

/** Write a generic error back to the client socket because something has gone wrong */
internal suspend fun writeError(output: ByteWriteChannel) {
  proxyResponse(output, "HTTP/1.1 502 Bad Gateway")
}

/**
 * Respond to the client with a message string
 *
 * Properly line-ended with flushed output
 */
internal suspend fun proxyResponse(output: ByteWriteChannel, response: String) {
  output.apply {
    writeFully(writeMessageAndAwaitMore(response))
    writeFully(LINE_ENDING.encodeToByteArray())
    flush()
  }
}

/**
 * Convert a message string into a byte array
 *
 * Correctly end the line with return and newline
 */
@CheckResult
internal fun writeMessageAndAwaitMore(message: String): ByteArray {
  val msg = if (message.endsWith(LINE_ENDING)) message else "${message}$LINE_ENDING"
  return msg.encodeToByteArray()
}

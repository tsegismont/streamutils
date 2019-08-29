/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.github.tsegismont.streamutils

import io.github.tsegismont.streamutils.kotlin.limit
import io.github.tsegismont.streamutils.kotlin.map
import io.github.tsegismont.streamutils.kotlin.skip
import io.vertx.core.buffer.Buffer
import io.vertx.core.parsetools.RecordParser
import io.vertx.kotlin.core.file.openOptionsOf
import io.vertx.test.core.TestUtils
import io.vertx.test.core.VertxTestBase
import org.junit.Test
import java.nio.file.Files

class KotlinFileTest : VertxTestBase() {

  @Test
  fun testFileTransformation() {
    val source = TestUtils.tmpFile("stream-source")

    val data = (0 until 1000).map { TestUtils.randomAlphaString(it) }
    Files.write(source.toPath(), data)

    vertx.fileSystem().open(source.absolutePath, openOptionsOf(read = true, write = false), onSuccess { asyncFile ->
      val parser = RecordParser.newDelimited("\n", asyncFile)

      val result = parser
        .map(Buffer::toString)
        .map(String::length)
        .skip(50)
        .limit(150)

      val actual = mutableListOf<Int>()
      result
        .exceptionHandler { fail(it) }
        .endHandler {
          assertEquals((50 until 200).toList(), actual)
          testComplete()
        }
        .handler { actual.add(it) }

    })

    await()
  }
}

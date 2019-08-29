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

import io.vertx.core.buffer.Buffer
import io.vertx.core.file.AsyncFile
import io.vertx.core.parsetools.RecordParser
import io.vertx.test.core.TestUtils
import io.vertx.test.core.VertxTestBase
import org.junit.Test

import java.nio.file.Files

class GroovyFileTest extends VertxTestBase {

  @Test
  void testFileTransformation() {
    def source = TestUtils.tmpFile("stream-source")

    def data = (0..<1000).collect { TestUtils.randomAlphaString(it) }
    Files.write(source.toPath(), data)

    vertx.fileSystem().open(source.getAbsolutePath(), [read: true, write: false], onSuccess { AsyncFile asyncFile ->

      def parser = RecordParser.newDelimited("\n", asyncFile)

      def result = parser
        .map { Buffer buffer -> buffer.toString() }
        .map { String line -> line.length() }
        .skip(50)
        .limit(150)

      def actual = []
      result
        .exceptionHandler { fail(it) }
        .endHandler({
          assertEquals(50..<200, actual)
          testComplete()
        })
        .handler { actual.add(it) }

    })

    await()
  }
}

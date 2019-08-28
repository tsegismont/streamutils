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

package io.github.tsegismont.streamutils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.streams.ReadStream;
import io.vertx.test.core.TestUtils;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * @author Thomas Segismont
 */
public class FileTest extends VertxTestBase {

  @Test
  public void testFileTransformation() throws Exception {
    File source = TestUtils.tmpFile("stream-source");
    List<String> data = IntStream.range(0, 1000)
      .mapToObj(size -> TestUtils.randomAlphaString(size))
      .collect(toList());
    Files.write(source.toPath(), data);

    vertx.fileSystem().open(source.getAbsolutePath(), new OpenOptions().setRead(true).setWrite(false), onSuccess(asyncFile -> {
      RecordParser parser = RecordParser.newDelimited("\n", asyncFile);

      ReadStream<String> lines = Streams.map(parser, Buffer::toString);
      ReadStream<Integer> sizes = Streams.map(lines, String::length);
      ReadStream<Integer> skipped = Streams.skip(sizes, 50);
      ReadStream<Integer> result = Streams.limit(skipped, 150);

      ArrayList<Object> expected = IntStream.range(50, 200)
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

      List<Integer> actual = new ArrayList<>();
      result
        .exceptionHandler(this::fail)
        .endHandler(v -> {
          assertEquals(expected, actual);
          testComplete();
        })
        .handler(size -> actual.add(size));

    }));

    await();
  }
}

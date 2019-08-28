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

import io.vertx.core.streams.ReadStream;
import io.vertx.test.core.TestUtils;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakestream.FakeStream;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * @author Thomas Segismont
 */
public class MapTest extends VertxTestBase {

  private List<String> strings;
  private List<Object> sizes;
  private FakeStream<String> fakeStream;
  private ReadStream<Integer> filtered;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    strings = IntStream.range(0, 100)
      .mapToObj(size -> TestUtils.randomAlphaString(size))
      .collect(toList());
    sizes = Collections.synchronizedList(new ArrayList<>());
    fakeStream = new FakeStream<>();
    filtered = Streams.map(fakeStream, String::length);
  }

  @Test
  public void testMap() {
    filtered
      .handler(size -> sizes.add(size))
      .endHandler(v -> {
        assertEquals(strings.stream().map(String::length).collect(toList()), sizes);
        testComplete();
      });
    fakeStream.emit(strings.stream());
    fakeStream.end();
    await();
  }

  @Test
  public void testSourceError() {
    Throwable error = new Exception();
    filtered
      .handler(size -> sizes.add(size))
      .exceptionHandler(throwable -> {
        assertSame(error, throwable);
        assertEquals(strings.stream().map(String::length).collect(toList()), sizes);
        testComplete();
      });
    fakeStream.emit(strings.stream());
    fakeStream.fail(error);
    await();
  }
}

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
public class SkipTest extends VertxTestBase {

  private static final int COUNT = 100;
  private static final int SKIP = COUNT / 2;

  private List<Integer> numbers;
  private List<Object> output;
  private FakeStream<Integer> fakeStream;
  private ReadStream<Integer> skipping;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    numbers = IntStream.range(0, COUNT).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    output = Collections.synchronizedList(new ArrayList<>());
    fakeStream = new FakeStream<>();
    skipping = Streams.skip(fakeStream, SKIP).handler(number -> output.add(number));
  }

  @Test
  public void testSkip() {
    skipping.endHandler(v -> {
      assertEquals(numbers.stream().skip(SKIP).collect(toList()), output);
      testComplete();
    });
    fakeStream.emit(numbers.stream());
    fakeStream.end();
    await();
  }

  @Test
  public void testSkipFetch() {
    skipping.endHandler(v -> {
      assertEquals(numbers.stream().skip(SKIP).collect(toList()), output);
      testComplete();
    }).pause();
    fakeStream.emit(numbers.stream());
    int batchSize = 10;
    for (int i = 0; i * batchSize < COUNT - SKIP; i++) {
      skipping.fetch(batchSize);
      int maxSize = batchSize * (i + 1);
      assertWaitUntil(() -> output.equals(numbers.stream().skip(SKIP).limit(maxSize).collect(toList())));
    }
    fakeStream.resume().end();
    await();
  }

  @Test
  public void testSourceError() {
    Throwable error = new Exception();
    skipping.exceptionHandler(throwable -> {
      assertSame(error, throwable);
      assertEquals(numbers.stream().skip(SKIP).collect(toList()), output);
      testComplete();
    });
    fakeStream.emit(numbers.stream());
    fakeStream.fail(error);
    await();
  }
}

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

package io.github.tsegismont.streamutils.groovy;


import io.github.tsegismont.streamutils.Streams;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.streams.ReadStream;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Thomas Segismont
 */
public class StreamsExtensionModule {

  /**
   * @see Streams#map(ReadStream, Function)
   */
  public static <T, R> ReadStream<R> map(ReadStream<T> self, Function<T, R> mapping) {
    return Streams.map(self, mapping);
  }

  /**
   * @see Streams#filter(ReadStream, Predicate)
   */
  public static <T> ReadStream<T> filter(ReadStream<T> self, Predicate<T> predicate) {
    return Streams.filter(self, predicate);
  }

  /**
   * @see Streams#filter(ReadStream, Predicate, Vertx)
   */
  public static <T> ReadStream<T> filter(ReadStream<T> self, Predicate<T> predicate, Vertx vertx) {
    return Streams.filter(self, predicate, vertx);
  }

  /**
   * @see Streams#filter(ReadStream, Predicate, Context)
   */
  public static <T> ReadStream<T> filter(ReadStream<T> self, Predicate<T> predicate, Context context) {
    return Streams.filter(self, predicate, context);
  }

  /**
   * @see Streams#skip(ReadStream, long)
   */
  public static <T> ReadStream<T> skip(ReadStream<T> self, long skip) {
    return Streams.skip(self, skip);
  }

  /**
   * @see Streams#limit(ReadStream, long)
   */
  public static <T> ReadStream<T> limit(ReadStream<T> self, long limit) {
    return Streams.limit(self, limit);
  }
}

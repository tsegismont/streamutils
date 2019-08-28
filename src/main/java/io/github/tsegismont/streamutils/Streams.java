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

import io.github.tsegismont.streamutils.impl.FilteringStream;
import io.github.tsegismont.streamutils.impl.LimitingStream;
import io.github.tsegismont.streamutils.impl.MappingStream;
import io.github.tsegismont.streamutils.impl.SkippingStream;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.streams.ReadStream;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A set of transformations for {@link ReadStream} objects.
 *
 * @author Thomas Segismont
 */
public class Streams {

  private Streams() {
    // Utility
  }

  /**
   * Returns a {@link ReadStream} that emit the result of the {@code mapping} function applied to every {@code source} item.
   */
  public static <T, R> ReadStream<R> map(ReadStream<T> source, Function<T, R> mapping) {
    return new MappingStream<>(source, mapping);
  }

  /**
   * Returns a {@link ReadStream} that emit the {@code source} items that match the given {@code predicate}.
   * <p>
   * This method <em>MUST</em> be invoked on a Vert.x thread.
   */
  public static <T> ReadStream<T> filter(ReadStream<T> source, Predicate<T> predicate) {
    return new FilteringStream<>(source, predicate);
  }

  /**
   * Returns a {@link ReadStream} that emit the {@code source} items that match the given {@code predicate}.
   * <p>
   * If this method is invoked on a Vert.x thread, the items are emitted on this thread.
   * Otherwise, the given {@code vertx} instance will provide a {@link Context} on which items are emitted.
   *
   * @param vertx the Vert.x instance that will provide the {@link Context} on which items are emitted
   */
  public static <T> ReadStream<T> filter(ReadStream<T> source, Predicate<T> predicate, Vertx vertx) {
    return new FilteringStream<>(source, predicate, vertx);
  }

  /**
   * Returns a {@link ReadStream} that emit the {@code source} items that match the given {@code predicate}.
   * <p>
   * This method may be invoked on any thread.
   *
   * @param context the {@link Context} on which items are emitted
   */
  public static <T> ReadStream<T> filter(ReadStream<T> source, Predicate<T> predicate, Context context) {
    return new FilteringStream<>(source, predicate, context);
  }

  /**
   * Returns a {@link ReadStream} that skips the {@code skip} first items of the {@code source} stream.
   */
  public static <T> ReadStream<T> skip(ReadStream<T> source, long skip) {
    return new SkippingStream<>(source, skip);
  }

  /**
   * Returns a {@link ReadStream} that emits no more than {@code limit} items of the {@code source} stream.
   */
  public static <T> ReadStream<T> limit(ReadStream<T> source, long limit) {
    return new LimitingStream<>(source, limit);
  }
}

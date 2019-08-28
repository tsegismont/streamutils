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

package io.github.tsegismont.streamutils.impl;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Thomas Segismont
 */
public final class MappingStream<T, R> implements ReadStream<R> {

  private final ReadStream<T> source;
  private final Function<T, R> mapping;

  public MappingStream(ReadStream<T> source, Function<T, R> mapping) {
    Objects.requireNonNull(source, "source stream cannot be null");
    Objects.requireNonNull(mapping, "Mapping function cannot be null");
    this.source = source;
    this.mapping = mapping;
  }

  @Override
  public ReadStream<R> exceptionHandler(Handler<Throwable> handler) {
    source.exceptionHandler(handler);
    return this;
  }

  @Override
  public ReadStream<R> handler(Handler<R> handler) {
    if (handler == null) {
      source.handler(null);
    } else {
      source.handler(event -> handler.handle(mapping.apply(event)));
    }
    return this;
  }

  @Override
  public ReadStream<R> pause() {
    source.pause();
    return this;
  }

  @Override
  public ReadStream<R> resume() {
    return fetch(Long.MAX_VALUE);
  }

  @Override
  public ReadStream<R> fetch(long amount) {
    source.fetch(amount);
    return this;
  }

  @Override
  public ReadStream<R> endHandler(Handler<Void> endHandler) {
    source.endHandler(endHandler);
    return this;
  }
}

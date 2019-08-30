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
import io.vertx.core.impl.Arguments;
import io.vertx.core.streams.ReadStream;

import java.util.Objects;

/**
 * @author Thomas Segismont
 */
public final class SkippingStream<T> implements ReadStream<T> {

  private final ReadStream<T> source;
  private final long skip;

  private long skipped;

  public SkippingStream(ReadStream<T> source, long skip) {
    Objects.requireNonNull(source, "Source cannot be null");
    Arguments.require(skip >= 0, "Skip amount must be positive");
    this.source = source;
    this.skip = skip;
  }

  @Override
  public ReadStream<T> exceptionHandler(Handler<Throwable> handler) {
    source.exceptionHandler(handler);
    return this;
  }

  @Override
  public ReadStream<T> handler(Handler<T> handler) {
    if (handler == null) {
      source.handler(null);
      return this;
    }
    source.handler(item -> {
      boolean emit;
      synchronized (this) {
        if (skipped < skip) {
          emit = false;
          skipped++;
        } else {
          emit = true;
        }
      }
      if (emit) {
        handler.handle(item);
      }
    });
    return this;
  }

  @Override
  public ReadStream<T> pause() {
    source.pause();
    return this;
  }

  @Override
  public ReadStream<T> resume() {
    return fetch(Long.MAX_VALUE);
  }

  @Override
  public ReadStream<T> fetch(long l) {
    long value;
    synchronized (this) {
      if (skipped < skip) {
        if (l < Long.MAX_VALUE - skip + skipped) {
          value = l + skip - skipped;
        } else {
          value = Long.MAX_VALUE;
        }
      } else {
        value = l;
      }
    }
    source.fetch(value);
    return this;
  }

  @Override
  public ReadStream<T> endHandler(Handler<Void> handler) {
    source.endHandler(handler);
    return this;
  }
}

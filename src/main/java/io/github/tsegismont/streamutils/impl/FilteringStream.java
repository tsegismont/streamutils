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

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.impl.InboundBuffer;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Thomas Segismont
 */
public final class FilteringStream<T> implements ReadStream<T> {

  private final ReadStream<T> source;
  private final Predicate<T> predicate;
  private final Context context;
  private final InboundBuffer<T> queue;

  private boolean ended;
  private Throwable error;
  private boolean stopped;
  private int inFlight;
  private Handler<T> handler;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;

  public FilteringStream(ReadStream<T> source, Predicate<T> predicate) {
    this(source, predicate, Vertx.currentContext());
  }

  public FilteringStream(ReadStream<T> source, Predicate<T> predicate, Vertx vertx) {
    this(source, predicate, vertx.getOrCreateContext());
  }

  public FilteringStream(ReadStream<T> source, Predicate<T> predicate, Context context) {
    Objects.requireNonNull(source, "Source cannot be null");
    Objects.requireNonNull(predicate, "Filtering function cannot be null");
    Objects.requireNonNull(context, "Context cannot be null");
    this.source = source;
    this.predicate = predicate;
    this.context = context;
    queue = new InboundBuffer<T>(context)
      .exceptionHandler(throwable -> notifyTerminalHandler(getExceptionHandler(), throwable))
      .drainHandler(v -> source.resume());
  }

  @Override
  public ReadStream<T> exceptionHandler(Handler<Throwable> handler) {
    synchronized (queue) {
      if (!stopped) {
        exceptionHandler = handler;
      }
    }
    return this;
  }

  private synchronized Handler<Throwable> getExceptionHandler() {
    return exceptionHandler;
  }

  @Override
  public ReadStream<T> handler(Handler<T> handler) {
    synchronized (queue) {
      if (stopped) {
        return this;
      }
    }
    if (handler == null) {
      notifyTerminalHandler(getEndHandler(), null);
      return this;
    }
    this.handler = handler;
    queue.handler(item -> handleOnContext(this::emit, item));
    source
      .handler(item -> handleOnContext(this::filter, item))
      .exceptionHandler(throwable -> handleOnContext(this::error, throwable))
      .endHandler(v -> handleOnContext(this::exhausted, null));
    return this;
  }

  private void error(Throwable t) {
    boolean terminate;
    synchronized (queue) {
      this.error = t;
      terminate = !stopped && inFlight == 0;
    }
    if (terminate) {
      notifyTerminalHandler(getExceptionHandler(), t);
    }
  }

  private void exhausted(T item) {
    boolean terminate;
    synchronized (queue) {
      ended = true;
      terminate = !stopped && inFlight == 0;
    }
    if (terminate) {
      notifyTerminalHandler(getEndHandler(), null);
    }
  }

  private void filter(T item) {
    if (predicate.test(item)) {
      synchronized (queue) {
        inFlight++;
      }
      if (!queue.write(item)) {
        source.pause();
      }
    }
  }

  private void emit(T item) {
    int terminate = 0;
    Handler<T> h;
    synchronized (queue) {
      inFlight--;
      h = stopped ? null : handler;
      terminate = (stopped || inFlight > 0) ? 0 : (error != null ? 2 : (ended ? 1 : 0));
    }
    if (h != null) {
      h.handle(item);
    }
    if (terminate == 1) {
      notifyTerminalHandler(getEndHandler(), null);
    } else if (terminate == 2) {
      notifyTerminalHandler(getExceptionHandler(), error);
    }
  }

  private <U> void handleOnContext(Handler<U> handler, U value) {
    if (context == Vertx.currentContext()) {
      handler.handle(value);
    } else {
      context.runOnContext(v -> handler.handle(value));
    }
  }

  @Override
  public ReadStream<T> pause() {
    synchronized (queue) {
      if (!stopped) {
        queue.pause();
      }
    }
    return this;
  }

  @Override
  public ReadStream<T> resume() {
    return fetch(Long.MAX_VALUE);
  }

  @Override
  public ReadStream<T> fetch(long amount) {
    synchronized (queue) {
      if (!stopped) {
        queue.fetch(amount);
      }
    }
    return this;
  }

  @Override
  public ReadStream<T> endHandler(Handler<Void> endHandler) {
    synchronized (queue) {
      if (!stopped) {
        this.endHandler = endHandler;
      }
    }
    return this;
  }

  private synchronized Handler<Void> getEndHandler() {
    return endHandler;
  }

  private <V> void notifyTerminalHandler(Handler<V> handler, V value) {
    Handler<V> h;
    synchronized (queue) {
      if (!stopped) {
        stopped = true;
        queue.handler(null).drainHandler(null);
        h = handler;
      } else {
        h = null;
      }
    }
    if (h != null) {
      if (context != Vertx.currentContext()) {
        context.runOnContext(v -> h.handle(value));
      } else {
        h.handle(value);
      }
    }
  }
}

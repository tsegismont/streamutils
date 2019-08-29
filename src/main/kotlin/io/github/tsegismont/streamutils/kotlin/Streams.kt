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

package io.github.tsegismont.streamutils.kotlin

import io.github.tsegismont.streamutils.Streams
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.streams.ReadStream

/**
 * @see Streams.map
 */
fun <T, R> ReadStream<T>.map(mapping: (input: T) -> R): ReadStream<R> = Streams.map(this, mapping)

/**
 * @see Streams.filter
 */
fun <T> ReadStream<T>.filter(predicate: (input: T) -> Boolean): ReadStream<T> = Streams.filter(this, predicate)

/**
 * Like [filter].
 *
 * If this method is invoked on a Vert.x thread, the items are emitted on this thread.
 * Otherwise, the given `vertx` instance will provide a [Context] on which items are emitted.
 */
fun <T> ReadStream<T>.filter(predicate: (input: T) -> Boolean, vertx: Vertx): ReadStream<T> = Streams.filter(this, predicate, vertx)

/**
 * Like [filter] but uses the given `context` to emit items.
 */
fun <T> ReadStream<T>.filter(predicate: (input: T) -> Boolean, context: Context): ReadStream<T> = Streams.filter(this, predicate, context)

/**
 * @see Streams.skip
 */
fun <T> ReadStream<T>.skip(skip: Long): ReadStream<T> = Streams.skip(this, skip)

/**
 * @see Streams.limit
 */
fun <T> ReadStream<T>.limit(limit: Long): ReadStream<T> = Streams.limit(this, limit)

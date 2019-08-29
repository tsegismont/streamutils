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

fun <T, R> ReadStream<T>.map(mapping: (input: T) -> R): ReadStream<R> = Streams.map(this, mapping)

fun <T> ReadStream<T>.filter(predicate: (input: T) -> Boolean): ReadStream<T> = Streams.filter(this, predicate)

fun <T> ReadStream<T>.filter(predicate: (input: T) -> Boolean, vertx: Vertx): ReadStream<T> = Streams.filter(this, predicate, vertx)

fun <T> ReadStream<T>.filter(predicate: (input: T) -> Boolean, context: Context): ReadStream<T> = Streams.filter(this, predicate, context)

fun <T> ReadStream<T>.skip(skip: Long): ReadStream<T> = Streams.skip(this, skip)

fun <T> ReadStream<T>.limit(limit: Long): ReadStream<T> = Streams.limit(this, limit)

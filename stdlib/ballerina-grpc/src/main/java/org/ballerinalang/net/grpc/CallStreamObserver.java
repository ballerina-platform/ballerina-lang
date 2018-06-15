/*
 * Copyright 2016, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.grpc;

/**
 * A refinement of StreamObserver provided by the GRPC runtime to the application that allows for
 * more complex interactions with call behavior.
 *
 * <p>In any call there are logically two {@link StreamObserver} implementations:
 * <ul>
 *   <li>'inbound' - which the GRPC runtime calls when it receives messages from the
 *   remote peer. This is implemented by the application.
 *   </li>
 *   <li>'outbound' - which the GRPC runtime provides to the application which it uses to
 *   send messages to the remote peer.
 *   </li>
 * </ul>
 *
 * <p>Implementations of this class represent the 'outbound' message stream.
 *
 * <p>Like {@code StreamObserver}, implementations are not required to be thread-safe; if multiple
 * threads will be writing to an instance concurrently, the application must synchronize its calls.
 *
 * @param <V> Message Type
 */
public abstract class CallStreamObserver<V> implements StreamObserver<V> {

  /**
   * If {@code true}, indicates that the observer is capable of sending additional messages
   * without requiring excessive buffering internally. This value is just a suggestion and the
   * application is free to ignore it, however doing so may result in excessive buffering within the
   * observer.
   */
  public abstract boolean isReady();

  /**
   * Sets message compression for subsequent calls to {@link #onNext}.
   *
   * @param enable whether to enable compression.
   */
  public abstract void setMessageCompression(boolean enable);
}

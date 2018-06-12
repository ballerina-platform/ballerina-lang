/*
 * Copyright 2015, gRPC Authors All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Encloses classes related to the compression and decompression of messages.
 */
@ThreadSafe
public final class DecompressorRegistry {
  private static final DecompressorRegistry DEFAULT_INSTANCE = new DecompressorRegistry(
      new Codec.Gzip(),
      Codec.Identity.NONE);

  /**
   * Returns the default instance used by gRPC when the registry is not specified.
   * Currently the registry just contains support for gzip.
   */
  public static DecompressorRegistry getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  /**
   * Returns a new instance with no registered decompressors.
   */
  public static DecompressorRegistry newEmptyInstance() {
    return new DecompressorRegistry();
  }

  private final ConcurrentMap<String, Decompressor> decompressors;

  @VisibleForTesting
  private DecompressorRegistry(Decompressor... cs) {
    decompressors = new ConcurrentHashMap<String, Decompressor>();
    for (Decompressor c : cs) {
      decompressors.put(c.getMessageEncoding(), c);
    }
  }

  @Nullable
  public Decompressor lookupDecompressor(String compressorName) {
    return decompressors.get(compressorName);
  }

  /**
   * Registers a compressor for both decompression and message encoding negotiation.
   *
   * @param c The compressor to register
   */
  public void register(Decompressor c) {
    String encoding = c.getMessageEncoding();
    checkArgument(!encoding.contains(","), "Comma is currently not allowed in message encoding");
    decompressors.put(encoding, c);
  }

  public Set<String> getAdvertisedMessageEncodings() {
    Set<String> advertisedDecompressors = new HashSet<String>(decompressors.size());
    for (Map.Entry<String, Decompressor> entry : decompressors.entrySet()) {
      if (Codec.Identity.NONE.getMessageEncoding().equals(entry.getKey())) {
        continue;
      }
        advertisedDecompressors.add(entry.getKey());
      }
    return Collections.unmodifiableSet(advertisedDecompressors);
  }
}

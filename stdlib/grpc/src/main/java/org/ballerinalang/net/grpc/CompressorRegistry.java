/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Compressor Registry to hold all decompressor instances.
 *
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * @since 0.980.0
 */
public final class CompressorRegistry {
  private static final CompressorRegistry DEFAULT_INSTANCE = new CompressorRegistry(
      new Codec.Gzip(),
      Codec.Identity.NONE);
  private final ConcurrentMap<String, Compressor> compressors;

  /**
   * Returns the default instance.
   *
   * @return default instance
   */
  public static CompressorRegistry getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private CompressorRegistry(Compressor... cs) {
    compressors = new ConcurrentHashMap<>();
    for (Compressor c : cs) {
      compressors.put(c.getMessageEncoding(), c);
    }
  }

  /**
   * Returns compressor instance for the given name.
   *
   * @param compressorName compressor name
   * @return compressor instance if exists, else null.
   */
  public Compressor lookupCompressor(String compressorName) {
    return compressors.get(compressorName);
  }

  /**
   * Registers a compressor for both decompression and message encoding negotiation.
   *
   * @param c The compressor to register
   */
  public void register(Compressor c) {
    String encoding = c.getMessageEncoding();
    if (encoding.contains(",")) {
      throw new IllegalArgumentException("Comma is currently not allowed in message encoding");
    }
    compressors.put(encoding, c);
  }
}

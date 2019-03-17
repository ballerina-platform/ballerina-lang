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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Decompressor Registry to hold all decompressor instances.
 *
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * @since 0.980.0
 */
public final class DecompressorRegistry {

    private static final DecompressorRegistry DEFAULT_INSTANCE = new DecompressorRegistry(
            new Codec.Gzip(),
            Codec.Identity.NONE);
    private final ConcurrentMap<String, Decompressor> decompressors;
    private final Set<String> advertisedDecompressors;

    /**
     * Returns the default instance.
     *
     * @return default instance
     */
    public static DecompressorRegistry getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    private DecompressorRegistry(Decompressor... cs) {
        decompressors = new ConcurrentHashMap<>();
        advertisedDecompressors = new HashSet<>();
        for (Decompressor c : cs) {
            decompressors.put(c.getMessageEncoding(), c);
            if (!Codec.Identity.NONE.getMessageEncoding().equals(c.getMessageEncoding())) {
                advertisedDecompressors.add(c.getMessageEncoding());
            }
        }
    }

    /**
     * Returns decompressor instance for the given name.
     *
     * @param compressorName compressor name
     * @return decompressor instance if exists, else null.
     */
    public Decompressor lookupDecompressor(String compressorName) {
        return decompressors.get(compressorName);
    }

    /**
     * Registers a compressor for both decompression and message encoding negotiation.
     *
     * @param decompressor The compressor to register
     */
    public void register(Decompressor decompressor) {
        String encoding = decompressor.getMessageEncoding();
        if (encoding.contains(",")) {
            throw new IllegalArgumentException("Comma is currently not allowed in message encoding");
        }
        decompressors.put(encoding, decompressor);
        advertisedDecompressors.add(encoding);
    }

    public Set<String> getAdvertisedMessageEncodings() {
        return advertisedDecompressors;
    }
}

/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encloses classes related to the compression and decompression of messages.
 *
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * @since 0.980.0
 */
public interface Codec extends Compressor, Decompressor {

    String GZIP = "gzip";
    String IDENTITY = "identity";

    /**
     * A gzip compressor and decompressor.
     */
    final class Gzip implements Codec {

        @Override
        public String getMessageEncoding() {
            return GZIP;
        }

        @Override
        public OutputStream compress(OutputStream os) throws IOException {
            return new GZIPOutputStream(os);
        }

        @Override
        public InputStream decompress(InputStream is) throws IOException {
            return new GZIPInputStream(is);
        }
    }

    /**
     * The "identity", or "none" codec.
     */
    final class Identity implements Codec {

        public static final Codec NONE = new Identity();

        @Override
        public InputStream decompress(InputStream is) {
            return is;
        }

        @Override
        public String getMessageEncoding() {
            return IDENTITY;
        }

        @Override
        public OutputStream compress(OutputStream os) {
            return os;
        }

        private Identity() {
        }
    }
}

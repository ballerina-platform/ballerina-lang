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
package org.ballerinalang.test.net.grpc;

import org.ballerinalang.net.grpc.Codec;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Test class for Encoding.
 */
public class CompressorCodecTest {

    @Test(description = "Test case for gzip encoding")
    public void testGzipEncoding() throws IOException {
        Codec.Gzip gzip = new Codec.Gzip();
        Assert.assertEquals(gzip.getMessageEncoding(), Codec.GZIP);
        byte[] dataToCompress = "This is the test data.".getBytes(StandardCharsets.ISO_8859_1);
        byte[] compressedData;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(dataToCompress.length);
             GZIPOutputStream gzipStream = (GZIPOutputStream) gzip.compress(byteStream)) {
            gzipStream.write(dataToCompress);
            gzipStream.close();
            compressedData = byteStream.toByteArray();
        }

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPInputStream gzipStream = (GZIPInputStream) gzip.decompress(byteStream)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            Assert.assertEquals(bos.toByteArray(), dataToCompress);
        }
    }

    @Test(description = "Test case for identity encoding")
    public void testIdentityEncoding() {
        Codec.Identity identity = (Codec.Identity) Codec.Identity.NONE;
        Assert.assertEquals(identity.getMessageEncoding(), Codec.IDENTITY);
        InputStream is = new ByteArrayInputStream(new byte[0]);
        Assert.assertEquals(identity.decompress(is), is);
        OutputStream os = new ByteArrayOutputStream();
        Assert.assertEquals(identity.compress(os), os);
    }
}

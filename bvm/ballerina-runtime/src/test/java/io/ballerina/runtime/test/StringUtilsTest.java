/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.test;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Test cases for {@link StringUtils#getStringFromInputStream(InputStream)} and
 * {@link StringUtils#getStringFromInputStream(InputStream, String)}.
 */
public class StringUtilsTest {

    // Contains 1-byte, 2-byte, 3-byte and 4-byte (non-BMP) UTF-8 characters.
    private static final String MIXED_TEXT = "aé€🛸";

    @Test
    public void testReadUtf8WithoutCharset() {
        String text = "héllo wörld 🛸";
        BString result = StringUtils.getStringFromInputStream(toStream(text, StandardCharsets.UTF_8));
        Assert.assertEquals(result.getValue(), text);
        // The flying saucer is one code point, so the Ballerina length is 13, not 14.
        Assert.assertEquals(result.length(), 13);
    }

    @Test
    public void testReadEmptyStreamWithoutCharset() {
        BString result = StringUtils.getStringFromInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertEquals(result.getValue(), "");
    }

    @Test
    public void testReadLargeStreamWithoutCharset() {
        // Larger than common buffer sizes, so multi-byte characters cross buffer boundaries.
        String text = MIXED_TEXT.repeat(50_000);
        BString result = StringUtils.getStringFromInputStream(toStream(text, StandardCharsets.UTF_8));
        Assert.assertEquals(result.getValue(), text);
        Assert.assertEquals(result.length(), 4 * 50_000);
    }

    @Test
    public void testMalformedBytesAreReplacedWithoutCharset() {
        byte[] bytes = {'a', (byte) 0xFF, 'b'};
        BString result = StringUtils.getStringFromInputStream(new ByteArrayInputStream(bytes));
        Assert.assertEquals(result.getValue(), "a�b");
    }

    @Test
    public void testStreamIsClosedWithoutCharset() {
        CloseTrackingStream stream = new CloseTrackingStream("abc".getBytes(StandardCharsets.UTF_8));
        StringUtils.getStringFromInputStream(stream);
        Assert.assertTrue(stream.closed);
    }

    @Test
    public void testReadErrorWithoutCharset() {
        try {
            StringUtils.getStringFromInputStream(new FailingInputStream());
            Assert.fail("Expected an error when the stream fails");
        } catch (BError e) {
            Assert.assertEquals(e.getErrorMessage().getValue(), "Error occurred when reading input stream");
        }
    }

    @DataProvider(name = "charsetTexts")
    public Object[][] charsetTexts() {
        return new Object[][]{
                {"UTF-8", MIXED_TEXT},
                {"UTF-16", MIXED_TEXT},
                {"UTF-16LE", MIXED_TEXT},
                {"ISO-8859-1", "Grüße aus Köln"},
                {"US-ASCII", "plain text"}
        };
    }

    @Test(dataProvider = "charsetTexts")
    public void testReadWithCharset(String charset, String text) {
        BString result = StringUtils.getStringFromInputStream(toStream(text, Charset.forName(charset)), charset);
        Assert.assertEquals(result.getValue(), text);
    }

    @Test
    public void testReadLargeStreamWithCharset() {
        String text = MIXED_TEXT.repeat(50_000);
        BString result = StringUtils.getStringFromInputStream(toStream(text, StandardCharsets.UTF_16), "UTF-16");
        Assert.assertEquals(result.getValue(), text);
    }

    @Test
    public void testReadEmptyStreamWithCharset() {
        BString result = StringUtils.getStringFromInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8");
        Assert.assertEquals(result.getValue(), "");
    }

    @Test
    public void testStreamIsClosedWithCharset() {
        CloseTrackingStream stream = new CloseTrackingStream("abc".getBytes(StandardCharsets.UTF_8));
        StringUtils.getStringFromInputStream(stream, "UTF-8");
        Assert.assertTrue(stream.closed);
    }

    @Test
    public void testReadErrorWithCharset() {
        try {
            StringUtils.getStringFromInputStream(new FailingInputStream(), "UTF-8");
            Assert.fail("Expected an error when the stream fails");
        } catch (BError e) {
            Assert.assertTrue(e.getErrorMessage().getValue()
                    .startsWith("Error occurred when reading input stream with the charset"));
        }
    }

    @Test(expectedExceptions = UnsupportedCharsetException.class)
    public void testUnsupportedCharset() {
        StringUtils.getStringFromInputStream(new ByteArrayInputStream(new byte[0]), "NOT-A-CHARSET");
    }

    private static InputStream toStream(String text, Charset charset) {
        return new ByteArrayInputStream(text.getBytes(charset));
    }

    private static final class CloseTrackingStream extends ByteArrayInputStream {

        private boolean closed = false;

        CloseTrackingStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    private static final class FailingInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            throw new IOException("read failed");
        }
    }
}

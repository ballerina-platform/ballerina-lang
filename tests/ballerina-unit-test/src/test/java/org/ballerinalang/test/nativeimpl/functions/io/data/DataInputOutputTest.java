/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.test.nativeimpl.functions.io.data;

import org.apache.commons.codec.CharEncoding;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.channels.base.Representation;
import org.ballerinalang.stdlib.io.channels.base.data.LongResult;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;

import static org.ballerinalang.stdlib.io.channels.base.Representation.BIT_16;
import static org.ballerinalang.stdlib.io.channels.base.Representation.BIT_32;
import static org.ballerinalang.stdlib.io.channels.base.Representation.BIT_64;

/**
 * Test  i/o.
 */
public class DataInputOutputTest {
    /**
     * Specifies the default directory path.
     */
    private String currentDirectoryPath = "/tmp/";

    @BeforeClass
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/target";
    }

    @Test(description = "Test fixed long value ranges", dataProvider = "SignedLongValues")
    public void testSignedFixedLong(long value, Representation representation) throws IOException, URISyntaxException {
        String filePath = currentDirectoryPath + "/sample.bin";
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(filePath);
        Channel channel = new MockByteChannel(byteChannel);
        DataChannel dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        dataChannel.writeLong(value, representation);
        channel.close();
        byteChannel = TestUtil.openForReadingAndWriting(filePath);
        channel = new MockByteChannel(byteChannel);
        dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        long readInt = dataChannel.readLong(representation).getValue();
        Assert.assertEquals(readInt, value);
    }

    @Test(description = "Test signed var long", dataProvider = "SignedVarLongValues")
    public void testSingedVarLong(long value, int byteCount) throws IOException {
        String filePath = currentDirectoryPath + "/sample.bin";
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(filePath);
        Channel channel = new MockByteChannel(byteChannel);
        DataChannel dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        dataChannel.writeLong(value, Representation.VARIABLE);
        channel.close();
        byteChannel = TestUtil.openForReadingAndWriting(filePath);
        channel = new MockByteChannel(byteChannel);
        dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        LongResult longResult = dataChannel.readLong(Representation.VARIABLE);
        long readInt = longResult.getValue();
        int nBytes = longResult.getByteCount();
        Assert.assertEquals(readInt, value);
        Assert.assertEquals(nBytes, byteCount);
    }

    @Test(description = "Test floating point values", dataProvider = "DoubleValues")
    public void testFloat(double value, Representation representation) throws IOException {
        String filePath = currentDirectoryPath + "/sample.bin";
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(filePath);
        Channel channel = new MockByteChannel(byteChannel);
        DataChannel dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        dataChannel.writeDouble(value, representation);
        byteChannel = TestUtil.openForReadingAndWriting(filePath);
        channel = new MockByteChannel(byteChannel);
        dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        double readFloat = dataChannel.readDouble(representation);
        Assert.assertEquals(readFloat, value);
    }

    @Test(description = "Test boolean read/write")
    public void testBoolean() throws IOException {
        String filePath = currentDirectoryPath + "/sample.bin";
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(filePath);
        Channel channel = new MockByteChannel(byteChannel);
        DataChannel dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        dataChannel.writeBoolean(false);
        byteChannel = TestUtil.openForReadingAndWriting(filePath);
        channel = new MockByteChannel(byteChannel);
        dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        Assert.assertFalse(dataChannel.readBoolean());
    }

    @Test(description = "Test string read/write", dataProvider = "StringValues")
    public void testString(String content, String encoding) throws IOException {
        String filePath = currentDirectoryPath + "/sample.bin";
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(filePath);
        Channel channel = new MockByteChannel(byteChannel);
        DataChannel dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        dataChannel.writeString(content, encoding);
        byteChannel = TestUtil.openForReadingAndWriting(filePath);
        channel = new MockByteChannel(byteChannel);
        dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        String readStr = dataChannel.readString(content.getBytes().length, encoding);
        Assert.assertEquals(readStr, content);
    }

    @Test(description = "Test reading/writing mixed  input/output")
    public void testMixedData() throws IOException {
        int writtenInt = 13;
        double writtenDouble = 48449.56f;
        String filePath = currentDirectoryPath + "/sample.bin";
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(filePath);
        Channel channel = new MockByteChannel(byteChannel);
        DataChannel dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        dataChannel.writeLong(writtenInt, Representation.BIT_32);
        dataChannel.writeDouble(writtenDouble, Representation.BIT_32);
        dataChannel.writeBoolean(false);
        byteChannel = TestUtil.openForReadingAndWriting(filePath);
        channel = new MockByteChannel(byteChannel);
        dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        long longValue = dataChannel.readLong(Representation.BIT_32).getValue();
        double doubleValue = dataChannel.readDouble(Representation.BIT_32);
        boolean booleanValue = dataChannel.readBoolean();
        Assert.assertEquals(writtenInt, longValue);
        Assert.assertEquals(writtenDouble, doubleValue);
        Assert.assertEquals(false, booleanValue);
    }

    @DataProvider(name = "StringValues")
    public static Object[][] stringValues() {
        return new Object[][]{
                {"Test", CharEncoding.UTF_8},
                {"aaabbǊ", CharEncoding.UTF_8}
        };
    }

    @DataProvider(name = "SignedLongValues")
    public static Object[][] signedLongValues() {
        return new Object[][]{
                {0, BIT_16}, {0, BIT_32}, {0, BIT_64},
                {-1, BIT_16}, {-1, BIT_32}, {-1, BIT_64},
                {Short.MIN_VALUE, BIT_16}, {Short.MIN_VALUE, BIT_32},
                {Short.MIN_VALUE, BIT_64}, {Short.MAX_VALUE, BIT_16},
                {Short.MAX_VALUE, BIT_32}, {Short.MAX_VALUE, BIT_64},
                {Integer.MIN_VALUE, BIT_32}, {Integer.MIN_VALUE, BIT_64},
                {Integer.MAX_VALUE, BIT_32}, {Integer.MAX_VALUE, BIT_64},
                {Long.MIN_VALUE, BIT_64}, {Long.MAX_VALUE, BIT_64}
        };
    }

    @DataProvider(name = "SignedVarLongValues")
    public static Object[][] signedVarLongValues() {
        return new Object[][]{
                {0, 1}, {0, 1}, {0, 1},
                {-1, 1}, {-1, 1}, {-1, 1},
                {Short.MIN_VALUE, 3}, {Short.MIN_VALUE, 3},
                {Integer.MIN_VALUE, 5}, {Integer.MIN_VALUE, 5},
                {IOConstants.VAR_INT_MAX, 8}, {IOConstants.VAR_INT_MIN, 8}
        };
    }

    @DataProvider(name = "DoubleValues")
    public static Object[][] doubleValues() {
        return new Object[][]{
                {0.0f, BIT_32}, {0.0f, BIT_64},
                {-1.0f, BIT_32}, {-1.0f, BIT_64},
                {Float.MIN_VALUE, BIT_32}, {Float.MIN_VALUE, BIT_64},
                {Float.MAX_VALUE, BIT_32}, {Float.MAX_VALUE, BIT_64},
                {Double.MIN_VALUE, BIT_64}, {Double.MAX_VALUE, BIT_64}
        };
    }
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.nativeimpl.functions.io.bytes;

import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.base.Buffer;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Defines the unit test to test the functionality of Byte I/O related use cases.
 */
public class BytesInputOutputBufferTest {

    /**
     * Specifies the default directory path.
     */
    private String currentDirectoryPath = "/tmp/";

    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/target/";
    }

    @Test(description = "Reads files into multiple iterations")
    public void multiReadFile() throws IOException, URISyntaxException {
        int initialReadLimit = 3;
        int secondLapReadLimit = 3;
        int thirdLapReadLimit = 3;
        //During the 3rd lap all the bytes were get
        int thirdLapReadLimitExpected = 0;

        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel, 0);
        byte[] readBytes = channel.read(initialReadLimit);

        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, initialReadLimit);

        readBytes = channel.read(secondLapReadLimit);

        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, secondLapReadLimit);

        readBytes = channel.read(thirdLapReadLimit);

        channel.close();
        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, thirdLapReadLimitExpected);
    }

    @Test(description = "Reads file which has varying buffer sizes")
    public void varyingBufferSizeTest() throws IOException, URISyntaxException {
        final int numberOfBytesInFile = 7;
        final int fixedBufferSize = 15;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/sequenceOfChars");
        Channel channel = new MockByteChannel(byteChannel, fixedBufferSize);
        byte[] readBytes = channel.read(8);

        Assert.assertEquals(readBytes.length, numberOfBytesInFile);
    }

    @Test(description = "Reads bytes which has a fixed buffer for multiple read iterations")
    public void fixedBufferIterativeRead() throws IOException, URISyntaxException {
        final int numberOfBytesInFile = 7;
        final int fixedBufferSize = 15;
        int readByteLength = -1;
        ByteBuffer content = ByteBuffer.allocate(fixedBufferSize);
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/sequenceOfChars");
        Channel channel = new MockByteChannel(byteChannel, fixedBufferSize);
        while (readByteLength != 0) {
            byte[] readBytes = channel.read(3);
            content.put(readBytes);
            readByteLength = readBytes.length;
        }
        content.flip();

        Assert.assertEquals(content.limit(), numberOfBytesInFile);
    }

    @Test(expectedExceptions = BallerinaIOException.class)
    public void reverseFromNonExistingBuffer() {
        final int fixedBufferSize = 15;
        Buffer buffer = new Buffer(fixedBufferSize);
        buffer.reverse(2);
    }

    @Test(description = "Copy I/O byte file as a stream")
    public void fileStreamCopyTest() throws IOException, URISyntaxException {
        final int readLimit = 10000;
        int readByteCount = -1;
        int totalNumberOfBytesRead = 0;
        int totalNumberOfBytesWritten = 0;
        final int numberOfBytesInFile = 45613;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/images/ballerina.png");
        Channel channel = new MockByteChannel(byteChannel, IOConstants.CHANNEL_BUFFER_SIZE);
        ByteChannel writeByteChannel = TestUtil.openForWriting(currentDirectoryPath + "ballerinaCopy.png");
        Channel writeChannel = new MockByteChannel(writeByteChannel, 0);
        while (readByteCount != 0) {
            byte[] readBytes = channel.read(readLimit);
            int writtenByteCount = writeChannel.write(readBytes, 0);
            readByteCount = readBytes.length;
            totalNumberOfBytesRead = totalNumberOfBytesRead + readByteCount;
            totalNumberOfBytesWritten = totalNumberOfBytesWritten + writtenByteCount;
        }

        Assert.assertEquals(totalNumberOfBytesRead, numberOfBytesInFile);
        Assert.assertEquals(totalNumberOfBytesRead, totalNumberOfBytesWritten);
    }

    @Test(description = "Read bytes from fix buffer into multiple reads")
    public void multiReadFromFixedBuffer() throws IOException, URISyntaxException {

        int initialReadLimit = 3;
        int secondLapReadLimit = 3;
        int thirdLapReadLimit = 3;
        //During the 3rd lap all the bytes were get
        int thirdLapReadLimitExpected = 0;

        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel, 2);
        byte[] readBytes = channel.read(initialReadLimit);

        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, initialReadLimit);

        readBytes = channel.read(secondLapReadLimit);

        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, secondLapReadLimit);

        readBytes = channel.read(thirdLapReadLimit);

        channel.close();
        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, thirdLapReadLimitExpected);
    }

    @Test(description = "Request for bytes more exceeding the available limit")
    public void requestForExcessBytes() throws IOException, URISyntaxException {
        int requestedLimit = 10;
        int expectedLimit = 6;
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel, 0);
        byte[] readBytes = channel.read(requestedLimit);
        channel.close();
        Assert.assertEquals(readBytes.length, expectedLimit);
    }

    @Test(description = "Write bytes to file")
    public void writeBytesToFile() throws IOException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForWriting(currentDirectoryPath + "write.txt");
        Channel channel = new MockByteChannel(byteChannel, 0);
        byte[] bytes = "hello".getBytes();
        int numberOfBytesWritten = channel.write(bytes, 0);
        Assert.assertEquals(numberOfBytesWritten, bytes.length);
    }

}

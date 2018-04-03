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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.stream.Collectors;

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

    /**
     * <p>
     * Reads bytes into the array. This will ensure that the array is filled
     * </p>
     * <p>
     * The function will rerun only if the array is filled on the I/O source return EoF
     * </p>
     *
     * @param content initialized array that will hold the read content.
     * @param channel channel the bytes will be read from.
     * @return the number of bytes read.
     */
    private int readFull(byte[] content, Channel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(content);
        int numberOfBytesRead;
        int totalNumberOfBytesRead = 0;
        do {
            numberOfBytesRead = channel.read(buffer);
            numberOfBytesRead = numberOfBytesRead > 0 ? numberOfBytesRead : 0;
            totalNumberOfBytesRead = totalNumberOfBytesRead + numberOfBytesRead;
        } while (numberOfBytesRead > 0 && buffer.hasRemaining());
        return totalNumberOfBytesRead;
    }

    /**
     * Initializes a bytes array and read the specified amount of bytes.
     *
     * @param numberOfBytes number of bytes which should be read.
     * @return initialized byte []
     */
    private ReadByteResult read(int numberOfBytes, Channel channel) throws IOException {
        byte[] content = new byte[numberOfBytes];
        int numberOfBytesRead = readFull(content, channel);
        return new ReadByteResult(content, numberOfBytesRead);
    }

    /**
     * Writes the content to the given channel.
     *
     * @param content the content which should be written to the channel.
     * @param size    the number of bytes which should be written.
     * @param channel the channel the content will be written.
     * @return the number of bytes written.
     */
    private int writeFull(byte[] content, int size, Channel channel) throws IOException {
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        contentBuffer.limit(size);
        int totalNumberOfBytesWritten = 0;
        int numberOfBytesWritten;
        do {
            numberOfBytesWritten = channel.write(contentBuffer);
            totalNumberOfBytesWritten = totalNumberOfBytesWritten + numberOfBytesWritten;
        } while (contentBuffer.hasRemaining());
        return totalNumberOfBytesWritten;
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
        Channel channel = new MockByteChannel(byteChannel);
        byte[] readBytes = read(initialReadLimit, channel).getContent();

        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, initialReadLimit);

        readBytes = read(secondLapReadLimit, channel).getContent();

        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, secondLapReadLimit);

        ReadByteResult result = read(thirdLapReadLimit, channel);
        int numberOfReadBytes = result.getNumberOfBytesRead();

        channel.close();
        //This should hold the number of bytes get
        Assert.assertEquals(numberOfReadBytes, thirdLapReadLimitExpected);
    }

    @Test(description = "Read all bytes from file with larger buffer size")
    public void excessBufferAllocation() throws IOException, URISyntaxException {
        int initialReadLimit = 3;
        int secondLapReadLimit = 3;
        int thirdLapReadLimit = 3;
        //During the 3rd lap all the bytes were get
        int thirdLapReadLimitExpected = 0;

        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel, IOConstants.CHANNEL_BUFFER_SIZE);
        byte[] readBytes = channel.readFull(initialReadLimit);

        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, initialReadLimit);

        readBytes = channel.readFull(secondLapReadLimit);

        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, secondLapReadLimit);

        readBytes = channel.readFull(thirdLapReadLimit);

        channel.close();
        //This should hold the number of bytes get
        Assert.assertEquals(readBytes.length, thirdLapReadLimitExpected);
    }

    @Test(description = "Reads file which has varying buffer sizes")
    public void varyingBufferSizeTest() throws IOException, URISyntaxException {
        final int numberOfBytesInFile = 7;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/sequenceOfChars");
        Channel channel = new MockByteChannel(byteChannel);
        int numberOfBytesRead = read(8, channel).getNumberOfBytesRead();

        Assert.assertEquals(numberOfBytesRead, numberOfBytesInFile);
    }

    @Test(description = "Reads bytes which has a fixed buffer for multiple read iterations")
    public void fixedBufferIterativeRead() throws IOException, URISyntaxException {
        final int numberOfBytesInFile = 7;
        final int fixedBufferSize = 15;
        int readByteLength = -1;
        int totalNumberOfBytesRead = 0;
        ByteBuffer content = ByteBuffer.allocate(fixedBufferSize);
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/sequenceOfChars");
        Channel channel = new MockByteChannel(byteChannel);
        while (readByteLength != 0) {
            ReadByteResult readByteResult = read(3, channel);
            byte[] readBytes = readByteResult.getContent();
            content.put(readBytes);
            readByteLength = readByteResult.getNumberOfBytesRead();
            totalNumberOfBytesRead = totalNumberOfBytesRead + readByteLength;
        }
        content.flip();

        Assert.assertEquals(totalNumberOfBytesRead, numberOfBytesInFile);
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
        int totalNumberOfBytesRead = 0;
        int totalNumberOfBytesWritten = 0;
        final int numberOfBytesInFile = 45613;
        int numberOfBytesRead = 38938;
        int numberOfBytesWritten;
        //Number of characters in this file would be 6
        ByteChannel readByteChannel = TestUtil.openForReading("datafiles/io/images/ballerina.png");
        ByteChannel writeByteChannel = TestUtil.openForWriting(currentDirectoryPath + "ballerinaCopy.png");
        Channel readChannel = new MockByteChannel(readByteChannel);
        Channel writeChannel = new MockByteChannel(writeByteChannel);
        while (numberOfBytesRead > 0) {
            ReadByteResult readByteResult = read(readLimit, readChannel);
            byte[] readBytes = readByteResult.getContent();
            numberOfBytesRead = readByteResult.getNumberOfBytesRead();
            numberOfBytesWritten = writeFull(readBytes, numberOfBytesRead, writeChannel);
            totalNumberOfBytesRead = totalNumberOfBytesRead + numberOfBytesRead;
            totalNumberOfBytesWritten = totalNumberOfBytesWritten + numberOfBytesWritten;
        }

        Assert.assertEquals(totalNumberOfBytesRead, numberOfBytesInFile);
        Assert.assertEquals(totalNumberOfBytesRead, totalNumberOfBytesWritten);
    }

    @Test(description = "Read bytes from fix buffer into multiple reads")
    public void multiReadFromFixedBuffer() throws IOException, URISyntaxException {

        int initialReadLimit = 3;
        int secondLapReadLimit = 3;
        int thirdLapReadLimit = 3;
        //During the 3rd lap all the bytes were read
        int thirdLapReadLimitExpected = 0;

        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel);
        int numberOfBytesRead = read(initialReadLimit, channel).getNumberOfBytesRead();

        //This should hold the number of bytes read
        Assert.assertEquals(numberOfBytesRead, initialReadLimit);

        numberOfBytesRead = read(secondLapReadLimit, channel).getNumberOfBytesRead();

        //This should hold the number of bytes get
        Assert.assertEquals(numberOfBytesRead, secondLapReadLimit);

        numberOfBytesRead = read(thirdLapReadLimit, channel).getNumberOfBytesRead();

        channel.close();
        //This should hold the number of bytes get
        Assert.assertEquals(numberOfBytesRead, thirdLapReadLimitExpected);
    }

    @Test(description = "Request for bytes more exceeding the available limit")
    public void requestForExcessBytes() throws IOException, URISyntaxException {
        int requestedLimit = 10;
        int expectedLimit = 6;
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel);
        int numberOfBytes = read(requestedLimit, channel).getNumberOfBytesRead();
        channel.close();
        Assert.assertEquals(numberOfBytes, expectedLimit);
    }

    @Test(description = "Write bytes to file")
    public void writeBytesToFile() throws IOException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForWriting(currentDirectoryPath + "write.txt");
        Channel channel = new MockByteChannel(byteChannel);
        byte[] bytes = "hello".getBytes();
        int numberOfBytesWritten = writeFull(bytes, bytes.length, channel);
        Assert.assertEquals(numberOfBytesWritten, bytes.length);
    }

    @Test(description = "Get content via InputStream")
    public void getContentViaInputStream() throws IOException, URISyntaxException {
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel, 0);
        final InputStream inputStream = channel.getInputStream();
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            final String result = buffer.lines().collect(Collectors.joining("\n"));
            Assert.assertEquals(result, "123456", "Did not get expected string output.");
        }
        inputStream.close();
    }

    @Test(description = "Validate getting InputStream from closed channel",
            expectedExceptions = BallerinaIOException.class,
            expectedExceptionsMessageRegExp = "Channel is already closed.")
    public void checkChannelCloseStatus() throws IOException, URISyntaxException {
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel, 0);
        final InputStream inputStream = channel.getInputStream();
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            final String result = buffer.lines().collect(Collectors.joining("\n"));
            Assert.assertEquals(result, "123456", "Did not get expected string output.");
        }
        inputStream.close();
        channel.close();
        channel.getInputStream();
    }
}

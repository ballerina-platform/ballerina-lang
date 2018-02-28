/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.nativeimpl.functions.io.bytes;

import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResponse;
import org.ballerinalang.nativeimpl.io.events.bytes.ReadBytesEvent;
import org.ballerinalang.nativeimpl.io.events.bytes.WriteBytesEvent;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Tests operations through the async io framework
 */
public class AsyncReadWriteTest {

    /**
     * Specifies the default directory path.
     */
    private String currentDirectoryPath = "/tmp/";

    /**
     * Will be the I/O event handler
     */
    private EventManager eventManager = EventManager.getInstance();

    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/target/";
    }

    /**
     * Wraps byte [] to a buffer.
     *
     * @return the buffer which as the wrapped byte []
     */
    private ByteBuffer wrapByteArray(byte[] content, int offset) {
        ByteBuffer bufferedContent = ByteBuffer.wrap(content);
        bufferedContent.position(offset);
        return bufferedContent;
    }

    /**
     * Asynchronously reads bytes from the channel.
     *
     * @param content               the initialized array which should be filled with the content.
     * @param channel               the channel the content should be read into.
     * @param expectedNumberOfBytes the number of bytes which should be read
     * @throws InterruptedException during interrupt error.
     * @throws ExecutionException   errors which occurs while execution.
     */
    private int readAsync(byte[] content, Channel channel, int expectedNumberOfBytes)
            throws InterruptedException, ExecutionException {
        int numberOfBytesRead = 0;
        do {
            ByteBuffer contentBuffer = wrapByteArray(content, numberOfBytesRead);
            ReadBytesEvent event = new ReadBytesEvent(contentBuffer, channel);
            Future<EventResponse> future = eventManager.publish(event);
            EventResponse eventResponse = future.get();
            numberOfBytesRead = numberOfBytesRead + (Integer) eventResponse.getResponse();
        } while (numberOfBytesRead < expectedNumberOfBytes);
        return numberOfBytesRead;
    }

    /**
     * Asynchronously writes bytes to a channel.
     *
     * @param content content which should be written.
     * @param channel the channel the bytes should be written.
     * @return the number of bytes written to the channel.
     * @throws ExecutionException   errors which occur during execution.
     * @throws InterruptedException during interrupt error.
     */
    private int writeAsync(byte[] content, Channel channel) throws ExecutionException, InterruptedException {
        int numberOfBytesWritten = 0;
        do {
            int arrayLength = content.length;
            WriteBytesEvent writeBytesEvent = new WriteBytesEvent(channel, content, numberOfBytesWritten, arrayLength);
            Future<EventResponse> future = eventManager.publish(writeBytesEvent);
            EventResponse eventResponse = future.get();
            numberOfBytesWritten = numberOfBytesWritten + (Integer) eventResponse.getResponse();
        } while (numberOfBytesWritten < content.length);
        return numberOfBytesWritten;
    }

    @Test(description = "Read into fixed byte[] using async io framework")
    public void readBytes() throws IOException, URISyntaxException, ExecutionException,
            InterruptedException {
        byte[] content = new byte[2];
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel);

        byte[] expected = {49, 50};
        int expectedNumberOfBytes = 2;
        readAsync(content, channel, expectedNumberOfBytes);
        Assert.assertEquals(expected, content);

        expected = new byte[]{51, 52};
        readAsync(content, channel, expectedNumberOfBytes);
        Assert.assertEquals(expected, content);

        expected = new byte[]{53, 54};
        readAsync(content, channel, expectedNumberOfBytes);
        Assert.assertEquals(expected, content);

        expectedNumberOfBytes = -1;
        content = new byte[2];
        expected = new byte[]{0, 0};
        int numberOfBytesRead = readAsync(content, channel, expectedNumberOfBytes);
        Assert.assertEquals(numberOfBytesRead, expectedNumberOfBytes);
        Assert.assertEquals(expected, content);
    }

    @Test(description = "Write into a channel using async io framework")
    public void writeBytes() throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForWriting(currentDirectoryPath + "write.txt");
        Channel channel = new MockByteChannel(byteChannel, 0);
        byte[] bytes = "hello".getBytes();

        int numberOfBytesWritten = writeAsync(bytes, channel);
        Assert.assertEquals(numberOfBytesWritten, bytes.length);
    }

}

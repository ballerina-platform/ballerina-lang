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

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.util.concurrent.ExecutionException;

/**
 * Tests operations through the async io framework.
 */
public class AsyncReadWriteTest {

    /**
     * Specifies the default directory path.
     */
    private String currentDirectoryPath = "/tmp/";

    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/target/";
    }

    @Test(description = "Read into fixed byte[] using async io framework")
    public void readBytes() throws IOException, URISyntaxException, ExecutionException,
            InterruptedException {
        byte[] content = new byte[2];
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel);

        byte[] expected = {49, 50};
        IOUtils.readFull(channel, content, new EventContext());
        Assert.assertEquals(expected, content);

        expected = new byte[]{51, 52};
        IOUtils.readFull(channel, content, new EventContext());
        Assert.assertEquals(expected, content);

        expected = new byte[]{53, 54};
        IOUtils.readFull(channel, content, new EventContext());
        Assert.assertEquals(expected, content);

        int expectedNumberOfBytes = 0;
        content = new byte[2];
        expected = new byte[]{0, 0};
        int numberOfBytesRead = IOUtils.readFull(channel, content, new EventContext());
        Assert.assertEquals(numberOfBytesRead, expectedNumberOfBytes);
        Assert.assertEquals(expected, content);
    }

    @Test(description = "Reads bytes and validate the byte content")
    public void readContentValidationTest() throws IOException, URISyntaxException, ExecutionException,
            InterruptedException {
        byte[] content = new byte[3];
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel);

        byte[] expected = "123".getBytes();

        IOUtils.readFull(channel, content, new EventContext());
        Assert.assertEquals(expected, content);

        expected = "456".getBytes();
        IOUtils.readFull(channel, content, new EventContext());
        Assert.assertEquals(expected, content);

        content = new byte[3];
        expected = new byte[3];
        IOUtils.readFull(channel, content, new EventContext());
        Assert.assertEquals(expected, content);
    }

    @Test(description = "Write into a channel using async io framework")
    public void writeBytes() throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(currentDirectoryPath + "write.txt");
        Channel channel = new MockByteChannel(byteChannel);
        byte[] bytes = "hello".getBytes();

        int numberOfBytesWritten = IOUtils.writeFull(channel, bytes, 0, new EventContext());
        Assert.assertEquals(numberOfBytesWritten, bytes.length);
    }

}

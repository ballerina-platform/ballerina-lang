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

import org.ballerinalang.nativeimpl.io.channels.BByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.channels.ByteChannel;

/**
 * Defines the unit test to test the functionality of reading bytes
 */
public class BytesInputOutputBufferTest {

    /**
     * Specifies the default directory path
     */
    private String currentDirectoryPath = "/tmp/";


    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/modules/ballerina-test/target/";
    }

    /**
     * <p>
     * Read bytes into multiple iterations, validates whether the state is maintained properly
     * </p>
     * <p>
     * This test covers varying buffer sizes when the bytes are read into multiple iterations
     * </p>
     *
     * @throws IOException
     */
    @Test
    public void multiReadFile() throws IOException {

        int initialReadLimit = 3;
        int secondLapReadLimit = 3;
        int thirdLapReadLimit = 3;
        //During the 3rd lap all the bytes were read
        int thirdLapReadLimitExpected = 0;

        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        BByteChannel channel = new BByteChannel(byteChannel);
        byte[] readBytes = channel.read(initialReadLimit);

        //This should hold the number of bytes read
        Assert.assertEquals(readBytes.length, initialReadLimit);

        readBytes = channel.read(secondLapReadLimit);

        //This should hold the number of bytes read
        Assert.assertEquals(readBytes.length, secondLapReadLimit);

        readBytes = channel.read(thirdLapReadLimit);

        channel.close();
        //This should hold the number of bytes read
        Assert.assertEquals(readBytes.length, thirdLapReadLimitExpected);
    }

    /**
     * <p>
     * Read bytes into multiple iterations, validates whether the state is maintained properly
     * </p>
     * <p>
     * This test covers varying buffer sizes when the bytes are read into multiple iterations
     * </p>
     *
     * @throws IOException
     */
    @Test
    public void multiReadFromFixedBuffer() throws IOException {

        int initialReadLimit = 3;
        int secondLapReadLimit = 3;
        int thirdLapReadLimit = 3;
        //During the 3rd lap all the bytes were read
        int thirdLapReadLimitExpected = 0;

        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        BByteChannel channel = new BByteChannel(byteChannel, 2);
        byte[] readBytes = channel.read(initialReadLimit);

        //This should hold the number of bytes read
        Assert.assertEquals(readBytes.length, initialReadLimit);

        readBytes = channel.read(secondLapReadLimit);

        //This should hold the number of bytes read
        Assert.assertEquals(readBytes.length, secondLapReadLimit);

        readBytes = channel.read(thirdLapReadLimit);

        channel.close();
        //This should hold the number of bytes read
        Assert.assertEquals(readBytes.length, thirdLapReadLimitExpected);
    }

    @Test
    public void requestForExcessBytes() throws IOException {
        int requestedLimit = 10;
        int expectedLimit = 6;

        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        BByteChannel channel = new BByteChannel(byteChannel);
        byte[] readBytes = channel.read(requestedLimit);

        channel.close();
        Assert.assertEquals(readBytes.length, expectedLimit);
    }

    @Test
    public void writeBytesToFile() throws IOException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForWriting(currentDirectoryPath + "write.txt");
        BByteChannel channel = new BByteChannel(byteChannel);
        byte[] bytes = "hello".getBytes();
        int numberOfBytesWritten = channel.write(bytes, 0);

        Assert.assertEquals(numberOfBytesWritten, bytes.length);
    }


}

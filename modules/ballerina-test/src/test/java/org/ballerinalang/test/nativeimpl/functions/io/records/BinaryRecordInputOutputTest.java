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

package org.ballerinalang.test.nativeimpl.functions.io.records;

import org.ballerinalang.nativeimpl.io.channels.base.AbstractChannel;
import org.ballerinalang.nativeimpl.io.channels.base.BDelimitedBinaryRecordChannel;
import org.ballerinalang.test.nativeimpl.functions.io.BByteChannelTest;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.channels.ByteChannel;

/**
 * Test binary record I/O
 */
public class BinaryRecordInputOutputTest {

    /**
     * Specifies the default directory path
     */
    private String currentDirectoryPath = "/tmp/";


    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/modules/ballerina-test/target/";
    }

    /**
     * Will read the binary records through the channel
     */
    @Test
    public void testReadBinaryRecordChannel() throws IOException {
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/records/sample3");
        byte[] delimiter = "\n".getBytes();
        AbstractChannel channel = new BByteChannelTest(byteChannel, 10);
        BDelimitedBinaryRecordChannel recordChannel = new BDelimitedBinaryRecordChannel(channel, delimiter);
        //Expected Bytes
        byte[] expectedBytes = "ff d8 ff e0 00 10 4a 46 49 46 00 01 01 01 01 2c".getBytes();
        //Read the first record
        byte[] readBytes = recordChannel.read();
        Assert.assertEquals(expectedBytes, readBytes);
        //Will take the next round of iteration
        expectedBytes = "01 2c 00 00 ff db 00 43 00 0d 09 0a 0b 0a 08 0d".getBytes();
        readBytes = recordChannel.read();
        Assert.assertEquals(expectedBytes, readBytes);
        //The next round does not contain any data
        expectedBytes = new byte[0];
        readBytes = recordChannel.read();
        Assert.assertEquals(expectedBytes, readBytes);
    }

    @Test
    public void testWriteBinaryRecord() throws IOException {
        ByteChannel byteChannel = TestUtil.openForWriting(currentDirectoryPath + "binaryRecords");
        AbstractChannel channel = new BByteChannelTest(byteChannel, 10);
        byte[] delimiter = "\n".getBytes();
        BDelimitedBinaryRecordChannel recordChannel = new BDelimitedBinaryRecordChannel(channel, delimiter);
        byte [] data = "55 d9 ff 00 2c".getBytes();
        recordChannel.write(data);
    }
}

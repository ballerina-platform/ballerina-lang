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

import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.channels.base.DataChannel;
import org.ballerinalang.nativeimpl.io.channels.base.Representation;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;

/**
 * Test data i/o
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


    @Test(description = "Test ''ReadInteger")
    public void testReadInteger() throws IOException, URISyntaxException {
        String filePath = currentDirectoryPath + "/sample.bin";
        long writtenInt = -3939;
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(filePath);
        Channel channel = new MockByteChannel(byteChannel);
        DataChannel dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        dataChannel.writeFixedInt(writtenInt, Representation.BIT_32);
        channel.close();
        byteChannel = TestUtil.openForReadingAndWriting(filePath);
        channel = new MockByteChannel(byteChannel);
        dataChannel = new DataChannel(channel, ByteOrder.nativeOrder());
        long readInt = dataChannel.readFixedInt(Representation.BIT_32);
        Assert.assertEquals(readInt, writtenInt);
    }

}

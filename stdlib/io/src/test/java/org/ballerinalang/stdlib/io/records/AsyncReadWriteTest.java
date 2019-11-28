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

package org.ballerinalang.stdlib.io.records;

import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.stdlib.io.MockByteChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.util.TestUtil;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Represents the framework for read/write delimited text records.
 */
public class AsyncReadWriteTest {
    /**
     * Specifies the default directory path.
     */
    private String currentDirectoryPath = "/tmp/";

    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/build/";
    }

    @Test(description = "Test which will read text records from a given channel using async io framework")
    public void readTextRecords() throws IOException, URISyntaxException, BallerinaIOException {
        int expectedFieldCount = 3;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/records/sample.csv");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
        DelimitedRecordChannel recordChannel = new DelimitedRecordChannel(characterChannel, "\n", ",");
        Assert.assertEquals(recordChannel.read().length, expectedFieldCount);
        Assert.assertEquals(recordChannel.read().length, expectedFieldCount);
        Assert.assertEquals(recordChannel.read().length, expectedFieldCount);
        recordChannel.close();
    }

    @Test(description = "Test which will write records to a channel using async io framework")
    public void writeRecords() throws IOException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(currentDirectoryPath + "records.csv");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
        DelimitedRecordChannel recordChannel = new DelimitedRecordChannel(characterChannel, "\n", ",");

        String[] recordOne = {"Foo", "Bar", "911"};
        ArrayValue recordOneArr = new ArrayValueImpl(recordOne);
        recordChannel.write(recordOneArr);

        String[] recordTwo = {"Jim", "Com", "119"};
        ArrayValue recordTwoArr = new ArrayValueImpl(recordTwo);
        recordChannel.write(recordTwoArr);
        recordChannel.close();
    }
}

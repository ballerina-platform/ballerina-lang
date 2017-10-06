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

import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.nativeimpl.io.channels.BByteChannel;
import org.ballerinalang.nativeimpl.io.channels.BCharacterChannel;
import org.ballerinalang.nativeimpl.io.channels.BTextRecordChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Tests record I/O functionality
 */
public class RecordInputOutputTest {

    /**
     * Specifies the default directory path
     */
    private String currentDirectoryPath = "/tmp/";


    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/modules/ballerina-test/target/";
    }

    /**
     * Tests reading characters
     *
     * @throws IOException during I/O error
     */
    @Test
    public void readRecords() throws IOException {

        int expectedRecordCount = 3;

        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/records/sample.csv");
        BByteChannel channel = new BByteChannel(byteChannel);
        BCharacterChannel characterChannel = new BCharacterChannel(channel, StandardCharsets.UTF_8.name());
        BTextRecordChannel recordChannel = new BTextRecordChannel(characterChannel, "\n", ",");

        String[] readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedRecordCount);

        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedRecordCount);

        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedRecordCount);

        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, 0);
    }

    @Test
    public void readNonIndentedRecords() throws IOException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/records/sample2.csv");
        BByteChannel channel = new BByteChannel(byteChannel);
        BCharacterChannel characterChannel = new BCharacterChannel(channel, StandardCharsets.UTF_8.name());
        BTextRecordChannel recordChannel = new BTextRecordChannel(characterChannel, "\n", ",");

        String[] readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, 9);

        //This will be a blank record
        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, 1);

        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, 9);

    }

    @Test
    public void writeRecords() throws IOException {
        int expectedRecordCount = 3;

        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForWriting(currentDirectoryPath + "records.csv");
        BByteChannel channel = new BByteChannel(byteChannel);
        BCharacterChannel characterChannel = new BCharacterChannel(channel, StandardCharsets.UTF_8.name());
        BTextRecordChannel recordChannel = new BTextRecordChannel(characterChannel, "\n", ",");

        String[] recordOne = {"Foo", "Bar", "911"};
        BStringArray recordOneArr = new BStringArray(recordOne);

        recordChannel.write(recordOneArr);

        String[] recordTwo = {"Jim", "Com", "119"};
        BStringArray recordTwoArr = new BStringArray(recordTwo);

        recordChannel.write(recordTwoArr);
    }
}

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
 * Tests record I/O functionality.
 */
public class RecordInputOutputTest {
    /**
     * Specifies the default directory path.
     */
    private String currentDirectoryPath = "/tmp/";

    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/build/";
    }

    @Test(description = "Reads records from file")
    public void readRecords() throws IOException, URISyntaxException, BallerinaIOException {
        int expectedFieldCount = 3;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/records/sample.csv");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
        DelimitedRecordChannel recordChannel = new DelimitedRecordChannel(characterChannel, "\n", ",");

        String[] readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedFieldCount);

        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedFieldCount);

        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedFieldCount);

        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, 0);

        recordChannel.close();
    }

    @Test(description = "Processors records in sequence with hasNext()")
    public void processRecordSequence() throws IOException, URISyntaxException, BallerinaIOException {
        int expectedFieldCount = 3;
        boolean hasNext = false;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/records/sample.csv");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
        DelimitedRecordChannel recordChannel = new DelimitedRecordChannel(characterChannel, "\n", ",");

        hasNext = recordChannel.hasNext();
        String[] readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedFieldCount);
        Assert.assertTrue(hasNext);

        hasNext = recordChannel.hasNext();
        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedFieldCount);
        Assert.assertTrue(hasNext);

        hasNext = recordChannel.hasNext();
        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedFieldCount);
        Assert.assertTrue(hasNext);

        hasNext = recordChannel.hasNext();
        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, 0);
        Assert.assertFalse(hasNext);

        recordChannel.close();
    }

    @Test(description = "Read lengthy records")
    public void readLongRecord() throws IOException, URISyntaxException, BallerinaIOException {
        int expectedFieldCount = 18;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/records/sample4.csv");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
        DelimitedRecordChannel recordChannel = new DelimitedRecordChannel(characterChannel, "\n", ",");

        String[] readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedFieldCount);

        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, expectedFieldCount);
    }

    @Test(description = "Read records which are not indented properly")
    public void readNonIndentedRecords() throws IOException, URISyntaxException, BallerinaIOException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/records/sample2.csv");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
        DelimitedRecordChannel recordChannel = new DelimitedRecordChannel(characterChannel, "\n", ",");

        String[] readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, 9);
        Assert.assertTrue(recordChannel.hasNext(), "Expecting more records but received as EOL.");

        //This will be a blank record
        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, 1);
        Assert.assertTrue(recordChannel.hasNext(), "Expecting more records but received as EOL.");

        readRecord = recordChannel.read();
        Assert.assertEquals(readRecord.length, 9);
        Assert.assertFalse(recordChannel.hasNext(),
                "Last record received, but indicate as more records available.");

        recordChannel.close();
    }

    @Test(description = "Writes records to channel")
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

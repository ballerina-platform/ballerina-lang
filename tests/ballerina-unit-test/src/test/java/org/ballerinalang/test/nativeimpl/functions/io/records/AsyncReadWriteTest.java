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

package org.ballerinalang.test.nativeimpl.functions.io.records;

import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.events.EventManager;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.records.DelimitedRecordReadEvent;
import org.ballerinalang.stdlib.io.events.records.DelimitedRecordWriteEvent;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Represents the framework for read/write delimited text records.
 */
public class AsyncReadWriteTest {
    /**
     * Specifies the default directory path.
     */
    private String currentDirectoryPath = "/tmp/";

    /**
     * Will be the I/O event handler.
     */
    private EventManager eventManager = EventManager.getInstance();

    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/target/";
    }

    @Test(description = "Test which will read text records from a given channel using async io framework")
    public void readTextRecords() throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        int expectedFieldCount = 3;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/records/sample.csv");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
        DelimitedRecordChannel recordChannel = new DelimitedRecordChannel(characterChannel, "\n", ",");

        DelimitedRecordReadEvent event = new DelimitedRecordReadEvent(recordChannel);
        Future<EventResult> future = eventManager.publish(event);
        EventResult eventResult = future.get();
        String[] readRecord = (String[]) eventResult.getResponse();
        Assert.assertEquals(readRecord.length, expectedFieldCount);

        event = new DelimitedRecordReadEvent(recordChannel);
        future = eventManager.publish(event);
        eventResult = future.get();
        readRecord = (String[]) eventResult.getResponse();
        Assert.assertEquals(readRecord.length, expectedFieldCount);

        event = new DelimitedRecordReadEvent(recordChannel);
        future = eventManager.publish(event);
        eventResult = future.get();
        readRecord = (String[]) eventResult.getResponse();
        Assert.assertEquals(readRecord.length, expectedFieldCount);

        event = new DelimitedRecordReadEvent(recordChannel);
        future = eventManager.publish(event);
        eventResult = future.get();
        readRecord = (String[]) eventResult.getResponse();
        Assert.assertEquals(readRecord.length, 0);

        recordChannel.close();
    }

    @Test(description = "Test which will write records to a channel using async io framework")
    public void writeRecords() throws IOException, ExecutionException, InterruptedException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(currentDirectoryPath + "records.csv");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
        DelimitedRecordChannel recordChannel = new DelimitedRecordChannel(characterChannel, "\n", ",");

        String[] recordOne = {"Foo", "Bar", "911"};
        BStringArray recordOneArr = new BStringArray(recordOne);

        DelimitedRecordWriteEvent recordWriteEvent = new DelimitedRecordWriteEvent(recordChannel, recordOneArr);
        Future<EventResult> future = eventManager.publish(recordWriteEvent);
        future.get();

        String[] recordTwo = {"Jim", "Com", "119"};
        BStringArray recordTwoArr = new BStringArray(recordTwo);

        recordWriteEvent = new DelimitedRecordWriteEvent(recordChannel, recordTwoArr);
        future = eventManager.publish(recordWriteEvent);
        future.get();

        recordChannel.close();
    }
}

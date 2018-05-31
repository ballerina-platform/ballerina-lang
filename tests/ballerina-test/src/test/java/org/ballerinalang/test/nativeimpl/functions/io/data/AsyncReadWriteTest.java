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

package org.ballerinalang.test.nativeimpl.functions.io.data;

import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.data.ReadIntegerEvent;
import org.ballerinalang.nativeimpl.io.events.data.WriteIntegerEvent;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Test data io async operations.
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

    @Test(description = "Read integer from file")
    public void processInteger() throws IOException, URISyntaxException, ExecutionException,
            InterruptedException {

        String path = currentDirectoryPath + "sample.dat";

        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(currentDirectoryPath + "sample.dat");
        Channel channel = new MockByteChannel(byteChannel);
        WriteIntegerEvent writeBytesEvent = new WriteIntegerEvent(channel,-123,new EventContext());

        CompletableFuture<EventResult> publish = EventManager.getInstance().publish(writeBytesEvent);
        EventResult eventResult = publish.get();

        //Number of characters in this file would be 6
        byteChannel = TestUtil.openForReadingAndWriting(path);
        channel = new MockByteChannel(byteChannel);

        ReadIntegerEvent event = new ReadIntegerEvent(channel, new EventContext());
        publish = EventManager.getInstance().publish(event);
        eventResult = publish.get();
        Assert.assertEquals(123,(int) eventResult.getResponse());
    }
}

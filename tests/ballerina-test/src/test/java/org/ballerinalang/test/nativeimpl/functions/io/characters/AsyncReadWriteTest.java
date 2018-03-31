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

package org.ballerinalang.test.nativeimpl.functions.io.characters;

import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.characters.ReadCharactersEvent;
import org.ballerinalang.nativeimpl.io.events.characters.WriteCharactersEvent;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Represents the framework for reading writing characters using async io framework.
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

    @Test(description = "Tests reading characters through the async io framework")
    public void readCharacters() throws Exception {
        int numberOfCharactersToRead = 2;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/longChars.txt");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());

        ReadCharactersEvent event = new ReadCharactersEvent(characterChannel, numberOfCharactersToRead);
        Future<EventResult> future = eventManager.publish(event);
        EventResult eventResult = future.get();
        String content = (String) eventResult.getResponse();

        Assert.assertEquals("Ǌa", content);

        numberOfCharactersToRead = 3;
        event = new ReadCharactersEvent(characterChannel, numberOfCharactersToRead);
        future = eventManager.publish(event);
        eventResult = future.get();
        content = (String) eventResult.getResponse();

        Assert.assertEquals("bcǊ", content);

        numberOfCharactersToRead = 4;
        event = new ReadCharactersEvent(characterChannel, numberOfCharactersToRead);
        future = eventManager.publish(event);
        eventResult = future.get();
        content = (String) eventResult.getResponse();

        Assert.assertEquals("ff", content);

        characterChannel.close();
    }

    @Test(description = "Test writing characters through async io framework")
    public void writeCharacters() throws IOException, ExecutionException, InterruptedException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForWriting(currentDirectoryPath + "write.txt");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());

        String text = "HelloǊ";
        int numberOfBytes = text.getBytes().length;

        WriteCharactersEvent event = new WriteCharactersEvent(characterChannel, text, 0);
        Future<EventResult> future = eventManager.publish(event);
        EventResult eventResult = future.get();
        int numberOfCharactersWritten = (int) eventResult.getResponse();
        Assert.assertEquals(numberOfCharactersWritten, numberOfBytes);
        characterChannel.close();
    }
}

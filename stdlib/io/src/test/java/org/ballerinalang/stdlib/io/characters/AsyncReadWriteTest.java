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

package org.ballerinalang.stdlib.io.characters;

import org.ballerinalang.stdlib.io.MockByteChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Represents the framework for reading writing characters using async io framework.
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

    @Test(description = "Tests reading characters through the async io framework")
    public void readCharacters() throws Exception {
        int numberOfCharactersToRead = 2;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/longChars.txt");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
        Assert.assertEquals("Ǌa", characterChannel.read(numberOfCharactersToRead));

        numberOfCharactersToRead = 3;
        Assert.assertEquals("bcǊ", characterChannel.read(numberOfCharactersToRead));

        numberOfCharactersToRead = 4;
        Assert.assertEquals("ff", characterChannel.read(numberOfCharactersToRead));

        characterChannel.close();
    }

    @Test(description = "Test writing characters through async io framework")
    public void writeCharacters() throws IOException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(currentDirectoryPath + "write.txt");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());

        String text = "HelloǊ";
        int numberOfBytes = text.getBytes().length;

        Assert.assertEquals(characterChannel.write(text, 0), numberOfBytes);
        characterChannel.close();
    }
}

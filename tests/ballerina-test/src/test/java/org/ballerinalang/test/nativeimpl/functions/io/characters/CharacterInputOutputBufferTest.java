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

package org.ballerinalang.test.nativeimpl.functions.io.characters;

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Tests characters I/O operations.
 */
public class CharacterInputOutputBufferTest {
    /**
     * Specifies the default directory path.
     */
    private String currentDirectoryPath = "/tmp/";

    @BeforeSuite
    public void setup() {
        currentDirectoryPath = System.getProperty("user.dir") + "/target/";
    }

    @Test(description = "Read characters which does not fit to the fixed buffer limit")
    public void readFussyCharacters() throws IOException, URISyntaxException {
        int numberOfCharactersToRead = 2;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/utf8file.txt");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());

        String readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters, "aa");

        numberOfCharactersToRead = 4;
        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters, "abbǊ");

        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters.length(), 0);
        characterChannel.close();
    }

    @Test(description = "Reads characters which are represented with long bytes")
    public void readLongCharacters() throws IOException, URISyntaxException {
        int numberOfCharactersToRead = 2;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/longChars.txt");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());

        String readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals("Ǌa", readCharacters);

        numberOfCharactersToRead = 3;
        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals("bcǊ", readCharacters);

        numberOfCharactersToRead = 4;
        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals("ff", readCharacters);

        characterChannel.close();
    }

    @Test(description = "Read corrupted characters from text")
    public void readCorruptedCharacters() throws IOException, URISyntaxException {
        int numberOfCharactersToRead;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/corruptedText");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());

        numberOfCharactersToRead = 2;
        String readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals("�T", readCharacters);

        numberOfCharactersToRead = 11;
        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals("est Curr ÃŸ", readCharacters);

        numberOfCharactersToRead = 4;
        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(" �", readCharacters);

        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters, "");

        characterChannel.close();
    }

    @Test(description = "Read from file which has all corrupted chars")
    public void readCorruptedCharactersIntoMultipleChannelReads() throws IOException, URISyntaxException {
        int numberOfCharactersToRead;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/corruptedText2");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());

        numberOfCharactersToRead = 3;
        String readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals("���", readCharacters);

        numberOfCharactersToRead = 3;
        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals("��a", readCharacters);

        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters, "");

        characterChannel.close();
    }

    @Test(description = "Read characters into multiple iterations")
    public void readCharacters() throws IOException, URISyntaxException {
        int numberOfCharactersToRead = 2;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());

        String readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters.length(), numberOfCharactersToRead);

        numberOfCharactersToRead = 1;
        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters.length(), numberOfCharactersToRead);

        numberOfCharactersToRead = 4;
        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters.length(), 3);
        characterChannel.close();
    }

    @Test(description = "Write characters to file")
    public void writeCharacters() throws IOException {
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReadingAndWriting(currentDirectoryPath + "write.txt");
        Channel channel = new MockByteChannel(byteChannel);
        CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());

        String text = "HelloǊ";
        int numberOfBytes = text.getBytes().length;
        int numberOfBytesWritten = characterChannel.write(text, 0);

        Assert.assertEquals(numberOfBytesWritten, numberOfBytes);
        characterChannel.close();
    }
}

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

import org.ballerinalang.nativeimpl.io.channels.base.AbstractChannel;
import org.ballerinalang.nativeimpl.io.channels.base.BCharacterChannel;
import org.ballerinalang.test.nativeimpl.functions.io.MockBByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
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
        currentDirectoryPath = System.getProperty("user.dir") + "/modules/ballerina-test/target/";
    }

    @Test(description = "Read characters which does not fit to the fixed buffer limit")
    public void readFussyCharacters() throws IOException {
        int numberOfCharactersToRead = 2;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/utf8file.txt");
        AbstractChannel channel = new MockBByteChannel(byteChannel, 10);
        BCharacterChannel characterChannel = new BCharacterChannel(channel, StandardCharsets.UTF_8.name());

        String readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters.length(), numberOfCharactersToRead);

        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters.length(), numberOfCharactersToRead);

        readCharacters = characterChannel.read(numberOfCharactersToRead);
        Assert.assertEquals(readCharacters.length(), 0);
        characterChannel.close();
    }

    @Test(description = "Read characters into multiple iterations")
    public void readCharacters() throws IOException {
        int numberOfCharactersToRead = 2;
        //Number of characters in this file would be 6
        ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/6charfile.txt");
        AbstractChannel channel = new MockBByteChannel(byteChannel, 10);
        BCharacterChannel characterChannel = new BCharacterChannel(channel, StandardCharsets.UTF_8.name());

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
        ByteChannel byteChannel = TestUtil.openForWriting(currentDirectoryPath + "write.txt");
        AbstractChannel channel = new MockBByteChannel(byteChannel, 10);
        BCharacterChannel characterChannel = new BCharacterChannel(channel, StandardCharsets.UTF_8.name());

        String text = "HelloǊ";
        int numberOfBytes = text.getBytes().length;
        int numberOfBytesWritten = characterChannel.write(text, 0);

        Assert.assertEquals(numberOfBytesWritten, numberOfBytes);
        characterChannel.close();
    }
}

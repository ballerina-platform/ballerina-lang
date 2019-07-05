/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.io;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases for simple IO operation.
 * TODO: Remove once IO module unit tests are migrated.
 *
 * @since 0.995.0
 */
public class IOTest {

    private CompileResult bytesInputOutputProgramFile;
    private CompileResult characterInputOutputProgramFile;

    @BeforeClass
    public void setup() {
        bytesInputOutputProgramFile = BCompileUtil.compile("test-src/io/bytes_io.bal");
        characterInputOutputProgramFile = BCompileUtil.compile("test-src/io/char_io.bal");
    }

    @Test(description = "Test 'readBytes' function in ballerina/io package")
    public void testReadBytes() throws URISyntaxException {
        int numberOfBytesToRead = 3;
        String resourceToRead = "datafiles/io/text/6charfile.txt";
        BValueArray readBytes;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead))};
        BRunUtil.invoke(bytesInputOutputProgramFile, "initReadableChannel", args);

        //Reads the 1st three bytes "123"
        byte[] expectedBytes = "123".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        BValue[] returns = BRunUtil.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BValueArray) returns[0];
        Assert.assertEquals(readBytes.getBytes(), expectedBytes);

        //Reads the next three bytes "456"
        expectedBytes = "456".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BRunUtil.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BValueArray) returns[0];
        Assert.assertEquals(readBytes.getBytes(), expectedBytes);

        //Request for a get, the bytes will be empty
        expectedBytes = new byte[0];
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BRunUtil.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BValueArray) returns[0];
        Assert.assertEquals(readBytes.getBytes(), expectedBytes);

        BRunUtil.invoke(bytesInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test 'readCharacters' function in ballerina/io package")
    public void testReadCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/utf8file.txt";
        int numberOfCharactersToRead = 3;
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8")};
        BRunUtil.invoke(characterInputOutputProgramFile, "initReadableChannel", args);

        String expectedCharacters = "aaa";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "bbǊ";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        returns = BRunUtil.invoke(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        returns = BRunUtil.invoke(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        BRunUtil.invoke(characterInputOutputProgramFile, "closeReadableChannel");
    }

    private static String getAbsoluteFilePath(String relativePath) throws URISyntaxException {
        URL fileResource = IOTest.class.getClassLoader().getResource(relativePath);
        String pathValue = "";
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            pathValue = path.toAbsolutePath().toString();
        }
        return pathValue;
    }

}

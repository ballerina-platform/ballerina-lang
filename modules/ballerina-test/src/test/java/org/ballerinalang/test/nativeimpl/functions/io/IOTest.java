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

package org.ballerinalang.test.nativeimpl.functions.io;

import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;

/**
 * Tests I/O related functions
 */
public class IOTest {
    private CompileResult bytesInputOutputProgramFile;
    private CompileResult characterInputOutputProgramFile;
    private CompileResult recordsInputOutputProgramFile;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        bytesInputOutputProgramFile =  BTestUtils.compile("test-src/io/bytesio.bal");
        characterInputOutputProgramFile = BTestUtils.compile("test-src/io/chario.bal");
        recordsInputOutputProgramFile = BTestUtils.compile("test-src/io/recordio.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/modules/ballerina-test/target";
    }

    /**
     * Will identify the absolute path from the relative
     *
     * @param relativePath the relative file path location
     * @return the absolute path
     */
    private String getAbsoluteFilePath(String relativePath) {
        URL fileResource = BTestUtils.class.getClassLoader().getResource(relativePath);
        String path = fileResource.getPath();
        return path;
    }

    @Test(description = "Test 'readBytes' function in ballerina.io package")
    public void testReadBytes() {

        int numberOfBytesToRead = 3;
        String resourceToRead = "datafiles/io/text/6charfile.txt";
        BBlob readBytes;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r")};
        BTestUtils.invoke(bytesInputOutputProgramFile, "initFileChannel", args);

        //Reads the 1st three bytes "123"
        byte[] expectedBytes = "123".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        BValue[] returns = BTestUtils.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BBlob) returns[0];
        Assert.assertEquals(expectedBytes, readBytes.blobValue());

        //Reads the next three bytes "456"
        expectedBytes = "456".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BTestUtils.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BBlob) returns[0];
        Assert.assertEquals(expectedBytes, readBytes.blobValue());

        //Request for a read, the bytes will be empty
        expectedBytes = new byte[0];
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BTestUtils.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BBlob) returns[0];
        Assert.assertEquals(expectedBytes, readBytes.blobValue());
    }

    @Test(description = "Test 'readCharacters' function in ballerina.io package")
    public void testReadCharacters() {

        String resourceToRead = "datafiles/io/text/utf8file.txt";
        int numberOfCharactersToRead = 2;
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8")};
        BTestUtils.invoke(characterInputOutputProgramFile, "initFileChannel", args);

        //Reads the 1st three bytes "123"
        String expectedCharacters = "aa";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        BValue[] returns =  BTestUtils.invoke(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "aǊ";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        returns =  BTestUtils.invoke(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        returns =  BTestUtils.invoke(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);
    }

    @Test(description = "Test 'readRecords' function in ballerina.io package")
    public void testReadRecords() {

        String resourceToRead = "datafiles/io/records/sample.csv";
        BStringArray records;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8"),
                new BString("\n"), new BString(",")};
        BTestUtils.invoke(recordsInputOutputProgramFile, "initFileChannel", args);

        BValue[] returns =  BTestUtils.invoke(recordsInputOutputProgramFile, "readRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.length(), expectedRecordLength);

        returns =  BTestUtils.invoke(recordsInputOutputProgramFile, "readRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.length(), expectedRecordLength);

        returns =  BTestUtils.invoke(recordsInputOutputProgramFile, "readRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.length(), expectedRecordLength);

        returns =  BTestUtils.invoke(recordsInputOutputProgramFile, "readRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.length(), 0);

    }

    @Test(description = "Test 'writeBytes' function in ballerina.io package")
    public void testWriteBytes() {

        byte[] content = {-1, 46, 77, 90, 38};
        String sourceToWrite = currentDirectoryPath + "/bytesFile.txt";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w")};
        BTestUtils.invoke(bytesInputOutputProgramFile, "initFileChannel", args);

        args = new BValue[]{new BBlob(content), new BInteger(0)};
        BValue[] returns =  BTestUtils.invoke(bytesInputOutputProgramFile, "writeBytes", args);

    }

    @Test(description = "Test 'writeCharacters' function in ballerina.io package")
    public void testWriteCharacters() {

        String content = "The quick brown fox jumps over the lazy dog";
        String sourceToWrite = currentDirectoryPath + "/characterFile.txt";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8")};
        BTestUtils.invoke(characterInputOutputProgramFile, "initFileChannel", args);

        args = new BValue[]{new BString(content), new BInteger(0)};
        BValue[] returns =  BTestUtils.invoke(characterInputOutputProgramFile, "writeCharacters", args);
    }

    @Test(description = "Test 'writeRecords' function in ballerina.io package")
    public void testWriteRecords() {
        String[] content = {"Name", "Email", "Telephone"};
        BStringArray record = new BStringArray(content);
        String sourceToWrite = currentDirectoryPath + "/recordsFile.csv";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8"), new BString("\n"), new
                BString(",")};
        BTestUtils.invoke(recordsInputOutputProgramFile, "initFileChannel", args);

        args = new BValue[]{record};
        BValue[] returns =  BTestUtils.invoke(recordsInputOutputProgramFile, "writeRecord", args);

    }

}

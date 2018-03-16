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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Tests I/O related functions.
 */
public class IOTest {
    private CompileResult bytesInputOutputProgramFile;
    private CompileResult characterInputOutputProgramFile;
    private CompileResult recordsInputOutputProgramFile;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        bytesInputOutputProgramFile = BCompileUtil.compile("test-src/io/bytesio.bal");
        characterInputOutputProgramFile = BCompileUtil.compile("test-src/io/chario.bal");
        recordsInputOutputProgramFile = BCompileUtil.compile("test-src/io/recordio.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/target";
    }

    /**
     * Will identify the absolute path from the relative.
     *
     * @param relativePath the relative file path location.
     * @return the absolute path.
     */
    private String getAbsoluteFilePath(String relativePath) throws URISyntaxException {
        URL fileResource = BServiceUtil.class.getClassLoader().getResource(relativePath);
        String pathValue = "";
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            pathValue = path.toAbsolutePath().toString();
        }
        return pathValue;
    }

    /**
     * Resize a given array to fit with the specified size.
     *
     * @param source the source byte array.
     * @param size   the size which the array should be resized.
     * @return the resized byte array.
     */
    private byte[] getResizedArray(byte[] source, int size) {
        return Arrays.copyOfRange(source, 0, size);
    }

    @Test(description = "Test 'readBytes' function in ballerina.io package")
    public void testReadBytes() throws URISyntaxException {
        int numberOfBytesToRead = 3;
        String resourceToRead = "datafiles/io/text/6charfile.txt";
        BByteArray readBytes;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r")};
        BRunUtil.invoke(bytesInputOutputProgramFile, "initFileChannel", args);

        //Reads the 1st three bytes "123"
        byte[] expectedBytes = "123".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        BValue[] returns = BRunUtil.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BByteArray) returns[0];
        byte[] values = getResizedArray(readBytes.getValues(), expectedBytes.length);
        Assert.assertEquals(expectedBytes, values);

        //Reads the next three bytes "456"
        expectedBytes = "456".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BRunUtil.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BByteArray) returns[0];
        values = getResizedArray(readBytes.getValues(), expectedBytes.length);
        Assert.assertEquals(expectedBytes, values);

        //Request for a get, the bytes will be empty
        expectedBytes = new byte[3];
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BRunUtil.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BByteArray) returns[0];
        values = getResizedArray(readBytes.getValues(), expectedBytes.length);
        Assert.assertEquals(expectedBytes, values);

        BRunUtil.invoke(bytesInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readCharacters' function in ballerina.io package")
    public void testReadCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/utf8file.txt";
        int numberOfCharactersToRead = 3;
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8")};
        BRunUtil.invoke(characterInputOutputProgramFile, "initFileChannel", args);

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

        BRunUtil.invoke(characterInputOutputProgramFile, "close");

    }

    @Test(description = "Test 'readRecords' function in ballerina.io package")
    public void testReadRecords() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample.csv";
        BStringArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8"),
                new BString("\n"), new BString(",")};
        BRunUtil.invoke(recordsInputOutputProgramFile, "initFileChannel", args);

        BValue[] returns = BRunUtil.invoke(recordsInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), 0);
        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invoke(recordsInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeBytes' function in ballerina.io package")
    public void testWriteBytes() {
        byte[] content = {-1, 46, 77, 90, 38};
        String sourceToWrite = currentDirectoryPath + "/bytesFile.txt";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w")};
        BRunUtil.invoke(bytesInputOutputProgramFile, "initFileChannel", args);

        args = new BValue[]{new BByteArray(content), new BInteger(content.length), new BInteger(0)};
        BRunUtil.invoke(bytesInputOutputProgramFile, "writeBytes", args);

        BRunUtil.invoke(bytesInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeCharacters' function in ballerina.io package")
    public void testWriteCharacters() {
        String content = "The quick brown fox jumps over the lazy dog";
        String sourceToWrite = currentDirectoryPath + "/characterFile.txt";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8")};
        BRunUtil.invoke(characterInputOutputProgramFile, "initFileChannel", args);

        args = new BValue[]{new BString(content), new BInteger(0)};
        BRunUtil.invoke(characterInputOutputProgramFile, "writeCharacters", args);

        BRunUtil.invoke(characterInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeRecords' function in ballerina.io package")
    public void testWriteRecords() {
        String[] content = {"Name", "Email", "Telephone"};
        BStringArray record = new BStringArray(content);
        String sourceToWrite = currentDirectoryPath + "/recordsFile.csv";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8"), new BString("\n"), new
                BString(",")};
        BRunUtil.invoke(recordsInputOutputProgramFile, "initFileChannel", args);

        args = new BValue[]{record};
        BRunUtil.invoke(recordsInputOutputProgramFile, "writeRecord", args);

        BRunUtil.invoke(recordsInputOutputProgramFile, "close");
    }

}


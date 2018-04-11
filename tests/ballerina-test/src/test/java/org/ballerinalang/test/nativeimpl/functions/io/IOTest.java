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
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

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
        bytesInputOutputProgramFile = BCompileUtil.compileAndSetup("test-src/io/bytes_io.bal");
        characterInputOutputProgramFile = BCompileUtil.compileAndSetup("test-src/io/char_io.bal");
        recordsInputOutputProgramFile = BCompileUtil.compileAndSetup("test-src/io/record_io.bal");
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

    @Test(description = "Test 'readBytes' function in ballerina.io package")
    public void testReadBytes() throws URISyntaxException {
        int numberOfBytesToRead = 3;
        String resourceToRead = "datafiles/io/text/6charfile.txt";
        BBlob readBytes;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r")};
        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "initFileChannel", args);

        //Reads the 1st three bytes "123"
        byte[] expectedBytes = "123".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        BValue[] returns = BRunUtil.invokeStateful(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BBlob) returns[0];
        Assert.assertEquals(expectedBytes, readBytes.blobValue());

        //Reads the next three bytes "456"
        expectedBytes = "456".getBytes();
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BRunUtil.invokeStateful(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BBlob) returns[0];
        Assert.assertEquals(expectedBytes, readBytes.blobValue());

        //Request for a get, the bytes will be empty
        expectedBytes = new byte[0];
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        returns = BRunUtil.invokeStateful(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BBlob) returns[0];
        Assert.assertEquals(expectedBytes, readBytes.blobValue());

        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "close");
    }

    @Test(description = "Test permission errors in byte read operations")
    public void testByteOperationPermissionError() throws URISyntaxException {
        int numberOfBytesToRead = 3;
        String resourceToRead = "datafiles/io/text/6charfile.txt";
        BStruct readBytes;

        //Will initialize the channel with write permission
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("w")};
        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "initFileChannel", args);

        //We try to read bytes
        args = new BValue[]{new BInteger(numberOfBytesToRead)};
        BValue[] returns = BRunUtil.invokeStateful(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BStruct) returns[0];

        Assert.assertTrue(readBytes.toString().startsWith("{message:\"could not read"));

        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readCharacters' function in ballerina.io package")
    public void testReadCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/utf8file.txt";
        int numberOfCharactersToRead = 3;
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initCharacterChannel", args);

        String expectedCharacters = "aaa";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "bbǊ";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "";
        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "close");

    }

    @Test(description = "Test permission errors in byte read operations")
    public void testCharacterOperationPermissionError() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/utf8file.txt";
        int numberOfCharactersToRead = 3;
        BStruct readCharacters;

        //Will initialize the channel with write permissions
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("w"), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initCharacterChannel", args);

        args = new BValue[]{new BInteger(numberOfCharactersToRead)};
        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BStruct) returns[0];

        Assert.assertTrue(readCharacters.toString().startsWith("{message:\"Error occurred"));

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readCharacters' function in ballerina.io package")
    public void testReadAllCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/fileThatExceeds2MB.txt";
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initCharacterChannel", args);

        int expectedNumberOfCharacters = 2265223;
        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readAllCharacters");
        readCharacters = (BString) returns[0];

        String returnedString = readCharacters.stringValue();
        Assert.assertEquals(returnedString.length(), expectedNumberOfCharacters);
    }

    @Test(description = "Test 'readCharacters' function in ballerina.io package")
    public void testReadAllCharactersFromEmptyFile() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/emptyFile.txt";
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initCharacterChannel", args);

        int expectedNumberOfCharacters = 0;
        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readAllCharacters");
        readCharacters = (BString) returns[0];

        String returnedString = readCharacters.stringValue();
        Assert.assertEquals(returnedString.length(), expectedNumberOfCharacters);
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
        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "initDelimitedRecordChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), 0);
        returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "close");
    }

    @Test(description = "Test permission errors in record read operations")
    public void testRecordOperationPermissionError() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample.csv";
        BStruct records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel with write permissions
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("w"), new BString("UTF-8"),
                new BString("\n"), new BString(",")};
        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "initDelimitedRecordChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(recordsInputOutputProgramFile, "nextRecord");
        records = (BStruct) returns[0];

        Assert.assertTrue(records.toString().startsWith("{message:\"Error occurred"));

        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "close");
    }


    @Test(description = "Test 'writeBytes' function in ballerina.io package")
    public void testWriteBytes() {
        byte[] content = {-1, 46, 77, 90, 38};
        String sourceToWrite = currentDirectoryPath + "/bytesFile.txt";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w")};
        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "initFileChannel", args);

        args = new BValue[]{new BBlob(content), new BInteger(0)};
        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "writeBytes", args);

        BRunUtil.invokeStateful(bytesInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeCharacters' function in ballerina.io package")
    public void testWriteCharacters() {
        String content = "The quick brown fox jumps over the lazy dog";
        String sourceToWrite = currentDirectoryPath + "/characterFile.txt";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initCharacterChannel", args);

        args = new BValue[]{new BString(content), new BInteger(0)};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "writeCharacters", args);

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeRecords' function in ballerina.io package")
    public void testWriteRecords() {
        String[] content = {"Name", "Email", "Telephone"};
        BStringArray record = new BStringArray(content);
        String sourceToWrite = currentDirectoryPath + "/recordsFile.csv";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8"), new BString("\n"), new
                BString(",")};
        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "initDelimitedRecordChannel", args);

        args = new BValue[]{record};
        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "writeRecord", args);

        BRunUtil.invokeStateful(recordsInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readJson' function in ballerina.io package")
    public void testJsonCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/web-app.json";

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initCharacterChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readJson");
        BJSON readJson = (BJSON) returns[0];
        Assert.assertNotNull(readJson.getMessageAsString());
        Assert.assertEquals(readJson.getMessageAsString(), readFileContent(resourceToRead), "JSON content mismatch.");

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeJson' function in ballerina.io package")
    public void testWriteJsonCharacters() {
        String content = "{\n" +
                "  \"test\": { \"name\": \"Foo\" }\n" +
                "}";

        String sourceToWrite = currentDirectoryPath + "/jsonCharsFile.json";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initCharacterChannel", args);

        args = new BValue[]{new BJSON(content)};
        BValue[] result = BRunUtil.invokeStateful(characterInputOutputProgramFile, "writeJson", args);

        //Assert if there's no error return
        Assert.assertTrue(result.length == 0);

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeXml' function in ballerina.io package")
    public void testWriteXmlCharacters() {
        String content = "\t<test>\n" +
                "\t\t<name>Foo</name>\n" +
                "\t</test>";

        String sourceToWrite = currentDirectoryPath + "/xmlCharsFile.xml";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initCharacterChannel", args);

        args = new BValue[]{new BXMLItem(content)};
        BValue[] result = BRunUtil.invokeStateful(characterInputOutputProgramFile, "writeXml", args);

        //Assert if there's no error return
        Assert.assertTrue(result.length == 0);

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "close");
    }


    @Test(description = "Test 'readXml' function in ballerina.io package")
    public void testXmlCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/cd_catalog.xml";

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8")};
        BRunUtil.invokeStateful(characterInputOutputProgramFile, "initCharacterChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(characterInputOutputProgramFile, "readXml");
        BXML readJson = (BXML) returns[0];
        Assert.assertNotNull(readJson.getMessageAsString());
        Assert.assertEquals(readJson.getMessageAsString(), readFileContent(resourceToRead), "XML content mismatch.");

        BRunUtil.invokeStateful(characterInputOutputProgramFile, "close");
    }

    private String readFileContent(String filePath) throws URISyntaxException {
        Path path = Paths.get(getAbsoluteFilePath(filePath));
        StringBuilder data = new StringBuilder();
        Stream<String> lines = null;
        try {
            lines = Files.lines(path);
        } catch (IOException e) {
            return "";
        }
        lines.forEach(line -> data.append(line.trim()));
        lines.close();
        return data.toString();
    }
}


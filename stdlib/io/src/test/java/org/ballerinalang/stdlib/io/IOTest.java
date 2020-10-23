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

package org.ballerinalang.stdlib.io;

import io.ballerina.runtime.XMLFactory;
import org.apache.axiom.om.OMNode;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXMLItem;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import static org.ballerinalang.stdlib.common.CommonTestUtils.getAbsoluteFilePath;

/**
 * Tests I/O related functions.
 */
public class IOTest {

    private CompileResult bytesInputOutputProgramFile;
    private CompileResult characterInputOutputProgramFile;
    private CompileResult recordsInputOutputProgramFile;
    private CompileResult stringInputOutputProgramFile;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        bytesInputOutputProgramFile = BCompileUtil.compileOffline("test-src/io/bytes_io.bal");
        characterInputOutputProgramFile = BCompileUtil.compileOffline("test-src/io/char_io.bal");
        recordsInputOutputProgramFile = BCompileUtil.compileOffline("test-src/io/record_io.bal");
        stringInputOutputProgramFile = BCompileUtil.compileOffline("test-src/io/string_io.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/build";
    }

    @Test(description = "Test 'readBytes' function in ballerina/io package")
    public void testReadBytes() throws URISyntaxException {
        int numberOfBytesToRead = 3;
        String resourceToRead = "datafiles/io/text/6charfile.txt";
        BValueArray readBytes;

        //Will initialize the channel
        BValue[] args = { new BString(getAbsoluteFilePath(resourceToRead)) };
        BRunUtil.invoke(bytesInputOutputProgramFile, "initReadableChannel", args);

        //Reads the 1st three bytes "123"
        byte[] expectedBytes = "123".getBytes();
        args = new BValue[] { new BInteger(numberOfBytesToRead) };
        BValue[] returns = BRunUtil.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BValueArray) returns[0];
        Assert.assertEquals(readBytes.getBytes(), expectedBytes);

        //Reads the next three bytes "456"
        expectedBytes = "456".getBytes();
        args = new BValue[] { new BInteger(numberOfBytesToRead) };
        returns = BRunUtil.invoke(bytesInputOutputProgramFile, "readBytes", args);
        readBytes = (BValueArray) returns[0];
        Assert.assertEquals(readBytes.getBytes(), expectedBytes);

        //Request for a get, the bytes will be empty
        expectedBytes = new byte[0];
        args = new BValue[] { new BInteger(numberOfBytesToRead) };
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
        BValue[] args = { new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initReadableChannel", args);

        String expectedCharacters = "aaa";
        args = new BValue[] { new BInteger(numberOfCharactersToRead) };
        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "bbǊ";
        args = new BValue[] { new BInteger(numberOfCharactersToRead) };
        returns = BRunUtil.invoke(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        expectedCharacters = "";
        args = new BValue[] { new BInteger(numberOfCharactersToRead) };
        returns = BRunUtil.invoke(characterInputOutputProgramFile, "readCharacters", args);
        readCharacters = (BString) returns[0];

        Assert.assertEquals(readCharacters.stringValue(), expectedCharacters);

        BRunUtil.invoke(characterInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test 'readCharacters' function in ballerina/io package")
    public void testReadAllCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/fileThatExceeds2MB.txt";
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = { new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initReadableChannel", args);

        int expectedNumberOfCharacters = 2265223;
        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "readAllCharacters");
        readCharacters = (BString) returns[0];

        //getting the result string and filtering the CR characters which were added when running in windows
        String returnedString = readCharacters.stringValue().replaceAll("\r", "");
        Assert.assertEquals(returnedString.length(), expectedNumberOfCharacters);
    }

    @Test(description = "Test 'readCharacters' function in ballerina/io package")
    public void testReadAllCharactersFromEmptyFile() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/emptyFile.txt";
        BString readCharacters;

        //Will initialize the channel
        BValue[] args = { new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initReadableChannel", args);

        int expectedNumberOfCharacters = 0;
        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "readAllCharacters");
        readCharacters = (BString) returns[0];

        String returnedString = readCharacters.stringValue();
        Assert.assertEquals(returnedString.length(), expectedNumberOfCharacters);
    }

    @Test(description = "Test 'readRecords' function in ballerina/io package")
    public void testReadRecords() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample.csv";
        BValueArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {
                new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8"), new BString("\n"),
                new BString(",")
        };
        BRunUtil.invoke(recordsInputOutputProgramFile, "initReadableChannel", args);

        BValue[] returns = BRunUtil.invoke(recordsInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "nextRecord");
        BError error = (BError) returns[0];
        Assert.assertEquals(error.getMessage(), "EoF when reading from the channel");
        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invoke(recordsInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test 'writeBytes' function in ballerina/io package")
    public void testWriteBytes() {
        byte[] content = { -1, 46, 77, 90, 38 };
        String sourceToWrite = currentDirectoryPath + "/bytesFile.txt";

        //Will initialize the channel
        BValue[] args = { new BString(sourceToWrite) };
        BRunUtil.invoke(bytesInputOutputProgramFile, "initWritableChannel", args);

        args = new BValue[] { new BValueArray(content), new BInteger(0) };
        BRunUtil.invoke(bytesInputOutputProgramFile, "writeBytes", args);

        BRunUtil.invoke(bytesInputOutputProgramFile, "closeWritableChannel");
    }

    @Test(description = "Test 'writeCharacters' function in ballerina/io package")
    public void testWriteCharacters() {
        String content = "The quick brown fox jumps over the lazy dog";
        String sourceToWrite = currentDirectoryPath + "/characterFile.txt";

        //Will initialize the channel
        BValue[] args = { new BString(sourceToWrite), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initWritableChannel", args);

        args = new BValue[] { new BString(content), new BInteger(0) };
        BRunUtil.invoke(characterInputOutputProgramFile, "writeCharacters", args);

        BRunUtil.invoke(characterInputOutputProgramFile, "closeWritableChannel");
    }

    @Test(description = "Test 'write' function with `append = true` in ballerina/io package")
    public void appendCharacters() throws IOException {
        String initialContent = "Hi, I'm the initial content. ";
        String appendingContent = "Hi, I was appended later. ";
        String sourceToWrite = currentDirectoryPath + "/appendCharacterFile.txt";
        //Will initialize the writable channel
        BValue[] args = { new BString(sourceToWrite), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initWritableChannel", args);

        // Write chars to file
        args = new BValue[] { new BString(initialContent), new BInteger(0) };
        BRunUtil.invoke(characterInputOutputProgramFile, "writeCharacters", args);

        //Will initialize the writable channel to append characters
        args = new BValue[] { new BString(sourceToWrite), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initWritableChannelToAppend", args);

        // Append chars to file
        args = new BValue[] { new BString(appendingContent), new BInteger(0) };
        BRunUtil.invoke(characterInputOutputProgramFile, "appendCharacters", args);

        Assert.assertEquals(readFile(sourceToWrite), initialContent + appendingContent);

        BRunUtil.invoke(characterInputOutputProgramFile, "closeWritableChannel");
        BRunUtil.invoke(characterInputOutputProgramFile, "closeWritableChannelToAppend");
        deleteFile(sourceToWrite);
    }

    @Test(description = "Test 'writeRecords' function in ballerina/io package")
    public void testWriteRecords() throws URISyntaxException {
        String[] content = { "Name", "Email", "Telephone" };
        BValueArray record = new BValueArray(content);
        String sourceToWrite = currentDirectoryPath + "/recordsFile.csv";
        //Will initialize the channel
        BValue[] args = {
                new BString(sourceToWrite), new BString("UTF-8"), new BString("\n"), new BString(",")
        };
        BRunUtil.invoke(recordsInputOutputProgramFile, "initWritableChannel", args);
        BValue[] recordArgs = new BValue[] { record };
        BRunUtil.invoke(recordsInputOutputProgramFile, "writeRecord", recordArgs);
        BRunUtil.invoke(recordsInputOutputProgramFile, "closeWritableChannel");

        BRunUtil.invoke(recordsInputOutputProgramFile, "initReadableChannel", args);
        BValue[] returns = BRunUtil.invoke(recordsInputOutputProgramFile, "nextRecord");
        BValueArray records = (BValueArray) returns[0];
        Assert.assertEquals(records.getStringArray()[0], content[0], "Content mismatch in the returned array");
        Assert.assertEquals(records.getStringArray()[1], content[1], "Content mismatch in the returned array");
        Assert.assertEquals(records.getStringArray()[2], content[2], "Content mismatch in the returned array");
        Assert.assertEquals(records.size(), 3);
        returns = BRunUtil.invoke(recordsInputOutputProgramFile, "hasNextRecord");
        BBoolean hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Expecting one record but there are more.");
    }

    @Test(description = "Test 'readJson' function in ballerina/io package")
    public void testJsonCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/web-app.json";

        //Will initialize the channel
        BValue[] args = { new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initReadableChannel", args);

        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "readJson");
        Assert.assertNotNull(returns[0].stringValue());
        Assert.assertEquals(returns[0].stringValue().replace(", ", ","), readFileContent(resourceToRead),
                "JSON content mismatch.");

        BRunUtil.invoke(characterInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test 'writeJson' function in ballerina/io package")
    public void testWriteJsonCharacters() {
        String content = "{\n" + "  \"test\": { \"name\": \"Foo\" }\n" + "}";

        String sourceToWrite = currentDirectoryPath + "/jsonCharsFile.json";

        //Will initialize the channel
        BValue[] args = { new BString(sourceToWrite), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initWritableChannel", args);

        args = new BValue[] { JsonParser.parse(content) };
        BValue[] result = BRunUtil.invoke(characterInputOutputProgramFile, "writeJson", args);

        //Assert if there's no error return
        Assert.assertNull(result[0]);

        BRunUtil.invoke(characterInputOutputProgramFile, "closeWritableChannel");
    }

    @Test(description = "Test double byte unicode write function in ballerina/io package")
    public void testWriteHigherUnicodeRangeJsonCharacters() {
        String sourceToWrite = currentDirectoryPath + "/unicode.json";
        //Will initialize the channel
        BValue[] args = { new BString(sourceToWrite), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initWritableChannel", args);
        BValue[] result = BRunUtil.invoke(characterInputOutputProgramFile, "writeJsonWithHigherUnicodeRange");
        //Assert if there's no error return
        Assert.assertNull(result[0]);
        BRunUtil.invoke(characterInputOutputProgramFile, "closeWritableChannel");
        try {
            String content = "{\"loop\":\"É\"}";
            Assert.assertEquals(content,
                    new String(Files.readAllBytes(Paths.get(sourceToWrite)), StandardCharsets.UTF_8).trim());
        } catch (IOException e) {
            Assert.fail("Unable to read from file", e);
        }
    }

    @Test(description = "Test 'writeXml' function in ballerina/io package")
    public void testWriteXmlCharacters() throws XMLStreamException {
        String content = "<test>\n" + "\t\t<name>Foo</name>\n" + "\t</test>";

        String sourceToWrite = currentDirectoryPath + "/xmlCharsFile.xml";

        //Will initialize the channel
        BValue[] args = { new BString(sourceToWrite), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initWritableChannel", args);
        OMNode omNode = (OMNode) XMLFactory.stringToOM(content);
        args = new BValue[] { new BXMLItem(omNode) };
        BValue[] result = BRunUtil.invoke(characterInputOutputProgramFile, "writeXml", args);

        //Assert if there's no error return
        Assert.assertNull(result[0]);

        BRunUtil.invoke(characterInputOutputProgramFile, "closeWritableChannel");
    }

    @Test(description = "Test 'readXml' function in ballerina/io package")
    public void testXmlCharacters() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/cd_catalog.xml";

        //Will initialize the channel
        BValue[] args = { new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initReadableChannel", args);

        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "readXml");
        Assert.assertNotNull(returns[0].stringValue());
        Assert.assertEquals(returns[0].stringValue(), readFileContent(resourceToRead), "XML content mismatch.");

        BRunUtil.invoke(characterInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test function to convert string to json")
    public void convertStringToJsonTest() throws URISyntaxException {
        String content = "{\n" + "  \"test\": { \"name\": \"Foo\" }\n" + "}";
        BValue[] args = { new BString(content), new BString("UTF-8") };
        BValue[] result = BRunUtil.invoke(stringInputOutputProgramFile, "getJson", args);
        Assert.assertTrue(result[0].stringValue().contains("Foo"));
    }

    @Test(description = "Test function to convert xml to string")
    public void convertStringToXmlTest() throws URISyntaxException {
        String content = "\t<test>\n" + "\t\t<name>Foo</name>\n" + "\t</test>";
        BValue[] args = { new BString(content), new BString("UTF-8") };
        BValue[] result = BRunUtil.invoke(stringInputOutputProgramFile, "getXml", args);
        Assert.assertTrue(result[0].stringValue().contains("Foo"));
    }

    @Test(description = "Test 'readProperty' function in ballerina/io package")
    public void testReadAvailableProperty() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/person.properties";
        BString readCharacters;

        //Will initialize the channel
        BValue[] initArgs = { new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initReadableChannel", initArgs);

        BValue[] testArgs = { new BString("name")};
        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "readAvailableProperty", testArgs);
        readCharacters = (BString) returns[0];

        String returnedString = readCharacters.stringValue();
        String expectedString = "John Smith";
        Assert.assertEquals(returnedString, expectedString);

        BRunUtil.invoke(characterInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test 'readAllProperties' function in ballerina/io package")
    public void testAllProperties() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/person.properties";
        BBoolean succeed;

        //Will initialize the channel
        BValue[] initArgs = { new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initReadableChannel", initArgs);

        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "readAllProperties");
        succeed = (BBoolean) returns[0];
        Assert.assertTrue(succeed.booleanValue());

        BRunUtil.invoke(characterInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Negative test for 'readProperty' function in ballerina/io package")
    public void testReadUnavailableProperty() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/person.properties";
        BBoolean succeed;

        //Will initialize the channel
        BValue[] initArgs = { new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initReadableChannel", initArgs);

        BValue[] testArgs = { new BString("key")};
        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "readUnavailableProperty", testArgs);
        succeed = (BBoolean) returns[0];
        Assert.assertTrue(succeed.booleanValue());

        BRunUtil.invoke(characterInputOutputProgramFile, "closeReadableChannel");
    }

    @Test(description = "Test 'writeProperties' function in ballerina/io package")
    public void testWriteProperties() throws URISyntaxException {
        String sourceToWrite = currentDirectoryPath + "/tmp_person.properties";
        BBoolean succeed;

        //Will initialize the channel
        BValue[] args = { new BString(sourceToWrite), new BString("UTF-8") };
        BRunUtil.invoke(characterInputOutputProgramFile, "initWritableChannel", args);

        BValue[] returns = BRunUtil.invoke(characterInputOutputProgramFile, "writePropertiesFromMap");
        succeed = (BBoolean) returns[0];
        Assert.assertTrue(succeed.booleanValue());

        BRunUtil.invoke(characterInputOutputProgramFile, "closeWritableChannel");
    }

    private String readFileContent(String filePath) throws URISyntaxException {
        Path path = Paths.get(getAbsoluteFilePath(filePath));
        StringBuilder data = new StringBuilder();
        Stream<String> lines;
        try {
            lines = Files.lines(path);
        } catch (IOException e) {
            return "";
        }
        lines.forEach(line -> data.append(line.trim()));
        lines.close();
        return data.toString();
    }

    private String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    private void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}

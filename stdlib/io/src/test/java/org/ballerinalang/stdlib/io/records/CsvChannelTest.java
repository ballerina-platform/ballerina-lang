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

package org.ballerinalang.stdlib.io.records;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tests the CSV channel with the specified values.
 */
public class CsvChannelTest {
    private CompileResult csvInputOutputProgramFile;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        csvInputOutputProgramFile = BCompileUtil.compileOffline("test-src/io/csv_io.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/build";
    }

    @Test(description = "Test 'readDefaultCSVRecords'")
    public void readCsvTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample.csv";
        BValueArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {
                new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8"), new BString(",")
        };
        BRunUtil.invoke(csvInputOutputProgramFile, "initReadableCsvChannel", args);

        BValue[] returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        BError error = (BError) returns[0];
        Assert.assertEquals(error.getMessage(), "EoF when reading from the channel");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invoke(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readDefaultCSVRecords'")
    public void openAndReadCsvTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample.csv";
        BValueArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {
                new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8"), new BString(","),
                new BInteger(0)
        };
        BRunUtil.invoke(csvInputOutputProgramFile, "initOpenCsvChannel", args);

        BValue[] returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        BError error = (BError) returns[0];
        Assert.assertEquals(error.getMessage(), "EoF when reading from the channel");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invoke(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readColonDelimitedRecords'")
    public void openAndReadColonDelimitedFileTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sampleWithColon.txt";
        BValueArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {
                new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8"), new BString(":"),
                new BInteger(0)
        };
        BRunUtil.invoke(csvInputOutputProgramFile, "initOpenCsvChannel", args);

        BValue[] returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), 0);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invoke(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readCSVWithHeaders'")
    public void openAndReadCsvWithHeadersTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sampleWithHeader.csv";
        BValueArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {
                new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8"), new BString(","),
                new BInteger(1)
        };
        BRunUtil.invoke(csvInputOutputProgramFile, "initOpenCsvChannel", args);

        BValue[] returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Expecting more records");

        BRunUtil.invoke(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readRfcCSVRecords'")
    public void readRfcTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sampleRfc.csv";
        BValueArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {
                new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8"), new BString(",")
        };
        BRunUtil.invoke(csvInputOutputProgramFile, "initReadableCsvChannel", args);

        BValue[] returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        Assert.assertEquals(records.stringValue(), "[\"User1,12\", \"WSO2\", \"07xxxxxx\"]");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        Assert.assertEquals(records.stringValue(), "[\"User4\", \"\", \"123xxxxx\"]");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        BError error = (BError) returns[0];
        Assert.assertEquals(error.getMessage(), "EoF when reading from the channel");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invoke(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readTdfCSVRecords'")
    public void readTdfTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sampleTdf.tsv";
        BValueArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {
                new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8"), new BString("\t")
        };
        BRunUtil.invoke(csvInputOutputProgramFile, "initReadableCsvChannel", args);

        BValue[] returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        records = (BValueArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        BError error = (BError) returns[0];
        Assert.assertEquals(error.getMessage(), "EoF when reading from the channel");

        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invoke(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeDefaultCSVRecords'")
    public void testWriteDefaultCsv() {
        String[] content1 = { "Name", "Email", "Telephone" };
        String[] content2 = { "Foo,12", "foo@ballerina/io", "332424242" };
        String sourceToWrite = currentDirectoryPath + "/recordsDefault.csv";

        //Will initialize the channel
        BValue[] params = { new BString(sourceToWrite), new BString("UTF-8"), new BString(",") };

        BRunUtil.invoke(csvInputOutputProgramFile, "initWritableCsvChannel", params);
        BRunUtil.invoke(csvInputOutputProgramFile, "writeRecord", new BValue[] { new BValueArray(content1) });
        BRunUtil.invoke(csvInputOutputProgramFile, "writeRecord", new BValue[] { new BValueArray(content2) });

        BRunUtil.invoke(csvInputOutputProgramFile, "initReadableCsvChannel", params);
        BValue[] returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        validateCsvRecord(content1, returns, true);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        validateCsvRecord(content2, returns, false);

        BRunUtil.invoke(csvInputOutputProgramFile, "close");
    }

    private void validateCsvRecord(String[] content, BValue[] returns, boolean expectMore) {
        BValueArray records = (BValueArray) returns[0];
        Assert.assertEquals(records.getString(1), content[1]);
        Assert.assertEquals(records.getString(2), content[2]);
        Assert.assertEquals(records.size(), content.length);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "hasNextRecord");
        BBoolean hasNextRecord = (BBoolean) returns[0];
        Assert.assertEquals(hasNextRecord.booleanValue(), expectMore);
    }

    @Test(description = "Check with user defined separator")
    public void testReadWriteCustomSeparator() {
        String[][] data = {
                { "1", "James", "10000" }, { "2", "Nathan", "150000" }, { "3", "Ronald", "120000" },
                { "4", "Roy", "6000" }, { "5", "Oliver", "1100000" }
        };
        String sourceToWrite = currentDirectoryPath + "/recordsUserDefine.csv";
        BValue[] params = { new BString(sourceToWrite), new BString("UTF-8"), new BString("~") };
        BRunUtil.invoke(csvInputOutputProgramFile, "initWritableCsvChannel", params);
        for (String[] rec : data) {
            BRunUtil.invoke(csvInputOutputProgramFile, "writeRecord", new BValue[] { new BValueArray(rec) });
        }

        BRunUtil.invoke(csvInputOutputProgramFile, "initReadableCsvChannel", params);
        BValue[] returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        validateCsvRecord(data[0], returns, true);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        validateCsvRecord(data[1], returns, true);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        validateCsvRecord(data[2], returns, true);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        validateCsvRecord(data[3], returns, true);
        returns = BRunUtil.invoke(csvInputOutputProgramFile, "nextRecord");
        validateCsvRecord(data[4], returns, false);

        BRunUtil.invoke(csvInputOutputProgramFile, "close");

        try {
            List<String> lines = Files.readAllLines(Paths.get(sourceToWrite));
            Assert.assertEquals(lines.get(0), "1~James~10000");
            Assert.assertEquals(lines.get(2), "3~Ronald~120000");
            Assert.assertEquals(lines.get(4), "5~Oliver~1100000");
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test 'writeTdfCSVRecords'")
    public void testWriteTdf() {
        String[] content = { "Name", "Email", "Telephone" };
        BValueArray record = new BValueArray(content);
        String sourceToWrite = currentDirectoryPath + "/recordsTdf.csv";

        //Will initialize the channel
        BValue[] args = {
                new BString(sourceToWrite), new BString("UTF-8"), new BString("\t")
        };
        BRunUtil.invoke(csvInputOutputProgramFile, "initWritableCsvChannel", args);

        args = new BValue[] { record };
        BRunUtil.invoke(csvInputOutputProgramFile, "writeRecord", args);

        BRunUtil.invoke(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test successful data load")
    public void loadRecordFromFile() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample5.csv";
        BValue[] args = {
                new BString(getAbsoluteFilePath(resourceToRead)), new BString("UTF-8"), new BString(",")
        };
        final BValue[] result = BRunUtil.invoke(csvInputOutputProgramFile, "getTable", args);
        final BFloat totalSalary = (BFloat) result[0];
        Assert.assertEquals(totalSalary.floatValue(), 60001.00d);
    }

    @Test(description = "Test successful data load will nill values")
    public void getTableWithNull() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample6.csv";
        BValue[] args = { new BString(getAbsoluteFilePath(resourceToRead)) };
        final BValue[] result = BRunUtil.invoke(csvInputOutputProgramFile, "getTableWithNill", args);
        BString names = (BString) result[0];
        BString departments = (BString) result[1];
        Assert.assertEquals(names.toString(), "Person1Person2Person3");
        Assert.assertEquals(departments.toString(), "EngMrk-1");
    }

    @Test(description = "Test successful data load will nill values")
    public void getTableWithHeader() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample7.csv";
        BValue[] args = { new BString(getAbsoluteFilePath(resourceToRead)) };
        final BValue[] result = BRunUtil.invoke(csvInputOutputProgramFile, "getTableWithHeader", args);
        BValueArray values = (BValueArray) result[0];
        Assert.assertEquals(values.size(), 7);
        String[] expectedOutput = {
                "Common App ID", "11111111", "22222222", "33333333", "44444444", "55555555", "55555556"
        };
        for (int i = 0; i < expectedOutput.length; i++) {
            Assert.assertEquals(values.getStringArray()[i], expectedOutput[i]);
        }
    }

    private String getAbsoluteFilePath(String relativePath) throws URISyntaxException {
        URL fileResource = BCompileUtil.class.getClassLoader().getResource(relativePath);
        String pathValue = "";
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            pathValue = path.toAbsolutePath().toString();
        }
        return pathValue;
    }

}

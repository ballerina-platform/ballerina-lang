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

package org.ballerinalang.test.nativeimpl.functions.io.records;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
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

/**
 * Tests the CSV channel with the specified values.
 */
public class CsvChannelTest {
    private CompileResult csvInputOutputProgramFile;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        csvInputOutputProgramFile = BCompileUtil.compileAndSetup("test-src/io/csv_io.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/target";
    }

    private String getAbsoluteFilePath(String relativePath) throws URISyntaxException {
        URL fileResource = BServiceUtil.class.getClassLoader().getResource(relativePath);
        String pathValue = "";
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            pathValue = path.toAbsolutePath().toString();
        }
        return pathValue;
    }

    @Test(description = "Test 'readDefaultCSVRecords'")
    public void readCsvTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample.csv";
        BStringArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8"),
                new BString(",")};
        BRunUtil.invokeStateful(csvInputOutputProgramFile, "initCSVChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), 0);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invokeStateful(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readDefaultCSVRecords'")
    public void openAndReadCsvTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample.csv";
        BStringArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8"),
                new BString(",")};
        BRunUtil.invokeStateful(csvInputOutputProgramFile, "initOpenCsv", args);

        BValue[] returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), 0);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invokeStateful(csvInputOutputProgramFile, "close");
    }



    @Test(description = "Test 'readRfcCSVRecords'")
    public void readRfcTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sampleRfc.csv";
        BStringArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8"),
                new BString(",")};
        BRunUtil.invokeStateful(csvInputOutputProgramFile, "initCSVChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), 0);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invokeStateful(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'readTdfCSVRecords'")
    public void readTdfTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sampleTdf.tsv";
        BStringArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead)), new BString("r"), new BString("UTF-8"),
                new BString("\t")};
        BRunUtil.invokeStateful(csvInputOutputProgramFile, "initCSVChannel", args);

        BValue[] returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), expectedRecordLength);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertTrue(hasNextRecord.booleanValue(), "Expecting more records");

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];

        Assert.assertEquals(records.size(), expectedRecordLength);

        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "nextRecord");
        records = (BStringArray) returns[0];
        Assert.assertEquals(records.size(), 0);
        returns = BRunUtil.invokeStateful(csvInputOutputProgramFile, "hasNextRecord");
        hasNextRecord = (BBoolean) returns[0];
        Assert.assertFalse(hasNextRecord.booleanValue(), "Not expecting anymore records");

        BRunUtil.invokeStateful(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeDefaultCSVRecords'")
    public void testWriteDefaultCsv() {
        String[] content = {"Name", "Email", "Telephone"};
        BStringArray record = new BStringArray(content);
        String sourceToWrite = currentDirectoryPath + "/recordsDefault.csv";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8"),
                new BString(",")};
        BRunUtil.invokeStateful(csvInputOutputProgramFile, "initCSVChannel", args);

        args = new BValue[]{record};
        BRunUtil.invokeStateful(csvInputOutputProgramFile, "writeRecord", args);

        BRunUtil.invokeStateful(csvInputOutputProgramFile, "close");
    }

    @Test(description = "Test 'writeTdfCSVRecords'")
    public void testWriteTdf() {
        String[] content = {"Name", "Email", "Telephone"};
        BStringArray record = new BStringArray(content);
        String sourceToWrite = currentDirectoryPath + "/recordsTdf.csv";

        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite), new BString("w"), new BString("UTF-8"),
                new BString("\t")};
        BRunUtil.invokeStateful(csvInputOutputProgramFile, "initCSVChannel", args);

        args = new BValue[]{record};
        BRunUtil.invokeStateful(csvInputOutputProgramFile, "writeRecord", args);

        BRunUtil.invokeStateful(csvInputOutputProgramFile, "close");
    }

}

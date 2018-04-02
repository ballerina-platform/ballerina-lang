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
    private CompileResult recordsInputOutputProgramFile;

    @BeforeClass
    public void setup() {
        recordsInputOutputProgramFile = BCompileUtil.compile("test-src/io/record_io.bal");
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

    @Test(description = "Test successful data load")
    public void readDefaultCsvTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample.csv";
        BStringArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead))};
        BRunUtil.invoke(recordsInputOutputProgramFile, "initDefaultCsv", args);

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

    @Test(description = "Test successful data load")
    public void readRfcTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sampleRfc.csv";
        BStringArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead))};
        BRunUtil.invoke(recordsInputOutputProgramFile, "initRfc", args);

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

    @Test(description = "Test successful data load")
    public void readTdfTest() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sampleTdf.tsv";
        BStringArray records;
        BBoolean hasNextRecord;
        int expectedRecordLength = 3;

        //Will initialize the channel
        BValue[] args = {new BString(getAbsoluteFilePath(resourceToRead))};
        BRunUtil.invoke(recordsInputOutputProgramFile, "initTdf", args);

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

}

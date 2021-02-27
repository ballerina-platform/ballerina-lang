/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.stdlib.io.data;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.ByteOrder;

/**
 * Test data io operations.
 */
public class DataChannelTest {
    private CompileResult dataChannel;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        dataChannel = BCompileUtil.compileOffline("test-src/io/data_io.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/build";
    }

    @Test(description = "read and write fixed size integers", dataProvider = "Endianness")
    public void processFixedInteger(ByteOrder order) {
        String sourceToWrite = currentDirectoryPath + "/integer.bin";
        //Will initialize the channel
        int value = 123;
        BValue[] args = {new BInteger(value),
                new BString(sourceToWrite),
                new BString(order.toString())};
        BRunUtil.invoke(dataChannel, "testWriteFixedSignedInt", args);

        BValue[] args2 = {new BString(sourceToWrite), new BString(order.toString())};
        BValue[] result = BRunUtil.invoke(dataChannel, "testReadFixedSignedInt", args2);

        Assert.assertEquals(value, ((BInteger) result[0]).intValue());
    }

    @Test(description = "read and write var integers", dataProvider = "Endianness")
    public void processVarInteger(ByteOrder order) {
        String sourceToWrite = currentDirectoryPath + "/varint.bin";
        //Will initialize the channel
        int value = 2;
        BValue[] args = {new BInteger(value), new BString(sourceToWrite), new BString(order.toString())};
        BRunUtil.invoke(dataChannel, "testWriteVarInt", args);

        BValue[] args2 = {new BString(sourceToWrite), new BString(order.toString())};
        BValue[] result = BRunUtil.invoke(dataChannel, "testReadVarInt", args2);

        Assert.assertEquals(value, ((BInteger) result[0]).intValue());
    }

    @Test(description = "read and write fixed size float values", dataProvider = "Endianness")
    public void processFixedFloat(ByteOrder order) {
        String sourceToWrite = currentDirectoryPath + "/float.bin";
        //Will initialize the channel
        double value = 1359494.69;
        BValue[] args = {new BFloat(value), new BString(sourceToWrite), new BString(order.toString())};
        BRunUtil.invoke(dataChannel, "testWriteFixedFloat", args);

        BValue[] args2 = {new BString(sourceToWrite), new BString(order.toString())};
        BValue[] result = BRunUtil.invoke(dataChannel, "testReadFixedFloat", args2);

        Assert.assertEquals(value, ((BFloat) result[0]).floatValue());
    }

    @Test(description = "read and write bool", dataProvider = "Endianness")
    public void processBool(ByteOrder order) {
        String sourceToWrite = currentDirectoryPath + "/boolean.bin";
        BValue[] args = {new BBoolean(false), new BString(sourceToWrite), new BString(order.toString())};
        BRunUtil.invoke(dataChannel, "testWriteBool", args);

        BValue[] args2 = {new BString(sourceToWrite), new BString(order.toString())};
        BValue[] result = BRunUtil.invoke(dataChannel, "testReadBool", args2);

        Assert.assertEquals(false, ((BBoolean) result[0]).booleanValue());
    }

    @Test(description = "read and write string", dataProvider = "Endianness")
    public void processString(ByteOrder order) {
        String sourceToWrite = currentDirectoryPath + "/string.bin";
        String content = "Ballerina";
        String encoding = "UTF-8";
        BValue[] args = {new BString(sourceToWrite), new BString(content), new BString(encoding),
                new BString(order.toString())};
        BRunUtil.invoke(dataChannel, "testWriteString", args);

        BValue[] args2 = {new BString(sourceToWrite), new BInteger(content.getBytes().length), new BString(encoding),
                new BString(order.toString())};
        BValue[] result = BRunUtil.invoke(dataChannel, "testReadString", args2);

        Assert.assertEquals(content, result[0].stringValue());
    }

    @DataProvider(name = "Endianness")
    public Object[][] endianness() {
        return new Object[][]{{ByteOrder.BIG_ENDIAN}, {ByteOrder.LITTLE_ENDIAN}};
    }

}

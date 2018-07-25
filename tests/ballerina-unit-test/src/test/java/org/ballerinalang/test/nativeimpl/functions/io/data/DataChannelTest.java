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

package org.ballerinalang.test.nativeimpl.functions.io.data;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test  io operations.
 */
public class DataChannelTest {
    private CompileResult dataChannel;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        dataChannel = BCompileUtil.compileAndSetup("test-src/io/data_io.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/target";
    }

    @Test(description = "read and write fixed size integers")
    public void processFixedInteger() {
        String sourceToWrite = currentDirectoryPath + "/integer.bin";
        //Will initialize the channel
        int value = 123;
        BValue[] args = {new BInteger(value), new BString(sourceToWrite)};
        BRunUtil.invokeStateful(dataChannel, "testWriteFixedSignedInt", args);

        BValue[] args2 = {new BString(sourceToWrite)};
        BValue[] result = BRunUtil.invokeStateful(dataChannel, "testReadFixedSignedInt", args2);

        Assert.assertEquals(value, ((BInteger) result[0]).intValue());
    }

    @Test(description = "read and write var integers")
    public void processVarInteger() {
        String sourceToWrite = currentDirectoryPath + "/varint.bin";
        //Will initialize the channel
        int value = 2;
        BValue[] args = {new BInteger(value), new BString(sourceToWrite)};
        BRunUtil.invokeStateful(dataChannel, "testWriteVarInt", args);

        BValue[] args2 = {new BString(sourceToWrite)};
        BValue[] result = BRunUtil.invokeStateful(dataChannel, "testReadVarInt", args2);

        Assert.assertEquals(value, ((BInteger) result[0]).intValue());
    }

    @Test(description = "read and write fixed size float values")
    public void processFixedFloat() {
        String sourceToWrite = currentDirectoryPath + "/float.bin";
        //Will initialize the channel
        double value = 1359494.69;
        BValue[] args = {new BFloat(value), new BString(sourceToWrite)};
        BRunUtil.invokeStateful(dataChannel, "testWriteFixedFloat", args);

        BValue[] args2 = {new BString(sourceToWrite)};
        BValue[] result = BRunUtil.invokeStateful(dataChannel, "testReadFixedFloat", args2);

        Assert.assertEquals(value, ((BFloat) result[0]).floatValue());
    }

    @Test(description = "read and write bool")
    public void processBool() {
        String sourceToWrite = currentDirectoryPath + "/boolean.bin";
        BValue[] args = {new BBoolean(false), new BString(sourceToWrite)};
        BRunUtil.invokeStateful(dataChannel, "testWriteBool", args);

        BValue[] args2 = {new BString(sourceToWrite)};
        BValue[] result = BRunUtil.invokeStateful(dataChannel, "testReadBool", args2);

        Assert.assertEquals(false, ((BBoolean) result[0]).booleanValue());
    }

    @Test(description = "read and write string")
    public void processString() {
        String sourceToWrite = currentDirectoryPath + "/string.bin";
        String content = "Ballerina";
        String encoding = "UTF-8";
        BValue[] args = {new BString(sourceToWrite), new BString(content), new BString(encoding)};
        BRunUtil.invokeStateful(dataChannel, "testWriteString", args);

        BValue[] args2 = {new BString(sourceToWrite), new BInteger(content.getBytes().length), new BString(encoding)};
        BValue[] result = BRunUtil.invokeStateful(dataChannel, "testReadString", args2);

        Assert.assertEquals(content, result[0].stringValue());
    }
}

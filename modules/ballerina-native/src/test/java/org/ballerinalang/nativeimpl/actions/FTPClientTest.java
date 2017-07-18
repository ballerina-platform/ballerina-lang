/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.actions;

import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test cases for jms client.
 */
public class FTPClientTest {
    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/ftpClientConnectorTest.bal");
        BuiltInNativeConstructLoader.loadConstructs();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: ballerina.lang.errors:Error.*")
    public void testCopy() {
        String sourcePath = new File("temp/copy-file.txt").getAbsolutePath();
        String destPath = new File("temp/coppied-file.txt").getAbsolutePath();
        BValue[] args = { new BString((new File(sourcePath).getAbsolutePath())),
                          new BString((new File(destPath).getAbsolutePath())) };
        BLangFunctions.invokeNew(programFile, "testCopy", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: ballerina.lang.errors:Error.*")
    public void testMove() {
        String sourcePath = new File("temp/move-file.txt").getAbsolutePath();
        String destPath = new File("temp/test/move-file.txt").getAbsolutePath();
        BValue[] args = { new BString(sourcePath), new BString(destPath) };
        BLangFunctions.invokeNew(programFile, "testMove", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: ballerina.lang.errors:Error.*")
    public void testDelete() {
        String sourcePath = new File("temp/move-file.txt").getAbsolutePath();
        BValue[] args = { new BString(sourcePath) };
        BLangFunctions.invokeNew(programFile, "testDelete", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: ballerina.lang.errors:Error.*")
    public void testWrite() {
        String targetPath = new File("temp/write-file.txt").getAbsolutePath();
        byte[] content = "Sample Text".getBytes();
        BBlob byteContent = new BBlob(content);
        BValue[] args = { byteContent, new BString(targetPath) };
        BLangFunctions.invokeNew(programFile, "testWrite", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: ballerina.lang.errors:Error.*")
    public void testRead() {
        String targetPath = new File("temp/read-file.txt").getAbsolutePath();
        byte[] content = "Sample Text".getBytes();
        BValue[] args = {new BString(targetPath), new BBlob(content)};
        BValue[] results = BLangFunctions.invokeNew(programFile, "testRead", args);
        Assert.assertEquals(content, ((BBlob) results[0]).blobValue(), "Not read properly");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: ballerina.lang.errors:Error.*")
    public void testCreate() {
        String targetPath = new File("temp/create-file.txt").getAbsolutePath();
        BValue[] args = { new BString(targetPath) };
        BLangFunctions.invokeNew(programFile, "testCreate", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: ballerina.lang.errors:Error.*")
    public void testIsExist() {
        String targetPath = new File("temp/existing-file.txt").getAbsolutePath();
        BValue[] args = { new BString(targetPath) };
        BLangFunctions.invokeNew(programFile, "testIsExist", args);
    }

}

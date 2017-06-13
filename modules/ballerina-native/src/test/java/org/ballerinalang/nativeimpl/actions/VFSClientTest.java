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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test cases for jms client.
 */
public class VFSClientTest {
    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/vfsClientConnectorTest.bal");
        BuiltInNativeConstructLoader.loadConstructs();
    }

    @Test
    public void testCopy() {
        String sourcePath = new File("temp/copy-file.txt").getAbsolutePath();
        String destPath = new File("temp/coppied-file.txt").getAbsolutePath();
        BValue[] args = { new BString((new File(sourcePath).getAbsolutePath())),
                          new BString((new File(destPath).getAbsolutePath())) };
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testCopy", args);
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue(), "File wasn't copied");
    }

    @Test(expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp =
                    "error: ballerina.lang.errors:Error, message: failed to copy file: file " +
                    "not found: temp[\\\\/]copy-non-existing-file.txt.*")
    public void testCopyNonExistentFile() {
        String sourcePath = new File("temp/copy-non-existing-file.txt").getAbsolutePath();
        String destPath = new File("temp/duplicate-non-existing-file.txt").getAbsolutePath();
        BValue[] args = { new BString(sourcePath), new BString(destPath) };
        BLangFunctions.invokeNew(programFile, "testCopyNonExistent", args);
    }

    @Test
    public void testCopyDir() {
        String fileOne = new File("temp/copy-file-one.txt").getAbsolutePath();
        String sourcePath = new File("temp").getAbsolutePath();
        String destPath = new File("tempDir").getAbsolutePath();
        String fileDest = new File("tempDir/copy-file-one.txt").getAbsolutePath();
        BValue[] args = { new BString(fileOne), new BString(fileDest), new BString(sourcePath), new BString(destPath) };

        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testCopy", args);
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue(), "Source file does not exist");
        Assert.assertTrue(((BBoolean) returnVals[1]).booleanValue(), "File wasn't copied");

    }

    @Test
    public void testMove() {
        String sourcePath = new File("temp/move-file.txt").getAbsolutePath();
        String destPath = new File("temp/test/move-file.txt").getAbsolutePath();
        BValue[] args = { new BString(sourcePath), new BString(destPath) };
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMove", args);
        Assert.assertFalse(((BBoolean) returnVals[0]).booleanValue(), "Source file exists");
        Assert.assertTrue(((BBoolean) returnVals[1]).booleanValue(), "File wasn't moved");
    }

    @Test(expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp =
                    "error: ballerina.lang.errors:Error, message: failed to move file: file " +
                    "not found: temp.*")
    public void testMoveNonExistentFile() {
        String sourcePath = "temp/move-non-existing-file.txt";
        String destPath = "temp/test/move-non-existing-file.txt";
        BValue[] args = { new BString(sourcePath), new BString(destPath) };
        BLangFunctions.invokeNew(programFile, "testMoveNonExistent", args);
    }

    @Test
    public void testMoveDir() {
        String fileOne = new File("temp/move-file-one.txt").getAbsolutePath();
        String sourcePath = new File("temp").getAbsolutePath();
        String destPath = new File("tempDir").getAbsolutePath();
        String fileDest = new File("tempDir/move-file-one.txt").getAbsolutePath();
        BValue[] args = { new BString(fileOne), new BString(fileDest), new BString(sourcePath), new BString(destPath) };

        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testMoveDir", args);
        Assert.assertFalse(((BBoolean) returnVals[0]).booleanValue(), "File wasn't moved");
        Assert.assertTrue(((BBoolean) returnVals[1]).booleanValue(), "File wasn't moved");
    }

    @Test
    public void testDelete() {
        String sourcePath = new File("temp/move-file.txt").getAbsolutePath();
        BValue[] args = { new BString(sourcePath) };
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testDelete", args);
        Assert.assertFalse(((BBoolean) returnVals[0]).booleanValue(), "File wasn't deleted");
    }

    @Test(expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to delete file: " +
                                              "file not found: temp[\\\\/]delete-non-existing-file.txt.*")
    public void testDeleteNonExistentFile() {
        String targetPath = new File("temp/delete-non-existing-file.txt").getAbsolutePath();
        BValue[] args = { new BString(targetPath) };
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testDeleteNonExistent", args);
        Assert.assertFalse(((BBoolean) returnVals[0]).booleanValue(), "File wasn't deleted");
    }

    @Test
    public void testDeleteDir() {
        String fileOne = new File("temp/delete-file-one.txt").getAbsolutePath();
        String targetPath = new File("temp").getAbsolutePath();
        BValue[] args = { new BString(fileOne), new BString(targetPath) };

        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testDeleteDir", args);
        Assert.assertFalse(((BBoolean) returnVals[0]).booleanValue(), "Directory wasn't deleted");
    }

    @Test
    public void testWrite() {
        String targetPath = new File("temp/write-file.txt").getAbsolutePath();
        byte[] content = "Sample Text".getBytes();
        BBlob byteContent = new BBlob(content);
        BValue[] args = { byteContent, new BString(targetPath) };
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testWrite", args);
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue(), "File not created");
        Assert.assertEquals(byteContent.blobValue(), ((BBlob) returnVals[1]).blobValue(), "Written wrong content");
    }

    @Test
    public void testRead() {
        String targetPath = new File("temp/read-file.txt").getAbsolutePath();
        byte[] content = "Sample Text".getBytes();

        BValue[] args = { new BString(targetPath), new BBlob(content), new BInteger(1000) };
        BValue[] results = BLangFunctions.invokeNew(programFile, "testRead", args);
        Assert.assertEquals(content, ((BBlob) results[0]).blobValue(), "Not read properly");
    }

    @Test public void testCreate() {
        String targetPath = new File("temp/create-file.txt").getAbsolutePath();

        BValue[] args = { new BString(targetPath) };
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testCreate", args);
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue(), "File not created");
    }

    @Test
    public void testIsExist() {
        String targetPath = new File("temp/existing-file.txt").getAbsolutePath();
        byte[] content = "Sample Text".getBytes();

        BValue[] args = { new BString(targetPath), new BBlob(content), new BInteger(1000) };
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testIsExist", args);
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue(), "File existence not checked properly");
    }

}

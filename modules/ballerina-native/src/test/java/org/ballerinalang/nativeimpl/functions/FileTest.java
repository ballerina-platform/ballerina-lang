/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Test cases for ballerina.model.arrays.
 */
public class FileTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/fileTest.bal");
    }

    @BeforeMethod
    public void createTempDir() {
        File temp = new File("temp");
        if (temp.exists()) {
            deleteDir(temp);
        } else {
            temp.mkdir();
        }
    }

    @AfterMethod
    public void deleteTempDir() {
        File temp = new File("temp");
        if (temp.exists()) {
            deleteDir(temp);
        }
    }


    @Test
    public void testCopy() throws IOException {

        String sourcePath = "temp/original.txt";
        String destPath = "temp/duplicate.txt";
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        if (sourceFile.createNewFile()) {

            BValue[] source = { new BString(sourcePath) };
            BValue[] dest = { new BString(destPath) };
            BStruct sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            BStruct destStruct = new BStruct(new StructDef(GlobalScope.getInstance()), dest);
            BValue[] args = { sourceStruct, destStruct };

            BLangFunctions.invoke(bLangProgram, "testCopy", args);
            Assert.assertTrue(sourceFile.exists(), "Source file does not exist");
            Assert.assertTrue(destFile.exists(), "File wasn't copied");
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test
    public void testMove() throws IOException {

        String sourcePath = "temp/original.txt";
        String destPath = "temp/test/original.txt";
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        if (sourceFile.createNewFile()) {

            BValue[] source = { new BString(sourcePath) };
            BValue[] dest = { new BString(destPath) };
            BStruct sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            BStruct destStruct = new BStruct(new StructDef(GlobalScope.getInstance()), dest);
            BValue[] args = { sourceStruct, destStruct };

            BLangFunctions.invoke(bLangProgram, "testMove", args);
            Assert.assertFalse(sourceFile.exists(), "Source file exists");
            Assert.assertTrue(destFile.exists(), "File wasn't moved");
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test
    public void testDelete() throws IOException {

        String targetPath = "temp/original.txt";
        File targetFile = new File(targetPath);
        if (targetFile.createNewFile()) {

            BValue[] source = { new BString(targetPath) };
            BStruct targetStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            BValue[] args = { targetStruct };

            BLangFunctions.invoke(bLangProgram, "testDelete", args);
            Assert.assertFalse(targetFile.exists(), "Target file exists");
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test
    public void testOpen() throws IOException {

        String sourcePath = "temp/original.txt";
        File sourceFile = new File(sourcePath);
        if (sourceFile.createNewFile()) {

            BValue[] source = { new BString(sourcePath) };
            BStruct sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            BValue[] args = { sourceStruct , new BString("rw")};

            BLangFunctions.invoke(bLangProgram, "testOpen", args);
            Assert.assertNotNull(sourceStruct.getNativeData("inStream"), "Input Stream not found");
            Assert.assertNotNull(sourceStruct.getNativeData("outStream"), "Output Stream not found");

            sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            args[0] = sourceStruct;
            args[1] = new BString("ra");

            BLangFunctions.invoke(bLangProgram, "testOpen", args);
            Assert.assertNotNull(sourceStruct.getNativeData("inStream"), "Input Stream not found");
            Assert.assertNotNull(sourceStruct.getNativeData("outStream"), "Output Stream not found");

            sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            args[0] = sourceStruct;
            args[1] = new BString("r");

            BLangFunctions.invoke(bLangProgram, "testOpen", args);
            Assert.assertNotNull(sourceStruct.getNativeData("inStream"), "Input Stream not found");
            Assert.assertNull(sourceStruct.getNativeData("outStream"), "Output Stream found in read mode");

            sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            args[0] = sourceStruct;
            args[1] = new BString("w");

            BLangFunctions.invoke(bLangProgram, "testOpen", args);
            Assert.assertNull(sourceStruct.getNativeData("inStream"), "Input Stream not found in write mode");
            Assert.assertNotNull(sourceStruct.getNativeData("outStream"), "Output Stream not found");

            sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            args[0] = sourceStruct;
            args[1] = new BString("a");

            BLangFunctions.invoke(bLangProgram, "testOpen", args);
            Assert.assertNull(sourceStruct.getNativeData("inStream"), "Input Stream not found in append mode");
            Assert.assertNotNull(sourceStruct.getNativeData("outStream"), "Output Stream not found");

        } else {
            Assert.fail("Error in file creation.");
        }

    }

    @Test
    public void testWrite() throws IOException {

        String targetPath = "temp/text.txt";
        File targetFile = new File(targetPath);
        byte[] content = "Sample Text".getBytes();
        BBlob byteContent = new BBlob(content);
        BValue[] source = { new BString(targetPath) };
        BStruct targetStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
        BValue[] args = { byteContent, targetStruct };

        BLangFunctions.invoke(bLangProgram, "testWrite", args);
        Assert.assertTrue(targetFile.exists(), "File not created");
        Assert.assertEquals(byteContent.blobValue(), getBytesFromFile(targetFile), "Written wrong content");
    }

    private void deleteDir(File dir) {

        String[] entries = dir.list();
        if (entries.length != 0) {
            for (String s : entries) {
                File currentFile = new File(dir.getPath(), s);
                if (currentFile.isDirectory()) {
                    deleteDir(currentFile);
                }
                currentFile.delete();
            }
        }
        dir.delete();
    }

    private byte[] getBytesFromFile(File file) throws IOException {

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too large!");
        }

        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead;
        FileInputStream is = new FileInputStream(file);

        try {
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            closeQuietly(is);
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        return bytes;
    }

    private void closeQuietly(Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (Exception e) {
            throw new BallerinaException("Exception during Resource.close()", e);
        }
    }

}

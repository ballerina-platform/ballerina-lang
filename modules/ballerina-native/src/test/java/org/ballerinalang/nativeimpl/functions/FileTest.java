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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.program.BLangFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Test cases for ballerina.model.file.
 */
public class FileTest {

    private static final Logger logger = LoggerFactory.getLogger(FileTest.class);
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

        File tempDir = new File("tempDir");
        if (tempDir.exists()) {
            deleteDir(tempDir);
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
    public void testCopyDir() throws IOException {

        String fileOne = "temp/fileOne.txt";
        String fileTwo = "temp/fileTwo.txt";
        File one = new File(fileOne);
        File two = new File(fileTwo);
        String sourcePath = "temp";
        String destPath = "tempDir";
        if (one.createNewFile() && two.createNewFile()) {

            BValue[] source = { new BString(sourcePath) };
            BValue[] dest = { new BString(destPath) };
            BStruct sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            BStruct destStruct = new BStruct(new StructDef(GlobalScope.getInstance()), dest);
            BValue[] args = { sourceStruct, destStruct };

            BLangFunctions.invoke(bLangProgram, "testCopy", args);
            Assert.assertTrue(new File("temp/fileOne.txt").exists(), "Source file does not exist");
            Assert.assertTrue(new File("temp/fileTwo.txt").exists(), "Source file does not exist");
            Assert.assertTrue(new File("tempDir/fileOne.txt").exists(), "File wasn't copied");
            Assert.assertTrue(new File("tempDir/fileTwo.txt").exists(), "File wasn't copied");

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
    public void testMoveDir() throws IOException {

        String fileOne = "temp/fileOne.txt";
        String fileTwo = "temp/fileTwo.txt";
        File one = new File(fileOne);
        File two = new File(fileTwo);
        String sourcePath = "temp";
        String destPath = "tempDir";
        if (one.createNewFile() && two.createNewFile()) {

            BValue[] source = { new BString(sourcePath) };
            BValue[] dest = { new BString(destPath) };
            BStruct sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            BStruct destStruct = new BStruct(new StructDef(GlobalScope.getInstance()), dest);
            BValue[] args = { sourceStruct, destStruct };

            BLangFunctions.invoke(bLangProgram, "testMove", args);
            Assert.assertFalse(new File("temp/fileOne.txt").exists(), "Source file exists");
            Assert.assertFalse(new File("temp/fileTwo.txt").exists(), "Source file exists");
            Assert.assertTrue(new File("tempDir/fileOne.txt").exists(), "File wasn't moved");
            Assert.assertTrue(new File("tempDir/fileTwo.txt").exists(), "File wasn't moved");

        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test
    public void testDelete() throws IOException {

        String targetPath = "temp/original.txt";
        File targetFile = new File(targetPath);
        if (targetFile.createNewFile()) {

            BValue[] target = { new BString(targetPath) };
            BStruct targetStruct = new BStruct(new StructDef(GlobalScope.getInstance()), target);
            BValue[] args = { targetStruct };

            BLangFunctions.invoke(bLangProgram, "testDelete", args);
            Assert.assertFalse(targetFile.exists(), "Target file exists");
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test
    public void testDeleteDir() throws IOException {

        String fileOne = "temp/fileOne.txt";
        String fileTwo = "temp/fileTwo.txt";
        File one = new File(fileOne);
        File two = new File(fileTwo);
        String targetPath = "temp";
        File targetFile = new File(targetPath);
        if (one.createNewFile() && two.createNewFile()) {

            BValue[] target = { new BString(targetPath) };
            BStruct targetStruct = new BStruct(new StructDef(GlobalScope.getInstance()), target);
            BValue[] args = { targetStruct };

            BLangFunctions.invoke(bLangProgram, "testDelete", args);
            Assert.assertFalse(targetFile.exists(), "Target Directory exists");
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

    @Test(expectedExceptions = IOException.class)
    public void testClose() throws IOException {

        String sourcePath = "temp/original.txt";
        File sourceFile = new File(sourcePath);
        if (sourceFile.createNewFile()) {
            BValue[] source = { new BString(sourcePath) };
            BStruct sourceStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            sourceStruct.addNativeData("inStream", inputStream);
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(sourceFile));
            sourceStruct.addNativeData("outStream", outputStream);
            BValue[] args = {sourceStruct};

            BLangFunctions.invoke(bLangProgram, "testClose", args);
            inputStream = (BufferedInputStream) sourceStruct.getNativeData("inStream");
            outputStream = (BufferedOutputStream) sourceStruct.getNativeData("outStream");
            inputStream.read();
            outputStream.write(1);

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

    @Test
    public void testRead() throws IOException {

        String targetPath = "temp/text.txt";
        File targetFile = new File(targetPath);
        OutputStream outputStream = new FileOutputStream(targetFile);
        byte[] content = "Sample Text".getBytes();
        outputStream.write(content);

        BValue[] source = { new BString(targetPath) };
        BStruct targetStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);

        BufferedInputStream is = new BufferedInputStream(new FileInputStream(targetFile));
        targetStruct.addNativeData("inStream", is);
        BValue[] args = { targetStruct, new BInteger(11) };

        BValue[] results = BLangFunctions.invoke(bLangProgram, "testRead", args);
        Assert.assertEquals(content, ((BBlob) results[0]).blobValue(), "Not read properly");
    }

    @Test(expectedExceptions = BallerinaException.class,
            expectedExceptionsMessageRegExp = "The file isn't opened in read mode")
    public void testReadWithoutOpeningFile() throws IOException {

        String targetPath = "temp/text.txt";

        BValue[] source = { new BString(targetPath) };
        BStruct targetStruct = new BStruct(new StructDef(GlobalScope.getInstance()), source);

        BValue[] args = { targetStruct, new BInteger(11) };
        BLangFunctions.invoke(bLangProgram, "testRead", args);
    }

    private void deleteDir(File dir) {

        String[] entries = dir.list();
        if (entries != null && entries.length != 0) {
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
        } catch (IOException e) {
            logger.error("Exception during Resource.close()", e);
        }
    }

}

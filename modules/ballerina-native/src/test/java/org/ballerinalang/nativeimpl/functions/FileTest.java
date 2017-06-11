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

import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Test cases for ballerina.model.file.
 */
public class FileTest {

    private static final Logger logger = LoggerFactory.getLogger(FileTest.class);
    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/fileTest.bal");
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
        String sourcePath = "temp/copy-file.txt";
        String destPath = "temp/coppied-file.txt";
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        if (sourceFile.createNewFile()) {
            BValue[] args = { new BString(sourcePath), new BString(destPath) };
            BLangFunctions.invokeNew(programFile, "testCopy", args);
            Assert.assertTrue(sourceFile.exists(), "Source file does not exist");
            Assert.assertTrue(destFile.exists(), "File wasn't copied");
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test(expectedExceptions = BLangRuntimeException.class, 
          expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to copy file: file " +
          "not found: temp[\\\\/]copy-non-existing-file.txt.*")
    public void testCopyNonExistentFile() {
        String sourcePath = "temp/copy-non-existing-file.txt";
        String destPath = "temp/duplicate-non-existing-file.txt";
        BValue[] args = { new BString(sourcePath), new BString(destPath) };
        BLangFunctions.invokeNew(programFile, "testCopy", args);
    }

    @Test
    public void testCopyDir() throws IOException {
        String fileOne = "temp/copy-file-one.txt";
        String fileTwo = "temp/copy-file-two.txt";
        File one = new File(fileOne);
        File two = new File(fileTwo);
        String sourcePath = "temp";
        String destPath = "tempDir";
        if (one.createNewFile() && two.createNewFile()) {
            BValue[] args = { new BString(sourcePath), new BString(destPath) };

            BLangFunctions.invokeNew(programFile, "testCopy", args);
            Assert.assertTrue(new File("temp/copy-file-one.txt").exists(), "Source file does not exist");
            Assert.assertTrue(new File("temp/copy-file-two.txt").exists(), "Source file does not exist");
            Assert.assertTrue(new File("tempDir/copy-file-one.txt").exists(), "File wasn't copied");
            Assert.assertTrue(new File("tempDir/copy-file-two.txt").exists(), "File wasn't copied");

        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test
    public void testMove() throws IOException {
        String sourcePath = "temp/move-file.txt";
        String destPath = "temp/test/move-file.txt";
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        if (sourceFile.createNewFile()) {
            BValue[] args = { new BString(sourcePath), new BString(destPath) };
            BLangFunctions.invokeNew(programFile, "testMove", args);
            Assert.assertFalse(sourceFile.exists(), "Source file exists");
            Assert.assertTrue(destFile.exists(), "File wasn't moved");
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test(expectedExceptions = BLangRuntimeException.class, 
          expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to move file: file " +
          "not found: temp.*")
    public void testMoveNonExistentFile() throws IOException {
        String sourcePath = "temp/move-non-existing-file.txt";
        String destPath = "temp/test/move-non-existing-file.txt";
        BValue[] args = { new BString(sourcePath), new BString(destPath) };
        BLangFunctions.invokeNew(programFile, "testMove", args);
    }

    @Test
    public void testMoveDir() throws IOException {
        String fileOne = "temp/move-file-one.txt";
        String fileTwo = "temp/move-file-two.txt";
        File one = new File(fileOne);
        File two = new File(fileTwo);
        String sourcePath = "temp";
        String destPath = "tempDir";
        if (one.createNewFile() && two.createNewFile()) {
            BValue[] args = { new BString(sourcePath), new BString(destPath) };
            BLangFunctions.invokeNew(programFile, "testMove", args);
            Assert.assertFalse(new File("temp/move-file-one.txt").exists(), "Source file exists");
            Assert.assertFalse(new File("temp/move-file-two.txt").exists(), "Source file exists");
            Assert.assertTrue(new File("tempDir/move-file-one.txt").exists(), "File wasn't moved");
            Assert.assertTrue(new File("tempDir/move-file-two.txt").exists(), "File wasn't moved");
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test
    public void testDelete() throws IOException {
        String targetPath = "temp/delete-file.txt";
        File targetFile = new File(targetPath);
        if (targetFile.createNewFile()) {
            BValue[] args = { new BString(targetPath)};
            BLangFunctions.invokeNew(programFile, "testDelete", args);
            Assert.assertFalse(targetFile.exists(), "Target file exists");
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to delete file: " +
            "file not found: temp[\\\\/]delete-non-existing-file.txt.*")
    public void testDeleteNonExistentFile() {
        String targetPath = "temp/delete-non-existing-file.txt";
        BValue[] args = { new BString(targetPath) };
        BLangFunctions.invokeNew(programFile, "testDelete", args);
    }

    @Test
    public void testDeleteDir() throws IOException {
        String fileOne = "temp/delete-file-one.txt";
        String fileTwo = "temp/delete-file-two.txt";
        File one = new File(fileOne);
        File two = new File(fileTwo);
        String targetPath = "temp";
        File targetFile = new File(targetPath);
        if (one.createNewFile() && two.createNewFile()) {
            BValue[] args = { new BString(targetPath) };

            BLangFunctions.invokeNew(programFile, "testDelete", args);
            Assert.assertFalse(targetFile.exists(), "Target Directory exists");
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test
    public void testOpen() throws IOException {
        String sourcePath = "temp/open-file.txt";
        File sourceFile = new File(sourcePath);
        if (sourceFile.createNewFile()) {

            BValue[] args = { new BString(sourcePath) , new BString("rw")};
            BStruct sourceStruct = (BStruct) BLangFunctions.invokeNew(programFile, "testOpen", args)[0];
            Assert.assertNotNull(sourceStruct.getNativeData("inStream"), "Input Stream not found");
            Assert.assertNotNull(sourceStruct.getNativeData("outStream"), "Output Stream not found");

            args[1] = new BString("ra");
            sourceStruct = (BStruct) BLangFunctions.invokeNew(programFile, "testOpen", args)[0];
            Assert.assertNotNull(sourceStruct.getNativeData("inStream"), "Input Stream not found");
            Assert.assertNotNull(sourceStruct.getNativeData("outStream"), "Output Stream not found");

            args[1] = new BString("r");
            sourceStruct =  (BStruct) BLangFunctions.invokeNew(programFile, "testOpen", args)[0];
            Assert.assertNotNull(sourceStruct.getNativeData("inStream"), "Input Stream not found");
            Assert.assertNull(sourceStruct.getNativeData("outStream"), "Output Stream found in read mode");

            args[1] = new BString("w");
            sourceStruct = (BStruct) BLangFunctions.invokeNew(programFile, "testOpen", args)[0];
            Assert.assertNull(sourceStruct.getNativeData("inStream"), "Input Stream not found in write mode");
            Assert.assertNotNull(sourceStruct.getNativeData("outStream"), "Output Stream not found");

            args[1] = new BString("a");
            sourceStruct = (BStruct) BLangFunctions.invokeNew(programFile, "testOpen", args)[0];
            Assert.assertNull(sourceStruct.getNativeData("inStream"), "Input Stream not found in append mode");
            Assert.assertNotNull(sourceStruct.getNativeData("outStream"), "Output Stream not found");

        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to open file: " +
            "file not found: temp[\\\\/]open-non-existing-file.txt.*")
    public void testOpenNonExistentFile() {
        String sourcePath = "temp/open-non-existing-file.txt";
        BValue[] args = { new BString(sourcePath), new BString("rw") };
        BLangFunctions.invokeNew(programFile, "testOpen", args);
    }

    @Test(expectedExceptions = IOException.class)
    public void testClose() throws IOException {
        String sourcePath = "temp/close-file.txt";
        File sourceFile = new File(sourcePath);
        if (sourceFile.createNewFile()) {
            BValue[] args = { new BString(sourcePath) };
            BStruct sourceStruct = (BStruct) BLangFunctions.invokeNew(programFile, "testClose", args)[0];
            InputStream inputStream = (BufferedInputStream) sourceStruct.getNativeData("inStream");
            OutputStream outputStream = (BufferedOutputStream) sourceStruct.getNativeData("outStream");
            inputStream.read();
            outputStream.write(1);
        } else {
            Assert.fail("Error in file creation.");
        }
    }

    @Test
    public void testWrite() throws IOException {
        String targetPath = "temp/write-file.txt";
        File targetFile = new File(targetPath);
        byte[] content = "Sample Text".getBytes();
        BBlob byteContent = new BBlob(content);
        BValue[] args = { byteContent, new BString(targetPath) };
        BLangFunctions.invokeNew(programFile, "testWrite", args);
        Assert.assertTrue(targetFile.exists(), "File not created");
        Assert.assertEquals(byteContent.blobValue(), getBytesFromFile(targetFile), "Written wrong content");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to write to " +
            "file: file is not opened in write or append mode.*")
    public void testWriteWithoutOpeningFile() {
        String targetPath = "temp/write-non-opened.txt";
        byte[] content = "Sample Text".getBytes();
        BBlob byteContent = new BBlob(content);
        BValue[] args = { byteContent, new BString(targetPath)  };
        BLangFunctions.invokeNew(programFile, "testWriteWithoutOpening", args);
    }

    @Test
    public void testRead() throws IOException {
        String targetPath = "temp/read-file.txt";
        File targetFile = new File(targetPath);
        OutputStream outputStream = new FileOutputStream(targetFile);
        byte[] content = "Sample Text".getBytes();
        outputStream.write(content);

        BValue[] args = { new BString(targetPath), new BInteger(11) };
        BValue[] results = BLangFunctions.invokeNew(programFile, "testRead", args);
        Assert.assertEquals(content, ((BBlob) results[0]).blobValue(), "Not read properly");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to read from " +
            "file: file is not opened in read mode.*")
    public void testReadWithoutOpeningFile() throws IOException {
        String targetPath = "temp/read-non-opened-file.txt";
        File targetFile = new File(targetPath);
        OutputStream outputStream = new FileOutputStream(targetFile);
        byte[] content = "Sample Text".getBytes();
        outputStream.write(content);
        
        BValue[] args = { new BString(targetPath) , new BInteger(11) };
        BLangFunctions.invokeNew(programFile, "testReadWithoutOpening", args);
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

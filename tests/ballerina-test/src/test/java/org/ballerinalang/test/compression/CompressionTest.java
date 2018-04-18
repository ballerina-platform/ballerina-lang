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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.compression;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Test Native functions in ballerina.compression.
 */
public class CompressionTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/compression/compression.bal");
    }

    @Test(description = "test unzipping/decompressing a compressed file with src and destination directory path")
    public void testDecompressFile() throws IOException, URISyntaxException {
        String resourceToRead = getAbsoluteFilePath("datafiles/compression/hello.zip");
        BString dirPath = new BString(resourceToRead);
        String destDirPath = getAbsoluteFilePath("datafiles/compression/");
        BString destDir = new BString(destDirPath);
        BValue[] inputArg = {dirPath, destDir};
        BRunUtil.invoke(compileResult, "decompressFile", inputArg);

        File file = new File(destDirPath + File.separator + "hello.txt");
        Assert.assertEquals(file.exists() && !file.isDirectory(), true);
    }

    @Test(description = "test unzipping/decompressing a compressed file without src directory path")
    public void testDecompressFileWithoutSrcDirectory() throws IOException, URISyntaxException {
        String destDirPath = getAbsoluteFilePath("datafiles/compression/");
        BString destDir = new BString(destDirPath);
        BValue[] inputArg = {new BString(""), destDir};
        BValue[] returns = BRunUtil.invoke(compileResult, "decompressFile", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Path of the folder to be " +
                "decompressed is not available");
    }

    @Test(description = "test unzipping/decompressing a compressed file without destination directory path")
    public void testDecompressFileWithoutDestDirectory() throws IOException, URISyntaxException {
        String resourceToRead = getAbsoluteFilePath("datafiles/compression/hello.zip");
        BString dirPath = new BString(resourceToRead);
        BValue[] inputArg = {dirPath, new BString("")};
        BValue[] returns = BRunUtil.invoke(compileResult, "decompressFile", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Path to place the decompressed " +
                "file is not available");
    }

    @Test(description = "test unzipping/decompressing a compressed file with incorrect src directory path")
    public void testDecompressFileWithIncorrectSrcDirectory() throws IOException, URISyntaxException {
        String resourceToRead = getAbsoluteFilePath("datafiles/compression/sample.zip");
        BString dirPath = new BString(resourceToRead);
        String destDirPath = getAbsoluteFilePath("datafiles/compression/");
        BString destDir = new BString(destDirPath);
        BValue[] inputArg = {dirPath, destDir};
        BValue[] returns = BRunUtil.invoke(compileResult, "decompressFile", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0).startsWith("Path of the folder to be " +
                                                                "decompressed is not available"), true);
    }

    @Test(description = "test unzipping/decompressing a compressed file with incorrect src directory path")
    public void testDecompressFileWithIncorrectDestDirectory() throws IOException, URISyntaxException {
        String resourceToRead = getAbsoluteFilePath("datafiles/compression/hello.zip");
        BString dirPath = new BString(resourceToRead);
        String destDirPath = getAbsoluteFilePath("datafiles/compression/sample");
        BString destDir = new BString(destDirPath);
        BValue[] inputArg = {dirPath, destDir};
        BValue[] returns = BRunUtil.invoke(compileResult, "decompressFile", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0).startsWith("Path to place the " +
                                "decompressed file is not available"), true);
    }

    @Test(description = "test zipping/compressing a folder")
    public void testCompressFolder() throws IOException, URISyntaxException {
        String resourceToRead = getAbsoluteFilePath("datafiles/compression/my.app");
        BString dirPath = new BString(resourceToRead);
        String destDirPath = getAbsoluteFilePath("datafiles/compression/");
        BString destDir = new BString(destDirPath + File.separator + "my.app.zip");
        BValue[] inputArg = {dirPath, destDir};
        BValue[] returns = BRunUtil.invoke(compileResult, "compressFile", inputArg);
        Assert.assertNotNull(returns);
        File f1 = new File(destDirPath + File.separator + "my.app.zip");
        Assert.assertEquals(f1.exists() && !f1.isDirectory(), true);

        ArrayList<String> filesInsideZip = getFilesInsideZip(destDirPath + File.separator
                                                                     + "my.app.zip");

        Assert.assertEquals(filesInsideZip.size() > 0, true);

    }

    @Test(description = "test zipping/compressing a single file")
    public void testCompressFile() throws IOException, URISyntaxException {
        String resourceToRead = getAbsoluteFilePath("datafiles/compression/my.app/test/main.bal");
        BString dirPath = new BString(resourceToRead);
        String destDirPath = getAbsoluteFilePath("datafiles/compression/");
        BString destDir = new BString(destDirPath + File.separator + "main.zip");
        BValue[] inputArg = {dirPath, destDir};
        BValue[] returns = BRunUtil.invoke(compileResult, "compressFile", inputArg);
        Assert.assertNotNull(returns);
        File f1 = new File(destDirPath + File.separator + "main.zip");
        Assert.assertEquals(f1.exists() && !f1.isDirectory(), true);

        ArrayList<String> filesInsideZip = getFilesInsideZip(destDirPath + File.separator
                                                                     + "main.zip");

        Assert.assertEquals(filesInsideZip.size() > 0, true);

    }

    @Test(description = "test zipping/compressing a file without providing a name for the zipped file")
    public void testCompressFileWithoutInvalidName() throws IOException, URISyntaxException {
        String resourceToRead = getAbsoluteFilePath("datafiles/compression/my.app");
        BString dirPath = new BString(resourceToRead);
        BString destDir = new BString(getAbsoluteFilePath("datafiles/compression/"));
        BValue[] inputArg = {dirPath, destDir};
        BValue[] returns = BRunUtil.invoke(compileResult, "compressFile", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0).startsWith("Error occurred when " +
               "compressing"), true);
    }

    @Test(description = "test zipping/compressing a file without src directory path")
    public void testCompressFileWithoutSrcDirectory() throws IOException, URISyntaxException {
        String destDirPath = getAbsoluteFilePath("datafiles/compression/");
        BString destDir = new BString(destDirPath + File.separator + "compression.zip");
        BValue[] inputArg = {new BString(""), destDir};
        BValue[] returns = BRunUtil.invoke(compileResult, "compressFile", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Path of the folder to be " +
                "compressed is not available");
    }

    @Test(description = "test zipping/compressing a file without destination directory path")
    public void testCompressFileWithoutDestinationDirectory() throws IOException, URISyntaxException {
        String resourceToRead = getAbsoluteFilePath("datafiles/compression/my.app");
        BString dirPath = new BString(resourceToRead);
        BValue[] inputArg = {dirPath, new BString("")};
        BValue[] returns = BRunUtil.invoke(compileResult, "compressFile", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Error occurred when " +
                "compressing  (No such file or directory)");
    }

    @Test(description = "test zipping/compressing a file without destination directory path")
    public void testCompressFileWithIncorrectSrcDirectory() throws IOException, URISyntaxException {
        String resourceToRead = getAbsoluteFilePath("datafiles/compression/sample.zip");
        BString dirPath = new BString(resourceToRead);
        BValue[] inputArg = {dirPath, new BString("")};
        BValue[] returns = BRunUtil.invoke(compileResult, "compressFile", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Path of the folder to be " +
                "compressed is not available");
    }


    @Test(description = "test unzipping/decompressing a byte array")
    public void testDecompressBlob() throws IOException, URISyntaxException {
        String dirPath = getAbsoluteFilePath("datafiles/compression/");
        byte[] fileContentAsByteArray = Files.readAllBytes(new File(dirPath +
                                                                            File.separator + "test.zip").toPath());
        BString destDir = new BString(dirPath);
        BBlob contentAsByteArray = new BBlob(fileContentAsByteArray);
        BValue[] inputArg = {contentAsByteArray, destDir};
        BRunUtil.invoke(compileResult, "decompressBlob", inputArg);

        File f1 = new File(dirPath + File.separator + "test" + File.separator);
        Assert.assertEquals(f1.exists() && f1.isDirectory(), true);

        File f2 = new File(dirPath + File.separator + "test" + File.separator + "test.txt");
        Assert.assertEquals(f2.exists() && !f2.isDirectory(), true);

        File f3 = new File(dirPath + File.separator + "test" + File.separator + "abc");
        Assert.assertEquals(f3.exists() && f3.isDirectory(), true);
    }

    @Test(description = "test unzipping/decompressing a byte array without the content as a blob/byte array")
    public void testDecompressBlobWithoutBytes() throws IOException, URISyntaxException {
        String dirPath = getAbsoluteFilePath("datafiles/compression/");
        BString destDir = new BString(dirPath);
        BBlob contentAsByteArray = new BBlob(new byte[0]);
        BValue[] inputArg = {contentAsByteArray, destDir};
        BValue[] returns = BRunUtil.invoke(compileResult, "decompressBlob", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Length of the byte array is empty");
    }

    @Test(description = "test unzipping/decompressing a byte array without the destination directory path")
    public void testDecompressBlobWithoutDestDirectory() throws IOException, URISyntaxException {
        String dirPath = getAbsoluteFilePath("datafiles/compression/");
        byte[] fileContentAsByteArray = Files.readAllBytes(new File(dirPath +
                                                                            File.separator + "test.zip").toPath());
        BBlob contentAsByteArray = new BBlob(fileContentAsByteArray);
        BValue[] inputArg = {contentAsByteArray, new BString("")};
        BValue[] returns = BRunUtil.invoke(compileResult, "decompressBlob", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Path to place the decompressed " +
                "file is not available");
    }

    @Test(description = "test unzipping/decompressing a byte array with an incorrect destination directory path")
    public void testDecompressBlobWithIncorrectDestDir() throws IOException, URISyntaxException {
        String dirPath = getAbsoluteFilePath("datafiles/compression/");
        byte[] fileContentAsByteArray = Files.readAllBytes(new File(dirPath +
                                                                            File.separator + "test.zip").toPath());
        BString destDir = new BString(dirPath + File.separator + "sample");
        BBlob contentAsByteArray = new BBlob(fileContentAsByteArray);
        BValue[] inputArg = {contentAsByteArray, destDir};
        BValue[] returns = BRunUtil.invoke(compileResult, "decompressBlob", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Path to place the decompressed " +
                "file is not available");
    }

    @Test(description = "test zipping/compressing a file to a byte array and decompressing it to check if it was " +
            " properly compressed")
    public void testCompressToBlob() throws IOException, URISyntaxException {
        String dirPath = getAbsoluteFilePath("datafiles/compression/");
        BString dirPathValue = new BString(dirPath + File.separator + "my.app");
        BValue[] inputArg = {dirPathValue};
        BValue[] returns = BRunUtil.invoke(compileResult, "compressDirToBlob", inputArg);

        // Write the byte array to a file
        FileOutputStream fos = new FileOutputStream(dirPath + File.separator + "temp.zip");
        BBlob readBytes = (BBlob) returns[0];
        fos.write(readBytes.blobValue());
        fos.close();
        File file = new File(dirPath + File.separator + "temp.zip");
        // Check if file is written
        Assert.assertEquals(file.exists() && !file.isDirectory(), true);

    }

    @Test(description = "test zipping/compressing a file to a byte array when the src directory is not given")
    public void testCompressToBlobWithoutSrcDir() throws IOException, URISyntaxException {
        BValue[] inputArg = {new BString("")};
        BValue[] returns = BRunUtil.invoke(compileResult, "compressDirToBlob", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Path of the folder to be " +
                "compressed is not available");
    }

    @Test(description = "test zipping/compressing a file to a byte array when an incorrect src directory is given")
    public void testCompressToBlobWithIncorrectSrcDir() throws IOException, URISyntaxException {
        String dirPath = getAbsoluteFilePath("datafiles/compression/");
        BString dirPathValue = new BString(dirPath + File.separator + "sample");
        BValue[] inputArg = {dirPathValue};
        BValue[] returns = BRunUtil.invoke(compileResult, "compressDirToBlob", inputArg);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "Path of the folder to be " +
                "compressed is not available");

    }
    /**
     * Will identify the absolute path from the relative.
     *
     * @param relativePath the relative file path location.
     * @return the absolute path.
     */
    private String getAbsoluteFilePath(String relativePath) throws URISyntaxException {
        URL fileResource = BServiceUtil.class.getClassLoader().getResource(relativePath);
        String pathValue = "";
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            pathValue = path.toAbsolutePath().toString();
        }
        return pathValue;
    }

    /**
     * Get files inside the zip file.
     *
     * @param zipFilePath path of the zip file
     * @return list of files available inside the zipped file
     */
    private ArrayList<String> getFilesInsideZip(String zipFilePath) {
        ArrayList<String> filesContained = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                filesContained.add(name);
            }
            zipFile.close();
        } catch (IOException e) {
            new BLangRuntimeException("Error occured when retrieving the files from the archived file");
        }
        return filesContained;
    }
}

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

package org.ballerinalang.test.nativeimpl.functions.file;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBlob;
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
 * Test file operations.
 */
public class FileOperationTest {
    private CompileResult fileOperationProgramFile;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        fileOperationProgramFile = BCompileUtil.compile("test-src/file/file_ops.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/target";
    }

    /**
     * <p>
     * Returns if the OS is Windows
     * </p>
     * <p>
     * When creating directories through java application by default in windows OS the file is created as read-only
     * This will be an issue when running tests related to deleted in Windows. Hence we do the check
     * </p>
     * <p>
     * Some related reference for this would be,
     * https://stackoverflow.com/questions/8041049/how-to-create-non-read-only-directories-from-java-in-windows
     * </p>
     *
     * @return true if the OS is windows.
     */
    private boolean isWindows() {
        String osName = System.getProperty("os.name");
        if (null != osName) {
            if (osName.toLowerCase().startsWith("windows")) {
                return true;
            }
        }
        return false;
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

    @Test(description = "Test 'toAbsolutePath' function in ballerina.file package")
    public void testFileAbsolutePath() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/utf8file.txt";
        BString absolutePathReturned;

        //Will initialize the channel
        String absoluteFilePath = getAbsoluteFilePath(resourceToRead);
        BValue[] args = {new BString(absoluteFilePath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testAbsolutePath", args);
        absolutePathReturned = (BString) returns[0];

        Assert.assertEquals(absolutePathReturned.stringValue(), absoluteFilePath);
    }

    @Test(description = "Test 'exists' function in ballerina.file package")
    public void testFileExistence() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/utf8file.txt";
        BBoolean existance;

        //Will initialize the channel
        String absoluteFilePath = getAbsoluteFilePath(resourceToRead);
        BValue[] args = {new BString(absoluteFilePath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testPathExistance", args);
        existance = (BBoolean) returns[0];

        Assert.assertEquals(existance.booleanValue(), true);

        resourceToRead = "./datafiles/io/text/nonExistingFile.txt";

        BValue[] args1 = {new BString(resourceToRead)};
        returns = BRunUtil.invoke(fileOperationProgramFile, "testPathExistance", args1);
        existance = (BBoolean) returns[0];

        Assert.assertEquals(existance.booleanValue(), false);
    }

    @Test(description = "Test 'list' function in ballerina.file package")
    public void testListDirectory() throws URISyntaxException {
        BStringArray result;
        //Will initialize the channel
        BValue[] args = {new BString(currentDirectoryPath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "createDirectoryAndList", args);
        result = (BStringArray) returns[0];

        boolean directoryNameCondition = result.get(0).endsWith("child1") || result.get(0).endsWith("child2");
        Assert.assertTrue(directoryNameCondition);
        Assert.assertEquals(result.size(), 2);
    }

    @Test(description = "Test 'createFile' function in ballerina.file package",
            dependsOnMethods = {"testListDirectory"})
    public void testCreateFile() {
        String path = currentDirectoryPath + "/parent/child1/";
        BString result;
        //Will initialize the channel
        BValue[] args = {new BString(path)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testCreateFile", args);
        result = (BString) returns[0];

        Assert.assertTrue(result.stringValue().endsWith("test.txt"));
    }

    @Test(description = "Test 'newByteChannel' function in ballerina.file package",
            dependsOnMethods = {"testCreateFile", "testListDirectory"})
    public void testWriteBytesToFile() {
        String path = currentDirectoryPath + "/parent/child1/test.txt";
        byte[] content = "This is a sample Text".getBytes();
        BBlob result;
        //Will initialize the channel
        BValue[] args = {new BString(path), new BString("w"), new BBlob(content)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testWriteFile", args);
        result = (BBlob) returns[0];
        Assert.assertEquals(content, result.blobValue());
    }

    @Test(description = "Test 'newByteChannel' function in ballerina.file package",
            dependsOnMethods = {"testCreateFile", "testListDirectory"})
    public void testFileMetaData() throws URISyntaxException {
        final String fileName = "test.txt";
        final String emptyString = "";
        String path = currentDirectoryPath + "/parent/child1/" + fileName;
        BString result;
        //Will initialize the channel
        BValue[] args = {new BString(path)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testGetFileName", args);
        result = (BString) returns[0];

        Assert.assertTrue(fileName.equals(result.stringValue()));

        returns = BRunUtil.invoke(fileOperationProgramFile, "testGetModifiedTime", args);
        result = (BString) returns[0];

        Assert.assertFalse(emptyString.equals(result.stringValue()));
    }

    @Test(description = "Test 'move' function in ballerina.file package",
            dependsOnMethods = {"testCreateFile", "testListDirectory"})
    public void testFileMove() throws URISyntaxException {
        String srcPath = currentDirectoryPath + "/parent";
        String dstPath = currentDirectoryPath + "/dst";
        //Will initialize the channel
        BValue[] args = {new BString(srcPath), new BString(dstPath)};
        BRunUtil.invoke(fileOperationProgramFile, "testMove", args);

        BValue[] args2 = {new BString(dstPath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testPathExistance", args2);

        BBoolean existance = (BBoolean) returns[0];
        Assert.assertEquals(existance.booleanValue(), true);
    }

    @Test(description = "Test 'newByteChannel' function in ballerina.file package",
            dependsOnMethods = {"testCreateFile", "testListDirectory", "testWriteBytesToFile"})
    public void testDirectoryExistanceAndDeletion() {
        BBoolean result;
        String path = currentDirectoryPath + "/parent/child1/test.txt";
        BValue[] args = {new BString(path)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testDirectoryExistance", args);
        result = (BBoolean) returns[0];
        Assert.assertFalse(result.booleanValue());

        path = currentDirectoryPath + "/parent/child1/";
        BValue[] args2 = {new BString(path)};
        returns = BRunUtil.invoke(fileOperationProgramFile, "testDirectoryExistance", args2);
        result = (BBoolean) returns[0];
        Assert.assertTrue(result.booleanValue());

        if (!isWindows()) {
            //This tests could only be executed in non-windows environment currently
            path = currentDirectoryPath + "/parent/";
            BValue[] args3 = {new BString(path)};
            BRunUtil.invoke(fileOperationProgramFile, "deleteDirectory", args3);

            BValue[] args4 = {new BString(path)};
            returns = BRunUtil.invoke(fileOperationProgramFile, "testPathExistance", args4);
            BBoolean existance = (BBoolean) returns[0];

            Assert.assertEquals(existance.booleanValue(), false);
        }

    }
}

/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.nativeimpl.functions;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases for file functions in the ballerina/internal package.
 */
public class InternalFileTest {
    private CompileResult fileOperationProgramFile;
    private Path currentDirectoryPath = Paths.get("/tmp");
    
    @BeforeClass
    public void setup() {
        fileOperationProgramFile = BCompileUtil.compile("test-src/file.internal/file-test.bal");
        currentDirectoryPath = Paths.get(System.getProperty("user.dir")).resolve("target").resolve("file-tests");
        File workingDir = new File(currentDirectoryPath.toString());
        FileUtils.deleteQuietly(workingDir);
    }
    
    @Test(description = "Test folder creation and existence")
    public void testFolderCreation() {
        BValue[] args = {new BString(currentDirectoryPath.toString())};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "createSourceAndTargetDirs", args);
        BBoolean returnVal = (BBoolean) returns[0];
        Assert.assertTrue(returnVal.booleanValue(), "'source' and 'target' directories could not be created.");
    }
    
    @Test(description = "Test folder and file creation", dependsOnMethods = "testFolderCreation")
    public void testFolderAndFileCreation() {
        BValue[] args = {new BString(currentDirectoryPath.resolve("source").toString())};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "createFolderStructure", args);
        BBoolean returnVal = (BBoolean) returns[0];
        Assert.assertTrue(returnVal.booleanValue(), "Expected files and folders were not created.");
    }
    
    @Test(description = "Test content created by ", dependsOnMethods = "testFolderAndFileCreation")
    public void testContentCreatedInFolder() {
        BValue[] args = {new BString(currentDirectoryPath.resolve("source").toString())};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testFolderContent", args);
        BBoolean returnVal = (BBoolean) returns[0];
        Assert.assertTrue(returnVal.booleanValue(), "Invalid folder content found.");
    }
    
    @Test(description = "Test the copyTo function", dependsOnMethods = "testContentCreatedInFolder")
    public void testCopyFunction() {
        BValue[] args = {
                new BString(currentDirectoryPath.resolve("source").toString()),
                new BString(currentDirectoryPath.resolve("target").toString())
        };
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testCopyToFunction", args);
        BBoolean returnVal = (BBoolean) returns[0];
        Assert.assertTrue(returnVal.booleanValue(), "Copy function did not work correctly.");
    }
    
    @Test(description = "Test the delete folder function", dependsOnMethods = "testCopyFunction")
    public void testDeleteFolder() {
        BValue[] args = {new BString(currentDirectoryPath.resolve("target").toString())};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testFolderDelete", args);
        BBoolean returnVal = (BBoolean) returns[0];
        Assert.assertTrue(returnVal.booleanValue(), "Folder could not be delete.");
    }
    
    @Test(description = "Test the moveTo function", dependsOnMethods = "testDeleteFolder")
    public void testMoveToFunction() {
        BValue[] args = {
                new BString(currentDirectoryPath.resolve("source").toString()),
                new BString(currentDirectoryPath.resolve("target").toString())
        };
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testMoveToFunction", args);
        BBoolean returnVal = (BBoolean) returns[0];
        Assert.assertTrue(returnVal.booleanValue(), "moveTo function did not work correctly.");
    }
}

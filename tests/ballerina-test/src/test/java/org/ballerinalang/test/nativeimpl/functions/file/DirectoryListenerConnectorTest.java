/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.test.nativeimpl.functions.file;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Test class for Directory Listener connector.
 */
@Test(sequential = true)
public class DirectoryListenerConnectorTest {

    private File rootDirectory;

    @BeforeClass
    public void init() {
        try {
            Path rootListenFolderPath = Files.createDirectory(Paths.get("target", "fs"));
            rootDirectory = rootListenFolderPath.toFile();
            rootDirectory.deleteOnExit();
        } catch (IOException e) {
            Assert.fail("Unable to create root folder to setup watch.", e);
        }
    }

    @AfterClass
    public void cleanup() {
        if (rootDirectory != null) {
            try {
                Files.walk(rootDirectory.toPath(), FileVisitOption.FOLLOW_LINKS)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException ignore) {
                //Ignore
            }
        }
    }

    @Test
    public void testValidLocalFileSystemServerConnectorSyntax() {
        CompileResult compileResult = BCompileUtil.compileAndSetup("test-src/file/file-system.bal");
        BServiceUtil.runService(compileResult);
        try {
            Files.createFile(Paths.get("target", "fs", "temp.txt"));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Ignore.
        }
        final BValue[] result = BRunUtil.invokeStateful(compileResult, "isInvoked");
        BBoolean isInvoked = (BBoolean) result[0];
        Assert.assertTrue(isInvoked.booleanValue(), "Resource didn't invoke for the file create.");
    }
}

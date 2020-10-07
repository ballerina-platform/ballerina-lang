/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.test;

import io.ballerina.build.BirWriter;
import io.ballerina.build.Module;
import io.ballerina.build.Package;
import io.ballerina.projects.directory.BuildProject;
import org.ballerinalang.compiler.BLangCompilerException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains cases to test the BirWriter.
 *
 * @since 2.0.0
 */
public class TestBirWriter {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test (description = "tests writing of the BIR")
    public void testBirWriter() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();

        Path tempDirectory = Files.createTempDirectory("ballerina-test-" + System.nanoTime());
        Path tempFile = tempDirectory.resolve("test.bir");
        Assert.assertFalse(tempFile.toFile().exists());
        BirWriter.write(defaultModule, tempFile);
        long lastModifiedTime = Files.getLastModifiedTime(tempFile).toMillis();
        Assert.assertTrue(tempFile.toFile().exists());
        Assert.assertTrue(tempFile.toFile().length() > 0);

        // Test writing to an existing file
        try {
            BirWriter.write(defaultModule, tempFile);
        } catch (BLangCompilerException e) {
            Assert.assertTrue(e.getCause() instanceof FileAlreadyExistsException);
        }

        // Test force write to an existing file
        try {
            Thread.sleep(1000); // Added to ensure the document is updated
        } catch (InterruptedException e) {
            // ignore exception
        }
        BirWriter.write(defaultModule, tempFile, true);
        Assert.assertTrue(tempFile.toFile().length() > 0);
        long newLastModifiedTime = Files.getLastModifiedTime(tempFile).toMillis();
        Assert.assertTrue(newLastModifiedTime > lastModifiedTime);
    }
}

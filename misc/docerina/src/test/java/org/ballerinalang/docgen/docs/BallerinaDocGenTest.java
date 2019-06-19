/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.docs;

import org.ballerinalang.docgen.docs.utils.BallerinaDocGenTestUtils;
import org.ballerinalang.docgen.model.ModuleDoc;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 *  Ballerina doc generation tests.
 */
public class BallerinaDocGenTest {

    private final PrintStream err = System.err;
    private Path testResourceRoot;
    private String originalUserDir;

    @BeforeClass()
    public void setup() {
        testResourceRoot = Paths.get("src", "test", "resources", "balFiles");
    }

    @AfterTest
    public void cleanup() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    public void createProjectRepo(Path path) throws IOException {
        Path filePath = path.resolve(".ballerina");
        Files.deleteIfExists(filePath);
        Files.createDirectory(filePath);
    }

    public void setUserDir(Path path) {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", path.toAbsolutePath().toString());
    }

    @Test(description = "Test Single Bal file")
    public void testSingleBalFile() {
        try {
            // Generate API docs
            Map<String, ModuleDoc> docsMap = BallerinaDocGenerator
                    .generatePackageDocsFromBallerina(testResourceRoot.toAbsolutePath().toString(), "helloWorld.bal");

            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
        } catch (IOException e) {
            err.println(e);
            Assert.fail(e.getMessage());
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test a folder with Bal files")
    public void testFolderWithBalFile() {
        try {
            Path folderPath = testResourceRoot.resolve("balFolder");
            // Create .ballerina
            createProjectRepo(folderPath);
            // Set user dir
            setUserDir(folderPath);
            SourceDirectory srcDirectory = new FileSystemProjectDirectory(folderPath);
            List<String> sourcePackageNames = srcDirectory.getSourcePackageNames();
            // Output folder path
            Path outputPath = testResourceRoot.resolve("api-docs");
            // Generate API docs
            BallerinaDocGenerator.generateApiDocs(folderPath.toAbsolutePath().toString(),
                    outputPath.toAbsolutePath().toString(), null, false, true,
                    sourcePackageNames.toArray(new String[0]));
            Map<String, ModuleDoc> docsMap = BallerinaDocDataHolder.getInstance().getPackageMap();
            Assert.assertNotNull(docsMap);
            // this folder has 3 bal files. 2 bal files out of those are in same package.
            Assert.assertEquals(docsMap.size(), 2);
            // assert package names
            Assert.assertTrue(docsMap.containsKey("a.b"));
            Assert.assertTrue(docsMap.containsKey("a.b.c"));
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test doc creation for a module")
    public void testBalPackage() {
        try {
            Path folderPath = testResourceRoot.resolve("balFolder");
            // Create .ballerina
            createProjectRepo(folderPath);
            // Set user dir
            setUserDir(folderPath);
            // Output folder path
            Path outputPath = testResourceRoot.resolve("api-docs2");
            // Generate API docs
            BallerinaDocGenerator.generateApiDocs(folderPath.toAbsolutePath().toString(),
                    outputPath.toAbsolutePath().toString(), null, false, true, "a.b");

            Assert.assertEquals(BallerinaDocDataHolder.getInstance().getPackageMap().size(), 1);
            // assert package names
            Assert.assertTrue(BallerinaDocDataHolder.getInstance().getPackageMap().containsKey("a.b"));
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}

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
import org.ballerinalang.docgen.model.PackageDoc;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.io.File;
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
    private String testResourceRoot;
    private String originalUserDir;

    @BeforeClass()
    public void setup() {
        testResourceRoot = BallerinaDocGenTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    @AfterTest
    public void cleanup() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    public void createDir(String path) throws IOException {
        // TODO : Done as a workaround to create the .ballerina directory
        Path filePath = Paths.get(path + "/.ballerina");
        Files.deleteIfExists(filePath);
        Files.createDirectory(filePath);
    }

    public void setUserDir(String path) {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", path);
    }

    @Test(description = "Test Single Bal file")
    public void testSingleBalFile() {
        try {
            Map<String, PackageDoc> docsMap = BallerinaDocGenerator
                    .generatePackageDocsFromBallerina(testResourceRoot + "balFiles", "helloWorld.bal");

            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
        } catch (IOException e) {
            err.println(e);
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test a folder with Bal files")
    public void testFolderWithBalFile() {
        try {
            String path = testResourceRoot + "balFiles/balFolder";
            createDir(path);
            setUserDir(path);
            SourceDirectory srcDirectory = new FileSystemProjectDirectory(Paths.get(path));
            List<String> sourcePackageNames = srcDirectory.getSourcePackageNames();
            BallerinaDocGenerator.generateApiDocs(path, testResourceRoot + File.separator + "api-docs", null, false,
                    sourcePackageNames.toArray(new String[sourcePackageNames.size()]));
            Map<String, PackageDoc> docsMap = BallerinaDocDataHolder.getInstance().getPackageMap();
            Assert.assertNotNull(docsMap);
            // this folder has 3 bal files. 2 bal files out of those are in same package.
            Assert.assertEquals(docsMap.size(), 2);
            // assert package names
            Assert.assertEquals(docsMap.containsKey("a.b"), true);
            Assert.assertEquals(docsMap.containsKey("a.b.c"), true);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test doc creation for a package")
    public void testBalPackage() {
        try {
            String path = testResourceRoot + "balFiles/balFolder";
            createDir(path);
            setUserDir(path);
            BallerinaDocGenerator.generateApiDocs(path, testResourceRoot + File.separator + "api-docs2", null, false,
                    "a.b");

            Assert.assertEquals(BallerinaDocDataHolder.getInstance().getPackageMap().size(), 1);
            // assert package names
            Assert.assertEquals(BallerinaDocDataHolder.getInstance().getPackageMap().containsKey("a.b"), true);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.docerina;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test case for checking docs has generated in the server home.
 */
public class BallerinaApiDocsTestCase extends BaseTest {

    private static final String DOCS = "docs";
    private static final String INDEX_HTML = "index.html";

    @Test(enabled = false, description = "Test docs directory generated in the server home")
    public void testDocsDirectory() {
        Assert.assertTrue(Files.exists(Paths.get(balServer.getServerHome(), DOCS)));
        Assert.assertTrue(Files.exists(Paths.get(balServer.getServerHome(), DOCS, INDEX_HTML)));
    }

    @Test(enabled = false, description = "Test docs directories of bir-cache modules",
            dependsOnMethods = "testDocsDirectory")
    public void testBirCacheModuleDocs() throws BallerinaTestException {
        PrintStream out = System.out;
        List<String> skipModules = new ArrayList<>(
                Arrays.asList("lang.__internal", "lang.annotations", "testobserve",
                        "lang.query", "lang.typedesc"));
        Path birCacheBallerinaDir = Paths.get(balServer.getServerHome(), "bir-cache", "ballerina");
        File[] birCacheBalDirFiles = new File(String.valueOf(birCacheBallerinaDir)).listFiles();

        if (birCacheBalDirFiles == null || birCacheBalDirFiles.length < 1) {
            throw new BallerinaTestException("bir-cache directory is empty");
        }

        for (final File fileEntry : birCacheBalDirFiles) {
            if (fileEntry.isDirectory() && (!skipModules.contains(fileEntry.getName()))) {
                out.println("testing docs of the bir-cache module: " + fileEntry.getName());
                Assert.assertTrue(Files.exists(Paths.get(balServer.getServerHome(), DOCS, fileEntry.getName())));
                Assert.assertTrue(
                        Files.exists(Paths.get(balServer.getServerHome(), DOCS, fileEntry.getName(), INDEX_HTML)));
            }
        }
    }

    @Test(enabled = false, description = "Test auth module docs", dependsOnMethods = "testBirCacheModuleDocs")
    public void testAuthModuleDocs() {
        Path authDocsDir = Paths.get(balServer.getServerHome(), DOCS, "auth");
        Assert.assertTrue(Files.exists(authDocsDir));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(authDocsDir), INDEX_HTML)));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(authDocsDir), "constants.html")));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(authDocsDir), "errors.html")));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(authDocsDir), "functions.html")));

        Path classDir = Paths.get(String.valueOf(authDocsDir), "bClasses");
        Assert.assertTrue(Files.exists(classDir));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(classDir), "InboundAuthProvider.html")));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(classDir), "InboundBasicAuthProvider.html")));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(classDir), "OutboundAuthProvider.html")));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(classDir), "OutboundBasicAuthProvider.html")));

        Path recordsDir = Paths.get(String.valueOf(authDocsDir), "records");
        Assert.assertTrue(Files.exists(recordsDir));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(recordsDir), "BasicAuthConfig.html")));
        Assert.assertTrue(Files.exists(Paths.get(String.valueOf(recordsDir), "Credential.html")));
    }
}

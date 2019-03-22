/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.packaging;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Testing pushing, pulling, searching a module from central and installing module to home repository.
 *
 * @since 0.982.0
 */
@Ignore
public class ListDependencyTestCase extends BaseTest {
    private Path tempProjectDirectory;

    @BeforeClass()
    public void setUp() throws IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        // Copy sources
        String projectPath = (new File("src/test/resources/list")).getAbsolutePath();
        FileUtils.copyDirectory(Paths.get(projectPath).toFile(), tempProjectDirectory.toFile());
        // Create .ballerina
        Files.createDirectories(tempProjectDirectory.resolve(".ballerina"));
    }

    @Test(description = "Test listing dependencies of a module")
    public void testListDependenciesOfModule() throws Exception {
        String[] clientArgs = {"foo"};

        String expectedOut = "myorg/foo:1.0.0\n" +
                "├── ballerina/io\n" +
                "└── ballerina/math\n";

        String actualOut = balClient.runMainAndReadStdOut("list", clientArgs, tempProjectDirectory.toString());
        Assert.assertEquals(actualOut, expectedOut);
    }

    @Test(description = "Test listing dependencies of a single bal file")
    public void testListDependenciesOfBalFile() throws Exception {
        String[] clientArgs = {"bar.bal"};

        String expectedOut = "bar.bal\n" +
                "├── ballerina/http\n" +
                "│   ├── ballerina/internal\n" +
                "│   │   ├── ballerina/time\n" +
                "│   │   └── ballerina/log\n" +
                "│   ├── ballerina/system\n" +
                "│   ├── ballerina/config\n" +
                "│   │   └── ballerina/system\n" +
                "│   ├── ballerina/math\n" +
                "│   ├── ballerina/crypto\n" +
                "│   ├── ballerina/mime\n" +
                "│   │   ├── ballerina/io\n" +
                "│   │   └── ballerina/file\n" +
                "│   ├── ballerina/file\n" +
                "│   ├── ballerina/time\n" +
                "│   ├── ballerina/io\n" +
                "│   ├── ballerina/runtime\n" +
                "│   ├── ballerina/cache\n" +
                "│   │   ├── ballerina/time\n" +
                "│   │   ├── ballerina/task\n" +
                "│   │   └── ballerina/system\n" +
                "│   ├── ballerina/log\n" +
                "│   ├── ballerina/reflect\n" +
                "│   └── ballerina/auth\n" +
                "│       ├── ballerina/system\n" +
                "│       ├── ballerina/time\n" +
                "│       ├── ballerina/log\n" +
                "│       ├── ballerina/internal\n" +
                "│       │   ├── ballerina/time\n" +
                "│       │   └── ballerina/log\n" +
                "│       ├── ballerina/cache\n" +
                "│       │   ├── ballerina/time\n" +
                "│       │   ├── ballerina/task\n" +
                "│       │   └── ballerina/system\n" +
                "│       ├── ballerina/runtime\n" +
                "│       └── ballerina/config\n" +
                "│           └── ballerina/system\n" +
                "├── ballerina/io\n" +
                "└── ballerina/log\n";
        String actualOut = balClient.runMainAndReadStdOut("list", clientArgs, tempProjectDirectory.toString());
        Assert.assertEquals(actualOut, expectedOut);
    }

    @Test(description = "Test listing dependencies of a project")
    public void testListDependenciesOfProject() throws Exception {
        String expectedOut = "Compiling source\n" +
                "    bar.bal\n" +
                "    myorg/foo:1.0.0\n" +
                "bar.bal\n" +
                "├── ballerina/http\n" +
                "│   ├── ballerina/internal\n" +
                "│   │   ├── ballerina/time\n" +
                "│   │   └── ballerina/log\n" +
                "│   ├── ballerina/system\n" +
                "│   ├── ballerina/config\n" +
                "│   │   └── ballerina/system\n" +
                "│   ├── ballerina/math\n" +
                "│   ├── ballerina/crypto\n" +
                "│   ├── ballerina/mime\n" +
                "│   │   ├── ballerina/io\n" +
                "│   │   └── ballerina/file\n" +
                "│   ├── ballerina/file\n" +
                "│   ├── ballerina/time\n" +
                "│   ├── ballerina/io\n" +
                "│   ├── ballerina/runtime\n" +
                "│   ├── ballerina/cache\n" +
                "│   │   ├── ballerina/time\n" +
                "│   │   ├── ballerina/task\n" +
                "│   │   └── ballerina/system\n" +
                "│   ├── ballerina/log\n" +
                "│   ├── ballerina/reflect\n" +
                "│   └── ballerina/auth\n" +
                "│       ├── ballerina/system\n" +
                "│       ├── ballerina/time\n" +
                "│       ├── ballerina/log\n" +
                "│       ├── ballerina/internal\n" +
                "│       │   ├── ballerina/time\n" +
                "│       │   └── ballerina/log\n" +
                "│       ├── ballerina/cache\n" +
                "│       │   ├── ballerina/time\n" +
                "│       │   ├── ballerina/task\n" +
                "│       │   └── ballerina/system\n" +
                "│       ├── ballerina/runtime\n" +
                "│       └── ballerina/config\n" +
                "│           └── ballerina/system\n" +
                "├── ballerina/io\n" +
                "└── ballerina/log\n" +
                "\n" +
                "myorg/foo:1.0.0\n" +
                "├── ballerina/io\n" +
                "└── ballerina/math\n";

        String actualOut = balClient.runMainAndReadStdOut("list", new String[0], tempProjectDirectory.toString());
        Assert.assertEquals(actualOut, expectedOut);
    }

    @AfterClass
    private void cleanup() throws Exception {
        TestUtils.deleteFiles(tempProjectDirectory);
    }
}

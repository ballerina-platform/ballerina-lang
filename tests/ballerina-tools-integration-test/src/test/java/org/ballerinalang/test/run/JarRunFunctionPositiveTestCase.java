/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.run;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class tests invoking main function in a bal for jvm target via Ballerina Build Command and run it the jar for
 * testing given outputs.
 */
public class JarRunFunctionPositiveTestCase extends BaseTest {

    private static final String JAR_NAME = "main_test.jar";

    private Path tempProjectDir;

    private String jarPath;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDir = Files.createTempDirectory("temp-jar-func-test");
        jarPath = Paths.get(tempProjectDir.toString(), JAR_NAME).toString();
        String[] clientArgs = {
                "--jvmTarget", "-o", jarPath,
                (new File("src/test/resources/run/jar/test_main_for_jvm_target.bal")).getAbsolutePath()
        };
        balClient.runMain("build", clientArgs, null, new String[0], new LogLeecher[0], tempProjectDir.toString());
    }

    @Test
    public void runJar() throws BallerinaTestException {
        String output = balClient.runMainAndReadStdOut("run", new String[] { new File(jarPath).getAbsolutePath() },
                                                       tempProjectDir.toString());
        Assert.assertEquals(output, "jvm main {}");
    }

    @AfterClass
    public void tearDown() throws BallerinaTestException {
        try {
            TestUtils.deleteFiles(tempProjectDir);
        } catch (IOException e) {
            throw new BallerinaTestException("Error deleting files");
        }
    }
}

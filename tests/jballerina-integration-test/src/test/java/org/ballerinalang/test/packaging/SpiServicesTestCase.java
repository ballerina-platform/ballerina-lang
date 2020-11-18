/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.packaging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;

/**
 * Test case for test packaging jar with java spi implementation definitions.
 */
public class SpiServicesTestCase extends BaseTest {
    private BMainInstance balClient;
    private Path testProjectPath;

    /**
     * Initialize the client.
     *
     * @throws BallerinaTestException When creating the ballerina client.
     */
    @BeforeClass()
    public void setUp() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
    }

    /**
     * Run TestProject and check jar has created correctly with spi implementation definitions.
     *
     * @throws BallerinaTestException When running commands.
     */
    @Test(description = "Test spi service are merged")
    public void testSpiServicesMerge() throws BallerinaTestException, IOException {
        testProjectPath = Paths.get("src", "test", "resources", "packaging", "spi", "TestProject")
                .toAbsolutePath();
        balClient.runMain("build", new String[]{"module"}, new HashMap<>(), new String[0],
                new LogLeecher[]{}, testProjectPath.toString());
        Path jarPath = Paths.get(this.testProjectPath.toString(), "target", "bin", "module" + ".jar");
        JarFile jar = new JarFile(jarPath.toFile());
        StringBuilder serviceList = new StringBuilder();
        //Check java.spi.service.test service implementations.
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
            JarEntry entry = enums.nextElement();
            if (entry.getName().endsWith("java.spi.service.test")) {
                try (BufferedReader fromBr = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)))) {
                    String text;
                    while ((text = fromBr.readLine()) != null) {
                        if (!text.equals("")) {
                            serviceList.append(" ").append(text);
                        }
                    }
                }
                break;
            }
        }
        String result = serviceList.toString();
        Assert.assertTrue(result.contains("serviceA"), "serviceA cannot be found in service List :" + result);
        Assert.assertTrue(result.contains("serviceB"), "serviceB cannot be found in service List :" + result);
        Assert.assertEquals(result.length(), 18);
        LogLeecher jarRunLeecher = new LogLeecher("org.hsqldb.jdbc.JDBCDriver");
        balClient.runMain("run", new String[]{jarPath.toString()}, new HashMap<>(), new String[0],
                new LogLeecher[]{jarRunLeecher}, testProjectPath.toString());
        jarRunLeecher.waitForText(2000);

        LogLeecher balRunLeecher = new LogLeecher("org.hsqldb.jdbc.JDBCDriver");
        balClient.runMain("run", new String[]{"module"}, new HashMap<>(), new String[0],
                new LogLeecher[]{balRunLeecher}, testProjectPath.toString());
        balRunLeecher.waitForText(2000);
    }

    @AfterClass()
    private void cleanup() throws Exception {
        deleteFiles(Paths.get(this.testProjectPath.toString(), "target").toAbsolutePath());
    }
}

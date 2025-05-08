/*
 *  Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.tools;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class InBuiltToolTests extends BaseTestCase {
    private BMainInstance balClient;
    private final String userDir = System.getProperty("user.dir");

    /* Test cases
     * 1. Only in dist - persist
     * 2. Only local - consolidate-packages
     * 3. Both in dist and local
     * 3.1 Locally active is compatible
     * 3.1.1 locally active < dist - edi
     * 3.1.2 locally active >= dist - graphql
     * 3.2 Local active is incompatible
     * 3.2.1 Dist > locals - openapi
     * 3.2.2 Locals > dist - asyncapi
     * 4 Distribution bump to higher version
     * 4.1 Dist > locally active - grpc
     * */

    @Test (description = "Test a tool that is only available in dist")
    public void testToolOnlyInDist() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String output = balClient.runMainAndReadStdOut("persist1", new String[]{},
                new HashMap<>(), userDir, true);
        Assert.assertEquals(output, "persist1 0.9.0");
    }

    @Test (description = "Test a tool that is only available in central cache")
    public void testToolOnlyInCentralCache() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String output = balClient.runMainAndReadStdOut("consolidate-packages", new String[]{},
                new HashMap<>(), userDir, true);
        Assert.assertEquals(output, "consolidate-packages 0.1.7");
    }

    @Test (description = "Test a tool that is available in both dist and local. " +
            "Locally active version is higher than the version in dist and compatible")
    public void testDistLowerThanLocalActive() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String output = balClient.runMainAndReadStdOut("graphql1", new String[]{},
                new HashMap<>(), userDir, true);
        Assert.assertEquals(output, "graphql1 1.1.5");
    }

    @Test (description = "Test a tool that is available in both dist and local. " +
            "Locally active version is less than the version in dist but compatible")
    public void testDistHigherThanLocalActive() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String output = balClient.runMainAndReadStdOut("edi", new String[]{},
                new HashMap<>(), userDir, true);
        Assert.assertEquals(output, "edi 1.1.0");
    }

    @Test (description = "Locally active version is incompatible. Dist has the highest compatible version")
    public void testLocalActiveIsIncompatible1() throws BallerinaTestException {
        // 1.2.2 is ignored since the repo is local)
        balClient = new BMainInstance(balServer);
        String output = balClient.runMainAndReadStdOut("openapi1", new String[]{},
                new HashMap<>(), userDir, true);
        Assert.assertEquals(output, "openapi1 1.2.1");
    }

    @Test (description = "Locally active version is incompatible. Local has the highest compatible version")
    public void testLocalActiveIsIncompatible2() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String output = balClient.runMainAndReadStdOut("asyncapi1", new String[]{},
                new HashMap<>(), userDir, true);
        Assert.assertEquals(output, "asyncapi1 1.1.0");
    }

    @Test (description = "Locally active version is compatible. Dist has upgraded")
    public void testDistributionBumpToHigher() throws BallerinaTestException, IOException {
        String projectVersion = System.getenv("PROJECT_VERSION");
        String dist = System.getProperty("server.path");
        Path packageJson = Paths.get(dist).resolve("repo/bala/ballerina/tool_grpc/1.1.0/java21/package.json");
        String content = Files.readString(packageJson);
        content = content.replaceAll("<PROJECT_VERSION>", projectVersion);
        Files.write(packageJson, content.getBytes());
        balClient = new BMainInstance(balServer);
        String output = balClient.runMainAndReadStdOut("grpc1", new String[]{},
                new HashMap<>(), userDir, true);
        Assert.assertEquals(output, "grpc1 1.1.0");
    }
}

/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.ballerinalang.test.tools;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/*
 * This class contains test cases for tool automation.
 *
 * @since 2201.13.0
 */
public class InBuiltToolAutomationTests extends BaseTestCase {
    private BMainInstance balClient;
    private final String userDir = System.getProperty("user.dir");
    private final Path projectsRoot = Paths.get("build/projects");

    @Test
    public void testProjectWithToolOnlyInDist() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String projectPath = projectsRoot.resolve("projectWithOneTool").toString();
        String output = balClient.runMainAndReadStdOut("build", new String[]{projectPath},
                new HashMap<>(), userDir, true);
        Assert.assertTrue(output.contains("dummytoolF 0.9.0"), output);
    }

    @Test
    public void testProjectWithDepsToml() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String projectPath = projectsRoot.resolve("projectWithOneToolWithDepsToml").toString();
        String output = balClient.runMainAndReadStdOut("build", new String[]{projectPath},
                new HashMap<>(), userDir, true);
        Assert.assertTrue(output.contains("dummytoolA 1.2.1"), output);
    }

    @Test
    public void testProjectWithDepsTomlAndSticky() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String projectPath = projectsRoot.resolve("projectWithOneToolWithDepsTomlAndSticky").toString();
        String output = balClient.runMainAndReadStdOut("build", new String[]{"--sticky", projectPath},
                new HashMap<>(), userDir, true);
        Assert.assertTrue(output.contains("dummytoolA 1.2.0"), output);
    }

    @Test
    public void testProjectWithIncompatibleToolVersion() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String projectPath = projectsRoot.resolve("projectWithIncompatibleVersionOfTool").toString();
        String output = balClient.runMainAndReadStdOut("build", new String[]{projectPath},
                new HashMap<>(), userDir, true);
        Assert.assertTrue(output.contains("Build tool 'dummytoolA:1.3.0' cannot be resolved"), output);
    }

    @Test
    public void testProjectWithMultipleTools() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        String projectPath = projectsRoot.resolve("projectWithMultipleTools").toString();
        String output = balClient.runMainAndReadStdOut("build", new String[]{"--offline", projectPath},
                new HashMap<>(), userDir, true);
        Assert.assertTrue(output.contains("dummytoolF 0.9.0"), output);
        Assert.assertTrue(output.contains("dummytoolA 1.2.1"), output);
        Assert.assertTrue(output.contains("dummyToolX 0.1.7"), output);
    }
}

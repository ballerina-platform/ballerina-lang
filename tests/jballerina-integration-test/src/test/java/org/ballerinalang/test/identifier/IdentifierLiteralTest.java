/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.identifier;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test cases to verify name clashes between generated identifiers.
 *
 *  @since 2.0.0
 */
public class IdentifierLiteralTest  extends BaseTest {

    private static final String testFileLocation = Paths.get("src", "test", "resources", "identifier")
            .toAbsolutePath().toString();
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test(description = "Test clashes in module names contain '.' and '_'")
    public void testModuleIdentifierClash() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "ModuleNameClashProject")
                .toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("1 passing");
        bMainInstance.runMain("test", new String[0], new HashMap<>(), new String[0],
                new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    @Test(description = "Test clashes in organization and module names that contain '_'")
    public void testPackageIDClash() throws BallerinaTestException {
        String importProjectPath = testFileLocation + File.separator + "testProject";
        LogLeecher buildLeecher = new LogLeecher("target/bala/a_b-foo-any-0.1.0.bala");
        LogLeecher pushLeecher =
                new LogLeecher("Successfully pushed target/bala/a_b-foo-any-0.1.0.bala to 'local' repository.");
        LogLeecher runLeecher = new LogLeecher("Tests passed");
        bMainInstance.runMain("pack", new String[]{}, null, null, new LogLeecher[]{buildLeecher},
                importProjectPath);
        buildLeecher.waitForText(5000);
        bMainInstance.runMain("push", new String[]{"--repository=local"}, null, null, new LogLeecher[]{pushLeecher},
                importProjectPath);
        pushLeecher.waitForText(5000);

        bMainInstance.runMain("run", new String[0], null, new String[0], new LogLeecher[]{runLeecher},
                testFileLocation + File.separator + "PackageNameClashProject");
        runLeecher.waitForText(5000);
    }

    @Test(description = "Test identifier clashes between generated and user defined identifiers")
    public void testUserDefinedIdentifierClash() throws BallerinaTestException {
        String testBalFile = "identifier_clash.bal";

        // Run and see output
        String msg = "Tests passed";
        LogLeecher runLeecher = new LogLeecher(msg);
        String runCommandPath =  testFileLocation + File.separator + testBalFile;
        bMainInstance.runMain("run", new String[]{runCommandPath}, new HashMap<>(), new String[0],
                new LogLeecher[]{runLeecher}, testFileLocation);
        runLeecher.waitForText(5000);
    }
}

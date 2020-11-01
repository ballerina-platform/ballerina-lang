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

    @BeforeClass(enabled = false)
    public void setup() throws BallerinaTestException {
    }

    @Test(description = "Test clashes in module names contain '.' and '_'")
    public void testModuleIdentifierClash() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "ModuleNameClashProject")
                .toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("1 passing");
        BMainInstance bMainInstance = new BMainInstance(balServer);
        bMainInstance.runMain("build", new String[]{"main"}, new HashMap<>(), new String[0],
                new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    @Test(description = "Test clashes in organization and module names that contain '_'")
    public void testPackageIDClash() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "PackageNameClashProject")
                .toAbsolutePath();
        Path importProjectPath = Paths.get(testFileLocation, "testProject")
                .toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("2 passing");
        BMainInstance bMainInstance = new BMainInstance(balServer);
        bMainInstance.runMain("build", new String[]{"foo"}, new HashMap<>(), new String[0],
                new LogLeecher[]{}, importProjectPath.toString());
        bMainInstance.runMain("build", new String[]{"main"}, new HashMap<>(), new String[0],
                new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }
}

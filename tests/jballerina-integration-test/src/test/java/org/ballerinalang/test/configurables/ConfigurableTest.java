/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.test.configurables;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.test.context.LogLeecher.LeecherType.ERROR;

/**
 * Test cases for checking configurable variables in Ballerina.
 */
public class ConfigurableTest extends BaseTest {

    private static final String testFileLocation = Paths.get("src", "test", "resources", "configurables")
            .toAbsolutePath().toString();
    private static final String negativeTestFileLocation =
            Paths.get(testFileLocation, "NegativeTests").toAbsolutePath().toString();
    private BMainInstance bMainInstance;
    private final String errorMsg = "error: Invalid `configuration.toml` file : ";

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testAccessConfigurablePrimitiveVariables() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "configurableProject").toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("Tests passed");
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    @Test
    public void testAccessForImportedModules() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "multiModuleProject").toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("Tests passed");
        bMainInstance.runMain("run", new String[]{"configPkg"}, null, new String[]{},
                new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    @Test
    public void testSingleBalFileWithConfigurables() throws BallerinaTestException {
        Path filePath = Paths.get(testFileLocation, "configTest.bal").toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("Tests passed");
        bMainInstance.runMain("run", new String[]{filePath.toString()}, null, new String[]{},
                new LogLeecher[]{runLeecher}, testFileLocation);
        runLeecher.waitForText(5000);
    }

    /** Negative test cases. */
    @Test
    public void testNoConfigFile() throws BallerinaTestException {
        Path filePath = Paths.get(negativeTestFileLocation, "noConfig.bal").toAbsolutePath();
        LogLeecher errorLeecher = new LogLeecher("error: Configuration toml file `configuration.toml` is not found",
                ERROR);
        bMainInstance.runMain("run", new String[]{filePath.toString()}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, testFileLocation + "/NegativeTests");
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidTomlFile() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "InvalidTomlFile").toAbsolutePath();
        String tomlError = "Invalid table definition on line 1: [testOrg..main.file..]]";
        LogLeecher errorLeecher = new LogLeecher(errorMsg + tomlError, ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidOrganizationName() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "InvalidOrgName").toAbsolutePath();
        LogLeecher errorLeecher = new LogLeecher(errorMsg + "Organization name 'testOrg' not found.", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidType() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "InvalidType").toAbsolutePath();
        String typeError = "invalid type found for variable 'intVar', expected type is 'int'";
        LogLeecher errorLeecher = new LogLeecher(errorMsg + typeError, ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testRequiredVariableNotFound() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "RequiredNegative").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Value not provided for required configurable variable 'stringVar'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

}

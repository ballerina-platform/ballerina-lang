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

import io.ballerina.runtime.internal.configurable.ConfigurableConstants;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.packaging.PackerinaTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
    private final String errorMsg = "error: Invalid `Config.toml` file : ";

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testAccessConfigurableVariables() throws BallerinaTestException {
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
    public void testBallerinaTestAPIWithConfigurableVariables() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "testProject").toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("4 passing");
        bMainInstance.runMain("test", new String[]{"configPkg"}, null, new String[]{},
                new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    @Test
    public void testAPIConfigFilePathOverRiding() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "testPathProject").toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("4 passing");
        bMainInstance.runMain("test", new String[]{"configPkg"}, null, new String[]{},
                new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    @Test
    public void testEnvironmentVariableBasedConfigFile() throws BallerinaTestException {
        String configFilePath = Paths.get(testFileLocation, "ConfigFiles", "Config.toml").toString();
        Path projectPath = Paths.get(testFileLocation).toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("Tests passed");
        bMainInstance.runMain("run", new String[]{"envVarPkg"}, addEnvVariables(configFilePath),
                new String[]{}, new LogLeecher[]{runLeecher}, projectPath.toString());
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
        LogLeecher errorLeecher = new LogLeecher("error: Value not provided for required configurable variable 'name'",
                ERROR);
        bMainInstance.runMain("run", new String[]{filePath.toString()}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, testFileLocation + "/NegativeTests");
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidTomlFile() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "InvalidTomlFile").toAbsolutePath();
        String tomlError1 = "missing identifier [Config.toml:(0:9,0:9)]";
        String tomlError2 = "missing identifier [Config.toml:(0:20,0:20)]";
        String tomlError3 = "missing identifier [Config.toml:(0:21,0:21)]";
        LogLeecher errorLeecher1 = new LogLeecher(errorMsg, ERROR);
        LogLeecher errorLeecher2 = new LogLeecher(tomlError1, ERROR);
        LogLeecher errorLeecher3 = new LogLeecher(tomlError2, ERROR);
        LogLeecher errorLeecher4 = new LogLeecher(tomlError3, ERROR);

        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher1, errorLeecher2, errorLeecher3, errorLeecher4}, projectPath.toString());
        errorLeecher1.waitForText(5000);
        errorLeecher2.waitForText(5000);
        errorLeecher3.waitForText(5000);
        errorLeecher4.waitForText(5000);
    }

    @Test
    public void testInvalidOrganizationName() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "InvalidOrgName").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Value not provided for required configurable variable 'booleanVar'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidType() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "InvalidType").toAbsolutePath();
        String typeError = "invalid type found for variable 'intVar', expected type is 'int', found 'DOUBLE'";
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

    //Need to provide proper error messages after fixing #28018
    @Test
    public void testNoModuleInTOML() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "NoModuleConfig").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Value not provided for required configurable variable 'intVar'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    /**
     * Get environment variables and add config file path as an env variable.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(String configFilePath) {
        Map<String, String> envVariables = PackerinaTestUtils.getEnvVariables();
        envVariables.put(ConfigurableConstants.CONFIG_ENV_VARIABLE, configFilePath);
        return envVariables;
    }

}

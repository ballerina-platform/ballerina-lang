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
            Paths.get(testFileLocation, "negative_tests").toAbsolutePath().toString();
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
        String configFilePath = Paths.get(testFileLocation, "config_files", "Config.toml").toString();
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

    @Test
    public void testRecordValueWithModuleClash() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "recordModuleProject").toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("Tests passed");
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    /** Negative test cases. */
    @Test
    public void testNoConfigFile() throws BallerinaTestException {
        Path filePath = Paths.get(negativeTestFileLocation, "no_config.bal").toAbsolutePath();
        LogLeecher errorLeecher = new LogLeecher("error: Value not provided for required configurable variable 'name'",
                ERROR);
        bMainInstance.runMain("run", new String[]{filePath.toString()}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, testFileLocation + "/negative_tests");
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidTomlFile() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "invalidTomlFile").toAbsolutePath();
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
        Path projectPath = Paths.get(negativeTestFileLocation, "invalidOrgName").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Value not provided for required configurable variable 'booleanVar'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidType() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "invalidType").toAbsolutePath();
        String typeError = "invalid type found for variable 'intVar', expected type is 'int', found 'DOUBLE'";
        LogLeecher errorLeecher = new LogLeecher(errorMsg + typeError, ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testRequiredVariableNotFound() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "requiredNegative").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Value not provided for required configurable variable 'stringVar'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    //Need to provide proper error messages after fixing #28018
    @Test
    public void testNoModuleInTOML() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "noModuleConfig").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Value not provided for required configurable variable 'intVar'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                              new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testUnsupportedRecordField() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "invalidRecordField").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Configurable feature is yet to be supported for field type " +
                                       "'string[][]' in variable 'testUser' of record 'main:AuthInfo'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                              new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidAdditionalRecordField() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "additionalField").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Additional field 'scopes' provided for configurable variable 'testUser' of record " +
                                       "'main:AuthInfo' is not supported", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                              new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testRequiredFieldNotFound() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "missingRequiredField").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Value not provided for non-defaultable required field 'username' of " +
                                       "record 'main:AuthInfo' in configurable variable 'testUser'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                              new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testTableKeyNotFound() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "missingTableKey").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Value required for key 'username' of type " +
                                       "'table<(main:AuthInfo & readonly)> key(username) & readonly'" +
                                       " in configurable variable 'users'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                              new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testUnsupportedMap() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "invalidMapType").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("Configurable feature is yet to be supported for type 'map<int> & readonly'", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                              new LogLeecher[]{errorLeecher}, projectPath.toString());
        errorLeecher.waitForText(5000);
    }

    // Encrypted Config related tests
    @Test
    public void testSingleBalFileWithEncryptedConfigs() throws BallerinaTestException {
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "correctSecret.txt").toString();
        Path singleBalFilePath = Paths.get(testFileLocation, "encryptedSingleBalFile").toAbsolutePath();
        LogLeecher runLeecher = new LogLeecher("Tests passed");
        bMainInstance.runMain("run", new String[]{"encryptedConfig.bal"}, addSecretEnvVariable(secretFilePath),
                              new String[]{}, new LogLeecher[]{runLeecher}, singleBalFilePath.toString());
        runLeecher.waitForText(5000);
    }

    @Test
    public void testEncryptedConfigs() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "encryptedConfigProject").toAbsolutePath();
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "correctSecret.txt").toString();
        LogLeecher runLeecher = new LogLeecher("Tests passed");
        bMainInstance.runMain("run", new String[]{"main"}, addSecretEnvVariable(secretFilePath), new String[]{},
                              new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    @Test
    public void testEncryptedConfigsWithIncorrectSecret() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "encryptedConfigProject").toAbsolutePath();
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "incorrectSecret.txt").toString();
        LogLeecher runLeecher = new LogLeecher("error: failed to retrieve the encrypted value for variable: " +
                                                       "'password' : Given final block not properly padded. Such " +
                                                       "issues can arise if a bad key is used during decryption.",
                                               LogLeecher.LeecherType.ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, addSecretEnvVariable(secretFilePath), new String[]{},
                              new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    @Test
    public void testEncryptedConfigsWithEmptySecret() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "encryptedConfigProject").toAbsolutePath();
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "emptySecret.txt").toString();
        LogLeecher runLeecher = new LogLeecher("error: failed to initialize the cipher tool due to empty secret text",
                                               LogLeecher.LeecherType.ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, addSecretEnvVariable(secretFilePath), new String[]{},
                              new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidAccessEncryptedConfigs() throws BallerinaTestException {
        Path projectPath = Paths.get(testFileLocation, "encryptedConfigProject").toAbsolutePath();
        String configFilePath = Paths.get(testFileLocation, "ConfigFiles", "InvalidEncryptedConfig.toml").toString();
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "correctSecret.txt").toString();
        LogLeecher runLeecher = new LogLeecher("error: failed to retrieve the encrypted value for variable: " +
                                                       "'password' : Input byte array has wrong 4-byte ending unit",
                                               LogLeecher.LeecherType.ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, addEnvVariables(configFilePath, secretFilePath),
                              new String[]{},
                              new LogLeecher[]{runLeecher}, projectPath.toString());
        runLeecher.waitForText(5000);
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

    private Map<String, String> addSecretEnvVariable(String secretFilePath) {
        Map<String, String> envVariables = PackerinaTestUtils.getEnvVariables();
        envVariables.put(ConfigurableConstants.CONFIG_SECRET_ENV_VARIABLE, secretFilePath);
        return envVariables;
    }

    private Map<String, String> addEnvVariables(String configFilePath, String secretFilePath) {
        Map<String, String> envVariables = PackerinaTestUtils.getEnvVariables();
        envVariables.put(ConfigurableConstants.CONFIG_ENV_VARIABLE, configFilePath);
        envVariables.put(ConfigurableConstants.CONFIG_SECRET_ENV_VARIABLE, secretFilePath);
        return envVariables;
    }


}

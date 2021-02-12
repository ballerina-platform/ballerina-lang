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
import org.testng.annotations.DataProvider;
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
    private final LogLeecher testsPassed = new LogLeecher("Tests passed");

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testAccessConfigurableVariables() throws BallerinaTestException {
        executeBalCommand("/configurableProject", testsPassed, "run", "main", null);
    }

    @Test
    public void testAccessForImportedModules() throws BallerinaTestException {
        executeBalCommand("/multiModuleProject", testsPassed, "run", "configPkg", null);
    }

    @Test
    public void testBallerinaTestAPIWithConfigurableVariables() throws BallerinaTestException {
        executeBalCommand("/testProject", new LogLeecher("4 passing"), "test", "configPkg", null);
    }

    @Test
    public void testAPIConfigFilePathOverRiding() throws BallerinaTestException {
        executeBalCommand("/testPathProject", new LogLeecher("4 passing"), "test", "configPkg", null);
    }

    @Test
    public void testEnvironmentVariableBasedConfigFile() throws BallerinaTestException {
        String configFilePath = Paths.get(testFileLocation, "config_files", "Config.toml").toString();
        executeBalCommand("", testsPassed, "run", "envVarPkg", addEnvVariables(configFilePath));
    }

    @Test
    public void testSingleBalFileWithConfigurables() throws BallerinaTestException {
        String filePath = testFileLocation + "/configTest.bal";
        executeBalCommand("", testsPassed, "run", filePath, null);
    }

    @Test
    public void testRecordValueWithModuleClash() throws BallerinaTestException {
        executeBalCommand("/recordModuleProject", testsPassed, "run", "main", null);
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

    @DataProvider(name = "negative-projects")
    public Object[][] getNegativeTestProjects() {
        return new Object[][]{
                {"invalidComplexArray", "Configurable feature is yet to be supported for type" +
                        " '(int[] & readonly)[] & readonly' used in variable 'intComplexArr'" },
                {"invalidRecordField", "Configurable feature is yet to be supported for field type " +
                        "'string[][]' in variable 'testUser' of record 'main:AuthInfo'"},
                {"invalidMapType", "Configurable feature is yet to be supported for type 'map<int> & readonly'"},
                {"invalidByteRange", "Value provided for byte variable 'byteVar' is out of range. Expected " +
                        "range is (0-255), found '355'"}
        };
    }

    @Test(dataProvider = "negative-projects")
    public void testNegativeCasesInProjects(String projectName, String errorMsg) throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, projectName).toAbsolutePath();
        LogLeecher errorLog = new LogLeecher(errorMsg, ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLog}, projectPath.toString());
        errorLog.waitForText(5000);
    }

    @Test(dataProvider = "negative-tests")
    public void testNegativeCases(String tomlFileName, String errorMsg) throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "configProject").toAbsolutePath();
        Path tomlPath = Paths.get(negativeTestFileLocation, "config_files", tomlFileName  + ".toml").toAbsolutePath();
        LogLeecher errorLog = new LogLeecher(errorMsg, ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, addEnvVariables(tomlPath.toString()), new String[]{},
                new LogLeecher[]{errorLog}, projectPath.toString());
        errorLog.waitForText(5000);
    }

    @DataProvider(name = "negative-tests")
    public Object[][] getNegativeTests() {
        return new Object[][]{
                {"no_module_config", "Value not provided for required configurable variable 'stringVar'"},
                {"invalid_org_name", "Value not provided for required configurable variable 'stringVar'" },
                {"invalid_org_structure", "invalid module structure found for module 'testOrg.main'. " +
                        "Please provide the module name as '[testOrg.main]'" },
                {"invalid_module_structure", "invalid module structure found for module 'main'. " +
                        "Please provide the module name as '[main]'" },
                {"invalid_sub_module_structure", "invalid module structure found for module 'foo'. " +
                        "Please provide the module name as '[foo]'" },
                {"required_negative", "Value not provided for required configurable variable 'stringVar'"},
                {"primitive_type_error", "invalid type found for variable 'intVar', expected TOML type " +
                        "for type 'int' is 'INTEGER', found 'DOUBLE'"},
                {"primitive_structure_error", "invalid TOML structure found for variable 'intVar', " +
                        "expected structure is 'KEY_VALUE', found 'TABLE'"},
                {"array_type_error", "invalid type found for variable 'intArr', expected TOML type " +
                        "for type 'int[] & readonly' is 'ARRAY', found 'STRING'"},
                {"array_structure_error", "invalid TOML structure found for variable 'intArr', " +
                        "expected structure is 'KEY_VALUE', found 'TABLE'"},
                {"array_element_structure", "invalid TOML structure found for variable 'intArr[2]', " +
                        "expected structure is 'INTEGER', found 'ARRAY'"},
                {"array_multi_type", "invalid TOML structure found for variable 'intArr[1]', " +
                        "expected structure is 'INTEGER', found 'STRING'"},
                {"additional_field", "Additional field 'scopes' provided for configurable variable 'testUser' of " +
                        "record 'main:AuthInfo' is not supported"},
                {"missing_record_field", "Value not provided for non-defaultable required field 'username' of record" +
                        " 'main:AuthInfo' in configurable variable 'testUser'"},
                {"record_type_error", "invalid type found for variable 'testUser', expected TOML type for type " +
                        "'main:(testOrg/main:0.1.0:AuthInfo & readonly)' is 'TABLE', found 'KEY_VALUE'"},
                {"record_field_structure_error", "invalid TOML table structure found for record variable 'testUser'," +
                        " found TOML table for 'testUser.username'"},
                {"record_field_type_error", "invalid type found for variable 'testUser.username', expected TOML type" +
                        " for type 'string' is 'STRING', found 'INTEGER'"},
                {"missing_table_key", "Value required for key 'username' of type 'table<(main:AuthInfo & readonly)>" +
                        " key(username) & readonly' in configurable variable 'users'"},
                {"table_type_error", "invalid type found for variable 'users', expected TOML type for type " +
                        "'table<(main:AuthInfo & readonly)> key(username) & readonly' is 'TABLE_ARRAY', found 'TABLE'"},
        };
    }

    // Encrypted Config related tests
    @Test
    public void testSingleBalFileWithEncryptedConfigs() throws BallerinaTestException {
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "correctSecret.txt").toString();
        executeBalCommand("/encryptedSingleBalFile", testsPassed, "run", "encryptedConfig.bal",
                addSecretEnvVariable(secretFilePath));
    }

    @Test
    public void testEncryptedConfigs() throws BallerinaTestException {
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "correctSecret.txt").toString();
        executeBalCommand("/encryptedConfigProject", testsPassed, "run", "main",
                addSecretEnvVariable(secretFilePath));
    }

    @Test
    public void testEncryptedConfigsWithIncorrectSecret() throws BallerinaTestException {
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "incorrectSecret.txt").toString();
        LogLeecher runLeecher = new LogLeecher("error: failed to retrieve the encrypted value for variable: " +
                "'password' : Given final block not properly padded. Such " +
                "issues can arise if a bad key is used during decryption.", ERROR);
        executeBalCommand("/encryptedConfigProject", runLeecher, "run", "main",
                addSecretEnvVariable(secretFilePath));
    }

    @Test
    public void testEncryptedConfigsWithEmptySecret() throws BallerinaTestException {
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "emptySecret.txt").toString();
        LogLeecher runLeecher =
                new LogLeecher("error: failed to initialize the cipher tool due to empty secret text", ERROR);
        executeBalCommand("/encryptedConfigProject", runLeecher, "run", "main",
                addSecretEnvVariable(secretFilePath));
    }

    @Test
    public void testInvalidAccessEncryptedConfigs() throws BallerinaTestException {
        String configFilePath = Paths.get(testFileLocation, "ConfigFiles", "InvalidEncryptedConfig.toml").toString();
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "correctSecret.txt").toString();
        LogLeecher runLeecher = new LogLeecher("error: failed to retrieve the encrypted value for variable: " +
                "'password' : Input byte array has wrong 4-byte ending unit", ERROR);
        executeBalCommand("/encryptedConfigProject", runLeecher, "run", "main",
                addEnvVariables(configFilePath, secretFilePath));
    }

    private void executeBalCommand(String projectPath, LogLeecher log, String command, String packageName,
                                   Map<String, String> envProperties) throws BallerinaTestException {
        bMainInstance.runMain(command, new String[]{packageName}, envProperties, new String[]{},
                new LogLeecher[]{log}, testFileLocation + projectPath);
        log.waitForText(5000);
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

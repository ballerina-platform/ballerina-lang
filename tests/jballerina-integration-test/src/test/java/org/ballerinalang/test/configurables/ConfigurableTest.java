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

import io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants;
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
    public void testAccessForOnlySubModules() throws BallerinaTestException {
        executeBalCommand("/subModuleProject", testsPassed, "run", "configPkg", null);
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
    public void testAPICNegativeTest() throws BallerinaTestException {
        String errorMsg = "[Config.toml:(3:14,3:28)] configurable variable 'configPkg:invalidArr' with type " +
                "'(int[] & readonly)[] & readonly' is not supported";
        executeBalCommand("/testErrorProject", new LogLeecher(errorMsg, ERROR), "test",
                "configPkg", null);
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
        LogLeecher errorLeecher = new LogLeecher("configuration file is not found in path ", ERROR);
        bMainInstance.runMain("run", new String[]{filePath.toString()}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, testFileLocation + "/negative_tests");
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidTomlFile() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "invalidTomlFile").toAbsolutePath();
        String tomlError1 = "[Config.toml:(0:9,0:9)] missing identifier";
        String tomlError2 = "[Config.toml:(0:20,0:20)] missing identifier";
        String tomlError3 = "[Config.toml:(0:21,0:21)] missing identifier";
        String errorMsg = "error: invalid `Config.toml` file : ";
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
                {"invalidComplexArray", "[Config.toml:(2:17,2:41)] configurable variable 'main:intComplexArr' with " +
                        "type '(int[] & readonly)[] & readonly' is not supported"},
                {"invalidRecordField", "[Config.toml:(4:1,4:40)] field type '(string[][] & readonly)' in configurable" +
                        " variable 'main:testUser' is not supported"},
                {"invalidByteRange", "value provided for byte variable 'main:byteVar' is out of range. " +
                        "Expected range is (0-255), found '355'"},
                {"invalidMapType",
                        "configurable variable 'main:intMap' with type 'map<int> & readonly' is not supported"},
                {"invalidTableConstraint", "[Config.toml:(1:1,2:16)] table constraint type 'map<string>' in " +
                        "configurable variable 'main:tab' is not supported"}
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
                {"empty_config_file", "an empty configuration file is found in path "},
                {"no_module_config", "[no_module_config.toml:(1:1,2:12)] invalid module structure found for module " +
                        "'main'. Please provide the module name as '[main]'"},
                {"invalid_org_name", "[invalid_org_name.toml:(1:1,3:21)] invalid module structure found for module " +
                        "'main'. Please provide the module name as '[main]'"},
                {"invalid_org_structure", "[invalid_org_structure.toml:(1:1,1:13)] invalid module structure found for" +
                        " module 'testOrg.main'. Please provide the module name as '[testOrg.main]'"},
                {"invalid_module_structure", "[invalid_module_structure.toml:(1:1,1:17)] invalid module structure " +
                        "found for module 'main'. Please provide the module name as '[main]'"},
                {"invalid_sub_module_structure", "[invalid_sub_module_structure.toml:(3:1,3:9)] invalid module " +
                        "structure found for module 'main.foo'. Please provide the module name as '[main.foo]'"},
                {"required_negative", "value not provided for required configurable variable 'main:stringVar'"},
                {"primitive_type_error", "[primitive_type_error.toml:(2:10,2:14)] configurable variable 'main:intVar'" +
                        " is expected to be of type 'int', but found 'float'"},
                {"primitive_structure_error", "[primitive_structure_error.toml:(2:1,2:24)] configurable variable " +
                        "'main:intVar' is expected to be of type 'int', but found 'record'"},
                {"array_type_error", "[array_type_error.toml:(4:10,4:17)] configurable variable 'main:intArr' is " +
                        "expected to be of type 'int[] & readonly', but found 'string'"},
                {"array_structure_error", "[array_structure_error.toml:(4:1,4:32)] configurable variable " +
                        "'main:intArr' is expected to be of type 'int[] & readonly', but found 'record'"},
                {"array_element_structure", "[array_element_structure.toml:(4:19,4:26)] configurable variable " +
                        "'main:intArr[2]' is expected to be of type 'int', but found 'array'"},
                {"array_multi_type", "[array_multi_type.toml:(4:15,4:21)] configurable variable 'main:intArr[1]' is " +
                        "expected to be of type 'int', but found 'string'"},
                {"additional_field", "[additional_field.toml:(7:1,7:19)] additional field 'scopes' provided for " +
                        "configurable variable 'main:testUser' of record " +
                        "'main:(testOrg/main:0.1.0:AuthInfo & readonly)' is not supported"},
                {"missing_record_field", "[missing_record_field.toml:(4:1,5:22)] value not provided for " +
                        "non-defaultable required field 'username' of record " +
                        "'main:(testOrg/main:0.1.0:AuthInfo & readonly)' in configurable variable 'main:testUser'"},
                {"record_type_error", "[record_type_error.toml:(3:1,3:39)] configurable variable 'main:testUser' is" +
                        " expected to be of type " +
                        "'main:(testOrg/main:0.1.0:AuthInfo & readonly)', but found 'string'"},
                {"record_field_structure_error", "[record_field_structure_error.toml:(5:1,5:28)] field 'username' " +
                        "from configurable variable 'main:testUser' is expected to be of type 'string', " +
                        "but found 'record'"},
                {"record_field_type_error", "[record_field_type_error.toml:(5:12,5:16)] field 'username' from " +
                        "configurable variable 'main:testUser' is expected to be of type 'string', but found 'int'"},
                {"missing_table_key", "[missing_table_key.toml:(8:1,9:21)] value required for key 'username' of type " +
                        "'table<main:AuthInfo> key(username)' in configurable variable 'main:users'"},
                {"table_type_error", "[table_type_error.toml:(4:1,6:21)] configurable variable 'main:users' is " +
                        "expected to be of type 'table<main:AuthInfo> key(username)', but found 'record'"},
                {"table_field_type_error", "[table_field_type_error.toml:(5:12,5:16)] field 'username' from " +
                        "configurable variable 'main:users' is expected to be of type 'string', but found 'int'"},
                {"table_field_structure_error", "[table_field_structure_error.toml:(5:1,5:29)] field 'username' from " +
                        "configurable variable 'main:users' is expected to be of type 'string', but found 'record'"},
                {"warning_defaultable_field", "[warning_defaultable_field.toml:(11:1,13:17)] defaultable readonly " +
                        "record field 'name' in configurable variable 'main:employee' is not supported"}
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
                "'main:password' : Given final block not properly padded. Such " +
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
                "'main:password' : Input byte array has wrong 4-byte ending unit", ERROR);
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
        envVariables.put(ConfigTomlConstants.CONFIG_ENV_VARIABLE, configFilePath);
        return envVariables;
    }

    private Map<String, String> addSecretEnvVariable(String secretFilePath) {
        Map<String, String> envVariables = PackerinaTestUtils.getEnvVariables();
        envVariables.put(ConfigTomlConstants.CONFIG_SECRET_ENV_VARIABLE, secretFilePath);
        return envVariables;
    }

    private Map<String, String> addEnvVariables(String configFilePath, String secretFilePath) {
        Map<String, String> envVariables = PackerinaTestUtils.getEnvVariables();
        envVariables.put(ConfigTomlConstants.CONFIG_ENV_VARIABLE, configFilePath);
        envVariables.put(ConfigTomlConstants.CONFIG_SECRET_ENV_VARIABLE, secretFilePath);
        return envVariables;
    }
}

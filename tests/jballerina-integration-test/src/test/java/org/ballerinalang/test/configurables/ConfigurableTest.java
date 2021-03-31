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
import org.ballerinalang.test.packaging.PackerinaTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_DATA_ENV_VARIABLE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_FILES_ENV_VARIABLE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.SECRET_DATA_ENV_VARIABLE;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.SECRET_FILE_ENV_VARIABLE;
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
    public void testConfigurableBalRun() throws BallerinaTestException {
        // bal run package with configurables
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                              new LogLeecher[]{testsPassed}, testFileLocation + "/configurableProject");
        testsPassed.waitForText(5000);

        // bal run single bal file with configurables
        bMainInstance.runMain("run", new String[]{testFileLocation + "/configTest.bal"}, null, new String[]{},
                              new LogLeecher[]{testsPassed}, testFileLocation);
        testsPassed.waitForText(5000);
    }

    @Test
    public void testConfigurableVariablesWithCliArgs() throws BallerinaTestException {
        bMainInstance.runMain(testFileLocation + "/configurableCliProject", "main", null,
                              new String[]{"--", "-CintVar=42",
                                      "-CbyteVar=22", "-CstringVar=waru=na", "-CbooleanVar=true",
                                      "-CxmlVar=<book>The Lost World</book>", "-CtestOrg.main.floatVar=3.5",
                                      "-Cmain.decimalVar=24.87"},
                              null, null, new LogLeecher[]{testsPassed});
        testsPassed.waitForText(5000);
    }

    @Test
    public void testConfigurableWithJavaJarRun() throws BallerinaTestException {
        executeBalCommand("/configurableProject", testsPassed, "main", null);
    }

    @Test
    public void testAccessForImportedModules() throws BallerinaTestException {
        executeBalCommand("/multiModuleProject", testsPassed, "configPkg", null);
    }

    @Test
    public void testAccessForOnlySubModules() throws BallerinaTestException {
        executeBalCommand("/subModuleProject", testsPassed, "configPkg", null);
    }

    @Test
    public void testBallerinaTestAPIWithConfigurableVariables() throws BallerinaTestException {
        LogLeecher testLog = new LogLeecher("4 passing");
        bMainInstance.runMain("test", new String[]{"configPkg"}, null, new String[]{},
                              new LogLeecher[]{testLog}, testFileLocation + "/testProject");
        testLog.waitForText(5000);
    }

    @Test
    public void testAPIConfigFileNegative() throws BallerinaTestException {
        String error = "error: value not provided for required configurable variable 'testInt'";
        LogLeecher errorLog = new LogLeecher(error, ERROR);
        bMainInstance.runMain("test", new String[]{"configPkg"}, null, new String[]{},
                              new LogLeecher[]{errorLog}, testFileLocation + "/testPathProject");
        errorLog.waitForText(5000);
    }

    @Test
    public void testAPICNegativeTest() throws BallerinaTestException {
        String errorMsg = "[Config.toml:(3:14,3:28)] configurable variable 'configPkg:invalidArr' with type " +
                "'(int[] & readonly)[] & readonly' is not supported";
        LogLeecher errorLog = new LogLeecher(errorMsg, ERROR);
        bMainInstance.runMain("test", new String[]{"configPkg"}, null, new String[]{},
                              new LogLeecher[]{errorLog}, testFileLocation + "/testErrorProject");
        errorLog.waitForText(5000);
    }

    @Test
    public void testEnvironmentVariableBasedConfigurable() throws BallerinaTestException {

        // test config file location through `BAL_CONFIG_FILES` env variable
        String configFilePaths = Paths.get(testFileLocation, "config_files", "Config-A.toml").toString() +
                File.pathSeparator + Paths.get(testFileLocation, "config_files", "Config-B.toml").toString();
        executeBalCommand("", testsPassed, "envVarPkg",
                          addEnvironmentVariables(Map.ofEntries(Map.entry(CONFIG_FILES_ENV_VARIABLE,
                                                                          configFilePaths))));

        // test configuration through `BAL_CONFIG_DATA` env variable
        String configData = "[envVarPkg] intVar = 42 floatVar = 3.5 stringVar = \"abc\" booleanVar = true " +
                "decimalVar = 24.87 intArr = [1,2,3] floatArr = [9.0, 5.6] " +
                "stringArr = [\"red\", \"yellow\", \"green\"] booleanArr = [true, false,false, true] " +
                "decimalArr = [8.9, 4.5, 6.2]";
        executeBalCommand("", testsPassed, "envVarPkg", addEnvironmentVariables(Map.
                ofEntries(Map.entry(CONFIG_DATA_ENV_VARIABLE, configData))));

        // test configuration through `BAL_CONFIG_SECRET_FILE` env variable
        String secretFilePath = Paths.get(testFileLocation, "config_files", "Config-secrets.toml").toString();
        executeBalCommand("", testsPassed, "envVarPkg",
                          addEnvironmentVariables(Map.ofEntries(Map.entry(SECRET_FILE_ENV_VARIABLE, secretFilePath))));

        // test configuration through `BAL_CONFIG_SECRET_DATA` env variable
        executeBalCommand("", testsPassed, "envVarPkg",
                          addEnvironmentVariables(Map.ofEntries(Map.entry(SECRET_DATA_ENV_VARIABLE, configData))));
    }

    @Test
    public void testSecretFileOverriding() throws BallerinaTestException {
        // Check multiple cases of TOML values getting overridden
        String secretFilePath =  Paths.get(testFileLocation, "config_files", "Config-secrets.toml").toString();
        String configFilePath =  Paths.get(testFileLocation, "config_files", "Config-override.toml").toString();

        // test secret file overriding config file
        Map<String, String> envVarMap = Map.ofEntries(
                Map.entry(SECRET_FILE_ENV_VARIABLE, secretFilePath),
                Map.entry(CONFIG_FILES_ENV_VARIABLE, configFilePath));
        executeBalCommand("", testsPassed, "envVarPkg", addEnvironmentVariables(envVarMap));

        // test secret file overriding config content
        String configData = "[envVarPkg] " +
                "booleanVar = false " +
                "decimalVar = 12.34 " +
                "floatVar = 23.1 " +
                "intVar = 22 " +
                "stringVar = \"this should get overridden\" " +
                "booleanArr = [true, false, false, true] " +
                "decimalArr = [9.1, 8.2, 7.3] " +
                "floatArr = [1.9, 2.8, 3.7] " +
                "intArr = [1, 9, 2, 8] " +
                "stringArr = [\"this\", \"should\", \"get\", \"overridden\"]";
        envVarMap = Map.ofEntries(
                Map.entry(SECRET_FILE_ENV_VARIABLE, secretFilePath),
                Map.entry(CONFIG_DATA_ENV_VARIABLE, configData));
        executeBalCommand("", testsPassed, "envVarPkg", addEnvironmentVariables(envVarMap));

        // test secret content overriding config file
        String secretData = "[envVarPkg] " +
                "intVar = 42 " +
                "floatVar = 3.5 " +
                "stringVar = \"abc\" " +
                "booleanVar = true " +
                "decimalVar = 24.87 " +
                "intArr = [1,2,3] " +
                "floatArr = [9.0, 5.6] " +
                "stringArr = [\"red\", \"yellow\", \"green\"] " +
                "booleanArr = [true, false, false, true] " +
                "decimalArr = [8.9, 4.5, 6.2] ";
        envVarMap = Map.ofEntries(
                Map.entry(SECRET_DATA_ENV_VARIABLE, secretData),
                Map.entry(CONFIG_FILES_ENV_VARIABLE, configFilePath));
        executeBalCommand("", testsPassed, "envVarPkg", addEnvironmentVariables(envVarMap));

        // test secret content overriding config content
        envVarMap = Map.ofEntries(
                Map.entry(SECRET_DATA_ENV_VARIABLE, secretData),
                Map.entry(CONFIG_DATA_ENV_VARIABLE, configData));
        executeBalCommand("", testsPassed, "envVarPkg", addEnvironmentVariables(envVarMap));

        // test secret file overriding config file when using default path
        executeBalCommand("", testsPassed, "envVarPkg", null);
    }

    @Test
    public void testSingleBalFileWithConfigurables() throws BallerinaTestException {
        String filePath = testFileLocation + "/configTest.bal";
        executeBalCommand("", testsPassed, filePath, null);
    }

    @Test
    public void testRecordValueWithModuleClash() throws BallerinaTestException {
        executeBalCommand("/recordModuleProject", testsPassed, "main", null);
    }

    /** Negative test cases. */

    @Test
    public void testConfigurableVariablesWithInvalidCliArgs() throws BallerinaTestException {
        LogLeecher errorLeecher1 = new LogLeecher("error: [intVar=waruna] configurable variable 'intVar' is expected " +
                                                          "to be of type 'int', but found 'waruna'", ERROR);
        LogLeecher errorLeecher2 = new LogLeecher("error: [byteVar=2200] value provided for byte variable 'byteVar' " +
                                                          "is out of range. Expected range is (0-255), found '2200'",
                                                  ERROR);
        LogLeecher errorLeecher3 = new LogLeecher("error: [testOrg.main.floatVar=eee] configurable variable " +
                                                          "'floatVar' is expected to be of type 'float', but found " +
                                                          "'eee'", ERROR);
        LogLeecher errorLeecher4 = new LogLeecher("error: value not provided for required configurable variable " +
                                                          "'stringVar'", ERROR);
        LogLeecher errorLeecher5 = new LogLeecher("error: [xmlVar=123<?????] configurable variable 'xmlVar' is " +
                                                          "expected to be of type 'xml<((lang.xml:Element|lang" +
                                                          ".xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)" +
                                                          " & readonly)>', but found '123<?????'", ERROR);
        bMainInstance.runMain("run", new String[]{"main", "--", "-CintVar=waruna", "-CbyteVar=2200", "-CbooleanVar" +
                "=true", "-CxmlVar=123<?????", "-CtestOrg.main.floatVar=eee",
                "-Cmain.decimalVar=24.87"}, null, new String[]{}, new LogLeecher[]{errorLeecher1,
                errorLeecher2, errorLeecher3, errorLeecher4, errorLeecher5}, testFileLocation +
                                      "/configurableCliProject");
        errorLeecher1.waitForText(5000);
        errorLeecher2.waitForText(5000);
        errorLeecher3.waitForText(5000);
        errorLeecher4.waitForText(5000);
        errorLeecher5.waitForText(5000);
    }

    @Test
    public void testNoConfigFile() throws BallerinaTestException {
        Path filePath = Paths.get(negativeTestFileLocation, "no_config.bal").toAbsolutePath();
        LogLeecher errorLeecher =
                new LogLeecher("error: value not provided for required configurable variable 'name'", ERROR);
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
        String errorMsg = "warning: invalid `Config.toml` file : ";
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
                {"invalidRecordField", " [Config.toml:(4:1,4:40)] field type 'string[][]' in configurable variable " +
                        "'main:testUsers' is not supported"},
                {"invalidByteRange", "value provided for byte variable 'main:byteVar' is out of range. " +
                        "Expected range is (0-255), found '355'"},
                {"invalidMapType", "[main.bal:(17:14,17:33)] configurable variable currently not supported for '" +
                        "(map<int> & readonly)'"},
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
        Path tomlPath = Paths.get(negativeTestFileLocation, "config_files", tomlFileName + ".toml").toAbsolutePath();
        LogLeecher errorLog = new LogLeecher(errorMsg, ERROR);
        bMainInstance.runMain("run", new String[]{"main"},
                              addEnvironmentVariables(Map.ofEntries(Map.entry(CONFIG_FILES_ENV_VARIABLE,
                                                                              tomlPath.toString()))),
                              new String[]{}, new LogLeecher[]{errorLog}, projectPath.toString());
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
                {"required_negative", "value not provided for required configurable variable 'stringVar'"},
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
                        "configurable variable 'main:users' of record 'main:AuthInfo' is not supported"},
                {"missing_record_field", "[missing_record_field.toml:(4:1,5:22)] value required for key 'username'" +
                        " of type 'table<main:AuthInfo> key(username)' in configurable variable 'main:users'"},
                {"missing_table_key", "[missing_table_key.toml:(8:1,9:21)] value required for key 'username' of type " +
                        "'table<main:AuthInfo> key(username)' in configurable variable 'main:users'"},
                {"table_type_error", "[table_type_error.toml:(4:1,6:21)] configurable variable 'main:users' is " +
                        "expected to be of type 'table<main:AuthInfo> key(username)', but found 'record'"},
                {"table_field_type_error", "[table_field_type_error.toml:(5:12,5:16)] field 'username' from " +
                        "configurable variable 'main:users' is expected to be of type 'string', but found 'int'"},
                {"table_field_structure_error", "[table_field_structure_error.toml:(5:1,5:29)] field 'username' from " +
                        "configurable variable 'main:users' is expected to be of type 'string', but found 'record'"},
                {"warning_defaultable_field", "[warning_defaultable_field.toml:(8:1,10:17)] defaultable readonly " +
                        "record field 'name' in configurable variable 'main:employees' is not supported"}
        };
    }

    private void executeBalCommand(String projectPath, LogLeecher log, String packageName,
                                   Map<String, String> envProperties) throws BallerinaTestException {
        bMainInstance.runMain(testFileLocation + projectPath, packageName, null, new String[]{}, envProperties, null,
                              new LogLeecher[]{log});
        log.waitForText(5000);
    }

    /**
     * Get environment variables and add config file path, data as an env variable.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvironmentVariables(Map<String, String> pathVariables) {
        Map<String, String> envVariables = PackerinaTestUtils.getEnvVariables();
        for (Map.Entry<String, String> pathVariable :pathVariables.entrySet()) {
            envVariables.put(pathVariable.getKey(), pathVariable.getValue());
        }
        return envVariables;
    }
}

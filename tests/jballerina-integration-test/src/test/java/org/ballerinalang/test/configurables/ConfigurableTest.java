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
import org.testng.annotations.Test;

import java.io.File;
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
    public void testInvalidDefaultableField() throws BallerinaTestException {
        String errorMsg = "[Config.toml:(1:1,3:17)] defaultable readonly record field 'name' in configurable variable" +
                " 'invalidDefaultable:employee' is not supported";
        LogLeecher errorLog = new LogLeecher(errorMsg, ERROR);
        bMainInstance.runMain("run", new String[]{"invalidDefaultable"}, null, new String[]{},
                new LogLeecher[]{errorLog}, negativeTestFileLocation);
        errorLog.waitForText(5000);
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

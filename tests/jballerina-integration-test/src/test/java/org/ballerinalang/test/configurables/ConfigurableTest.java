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
    private BMainInstance bMainInstance;
    private final String testsPassed = "Tests passed";

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);

        // Build and push config Lib project.
        LogLeecher buildLeecher = new LogLeecher("target/bala/testOrg-configLib-any-0.1.0.bala");
        LogLeecher pushLeecher = new LogLeecher("Successfully pushed target/bala/testOrg-configLib-any-0.1.0.bala to " +
                                                        "'local' repository.", ERROR);
        bMainInstance.runMain("build", new String[]{"-c"}, null, null, new LogLeecher[]{buildLeecher},
                              testFileLocation + "/configLibProject");
        buildLeecher.waitForText(5000);
        bMainInstance.runMain("push", new String[]{"--repository=local"}, null, null, new LogLeecher[]{pushLeecher},
                              testFileLocation + "/configLibProject");
        pushLeecher.waitForText(5000);
    }

    @Test
    public void testConfigurableBalRun() throws BallerinaTestException {
        // bal run package with configurables
        LogLeecher logLeecher1 = new LogLeecher(testsPassed);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                              new LogLeecher[]{logLeecher1}, testFileLocation + "/configurableProject");
        logLeecher1.waitForText(5000);

        // bal run single bal file with configurables
        LogLeecher logLeecher2 = new LogLeecher(testsPassed);
        bMainInstance.runMain("run", new String[]{testFileLocation + "/SingleBalFile/configTest.bal"}, null,
                new String[]{}, new LogLeecher[]{logLeecher2}, testFileLocation + "/SingleBalFile");
        logLeecher2.waitForText(5000);
    }

    @Test
    public void testConfigurableVariablesWithCliArgs() throws BallerinaTestException {
        LogLeecher logLeecher = new LogLeecher(testsPassed);
        bMainInstance.runMain(testFileLocation + "/configurableCliProject", "main", null,
                              new String[]{"-CintVar=42", "-CbyteVar=22", "-CstringVar=waru=na", "-CbooleanVar=true",
                                      "-CxmlVar=<book>The Lost World</book>", "-CtestOrg.main.floatVar=3.5",
                                      "-Cmain.decimalVar=24.87", "-Cmain.color=RED", "-Cmain.countryCode=Sri Lanka"},
                              null, null, new LogLeecher[]{logLeecher});
        logLeecher.waitForText(5000);
    }

    @Test
    public void testConfigurableWithJavaJarRun() throws BallerinaTestException {
        executeBalCommand("/configurableProject", "main", null);
    }

    @Test
    public void testBallerinaTestAPIWithConfigurableVariables() throws BallerinaTestException {
        LogLeecher testLog1 = new LogLeecher("5 passing");
        LogLeecher testLog2 = new LogLeecher("4 passing");
        bMainInstance.runMain("test", new String[]{"configPkg"}, null, new String[]{},
                              new LogLeecher[]{testLog1, testLog2}, testFileLocation + "/testProject");
        testLog1.waitForText(5000);
        testLog1.waitForText(5000);

        // Testing `bal test` command error log for configurables
        String errorMsg = "error: [Config.toml:(3:1,3:22)] configurable variable 'invalidMap' is expected to be of " +
                "type 'map<string> & readonly', but found 'string'";
        String errorLocationMsg = "\tat testOrg/configPkg:0.1.0(tests/main_test.bal:19)";
        LogLeecher errorLog = new LogLeecher(errorMsg, ERROR);
        LogLeecher errorLocationLog = new LogLeecher(errorLocationMsg, ERROR);
        bMainInstance.runMain("test", new String[]{"configPkg"}, null, new String[]{},
                              new LogLeecher[]{errorLog, errorLocationLog}, testFileLocation + "/testErrorProject");
        errorLog.waitForText(5000);
    }

    @Test
    public void testEnvironmentVariableBasedConfigurable() throws BallerinaTestException {

        // test config file location through `BAL_CONFIG_FILES` env variable
        String configFilePaths = Paths.get(testFileLocation, "config_files", "Config-A.toml") +
                File.pathSeparator + Paths.get(testFileLocation, "config_files", "Config-B.toml");
        executeBalCommand("", "envVarPkg",
                          addEnvironmentVariables(Map.ofEntries(Map.entry(CONFIG_FILES_ENV_VARIABLE,
                                                                          configFilePaths))));

        // test configuration through `BAL_CONFIG_DATA` env variable
        String configData = "[envVarPkg] intVar = 42 floatVar = 3.5 stringVar = \"abc\" booleanVar = true " +
                "decimalVar = 24.87 intArr = [1,2,3] floatArr = [9.0, 5.6] " +
                "stringArr = [\"red\", \"yellow\", \"green\"] booleanArr = [true, false,false, true] " +
                "decimalArr = [8.9, 4.5, 6.2]";
        executeBalCommand("", "envVarPkg",
                addEnvironmentVariables(Map.ofEntries(Map.entry(CONFIG_DATA_ENV_VARIABLE, configData))));

        // test configuration through `BAL_CONFIG_SECRET_FILE` env variable
        String secretFilePath = Paths.get(testFileLocation, "config_files", "Config-secrets.toml").toString();
        executeBalCommand("", "envVarPkg",
                addEnvironmentVariables(Map.ofEntries(Map.entry(SECRET_FILE_ENV_VARIABLE, secretFilePath))));

        // test configuration through `BAL_CONFIG_SECRET_DATA` env variable
        executeBalCommand("", "envVarPkg",
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
        executeBalCommand("", "envVarPkg", addEnvironmentVariables(envVarMap));

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
        executeBalCommand("", "envVarPkg", addEnvironmentVariables(envVarMap));

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
        executeBalCommand("", "envVarPkg", addEnvironmentVariables(envVarMap));

        // test secret content overriding config content
        envVarMap = Map.ofEntries(
                Map.entry(SECRET_DATA_ENV_VARIABLE, secretData),
                Map.entry(CONFIG_DATA_ENV_VARIABLE, configData));
        executeBalCommand("", "envVarPkg", addEnvironmentVariables(envVarMap));

        // test secret file overriding config file when using default path
        executeBalCommand("", "envVarPkg", null);
    }

    @Test
    public void testRecordValueWithModuleClash() throws BallerinaTestException {
        executeBalCommand("/recordModuleProject", "main", null);
    }

    @Test()
    public void testConfigurableRecordsAndRecordTables() throws BallerinaTestException {
        executeBalCommand("/configStructuredTypesProject", "configStructuredTypes", null);
    }

    @Test()
    public void testConfigurableUnionTypes() throws BallerinaTestException {
        executeBalCommand("/configUnionTypesProject", "configUnionTypes", null);
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
                                                          " & readonly)>', but found '123<?????", ERROR);

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
        LogLeecher errorLog = new LogLeecher("error: [Config.toml:(1:1,3:17)] defaultable readonly record field " +
                                                     "'name' in configurable variable'employees' is not supported",
                                             ERROR);
        LogLeecher errorLocationLog = new LogLeecher("\tat testOrg/main:0.1.0(main.bal:25)", ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLog, errorLocationLog}, testFileLocation + "/invalidDefaultable");
        errorLog.waitForText(5000);
    }

    private void executeBalCommand(String projectPath, String packageName,
                                   Map<String, String> envProperties) throws BallerinaTestException {
        LogLeecher logLeecher = new LogLeecher(testsPassed);
        bMainInstance.runMain(testFileLocation + projectPath, packageName, null, new String[]{}, envProperties, null,
                new LogLeecher[]{logLeecher});
        logLeecher.waitForText(5000);
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

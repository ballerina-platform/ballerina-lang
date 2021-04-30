/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.runtime;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Map;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_MAX_POOL_SIZE_ENV_VAR;
import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_FILES_ENV_VARIABLE;
import static org.ballerinalang.test.context.LogLeecher.LeecherType.ERROR;
import static org.ballerinalang.test.util.TestUtils.addEnvironmentVariables;

/**
 * Test cases for Scheduler thread pool size configuration.
 */
public class SchedulerConfigTest extends BaseTest {
    private static final String testFileLocation = Paths.get("src", "test", "resources", "runtime")
            .toAbsolutePath().toString();
    private BMainInstance bMainInstance;
    private final String testsPassed = "Tests passed";

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }
    @Test
    public void testSchedulerThreadPoolSize() throws BallerinaTestException {
        String configFilePath = Paths.get(testFileLocation, "schedulerTest", "Config.toml").toString();
        LogLeecher logLeecher = new LogLeecher(testsPassed);
        bMainInstance.runMain(testFileLocation, "schedulerTest", null, new String[]{},
                addEnvironmentVariables(Map.of(CONFIG_FILES_ENV_VARIABLE, configFilePath)), null,
                new LogLeecher[]{logLeecher});
        logLeecher.waitForText(5000);
    }

    @Test
    public void testSchedulerThreadPoolSizeWarning() throws BallerinaTestException {
        LogLeecher errorLog = new LogLeecher("warning: 'BALLERINA_MAX_POOL_SIZE' environment variable is deprecated. " +
                "Please provide the configuration through configurable variable 'lang.runtime.poolSize'", ERROR);
        LogLeecher log = new LogLeecher(testsPassed);
        bMainInstance.runMain("run", new String[]{testFileLocation + "/sampleBalSource/test.bal"},
                addEnvironmentVariables(Map.of(BALLERINA_MAX_POOL_SIZE_ENV_VAR, "16")),
                new String[]{}, new LogLeecher[]{errorLog, log}, testFileLocation + "/sampleBalSource");
        errorLog.waitForText(5000);
        log.waitForText(5000);
    }

}

/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.semver;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectConstants.HOME_REPO_ENV_KEY;

/**
 * Base implementation of the semver validator integration test suite.
 *
 * @since 2201.2.2
 */
public class SemverValidatorBaseTest extends BaseTest {

    protected Path tempProjectsDir;
    protected Path customRepoDir;
    protected BMainInstance balClient;

    protected void pushPackageToCustomRepo(String packageName) throws BallerinaTestException {
        Map<String, String> customRepoEnv = new HashMap<>();
        customRepoEnv.put(HOME_REPO_ENV_KEY, customRepoDir.toAbsolutePath().toString());

        balClient.runMain("pack", new String[]{}, customRepoEnv, new String[]{}, new LogLeecher[0],
                tempProjectsDir.resolve(packageName).toAbsolutePath().toString());

        balClient.runMain("push", new String[]{"--repository=local"}, customRepoEnv, new String[]{},
                new LogLeecher[0], tempProjectsDir.resolve(packageName).toAbsolutePath().toString());
    }

    protected void executeSemverCommand(String packageName, String[] commandArgs, List<String> expectedLogs)
            throws BallerinaTestException {
        executeSemverCommand(packageName, commandArgs, expectedLogs, new LinkedList<>());
    }

    protected void executeSemverCommand(String packageName, String[] commandArgs, List<String> expectedLogs,
                                        List<String> expectedErrors) throws BallerinaTestException {

        Map<String, String> customRepoEnv = new HashMap<>();
        customRepoEnv.put(HOME_REPO_ENV_KEY, customRepoDir.toAbsolutePath().toString());

        Stream<LogLeecher> logLeechers = expectedLogs.stream()
                .map(LogLeecher::new);
        Stream<LogLeecher> errorLeechers = expectedErrors.stream()
                .map(text -> new LogLeecher(text, LogLeecher.LeecherType.ERROR));
        LogLeecher[] leechers = Stream.concat(logLeechers, errorLeechers).toArray(LogLeecher[]::new);

        balClient.runMain("semver", commandArgs, customRepoEnv, new String[]{}, leechers,
                tempProjectsDir.resolve(packageName).toAbsolutePath().toString());

        for (LogLeecher leecher : leechers) {
            leecher.waitForText(10000);
        }
    }
}

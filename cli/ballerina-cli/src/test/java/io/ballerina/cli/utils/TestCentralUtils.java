/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.cli.utils;

import io.ballerina.projects.Settings;
import io.ballerina.projects.util.ProjectUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.cli.utils.CentralUtils.readSettings;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;

/**
 * Test cases to test utilities.
 */
@PrepareForTest({ ProjectUtils.class, System.class })
@PowerMockIgnore("jdk.internal.reflect.*")
public class TestCentralUtils extends PowerMockTestCase {

    private static final Path VALID_BAL_HOME = Paths.get("src/test/resources/test-resources/central-utils/valid");
    private static final Path INVALID_BAL_HOME = Paths.get("src/test/resources/test-resources/central-utils/invalid");
    private static final String EXPECTED_ACCESS_TOKEN = "273cc9f6-c333-36ab-aa2q-f08e9513ff5y";

    @Test(description = "Test get access token from Settings.toml")
    public void testGetAccessTokenFromSettings() {
        PowerMockito.mockStatic(ProjectUtils.class);
        PowerMockito.when(ProjectUtils.createAndGetHomeReposPath()).thenReturn(VALID_BAL_HOME);
        Settings settings = readSettings();
        Assert.assertEquals(settings.getCentral().getAccessToken(), EXPECTED_ACCESS_TOKEN);
        Assert.assertEquals(getAccessTokenOfCLI(settings), EXPECTED_ACCESS_TOKEN);
    }

    @Test(description = "Test get access token from invalid, but recoverable Settings.toml")
    public void testGetAccessTokenFromInvalidSettings() {
        PowerMockito.mockStatic(ProjectUtils.class);
        PowerMockito.when(ProjectUtils.createAndGetHomeReposPath()).thenReturn(INVALID_BAL_HOME);
        Settings settings = readSettings();
        Assert.assertEquals(getAccessTokenOfCLI(settings), EXPECTED_ACCESS_TOKEN);
    }
}

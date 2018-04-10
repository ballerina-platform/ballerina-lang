/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.services.dispatching;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.Test;

/**
 * Service versioning dispatching related negative test cases.
 */
public class VersioningNegativeTest {

    private static final String MOCK_ENDPOINT_NAME = "passthruEP";
    private static final String PKG_NAME = "pqr.stv";

    @Test(description = "Test dispatching with invalid version segments",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*invalid versioning pattern.*")
    public void testInvalidVersionSegmentsNegative() {
        CompileResult result = BServiceUtil
                .setupProgramFile(this, "test-src/services/dispatching/versioning/negativecase1", PKG_NAME);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/hello1/v2.4/bar/go", "GET");
        Services.invokeNew(result, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);
    }

    @Test(description = "Test dispatching with minor version",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*invalid versioning pattern.*")
    public void testWithMinorVersionTemplateNegative() {
        CompileResult result = BServiceUtil
                .setupProgramFile(this, "test-src/services/dispatching/versioning/negativecase2", PKG_NAME);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/hello6/v1.4/go", "GET");
        Services.invokeNew(result, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);
    }

    @Test(description = "Test dispatching with minor version",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*service registration failed: two services have the same basePath : " +
                  "/echo/v2/bar.*")
    public void testRegisteringTwoServicedsWithSameBasePath() {
        CompileResult result = BServiceUtil
                .setupProgramFile(this, "test-src/services/dispatching/versioning/negativecase3", PKG_NAME);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/echo/v2.4/bar", "GET");
        Services.invokeNew(result, PKG_NAME, MOCK_ENDPOINT_NAME, cMsg);
    }
}

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

package org.ballerinalang.stdlib.services.dispatching;

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service versioning dispatching related negative test cases.
 */
public class VersioningNegativeTest {

    private static final int MOCK_ENDPOINT_1_PORT = 9090;
    private static final int MOCK_ENDPOINT_2_PORT = 9091;
    private static final int MOCK_ENDPOINT_3_PORT = 9092;
    private static final String PKG_NAME = "pqr.stv";
    private Path sourceRoot = Paths.get("test-src", "services", "dispatching", "versioning");

    @Test(description = "Test dispatching with invalid version segments",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*Invalid versioning pattern.*")
    public void testInvalidVersionSegmentsNegative() {
        BCompileUtil.compile(sourceRoot.resolve("negativecase1").toString(), PKG_NAME);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/hello1/v2.4/bar/go", "GET");
        Services.invoke(MOCK_ENDPOINT_1_PORT, cMsg);
    }

    @Test(description = "Test dispatching with minor version",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*Invalid versioning pattern.*")
    public void testWithMinorVersionTemplateNegative() {
        BCompileUtil.compile(sourceRoot.resolve("negativecase2").toString(), PKG_NAME);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/hello6/v1.4/go", "GET");
        Services.invoke(MOCK_ENDPOINT_2_PORT, cMsg);
    }

    @Test(description = "Test dispatching with minor version",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*Service registration failed: two services have the same basePath : " +
                  "'/echo/v2/bar'.*")
    public void testRegisteringTwoServicedsWithSameBasePath() {
        BCompileUtil.compile(sourceRoot.resolve("negativecase3").toString(), PKG_NAME);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/echo/v2.4/bar", "GET");
        Services.invoke(MOCK_ENDPOINT_3_PORT, cMsg);
    }
}

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.net.http.resiliency;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for the Failover Connector.
 */
public class FailoverConnectorTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/net/http/resiliency/failover-connector-test.bal");
    }

    /**
     * Test case scenario:
     * - First endpoint returns HttpConnectorError for the request.
     * - Failover connector should retry the second endpoint.
     * - Second endpoints returns success response.
     */
    @Test(description = "Test case for failover connector for at least one endpoint send success response.")
    public void testSuccessScenario() {
        long expectedHttpSC = 200;
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testSuccessScenario");

        Assert.assertNotNull(returnVals);
        Assert.assertEquals(returnVals.length, 1);
        BStruct res = (BStruct) returnVals[0];

        if (res != null) {
            long statusCode = res.getIntField(0);
            Assert.assertEquals(statusCode, expectedHttpSC);
        }
    }

    /**
     * Test case scenario:
     * - All Endpoints return HttpConnectorError for the requests.
     * - Once all endpoints were tried out failover connector responds with
     * - status code of 500 and the error return from the last endpoint.
     */
    @Test(description = "Test case for failover connector when all endpoints return error response.")
    public void testFailureScenario() {
        long expectedHttpSC = 500;
        String expectedErrprMessageContent =
                "All the failover endpoints failed. Last endpoint returned response is: 502 ";
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testFailureScenario");

        Assert.assertNotNull(returnVals);
        Assert.assertEquals(returnVals.length, 1);
        BStruct res = (BStruct) returnVals[0];

        if (res != null) {
            long statusCode = res.getIntField(0);
            String errorMsg = res.getStringField(0);
            Assert.assertEquals(statusCode, expectedHttpSC);
            Assert.assertEquals(errorMsg, expectedErrprMessageContent);
        }
    }
}

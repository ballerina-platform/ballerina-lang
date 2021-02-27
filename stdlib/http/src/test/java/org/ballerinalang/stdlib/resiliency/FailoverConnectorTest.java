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

package org.ballerinalang.stdlib.resiliency;

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for the Failover Connector.
 */
public class FailoverConnectorTest {

    private CompileResult compileResult;
    private static final String STATUS_CODE_FIELD = "statusCode";

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/resiliency/failover-connector-test.bal");
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
        BMap<String, BValue> res = (BMap<String, BValue>) returnVals[0];

        if (res != null) {
            long statusCode = ((BInteger) res.get(STATUS_CODE_FIELD)).intValue();
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
        String expectedErrorMessageContent =
                "All the failover endpoints failed. Last endpoint returned response is: 500 ";
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testFailureScenario");

        Assert.assertNotNull(returnVals);
        Assert.assertEquals(returnVals.length, 1);
        BError error = (BError) returnVals[0];

        if (error != null) {
            Assert.assertEquals(error.getMessage(), expectedErrorMessageContent);
        }
    }
}

/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.filter;

import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for verifying multiple http filters for a service.
 */
@Test(groups = "filter-test")
public class MultpleFiltersTestCase extends FilterTestCommons {

    @Test(description = "Single filter execution success case")
    public void testMultipleFiltersSuccess() throws Exception {
        HttpResponse response = getHttpResponse(9092, "echo/test");
        Assert.assertEquals(response.getResponseCode(), 200, RESPONSE_CODE_MISMATCHED);
    }

    @Test(description = "Single filter execution failure case")
    public void testMultipleFiltersFailureFromLast() throws Exception {
        HttpResponse response = getHttpResponse(9094, "echo/test");
        Assert.assertEquals(response.getResponseCode(), 403, RESPONSE_CODE_MISMATCHED);
    }

    @Test(description = "Single filter execution failure case")
    public void testMultipleFiltersFailureFromMiddle() throws Exception {
        HttpResponse response = getHttpResponse(9091, "echo/test");
        Assert.assertEquals(response.getResponseCode(), 405, RESPONSE_CODE_MISMATCHED);
    }

    @Test(description = "Multiple filter attribute sharing test")
    public void testMultipleFiltersContextSharingTest() throws Exception {
        HttpResponse response = getHttpResponse(9090, "echo/test");
        Assert.assertEquals(response.getResponseCode(), 200, RESPONSE_CODE_MISMATCHED);
    }

    @Test(description = "Single response filter execution failure case")
    public void testMultipleResponseFiltersFailure() throws Exception {
        HttpResponse response = getHttpResponse(9093, "echo/test");
        Assert.assertEquals(response.getResponseCode(), 500, RESPONSE_CODE_MISMATCHED);
    }

    @Test(description = "Tests the ordering of the request and response filters")
    public void testFilterOrdering() throws Exception {
        HttpResponse response = getHttpResponse(9099, "filter/order");
        Assert.assertEquals(response.getResponseCode(), 200, RESPONSE_CODE_MISMATCHED);
        Assert.assertEquals(response.getData(), "RequestFilter1 RequestFilter2 CommonFilterReq CommonFilterResp " +
                "ResponseFilter2 ResponseFilter1 ", "Response mismatch");
    }
}

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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Utils;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base test class for Filter integration test cases which starts/stops the filter services as ballerina packages before
 * and after tests are run.
 */
public class FilterTestCommons extends BaseTest {
    protected static BServerInstance serverInstance;
    static final String RESPONSE_CODE_MISMATCHED = "Response code mismatched";

    @BeforeGroups(value = "filter-test", alwaysRun = true)
    public void start() throws BallerinaTestException {
        int[] requiredPorts = new int[]{9090, 9091, 9092, 9093, 9094, 9095, 9096, 9097, 9098, 9099};
        Utils.checkPortsAvailability(requiredPorts);

        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                           "filter").getAbsolutePath();

        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(basePath, "filterservices");
    }

    @AfterGroups(value = "filter-test", alwaysRun = true)
    public void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }

    HttpResponse getHttpResponse(int i, String s) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(i, s), headers);
        Assert.assertNotNull(response);
        return response;
    }
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.service;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.siddhi.service.api.NotFoundException;
import org.wso2.siddhi.service.impl.SiddhiApiServiceImpl;

import javax.ws.rs.core.Response;

public class SiddhiApiTestCase {

    @Test
    /**
     * Check deploy and undeploy functionality
     */
    public void testDeployAndUndeploy() throws NotFoundException {
        SiddhiApiServiceImpl apiService = new SiddhiApiServiceImpl();
        String siddhiApp = "@app:name('filterTest1') " +
                "" +
                "define stream cseEventStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select symbol, price " +
                "insert into outputStream;" +
                "" +
                "@info(name = 'query2') " +
                "from outputStream[70 > price] " +
                "select symbol, price " +
                "insert into outputStream2 ;";
        Response response = apiService.siddhiArtifactDeployPost(siddhiApp);
        Assert.assertEquals(response.getStatus(), 200, "HTTP 200 should be returned");
        Assert.assertTrue(response.getEntity().toString().contains("Siddhi app is deployed and runtime is created"),
                          "Siddhi App creation message should be returned");

        Response undeployResponse = apiService.siddhiArtifactUndeploySiddhiAppGet("filterTest1");
        Assert.assertEquals(undeployResponse.getStatus(), 200, "HTTP 200 should be returned");
        Assert.assertTrue(undeployResponse.getEntity().toString().contains("Siddhi app removed successfully"),
                          "Siddhi App removed message should be returned");
    }
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.connectors;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Test class for Connector service.
 */
public class ConnectorServiceTest {


    private CompileResult result;

    @BeforeClass()
    public void setup() {
        result = BServiceUtil.setupProgramFile(this, "test-src/connectors/connector-in-service.bal");
    }

    @Test(description = "Test action3Resource")
    public void testAction3Resource() {

        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action3", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);

        Assert.assertNotNull(response);
        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "MyParam1");
    }

    @Test(description = "Test action1Resource", priority = 1)
    public void testAction1Resource() {

        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);

        Assert.assertNotNull(response);
        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "false");
    }

    @Test(description = "Test action1Resource after calling action2Resource", priority = 2)
    public void testAction2Resource() {
        HTTPCarbonMessage action2Req = MessageUtils.generateHTTPMessage("/invoke/action2", "GET");
        Services.invokeNew(action2Req);

        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action1", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);

        Assert.assertNotNull(response);
        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "true");
    }

    @Test(description = "Test action5Resource")
    public void testAction5Resource() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action5", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);

        Assert.assertNotNull(response);
        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "MyParam1, MyParam1");
    }

    @Test(description = "Test action6Resource")
    public void testAction6Resource() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/invoke/action6", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);

        Assert.assertNotNull(response);
        StringDataSource stringDataSource = (StringDataSource) response.getMessageDataSource();
        Assert.assertNotNull(stringDataSource);
        Assert.assertEquals(stringDataSource.getValue(), "Hello, World");
    }

    @AfterClass
    public void tearDown() {
        BServiceUtil.cleanup(result);
    }

}

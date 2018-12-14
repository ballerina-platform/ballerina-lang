/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.streaming;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.util.ArrayList;

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;

/**
 * This contains methods to test alert generation in services Ballerina Streaming V2.
 *
 * @since 0.990.0
 */
public class BallerinaStreamsV2AlertsWithServicesTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BServiceUtil.setupProgramFile(this, "test-src/streaming/streamingv2-alerts-with-services.bal");
    }

    @Test
    public void testAlerts() {
        java.util.List<org.wso2.carbon.messaging.Header> headers = new ArrayList<>();
        headers.add(new org.wso2.carbon.messaging.Header("Content-Type", APPLICATION_JSON));
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/rawmaterial", "POST", headers,
                                                                      "{'name':'Teak', 'amount':1000.0}");
        Services.invokeNew(result, "productMaterialListener", requestMsg);


        requestMsg = MessageUtils.generateHTTPMessage("/productionmaterial", "POST", headers,
                                                                      "{'name':'Teak','amount':500.0}");
        Services.invokeNew(result, "productMaterialListener", requestMsg);

        requestMsg = MessageUtils.generateHTTPMessage("/alerts", "GET");
        HttpCarbonMessage responseMsg = Services.invokeNew(result, "productMaterialListener", requestMsg);
        String responseMsgPayload =
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload, "ALERT!! : Material usage is higher than the expected limit for " +
                                                "material : Teak, usage difference (%) : 50.0");
    }

    @AfterClass
    public void release() {
        result = null;
    }
}

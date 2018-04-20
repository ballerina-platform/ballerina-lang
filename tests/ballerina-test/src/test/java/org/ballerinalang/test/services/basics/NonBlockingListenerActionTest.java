/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.test.services.basics;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Non-Blocking listener action test class.
 *
 * @since 0.970
 */
public class NonBlockingListenerActionTest {

    private static final String TEST_ENDPOINT_NAME = "echoEP";
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BServiceUtil.setupProgramFile(this, "test-src/services/listener-action-test.bal");
    }

    @Test()
    public void testServiceAvailabilityCheck() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/listener/message", "GET");
        HTTPCarbonMessage responseMsg = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, requestMsg);
        String responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload, "sample value");

        requestMsg = MessageUtils.generateHTTPMessage("/listener/message", "GET");
        responseMsg = Services.invokeNew(compileResult, TEST_ENDPOINT_NAME, requestMsg);
        responseMsgPayload = StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload, "done");
    }
}

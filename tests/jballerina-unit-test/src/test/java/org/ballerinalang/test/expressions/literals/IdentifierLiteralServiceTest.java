/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.expressions.literals;

import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * identifier literals in service and resource names.
 */
@Test(groups = { "disableOnOldParser" })
public class IdentifierLiteralServiceTest {

    private static final int MOCK_ENDPOINT_PORT = 9090;

    @BeforeClass
    public void setup() {
        BCompileUtil.compile("test-src/expressions/literals/identifierliteral/identifier-literal-service.bal");
    }

    @Test(description = "Test using identifier literals in service and resource names")
    public void testUsingIdentifierLiteralsInServiceAndResourceNames() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/identifierLiteral/resource", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("key").stringValue(), "keyVal");
        Assert.assertEquals(((BMap<String, BValue>) bJson).get("value").stringValue(), "valueOfTheString");
    }

    @Test(description = "Test identifier literals payload")
    public void testIdentifierLiteralsInPayload() {
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/identifierLiteral/resource2", "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg);
        Assert.assertNotNull(response);
        String payload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(payload, "hello");
    }
}

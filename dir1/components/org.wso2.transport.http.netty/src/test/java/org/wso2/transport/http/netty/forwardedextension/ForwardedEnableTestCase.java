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

package org.wso2.transport.http.netty.forwardedextension;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.config.ForwardedExtensionConfig;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.TestUtil;

import static org.testng.AssertJUnit.assertEquals;

/**
 * A test class for enable Forwarded behaviour.
 */
public class ForwardedEnableTestCase extends ForwardedClientTemplate {
    @BeforeClass
    public void setUp() {
        senderConfiguration.setForwardedExtensionConfig(ForwardedExtensionConfig.ENABLE);
        super.setUp();
    }

    @Test
    public void testSingleHeader() {
        try {
            HttpCarbonMessage response = sendRequest(new DefaultHttpHeaders());
            assertEquals(response.getHeader(Constants.FORWARDED), "by=127.0.0.1; proto=http");

            response = sendRequest(new DefaultHttpHeaders()
                    .set(Constants.FORWARDED, "for=192.0.2.43;by=203.0.113.60;proto=http;host=example.com"));
            assertEquals(response.getHeader(Constants.FORWARDED),
                    "for=192.0.2.43, for=203.0.113.60; by=127.0.0.1; host=example.com; proto=http");
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running postTest", e);
        }
    }

    @Test
    public void testMultipleHeader() {
        try {
            HttpCarbonMessage response = sendRequest(TestUtil.getForwardedHeaderSet1());
            assertEquals(response.getHeader(Constants.FORWARDED),
                    "for=203.0.113.60; by=127.0.0.1; host=example.com; proto=http");
            assertEquals(response.getHeader(Constants.X_FORWARDED_FOR), "123.34.24.67");

        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while running postTest", e);
        }
    }
}

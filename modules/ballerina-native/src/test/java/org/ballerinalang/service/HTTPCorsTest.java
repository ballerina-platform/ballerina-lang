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

package org.ballerinalang.service;

import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.MessageUtils;
import org.ballerinalang.testutils.Services;
import org.ballerinalang.util.codegen.ProgramFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

import static org.ballerinalang.services.dispatchers.http.Constants.AC_ALLOW_CREDENTIALS;
import static org.ballerinalang.services.dispatchers.http.Constants.AC_ALLOW_ORIGIN;

/**
 * Test cases related to HTTP CORS
 */
public class HTTPCorsTest {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = EnvironmentInitializer.setupProgramFile("lang/service/corsTest.bal");
    }

    @Test(description = "Test for CORS override at two levels")
    public void testServiceResourceCorsOverride() {
        String path = "/hello1/test1";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "POST", "Hello there");
        cMsg.setHeader("Origin", "http://www.wso2.com");
        CarbonMessage response = Services.invoke(cMsg);

        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("echo").asText(), "resCors"
                , "Resource dispatched to wrong template");
        String origin = response.getHeader(AC_ALLOW_ORIGIN);
        String cred = response.getHeader(AC_ALLOW_CREDENTIALS);
        Assert.assertEquals(origin, "http://www.wso2.com");
        Assert.assertEquals(cred, "true");
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(programFile);
    }

}

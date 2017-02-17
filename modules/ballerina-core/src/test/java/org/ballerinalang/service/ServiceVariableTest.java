/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.service;

import org.ballerinalang.core.EnvironmentInitializer;
import org.ballerinalang.core.utils.MessageUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.util.Services;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Service/Resource variable test class.
 */
public class ServiceVariableTest {

    BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = EnvironmentInitializer.setup("lang/service/serviceLevelVariable.bal");
    }

    @Test
    public void testServiceLevelVariable() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/var/message", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response);
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(bLangProgram);
    }
}

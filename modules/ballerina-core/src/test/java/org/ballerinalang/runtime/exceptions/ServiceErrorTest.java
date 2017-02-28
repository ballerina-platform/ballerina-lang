/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.runtime.exceptions;

import org.ballerinalang.core.EnvironmentInitializer;
import org.ballerinalang.core.utils.MessageUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.util.Services;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Test Cases for Service Related Error handling.
 */
public class ServiceErrorTest {

    private BLangProgram bLangProgram;
    private PrintStream original;

    @BeforeClass
    public void setup() {
        original = System.err;
        bLangProgram = EnvironmentInitializer.setup("lang/exceptions/service/service.bal");
    }

    @AfterClass
    public void tearDown() {
        System.setErr(original);
        EnvironmentInitializer.cleanup(bLangProgram);
    }

    @Test(description = "Testcase for resource level error handling.")
    public void testServiceLevelVariable() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(outContent));
            CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/var/message", "GET");
            DefaultCarbonMessage response = (DefaultCarbonMessage) Services.invoke(cMsg);
            Assert.assertNotNull(response);
            String out = org.ballerinalang.model.util.MessageUtils.getStringFromInputStream(response.getInputStream());
            Assert.assertEquals(out, "error in ballerina program: arrays index out of range: Index: 10, Size: 4");

            String expectedStackTrace = "error in ballerina program: arrays index out of range: Index: 10, Size: 4\n" +
                    "\t at echo(service.bal:8)\n" +
                    "\t at echo(service.bal:6)\n" +
                    "\t at service echo(service.bal:2)\n";
            Assert.assertEquals(outContent.toString(), expectedStackTrace);
        } finally {
            outContent.close();
            System.setErr(original);
        }
    }
}

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

package org.ballerinalang.test.service.grpc.sample;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.Utils;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for gRPC client invocation of unavailable service.
 */
@Test(groups = "grpc-test")
public class ServiceUnavailableTestCase extends IntegrationTestCase {

    @BeforeClass
    private void setup() throws Exception {
        Utils.checkPortAvailability(9110);
        TestUtils.prepareBalo(this);
    }

    @Test(description = "Test invoking unavailable service. Connector error is expected with connection refused.")
    public void testUnavailableServiceInvoke() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "clients", "unavailable_service_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        BString request = new BString("WSO2");
        final String expectedMsg = "Error from Connector: Status{ code UNAVAILABLE, description null, cause " +
                "Connection refused:";
        final String expectedHostDetails = "localhost/127.0.0.1:9110";

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingClient", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertTrue(responses[0].stringValue().startsWith(expectedMsg) && responses[0].stringValue()
                .contains(expectedHostDetails));
    }

}

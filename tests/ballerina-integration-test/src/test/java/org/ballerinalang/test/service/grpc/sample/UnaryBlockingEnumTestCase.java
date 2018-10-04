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
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for testing Enum.
 */
@Test(groups = "grpc-test")
public class UnaryBlockingEnumTestCase extends GrpcBaseTest {

    private CompileResult result;
    private BServerInstance enumServer;

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        enumServer = new BServerInstance(balServer);
        String enumServiceBal = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "grpc"
                        + File.separator + "enum" + File.separator + "grpc_enum_test_service.bal").getAbsolutePath();
        enumServer.startServer(enumServiceBal, new int[]{8555});
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "clients", "grpc_enum_test_client.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test
    public void testEnumBlockingBallerinaClient() {
        final String serverMsg = "r";
        BValue[] responses = BRunUtil.invoke(result, "testEnum");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @AfterClass
    public void teardown() throws Exception {
        enumServer.shutdownServer();
    }
}

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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for error gRPC service.
 *
 * @since 0.982.0
 */
@Test(groups = "grpc-test")
public class ErrorServiceTestCase extends BaseTest {

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
    }

    @Test(description = "Test case for running unary service without listener port")
    public void testServiceWithoutPort() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "errorservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "service_without_port.bal", false, false);
        Assert.assertEquals(result.getErrorCount(), 1);
        Assert.assertEquals(
                result.getDiagnostics()[0].getMessage(), "missing required parameter 'port' in call to 'new'()");
    }

    @Test(description = "Test case for running unary service with same port", enabled = false)
    public void testServiceWithSamePort() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "errorservices",
                "service_with_sameport.bal");
        try {
            BServerInstance grpcServer = new BServerInstance(balServer);
            grpcServer.startServer(balFilePath.toAbsolutePath().toString());
            Assert.fail("Service should not start with same listener port");
        } catch (BLangRuntimeException | BallerinaTestException ex) {
            Assert.assertTrue(ex.getMessage()
                    .contains("'localhost:8085': Address already in use"));
        }
    }
}

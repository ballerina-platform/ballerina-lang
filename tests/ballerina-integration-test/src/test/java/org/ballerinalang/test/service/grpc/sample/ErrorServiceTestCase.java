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
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.test.BaseTest;
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
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "errorservices", "service_without_port.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertUnaryCompileResult(result);
        try {
            BServiceUtil.runService(result);
            Assert.fail("Service should not start without listener port");
        } catch (BLangRuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains("error: Listener port is not defined!"));
        }
    }

    @Test(description = "Test case for running secured unary service without keystore file")
    public void testServiceWithoutKeystoreFile() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "errorservices",
                "service_without_keystorefile.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertUnaryCompileResult(result);
        try {
            BServiceUtil.runService(result);
            Assert.fail("Secure Service should not start without keystore file");
        } catch (BLangRuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains(
                    "error: Keystore file location must be provided for secure connection"));
        }
    }

    @Test(description = "Test case for running secured unary service without keystore password")
    public void testServiceWithoutKeystorePassword() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "errorservices",
                "service_without_keystorepass.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertUnaryCompileResult(result);
        try {
            BServiceUtil.runService(result);
            Assert.fail("Secure Service should not start without keystore file");
        } catch (BLangRuntimeException ex) {
            Assert.assertTrue(ex.getMessage()
                    .contains("error: Keystore password must be provided for secure connection"));
        }
    }

    @Test(description = "Test case for running unary service with same port")
    public void testServiceWithSamePort() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "errorservices",
                "service_with_sameport.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertUnaryCompileResult(result);
        try {
            BServiceUtil.runService(result);
            Assert.fail("Service should not start with same listener port");
        } catch (BLangRuntimeException ex) {
            Assert.assertTrue(ex.getMessage()
                    .contains("'localhost:8085': Address already in use"));
        }
    }

    private void assertUnaryCompileResult(CompileResult result) {
        Assert.assertEquals(result.getErrorCount(), 0, "Compilation errors in source file");
        Assert.assertEquals(result.getAST().getServices().size(), 1, "File should have one service defined.");
        Assert.assertEquals(result.getAST().getServices().get(0).getAnnotationAttachments().size(), 1,
                "Service node should have one default annotation");
        Assert.assertEquals(result.getAST().getServices().get(0).getAnnotationAttachments().get(0).getAnnotationName
                ().getValue(), "ServiceDescriptor");
    }
}

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
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnostic;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test gRPC service build when invalid message type is passed. Should get a compilation error.
 */
public class InvalidMessageTypeTestCase extends IntegrationTestCase {
    
    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
    }

    @Test(description = "Test gRPC service build when invalid message type is passed as request. Should get a " +
            "compilation error.")
    public void testInvalidRequestMessageType() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "invalid_request_message.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        Assert.assertEquals(result.getErrorCount(), 1);
        Assert.assertEquals(((BDiagnostic) result.getDiagnostics()[0]).msg, "Invalid message type. Message type " +
                "doesn't have type symbol");
    }


    @Test(description = "Test gRPC service build when invalid message type is passed as response. Should get a " +
            "compilation error.")
    public void testInvalidResponseMessageType() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "invalid_response_message.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        Assert.assertEquals(result.getErrorCount(), 1);
        Assert.assertEquals(((BDiagnostic) result.getDiagnostics()[0]).msg, "Invalid message type. Message type " +
                "doesn't have type symbol");
    }


}

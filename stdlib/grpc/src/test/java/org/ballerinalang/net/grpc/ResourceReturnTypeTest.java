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
package org.ballerinalang.net.grpc;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for gRPC resource return type.
 *
 */
public class ResourceReturnTypeTest {

    @Test
    public void testValidReturnType() {

        Path serviceBalPath = Paths.get("src", "test", "resources", "test-src",
                "resource_with_vaild_return_type.bal");
        CompileResult result = BCompileUtil.compileOffline(serviceBalPath.toAbsolutePath().toString());
        Assert.assertEquals(0, result.getErrorCount());
    }

    @Test
    public void testInvalidReturnType() {

        Path serviceBalPath = Paths.get("src", "test", "resources", "test-src",
                "resource_with_invalid_return_type.bal");
        CompileResult result = BCompileUtil.compileOffline(serviceBalPath.toAbsolutePath().toString());
        Assert.assertEquals(result.getErrorCount(), 5);
        Assert.assertEquals(result.getDiagnostics().length, 5);
        Assert.assertEquals(result.getDiagnostics()[0].getMessage(), "invalid resource function return type " +
                "'int', expected a subtype of 'error?' containing '()'");
    }
}

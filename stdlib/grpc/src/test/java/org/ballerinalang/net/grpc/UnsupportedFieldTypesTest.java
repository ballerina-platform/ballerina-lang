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
 * Test class for gRPC unsupported types.
 *
 */
public class UnsupportedFieldTypesTest {

    @Test
    public void testUnsupportedJsonType() {

        Path serviceBalPath = Paths.get("src", "test", "resources", "test-src",
                "negative_json_message_type.bal");
        CompileResult result = BCompileUtil.compileOffline(serviceBalPath.toAbsolutePath().toString());
        Assert.assertEquals(1, result.getErrorCount());
        Assert.assertEquals(1, result.getDiagnostics().length);
        Assert.assertEquals("Unsupported field type, field type json currently not supported.", result
                .getDiagnostics()[0].getMessage());
    }

    @Test
    public void testUnsupportedMapType() {

        Path serviceBalPath = Paths.get("src", "test", "resources", "test-src",
                "negative_map_message_type.bal");
        CompileResult result = BCompileUtil.compileOffline(serviceBalPath.toAbsolutePath().toString());
        Assert.assertEquals(1, result.getErrorCount());
        Assert.assertEquals(1, result.getDiagnostics().length);
        Assert.assertEquals("Unsupported field type, field type map currently not supported.", result.getDiagnostics
                ()[0].getMessage());
    }
}

/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.messaging.artemis;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test Artemis Listener Service Compilation.
 */
public class ArtemisDataBindingCompilationTest {
    private static final Path TEST_PATH = Paths.get("src", "test", "resources", "test-src", "data-binding");

    @Test(description = "Successfully compiling Artemis services", enabled = false)
    public void testValidService() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("artemis_success.bal").toAbsolutePath()
                .toString());

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "Unsupported type int[]")
    public void testUnsupportedTypeInResource() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("unsupported_type.bal")
                .toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for onMessage resource in service : " +
                        "The second parameter " +
                        "should be a string, json, xml, byte[], map or a record type",
                27, 5);
    }

    @Test(description = "Unsupported map type map<int[]>")
    public void testUnsupportedMapTypeInResource() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("unsupported_map_type.bal")
                .toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for onMessage resource in service :" +
                        " The second parameter " +
                        "should be a map of string, int, float, byte, boolean or byte[]",
                27, 5);
    }

    @Test(description = "Mandatory onError resource")
    public void testMandatoryOnErrorResource() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("mandatory_on_error_resource.bal")
                .toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                "onError resource function is not found; any errors that occur due to data " +
                        "binding will be discarded along with the message",
                27, 5);
    }

    private void assertExpectedDiagnosticsLength(CompileResult compileResult) {
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
    }
}

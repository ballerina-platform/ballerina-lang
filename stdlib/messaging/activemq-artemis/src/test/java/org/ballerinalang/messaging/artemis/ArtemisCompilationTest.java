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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test Artemis Listener Service Compilation.
 *
 * @since 0.995.0
 */
public class ArtemisCompilationTest {
    private static final Path TEST_PATH = Paths.get("src", "test", "resources", "test-src");

    @Test(description = "Successfully compiling Artemis service")
    public void testValidService() {
        CompileResult compileResult = getCompileResult("artemis_success.bal");

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "More than expected number of resources in the service")
    public void testMoreResourcesInService() {
        CompileResult compileResult = getCompileResult("artemis_more_resources.bal");

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0, "Only one resource is allowed in the service", 24, 1);
    }

    @Test(description = "More than expected number of annotations for the service")
    public void testMoreServiceAnnotationsForService() {
        CompileResult compileResult = getCompileResult("artemis_multiple_service_annotation.bal");

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0, "There cannot be more than one Artemis service annotations", 29, 1);
    }

    @Test(description = "One service annotation is mandatory")
    public void testMandatoryServiceAnnotation() {
        CompileResult compileResult = getCompileResult("artemis_mandatory_service_annotation.bal");

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                                  "There has to be an artemis:ServiceConfig annotation declared for service", 19, 1);
    }

    @Test(description = "Resource returns can only be error or nil")
    public void testResourceReturn() {
        CompileResult compileResult = getCompileResult("artemis_resource_return.bal");

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0, "Invalid return type: expected error?", 25, 5);
    }

    @Test(description = "Resource without resource parameters")
    public void testNoResourceParams() {
        CompileResult compileResult = getCompileResult("artemis_no_resource_params.bal");

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                                  "Invalid resource signature for onMsg resource: Unexpected parameter count(expected" +
                                          " parameter count 1 or 2)", 25, 5);
    }

    @Test(description = "Resource with multiple resource parameters")
    public void testMultipleResourceParams() {
        CompileResult compileResult = getCompileResult("artemis_more_resource_params.bal");

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                                  "Invalid resource signature for onMsg resource: Unexpected parameter count(expected" +
                                          " parameter count 1 or 2)", 25, 5);
    }

    @Test(description = "Invalid resource parameters")
    public void testDifferentResourceParams() {
        CompileResult compileResult = getCompileResult("artemis_different_resource_param.bal");

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(
                compileResult, 0,
                "Invalid resource signature for xyz resource: The first parameter should be an artemis:Message", 25, 5);
    }

    private CompileResult getCompileResult(String s) {
        return BCompileUtil.compile(TEST_PATH.resolve(s).toAbsolutePath().toString());
    }

    private void assertExpectedDiagnosticsLength(CompileResult compileResult) {
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
    }
}

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.taintchecking;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test taint checking related annotations (sensitive / tainted / untainted).
 *
 * @since 0.965.0
 */
public class AnnotationTest {

    // Test @sensitive annotation.

    @Test
    public void testSensitive() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/annotations/sensitive.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testSensitiveNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/annotations/sensitive-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 39);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 3, 50);
    }

    @Test
    public void testSensitiveDefaultArgs() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/annotations/sensitive-default-args.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testSensitiveDefaultArgsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/annotations/sensitive-default-args-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 56);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 3, 81);
    }

    @Test
    public void testSensitiveRestArgs() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/annotations/sensitive-rest-args.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testSensitiveRestArgsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/annotations/sensitive-rest-args-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 30);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 3, 29);
    }

    @Test
    public void testSensitiveRestParamsWithVaryingInvocationArgs() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/annotations/sensitive-rest-params-with-varying-invocation-args.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testSensitiveRestParamsWithVaryingInvocationArgsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/annotations/" +
                        "sensitive-rest-params-with-varying-invocation-args-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 3);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'restParams'", 2, 46);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'restParams'", 3, 68);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'restParams'", 4, 82);
    }

    @Test
    public void testSensitiveDefaultParamsWithUnorderedArgs() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/annotations/sensitive-default-params-with-unordered-args.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testSensitiveDefaultParamsWithUnorderedArgsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/annotations/" +
                        "sensitive-default-params-with-unordered-args-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'defaultableInput2'", 2, 43);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'defaultableInput2'", 3, 61);
    }
    // Test @tainted annotation.

    @Test
    public void testTainted() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/annotations/tainted.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 20);
    }

    // Test @untainted annotation.

    @Test
    public void testUntainted() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/annotations/untainted.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }
}

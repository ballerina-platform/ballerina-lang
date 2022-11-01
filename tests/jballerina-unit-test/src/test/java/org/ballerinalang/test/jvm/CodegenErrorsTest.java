/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.jvm;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases to cover scenarios where backend-jvm code generates errors.
 *
 * @since 1.0
 */
@Test
public class CodegenErrorsTest {

    @Test
    public void testTooLargeMethod() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/too-large-method.bal");
        BAssertUtil.validateError(result, 0, "method is too large: 'getXML'", "too-large-method.bal", 17, 1);
    }

    @Test
    public void testTooLargeObjectMethod() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/too-large-object-field.bal");
        BAssertUtil.validateError(result, 0, "method is too large: '$init$'", "too-large-object-field.bal", 17, 1);
    }

    @Test
    public void testTooLargeObjectField() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/too-large-object-method.bal");
        BAssertUtil.validateError(result, 0, "method is too large: 'getXML'", "too-large-object-method.bal", 18, 5);
    }

    @Test
    public void testTooLargePackageVar() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/too-large-package-variable.bal");
        BAssertUtil.validateError(result, 0, "method is too large: '.<init>'", ".", 1, 1);
    }

    @Test
    public void testTooLargeStringConstants() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/too-large-string-constants.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void testTooLargeProject() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/largePackage");
        BRunUtil.invoke(result, "main");
    }

    @Test
    public void testLargeMethods() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/largeMethods");
        BRunUtil.invoke(result, "main");
    }

    @Test
    public void testLargeNumberOfListeners() {
        CompileResult compileResult = BCompileUtil.compile("test-src/jvm/large-number-of-listeners.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        final Object result = BRunUtil.invoke(compileResult, "getStartCount");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.toString(), "500");
    }

    @Test
    public void testTooLargeHardCodedStringValue() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/largeStringConstants");
        Assert.assertEquals(result.getErrorCount(), 0);
        BRunUtil.invoke(result, "main");
    }

    @Test
    public void testTooLargeMethodWithMultipleCheckedExpression() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/checked_expr_method_too_large.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }
}

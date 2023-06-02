/*
*  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.expressions.invocations;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test invocation of function pointers which are fields of a class/object with method invocation syntax.
 *
 * @since 2.0
 */
public class ObjectFieldFunctionPointerInvocationTest {

    private CompileResult funcInvocationExpResult;

    @BeforeClass
    public void setup() {
        funcInvocationExpResult = BCompileUtil.compile(
                "test-src/expressions/invocations/object_field_method_invocation.bal");
    }

    @Test
    public void testInvocationOfObjectField() {
        BRunUtil.invoke(funcInvocationExpResult, "testInvocationOfObjectField");
    }

    @Test
    public void testInvocationOfObjectFieldWithMutableState() {
        BRunUtil.invoke(funcInvocationExpResult, "testInvocationOfObjectFieldWithMutableState");
    }

    @Test
    public void testMethodCallNegativeCases() {
        CompileResult neg = BCompileUtil.compile(
                "test-src/expressions/invocations/object_field_method_invocation_negative.bal");
        int i = 0;
        validateError(neg, i++, "too many arguments in call to 'f()'", 29, 13);
        validateError(neg, i++, "missing required parameter '' in call to 'g()'", 31, 17);
        validateError(neg, i++, "incompatible types: expected 'string', found 'int'", 32, 23);
        validateError(neg, i++, "incompatible types: expected 'int', found 'string'", 33, 26);
        validateError(neg, i++, "incompatible types: expected 'int', found 'string'", 35, 19);
        Assert.assertEquals(i,  neg.getErrorCount());
    }

    @AfterClass
    public void tearDown() {
        funcInvocationExpResult = null;
    }
}

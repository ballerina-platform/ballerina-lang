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
package org.ballerinalang.test.action.start;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test start action invocation of function pointers which are fields of a class/object with start action syntax.
 *
 * @since 2.0
 */
public class ObjectFieldFunctionPointerStartActionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(
                "test-src/action/start/object_field_start_method_invocation.bal");
    }

    @Test
    public void testInvocationOfObjectField() {
        BRunUtil.invoke(result, "testInvocationOfObjectField");
    }

    @Test
    public void testInvocationOfObjectFieldWithMutableState() {
        BRunUtil.invoke(result, "testInvocationOfObjectFieldWithMutableState");
    }

    @Test
    public void testInvocationOfObjectFieldWithTypeInit() {
        BRunUtil.invoke(result, "testInvocationOfObjectFieldWithTypeInit");
    }

    @Test
    public void testMethodCallNegativeCases() {
        CompileResult neg = BCompileUtil.compile(
                "test-src/action/start/object_field_start_action_method_invocation_negative.bal");
        int i = 0;
        validateError(neg, i++, "missing required parameter '' in call to 'g()'", 30, 25);
        validateError(neg, i++, "incompatible types: expected 'future<string>', found 'future<int>'", 31, 31);
        validateError(neg, i++, "incompatible types: expected 'int', found 'string'", 32, 40);
        validateError(neg, i++, "incompatible types: expected 'int', found 'string'", 34, 25);
        Assert.assertEquals(i,  neg.getErrorCount());
    }
}

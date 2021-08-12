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
package org.ballerinalang.test.action.start;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Start action test cases.
 */
public class StartActionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/action/start/start_action.bal");
    }

    @Test(description = "Test negative start action usage")
    public void testStartActionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/action/start/start-action-negative.bal");
        int indx = 0;
        BAssertUtil.validateError(result, indx++, "action invocation as an expression not allowed here", 37, 17);
        BAssertUtil.validateError(result, indx++, "action invocation as an expression not allowed here", 38, 32);
        BAssertUtil.validateError(result, indx++, "action invocation as an expression not allowed here", 39, 37);
        BAssertUtil.validateError(result, indx++, "'wait' cannot be used with actions", 53, 14);
        BAssertUtil.validateError(result, indx++, "invalid remote method call: expected a client object, " +
                "but found 'int'", 53, 14);
        BAssertUtil.validateError(result, indx++, "missing close parenthesis token", 53, 31);
        BAssertUtil.validateError(result, indx++, "missing open parenthesis token", 53, 31);
        BAssertUtil.validateError(result, indx++, "action invocation as an expression not allowed here", 56, 37);
        BAssertUtil.validateError(result, indx++, "'wait' cannot be used with actions", 58, 26);
        BAssertUtil.validateError(result, indx++, "action invocation as an expression not allowed here", 58, 49);
        BAssertUtil.validateError(result, indx++, "action invocation as an expression not allowed here", 71, 17);
        BAssertUtil.validateError(result, indx++, "incompatible types: expected 'int', found eventual type " +
                "'(int|error)' for wait future expression 'bar($missingNode$_6)'", 72, 13);
        BAssertUtil.validateError(result, indx++, "'wait' cannot be used with actions", 72, 18);
        BAssertUtil.validateError(result, indx++, "action invocation as an expression not allowed here", 72, 28);
        BAssertUtil.validateError(result, indx++, "action invocation as an expression not allowed here", 76, 25);
        BAssertUtil.validateError(result, indx++, "incompatible types: expected 'other', found 'int'", 90, 13);
        BAssertUtil.validateError(result, indx++, "incompatible types: '(int[]|error)' is not an iterable collection"
                , 90, 22);
        BAssertUtil.validateError(result, indx++, "'wait' cannot be used with actions", 90, 27);
        BAssertUtil.validateError(result, indx++, "missing close parenthesis token", 97, 1);
        BAssertUtil.validateError(result, indx++, "missing expression", 97, 1);
        BAssertUtil.validateError(result, indx++, "missing open parenthesis token", 97, 1);
        BAssertUtil.validateError(result, indx++, "missing semicolon token", 97, 1);
        BAssertUtil.validateError(result, indx++, "invalid expression in start action", 100, 11);
        Assert.assertEquals(result.getErrorCount(), indx);
    }

    @Test(dataProvider = "FuncList")
    public void testStartAction(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @Test(description = "Test casting for lambda functions")
    public void testStartLambdaParameterCasting() {
        BRunUtil.invoke(result, "testCast");
    }

    @Test(description = "Test casting for lambda functions for functions from another package")
    public void testStartLambdaParameterCastingFromOtherPackage() {
        CompileResult compileResult = BCompileUtil.compile("test-src/action/start/StartTypeCastProject");
        BRunUtil.ExitDetails output = BRunUtil.run(compileResult, new String[]{});
        Assert.assertEquals("", output.errorOutput);
    }

    @DataProvider(name = "FuncList")
    public Object[][] getFunctionNames() {
        return new Object[][]{
                {"testRecFieldFuncPointerAsyncCall"},
                {"testObjectMethodsAsAsyncCalls"}
        };
    }
}

/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.ballerinalang.test.functions;

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for expression bodied functions.
 *
 * @since 1.2.0
 */
public class ExprBodiedFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/functions/expr_bodied_functions.bal");
    }

    @Test(groups = { "disableOnOldParser" })
    public void testSyntaxErrors() {
        CompileResult result = BCompileUtil.compile("test-src/functions/expr_bodied_functions_negative.bal");
        int index = 0;

        validateError(result, index++,
                "a type compatible with mapping constructor expressions not found in type 'int'", 18, 43);
        validateError(result, index++, "missing open bracket token", 19, 1);
        validateError(result, index++, "invalid token 'return'", 19, 12);
        validateError(result, index++, "missing close bracket token", 19, 17);
        validateError(result, index++, "missing colon token", 19, 17);
        validateError(result, index++, "missing identifier", 19, 17);
        validateError(result, index++, "missing object keyword", 19, 17);
        validateError(result, index++, "missing open brace token", 19, 17);
        validateError(result, index++, "invalid token ';'", 20, 1);
        validateError(result, index++, "missing close brace token", 21, 1);
        validateError(result, index++, "missing semicolon token", 21, 1);

        validateError(result, index++,
                "incompatible types: expected 'int', found 'function (int,int) returns (int)'", 26, 1);
        validateError(result, index++, "invalid token 'external'", 26, 1);
        validateError(result, index++, "invalid token 'sum'", 26, 13);
        validateError(result, index++, "incompatible types: expected 'int', found 'typedesc<int>'", 26, 43);
        validateError(result, index++, "missing semicolon token", 26, 47);
        validateError(result, index++, "unknown type 'x'", 26, 47);
        validateError(result, index++, "missing identifier", 26, 49);

        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testTaintChecking() {
        CompileResult result = BCompileUtil.compile("test-src/functions/expr_bodied_functions_taint.bal");
        int index = 0;
        validateError(result, index++, "tainted value passed to untainted parameter 'param'", 25, 32);
        validateError(result, index++, "tainted value passed to untainted parameter 'param'", 32, 32);
        validateError(result, index++, "tainted value passed to untainted parameter 'param'", 41, 32);
        assertEquals(result.getErrorCount(), index);
    }

    @Test(dataProvider = "FunctionList")
    public void testExprBodiedFunctions(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @Test
    public void testClosures() {
        BRunUtil.invoke(compileResult, "testClosures", new Object[]{10});
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*NumberParsingError \\{\"message\":\"'string' value " +
                  "'invalid' cannot be converted to 'int'.*")
    public void testCheckPanic() {
        BRunUtil.invoke(compileResult, "testCheckPanic");
    }

    @DataProvider(name = "FunctionList")
    public Object[][] getTestFunctions() {
        return new Object[][]{
                {"testReturningLiterals"},
                {"testReturningLists"},
                {"testBinaryExprs"},
                {"testNilReturningFunctions"},
                {"testRecordAsAnExpr"},
                {"testSameVarRefAsExpr"},
                {"testFunctionInvocation"},
                {"testFunctionInvocationAsLambdas"},
                {"testExprsBodiesInMethods"},
                {"testObjectInitBodyAsAnExpr"},
                {"testObjectsAsExprBody"},
                {"testAnonFuncsAsExprBody"},
                {"testReturningXML"},
                {"testReturningStringTemplate"},
                {"testReturningServiceConstructors"},
                {"testLetExprAsExprBody"},
        };
    }
}

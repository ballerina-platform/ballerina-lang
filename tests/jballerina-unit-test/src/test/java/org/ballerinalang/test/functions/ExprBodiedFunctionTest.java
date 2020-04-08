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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    @Test
    public void testSyntaxErrors() {
        CompileResult result = BCompileUtil.compile("test-src/functions/expr_bodied_functions_negative.bal");
        Set<Integer> errorLines = new HashSet<>(Arrays.asList(19, 23, 26));
        for (Diagnostic diagnostic : result.getDiagnostics()) {
            if (!errorLines.contains(diagnostic.getPosition().getStartLine())) {
                Assert.fail("Unexpected error at line: " + diagnostic.getPosition().getStartLine());
            }
        }
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
          expectedExceptionsMessageRegExp = ".*NumberParsingError message='string' value " +
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

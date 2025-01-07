/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.ballerinalang.test.types.constant;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Negative tests for list constructor expr in constant context.
 */
public class ListConstantNegativeTest {

    @Test
    public void testListConstructorExprAsConstantExprNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/list_constant_negative.bal");

        int i = 0;
        validateError(compileResult, i++, "incompatible types: expected 'string', found '3'", 17, 29);
        validateError(compileResult, i++, "incompatible types: expected 'string', found '1'", 17, 32);
        validateError(compileResult, i++, "size mismatch in closed array. expected '1', but found '2'", 18, 25);
        validateError(compileResult, i++, "operator '+' not defined for '2.0f' and '1d'", 19, 40);
        validateError(compileResult, i++, "expression is not a constant expression", 20, 31);
        validateError(compileResult, i++, "incompatible types: expected 'float', found '1'", 27, 32);
        validateError(compileResult, i++, "incompatible types: expected 'float', found '3'", 27, 32);
        validateError(compileResult, i++, "tuple and expression size does not match", 29, 33);
        validateError(compileResult, i++, "invalid usage of list constructor: type 'record {| int a; 2 b; |}'" +
                " does not have a filler value", 30, 41);
        validateError(compileResult, i++, "invalid usage of list constructor: type '1|2' does not have a filler value",
                31, 26);
        validateError(compileResult, i++, "incompatible types: expected '1', found '3'", 33, 27);
        validateError(compileResult, i++, "incompatible types: expected 'byte', found '300'", 34, 24);
        validateError(compileResult, i++, "size mismatch in closed array. expected '2', but found '3'", 35, 23);
        validateError(compileResult, i++, "incompatible types: expected 'byte', found '300'", 36, 24);
        validateError(compileResult, i++, "incompatible types: expected 'byte', found '400'", 36, 29);
        validateError(compileResult, i++, "incompatible types: expected 'byte', found '300'", 37, 44);
        validateError(compileResult, i++, "incompatible types: expected 'byte', found '400'", 37, 49);
        validateError(compileResult, i++, "incompatible types: expected 'int:Signed8', found '500'", 37, 54);
        validateError(compileResult, i++, "ambiguous type '(float[]|decimal[])'", 39, 33);
        validateError(compileResult, i++, "incompatible types: expected '(string[]|boolean[])', found '[1, 2.0]'",
                40, 34);
        validateError(compileResult, i++, "incompatible types: expected '(int|float)', found '[1, 2.0]'", 41, 25);
        validateError(compileResult, i++, "incompatible types: expected 'int', found '[1, 2.0]'", 42, 19);
        validateError(compileResult, i++, "const expressions are not yet supported here", 43, 18);
        validateError(compileResult, i++, "constant declaration not yet supported for type " +
                "'[record {| int a; 2 b; |} 0]'", 44, 7);
        validateError(compileResult, i++, "constant declaration not yet supported for type '[int 0,table<int> 1]'",
                45, 7);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }

}

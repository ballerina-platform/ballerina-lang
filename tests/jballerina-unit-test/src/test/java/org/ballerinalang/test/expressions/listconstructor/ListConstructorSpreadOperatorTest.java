/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.listconstructor;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for list constructor spread operator.
 *
 * @since 2201.1.0
 */
public class ListConstructorSpreadOperatorTest {

    @Test
    public void testSpreadOperatorNegative() {
        CompileResult resultNegative = BCompileUtil.compile(
                "test-src/expressions/listconstructor/list_constructor_spread_operator_negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 18, 19);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a1'", 18, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 19, 25);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a2'", 19, 25);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 20, 25);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a3'", 20, 25);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 21, 26);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a4'", 21, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'other'", 22, 26);
        BAssertUtil.validateError(resultNegative, i++, "undefined symbol 'a5'", 22, 26);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected an array or a tuple, found 'int'", 27, 25);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 30, 25);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found '(int|string)'", 33, 19);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 38, 25);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 39, 25);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 40, 26);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 41, 26);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found '(int|string)'", 45, 19);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 49, 29);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected an array or a tuple, found 'int'", 54, 40);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected an array or a tuple, found 'int'", 55, 43);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found 'int'", 58, 35);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 61, 36);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '\"s\"', found 'string'", 67, 32);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 68, 40);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '(int|boolean)', found 'string'", 69, 45);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '(int|string)', found '(int|boolean)'", 70, 44);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found '(string|int)'", 74, 32);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '(int|string)', found 'any'", 78, 36);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 83, 35);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found '(int|string)'", 86, 35);
        BAssertUtil.validateError(resultNegative, i++, "tuple and expression size does not match", 89, 24);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 94, 32);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 95, 40);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'any'", 96, 45);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed member expected for 'int'", 97, 44);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'string', found '(int|string)'", 101, 35);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed member expected for 'string'", 105, 46);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 110, 22);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'int', found 'string'", 111, 23);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 117, 30);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 118, 25);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 119, 25);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 120, 26);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '\"x\"|\"y\"', found '(int|string)'", 121, 26);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '(string|boolean)', found '(int|string)'", 122, 37);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 125, 32);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '(int|string)', found '(int|boolean)'", 128, 44);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 135, 22);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 136, 27);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 137, 27);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 138, 23);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed length list expected", 139, 20);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed member expected for 'string'", 144, 41);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed member expected for 'any'", 147, 44);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed member expected for '(int|string)'", 150, 36);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of spread operator: fixed member expected for 'int'", 153, 47);
        BAssertUtil.validateError(resultNegative, i++, "invalid usage of list constructor: type '(string|int)' does not have a filler value", 158, 43);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }
}

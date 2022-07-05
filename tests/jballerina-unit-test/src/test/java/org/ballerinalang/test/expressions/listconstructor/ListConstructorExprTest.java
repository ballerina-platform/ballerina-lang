/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for list constructor expressions.
 */
public class ListConstructorExprTest {

    private CompileResult result, resultInferType, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/listconstructor/list_constructor.bal");
        resultInferType = BCompileUtil.compile("test-src/expressions/listconstructor/list_constructor_infer_type.bal");
        resultNegative = BCompileUtil.compile(
                "test-src/expressions/listconstructor/list_constructor_negative.bal");
    }

    @Test
    public void testListConstructorExpr() {
        Object returns = BRunUtil.invoke(result, "testListConstructorExpr");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void diagnosticsTest() {
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "invalid list constructor expression: " +
                "types cannot be inferred for '[hello, 123, 34.56]'", 32, 24);
        BAssertUtil.validateError(resultNegative, i++, "invalid list constructor expression: types cannot be " +
                "inferred for '[1, e]'", 35, 58);
        BAssertUtil.validateError(resultNegative, i++, "invalid list constructor expression: types cannot be " +
                "inferred for '[1, p]'", 38, 35);
        BAssertUtil.validateError(resultNegative, i++, "invalid list constructor expression: types cannot be " +
                "inferred for '[a, 4]'", 41, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of list constructor: type 'NoFillerObject' does not have a filler value",
                45, 31);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid usage of list constructor: type '[NoFillerObject,NoFillerObject]'" +
                        " does not have a filler value", 46, 56);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '[record {| int id; string name;" +
                                          " int city; |},record {| anydata...; |},boolean,string]', found '[record {|" +
                                          " int id; string name; string city; |},record {| int id; string name; " +
                                          "int age; |},int,string]'", 65, 40);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'record {| int id; string name; " +
                                          "string age; |}', found 'record {| int id; string name; int age; |}'",
                                  71, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'float', found 'int'",
                                  72, 16);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'readonly', found 'int[]'", 80,
                                  23);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'readonly', found 'future'",
                                  80, 28);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '(readonly|int[])', " +
                                          "found '[int,map<(boolean|int)>]'", 89, 25);
        BAssertUtil.validateError(resultNegative, i++, "ambiguous type '(boolean[][]|readonly)'", 93, 31);
        BAssertUtil.validateError(resultNegative, i++, "unknown type 'Foo'", 97, 5);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: 'int' cannot be cast to 'string'", 97, 23);
        BAssertUtil.validateError(resultNegative, i++, "unknown type 'Foo'", 98, 14);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: 'int' cannot be cast to 'string'", 98, 23);
        BAssertUtil.validateError(resultNegative, i++, "ambiguous type '(any|any[])'", 102, 19);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @Test
    public void testListConstructorAutoFillExpr() {
        BRunUtil.invoke(result, "testListConstructorAutoFillExpr");
    }

    @Test
    public void testListConstructorWithBroadACET() {
        BRunUtil.invoke(result, "testListConstructorWithAnyACET");
        BRunUtil.invoke(result, "testListConstructorWithAnydataACET");
        BRunUtil.invoke(result, "testListConstructorWithJsonACET");
    }

    @Test
    public void testTypeWithReadOnlyInUnionCET() {
        BRunUtil.invoke(result, "testTypeWithReadOnlyInUnionCET");
    }

    @Test
    public void testListConstructorInferType() {
        BRunUtil.invoke(resultInferType, "inferSimpleTuple");
        BRunUtil.invoke(resultInferType, "inferStructuredTuple");
        BRunUtil.invoke(resultInferType, "inferNestedTuple");
        BRunUtil.invoke(resultInferType, "testInferSameRecordsInTuple");
        BRunUtil.invoke(resultInferType, "testInferDifferentRecordsInTuple");
        BRunUtil.invoke(resultInferType, "testInferringForReadOnly");
        BRunUtil.invoke(resultInferType, "testInferringForReadOnlyInUnion");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultInferType = null;
        resultNegative = null;
    }
}

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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] returns = BRunUtil.invoke(result, "testListConstructorExpr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void diagnosticsTest() {
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "invalid list constructor expression: " +
                "types cannot be inferred for '[v1, v2, v3]'", 18, 24);
        BAssertUtil.validateError(resultNegative, i++, "tuple and expression size does not match",
                22, 31);
        BAssertUtil.validateError(resultNegative, i++, "tuple and expression size does not match",
                23, 56);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '[record {| int id; string name;" +
                                          " int city; |},record {| anydata...; |},boolean,string]', found '[record {|" +
                                          " int id; string name; string city; |},record {| int id; string name; " +
                                          "int age; |},int,string]'", 42, 40);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'record {| int id; string name; " +
                                          "string age; |}', found 'record {| int id; string name; int age; |}'",
                                  48, 13);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'float', found 'int'",
                                  49, 16);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'readonly', found 'int[]'", 57,
                                  23);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'readonly', found 'future'",
                                  57, 28);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected '(readonly|int[])', " +
                                          "found '[int,map<(boolean|int)>]'", 66, 25);
        BAssertUtil.validateError(resultNegative, i++, "ambiguous type '(boolean[][]|readonly)'", 70, 31);
        BAssertUtil.validateError(resultNegative, i++, "unknown type 'Foo'", 74, 5);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: 'int' cannot be cast to 'string'", 74, 23);
        BAssertUtil.validateError(resultNegative, i++, "unknown type 'Foo'", 75, 14);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: 'int' cannot be cast to 'string'", 75, 23);
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

    @Test (enabled = false)
    public void testListConstructorInferType() {
        BRunUtil.invoke(resultInferType, "inferSimpleTuple");
        BRunUtil.invoke(resultInferType, "inferStructuredTuple");
        BRunUtil.invoke(resultInferType, "inferNestedTuple");
        BRunUtil.invoke(resultInferType, "testInferSameRecordsInTuple");
        BRunUtil.invoke(resultInferType, "testInferDifferentRecordsInTuple");
        BRunUtil.invoke(resultInferType, "testInferringForReadOnly");
        BRunUtil.invoke(resultInferType, "testInferringForReadOnlyInUnion");
    }
}

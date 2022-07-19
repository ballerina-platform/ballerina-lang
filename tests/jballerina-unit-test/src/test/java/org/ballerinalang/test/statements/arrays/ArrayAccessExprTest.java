/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.arrays;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Array access expression test.
 *
 * @since 0.88
 */
public class ArrayAccessExprTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-access-expr.bal");
    }

    //TODO try to validate all the lines in the exception message
    @Test(description = "Test access an non-initialized arrays",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"array index out of range: index: 5, size: 0.*")
    public void testNonInitArrayAccess() {
        BRunUtil.invoke(compileResult, "testNonInitArrayAccess");
        Assert.fail("Test should fail at this point.");
    }
    @Test(description = "Test invalid index access",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"array index out of range: index: -2, size: 2.*")
    public void testUnaryConstExpressionInIndexAccess() {
        BRunUtil.invoke(compileResult, "testUnaryConstExpressionInIndexAccess");
        Assert.fail("Test should fail at this point.");
    }

    @Test(description = "Test arrays access expression")
    public void testArrayAccessExpr() {
        Object[] args = { (100), (5)};
        Object returns = BRunUtil.invoke(compileResult, "arrayAccessTest", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 210;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test arrays access with finite type")
    public void testArrayAccessWithFiniteType() {
        Object returns = BRunUtil.invoke(compileResult, "testArrayAccessWithFiniteType");
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test arrays access with unions with finite types")
    public void testArrayAccessUsingUnionWithFiniteTypes() {
        Object returns = BRunUtil.invoke(compileResult, "testArrayAccessUsingUnionWithFiniteTypes");
        Assert.assertSame(returns.getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test arrays return value")
    public void testArrayReturnValue() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compileResult, "arrayReturnTest", args);

        Assert.assertTrue(returns instanceof BArray);

        BArray arrayValue = (BArray) returns;
        Assert.assertEquals(arrayValue.size(), 106);

        Assert.assertEquals(arrayValue.getInt(0), 100);
        Assert.assertEquals(arrayValue.getInt(1), 5);
        Assert.assertEquals(arrayValue.getInt(105), 105);
    }

    @Test(description = "Test arrays arg value")
    public void testArrayArgValue() {
        BArray arrayValue = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_INT));
        arrayValue.add(0, 10);
        arrayValue.add(1, 1);

        Object[] args = {arrayValue};
        Object returns = BRunUtil.invoke(compileResult, "arrayArgTest", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 11;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test accessing an out of bound arrays-index",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*array index out of range: index: 5, size: 2.*")
    public void testArrayIndexOutOfBoundError() {
        BRunUtil.invoke(compileResult, "arrayIndexOutOfBoundTest");
    }

    @Test(description = "Test array index out of range with finite type",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*array index out of range: index: 3, size: 2.*")
    public void testArrayIndexOutOfRangeErrorWithFiniteTypeIndex() {
        BRunUtil.invoke(compileResult, "testArrayIndexOutOfRangeErrorWithFiniteTypeIndex");
    }

    @Test(description = "Test array index out of range with union with finite type",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*array index out of range: index: 4, size: 2.*")
    public void testArrayIndexOutOfRangeErrorWithUnionWithFiniteTypesIndex() {
        BRunUtil.invoke(compileResult, "testArrayIndexOutOfRangeErrorWithUnionWithFiniteTypesIndex");
    }

    @Test(description = "Test arrays access with a key")
    public void testArrayAccessWithKey() {
        CompileResult compileResult = BCompileUtil.compile("test-src/statements/arrays/incorrect-array-access.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected 'int', found 'string'", 4, 20);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected 'int', found 'IntOrString'",
                                  12, 25);
        BAssertUtil.validateError(compileResult, i++, "list index out of range: index: '-1'", 19, 7);
        BAssertUtil.validateError(compileResult, i++, "list index out of range: index: '-2'", 20, 7);
        BAssertUtil.validateError(compileResult, i++, "list index out of range: index: '-1'", 25, 15);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }

    @Test(description = "Test access a primitive as an arrays")
    public void testAccessPrimitiveAsArray() {
        CompileResult compileResult = BCompileUtil.compile("test-src/statements/arrays/access-primitive-as-array.bal");
        BAssertUtil.validateError(compileResult, 0, "invalid operation: type 'int' does not " +
                "support member access", 3, 5);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

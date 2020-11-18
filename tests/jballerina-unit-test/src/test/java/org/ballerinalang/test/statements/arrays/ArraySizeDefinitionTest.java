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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Array size definition test.
 *
 * @since Swan Lake
 */
public class ArraySizeDefinitionTest {

    private String sizeMismatchError = "size mismatch in sealed array. expected '2', but found '3'";
    private String invalidReferenceExpressionError = "invalid reference expression " +
            "'intLength' as array size: expected a constant reference expression";
    private String incompatibleTypeError = "incompatible types: expected 'int', found 'string'";
    private String undefinedSymbolError = "undefined symbol 'length'";
    private String invalidArrayLengthError = "invalid array length";

    @Test(groups = { "disableOnOldParser" })
    public void testCompilationSizeReferenceErrors() {
        CompileResult resultPositive = BCompileUtil.compile("test-src/statements/arrays/array_size_test.bal");
        Assert.assertEquals(resultPositive.getDiagnostics().length, 0);

        int index = 0;
        CompileResult resultNegative = BCompileUtil.compile("test-src/statements/arrays/array_size_test_" +
                "negative.bal");
        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 22, 26);
        BAssertUtil.validateError(resultNegative, index++, invalidReferenceExpressionError, 23, 9);
        BAssertUtil.validateError(resultNegative, index++, incompatibleTypeError, 24, 9);
        BAssertUtil.validateError(resultNegative, index++, undefinedSymbolError, 25, 5);
        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 26, 16);

        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 28, 36);
        BAssertUtil.validateError(resultNegative, index++, invalidReferenceExpressionError, 29, 12);
        BAssertUtil.validateError(resultNegative, index++, incompatibleTypeError, 30, 12);
        BAssertUtil.validateError(resultNegative, index++, undefinedSymbolError, 31, 5);
        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 32, 26);

        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 34, 29);
        BAssertUtil.validateError(resultNegative, index++, invalidReferenceExpressionError, 35, 9);
        BAssertUtil.validateError(resultNegative, index++, incompatibleTypeError, 36, 9);
        BAssertUtil.validateError(resultNegative, index++, undefinedSymbolError, 37, 5);
        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 38, 19);

        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 40, 33);
        BAssertUtil.validateError(resultNegative, index++, invalidReferenceExpressionError, 41, 12);
        BAssertUtil.validateError(resultNegative, index++, incompatibleTypeError, 42, 12);
        BAssertUtil.validateError(resultNegative, index++, undefinedSymbolError, 43, 5);
        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 44, 23);
        BAssertUtil.validateError(resultNegative, index++, invalidArrayLengthError, 45, 8);
        Assert.assertEquals(resultNegative.getDiagnostics().length, index);
    }

    @Test(groups = { "disableOnOldParser" })
    public void arraySizeReferenceInDifferentScopeTest() {
        CompileResult resultPositive = BCompileUtil.compile("test-src/statements/arrays/array_size_scope_test.bal");
        Assert.assertEquals(resultPositive.getDiagnostics().length, 0);

        int index = 0;
        CompileResult resultNegative = BCompileUtil.compile("test-src/statements/arrays/array_size_scope_test_" +
                "negative.bal");
        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 17, 24);
        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 23, 22);
        BAssertUtil.validateError(resultNegative, index++, invalidReferenceExpressionError, 24, 5);
        BAssertUtil.validateError(resultNegative, index++, incompatibleTypeError, 25, 5);
        BAssertUtil.validateError(resultNegative, index++, undefinedSymbolError, 26, 1);

        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 29, 26);
        BAssertUtil.validateError(resultNegative, index++, invalidReferenceExpressionError, 30, 9);
        BAssertUtil.validateError(resultNegative, index++, incompatibleTypeError, 31, 9);
        BAssertUtil.validateError(resultNegative, index++, undefinedSymbolError, 32, 5);

        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 36, 41);
        BAssertUtil.validateError(resultNegative, index++, invalidReferenceExpressionError, 37, 24);
        BAssertUtil.validateError(resultNegative, index++, incompatibleTypeError, 38, 24);
        BAssertUtil.validateError(resultNegative, index++, undefinedSymbolError, 39, 20);

        BAssertUtil.validateError(resultNegative, index++, sizeMismatchError, 47, 30);
        BAssertUtil.validateError(resultNegative, index++, invalidReferenceExpressionError, 48, 13);
        BAssertUtil.validateError(resultNegative, index++, incompatibleTypeError, 49, 13);
        BAssertUtil.validateError(resultNegative, index++, undefinedSymbolError, 50, 9);
        Assert.assertEquals(resultNegative.getDiagnostics().length, index);
    }
}

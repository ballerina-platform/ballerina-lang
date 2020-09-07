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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Array size definition test.
 *
 */
public class ArraySizeDefinitionTest {

    private String sizeMismatchError = "size mismatch in sealed array. expected '2', but found '3'";
    private String invalidReferenceExpressionError = "invalid reference expression 'intLength' as array size: expected a constant reference expression";
    private String incompatibleTypeError = "incompatible types: expected 'int', found 'string'";
    private String undefinedSymbolError = "undefined symbol 'length'";

    @Test(groups = { "disableOnOldParser" })
    public void testCompilationSizeReferenceErrors() {
        int index = 0;
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/arrays/array-size-test.bal");
        Assert.assertEquals(result.getDiagnostics().length, 20);
        BAssertUtil.validateError(result, index++, sizeMismatchError, 6, 26);
        BAssertUtil.validateError(result, index++, invalidReferenceExpressionError,7, 9);
        BAssertUtil.validateError(result, index++, incompatibleTypeError, 8, 9);
        BAssertUtil.validateError(result, index++, undefinedSymbolError, 9, 5);
        BAssertUtil.validateError(result, index++, sizeMismatchError, 10, 16);

        BAssertUtil.validateError(result, index++, sizeMismatchError, 12, 36);
        BAssertUtil.validateError(result, index++, invalidReferenceExpressionError,13, 12);
        BAssertUtil.validateError(result, index++, incompatibleTypeError, 14, 12);
        BAssertUtil.validateError(result, index++, undefinedSymbolError, 15, 5);
        BAssertUtil.validateError(result, index++, sizeMismatchError, 16, 26);

        BAssertUtil.validateError(result, index++, sizeMismatchError, 18, 29);
        BAssertUtil.validateError(result, index++, invalidReferenceExpressionError,19, 9);
        BAssertUtil.validateError(result, index++, incompatibleTypeError, 20, 9);
        BAssertUtil.validateError(result, index++, undefinedSymbolError, 21, 5);
        BAssertUtil.validateError(result, index++, sizeMismatchError, 22, 19);

        BAssertUtil.validateError(result, index++, sizeMismatchError, 24, 33);
        BAssertUtil.validateError(result, index++, invalidReferenceExpressionError,25, 12);
        BAssertUtil.validateError(result, index++, incompatibleTypeError, 26, 12);
        BAssertUtil.validateError(result, index++, undefinedSymbolError, 27, 5);
        BAssertUtil.validateError(result, index++, sizeMismatchError, 28, 23);
    }

    @Test(groups = { "disableOnOldParser" })
    public void arraySizeReferenceInDifferentScopeTest() {
        int index = 0;
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/arrays/array-size-scope-test.bal");
        Assert.assertEquals(result.getDiagnostics().length, 15);
//        BAssertUtil.validateError(result, index++, sizeMismatchError, 6, 26);
        BAssertUtil.validateError(result, index++, invalidReferenceExpressionError,7, 5);
        BAssertUtil.validateError(result, index++, incompatibleTypeError, 8, 5);
        BAssertUtil.validateError(result, index++, undefinedSymbolError, 9, 1);

        BAssertUtil.validateError(result, index++, sizeMismatchError, 13, 26);
        BAssertUtil.validateError(result, index++, invalidReferenceExpressionError,14, 9);
        BAssertUtil.validateError(result, index++, incompatibleTypeError, 15, 9);
        BAssertUtil.validateError(result, index++, undefinedSymbolError, 16, 5);

        BAssertUtil.validateError(result, index++, sizeMismatchError, 21, 41);
        BAssertUtil.validateError(result, index++, invalidReferenceExpressionError,22, 24);
        BAssertUtil.validateError(result, index++, incompatibleTypeError, 23, 24);
        BAssertUtil.validateError(result, index++, undefinedSymbolError, 24, 20);

        BAssertUtil.validateError(result, index++, sizeMismatchError, 33, 30);
        BAssertUtil.validateError(result, index++, invalidReferenceExpressionError,34, 13);
        BAssertUtil.validateError(result, index++, incompatibleTypeError, 35, 13);
        BAssertUtil.validateError(result, index++, undefinedSymbolError, 36, 9);
    }
}

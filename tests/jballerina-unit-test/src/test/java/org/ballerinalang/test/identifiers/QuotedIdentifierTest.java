// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.test.identifiers;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test Quoted identifiers.
 *
 * @since slalpha6
 */
public class QuotedIdentifierTest {

    @Test(dataProvider = "errorAsIdentifierFunctions")
    public void testErrorAsIdentifier(String function) {
        CompileResult errorTestCompileResult = BCompileUtil.compile("test-src/identifiers/error_as_identifier.bal");
        BRunUtil.invoke(errorTestCompileResult, function);
    }

    @DataProvider(name = "errorAsIdentifierFunctions")
    public Object[][] errorAsIdentifierFunctions() {
        return new Object[][] {
                { "testErrorNamedDefaultArgument" },
                { "testErrorAsObjectField" },
                { "testErrorDataWithErrorField" },
                { "testErrorConstructorWithErrorField" },
                { "testErrorNamedIncludedParam" },
                { "testErrorNamedRequiredParam" },
                { "testErrorNamedRestParam" },
        };
    }

    @Test(dataProvider = "errorAsIdentifierFunctions")
    public void testErrorAsIdentifierNegative(String function) {
        CompileResult errorTestCompileResult = BCompileUtil.compile("test-src/identifiers/error_as_identifier.bal");
        BRunUtil.invoke(errorTestCompileResult, function);
    }

    @Test(description = "Test error as a identifier negative cases")
    public void testInvalidImportOnMultipleFiles() {
        CompileResult result = BCompileUtil.compile("test-src/identifiers/error_as_identifier_negative.bal");
        int index = 0;
        validateError(result, index++, "redeclared symbol 'error'", 19, 18);
        validateError(result, index++, "redeclared symbol 'error'", 30, 11);
        validateError(result, index++, "redeclared symbol 'error'", 37, 73);
        validateError(result, index++, "redeclared symbol 'error'", 39, 72);
        validateError(result, index++, "redeclared symbol 'error'", 39, 96);
        validateError(result, index++, "redeclared symbol 'error'", 41, 94);
        validateError(result, index++, "redeclared symbol 'error'", 43, 93);
        validateError(result, index++, "redeclared symbol 'error'", 45, 82);
        validateError(result, index++, "redeclared symbol 'error'", 45, 106);
        assertEquals(result.getErrorCount(), index);
    }
}

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
package org.ballerinalang.test.expressions.mappingconstructor;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases for mapping constructor expressions.
 */
public class MappingConstructorExprTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
         result = BCompileUtil.compile("test-src/expressions/mappingconstructor/mapping_constructor.bal");
    }

    @Test
    public void diagnosticsTest() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/mapping_constructor_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        validateError(result, 0, "incompatible mapping constructor expression for type '(string|Person)'", 33, 23);
        validateError(result, 1, "ambiguous type '(PersonTwo|PersonThree)'", 37, 31);
        validateError(result, 2,
                      "a type compatible with mapping constructor expressions not found in type '(int|float)'", 41, 19);
    }

    @Test
    public void testVarNameFieldSemanticAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/var_name_field_semantic_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 6);
        validateError(result, 0, "incompatible types: expected 'string', found 'int'", 30, 14);
        validateError(result, 1, "incompatible types: expected 'string', found 'int'", 31, 22);
        validateError(result, 2, "undefined field 'b' in record 'Foo'", 37, 32);
        validateError(result, 3, "undefined symbol 'i'", 41, 26);
        validateError(result, 4, "undefined symbol 'c'", 42, 37);
        validateError(result, 5, "undefined symbol 'PI'", 46, 36);
    }

    @Test
    public void testVarNameFieldCodeAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/var_name_field_code_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        validateError(result, 0, "invalid usage of record literal: duplicate key 's'", 26, 17);
        validateError(result, 1, "invalid usage of record literal: duplicate key 'i'", 26, 23);
        validateError(result, 2, "invalid usage of map literal: duplicate key 'i'", 27, 34);
        validateError(result, 3, "invalid usage of map literal: duplicate key 'i'", 27, 42);
    }

    @Test
    public void testVarNameFieldTaintAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/mappingconstructor/var_name_field_taint_analysis_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        validateError(result, 0, "tainted value passed to global variable 'f'", 26, 5);
        validateError(result, 1, "tainted value passed to global variable 'm'", 27, 5);
    }

    @Test(dataProvider = "varNameFieldTests")
    public void testVarNameField(String test) {
        BRunUtil.invoke(result, test);
    }

    @DataProvider(name = "varNameFieldTests")
    public Object[][] varNameFieldTests() {
        return new Object[][] {
                { "testVarNameAsRecordField" },
                { "testVarNameAsMapField" },
                { "testVarNameAsJsonField" },
                { "testLikeModuleQualifiedVarNameAsJsonField" },
                { "testVarNameFieldInAnnotation" }, // final test using `s` since `s` is updated
                { "testMappingConstuctorWithAnyACET" },
                { "testMappingConstuctorWithAnydataACET" },
                { "testMappingConstuctorWithJsonACET" },
                { "testMappingConstrExprWithNoACET" }
        };
    }
}

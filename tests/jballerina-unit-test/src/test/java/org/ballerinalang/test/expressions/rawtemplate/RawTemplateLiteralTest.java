/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.rawtemplate;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for the raw template literals.
 */
public class RawTemplateLiteralTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/rawtemplate/raw_template_literal_test.bal");
    }

    @Test
    public void testNegatives() {
        CompileResult errors = BCompileUtil.compile(
                "test-src/expressions/rawtemplate/raw_template_literal_negative.bal");
        int indx = 0;

        // Attempts to assign raw templates to incompatible types
        validateError(errors, indx++, "invalid literal for type 'string': raw templates can only be assigned " +
                "to abstract subtypes of 'ballerina/lang.object:1.0.0:RawTemplate'", 21, 18);
        validateError(errors, indx++, "invalid literal for type 'anydata': raw templates can only be assigned " +
                "to abstract subtypes of 'ballerina/lang.object:1.0.0:RawTemplate'", 22, 19);
        validateError(errors, indx++, "invalid literal for type 'object { public (string[] & readonly) strings; " +
                "public int insertions; }': raw templates can only be assigned to abstract subtypes of " +
                "'ballerina/lang.object:1.0.0:RawTemplate'", 32, 15);
        validateError(errors, indx++, "invalid literal for type 'object { public string strings; public int[] " +
                "insertions; }': raw templates can only be assigned to abstract subtypes of " +
                "'ballerina/lang.object:1.0.0:RawTemplate'", 37, 15);

        validateError(errors, indx++, "invalid raw template: expected 2 insertion(s), but found " +
                "3 insertion(s)", 50, 19);
        validateError(errors, indx++, "invalid raw template: expected 2 insertion(s), but found " +
                "1 insertion(s)", 51, 19);
        validateError(errors, indx++, "incompatible types: expected 'anydata', found 'Template'", 56, 46);
        validateError(errors, indx++, "incompatible types: expected '(Foo|Bar)', found 'string'", 70, 16);
        validateError(errors, indx++, "invalid raw template assignment: 'Template1' should be an abstract object",
                      82, 19);
        validateError(errors, indx++, "invalid raw template assignment: 'object { public (string[] & readonly) " +
                "strings; public [anydata...] insertions; string name; }' should only have the 'strings' and " +
                "'insertions' fields", 94, 15);
        validateError(errors, indx++, "invalid raw template assignment: 'object { public (string[] & readonly) " +
                "strings; public int[] insertions; int name; }' should only have the 'strings' and " +
                "'insertions' fields", 102, 13);

        validateError(errors, indx++, "invalid literal for type 'object { }': raw templates can only be assigned " +
                "to abstract subtypes of 'ballerina/lang.object:1.0.0:RawTemplate'", 105, 13);
        validateError(errors, indx++, "invalid literal for type 'object { public (string[] & readonly) strings; }':" +
                " raw templates can only be assigned to abstract subtypes of 'ballerina/lang.object:1.0" +
                ".0:RawTemplate'", 109, 13);
        validateError(errors, indx++, "invalid literal for type 'object { public int[] insertions; }': raw templates " +
                "can only be assigned to abstract subtypes of 'ballerina/lang.object:1.0.0:RawTemplate'", 113, 13);
        validateError(errors, indx++, "invalid literal for type 'object { public int[] insertions; int foo; }': raw " +
                "templates can only be assigned to abstract subtypes of " +
                "'ballerina/lang.object:1.0.0:RawTemplate'", 118, 13);

        validateError(errors, indx++, "invalid raw template assignment: 'object { public (string[] & readonly) " +
                "strings; public int[] insertions; function shouldNotBeHere () returns (); }' should be a type " +
                "without methods", 125, 13);
        validateError(errors, indx++, "invalid raw template: expected 2 insertion(s), but found 3 insertion(s)",
                      134, 17);
        validateError(errors, indx++, "invalid raw template: expected 3 string(s), but found 4 string(s)", 134, 17);
        validateError(errors, indx++, "invalid raw template: expected 2 insertion(s), but found 1 insertion(s)",
                      135, 17);
        validateError(errors, indx++, "invalid raw template: expected 3 string(s), but found 2 string(s)", 135, 17);
        validateError(errors, indx++, "incompatible types: expected 'float', found 'string'", 136, 30);

        // Fixed length arrays
        validateError(errors, indx++, "invalid raw template: expected 1 insertion(s), but found 2 insertion(s)", 145,
                      15);
        validateError(errors, indx++, "invalid raw template: expected 2 string(s), but found 3 string(s)", 145, 15);
        validateError(errors, indx++, "invalid raw template: expected 1 insertion(s), but found 0 insertion(s)",
                      146, 9);
        validateError(errors, indx++, "invalid raw template: expected 2 string(s), but found 1 string(s)", 146, 9);
        validateError(errors, indx++, "invalid literal for type 'Temp3': raw templates can only be assigned to " +
                "abstract subtypes of 'ballerina/lang.object:1.0.0:RawTemplate'", 155, 15);

        validateError(errors, indx++, "ambiguous type for raw template: found multiple types compatible with " +
                "'ballerina/lang.object:1.0.0:RawTemplate' in '(ballerina/lang.object:1.0" +
                ".0:RawTemplate|Template1)'", 159, 35);

        assertEquals(errors.getErrorCount(), indx);
    }

    @Test
    public void testCodeAnalyzerNegatives() {
        CompileResult errors = BCompileUtil.compile(
                "test-src/expressions/rawtemplate/raw_template_negative_code_analyzer.bal");
        validateError(errors, 0, "action invocation as an expression not allowed here", 22, 34);
        assertEquals(errors.getErrorCount(), 1);
    }

    @Test(dataProvider = "FunctionNames", enabled = false)
    public void testRawTemplateLiteral(String func) {
        BRunUtil.invoke(result, func);
    }

    @DataProvider(name = "FunctionNames")
    public Object[][] getFunctions() {
        return new Object[][]{
                {"testBasicUsage"},
                {"testEmptyLiteral"},
                {"testLiteralWithNoInterpolations"},
                {"testLiteralWithNoStrings"},
                {"testComplexExpressions"},
                {"testSubtyping1"},
                {"testSubtyping2"},
                {"testSubtyping3"},
                {"testUsageWithQueryExpressions"},
                {"testUsageWithQueryExpressions2"},
                {"testUseWithVar"},
                {"testUseWithAny"},
                {"testFixedLengthArrayFields"},
                {"testAnyInUnion"},
                {"testAssignmentToUnion"},
                {"testIndirectAssignmentToConcreteType"},
                {"testModifyStringsField"}
        };
    }
}

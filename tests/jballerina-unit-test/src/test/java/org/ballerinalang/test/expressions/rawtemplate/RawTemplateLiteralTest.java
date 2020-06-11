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

        validateError(errors, indx++, "incompatible types: expected 'ballerina/lang.object:1.0.0:RawTemplate'" +
                ", found 'string'", 21, 18);
        validateError(errors, indx++, "incompatible types: expected 'ballerina/lang.object:1.0.0:RawTemplate'" +
                ", found 'anydata'", 22, 19);
        validateError(errors, indx++, "incompatible types: expected 'ballerina/lang.object:1.0.0:RawTemplate', found " +
                "'object { public string[] strings; public int insertions; }'", 32, 15);
        validateError(errors, indx++, "incompatible types: expected 'ballerina/lang.object:1.0.0:RawTemplate', found " +
                "'object { public string strings; public int[] insertions; }'", 37, 15);
        validateError(errors, indx++, "invalid raw template: expected 2 insertions, but found " +
                "3 insertion(s)", 50, 19);
        validateError(errors, indx++, "invalid raw template: expected 2 insertions, but found " +
                "1 insertion(s)", 51, 19);
        validateError(errors, indx++, "incompatible types: expected 'anydata', found 'Template'", 56, 46);
        validateError(errors, indx++, "incompatible types: expected '(Foo|Bar)', found 'string'", 70, 16);
        validateError(errors, indx++, "invalid raw template assignment: 'Template1' expected to be abstract", 82, 19);
        validateError(errors, indx++, "invalid raw template assignment: 'object { public string[] strings; public " +
                "[anydata...] insertions; string name; }' expected to be abstract", 94, 15);
        validateError(errors, indx++, "invalid raw template assignment: 'object { public string[] strings; public " +
                "int[] insertions; int name; }' expected to have only the 'strings' and 'insertions' fields", 102, 13);
        validateError(errors, indx++, "incompatible types: expected 'ballerina/lang.object:1.0.0:" +
                "RawTemplate', found 'object { }'", 105, 13);
        validateError(errors, indx++, "incompatible types: expected 'ballerina/lang.object:1.0.0:" +
                "RawTemplate', found 'object { public string[] strings; }'", 109, 13);
        validateError(errors, indx++, "incompatible types: expected 'ballerina/lang.object:1.0.0:" +
                "RawTemplate', found 'object { public int[] insertions; }'", 113, 13);
        validateError(errors, indx++, "incompatible types: expected 'ballerina/lang.object:1.0.0:" +
                "RawTemplate', found 'object { public int[] insertions; int foo; }'", 118, 13);
        validateError(errors, indx++, "invalid raw template assignment: 'object { public string[] strings; public " +
                "int[] insertions; function shouldNotBeHere () returns (); }' expected to be a type " +
                "without methods", 125, 13);

        assertEquals(errors.getErrorCount(), indx);
    }

    @Test
    public void testCodeAnalyzerNegatives() {
        CompileResult errors = BCompileUtil.compile(
                "test-src/expressions/rawtemplate/raw_template_negative_code_analyzer.bal");
        validateError(errors, 0, "action invocation as an expression not allowed here", 22, 40);
        assertEquals(errors.getErrorCount(), 1);
    }

    @Test(dataProvider = "FunctionNames")
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
                {"testUseWithVar"}
        };
    }
}

/*
 * Copyright (c) 2025, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.test.expressions.natural;

import io.ballerina.projects.BuildOptions;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Negative semantic test cases for natural expressions.
 *
 * @since 2201.13.0
 */
public class NaturalExpressionTest {

    @Test
    public void testNaturalExprSemanticAnalysisNegative() {
        CompileResult negativeRes = BCompileUtil.compileWithoutInitInvocation(
                "test-src/expressions/naturalexpr/natural_expr_semantic_analysis_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeRes, i++,
                "the expected type for a 'natural' expression must contain 'error'", 17, 9);
        BAssertUtil.validateError(negativeRes, i++,
                "the expected type for a 'natural' expression must be a subtype of 'anydata|error'", 26, 55);
        BAssertUtil.validateError(negativeRes, i++, "undefined symbol 'day'", 37, 15);
        BAssertUtil.validateError(negativeRes, i++, "incompatible types: expected 'anydata', found 'PersonObject'",
                37, 29);
        BAssertUtil.validateError(negativeRes, i++,
                "the expected type for a 'natural' expression must contain both a non-'error' type and 'error'",
                40, 32);
        BAssertUtil.validateError(negativeRes, i++,
                "an insertion in a 'const' 'natural' expression must be a constant expression", 46, 15);
        BAssertUtil.validateError(negativeRes, i++,
                "the expected type for a 'const' 'natural' expression must be a subtype of 'anydata'", 49, 11);
        BAssertUtil.validateError(negativeRes, i++,
                "the expected type for a 'const' 'natural' expression must be a subtype of 'anydata'", 55, 18);
        BAssertUtil.validateError(negativeRes, i++, "undefined symbol 'mdl'", 59, 48);
        Assert.assertEquals(negativeRes.getErrorCount(), i);
    }

    @Test
    public void testNaturalExprCodeAnalysisNegative() {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder.setExperimental(true);
        List<Diagnostic> negativeRes =
                BCompileUtil.loadProject("test-src/expressions/naturalexpr/natural_expr_code_analysis_negative.bal",
                                buildOptionsBuilder.build())
                        .currentPackage().getCompilation().defaultModuleBLangPackage().getDiagnostics();

        int i = 0;
        validateError(negativeRes, i++, "variable 'mdl1' is not initialized", 19, 34);
        validateError(negativeRes, i++, "variable 'day2' is not initialized", 28, 13);
        validateError(negativeRes, i++, "invalid access of mutable storage in an 'isolated' function",
                47, 17);
        validateError(negativeRes, i++, "invalid access of mutable storage in an 'isolated' function",
                50, 17);
        validateError(negativeRes, i++, "invalid access of mutable storage in an 'isolated' function",
                58, 17);
        validateError(negativeRes, i++, "invalid key 'name': identifiers cannot be used as rest " +
                "field keys, expected a string literal or an expression", 70, 9);
        validateError(negativeRes, i++, "invalid key 'interests': identifiers cannot be used as rest " +
                "field keys, expected a string literal or an expression", 77, 17);
        Assert.assertEquals(negativeRes.size(), i);
    }

    private static void validateError(List<Diagnostic> diagnostics, int errorIndex, String expectedErrMsg,
                                      int expectedErrLine, int expectedErrCol) {
        Diagnostic diag = diagnostics.get(errorIndex);
        Assert.assertEquals(diag.diagnosticInfo().severity(), DiagnosticSeverity.ERROR, "incorrect diagnostic type");
        Assert.assertEquals(diag.message(), expectedErrMsg, "incorrect error message:");
        Assert.assertEquals(diag.location().lineRange().startLine().line() + 1, expectedErrLine,
                "incorrect line number:");
        Assert.assertEquals(diag.location().lineRange().startLine().offset() + 1, expectedErrCol,
                "incorrect column position:");
    }
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.tool.util;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;

/**
 * Utility methods for do result validations.
 *
 * @since 0.94
 */
public class BAssertUtil {

    private static final String CARRIAGE_RETURN_CHAR = "\\r";
    private static final String EMPTY_STRING = "";

    /**
     * Assert an error.
     *
     * @param result          Result from compilation
     * @param errorIndex      Index of the error in the result
     * @param expectedErrMsg  Expected error message
     * @param expectedErrLine Expected line number of the error
     * @param expectedErrCol  Expected column number of the error
     */
    public static void validateError(CompileResult result, int errorIndex, String expectedErrMsg, int expectedErrLine,
            int expectedErrCol) {
        Diagnostic diag = result.getDiagnostics()[errorIndex];
        Assert.assertEquals(diag.diagnosticInfo().severity(), DiagnosticSeverity.ERROR, "incorrect diagnostic type");
        Assert.assertEquals(diag.message().replace(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                            expectedErrMsg.replace(CARRIAGE_RETURN_CHAR, EMPTY_STRING), "incorrect error message:");
        Assert.assertEquals(diag.location().lineRange().startLine().line() + 1, expectedErrLine,
                            "incorrect line number:");
        Assert.assertEquals(diag.location().lineRange().startLine().offset() + 1, expectedErrCol,
                            "incorrect column position:");
    }

    /**
     * Assert an error (without error message).
     *
     * @param result          Result from compilation
     * @param errorIndex      Index of the error in the result
     * @param expectedErrLine Expected line number of the error
     * @param expectedErrCol  Expected column number of the error
     */
    public static void validateError(CompileResult result, int errorIndex, int expectedErrLine, int expectedErrCol) {
        Diagnostic diag = result.getDiagnostics()[errorIndex];
        Assert.assertEquals(diag.diagnosticInfo().severity(), DiagnosticSeverity.ERROR, "incorrect diagnostic type");
        Assert.assertEquals(diag.location().lineRange().startLine().line() + 1, expectedErrLine,
                            "incorrect line number:");
        Assert.assertEquals(diag.location().lineRange().startLine().offset() + 1, expectedErrCol,
                            "incorrect column position:");
    }

    /**
     * Validate if given text is contained in the error message.
     *
     * @param result               Result from compilation
     * @param errorIndex           Index of the error in the result
     * @param expectedPartOfErrMsg Expected part of error message
     */
    public static void validateErrorMessageOnly(CompileResult result, int errorIndex, String expectedPartOfErrMsg) {
        Diagnostic diag = result.getDiagnostics()[errorIndex];
        Assert.assertEquals(diag.diagnosticInfo().severity(), DiagnosticSeverity.ERROR, "incorrect diagnostic type");
        Assert.assertTrue(diag.message().contains(expectedPartOfErrMsg),
                '\'' + expectedPartOfErrMsg + "' is not contained in error message '" + diag.message() + '\'');
    }

    /**
     * Validate if at least one in the given list of texts is contained in the error message.
     *
     * @param result                Result from compilation
     * @param errorIndex            Index of the error in the result
     * @param expectedPartsOfErrMsg Array of expected parts of error message
     */
    public static void validateErrorMessageOnly(CompileResult result, int errorIndex, String[] expectedPartsOfErrMsg) {
        Diagnostic diag = result.getDiagnostics()[errorIndex];
        Assert.assertEquals(diag.diagnosticInfo().severity(), DiagnosticSeverity.ERROR, "incorrect diagnostic type");
        boolean contains = false;
        for (String part : expectedPartsOfErrMsg) {
            if (diag.message().contains(part)) {
                contains = true;
                break;
            }
        }
        Assert.assertTrue(contains,
                "None of given strings is contained in the error message '" + diag.message() + '\'');
    }

    /**
     * Assert a warning.
     *
     * @param result           Result from compilation
     * @param warningIndex     Index of the warning in the result
     * @param expectedWarnMsg  Expected warning message
     * @param expectedWarnLine Expected line number of the warning
     * @param expectedWarnCol  Expected column number of the warning
     */
    public static void validateWarning(CompileResult result, int warningIndex, String expectedWarnMsg,
            int expectedWarnLine, int expectedWarnCol) {
        Diagnostic diag = result.getDiagnostics()[warningIndex];
        Assert.assertEquals(diag.diagnosticInfo().severity(), DiagnosticSeverity.WARNING, "incorrect diagnostic type");
        Assert.assertEquals(diag.message(), expectedWarnMsg, "incorrect warning message:");
        Assert.assertEquals(diag.location().lineRange().startLine().line() + 1, expectedWarnLine,
                            "incorrect line number:");
        Assert.assertEquals(diag.location().lineRange().startLine().offset() + 1, expectedWarnCol,
                            "incorrect column position:");
    }
}

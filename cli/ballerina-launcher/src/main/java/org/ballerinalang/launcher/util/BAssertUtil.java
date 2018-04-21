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
package org.ballerinalang.launcher.util;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;

/**
 * Utility methods for do result validations.
 *
 * @since 0.94
 */
public class BAssertUtil {
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
        Assert.assertEquals(diag.getKind(), Diagnostic.Kind.ERROR, "incorrect diagnostic type");
        Assert.assertEquals(diag.getMessage(), expectedErrMsg, "incorrect error message:");
        Assert.assertEquals(diag.getPosition().getStartLine(), expectedErrLine, "incorrect line number:");
        Assert.assertEquals(diag.getPosition().getStartColumn(), expectedErrCol, "incorrect column position:");
    }

    /**
     * Assert an error (without error message).
     *
     * @param result          Result from compilation
     * @param errorIndex      Index of the error in the result
     * @param expectedErrLine Expected line number of the error
     * @param expectedErrCol  Expected column number of the error
     */
    public static void validateError(CompileResult result, int errorIndex, int expectedErrLine,
                                     int expectedErrCol) {
        Diagnostic diag = result.getDiagnostics()[errorIndex];
        Assert.assertEquals(diag.getKind(), Diagnostic.Kind.ERROR, "incorrect diagnostic type");
        Assert.assertEquals(diag.getPosition().getStartLine(), expectedErrLine, "incorrect line number:");
        Assert.assertEquals(diag.getPosition().getStartColumn(), expectedErrCol, "incorrect column position:");
    }

    /**
     * Validate if given text is contained in the error message.
     *
     * @param result          Result from compilation
     * @param errorIndex      Index of the error in the result
     * @param expectedPartOfErrMsg  Expected part of error message
     */
    public static void validateErrorMessageOnly(CompileResult result, int errorIndex, String expectedPartOfErrMsg) {
        Diagnostic diag = result.getDiagnostics()[errorIndex];
        Assert.assertEquals(diag.getKind(), Diagnostic.Kind.ERROR, "incorrect diagnostic type");
        Assert.assertTrue(diag.getMessage().contains(expectedPartOfErrMsg), '\'' + expectedPartOfErrMsg
                + "' is not contained in error message '" + diag.getMessage() + '\'');
    }

    /**
     * Validate if at least one in the given list of texts is contained in the error message.
     *
     * @param result          Result from compilation
     * @param errorIndex      Index of the error in the result
     * @param expectedPartsOfErrMsg  Array of expected parts of error message
     */
    public static void validateErrorMessageOnly(CompileResult result, int errorIndex, String[] expectedPartsOfErrMsg) {
        Diagnostic diag = result.getDiagnostics()[errorIndex];
        Assert.assertEquals(diag.getKind(), Diagnostic.Kind.ERROR, "incorrect diagnostic type");
        boolean contains = false;
        for (String part : expectedPartsOfErrMsg) {
            if (diag.getMessage().contains(part)) {
                contains = true;
                break;
            }
        }
        Assert.assertTrue(contains, "None of given strings is contained in the error message '"
                + diag.getMessage() + '\'');
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
        Assert.assertEquals(diag.getKind(), Diagnostic.Kind.WARNING, "incorrect diagnostic type");
        Assert.assertEquals(diag.getMessage(), expectedWarnMsg, "incorrect warning message:");
        Assert.assertEquals(diag.getPosition().getStartLine(), expectedWarnLine, "incorrect line number:");
        Assert.assertEquals(diag.getPosition().getStartColumn(), expectedWarnCol, "incorrect column position:");
    }
}

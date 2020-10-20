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

package toml.parser.test.errors;

import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.testng.Assert;

import java.io.PrintStream;
import java.util.List;

/**
 * Contains Test Utils used to test the TOML parser.
 */
public class ErrorTestUtils {
    private static final PrintStream OUT = System.out;

    public static void validateDiagnostic(TomlDiagnostic actual, LineRange expectedLineRange, String expectedMessage,
                                          DiagnosticSeverity diagnosticSeverity) {
        Assert.assertEquals(actual.location().lineRange(), expectedLineRange);
        Assert.assertEquals(actual.message(), expectedMessage);
        Assert.assertEquals(actual.diagnosticInfo().severity(), diagnosticSeverity);
    }

    public static LineRange toLineRange(int startLine, int endLine, int startCol, int endCol) {
        LinePosition startPos = LinePosition.from(startLine - 1, startCol - 1);
        LinePosition endPos = LinePosition.from(endLine - 1, endCol - 1);
        return LineRange.from(null, startPos, endPos);
    }

    private void printDiagnostics(List<TomlDiagnostic> diagnostics) {
        for (TomlDiagnostic diagnostic : diagnostics) {
            String message = diagnostic.message();
            OUT.println(diagnostic.diagnosticInfo().severity());
            OUT.println(message);
            OUT.println(diagnostic.location());
        }
    }
}

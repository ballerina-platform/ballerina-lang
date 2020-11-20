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

package toml.parser.test.api.errors;

import io.ballerina.toml.api.Toml;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.LineRange;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Test cases for Table Syntax errors in TOML.
 */
public class TableTest {

    @Test
    public void testMissingTableKey() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/tables/empty-table-key-negative.toml");
        Toml read = Toml.read(inputStream);
        List<Diagnostic> diagnostics = read.diagnostics();

        LineRange expectedLineRange = ErrorTestUtils.toLineRange(17, 17, 2, 2);
        Diagnostic actualDiag = diagnostics.get(0);

        ErrorTestUtils.validateDiagnostic(actualDiag, expectedLineRange, "missing identifier",
                DiagnosticSeverity.ERROR);
    }

    @Test
    public void testMissingTableClose() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/tables/empty-table-close-negative.toml");
        Toml read = Toml.read(inputStream);
        List<Diagnostic> diagnostics = read.diagnostics();

        LineRange expectedLineRange = ErrorTestUtils.toLineRange(18, 18, 1, 1);
        Diagnostic actualDiag = diagnostics.get(0);

        ErrorTestUtils.validateDiagnostic(actualDiag, expectedLineRange, "missing close bracket token",
                DiagnosticSeverity.ERROR);
    }

    @Test
    public void testWrongCloseBraceTableArray() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/tables/wrong-closing-brace-negative.toml");
        Toml read = Toml.read(inputStream);
        List<Diagnostic> diagnostics = read.diagnostics();

        LineRange expectedLineRange = ErrorTestUtils.toLineRange(18, 18, 1, 1);
        Diagnostic actualDiag = diagnostics.get(0);

        ErrorTestUtils.validateDiagnostic(actualDiag, expectedLineRange, "missing close bracket token",
                DiagnosticSeverity.ERROR);
    }
}

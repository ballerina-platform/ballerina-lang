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

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.LineRange;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Test cases for Key Value pair Syntax errors in TOML.
 */
public class KeyValuePairTest {

    @Test
    public void testMissingEquals() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("validations/syntax/missing-equal.toml");
        Toml read = Toml.read(inputStream);
        List<TomlDiagnostic> diagnostics = read.getDiagnostics();
        LineRange expectedLineRange = ErrorTestUtils.toLineRange(17, 17, 4, 4);
        TomlDiagnostic actualDiag = diagnostics.get(0);

        ErrorTestUtils
                .validateDiagnostic(actualDiag, expectedLineRange, "missing equal token", DiagnosticSeverity.ERROR);
    }

    @Test
    public void testMissingKey() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("validations/syntax/missing-key.toml");
        Toml read = Toml.read(inputStream);
        List<TomlDiagnostic> diagnostics = read.getDiagnostics();

        LineRange expectedLineRange = ErrorTestUtils.toLineRange(1, 1, 1, 1);
        TomlDiagnostic actualDiag = diagnostics.get(0);

        ErrorTestUtils.validateDiagnostic(actualDiag, expectedLineRange, "error missing key", DiagnosticSeverity.ERROR);
    }

    @Test
    public void testMissingValue() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("validations/syntax/missing-value.toml");
        Toml read = Toml.read(inputStream);
        List<TomlDiagnostic> diagnostics = read.getDiagnostics();

        LineRange expectedLineRange = ErrorTestUtils.toLineRange(18, 18, 1, 1);
        TomlDiagnostic actualDiag = diagnostics.get(0);

        ErrorTestUtils
                .validateDiagnostic(actualDiag, expectedLineRange, "error missing value", DiagnosticSeverity.ERROR);
    }

    @Test
    public void testMultipleMissing() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("validations/syntax/key-value-multi.toml");
        Toml read = Toml.read(inputStream);
        List<TomlDiagnostic> diagnostics = read.getDiagnostics();

        LineRange expectedLineRange = ErrorTestUtils.toLineRange(17, 17, 4, 4);
        TomlDiagnostic actualDiag = diagnostics.get(0);

        ErrorTestUtils
                .validateDiagnostic(actualDiag, expectedLineRange, "missing equal token", DiagnosticSeverity.ERROR);

        LineRange expectedLineRange1 = ErrorTestUtils.toLineRange(20, 20, 1, 1);
        TomlDiagnostic actualDiag1 = diagnostics.get(1);

        ErrorTestUtils
                .validateDiagnostic(actualDiag1, expectedLineRange1, "error missing key", DiagnosticSeverity.ERROR);

        LineRange expectedLineRange2 = ErrorTestUtils.toLineRange(26, 26, 1, 1);
        TomlDiagnostic actualDiag2 = diagnostics.get(2);

        ErrorTestUtils
                .validateDiagnostic(actualDiag2, expectedLineRange2, "error missing value", DiagnosticSeverity.ERROR);
    }

    @Test
    public void testArrayMissingComma() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("validations/syntax/array-missing-comma.toml");
        Toml read = Toml.read(inputStream);
        List<TomlDiagnostic> diagnostics = read.getDiagnostics();

        LineRange expectedLineRange = ErrorTestUtils.toLineRange(17, 17, 10, 11);
        TomlDiagnostic actualDiag = diagnostics.get(0);

        ErrorTestUtils
                .validateDiagnostic(actualDiag, expectedLineRange, "invalid token '2'", DiagnosticSeverity.ERROR);
    }

    @Test
    public void testArrayMissingValue() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("validations/syntax/array-missing-value.toml");
        Toml read = Toml.read(inputStream);
        List<TomlDiagnostic> diagnostics = read.getDiagnostics();

        LineRange expectedLineRange = ErrorTestUtils.toLineRange(17, 17, 8, 9);
        TomlDiagnostic actualDiag = diagnostics.get(0);

        ErrorTestUtils.validateDiagnostic(actualDiag, expectedLineRange,  "invalid token ','",
                DiagnosticSeverity.ERROR);
    }
}

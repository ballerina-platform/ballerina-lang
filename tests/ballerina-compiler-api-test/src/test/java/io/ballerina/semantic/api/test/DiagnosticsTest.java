/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Test cases for the APIs for getting the diagnostics of the semantic model.
 *
 * @since 2.0.0
 */
public class DiagnosticsTest {

    private final Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

    @Test
    public void testAllDiagnostics() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/test-project/", "bar", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        List<Diagnostic> diagnostics = model.diagnostics();
        Object[][] expErrs = getExpectedErrors();

        for (int i = 0; i < expErrs.length; i++) {
            assertDiagnostic(diagnostics.get(i), expErrs[i]);
        }
    }

    @Test
    public void testDiagnosticsInARange() {
        CompilerContext context = new CompilerContext();
        CompileResult result = compile("test-src/test-project/", "bar", context);
        BLangPackage pkg = (BLangPackage) result.getAST();
        BallerinaSemanticModel model = new BallerinaSemanticModel(pkg, context);

        LineRange range = LineRange.from("type_checking_errors.bal", LinePosition.from(1, 0),
                                         LinePosition.from(18, 19));
        List<Diagnostic> diagnostics = model.diagnostics(range);

        assertEquals(diagnostics.size(), 1);
        assertDiagnostic(diagnostics.get(0), getExpectedErrors()[3]);
    }

    // Utils

    private Object[][] getExpectedErrors() {
        return new Object[][]{
                {"missing semicolon token", 18, 0},
                {"missing identifier", 21, 8},
                {"invalid token 'string'", 21, 15},
                {"incompatible types: expected 'int', found 'string'", 17, 12},
                {"incompatible types: 'int' cannot be cast to 'string'", 19, 15}
        };
    }

    private void assertDiagnostic(Diagnostic diagnostic, Object[] expected) {
        assertEquals(diagnostic.message(), expected[0]);
        assertEquals(diagnostic.location().lineRange().startLine().line(), expected[1]);
        assertEquals(diagnostic.location().lineRange().startLine().offset(), expected[2]);
    }

    private CompileResult compile(String sourceRoot, String module, CompilerContext context) {
        String sourceRootAbsolute = BCompileUtil.concatFileName(sourceRoot, resourceDir.toAbsolutePath());
        return BCompileUtil.compileOnJBallerina(context, sourceRootAbsolute, module, false, true, false);
    }
}

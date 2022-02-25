/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.linter.codeactions.test;

import io.ballerina.compiler.linter.impl.codeactions.Constants;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Test Cases for Linter Code Action.
 *
 * @since 2.0.0
 */
public class CodeActionTests {


    public static final String SLASH = "/";

    @DataProvider
    public Object[] checkUsageDP() {
        return new Object[][]{
                {"source/check1.bal", LinePosition.from(1, 19)},
                {"source/check2.bal", LinePosition.from(1, 24)}
        };
    }

    @Test(dataProvider = "checkUsageDP")
    void testCheckUsageCodeAction(final String file, final LinePosition cursorPos) throws IOException {

        final String expectedProvider = Constants.BCE_INVALID_CHECK + SLASH + Constants.CA_CHECK_USAGE;
        testSingleDiagnosticCodeAction(file, cursorPos, expectedProvider);
    }

    @DataProvider
    public Object[] checkQNameDP() {
        return new Object[][]{
                {"source/qname1.bal", LinePosition.from(2, 5), Constants.BCE_INTERVENING_WS},
                {"source/qname2.bal", LinePosition.from(2, 5), Constants.BCE_INTERVENING_WS},
                {"source/qname3.bal", LinePosition.from(2, 9), Constants.BCE_INTERVENING_WS},
                {"source/qname4.bal", LinePosition.from(3, 4), Constants.BCE_INVALID_WS}
        };
    }

    @Test(dataProvider = "checkQNameDP")
    void testQNameCodeAction(final String file, final LinePosition cursorPos,
                             final String diagnosticCode) throws IOException {

        final String expectedProvider = diagnosticCode + SLASH + Constants.CA_QUALIFIED_IDENTIFIER;
        testSingleDiagnosticCodeAction(file, cursorPos, expectedProvider);
    }


    @DataProvider
    public Object[] floatingNumberDP() {
        return new Object[][]{
                {"source/floatingNumber1.bal", LinePosition.from(1, 18), Constants.BCE_MISSING_DIGIT_AFTER_DOT},
                {"source/floatingNumber2.bal", LinePosition.from(1, 18), Constants.BCE_MISSING_DIGIT_AFTER_DOT},
                {"source/floatingNumber3.bal", LinePosition.from(1, 18), Constants.BCE_MISSING_DIGIT_AFTER_DOT},
                {"source/floatingNumber4.bal", LinePosition.from(1, 18), Constants.BCE_MISSING_DIGIT_AFTER_DOT},
                {"source/floatingNumber5.bal", LinePosition.from(1, 18), Constants.BCE_MISSING_HEX_DIGIT_AFTER_DOT},
                {"source/floatingNumber6.bal", LinePosition.from(1, 18), Constants.BCE_MISSING_HEX_DIGIT_AFTER_DOT}
        };
    }

    @Test(dataProvider = "floatingNumberDP")
    public void testFloatingNumbersCodeAction(final String file, final LinePosition cursorPos,
                                              final String diagnosticCode) throws IOException {

        final String expectedProvider = diagnosticCode + SLASH + Constants.CA_FLOATING_POINT;
        testSingleDiagnosticCodeAction(file, cursorPos, expectedProvider);
    }

    private void testSingleDiagnosticCodeAction(final String file, final LinePosition cursorPos,
                                                final String expectedProvider) throws IOException {

        final Path filePath = CodeActionUtils.getProjectPath(file);
        final Project project = BCompileUtil.loadProject(file);

        final List<CodeActionInfo> codeActions = CodeActionUtils.getCodeActions(project, filePath, cursorPos);
        Assert.assertTrue(codeActions.size() > 0, "Expect at least 1 code action");

        final Optional<CodeActionInfo> found = codeActions.stream()
                .filter(info -> expectedProvider.equals(info.getProviderName()))
                .findFirst();
        Assert.assertTrue(found.isPresent(), "Code Action not found:" + expectedProvider);

        final List<DocumentEdit> actualEdits = CodeActionUtils.executeCodeAction(project, filePath, found.get());
        Assert.assertEquals(actualEdits.size(), 1, "Expected changes to n files");

        final Optional<DocumentEdit> actualEdit = actualEdits.stream()
                .filter(docEdit -> docEdit.getFileUri().equals(filePath.toUri().toString()))
                .findFirst();
        Assert.assertTrue(actualEdit.isPresent(), "Expected changes to a file");

        final String modifiedSourceCode = actualEdit.get().getModifiedSyntaxTree().toSourceCode();
        final String expectedSourceCode = CodeActionUtils.getExpectedSourceCode(file);
        Assert.assertEquals(modifiedSourceCode, expectedSourceCode,
                "Actual source code didn't match expected source code");
    }

}

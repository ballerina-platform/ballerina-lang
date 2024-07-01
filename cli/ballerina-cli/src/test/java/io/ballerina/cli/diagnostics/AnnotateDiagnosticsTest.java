/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.diagnostics;

import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.Package;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.cli.utils.OsUtils.isWindows;

/**
 * Test cases for AnnotateDiagnostics class.
 *
 * @since 2201.10.0
 */
public class AnnotateDiagnosticsTest {

    private Path testResources;

    @BeforeClass
    public void setup() throws URISyntaxException {
        URI testResourcesURI = Objects.requireNonNull(
                getClass().getClassLoader().getResource("test-resources")).toURI();
        this.testResources = Paths.get(testResourcesURI).resolve("diagnostics-test-files");
    }

    @Test(description = "Test annotations when the source file contains missing tokens")
    public void testMissingTokenAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-missing-error/semicolon-missing.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        String expectedOutput = Files.readString(testResources.resolve("bal-missing-error")
                .resolve("semicolon-missing-expected.txt"));
        Assert.assertTrue(diagnostics.length > 0);
        Diagnostic diagnostic = diagnostics[0];
        AnnotateDiagnostics annotateDiagnostics = new AnnotateDiagnostics(result.project().currentPackage());
        String output = annotateDiagnostics.renderDiagnostic(diagnostic).toString();
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains a missing function keyword")
    public void testMissingFunctionKeywordAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-missing-error/missing-function-keyword.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        String expectedOutput = Files.readString(testResources.resolve("bal-missing-error")
                .resolve("missing-function-keyword-expected.txt"));
        Assert.assertEquals(diagnostics.length, 1);
        Diagnostic diagnostic = diagnostics[0];
        AnnotateDiagnostics annotateDiagnostics = new AnnotateDiagnostics(result.project().currentPackage());
        String output = annotateDiagnostics.renderDiagnostic(diagnostic).toString();
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains a missing keywords")
    public void testMissingMultipleKeywordsAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-missing-error/missing-multiple-keywords.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        String expectedOutput = Files.readString(testResources.resolve("bal-missing-error")
                .resolve("missing-multiple-keywords-expected.txt"));
        Assert.assertTrue(diagnostics.length > 0);
        String output = getAnnotatedDiagnostics(diagnostics, result.project().currentPackage());
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains invalid tokens")
    void testInvalidTokenAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-invalid-error/invalid-token.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        String expectedOutput = Files.readString(testResources.resolve("bal-invalid-error")
                .resolve("invalid-token-expected.txt"));
        Assert.assertEquals(diagnostics.length, 1);
        Diagnostic diagnostic = diagnostics[0];
        AnnotateDiagnostics annotateDiagnostics = new AnnotateDiagnostics(result.project().currentPackage());
        String output = annotateDiagnostics.renderDiagnostic(diagnostic).toString();
        Assert.assertEquals(output, expectedOutput);
    }

    @DataProvider(name = "terminalWidthProvider")
    public Object[][] terminalWidthProvider() {
        return new Object[][]{
                {999},
                {200},
                {150},
                {100},
                {50},
                {40},
                {38}
        };
    }

    @Test(description = "Test annotations when erroneous line is longer than the terminal width. Tests truncation",
            dataProvider = "terminalWidthProvider")
    void testLongLineAnnotation(int terminalWidth) throws IOException, NoSuchFieldException, IllegalAccessException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-error/long-line.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        AnnotateDiagnostics annotateDiagnostics = new AnnotateDiagnostics(result.project().currentPackage());
        Assert.assertEquals(diagnostics.length, 1);
        Diagnostic diagnostic = diagnostics[0];
        Field terminalWidthField = AnnotateDiagnostics.class.getDeclaredField("terminalWidth");
        terminalWidthField.setAccessible(true);
        terminalWidthField.setInt(annotateDiagnostics, terminalWidth);
        String output = annotateDiagnostics.renderDiagnostic(diagnostic).toString();
        String expectedOutput =
                Files.readString(
                        testResources.resolve("bal-error").resolve("long-line-expected" + terminalWidth + ".txt"));
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when a ballerina project contains errors in multiple files")
    void testProjectErrorAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-project-error/project1");
        Diagnostic[] diagnostics = result.getDiagnostics();
        String output = getAnnotatedDiagnostics(diagnostics, result.project().currentPackage());
        String expectedOutput = getExpectedOutput(testResources.resolve("bal-project-error"), "project1-expected.txt");
        Assert.assertEquals(output, expectedOutput);

    }

    @Test(description = "Test warning annotations in a ballerina project")
    void testProjectWarningAnnotation() throws IOException {
        CompileResult result =
                BCompileUtil.compileWithoutInitInvocation(
                        "test-resources/diagnostics-test-files/bal-project-error/project2");
        Diagnostic[] diagnostics = result.getDiagnostics();
        AnnotateDiagnostics annotateDiagnostics = new AnnotateDiagnostics(result.project().currentPackage());
        StringBuilder output = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.diagnosticInfo().severity() != DiagnosticSeverity.WARNING) {
                continue;
            }
            output.append(annotateDiagnostics.renderDiagnostic(diagnostic).toString());
            output.append(System.lineSeparator());
        }
        String expectedOutput =
                getExpectedOutput(testResources.resolve("bal-project-error"), "project2-expected-warnings.txt");
        Assert.assertEquals(output.toString(), expectedOutput);
    }

    @Test(description = "Test hint annotations in a ballerina project")
    void testProjectHintAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileWithoutInitInvocation(
                "test-resources/diagnostics-test-files/bal-project-error/project2");
        Diagnostic[] diagnostics = result.getDiagnostics();
        AnnotateDiagnostics annotateDiagnostics = new AnnotateDiagnostics(result.project().currentPackage());
        StringBuilder output = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.diagnosticInfo().severity() != DiagnosticSeverity.HINT) {
                continue;
            }
            output.append(annotateDiagnostics.renderDiagnostic(diagnostic).toString());
            output.append(System.lineSeparator());
        }
        String expectedOutput =
                getExpectedOutput(testResources.resolve("bal-project-error"), "project2-expected-hints.txt");
        Assert.assertEquals(output.toString(), expectedOutput);
    }

    @Test(description = "Test annotations spanning two lines in the source file")
    void testTwoLinedAnnotations() throws IOException {
        CompileResult result =
                BCompileUtil.compile("test-resources/diagnostics-test-files/bal-error/two-line-error.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        String output = getAnnotatedDiagnostics(diagnostics, result.project().currentPackage());
        String expectedOutput =
                Files.readString(testResources.resolve("bal-error").resolve("two-line-error-expected.txt"));
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains diagnostics spanning multiple lines")
    void testMultiLinedAnnotations() throws IOException {
        CompileResult result =
                BCompileUtil.compile("test-resources/diagnostics-test-files/bal-error/multi-line.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        String output = getAnnotatedDiagnostics(diagnostics, result.project().currentPackage());
        String expectedOutput = Files.readString(testResources.resolve("bal-error").resolve("multi-line-expected.txt"));
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains tabs instead of spaces")
    void testAnnotationsWithTabs() throws IOException {
        CompileResult result = BCompileUtil.compile("test-resources/diagnostics-test-files/bal-error/tabs.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        String output = getAnnotatedDiagnostics(diagnostics, result.project().currentPackage());
        String expectedOutput = Files.readString(testResources.resolve("bal-error").resolve("tabs-expected.txt"));
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test non terminal node missing annotations")
    void testNonTerminalNodeMissingAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compile(
                "test-resources/diagnostics-test-files/bal-missing-error/missing-non-terminal.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        String expectedOutput = Files.readString(testResources.resolve("bal-missing-error")
                .resolve("missing-non-terminal-expected.txt"));
        Assert.assertEquals(diagnostics.length, 8);
        String output = getAnnotatedDiagnostics(diagnostics, result.project().currentPackage());
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test missing token annotation padding")
    void testMissingTokenAnnotationPadding() throws IOException, NoSuchFieldException, IllegalAccessException {
        CompileResult result = BCompileUtil.compile(
                "test-resources/diagnostics-test-files/bal-missing-error/missing-function-keyword.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Assert.assertEquals(diagnostics.length, 1);

        AnnotateDiagnostics annotateDiagnostics = new AnnotateDiagnostics(result.project().currentPackage());
        Field documentMapField = AnnotateDiagnostics.class.getDeclaredField("documentMap");
        documentMapField.setAccessible(true);
        Assert.assertTrue(documentMapField.get(annotateDiagnostics) instanceof Map<?, ?>);
        Object value = documentMapField.get(annotateDiagnostics);
        Assert.assertTrue(value instanceof Map);
        Map<String, Document> documentMap = (Map<String, Document>) value;

        Document document = documentMap.get(diagnostics[0].location().lineRange().fileName());
        int index = diagnostics[0].location().textRange().startOffset();

        String documentString = " " + document.textDocument().toString().substring(0, index - 1) +
                document.textDocument().toString().substring(index);
        DocumentConfig documentConfig = DocumentConfig.from(document.documentId(), documentString, document.name());
        Document updatedDocument = Document.from(documentConfig, document.module());
        documentMap.put(diagnostics[0].location().lineRange().fileName(), updatedDocument);

        String output = annotateDiagnostics.renderDiagnostic(diagnostics[0]).toString();
        String expectedOutput = Files.readString(testResources.resolve("bal-missing-error")
                .resolve("missing-token-padding-expected.txt"));

        Assert.assertEquals(output, expectedOutput);
    }

    private static String getAnnotatedDiagnostics(Diagnostic[] diagnostics, Package currentPackage) {
        StringBuilder output = new StringBuilder();
        AnnotateDiagnostics annotateDiagnostics = new AnnotateDiagnostics(currentPackage);
        for (Diagnostic diagnostic : diagnostics) {
            output.append(annotateDiagnostics.renderDiagnostic(diagnostic).toString());
            output.append(System.lineSeparator());
        }
        return output.toString();
    }

    private static String getExpectedOutput(Path path, String fileName) throws IOException {
        if (isWindows()) {
            return Files.readString(path.resolve("windows").resolve(fileName)).replaceAll("\r", "");
        } else {
            return Files.readString(path.resolve("unix").resolve(fileName));
        }
    }
}

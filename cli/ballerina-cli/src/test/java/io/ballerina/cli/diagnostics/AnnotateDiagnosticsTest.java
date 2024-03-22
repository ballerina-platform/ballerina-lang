package io.ballerina.cli.diagnostics;

import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.cli.utils.OsUtils.isWindows;

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
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        String expectedOutput = Files.readString(testResources.resolve("bal-missing-error")
                .resolve("semicolon-missing-expected.txt"));
        Assert.assertTrue(diagnostics.length > 0);
        Diagnostic diagnostic = diagnostics[0];
        Document document = documentMap.get(diagnostic.location().lineRange().fileName());
        String output = AnnotateDiagnostics.renderDiagnostic(diagnostic, document, 999, false).toString();
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains a missing function keyword")
    public void testMissingFunctionKeywordAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-missing-error/missing-function-keyword.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        String expectedOutput = Files.readString(testResources.resolve("bal-missing-error")
                .resolve("missing-function-keyword-expected.txt"));
        Assert.assertEquals(diagnostics.length, 1);
        Diagnostic diagnostic = diagnostics[0];
        Document document = documentMap.get(diagnostic.location().lineRange().fileName());
        String output = AnnotateDiagnostics.renderDiagnostic(diagnostic, document, 999, false).toString();
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains a missing keywords")
    public void testMissingMultipleKeywordsAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-missing-error/missing-multiple-keywords.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        String expectedOutput = Files.readString(testResources.resolve("bal-missing-error")
                .resolve("missing-multiple-keywords-expected.txt"));
        Assert.assertTrue(diagnostics.length > 0);
        String output = getAnnotatedDiagnostics(diagnostics, documentMap);
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains invalid tokens")
    void testInvalidTokenAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-invalid-error/invalid-token.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        String expectedOutput = Files.readString(testResources.resolve("bal-invalid-error")
                .resolve("invalid-token-expected.txt"));
        Assert.assertEquals(diagnostics.length, 1);
        Diagnostic diagnostic = diagnostics[0];
        Document document = documentMap.get(diagnostic.location().lineRange().fileName());
        String output = AnnotateDiagnostics.renderDiagnostic(diagnostic, document, 999, false).toString();
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when erroneous line is longer than the terminal width. Tests truncation")
    void testLongLineAnnotation() throws IOException {
        int[] terminalWidth = {999, 200, 150, 100, 50, 40};
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-error/long-line.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        Assert.assertEquals(diagnostics.length, 1);
        Diagnostic diagnostic = diagnostics[0];
        Document document = documentMap.get(diagnostic.location().lineRange().fileName());
        for (int i = 0; i < terminalWidth.length; i++) {
            String output =
                    AnnotateDiagnostics.renderDiagnostic(diagnostic, document, terminalWidth[i], false).toString();
            String expectedOutput =
                    Files.readString(testResources.resolve("bal-error").resolve("long-line-expected" + i + ".txt"));
            Assert.assertEquals(output, expectedOutput);
        }
    }

    @Test(description = "Test annotations when a ballerina project contains errors in multiple files")
    void testProjectErrorAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileOffline(
                "test-resources/diagnostics-test-files/bal-project-error/project1");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        String output = getAnnotatedDiagnostics(diagnostics, documentMap);
        String expectedOutput = getExpectedOutput(testResources.resolve("bal-project-error"), "project1-expected.txt");
        Assert.assertEquals(output, expectedOutput);

    }

    @Test(description = "Test warning annotations in a ballerina project")
    void testProjectWarningAnnotation() throws IOException {
        CompileResult result =
                BCompileUtil.compileWithoutInitInvocation(
                        "test-resources/diagnostics-test-files/bal-project-error/project2");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        StringBuilder output = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.diagnosticInfo().severity() != DiagnosticSeverity.WARNING) {
                continue;
            }
            Document document = documentMap.get(diagnostic.location().lineRange().fileName());
            output.append(AnnotateDiagnostics.renderDiagnostic(diagnostic, document, 999, false).toString());
            output.append(System.lineSeparator());
        }
        String expectedOutput = getExpectedOutput(testResources.resolve("bal-project-error"), "project2-expected.txt");
        Assert.assertEquals(output.toString(), expectedOutput);
    }

    @Test(description = "Test annotations spanning two lines in the source file")
    void testTwoLinedAnnotations() throws IOException {
        CompileResult result =
                BCompileUtil.compile("test-resources/diagnostics-test-files/bal-error/two-line-error.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        String output = getAnnotatedDiagnostics(diagnostics, documentMap);
        String expectedOutput =
                Files.readString(testResources.resolve("bal-error").resolve("two-line-error-expected.txt"));
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains diagnostics spanning multiple lines")
    void testMultiLinedAnnotations() throws IOException {
        CompileResult result =
                BCompileUtil.compile("test-resources/diagnostics-test-files/bal-error/multi-line.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        String output = getAnnotatedDiagnostics(diagnostics, documentMap);
        String expectedOutput = Files.readString(testResources.resolve("bal-error").resolve("multi-line-expected.txt"));
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test annotations when the source file contains tabs instead of spaces")
    void testAnnotationsWithTabs() throws IOException {
        CompileResult result = BCompileUtil.compile("test-resources/diagnostics-test-files/bal-error/tabs.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        String output = getAnnotatedDiagnostics(diagnostics, documentMap);
        String expectedOutput = Files.readString(testResources.resolve("bal-error").resolve("tabs-expected.txt"));
        Assert.assertEquals(output, expectedOutput);
    }

    @Test(description = "Test non terminal node missing annotations")
    void testNonTerminalNodeMissingAnnotation() throws IOException {
        CompileResult result = BCompileUtil.compileWithoutInitInvocation(
                "test-resources/diagnostics-test-files/bal-missing-error/missing-non-terminal.bal");
        Diagnostic[] diagnostics = result.getDiagnostics();
        Map<String, Document> documentMap = AnnotateDiagnostics.getDocumentMap(result.project().currentPackage());
        String expectedOutput = Files.readString(testResources.resolve("bal-missing-error")
                .resolve("missing-non-terminal-expected.txt"));
        Assert.assertEquals(diagnostics.length, 8);
        String output = getAnnotatedDiagnostics(diagnostics, documentMap);
        Assert.assertEquals(output, expectedOutput);
    }


    private static String getAnnotatedDiagnostics(Diagnostic[] diagnostics, Map<String, Document> documentMap) {
        StringBuilder output = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            Document document = documentMap.get(diagnostic.location().lineRange().fileName());
            output.append(AnnotateDiagnostics.renderDiagnostic(diagnostic, document, 999, false).toString());
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

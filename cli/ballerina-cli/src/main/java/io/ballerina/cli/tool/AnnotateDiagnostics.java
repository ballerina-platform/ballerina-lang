package io.ballerina.cli.tool;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextRange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class AnnotateDiagnostics {

    private static final Set<SyntaxKind> STATEMENT_NODES = Set.of(
            SyntaxKind.ASSIGNMENT_STATEMENT,
            SyntaxKind.CALL_STATEMENT,
            SyntaxKind.LOCAL_VAR_DECL,
            SyntaxKind.RETURN_STATEMENT);

    public static String renderDiagnostic(Diagnostic diagnostic) {

        if (diagnostic instanceof PackageDiagnostic packageDiagnostic) {
            DiagnosticAnnotation diagnosticAnnotation = getDiagnosticLineFromSyntaxAPI(
                    packageDiagnostic.diagnosticFilePath(), packageDiagnostic.location());
            return diagnosticAnnotation + "\n" + diagnostic;
        }

        return diagnostic.toString();
    }

    private static String getDiagnosticLineFromText(Path diagnosticFilePath, Location location) {
        String text = getSourceText(diagnosticFilePath);
        int lineNumber = location.lineRange().startLine().line();
        String[] lines = text.split("\n");
        String line = lines[lineNumber];
        int start = location.textRange().startOffset();
        int end = location.textRange().endOffset();
        return line;
    }

    private static DiagnosticAnnotation getDiagnosticLineFromSyntaxAPI(Path diagnosticFilePath, Location location) {
        String text = getSourceText(diagnosticFilePath);
        TextDocument textDocument = TextDocuments.from(text);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument, diagnosticFilePath.toString());
        boolean isMultiline = location.lineRange().startLine().line() != location.lineRange().endLine().line();
        int start = location.textRange().startOffset();
        int end = location.textRange().endOffset();
        int startLine = location.lineRange().startLine().line() + 1;
        NonTerminalNode diagnosticNode = ((ModulePartNode) syntaxTree.rootNode()).findNode(
                TextRange.from(start, end - start), true);
        NonTerminalNode statementNode = climbUpToStatementNode(diagnosticNode);

        return new DiagnosticAnnotation(
                statementNode.toString(),
                diagnosticNode.textRangeWithMinutiae().startOffset() -
                        statementNode.textRangeWithMinutiae().startOffset(),
                diagnosticNode.textRangeWithMinutiae().endOffset() -
                        diagnosticNode.textRangeWithMinutiae().startOffset(),
                isMultiline, startLine);
    }

    private static NonTerminalNode climbUpToStatementNode(NonTerminalNode node) {
        if (node.parent() == null) {
            return node;
        }
        if (STATEMENT_NODES.contains(node.parent().kind())) {
            return node.parent();
        }
        return climbUpToStatementNode(node.parent());
    }

    private static String getSourceText(Path sourceFilePath) {
        try {
            return new String(Files.readAllBytes(sourceFilePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}


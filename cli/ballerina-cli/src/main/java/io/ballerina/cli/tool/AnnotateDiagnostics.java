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
            return diagnostic + "\n" + diagnosticAnnotation;
        }

        return diagnostic.toString();
    }


    private static DiagnosticAnnotation getDiagnosticLineFromSyntaxAPI(Path diagnosticFilePath, Location location) {
        String text = getSourceText(diagnosticFilePath);
        TextDocument textDocument = TextDocuments.from(text);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument, diagnosticFilePath.toString());
        boolean isMultiline = location.lineRange().startLine().line() != location.lineRange().endLine().line();
        int start = location.textRange().startOffset();
        int end = location.textRange().endOffset();
        int startLine = location.lineRange().startLine().line();
        int endLine = location.lineRange().endLine().line();
        NonTerminalNode diagnosticNode = ((ModulePartNode) syntaxTree.rootNode()).findNode(
                TextRange.from(start, end - start), true);
        NonTerminalNode statementNode = climbUpToStatementNode(diagnosticNode, startLine, endLine);

        return new DiagnosticAnnotation(
                statementNode.toString(),

                diagnosticNode.textRange().startOffset() - statementNode.textRangeWithMinutiae().startOffset(),
                diagnosticNode.textRange().endOffset() - diagnosticNode.textRange().startOffset(),
                isMultiline, startLine + 1);
    }

    private static NonTerminalNode climbUpToStatementNode(NonTerminalNode node, int start, int end) {
        NonTerminalNode parent = node.parent();

        if (parent == null) {
            return node;
        }

        if (parent.lineRange().startLine().line() < start || parent.lineRange().endLine().line() > end) {
            return node;
        }

        if (STATEMENT_NODES.contains(parent.kind())) {
            return parent;
        }
        return climbUpToStatementNode(parent, start, end);
    }

    private static String getSourceText(Path sourceFilePath) {
        try {
            return new String(Files.readAllBytes(sourceFilePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}


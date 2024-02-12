package io.ballerina.shell.cli;

import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextRange;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

public class AnnotateDiagnostics {

    private static final Set<SyntaxKind> STATEMENT_NODES = Set.of(
            SyntaxKind.ASSIGNMENT_STATEMENT,
            SyntaxKind.CALL_STATEMENT,
            SyntaxKind.LOCAL_VAR_DECL,
            SyntaxKind.RETURN_STATEMENT);

    public static String renderDiagnostic(Diagnostic diagnostic, int terminalWidth) {

        if (diagnostic instanceof PackageDiagnostic packageDiagnostic) {
            DiagnosticAnnotation diagnosticAnnotation = getDiagnosticLineFromSyntaxAPI(
                    packageDiagnostic.diagnosticFilePath(), packageDiagnostic.location(), terminalWidth);
            return diagnostic + "\n" + diagnosticAnnotation;
        }

        return diagnostic.toString();
    }

    private static DiagnosticAnnotation getDiagnosticLineFromSyntaxAPI(Path diagnosticFilePath, Location location,
                                                                       int terminalWidth) {
        String text = getSourceText(diagnosticFilePath);
        TextDocument textDocument = TextDocuments.from(text);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument, diagnosticFilePath.toString());
        boolean isMultiline = location.lineRange().startLine().line() != location.lineRange().endLine().line();
        int start = location.textRange().startOffset();
        int end = location.textRange().endOffset();
        int startLine = location.lineRange().startLine().line();
        int endLine = location.lineRange().endLine().line();
//        NonTerminalNode diagnosticNode = ((ModulePartNode) syntaxTree.rootNode()).findNode(
//                TextRange.from(start, end - start), true);
//        NonTerminalNode statementNode = climbUpToStatementNode(diagnosticNode, startLine, endLine);
//        ArrayList<Node> siblings = getSiblingsOnSameRange(statementNode, startLine, endLine);

        if (isMultiline) {
            return new DiagnosticAnnotation(
                    getLines(textDocument, startLine, endLine),
                    location.lineRange().startLine().offset(),
                    textDocument.line(startLine).length() - location.lineRange().startLine().offset(),
                    location.lineRange().endLine().offset(),
                    startLine + 1,
                    terminalWidth);
        }

        return new DiagnosticAnnotation(
                getLines(textDocument, startLine, endLine),
                location.lineRange().startLine().offset(),
                location.lineRange().endLine().offset() - location.lineRange().startLine().offset(),
                startLine + 1,
                terminalWidth);
    }

    public static int getTerminalWidth() {
        try (var terminal = TerminalBuilder.terminal()) {
            return terminal.getWidth();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static NonTerminalNode climbUpToStatementNode(NonTerminalNode node, int start, int end) {
        NonTerminalNode parent = node.parent();

        if (parent == null) {
            return node;
        }

        if (parent.lineRange().startLine().line() < start || parent.lineRange().endLine().line() > end) {
            return node;
        }

//        if (STATEMENT_NODES.contains(parent.kind())) {
//            return parent;
//        }
        return climbUpToStatementNode(parent, start, end);
    }

    private static ArrayList<Node> getSiblingsOnSameRange(NonTerminalNode node, int start, int end) {
        NonTerminalNode parent = node.parent();
        ArrayList<Node> siblings = new ArrayList<>();
        if (parent == null) {
            siblings.add(node);
            return siblings;
        }
        ChildNodeList childNodeList = parent.children();
        for (Node child : childNodeList) {
            if (child.lineRange().startLine().line() >= start && child.lineRange().endLine().line() <= end) {
                siblings.add(child);
            }
        }
        return siblings;
    }

    private static ArrayList<String> getLines(TextDocument textDocument, int start, int end) {
        ArrayList<String> lines = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            lines.add(textDocument.line(i).text());
        }
        return lines;
    }

    private static String getSourceText(Path sourceFilePath) {
        try {
            return new String(Files.readAllBytes(sourceFilePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}


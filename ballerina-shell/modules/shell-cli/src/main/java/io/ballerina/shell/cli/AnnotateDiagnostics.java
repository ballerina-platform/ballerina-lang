package io.ballerina.shell.cli;

import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.projects.Document;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static io.ballerina.shell.cli.DiagnosticAnnotation.SEVERITY_COLORS;

public class AnnotateDiagnostics {

    private static final Set<SyntaxKind> STATEMENT_NODES = Set.of(
            SyntaxKind.ASSIGNMENT_STATEMENT,
            SyntaxKind.CALL_STATEMENT,
            SyntaxKind.LOCAL_VAR_DECL,
            SyntaxKind.RETURN_STATEMENT);

    public static String renderDiagnostic(Diagnostic diagnostic, Document document, int terminalWidth) {

        String diagnosticCode = diagnostic.diagnosticInfo().code();
        if (diagnosticCode.startsWith("BCE")) {
            int diagnosticCodeNumber = Integer.parseInt(diagnosticCode.substring(3));
            if (diagnosticCodeNumber < 1000) {
                return diagnosticToString(diagnostic) + getSyntaxDiagnosticAnnotation(
                        document, diagnostic.location(), terminalWidth);
            }
        }
        DiagnosticAnnotation diagnosticAnnotation = getDiagnosticLineFromSyntaxAPI(
                document, diagnostic.location(), diagnostic.diagnosticInfo().severity(), terminalWidth);
        return diagnosticToString(diagnostic) + "\n" + diagnosticAnnotation;

//        return diagnostic.toString();
    }

    public static String renderDiagnostic(Diagnostic diagnostic) {
        return diagnosticToString(diagnostic);
    }

    private static String diagnosticToString(Diagnostic diagnostic) {
        DiagnosticSeverity severity = diagnostic.diagnosticInfo().severity();
        int severityLength = severity.toString().length();
        return "@|" + SEVERITY_COLORS.get(severity) + " " + severity + "|@"
                + diagnostic.toString().substring(severityLength);

    }

    private static DiagnosticAnnotation getDiagnosticLineFromSyntaxAPI(Document document, Location location,
                                                                       DiagnosticSeverity severity, int terminalWidth) {
        TextDocument textDocument = document.textDocument();
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
                    severity,
                    terminalWidth);
        }

        return new DiagnosticAnnotation(
                getLines(textDocument, startLine, endLine),
                location.lineRange().startLine().offset(),
                location.lineRange().endLine().offset() - location.lineRange().startLine().offset(),
                startLine + 1,
                severity,
                terminalWidth);
    }

    private static String getSyntaxDiagnosticAnnotation(Document document, Location location, int terminalWidth) {
        SyntaxTree syntaxTree = document.syntaxTree();
        if (location.textRange().startOffset() == location.textRange().endOffset()) {
            Token searchStartToken =
                    ((ModulePartNode) syntaxTree.rootNode()).findToken(location.textRange().startOffset());
            if (isSyntaxErrorToken(searchStartToken, location.textRange().startOffset())) {
                return searchStartToken.toString();
            }
            HashSet<Integer> visited = new HashSet<>();
            return findSyntaxDiagnosticToken(searchStartToken.parent(),
                    location.textRange().startOffset(), visited).node().toString();
        }
        return "";
    }

    public static int getTerminalWidth() {
        try (var terminal = TerminalBuilder.terminal()) {
            return terminal.getWidth();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SyntaxErrorSearchResult findSyntaxDiagnosticToken(Node curr, int position,
                                                                     HashSet<Integer> visited) {
        if (curr instanceof Token token && isSyntaxErrorToken(curr, position)) {
            return new SyntaxErrorSearchResult(token, true);
        }

        if (curr instanceof NonTerminalNode nonTerminalNode) {
            if (isNotWithinRange(nonTerminalNode.textRangeWithMinutiae(), position)) {
                return findSyntaxDiagnosticToken(curr.parent(), position, visited);
            }

            for (Node child : nonTerminalNode.children()) {
                if (visited.contains(child.hashCode())) {
                    continue;
                }
                if (child instanceof NonTerminalNode && isNotWithinRange(child.textRangeWithMinutiae(), position)) {
                    visited.add(child.hashCode());
                    continue;
                }
                SyntaxErrorSearchResult result = findSyntaxDiagnosticToken(child, position, visited);
                if (result.found()) {
                    return result;
                }
            }

            visited.add(nonTerminalNode.hashCode());

            return findSyntaxDiagnosticToken(curr.parent(), position, visited);

        }

        return new SyntaxErrorSearchResult(curr, false);

    }

    private static boolean isNotWithinRange(TextRange textRange, int position) {
        return textRange.startOffset() > position || textRange.endOffset() < position;
    }

    private static boolean isSyntaxErrorToken(Node node, int position) {
        if (node instanceof Token token) {
            return token.textRange().startOffset() == position && token.textRange().endOffset() == position;
        }

        return false;
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

    private record SyntaxErrorSearchResult(Node node, boolean found) {

    }
}


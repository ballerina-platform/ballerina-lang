package io.ballerina.shell.cli;

import io.ballerina.compiler.internal.diagnostics.StringDiagnosticProperty;
import io.ballerina.projects.Document;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.TextDocument;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;

import static io.ballerina.shell.cli.DiagnosticAnnotation.SEVERITY_COLORS;

public class AnnotateDiagnostics {

    public static String renderDiagnostic(Diagnostic diagnostic, Document document, int terminalWidth) {

        String diagnosticCode = diagnostic.diagnosticInfo().code();
        if (diagnosticCode.startsWith("BCE")) {
            int diagnosticCodeNumber = Integer.parseInt(diagnosticCode.substring(3));
            if (diagnosticCodeNumber < 1000) {
                PackageDiagnostic packageDiagnostic = (PackageDiagnostic) diagnostic;
                return diagnosticToString(diagnostic) + "\n" + getSyntaxDiagnosticAnnotation(
                        document, packageDiagnostic, diagnosticCodeNumber, terminalWidth);
            }
        }
        DiagnosticAnnotation diagnosticAnnotation = getDiagnosticLineFromSyntaxAPI(
                document, diagnostic.location(), diagnostic.diagnosticInfo().severity(), terminalWidth);
        return diagnosticToString(diagnostic) + "\n" + diagnosticAnnotation;

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
        int startOffset = location.lineRange().startLine().offset();
        int endOffset = location.lineRange().endLine().offset();
        int startLine = location.lineRange().startLine().line();
        int endLine = location.lineRange().endLine().line();

        if (startLine != endLine) {
            return new DiagnosticAnnotation(
                    getLines(textDocument, startLine, endLine),
                    startOffset,
                    textDocument.line(startLine).length() - startOffset,
                    endOffset,
                    startLine + 1,
                    severity,
                    DiagnosticAnnotation.DiagnosticAnnotationType.REGULAR,
                    terminalWidth);
        }
        int length = endOffset - startOffset;
        return new DiagnosticAnnotation(
                getLines(textDocument, startLine, endLine),
                startOffset,
                length == 0 ? 1 : length,
                startLine + 1,
                severity,
                DiagnosticAnnotation.DiagnosticAnnotationType.REGULAR,
                terminalWidth);
    }

    private static DiagnosticAnnotation getSyntaxDiagnosticAnnotation(Document document,
                                                                      PackageDiagnostic packageDiagnostic,
                                                                      int diagnosticCode, int terminalWidth) {
        TextDocument textDocument = document.textDocument();
        Location location = packageDiagnostic.location();
        int startLine = location.lineRange().startLine().line();
        int startOffset = location.lineRange().startLine().offset();
        int padding = 0;
        int endLine = location.lineRange().endLine().line();
        int endOffset = location.lineRange().endLine().offset();
        // missing tokens and keywords
        if (diagnosticCode < 400) {
            StringDiagnosticProperty strProperty = (StringDiagnosticProperty) packageDiagnostic.properties().get(0);
            String lineString = textDocument.line(startLine).text();
            String missingTokenString = "@|red " + strProperty.value() + "|@";
            if (startOffset < lineString.length() && lineString.charAt(startOffset) != ' ') {
                missingTokenString = missingTokenString + " ";
            }
            if (startOffset > 0 && lineString.charAt(startOffset - 1) != ' ') {
                missingTokenString = " " + missingTokenString;
                padding++;
            }

            String lineWithMissingToken = lineString.substring(0, startOffset) + missingTokenString +
                    lineString.substring(startOffset);
            ArrayList<String> lines = new ArrayList<>();
            lines.add(lineWithMissingToken);
            return new DiagnosticAnnotation(
                    lines,
                    padding + startOffset,
                    strProperty.value().length(),
                    startLine + 1,
                    DiagnosticSeverity.ERROR,
                    DiagnosticAnnotation.DiagnosticAnnotationType.MISSING,
                    terminalWidth);
        }
        // Invalid Token
        if (diagnosticCode == 600) {
            ArrayList<String> lines = getLines(textDocument, startLine, endLine);
            if (lines.size() > 1) {
                String annotatedLine1 = lines.get(0).substring(0, startOffset) + "@|red " +
                        lines.get(0).substring(startOffset) + "|@";
                String annotatedLine2 = "@|red " + lines.get(lines.size() - 1).substring(0, endOffset) + "|@" +
                        lines.get(lines.size() - 1).substring(endOffset);
                lines.set(0, annotatedLine1);
                lines.set(lines.size() - 1, annotatedLine2);
                return new DiagnosticAnnotation(
                        lines,
                        startOffset,
                        textDocument.line(startLine).length() - location.lineRange().startLine().offset(),
                        endOffset,
                        startLine + 1,
                        DiagnosticSeverity.ERROR,
                        DiagnosticAnnotation.DiagnosticAnnotationType.INVALID,
                        terminalWidth);
            }
            String line = lines.get(0);
            String annotatedLine = line.substring(0, startOffset) + "@|red " +
                    line.substring(startOffset, endOffset) + "|@" + line.substring(endOffset);
            lines.set(0, annotatedLine);
            return new DiagnosticAnnotation(
                    lines,
                    startOffset,
                    endOffset - startOffset,
                    startLine + 1,
                    DiagnosticSeverity.ERROR,
                    DiagnosticAnnotation.DiagnosticAnnotationType.INVALID,
                    terminalWidth);
        }
        return getDiagnosticLineFromSyntaxAPI(document, location, DiagnosticSeverity.ERROR, terminalWidth);
    }

    public static int getTerminalWidth() {
        try (var terminal = TerminalBuilder.terminal()) {
            return terminal.getWidth();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<String> getLines(TextDocument textDocument, int start, int end) {
        ArrayList<String> lines = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            lines.add(textDocument.line(i).text());
        }
        return lines;
    }

}


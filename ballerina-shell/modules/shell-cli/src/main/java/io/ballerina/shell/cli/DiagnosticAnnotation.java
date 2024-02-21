package io.ballerina.shell.cli;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.HashMap;

public class DiagnosticAnnotation {

    private final ArrayList<String> lines;
    private final int start;
    private final int length;
    private final boolean isMultiline;
    private final int endOffset;
    private final int startLineNumber;
    private final int terminalWidth;
    private final DiagnosticSeverity severity;
    private final DiagnosticAnnotationType type;


    public static final HashMap<DiagnosticSeverity, String> SEVERITY_COLORS = new HashMap<>() {{
        put(DiagnosticSeverity.INTERNAL, "blue");
        put(DiagnosticSeverity.HINT, "blue");
        put(DiagnosticSeverity.INFO, "blue");
        put(DiagnosticSeverity.WARNING, "yellow");
        put(DiagnosticSeverity.ERROR, "red");
    }};

    public enum DiagnosticAnnotationType {
        REGULAR,
        MISSING,
        INVALID
    }

    public DiagnosticAnnotation(ArrayList<String> lines, int start, int length, int startLineNumber, DiagnosticSeverity severity, DiagnosticAnnotationType type, int terminalWidth) {
        this.start = start + 3 * countTabChars(lines.get(0), start);
        lines.set(0, replaceTabs(lines.get(0), start));
        this.lines = lines;
        this.length = length;
        this.endOffset = 0;
        this.isMultiline = false;
        this.startLineNumber = startLineNumber;
        this.severity = severity;
        this.type = type;
        this.terminalWidth = terminalWidth;
    }

    public DiagnosticAnnotation(ArrayList<String> lines, int start, int length, int endOffset, int startLineNumber, DiagnosticSeverity severity,DiagnosticAnnotationType type ,int terminalWidth) {
        this.start = start + 3 * countTabChars(lines.get(0), start);
        lines.set(0, replaceTabs(lines.get(0), start));
        this.lines = lines;
        this.length = length;
        this.endOffset = endOffset;
        this.isMultiline = true;
        this.startLineNumber = startLineNumber;
        this.severity = severity;
        this.type = type;
        this.terminalWidth = terminalWidth;
    }

    public String toString() {
        if (!isMultiline) {
            int n_digits = (int) Math.log10(startLineNumber) + 1;
            String padding = " ".repeat(n_digits + 1);
            int maxLength = terminalWidth - n_digits - 3;
            TruncateResult result = truncate(lines.get(0), maxLength, start, length);
            return padding + "| " + "\n"
                    + String.format("%" + n_digits + "d ", startLineNumber) + "| " + result.line + "\n"
                    + padding + "| " + getUnderline(result.diagnosticStart, result.diagnosticLength, this.severity, this.type) + "\n";

        }

        int max_length_line = Math.max(lines.get(0).length(), lines.get(lines.size() - 1).length());
        int n_digits_end = (int) Math.log10(startLineNumber + lines.size() - 1) + 1;
        String padding = " ".repeat(n_digits_end + 1);
        String paddingWithColon = " :" + " ".repeat(n_digits_end - 1);

        int tabsInLastLine = countTabChars(lines.get(lines.size() - 1), this.endOffset);
        lines.set(lines.size() - 1, replaceTabs(lines.get(lines.size() - 1), this.endOffset));
        int maxLength = terminalWidth - n_digits_end - 3;
        TruncateResult result1 = truncate(lines.get(0), maxLength, start, length);
        TruncateResult result2 = truncate(lines.get(lines.size() - 1), maxLength, 0,
                endOffset + 3 * tabsInLastLine);

        if (lines.size() == 2) {
            return padding + "| " + "\n"
                    + String.format("%" + n_digits_end + "d ", startLineNumber) + "| " + result1.line + "\n"
                    + padding + "| " + getUnderline(result1.diagnosticStart, result1.diagnosticLength, this.severity, this.type) + "\n"
                    + String.format("%" + n_digits_end + "d ", startLineNumber + 1) + "| " + result2.line + "\n"
                    + padding + "| " + getUnderline(0, result2.diagnosticLength, this.severity, this.type) + "\n"
                    + padding + "| " + "\n";
        }
        String padding2 = " ".repeat(Math.min(terminalWidth, max_length_line) / 2);
        return padding + "| " + "\n"
                + String.format("%" + n_digits_end + "d ", startLineNumber) + "| " + result1.line + "\n"
                + paddingWithColon + "| " + getUnderline(result1.diagnosticStart, result1.diagnosticLength, this.severity, this.type) + "\n"
                + paddingWithColon + "| " + padding2 + ":" + "\n"
                + paddingWithColon + "| " + padding2 + ":" + "\n"
                + String.format("%" + n_digits_end + "d ", startLineNumber + lines.size() - 1) + "| "
                + result2.line + "\n"
                + padding + "| " + getUnderline(0, result2.diagnosticLength, this.severity, this.type) + "\n"
                + padding + "| " + "\n";

    }

    private static String getUnderline(int offset, int length, DiagnosticSeverity severity, DiagnosticAnnotationType type) {
        String symbol = "^";
        if (type == DiagnosticAnnotationType.MISSING) {
            symbol = "+";
        } else if (type == DiagnosticAnnotationType.INVALID) {
            symbol = "-";
        }
        return " ".repeat(offset) + "@|" + SEVERITY_COLORS.get(severity) + " " + symbol.repeat(length) + "|@";
    }

    private static int countTabChars(String line, int end) {
        int count = 0;
        for (int i = 0; i < end; i++) {
            if (line.charAt(i) == '\t') {
                count++;
            }
        }
        return count;
    }

    private static TruncateResult truncate(String line, int maxLength, int diagnosticStart, int diagnosticLength) {
        if (line.length() < maxLength - 3) {
            return new TruncateResult(line, diagnosticStart, diagnosticLength);
        }
        if (diagnosticStart + diagnosticLength < maxLength - 3) {
            return new TruncateResult(line.substring(0, maxLength - 3) + "...", diagnosticStart, diagnosticLength);
        }
        int diagnosticMid = diagnosticStart + diagnosticLength / 2;
        int stepsToMoveWindow = Math.max(0, diagnosticMid - maxLength / 2);
        int border = Math.min(line.length() - 1, stepsToMoveWindow + maxLength - 6);
        int newDiagnosticStart = Math.max(0, diagnosticStart - stepsToMoveWindow + 3);
        int newDiagnosticLength = Math.min(diagnosticLength, maxLength - newDiagnosticStart - 3);
        return new TruncateResult("..." + line.substring(stepsToMoveWindow, Math.max(stepsToMoveWindow, border))
                + "...", newDiagnosticStart, Math.max(0, newDiagnosticLength));

    }

    private static String replaceTabs(String line, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < end; i++) {
            if (line.charAt(i) == '\t') {
                sb.append("    ");
            } else {
                sb.append(line.charAt(i));
            }
        }
        return sb + line.substring(end);
    }

    private record TruncateResult(String line, int diagnosticStart, int diagnosticLength) {
    }
}

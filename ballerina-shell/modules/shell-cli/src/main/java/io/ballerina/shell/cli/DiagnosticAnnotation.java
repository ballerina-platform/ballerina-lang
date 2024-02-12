package io.ballerina.shell.cli;

import java.util.ArrayList;

public class DiagnosticAnnotation {

    private final ArrayList<String> lines;
    private final int start;
    private final int length;
    private final boolean isMultiline;
    private final int endOffset;
    private final int startLineNumber;
    private final int terminalWidth;


    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";

    public DiagnosticAnnotation(ArrayList<String> lines, int start, int length, int startLineNumber, int terminalWidth) {
        this.start = start + 3 * countTabChars(lines.get(0), start);
        lines.set(0, replaceTabs(lines.get(0), start));
        this.lines = lines;
        this.length = length;
        this.endOffset = 0;
        this.isMultiline = false;
        this.startLineNumber = startLineNumber;
        this.terminalWidth = terminalWidth;
    }

    public DiagnosticAnnotation(ArrayList<String> lines, int start, int length, int endOffset, int startLineNumber, int terminalWidth) {
        this.start = start + 3 * countTabChars(lines.get(0), start);
        lines.set(0, replaceTabs(lines.get(0), start));
        this.lines = lines;
        this.length = length;
        this.endOffset = endOffset;
        this.isMultiline = true;
        this.startLineNumber = startLineNumber;
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
                    + padding + "| " + getCaretLine(result.diagnosticStart, length) + "\n";

        }


        int max_length_line = Math.max(lines.get(0).length(), lines.get(lines.size() - 1).length());
        int n_digits_end = (int) Math.log10(startLineNumber + lines.size() - 1) + 1;
        String padding = " ".repeat(n_digits_end + 1);
        String paddingWithColon = " :" + " ".repeat(n_digits_end - 1);

        int tabsInLastLine = countTabChars(lines.get(lines.size() - 1), this.endOffset);
        lines.set(lines.size() - 1, replaceTabs(lines.get(lines.size() - 1), this.endOffset));
        int maxLength = terminalWidth - n_digits_end - 3;
        TruncateResult result1 = truncate(lines.get(0), maxLength, start, length);
        int res1DiagnosticLen = result1.line().length() - result1.diagnosticStart;
        TruncateResult result2 = truncate(lines.get(lines.size() - 1), maxLength, 0,
                endOffset + 3 * tabsInLastLine);
        int res2DiagnosticLen = result2.line().length() - result2.diagnosticStart;

        if (lines.size() == 2) {
            return padding + "| " + "\n"
                    + String.format("%" + n_digits_end + "d ", startLineNumber) + "| " + result1.line + "\n"
                    + padding + "| " + getCaretLine(result1.diagnosticStart, res1DiagnosticLen) + "\n"
                    + String.format("%" + n_digits_end + "d ", startLineNumber + 1) + "| " + result2.line + "\n"
                    + padding + "| " + getCaretLine(0, endOffset + 3 * tabsInLastLine) + "\n"
                    + padding + "| " + "\n";
        }
        String padding2 = " ".repeat(Math.min(terminalWidth, max_length_line) / 2);
        return padding + "| " + "\n"
                + String.format("%" + n_digits_end + "d ", startLineNumber) + "| " + result1.line + "\n"
                + paddingWithColon + "| " + getCaretLine(result1.diagnosticStart, res1DiagnosticLen) + "\n"
                + paddingWithColon + "| " + padding2 + ":" + "\n"
                + paddingWithColon + "| " + padding2 + ":" + "\n"
                + String.format("%" + n_digits_end + "d ", startLineNumber + lines.size() - 1) + "| "
                + result2.line + "\n"
                + padding + "| " + getCaretLine(0, endOffset + 3 * tabsInLastLine) + "\n"
                + padding + "| " + "\n";

    }

    private static String getCaretLine(int offset, int length) {
        return " ".repeat(offset) + RED + "^".repeat(length) + RESET;
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
        if (line.length() < maxLength) {
            return new TruncateResult(line, diagnosticStart);
        }
        if (diagnosticStart + diagnosticLength < maxLength) {
            return new TruncateResult(line.substring(0, maxLength - 3) + "...", diagnosticStart);
        }
        int diagnosticMid = diagnosticStart + diagnosticLength / 2;
        int stepsToMoveWindow = diagnosticMid - maxLength / 2;
        return new TruncateResult("..." + line.substring(stepsToMoveWindow, maxLength - 6)
                + "...", diagnosticStart - stepsToMoveWindow + 3);

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

    private record TruncateResult(String line, int diagnosticStart) {
    }
}

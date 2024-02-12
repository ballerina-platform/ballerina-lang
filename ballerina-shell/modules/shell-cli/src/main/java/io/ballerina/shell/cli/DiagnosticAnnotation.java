package io.ballerina.shell.cli;

public class DiagnosticAnnotation {

    private final String line;
    private final int start;
    private final int length;
    private final boolean isMultiline;
    private final int endOffset;
    private final int startLineNumber;
    private final int terminalWidth;


    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";

    public DiagnosticAnnotation(String line, int start, int length, int startLineNumber, int terminalWidth) {
        this.start = start + 3 * countTabChars(line, start);
        this.line = replaceTabs(line, start);
        this.length = length;
        this.endOffset = 0;
        this.isMultiline = false;
        this.startLineNumber = startLineNumber;
        this.terminalWidth = terminalWidth;
    }

    public DiagnosticAnnotation(String line, int start, int length, int endOffset, int startLineNumber, int terminalWidth) {
        this.start = start + 3 * countTabChars(line, start);
        this.line = replaceTabs(line, start);
        this.length = length;
        this.endOffset = endOffset;
        this.isMultiline = true;
        this.startLineNumber = startLineNumber;
        this.terminalWidth = terminalWidth;
    }

    public String toString() {
        String line_ = line;
//            replace leading and trailing newlines
        if (line_.startsWith("\n")) {
            line_ = " " + line_.substring(1);
        }
        if (line_.endsWith("\n")) {
            line_ = line_.substring(0, line_.length() - 1);
        }
        if (!isMultiline) {
            int n_digits = (int) Math.log10(startLineNumber) + 1;
            String padding = " ".repeat(n_digits + 1);
            int maxLength = terminalWidth - n_digits - 3;
            TruncateResult result = truncate(line_, maxLength, start, length);
            return padding + "| " + "\n"
                    + String.format("%" + n_digits + "d ", startLineNumber) + "| " + result.line + "\n"
                    + padding + "| " + getCaretLine(result.diagnosticStart, length) + "\n";

        }

        // Multiline case
        String[] lines = line_.split("\n");
        int max_length_line = Math.max(lines[0].length(), lines[lines.length - 1].length());
        int n_digits_end = (int) Math.log10(startLineNumber + lines.length - 1) + 1;
        String padding = " ".repeat(n_digits_end + 1);
        String paddingWithColon = " :" + " ".repeat(n_digits_end - 1);

        int tabsInLastLine = countTabChars(lines[lines.length - 1], this.endOffset);
        lines[lines.length - 1] = replaceTabs(lines[lines.length - 1], this.endOffset);

        if (lines.length == 2) {
            return padding + "| " + "\n"
                    + String.format("%" + n_digits_end + "d ", startLineNumber) + "| " + lines[0] + "\n"
                    + padding + "| " + getCaretLine(start, lines[0].length() - start) + "\n"
                    + String.format("%" + n_digits_end + "d ", startLineNumber + 1) + "| " + lines[1] + "\n"
                    + padding + "| " + getCaretLine(0, endOffset + 3 * tabsInLastLine) + "\n"
                    + padding + "| " + "\n";
        }
        return padding + "| " + "\n"
                + String.format("%" + n_digits_end + "d ", startLineNumber) + "| " + lines[0] + "\n"
                + paddingWithColon + "| " + getCaretLine(start, lines[0].length() - start) + "\n"
                + paddingWithColon + "| " + " ".repeat(max_length_line / 2) + "." + "\n"
                + paddingWithColon + "| " + " ".repeat(max_length_line / 2) + "." + "\n"
                + String.format("%" + n_digits_end + "d ", startLineNumber + lines.length - 1) + "| "
                + lines[lines.length - 1] + "\n"
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
        return new TruncateResult("..." + line.substring(stepsToMoveWindow, stepsToMoveWindow + maxLength - 6)
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

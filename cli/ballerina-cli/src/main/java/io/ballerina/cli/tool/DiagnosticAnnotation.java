package io.ballerina.cli.tool;

public class DiagnosticAnnotation {

    private final String line;
    private final int start;
    private final int length;
    private final boolean isMultiline;
    private final int startLine;

    public DiagnosticAnnotation(String line, int start, int length, boolean isMultiline, int startLine) {
        this.start = start + 3 * countTabChars(line, start);
        this.line = replaceTabs(line, start);
        this.length = length;
        this.isMultiline = isMultiline;
        this.startLine = startLine;
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
            int n_digits = (int) Math.log10(startLine) + 1;
            String padding = " ".repeat(n_digits + 1);
            return padding + "| " + "\n"
                    + String.format("%" + n_digits + "d ", startLine) + "| " + line_ + "\n"
                    + padding + "| " + getCaretLine(start, length) + "\n";
        }

        // Multiline case
        String[] lines = line.split("\n");
        int n_digits_end = (int) Math.log10(startLine + lines.length - 1) + 1;
        String padding = " ".repeat(n_digits_end + 1);

        if (lines.length == 2) {
            return padding + "| " + "\n"
                    + String.format("%" + n_digits_end + "d ", startLine) + "| " + lines[0] + "\n"
                    + String.format("%" + n_digits_end + "d ", startLine + 1) + "| " + lines[1] + "\n"
                    + padding + "| " + "\n";
        }
        return padding + "| " + "\n"
                + String.format("%" + n_digits_end + "d ", startLine) + "| " + lines[0] + "\n"
                + " :" + " ".repeat(n_digits_end - 1) + "| " + "\n"
                + " :" + " ".repeat(n_digits_end - 1) + "| " + "\n"
                + String.format("%" + n_digits_end + "d ", startLine + lines.length - 1) + "| "
                + lines[lines.length - 1] + "\n"
                + padding + "| " + "\n";

    }

    private static String getCaretLine(int offset, int length) {
        return " ".repeat(offset) + "^".repeat(length);
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
}

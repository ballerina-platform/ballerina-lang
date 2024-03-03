/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.utils;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.List;
import java.util.Map;

/**
 * Represents a diagnostic annotation that is used to annotate the source code with diagnostics.
 *
 * @since 2201.9.0
 */
public class DiagnosticAnnotation {

    public static final String NEW_LINE = System.lineSeparator();
    public static final String JANSI_ANNOTATOR = "@|";
    public static final String JANSI_RESET = "|@";
    public static final Map<DiagnosticSeverity, String> SEVERITY_COLORS;

    static {
        SEVERITY_COLORS =
                Map.of(DiagnosticSeverity.INTERNAL, "blue", DiagnosticSeverity.HINT, "blue", DiagnosticSeverity.INFO,
                        "blue", DiagnosticSeverity.WARNING, "yellow", DiagnosticSeverity.ERROR, "red");
    }

    public enum DiagnosticAnnotationType {
        REGULAR,
        MISSING,
        INVALID
    }

    private final List<String> lines;
    private final int start;
    private final int length;
    private final boolean isMultiline;
    private final int endOffset;
    private final int startLineNumber;
    private final int terminalWidth;
    private final DiagnosticSeverity severity;
    private final DiagnosticAnnotationType type;
    private final boolean colorEnabled;

    public DiagnosticAnnotation(List<String> lines, int start, int length, boolean isMultiline, int endOffset,
                                int startLineNumber, DiagnosticSeverity severity, DiagnosticAnnotationType type,
                                int terminalWidth, boolean colorEnabled) {
        this.start = start + 3 * countTabChars(lines.get(0), start);
        lines.set(0, replaceTabs(lines.get(0), start));
        this.lines = lines;
        this.length = length;
        this.endOffset = endOffset;
        this.isMultiline = isMultiline;
        this.startLineNumber = startLineNumber;
        this.severity = severity;
        this.type = type;
        this.terminalWidth = terminalWidth;
        this.colorEnabled = colorEnabled;
    }

    public String toString() {
        if (!isMultiline) {
            int digitsNum = (int) Math.log10(startLineNumber) + 1;
            String padding = " ".repeat(digitsNum + 1);
            int maxLength = terminalWidth - digitsNum - 3;
            TruncateResult result = truncate(lines.get(0), maxLength, start, length);
            return padding + "|" + NEW_LINE
                    + String.format("%" + digitsNum + "d ", startLineNumber) + "| " + result.line + NEW_LINE
                    + padding + "| " +
                    getUnderline(result.diagnosticStart, result.diagnosticLength, this.severity, this.type,
                            colorEnabled) + NEW_LINE;

        }

        int maxLineLength = Math.max(lines.get(0).length(), lines.get(lines.size() - 1).length());
        int endDigitsNum = (int) Math.log10(startLineNumber + lines.size() - 1) + 1;
        String padding;
        String paddingWithColon;
        if (endDigitsNum == 1) {
            padding = " ".repeat(endDigitsNum + 1);
            paddingWithColon = ":" + " ".repeat(endDigitsNum);
        } else {
            padding = " ".repeat(endDigitsNum + 1);
            paddingWithColon = " :" + " ".repeat(endDigitsNum - 1);
        }
        int tabsInLastLine = countTabChars(lines.get(lines.size() - 1), this.endOffset);
        lines.set(lines.size() - 1, replaceTabs(lines.get(lines.size() - 1), this.endOffset));
        int maxLength = terminalWidth - endDigitsNum - 3;
        TruncateResult result1 = truncate(lines.get(0), maxLength, start, length);
        TruncateResult result2 = truncate(lines.get(lines.size() - 1), maxLength, 0,
                endOffset + 3 * tabsInLastLine);

        if (lines.size() == 2) {
            return padding + "|" + NEW_LINE
                    + String.format("%" + endDigitsNum + "d ", startLineNumber) + "| " + result1.line + NEW_LINE
                    + padding + "| " +
                    getUnderline(result1.diagnosticStart, result1.diagnosticLength, this.severity, this.type,
                            colorEnabled) + NEW_LINE
                    + String.format("%" + endDigitsNum + "d ", startLineNumber + 1) + "| " + result2.line + NEW_LINE
                    + padding + "| " +
                    getUnderline(0, result2.diagnosticLength, this.severity, this.type, colorEnabled) + NEW_LINE
                    + padding + "|" + NEW_LINE;
        }
        String padding2 = " ".repeat(Math.min(terminalWidth, maxLineLength) / 2);
        return padding + "|" + NEW_LINE
                + String.format("%" + endDigitsNum + "d ", startLineNumber) + "| " + result1.line + NEW_LINE
                + paddingWithColon + "| " +
                getUnderline(result1.diagnosticStart, result1.diagnosticLength, this.severity, this.type,
                        colorEnabled) + NEW_LINE
                + paddingWithColon + "| " + padding2 + ":" + NEW_LINE
                + paddingWithColon + "| " + padding2 + ":" + NEW_LINE
                + String.format("%" + endDigitsNum + "d ", startLineNumber + lines.size() - 1) + "| "
                + result2.line + NEW_LINE
                + padding + "| " + getUnderline(0, result2.diagnosticLength, this.severity, this.type, colorEnabled) +
                NEW_LINE
                + padding + "|" + NEW_LINE;

    }

    public static String getColoredString(String message, String color, boolean colorEnabled) {
        return colorEnabled ? JANSI_ANNOTATOR + color + " " + message + JANSI_RESET : message;
    }

    private static String getUnderline(int offset, int length, DiagnosticSeverity severity,
                                       DiagnosticAnnotationType type, boolean colorEnabled) {
        String symbol = "^";
        if (type == DiagnosticAnnotationType.MISSING) {
            symbol = "+";
        }
        return " ".repeat(offset) +
                getColoredString(symbol.repeat(length), SEVERITY_COLORS.get(severity), colorEnabled);
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

    /**
     * Represents a result of truncating a line.
     *
     * @param line             The truncated line
     * @param diagnosticStart  The start of the diagnostic in the truncated line
     * @param diagnosticLength The length of the diagnostic in the truncated line
     */
    private record TruncateResult(String line, int diagnosticStart, int diagnosticLength) {

    }
}

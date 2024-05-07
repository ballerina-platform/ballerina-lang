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

package io.ballerina.cli.diagnostics;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.List;
import java.util.Map;

/**
 * Represents a diagnostic annotation that is used to annotate the source code with diagnostics.
 *
 * @since 2201.10.0
 */
public final class DiagnosticAnnotation {

    public static final String NEW_LINE = System.lineSeparator();
    public static final String JANSI_ANNOTATOR = "@|";
    public static final String JANSI_RESET = "|@"; // @|red , TEXT |@
    private static final String PIPE = "|";
    private static final String COLON = ":";
    private static final String ELLIPSIS = "...";
    private static final int PIPE_AND_PADDING_LENGTH = " | ".length();
    private static final int MAX_LINES_BEFORE_HIDING = 2;
    private static final String INTERNAL_COLOR = "blue";
    private static final String HINT_COLOR = "green";
    private static final String INFO_COLOR = "blue";
    private static final String WARNING_COLOR = "yellow";
    private static final String ERROR_COLOR = "red";
    public static final Map<DiagnosticSeverity, String> SEVERITY_COLORS;

    static {
        SEVERITY_COLORS =
                Map.of(DiagnosticSeverity.INTERNAL, INTERNAL_COLOR, DiagnosticSeverity.HINT, HINT_COLOR,
                        DiagnosticSeverity.INFO, INFO_COLOR, DiagnosticSeverity.WARNING, WARNING_COLOR,
                        DiagnosticSeverity.ERROR, ERROR_COLOR);
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
    private final boolean isColorEnabled;

    public DiagnosticAnnotation(List<String> lines, int start, int length, boolean isMultiline, int endOffset,
                                int startLineNumber, DiagnosticSeverity severity, DiagnosticAnnotationType type,
                                int terminalWidth, boolean isColorEnabled) {
        this.start = getStart(lines, start);
        lines.set(0, replaceTabs(lines.get(0), start));
        this.lines = lines;
        this.length = length;
        this.endOffset = endOffset;
        this.isMultiline = isMultiline;
        this.startLineNumber = startLineNumber + 1;
        this.severity = severity;
        this.type = type;
        this.terminalWidth = terminalWidth;
        this.isColorEnabled = isColorEnabled;
    }

    @Override
    public String toString() {
        StringBuilder outputBuilder = new StringBuilder();
        if (!isMultiline) {
            int digitsNum = (int) Math.log10(startLineNumber) + 1;
            String padding = " ".repeat(digitsNum + 1);
            int maxLength = terminalWidth - digitsNum - PIPE_AND_PADDING_LENGTH;
            TruncateResult result = truncate(lines.get(0), maxLength, start, length);
            outputBuilder.append(padding).append(PIPE).append(NEW_LINE)
                    .append(getLineNumberString(digitsNum, startLineNumber)).append(PIPE).append(" ")
                    .append(result.line).append(NEW_LINE)
                    .append(padding).append(PIPE).append(" ")
                    .append(getUnderline(result.diagnosticStart, result.diagnosticLength, this.severity, this.type,
                            isColorEnabled)).append(NEW_LINE);
            return outputBuilder.toString();

        }
        String startLine = lines.get(0);
        String endLine = lines.get(lines.size() - 1);

        int maxLineLength = Math.max(startLine.length(), endLine.length());
        int endDigitsNum = (int) Math.log10(startLineNumber + lines.size() - 1) + 1;
        String padding = " ".repeat(endDigitsNum + 1);
        String paddingWithColon;
        if (endDigitsNum == 1) {
            paddingWithColon = COLON + " ".repeat(endDigitsNum);
        } else {
            paddingWithColon = " " + COLON + " ".repeat(endDigitsNum - 1);
        }
        int tabsInLastLine = countTabChars(endLine, this.endOffset);
        endLine = replaceTabs(endLine, this.endOffset);
        int maxLength = terminalWidth - endDigitsNum - PIPE_AND_PADDING_LENGTH;
        TruncateResult startLineResult = truncate(startLine, maxLength, start, length);
        TruncateResult endLineResult = truncate(endLine, maxLength, 0,
                endOffset + 3 * tabsInLastLine);

        outputBuilder.append(padding).append(PIPE).append(NEW_LINE)
                .append(getLineNumberString(endDigitsNum, startLineNumber)).append(PIPE).append(" ")
                .append(startLineResult.line).append(NEW_LINE);

        if (lines.size() <= MAX_LINES_BEFORE_HIDING) {
            outputBuilder.append(padding).append(PIPE).append(" ")
                    .append(getUnderline(startLineResult.diagnosticStart, startLineResult.diagnosticLength,
                            this.severity,
                            this.type, isColorEnabled)).append(NEW_LINE);
            for (int i = 1; i < lines.size() - 1; i++) {
                String line = replaceTabs(lines.get(i), 0);
                TruncateResult lineResult = truncate(line, maxLength, 0, line.length());
                outputBuilder.append(getLineNumberString(endDigitsNum, startLineNumber + i)).append(PIPE).append(" ")
                        .append(lineResult.line).append(NEW_LINE)
                        .append(padding).append(PIPE).append(" ")
                        .append(getUnderline(lineResult.diagnosticStart, lineResult.diagnosticLength, this.severity,
                                this.type, isColorEnabled)).append(NEW_LINE);
            }

        } else {
            String paddingToMiddleColon = " ".repeat(Math.min(terminalWidth, maxLineLength) / 2);
            String hiddenLinesPlaceholder = paddingWithColon + PIPE + " " + paddingToMiddleColon + COLON + NEW_LINE;
            outputBuilder.append(paddingWithColon).append(PIPE).append(" ")
                    .append(getUnderline(startLineResult.diagnosticStart, startLineResult.diagnosticLength,
                            this.severity,
                            this.type, isColorEnabled)).append(NEW_LINE)
                    .append(hiddenLinesPlaceholder).append(hiddenLinesPlaceholder);

        }
        return outputBuilder.append(getLineNumberString(endDigitsNum, startLineNumber + lines.size() - 1))
                .append(PIPE).append(" ").append(endLineResult.line).append(NEW_LINE)
                .append(padding).append(PIPE).append(" ")
                .append(getUnderline(0, endLineResult.diagnosticLength, this.severity, this.type, isColorEnabled))
                .append(NEW_LINE).toString();
    }

    public static String getColoredString(String message, String color, boolean isColorEnabled) {
        return isColorEnabled ? JANSI_ANNOTATOR + color + " " + message + JANSI_RESET : message;
    }

    private static String getLineNumberString(int numberOfDigits, int lineNumber) {
        return String.format("%" + numberOfDigits + "d ", lineNumber);
    }

    private static int getStart(List<String> lines, int start) {
        return start + 3 * countTabChars(lines.get(0), start);
    }

    private static String getUnderline(int offset, int length, DiagnosticSeverity severity,
                                       DiagnosticAnnotationType type, boolean isColorEnabled) {
        String symbol = "^";
        if (type == DiagnosticAnnotationType.MISSING) {
            symbol = "+";
        }
        return " ".repeat(offset) +
                getColoredString(symbol.repeat(length), SEVERITY_COLORS.get(severity), isColorEnabled);
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

    protected static TruncateResult truncate(String line, int maxLength, int diagnosticStart, int diagnosticLength) {
        if (line.length() <= maxLength) {
            return new TruncateResult(line, diagnosticStart, diagnosticLength);
        }

        StringBuilder truncatedLineBuilder = new StringBuilder();
        if (diagnosticStart + diagnosticLength <= maxLength - ELLIPSIS.length()) {
            truncatedLineBuilder.append(line, 0, maxLength - ELLIPSIS.length()).append(ELLIPSIS);
            return new TruncateResult(truncatedLineBuilder.toString(), diagnosticStart, diagnosticLength);
        }

        int diagnosticMid = diagnosticStart + (diagnosticLength / 2);
        int stepsToMoveWindow = Math.max(0, diagnosticMid - (maxLength / 2));
        int border = Math.min(line.length(), stepsToMoveWindow + maxLength - ELLIPSIS.length());
        int newDiagnosticStart = Math.max(ELLIPSIS.length(), diagnosticStart - stepsToMoveWindow);
        int newDiagnosticLength = Math.min(diagnosticLength, maxLength - newDiagnosticStart - ELLIPSIS.length());
        int stringStart = Math.min(stepsToMoveWindow + ELLIPSIS.length(), border);

        truncatedLineBuilder.append(ELLIPSIS).append(line, stringStart, border);
        if (border < line.length()) {
            truncatedLineBuilder.append(ELLIPSIS);
        }
        return new TruncateResult(truncatedLineBuilder.toString(), newDiagnosticStart,
                Math.max(0, newDiagnosticLength));
    }

    private static String replaceTabs(String line, int end) {
        int endIndex = Math.min(end, line.length());
        return line.substring(0, endIndex).replace("\t", "    ") + line.substring(endIndex);
    }

    /**
     * Represents a result of truncating a line.
     *
     * @param line             The truncated line
     * @param diagnosticStart  The start of the diagnostic in the truncated line
     * @param diagnosticLength The length of the diagnostic in the truncated line
     */
    protected record TruncateResult(String line, int diagnosticStart, int diagnosticLength) {

    }
}

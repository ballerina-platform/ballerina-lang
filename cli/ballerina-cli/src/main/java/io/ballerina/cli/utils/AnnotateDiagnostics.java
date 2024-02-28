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

import io.ballerina.compiler.internal.diagnostics.StringDiagnosticProperty;
import io.ballerina.projects.Document;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.TextDocument;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.cli.utils.DiagnosticAnnotation.NEW_LINE;
import static io.ballerina.cli.utils.DiagnosticAnnotation.SEVERITY_COLORS;
import static io.ballerina.cli.utils.DiagnosticAnnotation.getColoredString;

/**
 * This class is used to generate diagnostic annotations from diagnostics.
 *
 * @since 2201.9.0
 */
public class AnnotateDiagnostics {

    private static final String COMPILER_ERROR_PREFIX = "BCE";
    private static final int SYNTAX_ERROR_CODE_THRESHOLD = 1000;
    private static final int MISSING_TOKEN_KEYWORD_CODE_THRESHOLD = 400;
    private static final int INVALID_TOKEN_CODE = 600;

    public static String renderDiagnostic(Diagnostic diagnostic, Document document, int terminalWidth) {

        String diagnosticCode = diagnostic.diagnosticInfo().code();
        if (diagnostic instanceof PackageDiagnostic && diagnosticCode.startsWith(COMPILER_ERROR_PREFIX)) {
            int diagnosticCodeNumber = Integer.parseInt(diagnosticCode.substring(3));
            if (diagnosticCodeNumber < SYNTAX_ERROR_CODE_THRESHOLD) {
                PackageDiagnostic packageDiagnostic = (PackageDiagnostic) diagnostic;
                return diagnosticToString(diagnostic) + NEW_LINE + getSyntaxDiagnosticAnnotation(
                        document, packageDiagnostic, diagnosticCodeNumber, terminalWidth);
            }
        }
        DiagnosticAnnotation diagnosticAnnotation = getDiagnosticLineFromSyntaxAPI(
                document, diagnostic.location(), diagnostic.diagnosticInfo().severity(), terminalWidth);
        return diagnosticToString(diagnostic) + NEW_LINE + diagnosticAnnotation;

    }

    public static String renderDiagnostic(Diagnostic diagnostic) {
        return diagnosticToString(diagnostic);
    }

    private static String diagnosticToString(Diagnostic diagnostic) {
        DiagnosticSeverity severity = diagnostic.diagnosticInfo().severity();
        String severityString = severity.toString();
        String color = SEVERITY_COLORS.get(severity);
        String message = diagnostic.toString().substring(severityString.length());
        String code = diagnostic.diagnosticInfo().code();
        String formatString = getColoredString("%s", color) + "%s (%s)";

        return String.format(formatString, severityString, message, code);
    }

    private static DiagnosticAnnotation getDiagnosticLineFromSyntaxAPI(Document document, Location location,
                                                                       DiagnosticSeverity severity, int terminalWidth) {
        TextDocument textDocument = document.textDocument();
        int startOffset = location.lineRange().startLine().offset();
        int endOffset = location.lineRange().endLine().offset();
        int startLine = location.lineRange().startLine().line();
        int endLine = location.lineRange().endLine().line();
        boolean isMultiline = startLine != endLine;
        int length = isMultiline ? textDocument.line(startLine).length() - startOffset : endOffset - startOffset;

        return new DiagnosticAnnotation(
                getLines(textDocument, startLine, endLine),
                startOffset,
                length == 0 ? 1 : length,
                isMultiline,
                endOffset,
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
        String color = SEVERITY_COLORS.get(DiagnosticSeverity.ERROR);

        if (diagnosticCode < MISSING_TOKEN_KEYWORD_CODE_THRESHOLD) {
            StringDiagnosticProperty strProperty = (StringDiagnosticProperty) packageDiagnostic.properties().get(0);
            String lineString = textDocument.line(startLine).text();
            String missingTokenString = getColoredString(strProperty.value(), color);
            if (startOffset < lineString.length() && lineString.charAt(startOffset) != ' ') {
                missingTokenString = missingTokenString + " ";
            }
            if (startOffset > 0 && lineString.charAt(startOffset - 1) != ' ') {
                missingTokenString = " " + missingTokenString;
                padding++;
            }

            String lineWithMissingToken = lineString.substring(0, startOffset) + missingTokenString +
                    lineString.substring(startOffset);
            List<String> lines = new ArrayList<>();
            lines.add(lineWithMissingToken);
            return new DiagnosticAnnotation(
                    lines,
                    padding + startOffset,
                    strProperty.value().length(),
                    false,
                    0,
                    startLine + 1,
                    DiagnosticSeverity.ERROR,
                    DiagnosticAnnotation.DiagnosticAnnotationType.MISSING,
                    terminalWidth);
        }

        if (diagnosticCode == INVALID_TOKEN_CODE) {
            List<String> lines = getLines(textDocument, startLine, endLine);
            if (lines.size() > 1) {
                String annotatedLine1 = lines.get(0).substring(0, startOffset) +
                        getColoredString(lines.get(0).substring(startOffset), color);
                String annotatedLine2 =
                        getColoredString(lines.get(lines.size() - 1).substring(0, endOffset), color) +
                                lines.get(lines.size() - 1).substring(endOffset);
                lines.set(0, annotatedLine1);
                lines.set(lines.size() - 1, annotatedLine2);
                return new DiagnosticAnnotation(
                        lines,
                        startOffset,
                        textDocument.line(startLine).length() - location.lineRange().startLine().offset(),
                        true,
                        endOffset,
                        startLine + 1,
                        DiagnosticSeverity.ERROR,
                        DiagnosticAnnotation.DiagnosticAnnotationType.INVALID,
                        terminalWidth);
            }
            String line = lines.get(0);
            String annotatedLine = line.substring(0, startOffset) +
                    getColoredString(line.substring(startOffset, endOffset), color) + line.substring(endOffset);
            lines.set(0, annotatedLine);
            return new DiagnosticAnnotation(
                    lines,
                    startOffset,
                    endOffset - startOffset,
                    false,
                    0,
                    startLine + 1,
                    DiagnosticSeverity.ERROR,
                    DiagnosticAnnotation.DiagnosticAnnotationType.INVALID,
                    terminalWidth);
        }
        return getDiagnosticLineFromSyntaxAPI(document, location, DiagnosticSeverity.ERROR, terminalWidth);
    }

    private static List<String> getLines(TextDocument textDocument, int start, int end) {
        List<String> lines = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            lines.add(textDocument.line(i).text());
        }
        return lines;
    }

}

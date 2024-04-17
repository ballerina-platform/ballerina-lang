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

import io.ballerina.compiler.internal.diagnostics.StringDiagnosticProperty;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import org.jline.jansi.Ansi;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.cli.diagnostics.DiagnosticAnnotation.NEW_LINE;
import static io.ballerina.cli.diagnostics.DiagnosticAnnotation.SEVERITY_COLORS;
import static io.ballerina.cli.diagnostics.DiagnosticAnnotation.getColoredString;
import static io.ballerina.cli.utils.OsUtils.isWindows;

/**
 * This class is used to generate diagnostic annotated messages from diagnostics.
 *
 * @since 2201.9.0
 */
public class AnnotateDiagnostics {

    private static final String COMPILER_ERROR_PREFIX = "BCE";
    private static final int SYNTAX_ERROR_CODE_THRESHOLD = 1000;
    private static final int MISSING_TOKEN_KEYWORD_CODE_THRESHOLD = 400;
    private static final int INVALID_TOKEN_CODE = 600;
    private static final int NO_TRUNCATE_WIDTH = 999;

    private final Map<String, Document> documentMap;
    private int terminalWidth;
    private final boolean colorEnabled;

    public AnnotateDiagnostics(Package currentPackage) {
        this.documentMap = getDocumentMap(currentPackage);
        this.terminalWidth = getTerminalWidth();
        this.colorEnabled = this.terminalWidth != 0;
    }

    /**
     * Returns an annotated diagnostic that is ready to be printed to the console.
     *
     * @param diagnostic The diagnostic to be annotated.
     * @return The annotated diagnostic.
     */
    public Ansi renderDiagnostic(Diagnostic diagnostic) {
        Location diagnosticLocation = diagnostic.location();
        Document document = documentMap.get(diagnosticLocation.lineRange().fileName());
        if (document == null) {
            return renderAnsi(diagnosticToString(diagnostic, colorEnabled));
        }

        DiagnosticInfo diagnosticInfo = diagnostic.diagnosticInfo();
        String diagnosticCode = diagnosticInfo.code();
        this.terminalWidth = this.terminalWidth == 0 ? NO_TRUNCATE_WIDTH : this.terminalWidth;
        if (diagnostic instanceof PackageDiagnostic packageDiagnostic && diagnosticCode != null &&
                diagnosticCode.startsWith(COMPILER_ERROR_PREFIX)) {
            int diagnosticCodeNumber = Integer.parseInt(diagnosticCode.substring(3));
            if (diagnosticCodeNumber < SYNTAX_ERROR_CODE_THRESHOLD) {
                return renderAnsi(
                        diagnosticToString(diagnostic, colorEnabled) + NEW_LINE + getSyntaxDiagnosticAnnotation(
                                document, packageDiagnostic, diagnosticCodeNumber, this.terminalWidth, colorEnabled));
            }
        }

        DiagnosticAnnotation diagnosticAnnotation =
                getDiagnosticAnnotation(document, diagnosticLocation, diagnosticInfo.severity(), this.terminalWidth,
                        colorEnabled);
        return renderAnsi(diagnosticToString(diagnostic, colorEnabled) + NEW_LINE + diagnosticAnnotation);

    }

    private static int getTerminalWidth() {
        try {
            return TerminalBuilder.builder().dumb(true).build().getWidth();
        } catch (IOException e) {
            return NO_TRUNCATE_WIDTH;
        }
    }

    private static void processDocuments(Module module, DocumentId documentId, Map<String, Document> documentMap) {
        Document document = module.document(documentId);
        documentMap.put(getDocumentPath(module.moduleName(), document.name()), document);
    }

    /**
     * Returns a map of documents in the given package.
     *
     * @param currentPackage The package to get the documents from.
     * @return A map of document paths to documents.
     */
    private static Map<String, Document> getDocumentMap(Package currentPackage) {
        Map<String, Document> documentMap = new HashMap<>();
        currentPackage.moduleIds().forEach(moduleId -> {
            Module module = currentPackage.module(moduleId);
            module.documentIds().forEach(documentId -> processDocuments(module, documentId, documentMap));
            module.testDocumentIds().forEach(documentId -> processDocuments(module, documentId, documentMap));
        });

        return documentMap;
    }

    private static String getDocumentPath(ModuleName moduleName, String documentName) {
        String documentNameFixed = isWindows() ? documentName.replace("/", "\\") : documentName;
        if (moduleName.isDefaultModuleName()) {
            return documentNameFixed;
        }
        return Paths.get("modules", moduleName.moduleNamePart(), documentNameFixed).toString();
    }

    private static String diagnosticToString(Diagnostic diagnostic, boolean colorEnabled) {
        DiagnosticInfo diagnosticInfo = diagnostic.diagnosticInfo();
        DiagnosticSeverity severity = diagnosticInfo.severity();
        String severityString = severity.toString();
        String color = SEVERITY_COLORS.get(severity);
        String message = diagnostic.toString().substring(severityString.length());
        String code = diagnosticInfo.code();
        boolean isMultiline = diagnostic.message().contains(NEW_LINE);
        boolean isCodeNotNull = code != null;
        String formatString = getColoredString("%s", color, colorEnabled) + "%s" +
                (isCodeNotNull ? (isMultiline ? NEW_LINE + "(%s)" : " (%s)") : "");

        return String.format(formatString, severityString, message, isCodeNotNull ? code : "");
    }

    private static DiagnosticAnnotation getDiagnosticAnnotation(Document document, Location location,
                                                                DiagnosticSeverity severity, int terminalWidth,
                                                                boolean colorEnabled) {
        TextDocument textDocument = document.textDocument();
        LocationDetails locationDetails = getLocationDetails(location);
        boolean isMultiline = locationDetails.startLine != locationDetails.endLine;
        int length = isMultiline ? textDocument.line(locationDetails.startLine).length() - locationDetails.startOffset :
                locationDetails.endOffset - locationDetails.startOffset;

        return new DiagnosticAnnotation(
                getLines(textDocument, locationDetails.startLine, locationDetails.endLine),
                locationDetails.startOffset,
                length == 0 ? 1 : length,
                isMultiline,
                locationDetails.endOffset,
                locationDetails.startLine + 1,
                severity,
                DiagnosticAnnotation.DiagnosticAnnotationType.REGULAR,
                terminalWidth, colorEnabled);
    }

    private static DiagnosticAnnotation getSyntaxDiagnosticAnnotation(Document document,
                                                                      PackageDiagnostic packageDiagnostic,
                                                                      int diagnosticCode, int terminalWidth,
                                                                      boolean colorEnabled) {
        TextDocument textDocument = document.textDocument();
        Location location = packageDiagnostic.location();
        LocationDetails locationDetails = getLocationDetails(location);
        int padding = 0;
        String color = SEVERITY_COLORS.get(DiagnosticSeverity.ERROR);

        if (diagnosticCode < MISSING_TOKEN_KEYWORD_CODE_THRESHOLD) {
            return getMissingTokenAnnotation(packageDiagnostic, textDocument, locationDetails, color, colorEnabled,
                    terminalWidth, padding);
        }

        if (diagnosticCode == INVALID_TOKEN_CODE) {
            return getInvalidTokenAnnotation(textDocument, locationDetails, color, colorEnabled, terminalWidth);
        }
        return getDiagnosticAnnotation(document, location, DiagnosticSeverity.ERROR, terminalWidth,
                colorEnabled);
    }

    private static DiagnosticAnnotation getMissingTokenAnnotation(PackageDiagnostic packageDiagnostic,
                                                                  TextDocument textDocument,
                                                                  LocationDetails locationDetails, String color,
                                                                  boolean colorEnabled, int terminalWidth,
                                                                  int padding) {
        StringDiagnosticProperty strProperty = (StringDiagnosticProperty) packageDiagnostic.properties().get(0);
        String lineString = textDocument.line(locationDetails.startLine).text();
        String missingTokenString = getColoredString(strProperty.value(), color, colorEnabled);
        if (locationDetails.startOffset < lineString.length() &&
                lineString.charAt(locationDetails.startOffset) != ' ') {
            missingTokenString = missingTokenString + " ";
        }
        if (locationDetails.startOffset > 0 && lineString.charAt(locationDetails.startOffset - 1) != ' ') {
            missingTokenString = " " + missingTokenString;
            padding++;
        }

        String lineWithMissingToken = lineString.substring(0, locationDetails.startOffset) + missingTokenString +
                lineString.substring(locationDetails.startOffset);
        List<String> lines = new ArrayList<>();
        lines.add(lineWithMissingToken);
        return new DiagnosticAnnotation(
                lines,
                padding + locationDetails.startOffset,
                strProperty.value().length(),
                false,
                0,
                locationDetails.startLine + 1,
                DiagnosticSeverity.ERROR,
                DiagnosticAnnotation.DiagnosticAnnotationType.MISSING,
                terminalWidth, colorEnabled);
    }

    private static DiagnosticAnnotation getInvalidTokenAnnotation(TextDocument textDocument,
                                                                  LocationDetails locationDetails, String color,
                                                                  boolean colorEnabled, int terminalWidth) {
        List<String> lines = getLines(textDocument, locationDetails.startLine, locationDetails.endLine);
        String line = lines.get(0);
        String annotatedLine = line.substring(0, locationDetails.startOffset) +
                getColoredString(line.substring(locationDetails.startOffset, locationDetails.endOffset), color,
                        colorEnabled) +
                line.substring(locationDetails.endOffset);
        lines.set(0, annotatedLine);
        return new DiagnosticAnnotation(
                lines,
                locationDetails.startOffset,
                locationDetails.endOffset - locationDetails.startOffset,
                false,
                0,
                locationDetails.startLine + 1,
                DiagnosticSeverity.ERROR,
                DiagnosticAnnotation.DiagnosticAnnotationType.INVALID,
                terminalWidth, colorEnabled);
    }

    private static List<String> getLines(TextDocument textDocument, int start, int end) {
        List<String> lines = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            lines.add(textDocument.line(i).text());
        }
        return lines;
    }

    private static LocationDetails getLocationDetails(Location location) {
        LinePosition startLine = location.lineRange().startLine();
        LinePosition endLine = location.lineRange().endLine();
        return new LocationDetails(startLine.line(), startLine.offset(), endLine.line(), endLine.offset());
    }

    private static Ansi renderAnsi(String message) {
        return Ansi.ansi().render(message);
    }

    /**
     * Represents the location details of a diagnostic.
     *
     * @param startLine   The start line of the diagnostic.
     * @param startOffset The start offset of the diagnostic.
     * @param endLine     The end line of the diagnostic.
     * @param endOffset   The end offset of the diagnostic.
     */
    private record LocationDetails(int startLine, int startOffset, int endLine, int endOffset) {

    }

}

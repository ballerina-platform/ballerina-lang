/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.diagnostic;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for the diagnostics related operations.
 *
 * @since 0.983.0
 */
public class DiagnosticsHelper {
    private static final List<Diagnostic> EMPTY_DIAGNOSTIC_LIST = new ArrayList<>(0);
    /**
     * Holds last sent diagnostics for the purpose of clear-off when publishing new diagnostics.
     */
    private Map<String, List<Diagnostic>> lastDiagnosticMap;

    public DiagnosticsHelper() {
        this.lastDiagnosticMap = new HashMap<>();
    }

    /**
     * Compiles and publishes diagnostics for a source file.
     *
     * @param client            Language server client
     * @param lsCompiler        LS Compiler
     * @param filePath          file path
     * @param compilationPath   compilation path
     */
    public synchronized void compileAndSendDiagnostics(
            LanguageClient client, LSCompiler lsCompiler, Path filePath, Path compilationPath) {
        // Compile diagnostics
        BallerinaFile balFile = lsCompiler.compileFile(compilationPath, CompilerPhase.COMPILER_PLUGIN);
        Map<String, List<Diagnostic>> diagnosticMap = getDiagnostics(balFile, filePath);
        // If the client is null, returns
        if (client == null) {
            return;
        }
        // Or else, publish diagnostics
        clearDiagnostics(client, lastDiagnosticMap, diagnosticMap);
        // Publish diagnostics
        diagnosticMap.forEach((key, value) -> client.publishDiagnostics(new PublishDiagnosticsParams(key, value)));
        // Update home-repo packages
        balFile.getBLangPackage().ifPresent(this::updateHomeRepoPackages);
        lastDiagnosticMap = diagnosticMap;
    }

    /**
     * Clears already existing diagnostics.
     *
     * @param client            Language server client
     * @param lastDiagnosticMap last diagnostic map
     */
    public void clearDiagnostics(LanguageClient client, Map<String, List<Diagnostic>> lastDiagnosticMap) {
        clearDiagnostics(client, lastDiagnosticMap, new HashMap<>());
    }

    /**
     * Clears already existing diagnostics with the new diagnostics.
     *
     * @param client            Language server client
     * @param lastDiagnosticMap last diagnostic map
     * @param diagnosticMap     new diagnostic map
     */
    private void clearDiagnostics(LanguageClient client, Map<String, List<Diagnostic>> lastDiagnosticMap,
                                         Map<String, List<Diagnostic>> diagnosticMap) {
        lastDiagnosticMap.entrySet().stream()
                .filter(entry -> !diagnosticMap.containsKey(entry.getKey()))
                .forEach(entry -> client.publishDiagnostics(
                        new PublishDiagnosticsParams(entry.getKey(), EMPTY_DIAGNOSTIC_LIST))
                );
    }

    /**
     * Returns diagnostics for this file.
     *
     * @param balFile  Ballerina file
     * @param filePath file path
     * @return diagnostics map
     */
    private Map<String, List<Diagnostic>> getDiagnostics(BallerinaFile balFile, Path filePath) {
        List<org.ballerinalang.util.diagnostic.Diagnostic>
                balDiagnostics = balFile.getDiagnostics().orElseGet(ArrayList::new);
        Map<String, List<Diagnostic>> diagnosticsMap = new HashMap<>();
        balDiagnostics.forEach(diag -> {
            final org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition position = diag.getPosition();
            String fileName = position.getSource().getCompilationUnitName();
            String fileURI = Paths.get(filePath.getParent() + "", fileName).toUri().toString() + "";

            if (!diagnosticsMap.containsKey(fileURI)) {
                diagnosticsMap.put(fileURI, new ArrayList<>());
            }
            List<Diagnostic> clientDiagnostics = diagnosticsMap.get(fileURI);
            int startLine = position.getStartLine() - 1; // LSP diagnostics range is 0 based
            int startChar = position.getStartColumn() - 1;
            int endLine = position.getEndLine() - 1;
            int endChar = position.getEndColumn() - 1;

            endLine = (endLine <= 0) ? startLine : endLine;
            endChar = (endChar <= 0) ? startChar + 1 : endChar;

            Range range = new Range(new Position(startLine, startChar), new Position(endLine, endChar));
            Diagnostic diagnostic = new Diagnostic(range, diag.getMessage());
            diagnostic.setSeverity(DiagnosticSeverity.Error);
            clientDiagnostics.add(diagnostic);
        });
        return diagnosticsMap;
    }

    /**
     * Updates home repository packages list.
     *
     * @param bLangPackage {@link BLangPackage}
     */
    private void updateHomeRepoPackages(BLangPackage bLangPackage) {
        if (bLangPackage == null) {
            return;
        }
        List<TopLevelNode> importPkgs = new ArrayList<>();
        bLangPackage.getCompilationUnits().forEach(bLangCompilationUnit -> {
            importPkgs.addAll(bLangCompilationUnit.getTopLevelNodes().stream()
                                      .filter(topLevelNode -> topLevelNode instanceof ImportPackageNode)
                                      .collect(Collectors.toList()));
        });
        List<BallerinaPackage> ballerinaPackages = new ArrayList<>();
        Stream.of(LSPackageLoader.getSdkPackages(), LSPackageLoader.getHomeRepoPackages())
                .forEach(ballerinaPackages::addAll);
        importPkgs.forEach(bLangImportPackage -> {
            if (bLangImportPackage instanceof BLangImportPackage) {
                BLangImportPackage pkgNode = ((BLangImportPackage) bLangImportPackage);
                if (pkgNode.symbol != null
                        && !CommonUtil.listContainsPackage(pkgNode.symbol.pkgID.toString(), ballerinaPackages)) {
                    PackageID pkgID = pkgNode.symbol.pkgID;
                    LSPackageLoader.getHomeRepoPackages()
                            .add(new BallerinaPackage(pkgID.getOrgName().getValue(),
                                                      pkgID.getName().getValue(),
                                                      pkgID.getPackageVersion().getValue()));
                }
            }
        });
    }
}

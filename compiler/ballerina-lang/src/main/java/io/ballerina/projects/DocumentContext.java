/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ImportVersionNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.SeparatedNodeList;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.parser.NodeCloner;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Maintains the internal state of a {@code Document} instance.
 * <p>
 * Works as a document cache.
 *
 * @since 2.0.0
 */
class DocumentContext {
    private SyntaxTree syntaxTree;
    private TextDocument textDocument;
    private Set<ModuleLoadRequest> moduleLoadRequests;
    private BLangCompilationUnit compilationUnit;
    private NodeCloner nodeCloner;
    private DocumentId documentId;
    private String name;
    private String content;

    private DocumentContext(DocumentId documentId, String name, String content) {
        this.documentId = documentId;
        this.name = name;
        this.content = content;
    }

    static DocumentContext from(DocumentConfig documentConfig) {
        return new DocumentContext(documentConfig.documentId(), documentConfig.name(), documentConfig.content());
    }

    DocumentId documentId() {
        return this.documentId;
    }

    String name() {
        return this.name;
    }

    SyntaxTree syntaxTree() {
        if (this.syntaxTree != null) {
            return this.syntaxTree;
        }

        this.syntaxTree = SyntaxTree.from(this.textDocument());
        return this.syntaxTree;
    }

    TextDocument textDocument() {
        if (this.textDocument == null) {
            this.textDocument = TextDocuments.from(this.content);
        }
        return this.textDocument;
    }

    BLangCompilationUnit compilationUnit(CompilerContext compilerContext, PackageID pkgID) {
        if (compilationUnit != null) {
            return nodeCloner.clone(compilationUnit);
        }

        nodeCloner = NodeCloner.getInstance(compilerContext);
        BLangDiagnosticLog dlog = BLangDiagnosticLog.getInstance(compilerContext);

        SyntaxTree syntaxTree = syntaxTree();
        BDiagnosticSource diagnosticSource = new BDiagnosticSource(pkgID, this.name);
        reportSyntaxDiagnostics(diagnosticSource, syntaxTree, dlog);
        BLangNodeTransformer bLangNodeTransformer = new BLangNodeTransformer(compilerContext, diagnosticSource);
        compilationUnit = (BLangCompilationUnit) bLangNodeTransformer.accept(syntaxTree.rootNode()).get(0);
        return nodeCloner.clone(compilationUnit);
    }

    Set<ModuleLoadRequest> moduleLoadRequests() {
        if (this.moduleLoadRequests != null) {
            return this.moduleLoadRequests;
        }

        this.moduleLoadRequests = getModuleLoadRequests();
        return this.moduleLoadRequests;
    }

    private Set<ModuleLoadRequest> getModuleLoadRequests() {
        Set<ModuleLoadRequest> moduleLoadRequests = new HashSet<>();
        ModulePartNode modulePartNode = syntaxTree().rootNode();
        for (ImportDeclarationNode importDcl : modulePartNode.imports()) {
            moduleLoadRequests.add(getModuleLoadRequest(importDcl));
        }
        return moduleLoadRequests;
    }

    private ModuleLoadRequest getModuleLoadRequest(ImportDeclarationNode importDcl) {
        // Get organization name
        String orgName = importDcl.orgName()
                .map(orgNameNode -> orgNameNode.orgName().text())
                .orElse(null);

        // Compute package name
        SeparatedNodeList<IdentifierToken> identifierTokenList = importDcl.moduleName();
        PackageName packageName = PackageName.from(identifierTokenList.get(0).text());

        // Compute the module name
        StringJoiner stringJoiner = new StringJoiner(".");
        for (int i = 1; i < identifierTokenList.size(); i++) {
            stringJoiner.add(identifierTokenList.get(i).text());
        }
        String moduleNamePart = stringJoiner.toString();
        ModuleName moduleName = ModuleName.from(packageName, moduleNamePart.isEmpty() ? null : moduleNamePart);

        // Compute the version name
        SemanticVersion version = importDcl.version()
                .map(ImportVersionNode::versionNumber)
                .map(versionNumbers -> new SemanticVersion(
                        Integer.parseInt(versionNumbers.get(0).text()),
                        Integer.parseInt(versionNumbers.get(1).text()),
                        Integer.parseInt(versionNumbers.get(2).text())))
                .orElse(null);
        // TODO If the version is not there, check whether it is specified in the Ballerina.toml file
        return new ModuleLoadRequest(orgName, packageName, moduleName, version);
    }

    private void reportSyntaxDiagnostics(BDiagnosticSource diagnosticSource,
                                         SyntaxTree tree,
                                         BLangDiagnosticLog dlog) {
        for (Diagnostic syntaxDiagnostic : tree.diagnostics()) {
            DiagnosticPos pos = getPosition(syntaxDiagnostic.location(), diagnosticSource);

            // TODO This is the temporary mechanism
            // We need to merge the diagnostic reporting mechanisms of the new parser and the semantic analyzer
            DiagnosticCode code;

            DiagnosticSeverity severity = syntaxDiagnostic.diagnosticInfo().severity();
            if (severity == DiagnosticSeverity.WARNING) {
                code = DiagnosticCode.SYNTAX_WARNING;
                dlog.warning(pos, code, syntaxDiagnostic.message());
            } else {
                code = DiagnosticCode.SYNTAX_ERROR;
                dlog.error(pos, code, syntaxDiagnostic.message());
            }
        }
    }

    private DiagnosticPos getPosition(Location location, BDiagnosticSource diagnosticSource) {
        if (location == null) {
            return null;
        }
        LineRange lineRange = location.lineRange();
        LinePosition startPos = lineRange.startLine();
        LinePosition endPos = lineRange.endLine();
        return new DiagnosticPos(diagnosticSource, startPos.line() + 1, endPos.line() + 1,
                startPos.offset() + 1, endPos.offset() + 1);
    }
}

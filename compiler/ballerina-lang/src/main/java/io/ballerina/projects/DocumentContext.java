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

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.internal.IDLClients;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.internal.TransactionImportValidator;
import io.ballerina.projects.plugins.IDLClientGenerator;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.SourceKind;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangNodeBuilder;
import org.wso2.ballerinalang.compiler.parser.NodeCloner;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
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
    // TODO This constant should not be here
    private static final String IDENTIFIER_LITERAL_PREFIX = "'";

    private SyntaxTree syntaxTree;
    private TextDocument textDocument;
    private Set<ModuleLoadRequest> moduleLoadRequests;
    private BLangCompilationUnit compilationUnit;
    private NodeCloner nodeCloner;
    private final DocumentId documentId;
    private final String name;
    private final String content;

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

    void parse() {
        if (syntaxTree != null) {
            return;
        }

        syntaxTree = SyntaxTree.from(this.textDocument(), name);
    }

    SyntaxTree syntaxTree() {
        parse();
        return syntaxTree;
    }

    TextDocument textDocument() {
        if (this.textDocument == null) {
            this.textDocument = TextDocuments.from(this.content);
        }
        return this.textDocument;
    }

    BLangCompilationUnit compilationUnit(CompilerContext compilerContext, PackageID pkgID, SourceKind sourceKind) {
        BLangDiagnosticLog dlog = BLangDiagnosticLog.getInstance(compilerContext);
        SyntaxTree syntaxTree = syntaxTree();
        reportSyntaxDiagnostics(pkgID, syntaxTree, dlog);

        nodeCloner = NodeCloner.getInstance(compilerContext);
        if (compilationUnit != null) {
            return nodeCloner.cloneCUnit(compilationUnit);
        }
        BLangNodeBuilder bLangNodeBuilder = new BLangNodeBuilder(compilerContext, pkgID, this.name);
        compilationUnit = (BLangCompilationUnit) bLangNodeBuilder.accept(syntaxTree.rootNode()).get(0);
        compilationUnit.setSourceKind(sourceKind);
        return nodeCloner.cloneCUnit(compilationUnit);
    }

    Set<ModuleLoadRequest> moduleLoadRequests(ModuleName currentModuleName, PackageDependencyScope scope,
                                              IDLPluginManager idlPluginManager, Package currentPkg) {
        if (this.moduleLoadRequests != null) {
            return this.moduleLoadRequests;
        }

        this.moduleLoadRequests = getModuleLoadRequests(currentModuleName, scope, idlPluginManager, currentPkg);
        return this.moduleLoadRequests;
    }

    private Set<ModuleLoadRequest> getModuleLoadRequests(ModuleName currentModuleName,
                                                         PackageDependencyScope scope,
                                                         IDLPluginManager idlPluginManager,
                                                         Package currentPkg) {
        Set<ModuleLoadRequest> moduleLoadRequests = new LinkedHashSet<>();
        ModulePartNode modulePartNode = syntaxTree().rootNode();
        for (ImportDeclarationNode importDcl : modulePartNode.imports()) {
            moduleLoadRequests.add(getModuleLoadRequest(importDcl, scope));
        }

        // TODO This is a temporary solution for SLP6 release
        // TODO Traverse the syntax tree to see whether to import the ballerinai/transaction package or not
        TransactionImportValidator trxImportValidator = new TransactionImportValidator();

        if (trxImportValidator.shouldImportTransactionPackage(modulePartNode) &&
               !currentModuleName.toString().equals(Names.TRANSACTION.value)) {
            String moduleName = Names.TRANSACTION.value;
            ModuleLoadRequest ballerinaiLoadReq = new ModuleLoadRequest(
                    PackageOrg.from(Names.BALLERINA_INTERNAL_ORG.value),
                    moduleName, scope, DependencyResolutionType.PLATFORM_PROVIDED);
            moduleLoadRequests.add(ballerinaiLoadReq);
        }
        if (idlPluginManager != null) {
            Map<LineRange, PackageID> idlClientsMap = generateIDLClients(syntaxTree, idlPluginManager, currentPkg);
            // Add generated client modules to module load requests
            for (Map.Entry<LineRange, PackageID> locationPackageIDEntry : idlClientsMap.entrySet()) {
                PackageID packageID = locationPackageIDEntry.getValue();
                moduleLoadRequests.add(new ModuleLoadRequest(
                        PackageOrg.from(packageID.orgName.getValue()),
                        packageID.name.getValue(),
                        PackageDependencyScope.DEFAULT,
                        DependencyResolutionType.SOURCE));
            }
        }
        return moduleLoadRequests;
    }

    private Map<LineRange, PackageID> generateIDLClients(
            SyntaxTree syntaxTree, IDLPluginManager idlPluginManager, Package currentPkg) {
        CompilerContext compilerContext = currentPkg.project().projectEnvironmentContext()
                .getService(CompilerContext.class);
        IDLClients idlClients = IDLClients.getInstance(compilerContext);
        syntaxTree.rootNode().accept(new ClientNodeVisitor(idlPluginManager, currentPkg, idlClients.idlClientMap()));
        return idlClients.idlClientMap();
    }

    private ModuleLoadRequest getModuleLoadRequest(ImportDeclarationNode importDcl, PackageDependencyScope scope) {
        // TODO We need to handle syntax errors in importDcl
        // Get organization name
        PackageOrg orgName = importDcl.orgName()
                .map(orgNameNode -> PackageOrg.from(orgNameNode.orgName().text()))
                .orElse(null);

        // Compute the module name
        SeparatedNodeList<IdentifierToken> identifierTokenList = importDcl.moduleName();
        StringJoiner stringJoiner = new StringJoiner(".");
        for (int i = 0; i < identifierTokenList.size(); i++) {
            stringJoiner.add(handleQuotedIdentifier(identifierTokenList.get(i).text()));
        }
        String moduleName = stringJoiner.toString();

        // Create the module load request
        return new ModuleLoadRequest(orgName, moduleName, scope, DependencyResolutionType.SOURCE,
                importDcl.location());
    }

    private String handleQuotedIdentifier(String identifier) {
        if (identifier.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            return identifier.substring(1);
        } else {
            return identifier;
        }
    }

    private void reportSyntaxDiagnostics(PackageID pkgID, SyntaxTree tree, BLangDiagnosticLog dlog) {
        for (Diagnostic syntaxDiagnostic : tree.diagnostics()) {
            dlog.logDiagnostic(pkgID, syntaxDiagnostic);
        }
    }

    DocumentContext duplicate() {
        return new DocumentContext(this.documentId, this.name, syntaxTree().toSourceCode());
    }

    private static class ClientNodeVisitor extends NodeVisitor {

        private final IDLPluginManager idlPluginManager;
        private final Package currentPkg;
        private final Map<LineRange, PackageID> idlClientMap;

        public ClientNodeVisitor(IDLPluginManager idlPluginManager,
                                 Package currentPkg, Map<LineRange, PackageID> idlClientMap) {
            this.idlPluginManager = idlPluginManager;
            this.currentPkg = currentPkg;
            this.idlClientMap = idlClientMap;
        }

        @Override
        public void visit(ModuleClientDeclarationNode moduleClientDeclarationNode) {
            // report unsupported project error for single file
            if (this.currentPkg.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                ProjectDiagnosticErrorCode errorCode =
                        ProjectDiagnosticErrorCode.CLIENT_DECL_IN_UNSUPPORTED_PROJECT_KIND;
                Location location = moduleClientDeclarationNode.location();
                String message = "client declaration is not supported with standalone ballerina file";
                idlPluginManager.reportDiagnostic(createDiagnostic(errorCode, location, message));
                return;
            }

            // client declaration is in a BuildProject
            for (IDLPluginContextImpl idlPluginContext : idlPluginManager.idlPluginContexts()) {
                for (IDLClientGenerator idlClientGenerator : idlPluginContext.idlClientGenerators()) {
                    Path idlPath;
                    try {
                        idlPath = getIDLPath(moduleClientDeclarationNode);
                    } catch (IOException e) {
                        ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.INVALID_IDL_URI;
                        Location location = moduleClientDeclarationNode.location();
                        String message = "unable to get resource from uri, reason: " + e.getMessage();
                        idlPluginManager.reportDiagnostic(createDiagnostic(errorCode, location, message));
                        return;
                    }
                    IDLPluginManager.IDLSourceGeneratorContextImpl idlSourceGeneratorContext =
                            new IDLPluginManager.IDLSourceGeneratorContextImpl(
                                    moduleClientDeclarationNode,
                                    currentPkg, idlPath, idlClientMap,
                                    idlPluginManager.generatedModuleConfigs());
                    try {
                        if (idlClientGenerator.canHandle(idlSourceGeneratorContext)) {
                            idlClientGenerator.perform(idlSourceGeneratorContext);
                            idlPluginManager.diagnosticList().addAll(idlSourceGeneratorContext.reportedDiagnostics());
                            return; // Assumption: only one plugin will be able to handle a given client node
                        }
                    } catch (Exception e) {
                        ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.UNEXPECTED_IDL_EXCEPTION;
                        Location location = moduleClientDeclarationNode.location();
                        String message = "unexpected exception thrown from plugin class: " 
                                + idlClientGenerator.getClass().getName() + ", exception: " + e.getMessage();
                        idlPluginManager.reportDiagnostic(createDiagnostic(errorCode, location, message));
                        return;
                    }
                }
            }
            ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.MATCHING_PLUGIN_NOT_FOUND;
            Location location = moduleClientDeclarationNode.location();
            String message = "no matching plugin found for client declaration";
            idlPluginManager.reportDiagnostic(createDiagnostic(errorCode, location, message));
        }

        @Override
        public void visit(ClientDeclarationNode clientDeclarationNode) {
            // report unsupported project error for single file
            if (this.currentPkg.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                ProjectDiagnosticErrorCode errorCode =
                        ProjectDiagnosticErrorCode.CLIENT_DECL_IN_UNSUPPORTED_PROJECT_KIND;
                Location location = clientDeclarationNode.location();
                String message = "client declaration is not supported with standalone ballerina file";
                idlPluginManager.reportDiagnostic(createDiagnostic(errorCode, location, message));
                return;
            }

            // client declaration is in a BuildProject
            for (IDLPluginContextImpl idlPluginContext : idlPluginManager.idlPluginContexts()) {
                for (IDLClientGenerator idlClientGenerator : idlPluginContext.idlClientGenerators()) {
                    Path idlPath;
                    try {
                        idlPath = getIDLPath(clientDeclarationNode);
                    } catch (IOException e) {
                        ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.INVALID_IDL_URI;
                        Location location = clientDeclarationNode.location();
                        String message = "unable to get resource from uri, reason: " + e.getMessage();
                        idlPluginManager.reportDiagnostic(createDiagnostic(errorCode, location, message));
                        return;
                    }
                    IDLPluginManager.IDLSourceGeneratorContextImpl idlSourceGeneratorContext =
                            new IDLPluginManager.IDLSourceGeneratorContextImpl(
                                    clientDeclarationNode,
                                    currentPkg, idlPath, idlClientMap,
                                    idlPluginManager.generatedModuleConfigs());
                    try {
                        if (idlClientGenerator.canHandle(idlSourceGeneratorContext)) {
                            idlClientGenerator.perform(idlSourceGeneratorContext);
                            idlPluginManager.diagnosticList().addAll(idlSourceGeneratorContext.reportedDiagnostics());
                            return; // Assumption: only one plugin will be able to handle a given client node
                        }
                    } catch (Exception e) {
                        ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.UNEXPECTED_IDL_EXCEPTION;
                        Location location = clientDeclarationNode.location();
                        String message = "unexpected exception thrown from plugin class: "
                                + idlClientGenerator.getClass().getName() + ", exception: " + e.getMessage();
                        idlPluginManager.reportDiagnostic(createDiagnostic(errorCode, location, message));
                        return;
                    }
                }
            }
            ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.MATCHING_PLUGIN_NOT_FOUND;
            Location location = clientDeclarationNode.location();
            String message = "no matching plugin found for client declaration";
            idlPluginManager.reportDiagnostic(createDiagnostic(errorCode, location, message));
        }

        private Diagnostic createDiagnostic(ProjectDiagnosticErrorCode errorCode, Location location, String message) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                    errorCode.diagnosticId(), message, DiagnosticSeverity.ERROR);
            return DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
        }
      
        // TODO: implement validations
        private Path getIDLPath(Node clientNode) throws IOException {
            URL url = new URL(getUri(clientNode));
            Path resourcePath = this.currentPkg.project().targetDir().resolve(
                    "idl-resource" + System.currentTimeMillis());
            Files.createDirectories(this.currentPkg.project().targetDir());
            Files.createFile(resourcePath);
            try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(resourcePath.toFile())) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            }
            return resourcePath;
        }

        private String getUri(Node clientNode) {
            BasicLiteralNode clientUri;

            if (clientNode.kind() == SyntaxKind.MODULE_CLIENT_DECLARATION) {
                clientUri = ((ModuleClientDeclarationNode) clientNode).clientUri();
            } else {
                clientUri = ((ClientDeclarationNode) clientNode).clientUri();
            }

            String text = clientUri.literalToken().text();
            return text.substring(1, text.length() - 1);
        }
    }
}

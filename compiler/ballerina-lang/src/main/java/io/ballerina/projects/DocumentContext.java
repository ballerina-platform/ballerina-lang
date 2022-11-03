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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.internal.IDLClients;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.internal.TransactionImportValidator;
import io.ballerina.projects.internal.plugins.CompilerPlugins;
import io.ballerina.projects.plugins.IDLClientGenerator;
import io.ballerina.projects.util.ProjectConstants;
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
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
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

    Set<ModuleLoadRequest> moduleLoadRequests(ModuleDescriptor currentModuleDesc, PackageDependencyScope scope,
                                              IDLPluginManager idlPluginManager, CompilationOptions compilationOptions,
                                              Package currentPkg, List<Diagnostic> pluginDiagnosticList) {
        if (this.moduleLoadRequests != null) {
            return this.moduleLoadRequests;
        }

        this.moduleLoadRequests = getModuleLoadRequests(
                currentModuleDesc, scope, idlPluginManager, compilationOptions, currentPkg, pluginDiagnosticList);
        return this.moduleLoadRequests;
    }

    private Set<ModuleLoadRequest> getModuleLoadRequests(ModuleDescriptor currentModuleDesc,
                                                         PackageDependencyScope scope,
                                                         IDLPluginManager idlPluginManager,
                                                         CompilationOptions compilationOptions,
                                                         Package currentPkg,
                                                         List<Diagnostic> pluginDiagnosticList) {
        Set<ModuleLoadRequest> moduleLoadRequests = new LinkedHashSet<>();
        ModulePartNode modulePartNode = syntaxTree().rootNode();
        for (ImportDeclarationNode importDcl : modulePartNode.imports()) {
            moduleLoadRequests.add(getModuleLoadRequest(importDcl, scope));
        }

        // TODO This is a temporary solution for SLP6 release
        // TODO Traverse the syntax tree to see whether to import the ballerinai/transaction package or not
        TransactionImportValidator trxImportValidator = new TransactionImportValidator();

        if (trxImportValidator.shouldImportTransactionPackage(modulePartNode) &&
                !currentModuleDesc.name().toString().equals(Names.TRANSACTION.value)) {
            String moduleName = Names.TRANSACTION.value;
            ModuleLoadRequest ballerinaiLoadReq = new ModuleLoadRequest(
                    PackageOrg.from(Names.BALLERINA_INTERNAL_ORG.value),
                    moduleName, scope, DependencyResolutionType.PLATFORM_PROVIDED);
            moduleLoadRequests.add(ballerinaiLoadReq);
        }
        generateIDLClients(currentModuleDesc, syntaxTree, idlPluginManager, compilationOptions, currentPkg,
                moduleLoadRequests, pluginDiagnosticList);
        return moduleLoadRequests;
    }

    private void generateIDLClients(ModuleDescriptor currentModuleDesc, SyntaxTree syntaxTree,
                                    IDLPluginManager idlPluginManager, CompilationOptions compilationOptions,
                                    Package currentPkg, Set<ModuleLoadRequest> moduleLoadRequests,
                                    List<Diagnostic> pluginDiagnosticList) {

        CompilerContext compilerContext = currentPkg.project().projectEnvironmentContext()
                .getService(CompilerContext.class);
        IDLClients idlClients = IDLClients.getInstance(compilerContext);

        // Remove the client entries generated from the previous edit
        if (idlClients.idlClientMap().containsKey(currentModuleDesc.moduleCompilationId())) {
            idlClients.idlClientMap().get(currentModuleDesc.moduleCompilationId()).remove(name);
        }
        syntaxTree.rootNode().accept(new ClientNodeVisitor(
                idlPluginManager, compilationOptions, currentPkg, idlClients, moduleLoadRequests,
                pluginDiagnosticList, currentModuleDesc, name, documentId));
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
        private final CompilationOptions compilationOptions;
        private final Package currentPkg;
        private final IDLClients idlClients;
        private final Set<ModuleLoadRequest> moduleLoadRequests;
        private final List<Diagnostic> pluginDiagnosticList;
        private final ModuleDescriptor currentModuleDesc;
        private final String docName;
        private final DocumentId documentId;

        public ClientNodeVisitor(IDLPluginManager idlPluginManager,
                                 CompilationOptions compilationOptions,
                                 Package currentPkg, IDLClients idlClients,
                                 Set<ModuleLoadRequest> moduleLoadRequests,
                                 List<Diagnostic> pluginDiagnosticList,
                                 ModuleDescriptor moduleDescriptor, String docName, DocumentId documentId) {
            this.idlPluginManager = idlPluginManager;
            this.compilationOptions = compilationOptions;
            this.currentPkg = currentPkg;
            this.idlClients = idlClients;
            this.moduleLoadRequests = moduleLoadRequests;
            this.pluginDiagnosticList = pluginDiagnosticList;
            this.currentModuleDesc = moduleDescriptor;
            this.docName = docName;
            this.documentId = documentId;
        }

        @Override
        public void visit(ModuleClientDeclarationNode moduleClientDeclarationNode) {
            // report unsupported project error for single file
            if (this.currentPkg.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                ProjectDiagnosticErrorCode errorCode =
                        ProjectDiagnosticErrorCode.CLIENT_DECL_IN_UNSUPPORTED_PROJECT_KIND;
                Location location = moduleClientDeclarationNode.location();
                String message = "client declaration is not supported with standalone Ballerina file";
                pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
                return;
            }

            if (loadExistingModule(moduleClientDeclarationNode, moduleClientDeclarationNode.annotations(),
                    moduleClientDeclarationNode.clientPrefix().location().lineRange())) {
                return;
            }

            // client declaration is in a BuildProject
            executeIDLPlugin(moduleClientDeclarationNode, moduleClientDeclarationNode.location(),
                    moduleClientDeclarationNode.clientPrefix().location().lineRange());
        }

        @Override
        public void visit(ClientDeclarationNode clientDeclarationNode) {
            // report unsupported project error for single file
            if (this.currentPkg.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                ProjectDiagnosticErrorCode errorCode =
                        ProjectDiagnosticErrorCode.CLIENT_DECL_IN_UNSUPPORTED_PROJECT_KIND;
                Location location = clientDeclarationNode.location();
                String message = "client declaration is not supported with standalone Ballerina file";
                pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
                return;
            }

            if (loadExistingModule(clientDeclarationNode, clientDeclarationNode.annotations(),
                    clientDeclarationNode.clientPrefix().location().lineRange())) {
                return;
            }

            // client declaration is in a BuildProject
            executeIDLPlugin(clientDeclarationNode, clientDeclarationNode.location(),
                    clientDeclarationNode.clientPrefix().location().lineRange());
        }

        private boolean loadExistingModule(
                Node clientNode, NodeList<AnnotationNode> annotationsList, LineRange lineRange) {
            String uri = CompilerPlugins.getUri(clientNode);
            try {
                if (!isRemoteUrl(uri)) {
                    uri = getNormalizedUriPath(uri).toString();
                }
            } catch (MalformedURLException e) {
                // ignore since we only need to check if the uri is local
            }
            for (IDLClientEntry cachedPlugin : idlPluginManager.cachedClientEntries()) {
                String cachedUrl = cachedPlugin.url();
                try {
                    if (!isRemoteUrl(cachedUrl)) {
                        cachedUrl = cachedPlugin.filePath();
                    }
                } catch (MalformedURLException e) {
                    // ignore since we only need to check if the uri is local
                }
                if (cachedUrl.equals(uri)) {
                    cachedPlugin.annotations().sort(Comparator.naturalOrder());
                    if (!cachedPlugin.annotations().equals(
                            CompilerPlugins.annotationsAsStr(annotationsList))) {
                        continue;
                    }
                    if (idlPluginManager.generatedModuleConfigs().stream().noneMatch(moduleConfig ->
                            moduleConfig.moduleDescriptor().name().moduleNamePart()
                                    .equals(cachedPlugin.generatedModuleName()))) {

                        File specFile = new File(cachedPlugin.filePath());
                        if (specFile.exists()) {
                            if (cachedPlugin.lastModifiedTime() != specFile.lastModified()) {
                                // the idl resource file has been modified
                                return false;
                            }
                        }
                        if (!CompilerPlugins.moduleExists(cachedPlugin.generatedModuleName(), currentPkg.project())) {
                            // user has deleted the module
                            return false;
                        }
                    }

                    getGeneratedModuleEntry(cachedPlugin, lineRange);
                    return true;
                }
            }
            return false;
        }

        private void executeIDLPlugin(Node clientNode, Location location, LineRange lineRange) {
            if (!compilationOptions.withIDLGenerators()) {
                return;
            }

            for (IDLPluginContextImpl idlPluginContext : idlPluginManager.idlPluginContexts()) {
                for (IDLClientGenerator idlClientGenerator : idlPluginContext.idlClientGenerators()) {
                    Path idlPath;
                    try {
                        idlPath = getIdlPath(clientNode);
                    } catch (IOException e) {
                        ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.INVALID_IDL_URI;
                        String message = "unable to get resource from uri, reason: " + e.getMessage();
                        pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
                        return;
                    } catch (ProjectException e) {
                        ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.INVALID_IDL_URI;
                        pluginDiagnosticList.add(createDiagnostic(errorCode, location, e.getMessage()));
                        return;
                    }
                    IDLPluginManager.IDLSourceGeneratorContextImpl idlSourceGeneratorContext =
                            new IDLPluginManager.IDLSourceGeneratorContextImpl(
                                    clientNode, currentModuleDesc.moduleCompilationId(), docName,
                                    currentPkg, idlPath, idlClients, moduleLoadRequests,
                                    idlPluginManager.generatedModuleConfigs(), idlPluginManager.cachedClientEntries(),
                                    idlPluginManager.aliasNameCounter());
                    try {
                        if (idlClientGenerator.canHandle(idlSourceGeneratorContext)) {
                            idlClientGenerator.perform(idlSourceGeneratorContext);
                            pluginDiagnosticList.addAll(idlSourceGeneratorContext.reportedDiagnostics());
                            return;
                        }
                    } catch (Exception e) {
                        ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.UNEXPECTED_IDL_EXCEPTION;
                        String message = "unexpected exception thrown from plugin class: "
                                + idlClientGenerator.getClass().getName() + ", exception: " + e.getMessage();
                        pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
                        return;
                    }
                }
            }
            ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.MATCHING_PLUGIN_NOT_FOUND;
            String message = "no matching plugin found for client declaration";
            pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
            idlClients.addEntry(currentModuleDesc.moduleCompilationId(), docName, lineRange, null);
        }

        private void getGeneratedModuleEntry(IDLClientEntry cachedPlugin, LineRange lineRange) {
            String generatedModuleName = this.currentPkg.descriptor().name().value() +
                    ProjectConstants.DOT + cachedPlugin.generatedModuleName();
            PackageID packageID = new PackageID(new Name(this.currentPkg.descriptor().org().value()),
                    new Name(this.currentPkg.descriptor().name().value()),
                    new Name(generatedModuleName),
                    new Name(this.currentPkg.descriptor().version().toString()), null);
            idlClients.addEntry(currentModuleDesc.moduleCompilationId(), docName, lineRange,
                    packageID);
            moduleLoadRequests.add(new ModuleLoadRequest(
                    PackageOrg.from(packageID.orgName.getValue()),
                    packageID.name.getValue(),
                    PackageDependencyScope.DEFAULT,
                    DependencyResolutionType.SOURCE));
            idlPluginManager.addModuleToLoadFromCache(cachedPlugin.generatedModuleName());
        }

        private Diagnostic createDiagnostic(ProjectDiagnosticErrorCode errorCode, Location location, String message) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                    errorCode.diagnosticId(), message, DiagnosticSeverity.ERROR);
            return DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
        }

        private boolean isRemoteUrl(String uri) throws MalformedURLException {
            URL url;
            try {
                url = new URL(uri);
            } catch (MalformedURLException e) {
                if (uri.matches("(?!file\\b)\\w+?://.*")) {
                    // Remote file
                    throw e;
                }
                return false;
            }
            return !url.getProtocol().equals("file");
        }

        private Path getIdlPath(Node clientNode) throws IOException {
            String uri = CompilerPlugins.getUri(clientNode);
            if (!isRemoteUrl(uri)) {
                return resolveLocalPath(uri);
            }
            return resolveRemoteUrl(uri);
        }

        private Path resolveRemoteUrl(String uri) throws IOException {
            URL url = new URL(uri);
            String[] split = url.getFile().split("[~?=#@*+%{}<>/\\[\\]|\"^]");
            String fileName = split[split.length - 1];
            Path resourceName = Paths.get(fileName).getFileName();
            Path absResourcePath = this.currentPkg.project().sourceRoot().resolve(resourceName);

            if (Files.exists(absResourcePath)) {
                Files.delete(absResourcePath);
            }
            Files.createFile(absResourcePath);

            try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(absResourcePath.toFile())) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            }
            return resourceName;
        }

        private Path getNormalizedUriPath(String uri) {
            Path documentParent = Optional.of(this.currentPkg.project().documentPath(this.documentId)
                    .orElseThrow().getParent()).get();
            Path uriPath = Paths.get(uri);
            if (uriPath.isAbsolute()) {
                return Paths.get(uri);
            }
            return this.currentPkg.project().sourceRoot().relativize(documentParent.resolve(uri));
        }

        private Path resolveLocalPath(String uri) {
            Path localFilePath = getNormalizedUriPath(uri);
            Path absLocalFilePath = localFilePath;
            if (!absLocalFilePath.isAbsolute()) {
                absLocalFilePath = this.currentPkg.project().sourceRoot().resolve(localFilePath);
            }

            if (!Files.exists(absLocalFilePath)) {
                String message = "could not locate the file: " + uri;
                throw new ProjectException(message);
            }
            if (!Files.isRegularFile(absLocalFilePath)) {
                String message = "provided file is not a regular file: " + uri;
                throw new ProjectException(message);
            }
            if (!absLocalFilePath.toFile().canRead()) {
                String message = "provided file does not have read permission: " + uri;
                throw new ProjectException(message);
            }
            return localFilePath;
        }
    }
}

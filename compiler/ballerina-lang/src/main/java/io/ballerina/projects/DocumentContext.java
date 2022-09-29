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

import io.ballerina.compiler.internal.parser.tree.STAnnotationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
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
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.apache.commons.compress.utils.FileNameUtils;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
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
                pluginDiagnosticList, currentModuleDesc, name));
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
        private String docName;

        public ClientNodeVisitor(IDLPluginManager idlPluginManager,
                                 CompilationOptions compilationOptions,
                                 Package currentPkg, IDLClients idlClients,
                                 Set<ModuleLoadRequest> moduleLoadRequests,
                                 List<Diagnostic> pluginDiagnosticList,
                                 ModuleDescriptor moduleDescriptor, String docName) {
            this.idlPluginManager = idlPluginManager;
            this.compilationOptions = compilationOptions;
            this.currentPkg = currentPkg;
            this.idlClients = idlClients;
            this.moduleLoadRequests = moduleLoadRequests;
            this.pluginDiagnosticList = pluginDiagnosticList;
            this.currentModuleDesc = moduleDescriptor;
            this.docName = docName;
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

            String uri = getUri(moduleClientDeclarationNode);
            for (IDLClientEntry cachedPlugin : idlPluginManager.cachedClientEntries()) {
                if (cachedPlugin.url().equals(uri)) {
                    cachedPlugin.annotations().sort(Comparator.naturalOrder());
                    if (!cachedPlugin.annotations().equals(annotations(moduleClientDeclarationNode.annotations()))) {
                        continue;
                    }
                    if (!CompilerPlugins.moduleExists(cachedPlugin.generatedModuleName(), currentPkg.project())) {
                        break;
                    }
                    String generatedModuleName = this.currentPkg.descriptor().name().value() +
                            ProjectConstants.DOT + cachedPlugin.generatedModuleName();
                    PackageID packageID = new PackageID(new Name(this.currentPkg.descriptor().org().value()),
                            new Name(this.currentPkg.descriptor().name().value()),
                            new Name(generatedModuleName),
                            new Name(this.currentPkg.descriptor().version().toString()), null);
                    idlClients.addEntry(currentModuleDesc.moduleCompilationId(), docName,
                            moduleClientDeclarationNode.clientPrefix().location().lineRange(),
                            packageID);
                    moduleLoadRequests.add(new ModuleLoadRequest(
                            PackageOrg.from(packageID.orgName.getValue()),
                            packageID.name.getValue(),
                            PackageDependencyScope.DEFAULT,
                            DependencyResolutionType.SOURCE));
                    idlPluginManager.addModuleToLoadFromCache(cachedPlugin.generatedModuleName());
                    return;
                }
            }

            if (!compilationOptions.withIDLGenerators()) {
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
                        pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
                        return;
                    }
                    IDLPluginManager.IDLSourceGeneratorContextImpl idlSourceGeneratorContext =
                            new IDLPluginManager.IDLSourceGeneratorContextImpl(
                                    moduleClientDeclarationNode, currentModuleDesc.moduleCompilationId(), docName,
                                    currentPkg, idlPath, idlClients, moduleLoadRequests,
                                    idlPluginManager.generatedModuleConfigs(), idlPluginManager.cachedClientEntries());
                    try {
                        if (idlClientGenerator.canHandle(idlSourceGeneratorContext)) {
                            idlClientGenerator.perform(idlSourceGeneratorContext);
                            pluginDiagnosticList.addAll(idlSourceGeneratorContext.reportedDiagnostics());
                            return; // Assumption: only one plugin will be able to handle a given client node
                        }
                    } catch (Exception e) {
                        ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.UNEXPECTED_IDL_EXCEPTION;
                        Location location = moduleClientDeclarationNode.location();
                        String message = "unexpected exception thrown from plugin class: "
                                + idlClientGenerator.getClass().getName() + ", exception: " + e.getMessage();
                        pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
                        return;
                    }
                }
            }
            ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.MATCHING_PLUGIN_NOT_FOUND;
            Location location = moduleClientDeclarationNode.location();
            String message = "no matching plugin found for client declaration";
            pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
            idlClients.addEntry(currentModuleDesc.moduleCompilationId(), docName,
                    moduleClientDeclarationNode.clientPrefix().location().lineRange(), null);
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

            String uri = getUri(clientDeclarationNode);
            for (IDLClientEntry cachedPlugin : idlPluginManager.cachedClientEntries()) {
                if (cachedPlugin.url().equals(uri)) {
                    cachedPlugin.annotations().sort(Comparator.naturalOrder());
                    if (cachedPlugin.annotations().equals(annotations(clientDeclarationNode.annotations()))) {
                        if (!CompilerPlugins.moduleExists(cachedPlugin.generatedModuleName(), currentPkg.project())) {
                            break;
                        }
                        String generatedModuleName = this.currentPkg.descriptor().name().value() +
                                ProjectConstants.DOT + cachedPlugin.generatedModuleName();
                        PackageID packageID = new PackageID(new Name(this.currentPkg.descriptor().org().value()),
                                new Name(this.currentPkg.descriptor().name().value()),
                                new Name(generatedModuleName),
                                new Name(this.currentPkg.descriptor().version().toString()), null);
                        idlClients.addEntry(currentModuleDesc.moduleCompilationId(), docName,
                                clientDeclarationNode.clientPrefix().location().lineRange(),
                                packageID);
                        moduleLoadRequests.add(new ModuleLoadRequest(
                                PackageOrg.from(packageID.orgName.getValue()),
                                packageID.name.getValue(),
                                PackageDependencyScope.DEFAULT,
                                DependencyResolutionType.SOURCE));
                        idlPluginManager.addModuleToLoadFromCache(cachedPlugin.generatedModuleName());
                        return;
                    }
                }
            }

            if (!compilationOptions.withIDLGenerators()) {
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
                        pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
                        return;
                    }
                    IDLPluginManager.IDLSourceGeneratorContextImpl idlSourceGeneratorContext =
                            new IDLPluginManager.IDLSourceGeneratorContextImpl(
                                    clientDeclarationNode, currentModuleDesc.moduleCompilationId(), docName,
                                    currentPkg, idlPath, idlClients, moduleLoadRequests,
                                    idlPluginManager.generatedModuleConfigs(), idlPluginManager.cachedClientEntries());
                    try {
                        if (idlClientGenerator.canHandle(idlSourceGeneratorContext)) {
                            idlClientGenerator.perform(idlSourceGeneratorContext);
                            pluginDiagnosticList.addAll(idlSourceGeneratorContext.reportedDiagnostics());
                            return; // Assumption: only one plugin will be able to handle a given client node
                        }
                    } catch (Exception e) {
                        ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.UNEXPECTED_IDL_EXCEPTION;
                        Location location = clientDeclarationNode.location();
                        String message = "unexpected exception thrown from plugin class: "
                                + idlClientGenerator.getClass().getName() + ", exception: " + e.getMessage();
                        pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
                        return;
                    }
                }
            }
            ProjectDiagnosticErrorCode errorCode = ProjectDiagnosticErrorCode.MATCHING_PLUGIN_NOT_FOUND;
            Location location = clientDeclarationNode.location();
            String message = "no matching plugin found for client declaration";
            pluginDiagnosticList.add(createDiagnostic(errorCode, location, message));
            idlClients.addEntry(currentModuleDesc.moduleCompilationId(), docName,
                    clientDeclarationNode.clientPrefix().location().lineRange(), null);
        }

        private List<String> annotations(NodeList<AnnotationNode> supportedAnnotations) {
            List<String> annotations = new ArrayList<>();
            StringBuilder id = new StringBuilder();
            for (AnnotationNode annotation : supportedAnnotations) {
                String annotationRef = ((STAnnotationNode) annotation.internalNode()).annotReference.toString()
                        .replaceAll("\\s", "");
                id.append(annotationRef);

                String annotationVal = ((STAnnotationNode) annotation.internalNode()).annotValue.toString()
                        .replaceAll("\\s", "");
                id.append(annotationVal);
                annotations.add(id.toString());
            }
            annotations.sort(Comparator.naturalOrder());
            return annotations;
        }

        private Diagnostic createDiagnostic(ProjectDiagnosticErrorCode errorCode, Location location, String message) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                    errorCode.diagnosticId(), message, DiagnosticSeverity.ERROR);
            return DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
        }

        // TODO: implement validations
        private Path getIDLPath(Node clientNode) throws IOException {
            String uri = getUri(clientNode);
            URL url = getUrl(uri);
            String extension = FileNameUtils.getExtension(url.getFile());
            String fileName = "idl-spec-file" + System.currentTimeMillis();
            if (!"".equals(extension)) {
                fileName = fileName + ProjectConstants.DOT + extension;
            }
            Path resourceName = Paths.get(fileName);
            Path resourcePath = this.currentPkg.project().targetDir().resolve(resourceName);
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

        private URL getUrl(String uri) throws MalformedURLException {
            try {
                return new URL(uri);
            } catch (MalformedURLException e) {
                Path path = Paths.get(uri);
                if (!path.isAbsolute()) {
                    Path projectDir = this.currentPkg.project().sourceRoot();
                    ModuleName moduleName = this.currentModuleDesc.name();
                    if (moduleName.isDefaultModuleName()) {
                        path = projectDir.resolve(uri);
                    } else {
                        path = projectDir.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName.moduleNamePart())
                                .resolve(uri);
                    }
                }
                File file = path.toFile();
                if (file.exists()) {
                    return file.toURI().toURL();
                }
                throw e;
            }
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

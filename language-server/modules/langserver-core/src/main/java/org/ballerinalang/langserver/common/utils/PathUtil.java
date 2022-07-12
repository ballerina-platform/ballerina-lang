/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.util.RepoUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Carries a set of utilities used to perform Path related checks.
 *
 * @since 2201.1.1
 */
public class PathUtil {
    
    /**
     * Get the path from given string URI. Even if the given URI's scheme is expr or bala,
     * we convert it to file scheme and provide a valid Path.
     *
     * @param fileUri file uri
     * @return {@link Optional} Path from the URI
     */
    public static Optional<Path> getPathFromURI(String fileUri) {
        URI uri = URI.create(fileUri);
        String scheme = uri.getScheme();
        try {
            if (CommonUtil.EXPR_SCHEME.equals(uri.getScheme()) || CommonUtil.URI_SCHEME_BALA.equals(uri.getScheme())) {
                scheme = CommonUtil.URI_SCHEME_FILE;
            }
            URI converted = new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment());
            return Optional.of(Paths.get(converted));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    /**
     * Check if the provided path should be readonly. Paths residing in ballerina home and home repo are considered as
     * such.
     *
     * @param filePath Path to be checked
     * @return True if the provided path should be readonly
     */
    public static boolean isWriteProtectedPath(Path filePath) {
        Path homeReposPath = RepoUtils.createAndGetHomeReposPath();
        Path ballerinaHome = CommonUtil.BALLERINA_HOME != null ? Paths.get(CommonUtil.BALLERINA_HOME) : null;

        return filePath.startsWith(homeReposPath) || ballerinaHome != null && filePath.startsWith(ballerinaHome);
    }

    /**
     * Check and convert the URI scheme of the provided fileUri from bala (if it's bala) to file.
     *
     * @param fileUri URI to be converted.
     * @return URI with file scheme
     * @throws URISyntaxException URI parsing errors
     */
    public static String convertUriSchemeFromBala(String fileUri) throws URISyntaxException {
        URI uri = URI.create(fileUri);
        if (CommonUtil.URI_SCHEME_BALA.equals(uri.getScheme())) {
            URI converted = new URI(CommonUtil.URI_SCHEME_FILE, uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment());
            return converted.toString();
        }
        return fileUri;
    }

    /**
     * Get the URI with bala scheme for provided path. This method checks if the LS client supports bala scheme first.
     *
     * @param serverContext Language server context
     * @param filePath      File path
     * @return URI with bala scheme
     * @throws URISyntaxException URI creation errors
     */
    public static String getBalaUriForPath(LanguageServerContext serverContext,
                                           Path filePath) throws URISyntaxException {
        LSClientCapabilities clientCapabilities = serverContext.get(LSClientCapabilities.class);
        if (clientCapabilities.getInitializationOptions().isBalaSchemeSupported()) {
            return getUriForPath(filePath, CommonUtil.URI_SCHEME_BALA);
        }
        return filePath.toUri().toString();
    }

    /**
     * Returns the URI with bala scheme for the provided file path.
     *
     * @param filePath File path
     * @param scheme   URI Scheme
     * @return URI with the given scheme
     * @throws URISyntaxException URI parsing errors
     */
    public static String getUriForPath(Path filePath, String scheme) throws URISyntaxException {
        URI uri = filePath.toUri();
        uri = new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(),
                uri.getPath(), uri.getQuery(), uri.getFragment());
        return uri.toString();
    }

    /**
     * Returns the file path.
     *
     * @param orgName    organization name
     * @param moduleName module name
     * @param project    ballerina project
     * @param symbol     symbol
     * @param context    service operation context
     * @return file path
     */
    public static Optional<Path> getFilePathForDependency(String orgName, String moduleName,
                                                          Project project, Symbol symbol,
                                                          DocumentServiceContext context) {
        if (symbol.getLocation().isEmpty()) {
            return Optional.empty();
        }
        Collection<ResolvedPackageDependency> dependencies =
                project.currentPackage().getResolution().dependencyGraph().getNodes();
        Optional<Path> filepath = Optional.empty();
        String sourceFile = symbol.getLocation().get().lineRange().filePath();
        for (ResolvedPackageDependency depNode : dependencies) {
            Package depPackage = depNode.packageInstance();
            for (ModuleId moduleId : depPackage.moduleIds()) {
                if (depPackage.packageOrg().value().equals(orgName) &&
                        depPackage.module(moduleId).moduleName().toString().equals(moduleName)) {
                    Module module = depPackage.module(moduleId);
                    List<DocumentId> documentIds = new ArrayList<>(module.documentIds());
                    documentIds.addAll(module.testDocumentIds());
                    for (DocumentId docId : documentIds) {
                        if (module.document(docId).name().equals(sourceFile)) {
                            filepath =
                                    module.project().documentPath(docId);
                            break;
                        }
                    }
                }
                // Check for the cancellation after each of the module visit
                context.checkCancelled();
            }
        }
        return filepath;
    }

    /**
     * Returns the file path.
     *
     * @param symbol  symbol
     * @param project ballerina project
     * @param context service operation context
     * @return file path
     */
    public static Optional<Path> getFilePathForSymbol(Symbol symbol, Project project, DocumentServiceContext context) {
        if (symbol.getModule().isEmpty()) {
            return Optional.empty();
        }
        ModuleID moduleID = symbol.getModule().get().id();
        String orgName = moduleID.orgName();
        String moduleName = moduleID.moduleName();
        return getFilePathForDependency(orgName, moduleName, project, symbol, context);
    }

    /**
     * Returns the modified URI.
     *
     * @param workspaceManager  workspace manager instance
     * @param uri original URI
     * @return modified URI
     */
    public static String getModifiedUri(WorkspaceManager workspaceManager, String uri) {
        URI original = URI.create(uri);
        try {
            return new URI(workspaceManager.uriScheme(),
                    original.getSchemeSpecificPart(),
                    original.getFragment()).toString();
        } catch (URISyntaxException e) {
            return uri;
        }
    }

    /**
     * Returns the URI given the location.
     *
     * @param module  Module where the location resides
     * @param location Location
     * @return URI
     */
    public static String getUriFromLocation(Module module, Location location) {
        return getPathFromLocation(module, location).toUri().toString();
    }

    /**
     * Returns the file path given the location.
     *
     * @param module  Module where the location resides
     * @param location Location
     * @return file path
     */
    public static Path getPathFromLocation(Module module, Location location) {
        String filePath = location.lineRange().filePath();

        if (module.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            return module.project().sourceRoot();
        }

        if (module.project().kind() == ProjectKind.BALA_PROJECT) {
            // TODO Check if bala projects can exist within nested modules dir
            return module.project().sourceRoot().resolve("modules")
                    .resolve(module.moduleName().toString())
                    .resolve(filePath);
        }

        if (module.isDefaultModule()) {
            return module.project().sourceRoot().resolve(filePath);
        } else {
            return module.project().sourceRoot()
                    .resolve("modules")
                    .resolve(module.moduleName().moduleNamePart())
                    .resolve(filePath);
        }
    }

    /**
     * Returns the range given the location.
     *
     * @param referencePos Location
     * @return range
     */
    public static Range getRange(Location referencePos) {
        Position start = new Position(
                referencePos.lineRange().startLine().line(), referencePos.lineRange().startLine().offset());
        Position end = new Position(
                referencePos.lineRange().endLine().line(), referencePos.lineRange().endLine().offset());
        return new Range(start, end);
    }
}

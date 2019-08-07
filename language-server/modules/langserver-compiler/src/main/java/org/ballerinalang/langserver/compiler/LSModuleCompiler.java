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
package org.ballerinalang.langserver.compiler;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.prepareCompilerContext;

/**
 * Language server compiler implementation for Ballerina.
 */
public class LSModuleCompiler {

    private LSModuleCompiler() {
    }

    /**
     * Get the BLangPackage for a given program.
     *
     * @param context            Language Server Context
     * @param docManager         Document manager
     * @param preserveWS         Enable preserve whitespace
     * @param errStrategy        custom error strategy class
     * @param compileFullProject updateAndCompileFile full project from the source root
     * @return {@link List}      A list of packages when compile full project
     * @throws LSCompilerException when compilation fails
     */
    public static BLangPackage getBLangPackage(LSContext context,
                                        WorkspaceDocumentManager docManager, boolean preserveWS,
                                        Class<? extends ANTLRErrorStrategy> errStrategy,
                                        boolean compileFullProject) throws LSCompilerException {
        List<BLangPackage> bLangPackages = getBLangPackages(context, docManager, preserveWS, errStrategy,
                compileFullProject, false);
        if (bLangPackages.isEmpty()) {
            throw new LSCompilerException("Couldn't find any compiled artifact!");
        }
        return bLangPackages.get(0);
    }

    /**
     * Get the all ballerina modules for a given project.
     *
     * @param context            Language Server Context
     * @param docManager         Document manager
     * @param preserveWS         Enable preserve whitespace
     * @param errStrategy        Custom error strategy class
     * @return {@link List}      A list of packages when compile full project
     * @throws URISyntaxException when the uri of the source root is invalid
     */
    public static List<BLangPackage> getBLangModules(LSContext context, WorkspaceDocumentManager docManager,
                                              boolean preserveWS, Class<? extends ANTLRErrorStrategy> errStrategy)
                                              throws URISyntaxException {
        String sourceRoot = Paths.get(new URI(context.get(DocumentServiceKeys.SOURCE_ROOT_KEY))).toString();
        PackageRepository pkgRepo = new WorkspacePackageRepository(sourceRoot, docManager);

        CompilerContext compilerContext = prepareCompilerContext(pkgRepo, sourceRoot, preserveWS, docManager);
        Compiler compiler = LSCompilerUtil.getCompiler(context, "", compilerContext, errStrategy);
        return compiler.compilePackages(false);
    }


    /**
     * Get the BLangPackage for a given program.
     *
     * @param context            Language Server Context
     * @param docManager         Document manager
     * @param preserveWS         Enable preserve whitespace
     * @param errStrategy        custom error strategy class
     * @param compileFullProject updateAndCompileFile full project from the source root
     * @param clearProjectModules whether clear current project modules from ls package cache
     * @return {@link List}      A list of packages when compile full project
     * @throws LSCompilerException Whenever compilation fails
     */
    public static List<BLangPackage> getBLangPackages(LSContext context, WorkspaceDocumentManager docManager,
                                               boolean preserveWS, Class<? extends ANTLRErrorStrategy> errStrategy, 
                                               boolean compileFullProject, boolean clearProjectModules) 
            throws LSCompilerException {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Optional<String> unsavedFileId = LSCompilerUtil.getUntitledFileId(uri);
        if (unsavedFileId.isPresent()) {
            // If it is an unsaved file; overrides the file path
            uri = LSCompilerUtil.createTempFile(unsavedFileId.get()).toUri().toString();
            context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        }
        LSDocument sourceDoc = new LSDocument(uri);
        context.put(DocumentServiceKeys.LS_DOCUMENT_KEY, sourceDoc);
        String projectRoot = sourceDoc.getProjectRoot();
        PackageRepository pkgRepo = new WorkspacePackageRepository(projectRoot, docManager);
        List<BLangPackage> packages = new ArrayList<>();
        String pkgName = sourceDoc.getOwnerModule();
        PackageID pkgID;
        String relativeFilePath;

        // If the source file does not exist inside a ballerina module.
        if (pkgName.isEmpty()) {
            Path fileNamePath = sourceDoc.getPath().getFileName();
            relativeFilePath = fileNamePath == null ? "" : fileNamePath.toString();
            pkgID = new PackageID(relativeFilePath);
            pkgName = relativeFilePath;
            // No need to compile the full project for a file which is not inside a module.
            compileFullProject = false;
        } else {
            relativeFilePath = sourceDoc.getProjectRootPath().resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                    .resolve(pkgName).relativize(sourceDoc.getPath())
                    .toString();
            pkgID = generatePackageFromManifest(pkgName, projectRoot);
        }
        CompilerContext compilerContext = prepareCompilerContext(pkgID, pkgRepo, sourceDoc, preserveWS, docManager);

        context.put(DocumentServiceKeys.SOURCE_ROOT_KEY, projectRoot);
        context.put(DocumentServiceKeys.CURRENT_PKG_NAME_KEY, pkgID.getNameComps().stream()
                .map(Name::getValue)
                .collect(Collectors.joining(".")));
        try {
            if (compileFullProject && !projectRoot.isEmpty() && sourceDoc.isWithinProject()) {
                if (clearProjectModules) {
                    // If the flag is set, we remove all the modules in the current project from the LSPackageCache
                    LSPackageCache.getInstance(compilerContext).invalidateProjectModules(sourceDoc.getProjectModules());
                }
                Compiler compiler = LSCompilerUtil.getCompiler(context, relativeFilePath, compilerContext, errStrategy);
                List<BLangPackage> projectPackages = compiler.compilePackages(false);
                packages.addAll(projectPackages);
                Optional<BLangPackage> currentPkg = projectPackages.stream().filter(bLangPackage -> {
                    String name = bLangPackage.packageID.nameComps.stream()
                            .map(Name::getValue).collect(Collectors.joining("."));
                    return context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY).equals(name);
                }).findAny();
                // No need to check the option is existing since the current package always exist
                LSPackageCache.getInstance(compilerContext).invalidate(currentPkg.get().packageID);
            } else {
                Compiler compiler = LSCompilerUtil.getCompiler(context, relativeFilePath, compilerContext, errStrategy);
                BLangPackage bLangPackage = compiler.compile(pkgName);
                LSPackageCache.getInstance(compilerContext).invalidate(bLangPackage.packageID);
                packages.add(bLangPackage);
            }
        } catch (Exception e) {
            throw new LSCompilerException("Compilation failed", e);
        }
        Optional<BLangPackage> currentPackage = filterCurrentPackage(packages, context);
        currentPackage.ifPresent(bLangPackage -> {
            context.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
            context.put(DocumentServiceKeys.CURRENT_PACKAGE_ID_KEY, bLangPackage.packageID);
        });
        return packages;
    }

    private static PackageID generatePackageFromManifest(String pkgName, String projectRoot) {
        Manifest manifest = LSCompilerUtil.getManifest(Paths.get(projectRoot));
        Name orgName = manifest.getProject().getOrgName() == null || manifest.getProject().getOrgName().isEmpty() ?
                Names.ANON_ORG : new Name(manifest.getProject().getOrgName());
        Name version = manifest.getProject().getVersion() == null || manifest.getProject().getVersion().isEmpty() ?
                Names.DEFAULT_VERSION : new Name(manifest.getProject().getVersion());
        return new PackageID(orgName, new Name(pkgName), version);
    }

    /**
     * Get current package by given file name.
     *
     * @param packages list of packages to be searched
     * @param context  LSContext
     * @return {@link Optional} current package
     */
    private static Optional<BLangPackage> filterCurrentPackage(List<BLangPackage> packages, LSContext context) {
        String currentPkg = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        return packages.stream()
                .filter(bLangPackage -> bLangPackage.packageID.name.getValue().equals(currentPkg))
                .findAny();
    }
}


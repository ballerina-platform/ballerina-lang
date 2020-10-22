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

import io.ballerina.compiler.api.impl.SymbolFactory;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.common.LSDocumentIdentifierImpl;
import org.ballerinalang.langserver.compiler.config.LSClientConfig;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.prepareCompilerContext;

/**
 * Language server compiler implementation for Ballerina.
 */
public class LSModuleCompiler {
    protected LSModuleCompiler() {
    }

    /**
     * Get the BLangPackage for a given program.
     *
     * @param context              Language Server Context
     * @param docManager           Document manager
     * @param compileFullProject   updateAndCompileFile full project from the source root
     * @param stopOnSemanticErrors whether stop compilation on semantic errors
     * @return {@link List}      A list of packages when compile full project
     * @throws CompilationFailedException when compilation fails
     */
    public static BLangPackage getBLangPackage(LSContext context, WorkspaceDocumentManager docManager,
                                               boolean compileFullProject, boolean stopOnSemanticErrors)
            throws CompilationFailedException {
        List<BLangPackage> bLangPackages = getBLangPackages(context, docManager,
                compileFullProject, false, stopOnSemanticErrors);
        return bLangPackages.get(0);
    }

    /**
     * Get the all ballerina modules for a given project.
     *
     * @param context              Language Server Context
     * @param docManager           Document manager
     * @param stopOnSemanticErrors Whether stop compilation on semantic errors
     * @return {@link List}      A list of packages when compile full project
     * @throws URISyntaxException         when the uri of the source root is invalid
     * @throws CompilationFailedException when the compiler throws any error
     */
    public static List<BLangPackage> getBLangModules(LSContext context, WorkspaceDocumentManager docManager,
                                                     boolean stopOnSemanticErrors)
            throws URISyntaxException, CompilationFailedException {
        String sourceRoot = Paths.get(new URI(context.get(DocumentServiceKeys.SOURCE_ROOT_KEY))).toString();
        PackageRepository pkgRepo = new WorkspacePackageRepository(sourceRoot, docManager);

        CompilerContext compilerContext = prepareCompilerContext(pkgRepo, sourceRoot, docManager, stopOnSemanticErrors);
        Compiler compiler = LSCompilerUtil.getCompiler(context, compilerContext);
        return compilePackagesSafe(compiler, sourceRoot, false, context);
    }

    /**
     * Get the BLangPackage for a given program.
     *
     * @param context              Language Server Context
     * @param docManager           Document manager
     * @param compileFullProject   updateAndCompileFile full project from the source root
     * @param clearProjectModules  whether clear current project modules from ls package cache
     * @param stopOnSemanticErrors whether stop compilation on semantic errors
     * @return {@link List}      A list of packages when compile full project
     * @throws CompilationFailedException Whenever compilation fails
     */
    public static List<BLangPackage> getBLangPackages(LSContext context, WorkspaceDocumentManager docManager,
                                                      boolean compileFullProject, boolean clearProjectModules,
                                                      boolean stopOnSemanticErrors)
            throws CompilationFailedException {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Optional<String> unsavedFileId = LSCompilerUtil.getUntitledFileId(uri);
        if (unsavedFileId.isPresent()) {
            // If it is an unsaved file; overrides the file path
            uri = LSCompilerUtil.createTempFile(unsavedFileId.get()).toUri().toString();
            context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        }
        LSDocumentIdentifier sourceDoc = new LSDocumentIdentifierImpl(uri);
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
        context.put(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY, relativeFilePath);
        CompilerContext compilerContext = prepareCompilerContext(pkgID, pkgRepo, sourceDoc, docManager,
                stopOnSemanticErrors);

        context.put(DocumentServiceKeys.SOURCE_ROOT_KEY, projectRoot);
        context.put(DocumentServiceKeys.CURRENT_PKG_NAME_KEY, pkgID.getNameComps().stream()
                .map(Name::getValue)
                .collect(Collectors.joining(".")));

        if (compileFullProject && !projectRoot.isEmpty() && sourceDoc.isWithinProject()) {
            if (clearProjectModules) {
                // If the flag is set, we remove all the modules in the current project from the LSPackageCache
                LSPackageCache.getInstance(compilerContext).invalidateProjectModules(sourceDoc.getProjectModules());
            }
            Compiler compiler = LSCompilerUtil.getCompiler(context, compilerContext);
            List<BLangPackage> projectPackages = compilePackagesSafe(compiler, projectRoot, false, context);
            packages.addAll(projectPackages);
            Optional<BLangPackage> currentPkg = projectPackages.stream().filter(bLangPackage -> {
                String name = bLangPackage.packageID.nameComps.stream()
                        .map(Name::getValue).collect(Collectors.joining("."));
                return context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY).equals(name);
            }).findAny();
            // No need to check the option is existing since the current package always exist
            LSPackageCache.getInstance(compilerContext).invalidate(currentPkg.get().packageID);
        } else {
            Compiler compiler = LSCompilerUtil.getCompiler(context, compilerContext);
            BLangPackage bLangPackage = compileSafe(compiler, projectRoot, pkgName, context);
            LSPackageCache.getInstance(compilerContext).invalidate(bLangPackage.packageID);
            packages.add(bLangPackage);
        }
        if (packages.isEmpty()) {
            throw new CompilationFailedException("Couldn't find any compiled artifact!");
        }
        Optional<BLangPackage> currentPackage = filterCurrentPackage(packages, context);
        currentPackage.ifPresent(bLangPackage -> {
            context.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
            context.put(DocumentServiceKeys.CURRENT_MODULE_KEY, SymbolFactory.createModuleSymbol(bLangPackage.symbol,
                    bLangPackage.symbol.name.getValue()));
            context.put(DocumentServiceKeys.CURRENT_PACKAGE_ID_KEY, bLangPackage.packageID);
        });
        return packages;
    }

    /**
     * Compile a single BLangPackage.
     *
     * @param compiler    {@link Compiler}
     * @param projectRoot project root
     * @param pkgName     package name or file name
     * @param context     {@link LSContext}
     * @return {@link BLangPackage}
     * @throws CompilationFailedException thrown when compilation failed
     */
    protected static BLangPackage compileSafe(Compiler compiler, String projectRoot, String pkgName, LSContext context)
            throws CompilationFailedException {
        LSCompilerCache.Key key = new LSCompilerCache.Key(projectRoot, context);
        LSClientConfig config = LSClientConfigHolder.getInstance().getConfig();
        try {
            long startTime = 0L;
            if (config.isTraceLogEnabled()) {
                startTime = System.nanoTime();
            }
            boolean isCacheSupported = context.get(DocumentServiceKeys.IS_CACHE_SUPPORTED) != null &&
                    context.get(DocumentServiceKeys.IS_CACHE_SUPPORTED);
            boolean isOutdatedSupported = context.get(DocumentServiceKeys.IS_CACHE_OUTDATED_SUPPORTED) != null &&
                    context.get(DocumentServiceKeys.IS_CACHE_OUTDATED_SUPPORTED);
            BLangPackage bLangPackage;
            if (isCacheSupported) {
                LSCompilerCache.CacheEntry cacheEntry = LSCompilerCache.getPackage(key, context);
                if (cacheEntry != null && (isOutdatedSupported || !cacheEntry.isOutdated())) {
                    if (config.isTraceLogEnabled()) {
                        long endTime = System.nanoTime();
                        long eTime = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
                        LSClientLogger.logTrace("Operation '" + context.getOperation().getName() + "' {projectRoot: '" +
                                projectRoot + "'}, served through cache within " + eTime +
                                "ms");
                    }
                    // Cache hit
                    return cacheEntry.get().getLeft();
                }
                bLangPackage = compiler.compile(pkgName);
                LSCompilerCache.putPackage(key, bLangPackage, context);
            } else {
                bLangPackage = compiler.compile(pkgName);
            }
            if (config.isTraceLogEnabled()) {
                long endTime = System.nanoTime();
                long eTime = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
                LSClientLogger.logTrace("Operation '" + context.getOperation().getName() + "' {projectRoot: '" +
                        projectRoot + "'}, compilation took " + eTime + "ms");
            }
            return bLangPackage;
        } catch (RuntimeException e) {
            // NOTE: Remove current CompilerContext to try out a fresh CompilerContext next time
            // to avoid issues of reusing it.
//            LSContextManager.getInstance().removeCompilerContext(projectRoot);
            LSCompilerCache.markOutDated(key);
            throw new CompilationFailedException("Oh no, something really went wrong. Bad. Sad.", e);
        }
    }

    /**
     * Compile all project modules and get BLangPackages.
     *
     * @param compiler    {@link Compiler}
     * @param projectRoot project root
     * @param isBuild     if `True` builds all packages
     * @param context     {@link LSContext}
     * @return a list of {@link BLangPackage}
     * @throws CompilationFailedException thrown when compilation failed
     */
    protected static List<BLangPackage> compilePackagesSafe(Compiler compiler, String projectRoot, boolean isBuild,
                                                            LSContext context)
            throws CompilationFailedException {
        LSCompilerCache.Key key = new LSCompilerCache.Key(projectRoot, context);
        LSClientConfig config = LSClientConfigHolder.getInstance().getConfig();
        try {
            long startTime = 0L;
            if (config.isTraceLogEnabled()) {
                startTime = System.nanoTime();
            }
            boolean isCacheSupported = context.get(DocumentServiceKeys.IS_CACHE_SUPPORTED) != null &&
                    context.get(DocumentServiceKeys.IS_CACHE_SUPPORTED);
            boolean isOutdatedSupported = context.get(DocumentServiceKeys.IS_CACHE_OUTDATED_SUPPORTED) != null &&
                    context.get(DocumentServiceKeys.IS_CACHE_OUTDATED_SUPPORTED);
            List<BLangPackage> bLangPackages;
            if (isCacheSupported) {
                LSCompilerCache.CacheEntry cacheEntry = LSCompilerCache.getPackages(key, context);
                if (cacheEntry != null && (isOutdatedSupported || !cacheEntry.isOutdated())) {
                    if (config.isTraceLogEnabled()) {
                        long endTime = System.nanoTime();
                        long eTime = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
                        LSClientLogger.logTrace("Operation '" + context.getOperation().getName() + "' {projectRoot: '" +
                                projectRoot + "'}, served through cache within " + eTime +
                                "ms");
                    }
                    // Cache hit
                    return cacheEntry.get().getRight();
                }
                bLangPackages = compiler.compilePackages(isBuild);
                LSCompilerCache.putPackages(key, bLangPackages, context);
            } else {
                bLangPackages = compiler.compilePackages(isBuild);
            }
            if (config.isTraceLogEnabled()) {
                long endTime = System.nanoTime();
                long eTime = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
                LSClientLogger.logTrace("Operation '" + context.getOperation().getName() + "' {projectRoot: '" +
                        projectRoot + "'}, compilation took " + eTime + "ms");
            }
            return bLangPackages;
        } catch (RuntimeException e) {
            // NOTE: Remove current CompilerContext to try out a fresh CompilerContext next time
            // to avoid issues of reusing it.
//            LSContextManager.getInstance().removeCompilerContext(projectRoot);
            LSCompilerCache.markOutDated(key);
            throw new CompilationFailedException("Oh no, something really went wrong. Bad. Sad.", e);
        }
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


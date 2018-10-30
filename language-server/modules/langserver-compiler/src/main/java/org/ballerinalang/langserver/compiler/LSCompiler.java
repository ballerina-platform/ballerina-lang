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

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.workspace.ExtendedWorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.prepareCompilerContext;

/**
 * Language server compiler implementation for Ballerina.
 */
public class LSCompiler {

    private static final String BAL_EXTENSION = ".bal";

    private final WorkspaceDocumentManager documentManager;

    /**
     * Special LS Compiler instance with the Extended Document Manager to compile the content.
     */
    private static final LSCompiler INSTANCE = new LSCompiler(ExtendedWorkspaceDocumentManagerImpl.getInstance());

    /**
     * Returns a new LS Compiler instance with this document manager.
     *
     * @param documentManager document manager
     */
    public LSCompiler(WorkspaceDocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    /**
     * Returns a BallerinaFile compiling in-memory content.
     *
     * @param content content to be compiled
     * @param phase   {@link CompilerPhase} for the compiler
     * @return {@link BallerinaFile} containing the compiled package
     * @throws LSCompilerException when compiler error occurred
     */
    public static BallerinaFile compileContent(String content, CompilerPhase phase) throws LSCompilerException {
        java.nio.file.Path filePath = LSCompilerUtil.createTempFile(LSCompilerUtil.UNTITLED_BAL);
        ExtendedWorkspaceDocumentManagerImpl documentManager = ExtendedWorkspaceDocumentManagerImpl.getInstance();
        Optional<Lock> exModeLock = documentManager.enableExplicitMode(filePath);
        Optional<Lock> fileLock = Optional.empty();
        try {
            fileLock = documentManager.updateFile(filePath, content);
            BallerinaFile bFile = INSTANCE.compileFile(filePath, phase);
            documentManager.closeFile(filePath);
            return bFile;
        } catch (WorkspaceDocumentException e) {
            throw new LSCompilerException("Error occurred while compiling file:" + filePath.toString(), e);
        } finally {
            documentManager.disableExplicitMode(exModeLock.orElse(null));
            fileLock.ifPresent(Lock::unlock);
        }
    }

    /**
     * Compile file.
     *
     * @param filePath file {@link Path} of the file
     * @param phase    {@link CompilerPhase} for the compiler
     * @return {@link BallerinaFile} containing compiled package
     */
    public BallerinaFile compileFile(Path filePath, CompilerPhase phase) {
        String sourceRoot = LSCompilerUtil.getSourceRoot(filePath);
        String packageName = LSCompilerUtil.getPackageNameForGivenFile(sourceRoot, filePath.toString());
        LSDocument sourceDocument = new LSDocument(filePath.toUri().toString(), sourceRoot);

        PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, documentManager);
        PackageID packageID;
        if ("".equals(packageName)) {
            Path path = filePath.getFileName();
            if (path != null) {
                packageName = path.toString();
                packageID = new PackageID(packageName);
            } else {
                packageID = new PackageID(Names.ANON_ORG, new Name(packageName), Names.DEFAULT_VERSION);
            }
        } else {
            packageID = generatePackageFromManifest(packageName, sourceRoot);
        }
        CompilerContext context = prepareCompilerContext(packageID, packageRepository, sourceDocument,
                                                                        true, documentManager, phase);

        // In order to capture the syntactic errors, need to go through the default error strategy
        context.put(DefaultErrorStrategy.class, null);
        BLangPackage bLangPackage = null;
        if (context.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
            ((CollectDiagnosticListener) context.get(DiagnosticListener.class)).clearAll();
        }
        boolean isProjectDir = (LSCompilerUtil.isBallerinaProject(sourceRoot, filePath.toUri().toString()));
        try {
            BLangDiagnosticLog.getInstance(context).errorCount = 0;
            Compiler compiler = Compiler.getInstance(context);
            bLangPackage = compiler.compile(packageName);
            LSPackageCache.getInstance(context).invalidate(bLangPackage.packageID);
        } catch (RuntimeException e) {
            // Ignore.
        }
        BallerinaFile bfile = new BallerinaFile();
        bfile.setBallerinaProject(isProjectDir);
        bfile.setBLangPackage(bLangPackage);
        if (context.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
            List<Diagnostic> diagnostics = ((CollectDiagnosticListener) context.get(DiagnosticListener.class))
                    .getDiagnostics();
            bfile.setDiagnostics(diagnostics);
        } else {
            bfile.setDiagnostics(new ArrayList<>());
        }
        return bfile;
    }

    /**
     * Updates content and compile file.
     *
     * @param content         content need to be updated
     * @param filePath        file {@link Path} of the file
     * @param phase           {@link CompilerPhase} for the compiler
     * @param documentManager document manager
     * @return {@link BallerinaFile} containing compiled package
     * @throws LSCompilerException when compiler error occurred
     */
    public BallerinaFile updateAndCompileFile(Path filePath, String content, CompilerPhase phase,
                                              WorkspaceDocumentManager documentManager)
            throws LSCompilerException {
        Optional<Lock> lock = Optional.empty();
        try {
            lock = documentManager.updateFile(filePath, content);
            return this.compileFile(filePath, phase);
        } catch (WorkspaceDocumentException e) {
            throw new LSCompilerException(
                    "Error occurred while compiling the content in file path: " + filePath.toString(), e
            );
        } finally {
            lock.ifPresent(Lock::unlock);
        }
    }

    /**
     * Get the BLangPackage for a given program.
     *
     * @param context                   Language Server Context
     * @param docManager                Document manager
     * @param preserveWS                Enable preserve whitespace
     * @param errStrategy               custom error strategy class
     * @param compileFullProject        updateAndCompileFile full project from the source root
     * @return {@link Either}           Either single BLang Package or a list of packages when compile full project
     */
    public Either<List<BLangPackage>, BLangPackage> getBLangPackage(LSContext context,
                                              WorkspaceDocumentManager docManager, boolean preserveWS,
                                              Class errStrategy,
                                              boolean compileFullProject) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Optional<String> unsavedFileId = LSCompilerUtil.getUntitledFileId(uri);
        if (unsavedFileId.isPresent()) {
            // If it is an unsaved file; overrides the file path
            uri = LSCompilerUtil.createTempFile(unsavedFileId.get()).toUri().toString();
            context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        }
        LSDocument sourceDoc = new LSDocument(uri);
        String sourceRoot = sourceDoc.getSourceRoot();

        PackageRepository pkgRepo = new WorkspacePackageRepository(sourceRoot, docManager);
        List<BLangPackage> packages = new ArrayList<>();
        if (compileFullProject && !sourceRoot.isEmpty()) {
            File projectDir = new File(sourceRoot);
            Arrays.stream(projectDir.listFiles()).forEach(
                    file -> {
                        if (isBallerinaPackage(file) || isBallerinaFile(file)) {
                            Path filePath = sourceDoc.getPath();
                            Path fileNamePath = filePath.getFileName();
                            final String fileName = (fileNamePath != null) ? fileNamePath.toString() : "";
                            PackageID packageID = new PackageID(fileName);
                            CompilerContext compilerContext = prepareCompilerContext(packageID, pkgRepo, sourceDoc,
                                    preserveWS, docManager);
                            Compiler compiler = LSCompilerUtil.getCompiler(context, fileName, compilerContext, 
                                    errStrategy);
                            BLangPackage bLangPackage = compiler.compile(file.getName());
                            packages.add(bLangPackage);
                            LSPackageCache.getInstance(compilerContext).invalidate(bLangPackage.packageID);
                        }
                    }
            );
            return Either.forLeft(packages);
        } else {
            PackageID pkgID;
            String pkgName = LSCompilerUtil.getPackageNameForGivenFile(sourceRoot, sourceDoc.getPath().toString());
            String relativeFilePath;
            if (pkgName.isEmpty()) {
                Path fileNamePath = sourceDoc.getPath().getFileName();
                relativeFilePath = fileNamePath == null ? "" : fileNamePath.toString();
                pkgID = new PackageID(relativeFilePath);
                pkgName = relativeFilePath;
            } else {
                relativeFilePath = sourceDoc.getSourceRootPath().resolve(pkgName).relativize(sourceDoc.getPath())
                        .toString();
                pkgID = generatePackageFromManifest(pkgName, sourceRoot);
            }
            CompilerContext compilerContext = prepareCompilerContext(pkgID, pkgRepo, sourceDoc, preserveWS, docManager);
            Compiler compiler = LSCompilerUtil.getCompiler(context, relativeFilePath, compilerContext, errStrategy);
            BLangPackage bLangPackage = compiler.compile(pkgName);
            LSPackageCache.getInstance(compilerContext).invalidate(bLangPackage.packageID);

            return Either.forRight(bLangPackage);
        }
    }

    private boolean isBallerinaPackage(File dir) {
        if (!dir.isDirectory() || dir.getName().startsWith(".")) {
            return false;
        }
        File[] files = dir.listFiles((parent, name) -> isBallerinaFile(parent.toPath().resolve(name).toFile()));
        return files != null && files.length > 0;
    }

    private boolean isBallerinaFile(File file) {
        return !file.isDirectory() && file.getName().endsWith(BAL_EXTENSION);
    }
    
    private PackageID generatePackageFromManifest(String pkgName, String sourceRoot) {
        Manifest manifest = LSCompilerUtil.getManifest(Paths.get(sourceRoot));
        Name orgName = manifest.getName() == null || manifest.getName().isEmpty() ?
                Names.ANON_ORG : new Name(manifest.getName());
        Name version = manifest.getVersion() == null || manifest.getVersion().isEmpty() ?
                Names.DEFAULT_VERSION : new Name(manifest.getVersion());
        return new PackageID(orgName, new Name(pkgName), version);
    }
}


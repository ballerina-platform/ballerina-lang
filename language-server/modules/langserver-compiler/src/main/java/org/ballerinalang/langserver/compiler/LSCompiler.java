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

import com.google.common.io.Files;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.common.CustomErrorStrategyFactory;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.repository.LangServerFSProgramDirectory;
import org.ballerinalang.langserver.compiler.workspace.repository.LangServerFSProjectDirectory;
import org.ballerinalang.langserver.compiler.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Language server compiler implementation for Ballerina.
 */
public class LSCompiler {

    private static final Logger logger = LoggerFactory.getLogger(LSCompiler.class);

    public static final String UNTITLED_BAL = "untitled.bal";

    private static Path untitledProjectPath;

    private static final Pattern untitledFilePattern =
            Pattern.compile("^(?:file:\\/\\/)?\\/temp\\/(.*)\\/untitled.bal");

    static {
        // Here we will create a tmp directory as the untitled project repo.
        File untitledDir = Files.createTempDir();
        untitledProjectPath = untitledDir.toPath();
        // Now lets create a empty untitled.bal to fool compiler.
        File untitledBal = new File(Paths.get(untitledProjectPath.toString(), UNTITLED_BAL).toString());
        try {
            boolean created = untitledBal.createNewFile();
            if (created && logger.isDebugEnabled()) {
                logger.debug("A temp file created: " + untitledBal.toURI());
            }
        } catch (IOException e) {
            logger.error("Unable to create untitled project directory, " +
                                 "unsaved files might not work properly.");
        }
    }

    private final WorkspaceDocumentManager documentManager;

    public LSCompiler(WorkspaceDocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public BallerinaFile compileContent(String content, CompilerPhase phase) {
        return compileContent(content, phase, true);
    }

    /**
     * Returns a BallerinaFile compiling in-memory content.
     *
     * @param content
     * @param phase
     * @return
     */
    public BallerinaFile compileContent(String content, CompilerPhase phase, boolean preserveWhitespace) {
        java.nio.file.Path filePath = createAndGetTempFile(UNTITLED_BAL);
        Optional<Lock> fileLock = documentManager.lockFile(filePath);
        try {
            if (documentManager.isFileOpen(filePath)) {
                documentManager.updateFile(filePath, content);
            } else {
                documentManager.openFile(filePath, content);
            }

            BallerinaFile ballerinaFile = LSCompiler.compile(filePath, CompilerPhase.DEFINE, documentManager,
                                                             preserveWhitespace);

            documentManager.closeFile(filePath);
            return ballerinaFile;
        } finally {
            fileLock.ifPresent(Lock::unlock);
        }
    }

    public BallerinaFile compileContent(String content, Path filePath, CompilerPhase phase) {
        return compileContent(content, filePath, phase, true);
    }

    public BallerinaFile compileContent(String content, Path filePath, CompilerPhase phase,
                                               boolean preserveWhitespace) {
        return compileContent(content, filePath, phase, documentManager, preserveWhitespace);
    }

    public static BallerinaFile compileContent(String content, Path filePath, CompilerPhase phase,
                                               WorkspaceDocumentManager documentManager, boolean preserveWhitespace) {
        Optional<Lock> lock = documentManager.lockFile(filePath);
        try {
            if (documentManager.isFileOpen(filePath)) {
                documentManager.updateFile(filePath, content);
            } else {
                documentManager.openFile(filePath, content);
            }
            return LSCompiler.compile(filePath, phase, documentManager, preserveWhitespace);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
    }

    /**
     * Prepare the compiler context.
     *
     * @param packageID          Package Name
     * @param packageRepository  Package Repository
     * @param sourceRoot         LSDocument for Source Root
     * @param preserveWhitespace Preserve Whitespace
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(PackageID packageID, PackageRepository packageRepository,
                                                         LSDocument sourceRoot, boolean preserveWhitespace,
                                                         WorkspaceDocumentManager documentManager) {
        return prepareCompilerContext(packageID, packageRepository, sourceRoot, preserveWhitespace, documentManager,
                                      CompilerPhase.TAINT_ANALYZE);
    }

    /**
     * Prepare the compiler context.
     *
     * @param packageID          Package ID
     * @param packageRepository  Package Repository
     * @param sourceRoot         LSDocument for Source Root
     * @param preserveWhitespace Preserve Whitespace
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(PackageID packageID, PackageRepository packageRepository,
                                                         LSDocument sourceRoot, boolean preserveWhitespace,
                                                         WorkspaceDocumentManager documentManager,
                                                         CompilerPhase compilerPhase) {
        LSContextManager lsContextManager = LSContextManager.getInstance();
        CompilerContext context = lsContextManager.getCompilerContext(packageID, sourceRoot.getSourceRoot());
        context.put(PackageRepository.class, packageRepository);
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot.getSourceRoot());

        if (null == compilerPhase) {
            throw new AssertionError("Compiler Phase can not be null.");
        }

        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.valueOf(preserveWhitespace).toString());

        // In order to capture the syntactic errors, need to go through the default error strategy
        context.put(DefaultErrorStrategy.class, null);

        if (isProjectDir(sourceRoot.getSourceRoot(), sourceRoot.getURIString())) {
            context.put(SourceDirectory.class,
                        LangServerFSProjectDirectory.getInstance(context, sourceRoot.getSourceRootPath(),
                                                                 documentManager));
        } else {
            context.put(SourceDirectory.class,
                        LangServerFSProgramDirectory.getInstance(context, sourceRoot.getSourceRootPath(),
                                                                 documentManager));
        }
        return context;
    }

    private static BallerinaFile compile(Path path, CompilerPhase phase, WorkspaceDocumentManager documentManager,
                                         boolean preserveWhiteSpace) {
        String sourceRoot = getSourceRoot(path);
        String pkgName = getPackageNameForGivenFile(sourceRoot, path.toString());
        LSDocument sourceDocument = new LSDocument();
        sourceDocument.setUri(path.toUri().toString());
        sourceDocument.setSourceRoot(sourceRoot);

        PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, documentManager);
        PackageID packageID = new PackageID(Names.ANON_ORG, new Name(pkgName), Names.DEFAULT_VERSION);
        if ("".equals(pkgName)) {
            Path filePath = path.getFileName();
            if (filePath != null) {
                pkgName = filePath.toString();
                packageID = new PackageID(pkgName);
            }
        }
        CompilerContext context = prepareCompilerContext(packageID, packageRepository,
                                                         sourceDocument, preserveWhiteSpace,
                                                         documentManager, phase);

        // In order to capture the syntactic errors, need to go through the default error strategy
        context.put(DefaultErrorStrategy.class, null);
        return LSCompiler.compile(pkgName, path, phase, context);
    }

    private static BallerinaFile compile(String packageName, Path path, CompilerPhase phase, CompilerContext context) {
        CompilerOptions options = CompilerOptions.getInstance(context);
        String sourceRoot = options.get(PROJECT_DIR);
        BLangPackage bLangPackage = null;
        if (context.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
            ((CollectDiagnosticListener) context.get(DiagnosticListener.class)).clearAll();
        }
        boolean isProjectDir = (LSCompiler.isProjectDir(sourceRoot, path.toUri().toString()));
        try {
            BLangDiagnosticLog.getInstance(context).errorCount = 0;
            Compiler compiler = Compiler.getInstance(context);
            bLangPackage = compiler.compile(packageName);
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
     * Get the BLangPackage for a given program.
     *
     * @param context             Language Server Context
     * @param docManager          Document manager
     * @param preserveWhitespace  Enable preserve whitespace
     * @param customErrorStrategy custom error strategy class
     * @param compileFullProject  compile full project from the source root
     * @return {@link BLangPackage} BLang Package
     */
    public static List<BLangPackage> getBLangPackage(LSContext context,
                                                                  WorkspaceDocumentManager docManager,
                                                                  boolean preserveWhitespace, Class customErrorStrategy,
                                                                  boolean compileFullProject) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        String unsavedFileId = LSCompiler.getUnsavedFileIdOrNull(uri);
        if (unsavedFileId != null) {
            // if it is an unsaved file; overrides the file path
            uri = LSCompiler.createAndGetTempFile(unsavedFileId).toUri().toString();
            context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        }
        LSDocument document = new LSDocument(uri);
        Path filePath = getPath(document);
        Path fileNamePath = filePath.getFileName();
        String fileName = "";
        if (fileNamePath != null) {
            fileName = fileNamePath.toString();
        }

        String sourceRoot = LSCompiler.getSourceRoot(filePath);
        String pkgName = LSCompiler.getPackageNameForGivenFile(sourceRoot, filePath.toString());
        LSDocument sourceDocument = new LSDocument();
        sourceDocument.setUri(uri);
        sourceDocument.setSourceRoot(sourceRoot);

        PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, docManager);
        List<BLangPackage> packages = new ArrayList<>();
        if (compileFullProject) {
            if (!sourceRoot.isEmpty()) {
                File projectDir = new File(sourceRoot);
                File[] files = projectDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if ((file.isDirectory() && !file.getName().startsWith(".")) ||
                                (!file.isDirectory() && file.getName().endsWith(".bal"))) {
                            PackageID packageID = new PackageID(fileName);
                            Compiler compiler = getCompiler(context, fileName, packageID, packageRepository,
                                                            sourceDocument, preserveWhitespace, customErrorStrategy,
                                                            docManager);
                            packages.add(compiler.compile(file.getName()));
                        }
                    }
                }
            }
        } else {
            PackageID packageID;
            if ("".equals(pkgName)) {
                packageID = new PackageID(fileName);
                pkgName = fileName;
            } else {
                packageID = new PackageID(Names.ANON_ORG, new Name(pkgName), Names.DEFAULT_VERSION);
            }
            Compiler compiler = getCompiler(context, fileName, packageID, packageRepository, sourceDocument,
                                            preserveWhitespace, customErrorStrategy, docManager);
            packages.add(compiler.compile(pkgName));
        }
        return packages;
    }


    /**
     * Find project root directory.
     *
     * @param parentDir current parent directory
     * @return {@link String} project root | null
     */
    public static String findProjectRoot(String parentDir) {
        Path path = Paths.get(parentDir);
        if (!RepoUtils.hasProjectRepo(path)) {
            return null;
        }
        path = path.resolve(ProjectDirConstants.DOT_BALLERINA_DIR_NAME);
        if (java.nio.file.Files.exists(path)) {
            return parentDir;
        }

        List<String> pathParts = Arrays.asList(parentDir.split(Pattern.quote(File.separator)));
        if (pathParts.size() > 0) {
            List<String> dirPathParts = pathParts.subList(0, pathParts.size() - 1);
            if (dirPathParts.size() > 0) {
                String rootFolder = String.join(File.separator, dirPathParts);
                return findProjectRoot(rootFolder);
            } else {
                return null;
            }
        }

        return null;
    }

    /**
     * Get compiler for the given context and file.
     *
     * @param context             Language server context
     * @param fileName            File name which is currently open
     * @param packageID         Package ID
     * @param packageRepository   package repository
     * @param sourceRoot          LSDocument for root path of the source
     * @param preserveWhitespace  enable/disable preserve white space in compiler
     * @param customErrorStrategy custom error strategy class
     * @return {@link Compiler} ballerina compiler
     */
    private static Compiler getCompiler(LSContext context, String fileName, PackageID packageID,
                                        PackageRepository packageRepository, LSDocument sourceRoot,
                                        boolean preserveWhitespace, Class customErrorStrategy,
                                        WorkspaceDocumentManager documentManager) {
        CompilerContext compilerContext =
                LSCompiler.prepareCompilerContext(packageID, packageRepository, sourceRoot,
                                                  preserveWhitespace, documentManager);
        context.put(DocumentServiceKeys.FILE_NAME_KEY, fileName);
        context.put(DocumentServiceKeys.COMPILER_CONTEXT_KEY, compilerContext);
        context.put(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY, new LSServiceOperationContext());
        if (customErrorStrategy != null) {
            compilerContext.put(DefaultErrorStrategy.class,
                                CustomErrorStrategyFactory.getCustomErrorStrategy(customErrorStrategy, context));
        }
        BLangDiagnosticLog.getInstance(compilerContext).errorCount = 0;
        return Compiler.getInstance(compilerContext);
    }


    /**
     * Get the source root for the given package.
     *
     * @param filePath current file's path
     * @return {@link String} program directory path
     */
    public static String getSourceRoot(Path filePath) {
        if (filePath == null || filePath.getParent() == null) {
            return null;
        }
        Path parentPath = filePath.getParent();
        if (parentPath == null) {
            return null;
        }

        String fileRoot = findProjectRoot(parentPath.toString());
        return fileRoot != null ? fileRoot : parentPath.toString();
    }

    /**
     * Check whether given directory is a project dir.
     *
     * @param root root path
     * @return {@link Boolean} true if project dir, else false
     */
    public static boolean isProjectDir(String root, String fileUri) {
        return findProjectRoot(root) != null;
    }


    /**
     * Get the package name for given file.
     *
     * @param sourceRoot source root
     * @param filePath   full path of the file
     * @return {@link String} package name
     */
    public static String getPackageNameForGivenFile(String sourceRoot, String filePath) {
        String packageName = "";
        String packageStructure = filePath.substring(sourceRoot.length() + 1, filePath.length());
        String[] splittedPackageStructure = packageStructure.split(Pattern.quote(File.separator));
        if (splittedPackageStructure.length > 0 && !splittedPackageStructure[0].endsWith(".bal")) {
            packageName = packageStructure.split(Pattern.quote(File.separator))[0];
        }
        return packageName;
    }

    /**
     * Create and returns temp file.
     *
     * @param tempFileId temp file id
     * @return Path
     */
    public static Path createAndGetTempFile(String tempFileId) {
        if (UNTITLED_BAL.equals(tempFileId)) {
            return Paths.get(untitledProjectPath.toString(), tempFileId);
        }
        File tempInnerFolder = new File(Paths.get(untitledProjectPath.toString(), tempFileId).toString());
        File untitledBal = new File(Paths.get(tempInnerFolder.toString(), UNTITLED_BAL).toString());
        if (!untitledBal.exists()) {
            try {
                boolean mkdir = tempInnerFolder.mkdir();
                if (mkdir && logger.isDebugEnabled()) {
                    logger.debug("Temp directory created: " + tempInnerFolder.toURI());
                }
                boolean newFile = untitledBal.createNewFile();
                if (newFile && logger.isDebugEnabled()) {
                    logger.debug("Temp file created: " + untitledBal.toURI());
                }
            } catch (IOException e) {
                logger.error("Unable to create untitled project directory, unsaved files might not work properly.");
            }
        }
        return Paths.get(tempInnerFolder.toString(), UNTITLED_BAL);
    }

    /**
     * Returns unsaved file id or null.
     *
     * @param filePath file path
     * @return file id
     */
    public static String getUnsavedFileIdOrNull(String filePath) {
        Matcher pkgMatcher = untitledFilePattern.matcher(filePath);
        return (pkgMatcher.find()) ? pkgMatcher.group(1) : null;
    }


    /**
     * Common utility to get a Path from the given uri string.
     *
     * @param document LSDocument object of the file
     * @return {@link Path}     Path of the uri
     */
    private static Path getPath(LSDocument document) {
        Path path = null;
        try {
            path = document.getPath();
        } catch (URISyntaxException | MalformedURLException e) {
            // Do Nothing
        }

        return path;
    }
}


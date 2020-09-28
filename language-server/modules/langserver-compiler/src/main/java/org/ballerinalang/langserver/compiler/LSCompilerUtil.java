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

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.ballerinalang.langserver.compiler.workspace.repository.LangServerFSProgramDirectory;
import org.ballerinalang.langserver.compiler.workspace.repository.LangServerFSProjectDirectory;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.toml.exceptions.TomlException;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.TOOLING_COMPILATION;

/**
 * Language server compiler implementation for Ballerina.
 */
public class LSCompilerUtil {

    private static final Logger logger = LoggerFactory.getLogger(LSCompilerUtil.class);

    public static final String UNTITLED_BAL = "untitled.bal";

    private static Path untitledProjectPath;

    private static final Pattern untitledFilePattern =
            Pattern.compile(".*[/\\\\]temp[/\\\\](.*)[/\\\\]untitled.bal");

    private static EmptyPrintStream emptyPrintStream;

    static {
        try {
            emptyPrintStream = new EmptyPrintStream();
        } catch (IOException e) {
            logger.error("Unable to create the empty stream.");
        }

        // Here we will create a tmp directory as the untitled project repo.
        File untitledDir = com.google.common.io.Files.createTempDir();
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

    /**
     * Prepare the compiler context.
     *
     * @param packageID            Package Name
     * @param packageRepository    Package Repository
     * @param sourceRoot           The source root of the project
     * @param documentManager      {@link WorkspaceDocumentManager} Document Manager
     * @param compilerPhase        {@link CompilerPhase} Compiler Phase
     * @param stopOnSemanticErrors Whether stop compilation on semantic errors
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(PackageID packageID, PackageRepository packageRepository,
                                                         String sourceRoot,
                                                         WorkspaceDocumentManager documentManager,
                                                         CompilerPhase compilerPhase,
                                                         boolean stopOnSemanticErrors) {
        LSContextManager lsContextManager = LSContextManager.getInstance();
        CompilerContext context = lsContextManager.getCompilerContext(packageID, sourceRoot, documentManager);
        context.put(PackageRepository.class, packageRepository);
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        boolean isExperimentalEnabled = LSClientConfigHolder.getInstance().getConfig().isAllowExperimental();
        options.put(CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(isExperimentalEnabled));

        if (null == compilerPhase) {
            throw new AssertionError("Compiler Phase can not be null.");
        }
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.TRUE.toString());
        options.put(TEST_ENABLED, String.valueOf(true));
        options.put(SKIP_TESTS, String.valueOf(false));
        options.put(TOOLING_COMPILATION, String.valueOf(stopOnSemanticErrors));

        LangServerFSProjectDirectory projectDirectory =
                LangServerFSProjectDirectory.getInstance(Paths.get(sourceRoot), documentManager);
        context.put(SourceDirectory.class, projectDirectory);

        return context;
    }

    /**
     * Prepare the compiler context.
     *
     * @param packageRepository    Package Repository
     * @param sourceRoot           The source root of the project
     * @param documentManager      {@link WorkspaceDocumentManager} Document Manager
     * @param stopOnSemanticErrors Whether stop compilation on semantic errors
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(PackageRepository packageRepository,
                                                         String sourceRoot,
                                                         WorkspaceDocumentManager documentManager,
                                                         boolean stopOnSemanticErrors) {
        return prepareCompilerContext(null, packageRepository, sourceRoot,
                documentManager, CompilerPhase.TAINT_ANALYZE, stopOnSemanticErrors);
    }


    /**
     * Prepare the compiler context. Use this method if you don't have the source root but a LSDocument instance
     * for a lsDocument in the project.
     *
     * @param pkgID                Package ID
     * @param pkgRepo              Package Repository
     * @param lsDocument           LSDocument for Source Root
     * @param docManager           {@link WorkspaceDocumentManager} Document Manager
     * @param compilerPhase        {@link CompilerPhase} Compiler Phase
     * @param stopOnSemanticErrors Whether stop compilation on semantic errors
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(PackageID pkgID, PackageRepository pkgRepo,
                                                         LSDocumentIdentifier lsDocument,
                                                         WorkspaceDocumentManager docManager,
                                                         CompilerPhase compilerPhase, boolean stopOnSemanticErrors) {
        CompilerContext context = prepareCompilerContext(pkgID, pkgRepo, lsDocument.getProjectRoot(), docManager,
                compilerPhase, stopOnSemanticErrors);
        Path sourceRootPath = lsDocument.getProjectRootPath();
        if (lsDocument.isWithinProject()) {
            LangServerFSProjectDirectory projectDirectory =
                    LangServerFSProjectDirectory.getInstance(sourceRootPath, docManager);
            context.put(SourceDirectory.class, projectDirectory);
        } else {
            LangServerFSProgramDirectory programDirectory =
                    LangServerFSProgramDirectory.getInstance(sourceRootPath, docManager);
            context.put(SourceDirectory.class, programDirectory);
        }
        return context;
    }

    /**
     * Prepare the compiler context. Use this method if you don't have the source root but a LSDocument instance
     * for a document in the project.
     *
     * @param packageID            Package Name
     * @param packageRepository    Package Repository
     * @param sourceRoot           LSDocument for Source Root
     * @param documentManager      {@link WorkspaceDocumentManager} Document Manager
     * @param stopOnSemanticErrors Whether stop compilation on semantic errors
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(PackageID packageID, PackageRepository packageRepository,
                                                         LSDocumentIdentifier sourceRoot,
                                                         WorkspaceDocumentManager documentManager,
                                                         boolean stopOnSemanticErrors) {
        return prepareCompilerContext(packageID, packageRepository, sourceRoot, documentManager,
                CompilerPhase.COMPILER_PLUGIN, stopOnSemanticErrors);
    }

    /**
     * Get compiler for the given context and file.
     *
     * @param context             Language server context
     * @param compilerContext     Compiler context
     * @return {@link Compiler}     ballerina compiler
     */
    static Compiler getCompiler(LSContext context, CompilerContext compilerContext) {
        context.put(DocumentServiceKeys.COMPILER_CONTEXT_KEY, compilerContext);
        BLangDiagnosticLog.getInstance(compilerContext).resetErrorCount();
        Compiler compiler = Compiler.getInstance(compilerContext);
        compiler.setOutStream(emptyPrintStream);
        compiler.setErrorStream(emptyPrintStream);
        return compiler;
    }

    /**
     * Get the source root for the given package.
     *
     * @param filePath current file's path
     * @return {@link String} program directory path
     */
    public static String getProjectRoot(Path filePath) {
        if (filePath == null || filePath.getParent() == null) {
            return null;
        }
        Path parentPath = filePath.getParent();
        Path projectRoot = ProjectDirs.findProjectRoot(Paths.get(parentPath.toString()));
        return projectRoot != null ? projectRoot.toString() : parentPath.toString();
    }

    /**
     * Get the project dir for given file.
     *
     * @param filePath file path
     * @return {@link String} project directory path or null if not in a project
     */
    public static String getProjectDir(Path filePath) {
        if (filePath == null || filePath.getParent() == null) {
            return null;
        }
        Path parentPath = filePath.getParent();
        Path projectRoot = ProjectDirs.findProjectRoot(Paths.get(parentPath.toString()));
        return projectRoot == null ? null : projectRoot.toString();
    }

    /**
     * Returns top-level module path of a given file path.
     * <p>
     * If it is a non-project file; returns immediate parent.
     * </p>
     *
     * @param filePath file path
     * @return top-level module path
     */
    public static Path getCurrentModulePath(Path filePath) {
        Path projectRoot = Paths.get(LSCompilerUtil.getProjectRoot(filePath));
        Path currentModulePath = projectRoot;
        Path prevSourceRoot = filePath.getParent();
        try {
            if (prevSourceRoot == null || Files.isSameFile(prevSourceRoot, projectRoot)) {
                return currentModulePath;
            }
            while (true) {
                Path newSourceRoot = prevSourceRoot.getParent();
                currentModulePath = prevSourceRoot;
                if (newSourceRoot == null || newSourceRoot.toString().isEmpty() ||
                        "/".equals(newSourceRoot.toString()) || Files.isSameFile(newSourceRoot, projectRoot)) {
                    // We have reached the project root
                    break;
                }
                prevSourceRoot = newSourceRoot;
            }
        } catch (IOException e) {
            // do nothing
        }
        return currentModulePath;
    }

    /**
     * Create and returns temp file.
     *
     * @param tempFileId temp file id
     * @return Path
     */
    public static Path createTempFile(String tempFileId) {
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
    public static Optional<String> getUntitledFileId(String filePath) {
        Matcher pkgMatcher = untitledFilePattern.matcher(filePath);
        return (pkgMatcher.find()) ? Optional.of(pkgMatcher.group(1)) : Optional.empty();
    }

    /**
     * Returns unsaved file path.
     *
     * @param filePath file path
     * @return file path
     */
    public static Optional<Path> getUntitledFilePath(String filePath) {
        return getUntitledFileId(filePath).map(LSCompilerUtil::createTempFile);
    }

    /**
     * Get the toml content from the config.
     *
     * @param projectDirPath Project Directory Path
     * @return {@link Manifest} Toml Model
     */
    static Manifest getManifest(Path projectDirPath) {
        Path tomlFilePath = projectDirPath.resolve((ProjectDirConstants.MANIFEST_FILE_NAME));
        try {
            return ManifestProcessor.parseTomlContentFromFile(tomlFilePath);
        } catch (IOException | TomlException e) {
            return new Manifest();
        }
    }

    /**
     * Represents an empty print stream to avoid writing to the standard print stream.
     */
    public static class EmptyPrintStream extends PrintStream {
        public EmptyPrintStream() throws UnsupportedEncodingException {
            super(new OutputStream() {
                @Override
                public void write(int b) {
                }
            }, true, "UTF-8");
        }
    }
}

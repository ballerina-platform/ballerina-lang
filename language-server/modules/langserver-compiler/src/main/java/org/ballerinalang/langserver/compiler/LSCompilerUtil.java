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
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.common.CustomErrorStrategyFactory;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.repository.LangServerFSProgramDirectory;
import org.ballerinalang.langserver.compiler.workspace.repository.LangServerFSProjectDirectory;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

/**
 * Language server compiler implementation for Ballerina.
 */
public class LSCompilerUtil {

    private static final Logger logger = LoggerFactory.getLogger(LSCompilerUtil.class);

    public static final String UNTITLED_BAL = "untitled.bal";
    
    public static final boolean EXPERIMENTAL_FEATURES_ENABLED;

    private static Path untitledProjectPath;

    private static final Pattern untitledFilePattern =
            Pattern.compile(".*[/\\\\]temp[/\\\\](.*)[/\\\\]untitled.bal");

    static {
        String experimental = System.getProperty("experimental");
        EXPERIMENTAL_FEATURES_ENABLED = experimental != null && Boolean.parseBoolean(experimental);
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
     * @param packageID         Package Name
     * @param packageRepository Package Repository
     * @param sourceRoot        LSDocument for Source Root
     * @param preserveWhitespace Preserve Whitespace
     * @param documentManager {@link WorkspaceDocumentManager} Document Manager
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
     * @param packageID         Package ID
     * @param packageRepository Package Repository
     * @param document        LSDocument for Source Root
     * @param preserveWhitespace Preserve Whitespace
     * @param documentManager {@link WorkspaceDocumentManager} Document Manager
     * @param compilerPhase {@link CompilerPhase} Compiler Phase
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(PackageID packageID, PackageRepository packageRepository,
                                                         LSDocument document, boolean preserveWhitespace,
                                                         WorkspaceDocumentManager documentManager,
                                                         CompilerPhase compilerPhase) {
        LSContextManager lsContextManager = LSContextManager.getInstance();
        CompilerContext context = lsContextManager.getCompilerContext(packageID, document.getSourceRoot(),
                                                                      documentManager);
        context.put(PackageRepository.class, packageRepository);
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, document.getSourceRoot());
        options.put(CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(EXPERIMENTAL_FEATURES_ENABLED));

        if (null == compilerPhase) {
            throw new AssertionError("Compiler Phase can not be null.");
        }
        String phase = compilerPhase.toString().equals(CompilerPhase.COMPILER_PLUGIN.toString()) ? "annotationProcess"
                : compilerPhase.toString();

        options.put(COMPILER_PHASE, phase);
        options.put(PRESERVE_WHITESPACE, Boolean.valueOf(preserveWhitespace).toString());
        options.put(TEST_ENABLED, String.valueOf(true));
        options.put(SKIP_TESTS, String.valueOf(false));

        // In order to capture the syntactic errors, need to go through the default error strategy
        context.put(DefaultErrorStrategy.class, null);

        if (context.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
            ((CollectDiagnosticListener) context.get(DiagnosticListener.class)).clearAll();
        }

        Path sourceRootPath = document.getSourceRootPath();
        if (isBallerinaProject(document.getSourceRoot(), document.getURIString())) {
            LangServerFSProjectDirectory projectDirectory =
                    LangServerFSProjectDirectory.getInstance(sourceRootPath, documentManager);
            context.put(SourceDirectory.class, projectDirectory);
        } else {
            LangServerFSProgramDirectory programDirectory =
                    LangServerFSProgramDirectory.getInstance(sourceRootPath, documentManager);
            context.put(SourceDirectory.class, programDirectory);
        }
        return context;
    }

    /**
     * Find project root directory.
     *
     * @param parentDir current parent directory
     * @return {@link String} project root | null
     */
    public static String findProjectRoot(String parentDir) {
        return findProjectRoot(parentDir, RepoUtils.createAndGetHomeReposPath());
    }

    @CheckForNull
    public static String findProjectRoot(String parentDir, Path balHomePath) {
        if (parentDir == null) {
            return null;
        }
        Path pathWithDotBal = null;
        boolean pathWithDotBalExists = false;

        // Go to top till you find a project directory or ballerina home
        while (!pathWithDotBalExists && parentDir != null) {
            pathWithDotBal = Paths.get(parentDir, ProjectDirConstants.DOT_BALLERINA_DIR_NAME);
            pathWithDotBalExists = Files.exists(pathWithDotBal, LinkOption.NOFOLLOW_LINKS);
            if (!pathWithDotBalExists) {
                Path parentsParent = Paths.get(parentDir).getParent();
                parentDir = (parentsParent != null) ? parentsParent.toString() : null;
            }
        }

        boolean balHomeExists = Files.exists(balHomePath, LinkOption.NOFOLLOW_LINKS);

        // Check if you find ballerina home if so return null.
        if (pathWithDotBalExists && balHomeExists && isSameFile(pathWithDotBal, balHomePath)) {
            return null;
        }

        // Else return the project directory.
        if (pathWithDotBalExists) {
            return parentDir;
        } else {
            // If no directory found return null.
            return null;
        }
    }

    private static boolean isSameFile(Path path1, Path path2) {
        try {
            return path1.equals(path2) || Files.isSameFile(path1, path2);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get compiler for the given context and file.
     *
     * @param context               Language server context
     * @param relativeFilePath      File name which is currently open
     * @param compilerContext       Compiler context
     * @param customErrorStrategy   custom error strategy class
     * @return {@link Compiler}     ballerina compiler
     */
    public static Compiler getCompiler(LSContext context, String relativeFilePath, CompilerContext compilerContext,
                                       Class customErrorStrategy) {
        context.put(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY, relativeFilePath);
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
        if (parentPath == null) {
            return null;
        }

        return findProjectRoot(parentPath.toString());
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
        Path projectRoot = Paths.get(LSCompilerUtil.getSourceRoot(filePath));
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
     * Check whether given directory is a project dir.
     *
     * @param root root path
     * @param fileUri file Uri
     * @return {@link Boolean} true if project dir, else false
     */
    public static boolean isBallerinaProject(String root, String fileUri) {
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
     * @param projectDirPath    Project Directory Path
     * @return {@link Manifest} Toml Model
     */
    static Manifest getManifest(Path projectDirPath) {
        Path tomlFilePath = projectDirPath.resolve((ProjectDirConstants.MANIFEST_FILE_NAME));
        try {
            return ManifestProcessor.parseTomlContentFromFile(tomlFilePath.toString());
        } catch (IOException e) {
            return new Manifest();
        }
    }
}


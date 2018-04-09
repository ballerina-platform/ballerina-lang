/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.common.CustomErrorStrategyFactory;
import org.ballerinalang.langserver.common.LSDocument;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.LSParserUtils;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.workspace.repository.LangServerFSProgramDirectory;
import org.ballerinalang.langserver.workspace.repository.LangServerFSProjectDirectory;
import org.ballerinalang.langserver.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Compilation unit builder is for building ballerina compilation units.
 */
public class TextDocumentServiceUtil {

    private static final String PACKAGE_REGEX = "package\\s+([a-zA_Z_][\\.\\w]*);";

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
     * Find project root directory.
     *
     * @param parentDir current parent directory
     * @return {@link String} project root | null
     */
    public static String findProjectRoot(String parentDir) {
        Path path = Paths.get(parentDir, ".ballerina");
        if (Files.exists(path)) {
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
     * Get the project manifest details.
     *
     * @param sourceRoot source root of the project dir
     * @return {@link String} organization name
     */
    public static Manifest getProjectManifestDetails(String sourceRoot) {
        Path tomlFile = Paths.get(sourceRoot, ProjectDirConstants.MANIFEST_FILE_NAME);
        Manifest manifest = new Manifest();
        if (Files.exists(tomlFile)) {
            try {
                manifest = ManifestProcessor.parseTomlContentFromFile(tomlFile.toString());
            } catch (IOException e) {
                manifest = new Manifest();
            }
        }

        return manifest;
    }

    /**
     * Get the package from file content.
     *
     * @param fileContent - content of the file
     * @return - package declaration
     */
    public static String getPackageFromContent(String fileContent) {
        Pattern pkgPattern = Pattern.compile(PACKAGE_REGEX);
        Matcher pkgMatcher = pkgPattern.matcher(fileContent);

        if (!pkgMatcher.find()) {
            return "";
        }

        return pkgMatcher.group(1);
    }

    /**
     * Prepare the compiler context.
     *
     * @param packageName        Package Name
     * @param packageRepository  Package Repository
     * @param sourceRoot         LSDocument for Source Root
     * @param preserveWhitespace Preserve Whitespace
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(String packageName, PackageRepository packageRepository,
                                                         LSDocument sourceRoot, boolean preserveWhitespace,
                                                         WorkspaceDocumentManager documentManager) {
        return prepareCompilerContext(packageName, packageRepository, sourceRoot, preserveWhitespace, documentManager,
                CompilerPhase.TAINT_ANALYZE);
    }

    /**
     * Prepare the compiler context.
     *
     * @param packageName        Package Name
     * @param packageRepository  Package Repository
     * @param sourceRoot         LSDocument for Source Root
     * @param preserveWhitespace Preserve Whitespace
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(String packageName, PackageRepository packageRepository,
                                                         LSDocument sourceRoot, boolean preserveWhitespace,
                                                         WorkspaceDocumentManager documentManager,
                                                         CompilerPhase compilerPhase) {
        CompilerContext context = new CompilerContext();
        context.put(PackageRepository.class, packageRepository);
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot.getSourceRoot());

        if (null == compilerPhase) {
            throw new AssertionError("Compiler Phase can not be null.");
        }

        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.valueOf(preserveWhitespace).toString());

        if (isProjectDir(sourceRoot.getSourceRoot(), sourceRoot.getURIString())) {
            context.put(SourceDirectory.class,
                    new LangServerFSProjectDirectory(sourceRoot.getSourceRootPath(), documentManager));
        } else {
            context.put(SourceDirectory.class,
                    new LangServerFSProgramDirectory(sourceRoot.getSourceRootPath(), documentManager));
        }
        LSPackageCache globalPackageCache = LSPackageCache.getInstance();
        globalPackageCache.removePackage(new PackageID(Names.ANON_ORG,
                new Name(packageName), new Name("0.0.0")));
        PackageCache.setInstance(globalPackageCache.getPackageCache(), context);
        return context;
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
    public static List<BLangPackage> getBLangPackage(LSContext context, WorkspaceDocumentManager docManager,
                                                     boolean preserveWhitespace, Class customErrorStrategy,
                                                     boolean compileFullProject) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        String unsavedFileId = LSParserUtils.getUnsavedFileIdOrNull(uri);
        if (unsavedFileId != null) {
            // if it is an unsaved file; overrides the file path
            uri = LSParserUtils.createAndGetTempFile(unsavedFileId).toUri().toString();
            context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        }
        LSDocument document = new LSDocument(uri);
        Path filePath = CommonUtil.getPath(document);
        Path fileNamePath = filePath.getFileName();
        String fileName = "";
        if (fileNamePath != null) {
            fileName = fileNamePath.toString();
        }

        String sourceRoot = TextDocumentServiceUtil.getSourceRoot(filePath);
        String pkgName = TextDocumentServiceUtil.getPackageNameForGivenFile(sourceRoot, filePath.toString());
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
                            Compiler compiler = getCompiler(context, fileName, file.getName(), packageRepository,
                                                sourceDocument, preserveWhitespace, customErrorStrategy, docManager);
                            packages.add(compiler.compile(file.getName()));
                        }
                    }
                }
            }
        } else {
            pkgName = ("".equals(pkgName)) ? fileName : pkgName;
            Compiler compiler = getCompiler(context, fileName, pkgName, packageRepository, sourceDocument,
                                            preserveWhitespace, customErrorStrategy, docManager);
            packages.add(compiler.compile(pkgName));
        }
        return packages;
    }

    /**
     * Get compiler for the given context and file.
     *
     * @param context             Language server context
     * @param fileName            File name which is currently open
     * @param packageName         Package Name
     * @param packageRepository   package repository
     * @param sourceRoot          LSDocument for root path of the source
     * @param preserveWhitespace  enable/disable preserve white space in compiler
     * @param customErrorStrategy custom error strategy class
     * @return {@link Compiler} ballerina compiler
     */
    private static Compiler getCompiler(LSContext context, String fileName, String packageName,
                                        PackageRepository packageRepository, LSDocument sourceRoot,
                                        boolean preserveWhitespace, Class customErrorStrategy,
                                        WorkspaceDocumentManager documentManager) {
        CompilerContext compilerContext =
                TextDocumentServiceUtil.prepareCompilerContext(packageName, packageRepository, sourceRoot,
                        preserveWhitespace, documentManager);
        context.put(DocumentServiceKeys.FILE_NAME_KEY, fileName);
        context.put(DocumentServiceKeys.COMPILER_CONTEXT_KEY, compilerContext);
        context.put(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY, new LSServiceOperationContext());
        List<org.ballerinalang.util.diagnostic.Diagnostic> balDiagnostics = new ArrayList<>();
        CollectDiagnosticListener diagnosticListener = new CollectDiagnosticListener(balDiagnostics);
        compilerContext.put(DiagnosticListener.class, diagnosticListener);
        compilerContext.put(DefaultErrorStrategy.class,
                CustomErrorStrategyFactory.getCustomErrorStrategy(customErrorStrategy, context));
        return Compiler.getInstance(compilerContext);
    }
}

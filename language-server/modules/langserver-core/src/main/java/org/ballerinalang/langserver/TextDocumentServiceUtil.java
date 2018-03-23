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
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.workspace.repository.LangServerFSProjectDirectory;
import org.ballerinalang.langserver.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.File;
import java.net.URI;
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
     * @param pkgName  package that the file belongs to
     * @return {@link String} program directory path
     */
    public static String getSourceRoot(Path filePath, String pkgName) {
        if (filePath == null || filePath.getParent() == null) {
            return null;
        }
        Path parentPath = filePath.getParent();
        if (parentPath == null) {
            return null;
        }
        List<String> pathParts = Arrays.asList(parentPath.toString().split(Pattern.quote(File.separator)));
        // TODO: change this to find .ballerina meta folder when project structure is available.
        int pkgNameIndex = pathParts.size();
        if (!pkgName.isEmpty()) {
            for (int i = pathParts.size() - 1; i >= 0; i--) {
                if (pathParts.get(i).equals(pkgName)) {
                    pkgNameIndex = i;
                    break;
                }
            }
        }

        List<String> programDirParts = pathParts.subList(0, pkgNameIndex);
        return String.join(File.separator, programDirParts);
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
     * @param packageRepository  Package Repository
     * @param sourceRoot         LSDocument for Source Root
     * @param preserveWhitespace Preserve Whitespace
     * @return {@link CompilerContext}     Compiler context
     */
    public static CompilerContext prepareCompilerContext(PackageRepository packageRepository, LSDocument sourceRoot,
                                                         boolean preserveWhitespace,
                                                         WorkspaceDocumentManager documentManager) {
        org.wso2.ballerinalang.compiler.util.CompilerContext context = new CompilerContext();
        context.put(PackageRepository.class, packageRepository);
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot.getSourceRoot());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_ANALYZE.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.valueOf(preserveWhitespace).toString());
        context.put(SourceDirectory.class,
                new LangServerFSProjectDirectory(sourceRoot.getSourceRootPath(), documentManager));
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
    public static List<BLangPackage> getBLangPackage(LanguageServerContext context, WorkspaceDocumentManager docManager,
                                                     boolean preserveWhitespace, Class customErrorStrategy,
                                                     boolean compileFullProject) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        LSDocument document = new LSDocument(uri);
        String fileContent = docManager.getFileContent(Paths.get(URI.create(uri)));
        Path filePath = CommonUtil.getPath(document);
        Path fileNamePath = filePath.getFileName();
        String fileName = "";
        if (fileNamePath != null) {
            fileName = fileNamePath.toString();
        }

        String pkgName = TextDocumentServiceUtil.getPackageFromContent(fileContent);
        String sourceRoot = TextDocumentServiceUtil.getSourceRoot(filePath, pkgName);
        LSDocument sourceDocument = new LSDocument();
        sourceDocument.setUri(uri);
        sourceDocument.setSourceRoot(sourceRoot);

        PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, docManager);
        List<BLangPackage> packages = new ArrayList<>();
        if (compileFullProject) {
            if (sourceRoot != null && !sourceRoot.isEmpty()) {
                File projectDir = new File(sourceRoot);
                File[] files = projectDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.getName().equals(".ballerina")) {
                            Compiler compiler = getCompiler(context, fileName, packageRepository, sourceDocument,
                                    preserveWhitespace, customErrorStrategy, docManager);
                            packages.add(compiler.compile(file.getName()));
                        }
                    }
                }
            }
        } else {
            Compiler compiler = getCompiler(context, fileName, packageRepository, sourceDocument, preserveWhitespace,
                    customErrorStrategy, docManager);
            if ("".equals(pkgName)) {
                packages.add(compiler.compile(fileName));
            } else {
                packages.add(compiler.compile(pkgName));
            }
        }
        return packages;
    }

    /**
     * Get compiler for the given context and file.
     *
     * @param context             Language server context
     * @param fileName            File name which is currently open
     * @param packageRepository   package repository
     * @param sourceRoot          LSDocument for root path of the source
     * @param preserveWhitespace  enable/disable preserve white space in compiler
     * @param customErrorStrategy custom error strategy class
     * @return {@link Compiler} ballerina compiler
     */
    private static Compiler getCompiler(LanguageServerContext context, String fileName,
                                        PackageRepository packageRepository, LSDocument sourceRoot,
                                        boolean preserveWhitespace, Class customErrorStrategy,
                                        WorkspaceDocumentManager documentManager) {
        CompilerContext compilerContext =
                TextDocumentServiceUtil.prepareCompilerContext(packageRepository, sourceRoot,
                        preserveWhitespace, documentManager);
        context.put(DocumentServiceKeys.FILE_NAME_KEY, fileName);
        context.put(DocumentServiceKeys.COMPILER_CONTEXT_KEY, compilerContext);
        context.put(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY, new TextDocumentServiceContext());
        List<org.ballerinalang.util.diagnostic.Diagnostic> balDiagnostics = new ArrayList<>();
        CollectDiagnosticListener diagnosticListener = new CollectDiagnosticListener(balDiagnostics);
        compilerContext.put(DiagnosticListener.class, diagnosticListener);
        compilerContext.put(DefaultErrorStrategy.class,
                CustomErrorStrategyFactory.getCustomErrorStrategy(customErrorStrategy, context));
        return Compiler.getInstance(compilerContext);
    }
}

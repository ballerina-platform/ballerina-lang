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
import org.ballerinalang.langserver.completions.BallerinaCustomErrorStrategy;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * Compilation unit builder is for building ballerina compilation units.
 */
public class TextDocumentServiceUtil {
    private static final String PACKAGE_REGEX = "package\\s+([a-zA_Z_][\\.\\w]*);";

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaTextDocumentService.class);

    static String getSourceRoot(Path filePath, String pkgName) {
        if (filePath == null || filePath.getParent() == null) {
            return null;
        }
        Path parentPath = filePath.getParent();
        if (parentPath == null) {
            return null;
        }
        List<String> pathParts = Arrays.asList(parentPath.toString().split(Pattern.quote(File.separator)));

        List<String> pkgParts = "".equals(pkgName) ?
                new ArrayList<>() : Arrays.asList(pkgName.split(Pattern.quote(".")));
        Collections.reverse(pkgParts);
        boolean foundProgramDir = true;
        for (int i = 1; i <= pkgParts.size(); i++) {
            if (!pathParts.get(pathParts.size() - i).equals(pkgParts.get(i - 1))) {
                foundProgramDir = false;
                break;
            }
        }
        if (!foundProgramDir) {
            return null;
        }

        List<String> programDirParts = pathParts.subList(0, pathParts.size() - pkgParts.size());
        return String.join(File.separator, programDirParts);
    }

    /**
     * Get the package from file content.
     * @param fileContent - content of the file
     * @return - package declaration
     */
    static String getPackageFromContent(String fileContent) {
        Pattern pkgPattern = Pattern.compile(PACKAGE_REGEX);
        Matcher pkgMatcher = pkgPattern.matcher(fileContent);

        if (!pkgMatcher.find()) {
            return "";
        }

        return pkgMatcher.group(1);
    }

    /**
     * Prepare the compiler context.
     * @param packageRepository             Package Repository
     * @param sourceRoot                    Source Root
     * @return  {@link CompilerContext}     Compiler context
     */
    static CompilerContext prepareCompilerContext(PackageRepository packageRepository, String sourceRoot) {
        org.wso2.ballerinalang.compiler.util.CompilerContext context = new CompilerContext();
        context.put(PackageRepository.class, packageRepository);
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(SOURCE_ROOT, sourceRoot);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_ANALYZE.toString());
        return context;
    }

    /**
     * Get the BLangPackage for a given program.
     * @param context               Text Document Service Context
     * @param docManager            Document manager
     * @return {@link BLangPackage} BLang Package
     */
    public static BLangPackage getBLangPackage(TextDocumentServiceContext context,
                                               WorkspaceDocumentManager docManager) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        String fileContent = docManager.getFileContent(Paths.get(URI.create(uri)));
        Path filePath = getPath(uri);
        Path fileNamePath = filePath.getFileName();
        String fileName = "";
        if (fileNamePath != null) {
            fileName = fileNamePath.toString();
        }

        String pkgName = TextDocumentServiceUtil.getPackageFromContent(fileContent);
        String sourceRoot = TextDocumentServiceUtil.getSourceRoot(filePath, pkgName);
        PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, docManager);
        CompilerContext compilerContext = TextDocumentServiceUtil.prepareCompilerContext(packageRepository, sourceRoot);

        context.put(DocumentServiceKeys.FILE_NAME_KEY, fileName);
        context.put(DocumentServiceKeys.COMPILER_CONTEXT_KEY, compilerContext);

        List<org.ballerinalang.util.diagnostic.Diagnostic> balDiagnostics = new ArrayList<>();
        CollectDiagnosticListener diagnosticListener = new CollectDiagnosticListener(balDiagnostics);
        BallerinaCustomErrorStrategy customErrorStrategy = new BallerinaCustomErrorStrategy(context);
        compilerContext.put(DiagnosticListener.class, diagnosticListener);
        compilerContext.put(DefaultErrorStrategy.class, customErrorStrategy);

        Compiler compiler = Compiler.getInstance(compilerContext);
        if ("".equals(pkgName)) {
            compiler.compile(fileName);
        } else {
            compiler.compile(pkgName);
        }

        return (BLangPackage) compiler.getAST();
    }

    /**
     *  Get Path from URI.
     * @param uri               Path URI String
     * @return                  File Path
     */
    public static Path getPath(String uri) {
        Path path = null;
        try {
            path = Paths.get(new URL(uri).toURI());
        } catch (URISyntaxException | MalformedURLException e) {
            LOGGER.error(e.getMessage());
        } finally {
            return path;
        }
    }

    /**
     * Convert the diagnostic position to a zero based positioning diagnostic position.
     * @param diagnosticPos - diagnostic position to be cloned
     * @return {@link DiagnosticPos} converted diagnostic position
     */
    public static DiagnosticPos  toZeroBasedPosition(DiagnosticPos diagnosticPos) {
        int startLine = diagnosticPos.getStartLine() - 1;
        int endLine = diagnosticPos.getEndLine() - 1;
        int startColumn = diagnosticPos.getStartColumn() - 1;
        int endColumn = diagnosticPos.getEndColumn() - 1;
        return new DiagnosticPos(diagnosticPos.getSource(), startLine, endLine, startColumn, endColumn);
    }
}

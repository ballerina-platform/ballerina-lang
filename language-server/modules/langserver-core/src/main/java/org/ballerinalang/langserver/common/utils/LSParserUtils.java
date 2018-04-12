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
package org.ballerinalang.langserver.common.utils;

import com.google.common.io.Files;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.CollectDiagnosticListener;
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.common.LSDocument;
import org.ballerinalang.langserver.common.modal.BallerinaFile;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.langserver.LSContextManager.COMPILER_LOCK;

/**
 * Parser Utils.
 */
public class LSParserUtils {

    private static final Logger logger = LoggerFactory.getLogger(LSParserUtils.class);

    private static final WorkspaceDocumentManager documentManager = WorkspaceDocumentManagerImpl.getInstance();

    private static Path untitledProjectPath;

    public static final String UNTITLED_BAL = "untitled.bal";

    private static final Pattern untitledFilePattern =
            Pattern.compile("^(?:file:\\/\\/)?\\/temp\\/(.*)\\/untitled.bal");

    static {
        // Here we will create a tmp directory as the untitled project repo.
        File untitledDir = Files.createTempDir();
        untitledProjectPath = untitledDir.toPath();
        // Now lets create a empty untitled.bal to fool compiler.
        File untitledBal = new File(Paths.get(untitledProjectPath.toString(), UNTITLED_BAL).toString());
        try {
            untitledBal.createNewFile();
        } catch (IOException e) {
            logger.error("Unable to create untitled project directory, " +
                                 "unsaved files might not work properly.");
        }
    }

    /**
     * Return a compilation unit for a given text.
     *
     * @param content content of the fragment
     * @return BLangCompilationUnit
     */
    public static BLangCompilationUnit compileFragment(String content) {
        BallerinaFile model = compile(content, CompilerPhase.DEFINE);
        if (model.getBLangPackage() != null) {
            return model.getBLangPackage().getCompilationUnits().stream().
                    filter(compUnit -> UNTITLED_BAL.equals(compUnit.getName())).findFirst().get();
        }
        return null;
    }

    /**
     * Compile an unsaved Ballerina file.
     *
     * @param content file content
     * @param phase   {CompilerPhase} CompilerPhase
     * @return BallerinaFile
     */
    public static BallerinaFile compile(String content, CompilerPhase phase) {
        return compile(content, UNTITLED_BAL, phase, true);
    }

    /**
     * Compile a temp Ballerina file.
     *
     * @param content            content of the file
     * @param tempFileId         a unique id for the temp file
     * @param phase              {CompilerPhase} compiler phase
     * @param preserveWhitespace preserve Whitespace
     * @return BallerinaFile
     */
    public static BallerinaFile compile(String content, String tempFileId, CompilerPhase phase,
                                        boolean preserveWhitespace) {
        Path unsaved = createAndGetTempFile(tempFileId);
        synchronized (LSParserUtils.class) {
            // Since we use the same file name for all the fragment passes we need to make sure following -
            // does not run parallelly.
            String sourceRoot = TextDocumentServiceUtil.getSourceRoot(unsaved);
            String pkgName = TextDocumentServiceUtil.getPackageNameForGivenFile(sourceRoot, unsaved.toString());
            LSDocument sourceDocument = new LSDocument();
            sourceDocument.setUri(unsaved.toUri().toString());
            sourceDocument.setSourceRoot(sourceRoot);

            PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, documentManager);
            PackageID packageID = new PackageID(Names.ANON_ORG, new Name(pkgName), Names.DEFAULT_VERSION);
            if ("".equals(pkgName)) {
                Path filePath = unsaved.getFileName();
                if (filePath != null) {
                    pkgName = filePath.toString();
                    packageID = new PackageID(pkgName);
                }
            }
            CompilerContext context = TextDocumentServiceUtil.prepareCompilerContext(packageID, packageRepository,
                                                                                     sourceDocument, preserveWhitespace,
                                                                                     documentManager, phase);
            BallerinaFile model = compile(content, unsaved, phase, context);
            documentManager.closeFile(unsaved);
            return model;
        }
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
                tempInnerFolder.mkdir();
                untitledBal.createNewFile();
            } catch (IOException e) {
                logger.error("Unable to create untitled project directory, unsaved files might not work properly.");
            }
        }
        return Paths.get(tempInnerFolder.toString(), UNTITLED_BAL);
    }

    /**
     * Compile a Ballerina file.
     * <p>
     * Note: THis is used by the ballerina Composer
     *
     * @param content file content
     * @param path    file path
     * @param phase   {CompilerPhase} set phase for the compiler.
     * @return BallerinaFile
     */
    public static BallerinaFile compile(String content, Path path, CompilerPhase phase) {
        return compile(content, path, phase, true);
    }

    /**
     * Compile a Ballerina file.
     *
     * @param content file content
     * @param path    file path
     * @param phase   {CompilerPhase} set phase for the compiler.
     * @return BallerinaFile
     */
    public static BallerinaFile compile(String content, Path path, CompilerPhase phase, boolean preserveWhiteSpace) {
        String sourceRoot = TextDocumentServiceUtil.getSourceRoot(path);
        String pkgName = TextDocumentServiceUtil.getPackageNameForGivenFile(sourceRoot, path.toString());
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
        CompilerContext context = TextDocumentServiceUtil.prepareCompilerContext(packageID, packageRepository,
                                                                                 sourceDocument, preserveWhiteSpace,
                                                                                 documentManager, phase);

        // In order to capture the syntactic errors, need to go through the default error strategy
        context.put(DefaultErrorStrategy.class, null);

        return compile(content, path, phase, context);
    }

    /**
     * Compile a Ballerina file.
     *
     * @param content file content
     * @param path    file path
     * @param phase   {CompilerPhase} set phase for the compiler.
     * @return BallerinaFile
     */
    public static BallerinaFile compile(String content, Path path, CompilerPhase phase, CompilerContext context) {
        if (documentManager.isFileOpen(path)) {
            documentManager.updateFile(path, content);
        } else {
            documentManager.openFile(path, content);
        }

        String sourceRoot = TextDocumentServiceUtil.getSourceRoot(path);
        String pkgName = TextDocumentServiceUtil.getPackageNameForGivenFile(sourceRoot, path.toString());
        LSDocument sourceDocument = new LSDocument();
        sourceDocument.setUri(path.toUri().toString());
        sourceDocument.setSourceRoot(sourceRoot);

        if ("".equals(pkgName)) {
            Path filePath = path.getFileName();
            if (filePath != null) {
                pkgName = filePath.toString();
            }
        }
        BLangPackage bLangPackage = null;
        if (context.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
            ((CollectDiagnosticListener) context.get(DiagnosticListener.class)).getDiagnostics().clear();
        }
        try {
            BLangDiagnosticLog.getInstance(context).errorCount = 0;
            Compiler compiler = Compiler.getInstance(context);
            synchronized (COMPILER_LOCK) {
                bLangPackage = compiler.compile(pkgName);
            }
        } catch (Exception e) {
            // Ignore.
        }
        BallerinaFile bfile = new BallerinaFile();
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
     * Returns unsaved file id or null.
     *
     * @param filePath file path
     * @return file id
     */
    public static String getUnsavedFileIdOrNull(String filePath) {
        Matcher pkgMatcher = untitledFilePattern.matcher(filePath);
        return (pkgMatcher.find()) ? pkgMatcher.group(1) : null;
    }
}

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
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.CollectDiagnosticListener;
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.common.LSDocument;
import org.ballerinalang.langserver.common.modal.BallerinaFile;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser Utils.
 */
public class LSParserUtils {

    private static final Logger logger = LoggerFactory.getLogger(LSParserUtils.class);

    private static final WorkspaceDocumentManagerImpl documentManager =
            WorkspaceDocumentManagerImpl.getInstance();

    private static Path untitledProjectPath;

    public static final String UNTITLED_BAL = "untitled.bal";

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
        BallerinaFile model = compile(content, CompilerPhase.DEFINE, true);
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
     * @return BallerinaFile
     */
    public static BallerinaFile compile(String content, CompilerPhase compilerPhase, boolean preserveWhiteSpace) {
        return compile(content, UNTITLED_BAL, compilerPhase, preserveWhiteSpace);
    }

    /**
     * Compile a temp Ballerina file.
     *
     * @param content       content of the file
     * @param tempFileId    a unique id for the temp file
     * @param compilerPhase compiler phase
     * @param preserveWhiteSpace preserve white-space
     * @return BallerinaFile
     */
    public static BallerinaFile compile(String content, String tempFileId, CompilerPhase compilerPhase,
                                        boolean preserveWhiteSpace) {
        Path unsaved = createAndGetTempFile(untitledProjectPath, tempFileId);
        synchronized (LSParserUtils.class) {
            // Since we use the same file name for all the fragment passes we need to make sure following -
            // does not run parallelly.
            documentManager.openFile(unsaved, content);
            BallerinaFile model = compile(content, unsaved, compilerPhase, preserveWhiteSpace);
            documentManager.closeFile(unsaved);
            return model;
        }
    }

    private static Path createAndGetTempFile(Path tempFolder, String tempFileId) {
        if (UNTITLED_BAL.equals(tempFileId)) {
            return Paths.get(tempFolder.toString(), tempFileId);
        }
        File tempInnerFolder = new File(Paths.get(tempFolder.toString(), tempFileId).toString());
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
     *
     * @param content       file content
     * @param path          file path
     * @param phase {CompilerPhase} set phase for the compiler.
     * @return BallerinaFile
     */
    public static BallerinaFile compile(String content, Path path, CompilerPhase phase) {
        return compile(content, path, phase, true);
    }

    /**
     * Compile a Ballerina file.
     *
     * @param content       file content
     * @param path          file path
     * @param phase {CompilerPhase} set phase for the compiler.
     * @return BallerinaFile
     */
    public static BallerinaFile compile(String content, Path path, CompilerPhase phase, boolean preserveWhiteSpace) {
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

        PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, documentManager);
        if ("".equals(pkgName)) {
            Path filePath = path.getFileName();
            if (filePath != null) {
                pkgName = filePath.toString();
            }
        }
        CompilerContext context = TextDocumentServiceUtil.prepareCompilerContext(pkgName, packageRepository,
                                            sourceDocument, preserveWhiteSpace, documentManager, CompilerPhase.DEFINE);

        List<Diagnostic> balDiagnostics = new ArrayList<>();
        CollectDiagnosticListener diagnosticListener = new CollectDiagnosticListener(balDiagnostics);
        context.put(DiagnosticListener.class, diagnosticListener);

        BLangPackage bLangPackage = null;
        try {
            Compiler compiler = Compiler.getInstance(context);
            bLangPackage = compiler.compile(pkgName);
        } catch (Exception e) {
            // Ignore.
        }
        BallerinaFile bfile = new BallerinaFile();
        bfile.setBLangPackage(bLangPackage);
        bfile.setDiagnostics(balDiagnostics);
        return bfile;
    }
}

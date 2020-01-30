/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.workspace.ExtendedWorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.repository.LangServerFSProgramDirectory;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

/**
 * Extended Workspace document manager implementation.
 * Intention is to decouple the single file compilation and string content compilation.
 * 
 * @since 1.0.0
 */
public class ExtendedLSCompiler extends LSModuleCompiler {

    private static final Logger logger = LoggerFactory.getLogger(ExtendedLSCompiler.class);
    
    private static final ExtendedWorkspaceDocumentManagerImpl docManager =
            ExtendedWorkspaceDocumentManagerImpl.getInstance();
    /**
     * Returns a BallerinaFile compiling in-memory content.
     *
     * @param content content to be compiled
     * @param phase   {@link CompilerPhase} for the compiler
     * @return {@link BallerinaFile} containing the compiled package
     * @throws CompilationFailedException when compiler error occurred
     */
    public static BallerinaFile compileContent(String content, CompilerPhase phase) throws CompilationFailedException {
        java.nio.file.Path filePath = LSCompilerUtil.createTempFile(LSCompilerUtil.UNTITLED_BAL);
        Optional<Lock> exModeLock = docManager.enableExplicitMode(filePath);
        Optional<Lock> fileLock = docManager.lockFile(filePath);
        try {
            docManager.updateFile(filePath, content);
            BallerinaFile bFile = compileFile(filePath, phase);
            docManager.closeFile(filePath);
            return bFile;
        } catch (WorkspaceDocumentException e) {
            throw new CompilationFailedException("Error occurred while compiling file:" + filePath.toString(), e);
        } finally {
            docManager.disableExplicitMode(exModeLock.orElse(null));
            fileLock.ifPresent(Lock::unlock);
        }
    }

    /**
     * Compile file.
     *
     * @param filePath file {@link Path} of the file
     * @param compilerPhase    {@link CompilerPhase} for the compiler
     * @return {@link BallerinaFile} containing compiled package
     * @throws CompilationFailedException when compiler error occurred
     */
    public static BallerinaFile compileFile(Path filePath, CompilerPhase compilerPhase)
            throws CompilationFailedException {
        LSContextManager contextManager = LSContextManager.getInstance();
        Path path = filePath.getFileName();
        Path parent = filePath.getParent();
        if (parent == null) {
            return null;
        }
        String packageName = path.toString();
        CompilerContext context = contextManager.createNewCompilerContext(parent.toString(), docManager);
        LangServerFSProgramDirectory programDirectory = LangServerFSProgramDirectory.getInstance(parent, docManager);
        context.put(SourceDirectory.class, programDirectory);
        
        CompilerOptions options = CompilerOptions.getInstance(context);
        String phase = compilerPhase.toString().equals(CompilerPhase.COMPILER_PLUGIN.toString()) ? "annotationProcess"
                : compilerPhase.toString();
        options.put(COMPILER_PHASE, phase);
        options.put(PRESERVE_WHITESPACE, Boolean.TRUE.toString());
        options.put(TEST_ENABLED, String.valueOf(true));
        options.put(SKIP_TESTS, String.valueOf(false));
        BLangDiagnosticLog.getInstance(context).errorCount = 0;
        Compiler compiler = Compiler.getInstance(context);
        LSContext lsContext = new LSCompilerOperationContext
                .CompilerOperationContextBuilder(CompileFileContextOperation.COMPILE_FILE)
                .withCompileFileParams(context, packageName)
                .build();

        try {
            compiler.setOutStream(new LSCompilerUtil.EmptyPrintStream());
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to create the empty stream.");
        }
        BLangPackage bLangPackage = compileSafe(compiler, parent.toString(), packageName, lsContext);
        BallerinaFile bfile;
        if (context.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
            List<Diagnostic> diagnostics = ((CollectDiagnosticListener) context.get(DiagnosticListener.class))
                    .getDiagnostics();
            bfile = new BallerinaFile(bLangPackage, diagnostics, false, context);
        } else {
            bfile = new BallerinaFile(bLangPackage, new ArrayList<>(), false, context);
        }
        return bfile;
    }
}

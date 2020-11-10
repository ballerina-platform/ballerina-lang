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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.JsonObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.BallerinaWorkspaceService;
import org.ballerinalang.langserver.command.testgen.TestGenerator;
import org.ballerinalang.langserver.command.testgen.TestGeneratorException;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.ReferencesKeys;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Represents the create variable command executor.
 *
 * @since 0.985.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class CreateTestExecutor implements LSCommandExecutor {

    public static final String COMMAND = "CREATE_TEST";

    public static String generateTestFileName(Path sourceFilePath) {
        String fileName = FilenameUtils.removeExtension(sourceFilePath.toFile().getName());
        return fileName + "_test" + ProjectDirConstants.BLANG_SOURCE_EXT;
    }

    private static ImmutablePair<Path, Path> createTestFolderIfNotExists(Path sourceFilePath) {
        Path projectRoot = Paths.get(LSCompilerUtil.getProjectRoot(sourceFilePath));
        ImmutablePair<Path, Path> testsDirPath = getTestsDirPath(sourceFilePath, projectRoot);

        //Check for tests folder, if not exists create a new folder
        testsDirPath.getRight().toFile().mkdirs();

        return testsDirPath;
    }

    /**
     * Returns a pair of current module path and calculated target test path.
     *
     * @param sourceFilePath source file path
     * @param projectRoot    project root
     * @return a pair of currentModule path(left-side) and target test path(right-side)
     */
    private static ImmutablePair<Path, Path> getTestsDirPath(Path sourceFilePath, Path projectRoot) {
        if (sourceFilePath == null || projectRoot == null) {
            return null;
        }
        Path currentModulePath = projectRoot;
        Path prevSourceRoot = sourceFilePath.getParent();
        List<String> pathParts = new ArrayList<>();
        try {
            while (prevSourceRoot != null) {
                Path newSourceRoot = prevSourceRoot.getParent();
                currentModulePath = prevSourceRoot;
                if (newSourceRoot == null || Files.isSameFile(newSourceRoot, projectRoot)) {
                    // We have reached the project root
                    break;
                }
                pathParts.add(prevSourceRoot.getFileName().toString());
                prevSourceRoot = newSourceRoot;
            }
        } catch (IOException e) {
            // do nothing
        }

        // Append `tests` path
        Path testDirPath = currentModulePath.resolve(ProjectDirConstants.TEST_DIR_NAME);

        // Add same directory structure inside the module
        for (String part : pathParts) {
            testDirPath = testDirPath.resolve(part);
        }

        return new ImmutablePair<>(currentModulePath, testDirPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String docUri = null;
        int line = -1;
        int column = -1;

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            String argKey = ((JsonObject) arg).get(ARG_KEY).getAsString();
            String argVal = ((JsonObject) arg).get(ARG_VALUE).getAsString();
            switch (argKey) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    docUri = argVal;
                    context.put(DocumentServiceKeys.FILE_URI_KEY, docUri);
                    break;
                case CommandConstants.ARG_KEY_NODE_LINE:
                    line = Integer.parseInt(argVal);
                    break;
                case CommandConstants.ARG_KEY_NODE_COLUMN:
                    column = Integer.parseInt(argVal);
                    break;
                default:
            }
        }

        if (line == -1 || column == -1 || docUri == null) {
            throw new LSCommandExecutorException("Invalid parameters received for the create test command!");
        }

        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);

        // Compile the source file
        BLangPackage builtSourceFile;
        try {
            builtSourceFile = LSModuleCompiler.getBLangPackage(context, docManager, false, false);
        } catch (CompilationFailedException e) {
            throw new LSCommandExecutorException("Couldn't compile the source", e);
        }

        // Generate test file and notify Client
        LanguageServer languageServer = context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY);
        BallerinaWorkspaceService workspace = null;
        if (languageServer instanceof BallerinaLanguageServer) {
            BallerinaLanguageServer ballerinaLanguageServer = (BallerinaLanguageServer) languageServer;
            workspace = (BallerinaWorkspaceService) ballerinaLanguageServer.getWorkspaceService();
        }
        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY);
        try {
            if (builtSourceFile == null || builtSourceFile.hasErrors()) {
                String message = "Test generation failed due to compilation errors!";
                if (client != null) {
                    client.showMessage(new MessageParams(MessageType.Error, message));
                }
                throw new LSCommandExecutorException(message);
            }

            // Check for tests folder, if not exists create a new folder
            Path filePath = Paths.get(URI.create(docUri));
            ImmutablePair<Path, Path> testDirs = createTestFolderIfNotExists(filePath);
            File testsDir = testDirs.getRight().toFile();

            // Generate a unique name for the tests file
            File testFile = testsDir.toPath().resolve(generateTestFileName(filePath)).toFile();

            // Generate test content edits
            String pkgRelativeSourceFilePath = testDirs.getLeft().relativize(filePath).toString();

            LSDocumentIdentifier lsDocument = docManager.getLSDocument(filePath);
            Position pos = new Position(line, column + 1);
            context.put(ReferencesKeys.OFFSET_CURSOR_N_TRY_NEXT_BEST, true);
            SymbolReferencesModel.Reference refAtCursor = ReferencesUtil.getReferenceAtCursor(context, lsDocument, pos);
            BLangNode bLangNode = refAtCursor.getbLangNode();

            Position position = new Position(0, 0);
            Range focus = new Range(position, position);
            BiConsumer<Integer, Integer> focusLineAcceptor = (focusLine, incrementer) -> {
                if (focusLine != null) {
                    position.setLine(focusLine);
                }
                position.setLine(position.getLine() + incrementer);
            };
            List<TextEdit> content = TestGenerator.generate(docManager, bLangNode, focusLineAcceptor, builtSourceFile,
                                                            pkgRelativeSourceFilePath, testFile, context);

            // If not exists, create a new test file
            List<Either<TextDocumentEdit, ResourceOperation>> edits = new ArrayList<>();
            if (!testFile.exists()) {
                edits.add(Either.forRight(new CreateFile(testFile.toPath().toUri().toString())));
            }

            // Send edits
            VersionedTextDocumentIdentifier identifier = new VersionedTextDocumentIdentifier();
            identifier.setUri(testFile.toPath().toUri().toString());

            TextDocumentEdit textEdit = new TextDocumentEdit(identifier, content);
            edits.add(Either.forLeft(textEdit));

            WorkspaceEdit workspaceEdit = new WorkspaceEdit(edits);
            ApplyWorkspaceEditParams editParams = new ApplyWorkspaceEditParams(workspaceEdit);
            if (client != null) {
                client.applyEdit(editParams);
                String message = "Tests generated into the file:" + testFile.toString();
                client.showMessage(new MessageParams(MessageType.Info, message));
                LSClientCapabilities clientCapabilities = context.get(ExecuteCommandKeys.LS_CLIENT_CAPABILITIES_KEY);
                if (clientCapabilities.getExperimentalCapabilities().isShowTextDocumentEnabled()) {
                    showTextDocument(workspace, client, focus, identifier);
                }
            }
            return editParams;
        } catch (WorkspaceDocumentException | CompilationFailedException | TokenOrSymbolNotFoundException
                | TestGeneratorException e) {
            String message = "Test generation failed!: " + e.getMessage();
            if (client != null) {
                client.showMessage(new MessageParams(MessageType.Error, message));
            }
            throw new LSCommandExecutorException(message, e);
        }
    }

    private void showTextDocument(BallerinaWorkspaceService workspace, LanguageClient client, Range focus,
                                  VersionedTextDocumentIdentifier identifier) {
        if (workspace != null && client instanceof ExtendedLanguageClient) {
            Location location = new Location(identifier.getUri(), focus);
            ((ExtendedLanguageClient) client).showTextDocument(location);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}

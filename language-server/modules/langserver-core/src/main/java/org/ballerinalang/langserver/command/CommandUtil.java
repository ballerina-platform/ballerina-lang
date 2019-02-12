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
package org.ballerinalang.langserver.command;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.util.Flags;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static org.ballerinalang.langserver.common.utils.CommonUtil.FunctionGenerator.generateTypeDefinition;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Utilities for the command related operations.
 */
public class CommandUtil {

    private CommandUtil() {
    }

    /**
     * Get the commands for the given node type.
     *
     * @param topLevelNodeType Node Type
     * @param docUri           Document URI
     * @param line             Node line
     * @return {@link List}    List of commands for the line
     */
    public static List<Either<Command, CodeAction>> getCommandForNodeType(String topLevelNodeType, String docUri,
                                                                          int line) {
        List<Either<Command, CodeAction>> commands = new ArrayList<>();
        if (UtilSymbolKeys.OBJECT_KEYWORD_KEY.equals(topLevelNodeType)) {
            commands.add(getInitializerGenerationCommand(docUri, line));
        }
        commands.add(getDocGenerationCommand(topLevelNodeType, docUri, line));
        commands.add(getAllDocGenerationCommand(docUri));
        return commands;
    }

    /**
     * Get the command for generate test class.
     *
     * @param topLevelNodeType top level node
     * @param docUri           Document Uri
     * @param params           Code action parameters
     * @param documentManager  Document manager
     * @param lsCompiler       LS Compiler
     * @return {@link Command}  Test Generation command
     */
    public static List<Either<Command, CodeAction>> getTestGenerationCommand(String topLevelNodeType, String docUri,
                                                         CodeActionParams params,
                                                         WorkspaceDocumentManager documentManager,
                                                         LSCompiler lsCompiler) {
        LSServiceOperationContext context = new LSServiceOperationContext();
        List<Either<Command, CodeAction>> commands = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri));
        Position position = params.getRange().getStart();
        args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + position.getLine()));
        args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + position.getCharacter()));

        boolean isService = UtilSymbolKeys.SERVICE_KEYWORD_KEY.equals(topLevelNodeType);
        boolean isFunction = UtilSymbolKeys.FUNCTION_KEYWORD_KEY.equals(topLevelNodeType);
        if ((isService || isFunction) && !isTopLevelNode(docUri, documentManager, lsCompiler, context, position)) {
            return commands;
        }

        if (isService) {
            commands.add(Either.forLeft(new Command(CommandConstants.CREATE_TEST_SERVICE_TITLE,
                    CommandConstants.CMD_CREATE_TEST, args)));
        } else if (isFunction) {
            commands.add(Either.forLeft(new Command(CommandConstants.CREATE_TEST_FUNC_TITLE,
                    CommandConstants.CMD_CREATE_TEST, args)));
        }
        return commands;
    }

    private static boolean isTopLevelNode(String docUri, WorkspaceDocumentManager documentManager,
                                          LSCompiler lsCompiler, LSServiceOperationContext context, Position position) {
        Pair<BLangNode, Object> bLangNode = getBLangNode(position.getLine(), position.getCharacter(), docUri,
                                                         documentManager, lsCompiler, context);
        // Only supported for top-level nodes
        return (bLangNode.getLeft().parent instanceof BLangPackage);
    }

    /**
     * Get the command instances for a given diagnostic.
     *
     * @param diagnostic Diagnostic to get the command against
     * @param params     Code Action parameters
     * @return {@link List}     List of commands related to the given diagnostic
     */
    public static List<Either<Command, CodeAction>> getCommandsByDiagnostic(Diagnostic diagnostic,
                                                                            CodeActionParams params) {
        String diagnosticMessage = diagnostic.getMessage();
        List<Either<Command, CodeAction>> commands = new ArrayList<>();
        Position position = params.getRange().getStart();
        CommandArgument lineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE,
                                                      "" + position.getLine());
        CommandArgument colArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN,
                                                     "" + position.getCharacter());
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI,
                                                     params.getTextDocument().getUri());

        if (isUndefinedPackage(diagnosticMessage)) {
            String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                                                              diagnosticMessage.lastIndexOf("'"));
            LSDocument sourceDocument = new LSDocument(params.getTextDocument().getUri());
            String sourceRoot = LSCompilerUtil.getSourceRoot(sourceDocument.getPath());
            sourceDocument.setSourceRoot(sourceRoot);
            List<BallerinaPackage> packagesList = new ArrayList<>();
            Stream.of(LSPackageLoader.getSdkPackages(), LSPackageLoader.getHomeRepoPackages())
                    .forEach(packagesList::addAll);
            packagesList.stream()
                    .filter(pkgEntry -> {
                        String fullPkgName = pkgEntry.getFullPackageNameAlias();
                        return fullPkgName.endsWith("." + packageAlias) || fullPkgName.endsWith("/" + packageAlias);
                    })
                    .forEach(pkgEntry -> {
                        String commandTitle = CommandConstants.IMPORT_MODULE_TITLE + " "
                                + pkgEntry.getFullPackageNameAlias();
                        CommandArgument pkgArgument = new CommandArgument(CommandConstants.ARG_KEY_MODULE_NAME,
                                                                          pkgEntry.getFullPackageNameAlias());
                        commands.add(Either.forLeft(new Command(commandTitle, CommandConstants.CMD_IMPORT_MODULE,
                                                 new ArrayList<>(Arrays.asList(pkgArgument, uriArg)))));
                    });
        } else if (isUndefinedFunction(diagnosticMessage)) {
            List<Object> args = Arrays.asList(lineArg, colArg, uriArg);
            String functionName = "";
            Matcher matcher = CommandConstants.UNDEFINED_FUNCTION_PATTERN.matcher(diagnosticMessage);
            if (matcher.find() && matcher.groupCount() > 0) {
                functionName = matcher.group(1) + "(...)";
            }
            String commandTitle = CommandConstants.CREATE_FUNCTION_TITLE + functionName;
            commands.add(Either.forLeft(new Command(commandTitle, CommandConstants.CMD_CREATE_FUNCTION, args)));
        } else if (isVariableAssignmentRequired(diagnosticMessage)) {
            List<Object> args = Arrays.asList(lineArg, colArg, uriArg);
            String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
            commands.add(Either.forLeft(new Command(commandTitle, CommandConstants.CMD_CREATE_VARIABLE, args)));
        } else if (isUnresolvedPackage(diagnosticMessage)) {
            Matcher matcher = CommandConstants.UNRESOLVED_MODULE_PATTERN.matcher(
                    diagnosticMessage.toLowerCase(Locale.ROOT)
            );
            if (matcher.find() && matcher.groupCount() > 0) {
                List<Object> args = new ArrayList<>();
                String pkgName = matcher.group(1);
                args.add(new CommandArgument(CommandConstants.ARG_KEY_MODULE_NAME, pkgName));
                args.add(uriArg);
                String commandTitle = CommandConstants.PULL_MOD_TITLE;
                commands.add(Either.forLeft(new Command(commandTitle, CommandConstants.CMD_PULL_MODULE, args)));
            }
        }
        return commands;
    }

    /**
     * Get the object constructor snippet generated from public object fields.
     *
     * @param fields     List of Fields
     * @param baseOffset Offset of snippet
     * @return {@link String}   Constructor snippet as String
     */
    public static String getObjectConstructorSnippet(List<BLangSimpleVariable> fields, int baseOffset) {
        StringJoiner funcFields = new StringJoiner(", ");
        StringJoiner funcBody = new StringJoiner(CommonUtil.LINE_SEPARATOR);
        String offsetStr = String.join("", Collections.nCopies(baseOffset, " "));
        fields.stream()
                .filter(bField -> ((bField.symbol.flags & Flags.PUBLIC) != Flags.PUBLIC))
                .forEach(var -> {
                    funcFields.add(generateTypeDefinition(null, null, var) + " " + var.name.value);
                    funcBody.add(offsetStr + "    self." + var.name.value + " = " + var.name.value + ";");
                });

        return offsetStr + "public function __init(" + funcFields.toString() + ") {" + CommonUtil.LINE_SEPARATOR +
                funcBody.toString() + CommonUtil.LINE_SEPARATOR + offsetStr + "}" + CommonUtil.LINE_SEPARATOR;
    }

    /**
     * Sends a message to the language server client.
     *
     * @param client      Language Server client
     * @param messageType message type
     * @param message     message
     */
    public static void notifyClient(LanguageClient client, MessageType messageType, String message) {
        client.showMessage(new MessageParams(messageType, message));
    }

    /**
     * Clears diagnostics of the client by sending an text edit event.
     *
     * @param client            Language Server client
     * @param lsCompiler        Language Server Compiler instance
     * @param diagnosticsHelper diagnostics helper
     * @param documentUri       Current text document URI
     */
    public static void clearDiagnostics(LanguageClient client, LSCompiler lsCompiler,
                                        DiagnosticsHelper diagnosticsHelper, String documentUri) {
        Path filePath = Paths.get(URI.create(documentUri));
        Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath);
        diagnosticsHelper.compileAndSendDiagnostics(client, lsCompiler, filePath, compilationPath);
    }

    /**
     * Apply a given single text edit.
     *
     * @param editText   Edit text to be inserted
     * @param range      Line Range to be processed
     * @param identifier Document identifier
     * @param client     Language Client
     * @return {@link ApplyWorkspaceEditParams}     Workspace edit params
     */
    public static ApplyWorkspaceEditParams applySingleTextEdit(String editText, Range range,
                                                               VersionedTextDocumentIdentifier identifier,
                                                               LanguageClient client) {

        ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
        TextEdit textEdit = new TextEdit(range, editText);
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(identifier,
                                                                 Collections.singletonList(textEdit));
        Either<TextDocumentEdit, ResourceOperation> documentChange = Either.forLeft(textDocumentEdit);
        WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(documentChange));
        applyWorkspaceEditParams.setEdit(workspaceEdit);
        if (client != null) {
            client.applyEdit(applyWorkspaceEditParams);
        }
        return applyWorkspaceEditParams;
    }

    /**
     * Apply a workspace edit for the current instance.
     *
     * @param documentChanges   List of either document edits or set of resource changes for current session
     * @param client            Language Client
     * @return {@link Object}   workspace edit parameters
     */
    public static Object applyWorkspaceEdit(List<Either<TextDocumentEdit, ResourceOperation>> documentChanges,
                                            LanguageClient client) {
        WorkspaceEdit workspaceEdit = new WorkspaceEdit(documentChanges);
        ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams(workspaceEdit);
        if (client != null) {
            client.applyEdit(applyWorkspaceEditParams);
        }
        return applyWorkspaceEditParams;
    }

    public static BLangInvocation getFunctionNode(int line, int column, String uri,
                                                  WorkspaceDocumentManager documentManager, LSCompiler lsCompiler,
                                                  LSContext context) {
        Pair<BLangNode, Object> bLangNode = getBLangNode(line, column, uri, documentManager, lsCompiler, context);
        if (bLangNode.getLeft() instanceof BLangInvocation) {
            return (BLangInvocation) bLangNode.getLeft();
        } else if (bLangNode.getRight() instanceof BLangInvocation) {
            return (BLangInvocation) bLangNode.getRight();
        } else {
            return null;
        }
    }

    public static Pair<BLangNode, Object> getBLangNode(int line, int column, String uri,
                                                       WorkspaceDocumentManager documentManager, LSCompiler lsCompiler,
                                                       LSContext context) {
        Position position = new Position();
        position.setLine(line);
        position.setCharacter(column + 1);
        context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri);
        context.put(DocumentServiceKeys.POSITION_KEY, new TextDocumentPositionParams(identifier, position));
        List<BLangPackage> bLangPackages = lsCompiler.getBLangPackages(context, documentManager, false,
                                                                       LSCustomErrorStrategy.class, true);

        // Get the current package.
        BLangPackage currentBLangPackage = CommonUtil.getCurrentPackageByFileName(bLangPackages, uri);

        context.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY, currentBLangPackage.symbol.getName().getValue());

        // Run the position calculator for the current package.
        PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(context);
        currentBLangPackage.accept(positionTreeVisitor);
        return new ImmutablePair<>(context.get(NodeContextKeys.NODE_KEY),
                                   context.get(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY));
    }

    private static boolean isUndefinedPackage(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_MODULE);
    }

    private static boolean isUndefinedFunction(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNDEFINED_FUNCTION);
    }

    private static boolean isVariableAssignmentRequired(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED);
    }

    private static boolean isUnresolvedPackage(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.UNRESOLVED_MODULE);
    }

    private static Either<Command, CodeAction> getDocGenerationCommand(String nodeType, String docUri, int line) {
        CommandArgument nodeTypeArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_TYPE, nodeType);
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument lineStart = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, String.valueOf(line));
        List<Object> args = new ArrayList<>(Arrays.asList(nodeTypeArg, docUriArg, lineStart));

        return Either.forLeft(new Command(CommandConstants.ADD_DOCUMENTATION_TITLE,
                CommandConstants.CMD_ADD_DOCUMENTATION, args));
    }

    private static Either<Command, CodeAction> getAllDocGenerationCommand(String docUri) {
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        List<Object> args = new ArrayList<>(Collections.singletonList(docUriArg));
        return Either.forLeft(new Command(CommandConstants.ADD_ALL_DOC_TITLE, CommandConstants.CMD_ADD_ALL_DOC, args));
    }

    private static Either<Command, CodeAction> getInitializerGenerationCommand(String docUri, int line) {
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument startLineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, String.valueOf(line));
        List<Object> args = new ArrayList<>(Arrays.asList(docUriArg, startLineArg));
        return Either.forLeft(new Command(CommandConstants.CREATE_INITIALIZER_TITLE,
                CommandConstants.CMD_CREATE_INITIALIZER, args));
    }

    /**
     * Inner class for the command argument holding argument key and argument value.
     */
    public static class CommandArgument {
        private String argumentK;

        private String argumentV;

        public CommandArgument(String argumentK, String argumentV) {
            this.argumentK = argumentK;
            this.argumentV = argumentV;
        }

        public String getArgumentK() {
            return argumentK;
        }

        public String getArgumentV() {
            return argumentV;
        }
    }
}

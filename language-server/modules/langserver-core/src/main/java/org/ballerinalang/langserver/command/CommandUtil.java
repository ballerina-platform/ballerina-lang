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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.langserver.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.command.executors.AddAllDocumentationExecutor;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.ballerinalang.langserver.command.executors.ChangeAbstractTypeObjExecutor;
import org.ballerinalang.langserver.command.executors.CreateFunctionExecutor;
import org.ballerinalang.langserver.command.executors.CreateTestExecutor;
import org.ballerinalang.langserver.command.executors.ImportModuleExecutor;
import org.ballerinalang.langserver.command.executors.PullModuleExecutor;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;
import static org.ballerinalang.langserver.common.utils.CommonUtil.createVariableDeclaration;
import static org.ballerinalang.langserver.common.utils.FunctionGenerator.generateTypeDefinition;
import static org.ballerinalang.langserver.compiler.LSClientLogger.logError;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;
import static org.ballerinalang.langserver.util.references.ReferencesUtil.getReferenceAtCursor;

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
    public static List<CodeAction> getCommandForNodeType(CodeActionNodeType topLevelNodeType, String docUri, int line) {
        List<CodeAction> actions = new ArrayList<>();
        actions.add(getDocGenerationCommand(topLevelNodeType.name(), docUri, line));
        actions.add(getAllDocGenerationCommand(docUri));
        return actions;
    }

    /**
     * Get the command for generate test class.
     *
     * @param topLevelNodeType top level node
     * @param docUri Document URI
     * @param params Code action parameters
     * @param context {@link LSContext}
     * @return {@link Command} Test Generation command
     * @throws CompilationFailedException thrown when compilation failed
     */
    public static List<CodeAction> getTestGenerationCommand(String topLevelNodeType, String docUri,
                                                            CodeActionParams params, LSContext context)
            throws CompilationFailedException {
        List<CodeAction> actions = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri));
        Position position = params.getRange().getStart();
        args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + position.getLine()));
        args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + position.getCharacter()));

        boolean isService = CommonKeys.SERVICE_KEYWORD_KEY.equals(topLevelNodeType);
        boolean isFunction = CommonKeys.FUNCTION_KEYWORD_KEY.equals(topLevelNodeType);
        WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        if ((isService || isFunction) && !isTopLevelNode(docUri, documentManager, context, position)) {
            return actions;
        }

        if (isService) {
            CodeAction action = new CodeAction(CommandConstants.CREATE_TEST_SERVICE_TITLE);
            action.setCommand(new Command(CommandConstants.CREATE_TEST_SERVICE_TITLE,
                                          CreateTestExecutor.COMMAND, args));
            actions.add(action);
        } else if (isFunction) {
            CodeAction action = new CodeAction(CommandConstants.CREATE_TEST_FUNC_TITLE);
            action.setCommand(new Command(CommandConstants.CREATE_TEST_FUNC_TITLE,
                                          CreateTestExecutor.COMMAND, args));
            actions.add(action);
        }
        return actions;
    }

    private static boolean isTopLevelNode(String uri, WorkspaceDocumentManager docManager, LSContext context,
                                          Position position)
            throws CompilationFailedException {
        Pair<BLangNode, Object> bLangNode = getBLangNode(position.getLine(), position.getCharacter(), uri, docManager,
                                                         context);

        // Only supported for 'public' functions
        if (bLangNode.getLeft() instanceof BLangFunction &&
                !((BLangFunction) bLangNode.getLeft()).getFlags().contains(Flag.PUBLIC)) {
            return false;
        }

        // Only supported for top-level nodes
        return (bLangNode.getLeft() != null && bLangNode.getLeft().parent instanceof BLangPackage);
    }

    /**
     * Get the command instances for a given diagnostic.
     *
     *
     * @param document  {@link LSDocument}
     * @param diagnostic Diagnostic to get the command against
     * @param params     Code Action parameters
     * @param context    context
     * @return {@link List}     List of commands related to the given diagnostic
     */
    public static List<CodeAction> getCommandsByDiagnostic(LSDocument document, Diagnostic diagnostic,
                                                           CodeActionParams params, LSContext context) {
        String diagnosticMessage = diagnostic.getMessage();
        List<CodeAction> actions = new ArrayList<>();
        Position position = diagnostic.getRange().getStart();
        int line = position.getLine();
        int column = position.getCharacter();
        String uri = params.getTextDocument().getUri();
        CommandArgument lineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + line);
        CommandArgument colArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + column);
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, uri);
        List<Diagnostic> diagnostics = new ArrayList<>();
        diagnostics.add(diagnostic);
        String diagnosedContent = getDiagnosedContent(diagnostic, context, document);
        if (isUndefinedPackage(diagnosticMessage)) {
            String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                                                              diagnosticMessage.lastIndexOf("'"));
            LSDocument sourceDocument = new LSDocument(uri);
            String sourceRoot = LSCompilerUtil.getProjectRoot(sourceDocument.getPath());
            sourceDocument.setProjectRootRoot(sourceRoot);
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
                        CodeAction action = new CodeAction(commandTitle);
                        action.setKind(CodeActionKind.QuickFix);
                        action.setCommand(new Command(commandTitle, ImportModuleExecutor.COMMAND,
                                                      new ArrayList<>(Arrays.asList(pkgArgument, uriArg))));
                        action.setDiagnostics(diagnostics);
                        actions.add(action);
                    });
        } else if (isUndefinedFunction(diagnosticMessage)) {
            List<Object> args = Arrays.asList(lineArg, colArg, uriArg);
            Matcher matcher = CommandConstants.UNDEFINED_FUNCTION_PATTERN.matcher(diagnosticMessage);
            String functionName = (matcher.find() && matcher.groupCount() > 0) ? matcher.group(1) + "(...)" : "";
            WorkspaceDocumentManager docManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
            try {
                BLangInvocation node = getFunctionInvocationNode(line, column, document.getURIString(), docManager,
                                                                 context);
                if (node != null && node.pkgAlias.value.isEmpty()) {
                    boolean isWithinProject = (node.expr == null);
                    if (node.expr != null) {
                        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
                        List<String> currentModules = document.getProjectModules();
                        PackageID nodePkgId = node.expr.type.tsymbol.pkgID;
                        isWithinProject = bLangPackage.packageID.orgName.equals(nodePkgId.orgName) &&
                                currentModules.contains(nodePkgId.name.value);
                    }
                    if (isWithinProject) {
                        String commandTitle = CommandConstants.CREATE_FUNCTION_TITLE + functionName;
                        CodeAction action = new CodeAction(commandTitle);
                        action.setKind(CodeActionKind.QuickFix);
                        action.setCommand(new Command(commandTitle, CreateFunctionExecutor.COMMAND, args));
                        action.setDiagnostics(diagnostics);
                        actions.add(action);
                    }
                }
            } catch (CompilationFailedException e) {
                // ignore
            }
        } else if (isVariableAssignmentRequired(diagnosticMessage)) {
            try {
                Position afterAliasPos = offsetInvocation(diagnosedContent, position);
                SymbolReferencesModel.Reference refAtCursor = getReferenceAtCursor(context, document, afterAliasPos);
                BSymbol symbolAtCursor = refAtCursor.getSymbol();

                boolean isInvocation = symbolAtCursor instanceof BInvokableSymbol;
                boolean isRemoteInvocation = (symbolAtCursor.flags & Flags.REMOTE) == Flags.REMOTE;

                boolean hasDefaultInitFunction = false;
                boolean hasCustomInitFunction = false;
                if (refAtCursor.getbLangNode() instanceof BLangInvocation) {
                    hasDefaultInitFunction = symbolAtCursor instanceof BObjectTypeSymbol;
                    hasCustomInitFunction = symbolAtCursor instanceof BInvokableSymbol &&
                            symbolAtCursor.name.value.endsWith("__init");
                }
                boolean isInitInvocation = hasDefaultInitFunction || hasCustomInitFunction;

                String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
                CodeAction action = new CodeAction(commandTitle);
                List<TextEdit> edits = getCreateVariableCodeActionEdits(context, uri, refAtCursor,
                                                                        hasDefaultInitFunction, hasCustomInitFunction);
                action.setKind(CodeActionKind.QuickFix);
                action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                        new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits)))));
                action.setDiagnostics(diagnostics);
                actions.add(action);

                if (isInvocation || isInitInvocation) {
                    BType returnType;
                    if (hasDefaultInitFunction) {
                        returnType = symbolAtCursor.type;
                    } else if (hasCustomInitFunction) {
                        returnType = symbolAtCursor.owner.type;
                    } else {
                        returnType = ((BInvokableSymbol) symbolAtCursor).retType;
                    }
                    boolean hasError = false;
                    if (returnType instanceof BErrorType) {
                        hasError = true;
                    } else if (returnType instanceof BUnionType) {
                        BUnionType unionType = (BUnionType) returnType;
                        hasError = unionType.getMemberTypes().stream().anyMatch(s -> s instanceof BErrorType);
                        if (!isRemoteInvocation) {
                            // Add type guard code action
                            commandTitle = String.format(CommandConstants.TYPE_GUARD_TITLE, symbolAtCursor.name);
                            List<TextEdit> tEdits = getTypeGuardCodeActionEdits(context, uri, refAtCursor, unionType);
                            action = new CodeAction(commandTitle);
                            action.setKind(CodeActionKind.QuickFix);
                            action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                                    new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), tEdits)))));
                            action.setDiagnostics(diagnostics);
                            actions.add(action);
                        }
                    }
                    // Add ignore return value code action
                    if (!hasError) {
                        List<TextEdit> iEdits = getIgnoreCodeActionEdits(refAtCursor);
                        commandTitle = CommandConstants.IGNORE_RETURN_TITLE;
                        action = new CodeAction(commandTitle);
                        action.setKind(CodeActionKind.QuickFix);
                        action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                                new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), iEdits)))));
                        action.setDiagnostics(diagnostics);
                        actions.add(action);
                    }
                }
            } catch (CompilationFailedException | WorkspaceDocumentException | IOException e) {
                // ignore
            }
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
                CodeAction action = new CodeAction(commandTitle);
                action.setKind(CodeActionKind.QuickFix);
                action.setCommand(new Command(commandTitle, PullModuleExecutor.COMMAND, args));
                action.setDiagnostics(diagnostics);
                actions.add(action);
            }
        } else if (isIncompatibleTypes(diagnosticMessage)) {
            Matcher matcher = CommandConstants.INCOMPATIBLE_TYPE_PATTERN.matcher(diagnosticMessage);
            if (matcher.find() && matcher.groupCount() > 1) {
                String foundType = matcher.group(2);
                WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
                try {
                    BLangFunction func = CommandUtil.getFunctionNode(line, column, document, documentManager, context);
                    if (func != null && !BLangConstants.MAIN_FUNCTION_NAME.equals(func.name.value)) {
                        BLangStatement statement = CommandUtil.getStatementByLocation(func.getBody().getStatements(),
                                                                                      line + 1, column + 1);
                        if (statement instanceof BLangReturn) {
                            // Process full-qualified BType name  eg. ballerina/http:Client and if required; add
                            // auto-import
                            matcher = CommandConstants.FQ_TYPE_PATTERN.matcher(foundType);
                            List<TextEdit> edits = new ArrayList<>();
                            String editText = extractTypeName(context, matcher, foundType, edits);

                            // Process function node
                            Position start;
                            Position end;
                            if (func.returnTypeNode instanceof BLangValueType
                                    && TypeKind.NIL.equals(((BLangValueType) func.returnTypeNode).getTypeKind())
                                    && func.returnTypeNode.getWS() == null) {
                                // eg. function test() {...}
                                start = new Position(func.returnTypeNode.pos.sLine - 1,
                                                     func.returnTypeNode.pos.eCol - 1);
                                end = new Position(func.returnTypeNode.pos.eLine - 1, func.returnTypeNode.pos.eCol - 1);
                                editText = " returns (" + editText + ")";
                            } else {
                                // eg. function test() returns () {...}
                                start = new Position(func.returnTypeNode.pos.sLine - 1,
                                                     func.returnTypeNode.pos.sCol - 1);
                                end = new Position(func.returnTypeNode.pos.eLine - 1, func.returnTypeNode.pos.eCol - 1);
                            }
                            edits.add(new TextEdit(new Range(start, end), editText));

                            // Add code action
                            String commandTitle = CommandConstants.CHANGE_RETURN_TYPE_TITLE + foundType + "'";
                            CodeAction action = new CodeAction(commandTitle);
                            action.setKind(CodeActionKind.QuickFix);
                            action.setDiagnostics(diagnostics);
                            action.setEdit(new WorkspaceEdit(Collections.singletonList(
                                    Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null),
                                                                        edits))))
                            );
                            actions.add(action);
                        }
                    }
                } catch (CompilationFailedException e) {
                    // ignore
                }
            }
        } else if (isTaintedParamPassed(diagnosticMessage)) {
            try {
                Matcher matcher = CommandConstants.TAINTED_PARAM_PATTERN.matcher(diagnosticMessage);
                if (matcher.find() && matcher.groupCount() > 0) {
                    String param = matcher.group(1);
                    String commandTitle = String.format(CommandConstants.MARK_UNTAINTED_TITLE, param);
                    CodeAction action = new CodeAction(commandTitle);
                    action.setKind(CodeActionKind.QuickFix);
                    action.setDiagnostics(diagnostics);
                    // Extract specific content range
                    Range range = diagnostic.getRange();
                    WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
                    String content = getContentOfRange(documentManager, uri, range);
                    // Add `untaint` keyword
                    matcher = CommandConstants.NO_CONCAT_PATTERN.matcher(content);
                    String editText = matcher.find() ? "<@untained>  " + content : "<@untained> (" + content + ")";
                    // Create text-edit
                    List<TextEdit> edits = new ArrayList<>();
                    edits.add(new TextEdit(range, editText));
                    VersionedTextDocumentIdentifier identifier = new VersionedTextDocumentIdentifier(uri, null);
                    action.setEdit(new WorkspaceEdit(Collections.singletonList(
                            Either.forLeft(new TextDocumentEdit(identifier, edits)))));
                    actions.add(action);
                }
            } catch (WorkspaceDocumentException | IOException e) {
                //do nothing
            }
        } else if (isFuncSignatureInNonAbstractObj(diagnosticMessage)) {
            Matcher matcher = CommandConstants.NO_IMPL_FOUND_FOR_FUNCTION_PATTERN.matcher(diagnosticMessage);
            if (matcher.find() && matcher.groupCount() > 1) {
                String objectName = matcher.group(2);
                int colonIndex = objectName.lastIndexOf(":");
                String simpleObjName = (colonIndex > -1) ? objectName.substring(colonIndex + 1) : objectName;
                List<Object> args = Arrays.asList(lineArg, colArg, uriArg);
                String commandTitle = String.format(CommandConstants.MAKE_OBJ_ABSTRACT_TITLE, simpleObjName);

                CodeAction action = new CodeAction(commandTitle);
                action.setKind(CodeActionKind.QuickFix);
                action.setCommand(new Command(commandTitle, ChangeAbstractTypeObjExecutor.COMMAND, args));
                action.setDiagnostics(diagnostics);
                actions.add(action);
            }
        } else if (hasFuncBodyInAbstractObj(diagnosticMessage)) {
            Matcher matcher = CommandConstants.FUNC_IN_ABSTRACT_OBJ_PATTERN.matcher(diagnosticMessage);
            if (matcher.find() && matcher.groupCount() > 1) {
                String objectName = matcher.group(2);
                int colonIndex = objectName.lastIndexOf(":");
                String simpleObjName = (colonIndex > -1) ? objectName.substring(colonIndex + 1) : objectName;
                List<Object> args = Arrays.asList(lineArg, colArg, uriArg);
                String commandTitle = String.format(CommandConstants.MAKE_OBJ_NON_ABSTRACT_TITLE, simpleObjName);

                CodeAction action = new CodeAction(commandTitle);
                action.setKind(CodeActionKind.QuickFix);
                action.setCommand(new Command(commandTitle, ChangeAbstractTypeObjExecutor.COMMAND, args));
                action.setDiagnostics(diagnostics);
                actions.add(action);
            }
        }
        return actions;
    }

    private static Position offsetInvocation(String diagnosedContent, Position position) {
        // Diagnosed message only contains the erroneous part of the line
        // Thus we offset into last
        String quotesRemoved = diagnosedContent
                .replaceAll(".*:", "") // package invocation
                .replaceAll(".*->", "") // action invocation
                .replaceAll(".*\\.", ""); // object access invocation
        int bal = diagnosedContent.length() - quotesRemoved.length();
        if (bal > 0) {
            position.setCharacter(position.getCharacter() + bal + 1);
        }
        return position;
    }

    private static String getDiagnosedContent(Diagnostic diagnostic, LSContext context, LSDocument document) {
        WorkspaceDocumentManager docManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        StringBuilder content = new StringBuilder();
        Position start = diagnostic.getRange().getStart();
        Position end = diagnostic.getRange().getEnd();
        try (BufferedReader reader = new BufferedReader(
                new StringReader(docManager.getFileContent(document.getPath())))) {
            String strLine;
            int count = 0;
            while ((strLine = reader.readLine()) != null) {
                if (count >= start.getLine() && count <= end.getLine()) {
                    if (count == start.getLine()) {
                        content.append(strLine.substring(start.getCharacter()));
                        if (start.getLine() != end.getLine()) {
                            content.append(System.lineSeparator());
                        }
                    } else if (count == end.getLine()) {
                        content.append(strLine.substring(0, end.getCharacter()));
                    } else {
                        content.append(strLine).append(System.lineSeparator());
                    }
                }
                if (count == end.getLine()) {
                    break;
                }
                count++;
            }
        } catch (WorkspaceDocumentException | IOException e) {
            // ignore error
        }
        return content.toString();
    }

    private static List<TextEdit> getCreateVariableCodeActionEdits(LSContext context, String uri,
                                                                   SymbolReferencesModel.Reference referenceAtCursor,
                                                                   boolean hasDefaultInitFunction,
                                                                   boolean hasCustomInitFunction) {
        BLangNode bLangNode = referenceAtCursor.getbLangNode();
        List<TextEdit> edits = new ArrayList<>();
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        Set<String> nameEntries = CommonUtil.getAllNameEntries(bLangNode, compilerContext);
        String variableName = CommonUtil.generateVariableName(bLangNode, nameEntries);

        PackageID currentPkgId = bLangNode.pos.src.pkgID;
        BiConsumer<String, String> importsAcceptor = (orgName, alias) -> {
            boolean notFound = CommonUtil.getCurrentModuleImports(context).stream().noneMatch(
                    pkg -> (pkg.orgName.value.equals(orgName) && pkg.alias.value.equals(alias))
            );
            if (notFound) {
                String pkgName = orgName + "/" + alias;
                edits.add(addPackage(pkgName, context));
            }
        };
        String variableType;
        if (hasDefaultInitFunction) {
            BType bType = referenceAtCursor.getSymbol().type;
            variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bType);
            variableName = CommonUtil.generateVariableName(bType, nameEntries);
        } else if (hasCustomInitFunction) {
            BType bType = referenceAtCursor.getSymbol().owner.type;
            variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bType);
            variableName = CommonUtil.generateVariableName(bType, nameEntries);
        } else {
            variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bLangNode.type);
        }
        // Remove brackets of the unions
        variableType = variableType.replaceAll("^\\((.*)\\)$", "$1");
        String editText = createVariableDeclaration(variableName, variableType);
        Position position = new Position(bLangNode.pos.sLine - 1, bLangNode.pos.sCol - 1);
        edits.add(new TextEdit(new Range(position, position), editText));
        return edits;
    }

    private static TextEdit addPackage(String pkgName, LSContext context) {
        DiagnosticPos pos = null;

        // Filter the imports except the runtime import
        List<BLangImportPackage> imports = CommonUtil.getCurrentModuleImports(context);

        if (!imports.isEmpty()) {
            BLangImportPackage lastImport = CommonUtil.getLastItem(imports);
            pos = lastImport.getPosition();
        }

        int endCol = 0;
        int endLine = pos == null ? 0 : pos.getEndLine();

        String editText = "import " + pkgName + ";\n";
        Range range = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        return new TextEdit(range, editText);
    }

    private static List<TextEdit> getIgnoreCodeActionEdits(SymbolReferencesModel.Reference referenceAtCursor) {
        String editText = "_ = ";
        BLangNode bLangNode = referenceAtCursor.getbLangNode();
        Position position = new Position(bLangNode.pos.sLine - 1, bLangNode.pos.sCol - 1);
        List<TextEdit> edits = new ArrayList<>();
        edits.add(new TextEdit(new Range(position, position), editText));
        return edits;

    }

    private static List<TextEdit> getTypeGuardCodeActionEdits(LSContext context, String uri,
                                                              SymbolReferencesModel.Reference referenceAtCursor,
                                                              BUnionType unionType)
            throws WorkspaceDocumentException, IOException {
        WorkspaceDocumentManager docManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        BLangNode bLangNode = referenceAtCursor.getbLangNode();
        Position startPos = new Position(bLangNode.pos.sLine - 1, bLangNode.pos.sCol - 1);
        Position endPosWithSemiColon = new Position(bLangNode.pos.eLine - 1, bLangNode.pos.eCol);
        Position endPos = new Position(bLangNode.pos.eLine - 1, bLangNode.pos.eCol - 1);
        Range newTextRange = new Range(startPos, endPosWithSemiColon);

        List<TextEdit> edits = new ArrayList<>();
        String spaces = StringUtils.repeat(' ', bLangNode.pos.sCol - 1);
        String padding = LINE_SEPARATOR + LINE_SEPARATOR + spaces;
        String content = getContentOfRange(docManager, uri, new Range(startPos, endPos));
        boolean hasError = unionType.getMemberTypes().stream().anyMatch(s -> s instanceof BErrorType);

        List<BType> members = new ArrayList<>((unionType).getMemberTypes());
        long errorTypesCount = unionType.getMemberTypes().stream().filter(t -> t instanceof BErrorType).count();
        boolean transitiveBinaryUnion = unionType.getMemberTypes().size() - errorTypesCount == 1;
        if (transitiveBinaryUnion) {
            members.removeIf(s -> s instanceof BErrorType);
        }
        // Check is binary union type with error type
        if ((unionType.getMemberTypes().size() == 2 || transitiveBinaryUnion) && hasError) {
            members.forEach(bType -> {
                        if (bType instanceof BNilType) {
                            // if (foo() is error) {...}
                            String newText = String.format("if (%s is error) {%s}", content, padding);
                            edits.add(new TextEdit(newTextRange, newText));
                        } else {
                            // if (foo() is int) {...} else {...}
                            String type = CommonUtil.getBTypeName(bType, context, true);
                            String newText = String.format("if (%s is %s) {%s} else {%s}",
                                                           content, type, padding, padding);
                            edits.add(new TextEdit(newTextRange, newText));
                        }
                    });
        } else {
            CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
            Set<String> nameEntries = CommonUtil.getAllNameEntries(bLangNode, compilerContext);
            String varName = CommonUtil.generateVariableName(bLangNode, nameEntries);
            String typeDef = CommonUtil.getBTypeName(unionType, context, true);
            boolean addErrorTypeAtEnd;

            List<BType> tMembers = new ArrayList<>((unionType).getMemberTypes());
            if (errorTypesCount > 1) {
                tMembers.removeIf(s -> s instanceof BErrorType);
                addErrorTypeAtEnd = true;
            } else {
                addErrorTypeAtEnd = false;
            }
            List<String> memberTypes = new ArrayList<>();
            IntStream.range(0, tMembers.size())
                    .forEachOrdered(value -> {
                        BType bType = tMembers.get(value);
                        String bTypeName = CommonUtil.getBTypeName(bType, context, true);
                        boolean isErrorType = bType instanceof BErrorType;
                        if (isErrorType && !addErrorTypeAtEnd) {
                            memberTypes.add(bTypeName);
                        } else if (!isErrorType) {
                            memberTypes.add(bTypeName);
                        }
                    });

            if (addErrorTypeAtEnd) {
                memberTypes.add("error");
            }

            String newText = String.format("%s %s = %s;%s", typeDef, varName, content, LINE_SEPARATOR);
            newText += spaces + IntStream.range(0, memberTypes.size() - 1)
                    .mapToObj(value -> {
                        return String.format("if (%s is %s) {%s}", varName, memberTypes.get(value), padding);
                    })
                    .collect(Collectors.joining(" else "));
            newText += String.format(" else {%s}", padding);
            edits.add(new TextEdit(newTextRange, newText));
        }
        return edits;
    }

    private static String extractTypeName(LSContext context, Matcher matcher, String foundType, List<TextEdit> edits) {
        if (matcher.find() && matcher.groupCount() > 2) {
            String orgName = matcher.group(1);
            String alias = matcher.group(2);
            String typeName = matcher.group(3);
            String pkgId = orgName + "/" + alias;
            PackageID currentPkgId = context.get(
                    DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY).packageID;
            if (pkgId.equals(currentPkgId.toString())) {
                foundType = typeName;
            } else {
                List<BLangImportPackage> currentDocImports = CommonUtil.getCurrentFileImports(context);
                boolean pkgAlreadyImported = currentDocImports.stream()
                        .anyMatch(importPkg -> importPkg.orgName.value.equals(orgName)
                                && importPkg.alias.value.equals(alias));
                if (!pkgAlreadyImported) {
                    edits.addAll(CommonUtil.getAutoImportTextEdits(orgName, alias, context));
                }
                foundType = alias + CommonKeys.PKG_DELIMITER_KEYWORD + typeName;
            }
        }
        return foundType;
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
        StringJoiner funcBody = new StringJoiner(LINE_SEPARATOR);
        String offsetStr = String.join("", Collections.nCopies(baseOffset, " "));
        fields.stream()
                .filter(bField -> ((bField.symbol.flags & Flags.PUBLIC) != Flags.PUBLIC))
                .forEach(var -> {
                    funcFields.add(generateTypeDefinition(null, null, var) + " " + var.name.value);
                    funcBody.add(offsetStr + "    self." + var.name.value + " = " + var.name.value + ";");
                });

        return offsetStr + "public function __init(" + funcFields.toString() + ") {" + LINE_SEPARATOR +
                funcBody.toString() + LINE_SEPARATOR + offsetStr + "}" + LINE_SEPARATOR;
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
     * @param client Language Server client
     * @param diagHelper diagnostics helper
     * @param documentUri Current text document URI
     * @param context   {@link LSContext}
     */
    public static void clearDiagnostics(LanguageClient client, DiagnosticsHelper diagHelper, String documentUri,
                                        LSContext context) {
        context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
        WorkspaceDocumentManager docManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        try {
            LSDocument lsDocument = new LSDocument(documentUri);
            diagHelper.compileAndSendDiagnostics(client, context, lsDocument, docManager);
        } catch (CompilationFailedException e) {
            String msg = "Computing 'diagnostics' failed!";
            TextDocumentIdentifier identifier = new TextDocumentIdentifier(documentUri);
            logError(msg, e, identifier, (Position) null);
        }
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

    public static BLangObjectTypeNode getObjectNode(int line, int column, String uri,
                                                    WorkspaceDocumentManager documentManager, LSContext context)
            throws CompilationFailedException {
        Pair<BLangNode, Object> bLangNode = getBLangNode(line, column, uri, documentManager, context);
        if (bLangNode.getLeft() instanceof BLangObjectTypeNode) {
            return (BLangObjectTypeNode) bLangNode.getLeft();
        }
        if (bLangNode.getRight() instanceof BLangObjectTypeNode) {
            return (BLangObjectTypeNode) bLangNode.getRight();
        } else {
            BLangNode parent = bLangNode.getLeft().parent;
            while (parent != null) {
                if (parent instanceof BLangObjectTypeNode) {
                    return (BLangObjectTypeNode) parent;
                }
                parent = parent.parent;
            }
            return null;
        }
    }

    public static BLangInvocation getFunctionInvocationNode(int line, int column, String uri,
                                                            WorkspaceDocumentManager documentManager, LSContext context)
            throws CompilationFailedException {
        Pair<BLangNode, Object> bLangNode = getBLangNode(line, column, uri, documentManager, context);
        if (bLangNode.getLeft() instanceof BLangInvocation) {
            return (BLangInvocation) bLangNode.getLeft();
        } else if (bLangNode.getRight() instanceof BLangInvocation) {
            return (BLangInvocation) bLangNode.getRight();
        } else {
            BLangNode parent = bLangNode.getLeft().parent;
            while (parent != null) {
                if (parent instanceof BLangInvocation) {
                    return (BLangInvocation) parent;
                }
                parent = parent.parent;
            }
            return null;
        }
    }

    private static BLangFunction getFunctionNode(int line, int column, LSDocument document,
                                                 WorkspaceDocumentManager docManager,  LSContext context)
            throws CompilationFailedException {
        String uri = document.getURIString();
        Position position = new Position();
        position.setLine(line);
        position.setCharacter(column + 1);
        context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri);
        context.put(DocumentServiceKeys.POSITION_KEY, new TextDocumentPositionParams(identifier, position));
        LSModuleCompiler.getBLangPackages(context, docManager, LSCustomErrorStrategy.class, true, false, false);

        // Get the current package.
        BLangPackage currentPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);

        // If package is testable package process as tests
        // else process normally
        String relativeFilePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangCompilationUnit compilationUnit;
        if (relativeFilePath.startsWith("tests" + File.separator)) {
            compilationUnit = currentPackage.getTestablePkg().getCompilationUnits().stream().
                    filter(compUnit -> (relativeFilePath).equals(compUnit.getName()))
                    .findFirst().orElse(null);
        } else {
            compilationUnit = currentPackage.getCompilationUnits().stream().
                    filter(compUnit -> relativeFilePath.equals(compUnit.getName())).findFirst().orElse(null);
        }
        if (compilationUnit == null) {
            return null;
        }
        Iterator<TopLevelNode> nodeIterator = compilationUnit.getTopLevelNodes().iterator();
        BLangFunction result = null;
        TopLevelNode next = (nodeIterator.hasNext()) ? nodeIterator.next() : null;
        Function<org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition, Boolean> isWithinPosition =
                diagnosticPosition -> {
                    int sLine = diagnosticPosition.getStartLine();
                    int eLine = diagnosticPosition.getEndLine();
                    int sCol = diagnosticPosition.getStartColumn();
                    int eCol = diagnosticPosition.getEndColumn();
                    return ((line > sLine || (line == sLine && column >= sCol)) &&
                            (line < eLine || (line == eLine && column <= eCol)));
                };
        while (next != null) {
            if (isWithinPosition.apply(next.getPosition())) {
                if (next instanceof BLangFunction) {
                    result = (BLangFunction) next;
                    break;
                } else if (next instanceof BLangTypeDefinition) {
                    BLangTypeDefinition typeDefinition = (BLangTypeDefinition) next;
                    if (typeDefinition.typeNode instanceof BLangObjectTypeNode) {
                        BLangObjectTypeNode typeNode = (BLangObjectTypeNode) typeDefinition.typeNode;
                        for (BLangFunction function : typeNode.functions) {
                            if (isWithinPosition.apply(function.getPosition())) {
                                result = function;
                                break;
                            }
                        }
                    }
                } else if (next instanceof BLangService) {
                    BLangService bLangService = (BLangService) next;
                    for (BLangFunction function : bLangService.resourceFunctions) {
                        if (isWithinPosition.apply(function.getPosition())) {
                            result = function;
                            break;
                        }
                    }
                }
                break;
            }
            next = (nodeIterator.hasNext()) ? nodeIterator.next() : null;
        }
        return result;
    }

    /**
     * Find statement by location.
     *
     * @param statements list of statements
     * @param line       line
     * @param column     column
     * @return {@link BLangStatement} if found, NULL otherwise
     */
    public static BLangStatement getStatementByLocation(List<BLangStatement> statements, int line, int column) {
        BLangStatement node = null;
        for (BLangStatement statement : statements) {
            BLangStatement rv;
            if ((rv = getStatementByLocation(statement, line, column)) != null) {
                return rv;
            }
        }
        return node;
    }

    /**
     * Find statements by location.
     *
     * @param node   lookup {@link BLangNode}
     * @param line   line
     * @param column column
     * @return {@link BLangStatement} if found, NULL otherwise
     */
    public static BLangStatement getStatementByLocation(BLangNode node, int line, int column) {
        try {
            if (checkNodeWithin(node, line, column) && node instanceof BLangStatement) {
                return (BLangStatement) node;
            }
            for (Field field : node.getClass().getDeclaredFields()) {
                Object obj = field.get(node);
                if (obj instanceof BLangBlockStmt) {
                    // Found a block-statement field, drilling down further
                    BLangStatement rv;
                    if ((rv = getStatementByLocation(((BLangBlockStmt) obj).getStatements(), line, column)) != null) {
                        return rv;
                    }
                } else if (obj instanceof BLangStatement) {
                    if (checkNodeWithin((BLangStatement) obj, line, column)) {
                        return (BLangStatement) obj;
                    }
                    // Found a statement field, drilling down further
                    BLangStatement rv;
                    if ((rv = getStatementByLocation((BLangStatement) obj, line, column)) != null) {
                        return rv;
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return null;
        }
        return null;
    }

    public static Pair<BLangNode, Object> getBLangNode(int line, int column, String uri,
                                                       WorkspaceDocumentManager documentManager, LSContext context)
            throws CompilationFailedException {
        Position position = new Position();
        position.setLine(line);
        position.setCharacter(column + 1);
        context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri);
        context.put(DocumentServiceKeys.POSITION_KEY, new TextDocumentPositionParams(identifier, position));
        List<BLangPackage> bLangPackages = LSModuleCompiler.getBLangPackages(context, documentManager,
                LSCustomErrorStrategy.class, true, false, true);
        context.put(DocumentServiceKeys.BLANG_PACKAGES_CONTEXT_KEY, bLangPackages);
        // Get the current package.
        BLangPackage currentBLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        // Run the position calculator for the current package.
        PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(context);
        currentBLangPackage.accept(positionTreeVisitor);
        return new ImmutablePair<>(context.get(NodeContextKeys.NODE_KEY),
                                   context.get(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY));
    }

    private static boolean checkNodeWithin(BLangNode node, int line, int column) {
        int sLine = node.getPosition().getStartLine();
        int eLine = node.getPosition().getEndLine();
        int sCol = node.getPosition().getStartColumn();
        int eCol = node.getPosition().getEndColumn();
        return (line > sLine || (line == sLine && column >= sCol)) &&
                (line < eLine || (line == eLine && column <= eCol));
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

    private static boolean isIncompatibleTypes(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.INCOMPATIBLE_TYPES);
    }

    private static boolean isTaintedParamPassed(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.TAINTED_PARAM_PASSED);
    }

    private static boolean isFuncSignatureInNonAbstractObj(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.NO_IMPL_FOUND_FOR_FUNCTION);
    }

    private static boolean hasFuncBodyInAbstractObj(String diagnosticMessage) {
        return diagnosticMessage.toLowerCase(Locale.ROOT).contains(CommandConstants.FUNC_IMPL_FOUND_IN_ABSTRACT_OBJ);
    }

    private static CodeAction getDocGenerationCommand(String nodeType, String docUri, int line) {
        CommandArgument nodeTypeArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_TYPE, nodeType);
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument lineStart = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, String.valueOf(line));
        List<Object> args = new ArrayList<>(Arrays.asList(nodeTypeArg, docUriArg, lineStart));
        CodeAction action = new CodeAction(CommandConstants.ADD_DOCUMENTATION_TITLE);
        action.setCommand(new Command(CommandConstants.ADD_DOCUMENTATION_TITLE,
                                      AddDocumentationExecutor.COMMAND, args));
        return action;
    }

    private static CodeAction getAllDocGenerationCommand(String docUri) {
        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
        List<Object> args = new ArrayList<>(Collections.singletonList(docUriArg));
        CodeAction action = new CodeAction(CommandConstants.ADD_ALL_DOC_TITLE);
        action.setCommand(new Command(CommandConstants.ADD_ALL_DOC_TITLE, AddAllDocumentationExecutor.COMMAND, args));
        return action;
    }

//    private static CodeAction getInitializerGenerationCommand(String docUri, int line) {
//        CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri);
//        CommandArgument startLineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, String.valueOf(line));
//        List<Object> args = new ArrayList<>(Arrays.asList(docUriArg, startLineArg));
//        CodeAction codeAction = new CodeAction(CommandConstants.CREATE_INITIALIZER_TITLE);
//        codeAction.setCommand(new Command(CommandConstants.CREATE_INITIALIZER_TITLE,
//                                          CreateObjectInitializerExecutor.COMMAND, args));
//        return codeAction;
//    }

    private static String getContentOfRange(WorkspaceDocumentManager documentManager, String uri, Range range)
            throws WorkspaceDocumentException, IOException {
        Optional<Path> filePath = CommonUtil.getPathFromURI(uri);
        if (!filePath.isPresent()) {
            return "";
        }
        Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath.get());
        String fileContent = documentManager.getFileContent(compilationPath);

        BufferedReader reader = new BufferedReader(new StringReader(fileContent));
        StringBuilder capture = new StringBuilder();
        int lineNum = 1;
        int sLine = range.getStart().getLine() + 1;
        int eLine = range.getEnd().getLine() + 1;
        int sChar = range.getStart().getCharacter();
        int eChar = range.getEnd().getCharacter();
        String line;
        while ((line = reader.readLine()) != null && lineNum <= eLine) {
            if (lineNum >= sLine) {
                if (sLine == eLine) {
                    // single line range
                    capture.append(line, sChar, eChar);
                    if (line.length() == eChar) {
                        capture.append(System.lineSeparator());
                    }
                } else if (lineNum == sLine) {
                    // range start line
                    capture.append(line.substring(sChar)).append(System.lineSeparator());
                } else if (lineNum == eLine) {
                    // range end line
                    capture.append(line, 0, eChar);
                    if (line.length() == eChar) {
                        capture.append(System.lineSeparator());
                    }
                } else {
                    // range middle line
                    capture.append(line).append(System.lineSeparator());
                }
            }
            lineNum++;
        }
        return capture.toString();
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

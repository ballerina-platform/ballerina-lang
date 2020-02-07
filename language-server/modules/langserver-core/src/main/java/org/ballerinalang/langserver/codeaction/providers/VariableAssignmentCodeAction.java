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
package org.ballerinalang.langserver.codeaction.providers;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.expressions.IndexBasedAccessNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;
import static org.ballerinalang.langserver.common.utils.CommonUtil.createVariableDeclaration;
import static org.ballerinalang.langserver.util.references.ReferencesUtil.getReferenceAtCursor;

/**
 * Code Action provider for variable assignment.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class VariableAssignmentCodeAction extends AbstractCodeActionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<Diagnostic> diagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        WorkspaceDocumentManager documentManager = lsContext.get(CodeActionKeys.DOCUMENT_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(CodeActionKeys.FILE_URI_KEY));
        LSDocumentIdentifier document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }

        if (document != null) {
            for (Diagnostic diagnostic : diagnostics) {
                String diagnosticMsg = diagnostic.getMessage().toLowerCase(Locale.ROOT);
                if (diagnosticMsg.contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED)) {
                    actions.addAll(getVariableAssignmentCommand(document, diagnostic, lsContext));
                }
            }
        }
        return actions;
    }

    private static List<CodeAction> getVariableAssignmentCommand(LSDocumentIdentifier document, Diagnostic diagnostic,
                                                                 LSContext context) {
        List<CodeAction> actions = new ArrayList<>();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        String diagnosedContent = getDiagnosedContent(diagnostic, context, document);
        List<Diagnostic> diagnostics = new ArrayList<>();
        Position position = diagnostic.getRange().getStart();

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
        return actions;
    }

    private static String getDiagnosedContent(Diagnostic diagnostic, LSContext context, LSDocumentIdentifier document) {
        WorkspaceDocumentManager docManager = context.get(CodeActionKeys.DOCUMENT_MANAGER_KEY);
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

    private static Position offsetInvocation(String diagnosedContent, Position position) {
        // Diagnosed message only contains the erroneous part of the line
        // Thus we offset into last
        int leftParenthesisIndex = diagnosedContent.indexOf("(");
        diagnosedContent = (leftParenthesisIndex == -1) ? diagnosedContent
                : diagnosedContent.substring(0, leftParenthesisIndex);
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
            // Recursively find parent, when it is an indexBasedAccessNode
            while (bLangNode.parent instanceof IndexBasedAccessNode) {
                bLangNode = bLangNode.parent;
            }
            variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bLangNode.type);
        }
        // Remove brackets of the unions
        variableType = variableType.replaceAll("^\\((.*)\\)$", "$1");
        String editText = createVariableDeclaration(variableName, variableType);
        Position position = new Position(bLangNode.pos.sLine - 1, bLangNode.pos.sCol - 1);
        edits.add(new TextEdit(new Range(position, position), editText));
        return edits;
    }

    private static List<TextEdit> getTypeGuardCodeActionEdits(LSContext context, String uri,
                                                              SymbolReferencesModel.Reference referenceAtCursor,
                                                              BUnionType unionType)
            throws WorkspaceDocumentException, IOException {
        WorkspaceDocumentManager docManager = context.get(CodeActionKeys.DOCUMENT_MANAGER_KEY);
        BLangNode bLangNode = referenceAtCursor.getbLangNode();
        Position startPos = new Position(bLangNode.pos.sLine - 1, bLangNode.pos.sCol - 1);
        Position endPosWithSemiColon = new Position(bLangNode.pos.eLine - 1, bLangNode.pos.eCol);
        Position endPos = new Position(bLangNode.pos.eLine - 1, bLangNode.pos.eCol - 1);
        Range newTextRange = new Range(startPos, endPosWithSemiColon);

        List<TextEdit> edits = new ArrayList<>();
        String spaces = StringUtils.repeat(' ', bLangNode.pos.sCol - 1);
        String padding = LINE_SEPARATOR + LINE_SEPARATOR + spaces;
        String content = CommandUtil.getContentOfRange(docManager, uri, new Range(startPos, endPos));
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

    private static List<TextEdit> getIgnoreCodeActionEdits(SymbolReferencesModel.Reference referenceAtCursor) {
        String editText = "_ = ";
        BLangNode bLangNode = referenceAtCursor.getbLangNode();
        Position position = new Position(bLangNode.pos.sLine - 1, bLangNode.pos.sCol - 1);
        List<TextEdit> edits = new ArrayList<>();
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
}

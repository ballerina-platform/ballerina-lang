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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.ReferencesKeys;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel.Reference;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.IndexBasedAccessNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.ballerinalang.langserver.codeaction.CodeActionUtil.getSymbolAtCursor;
import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

/**
 * Code Action provider for variable assignment.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateVariableCodeAction extends AbstractCodeActionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        WorkspaceDocumentManager documentManager = lsContext.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(DocumentServiceKeys.FILE_URI_KEY));
        LSDocumentIdentifier document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }

        if (document != null) {
            for (Diagnostic diagnostic : diagnosticsOfRange) {
                String diagnosticMsg = diagnostic.getMessage().toLowerCase(Locale.ROOT);
                if (diagnosticMsg.contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED)) {
                    actions.addAll(getVariableAssignmentCommand(document, diagnostic, lsContext));
                }
            }
        }
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> allDiagnostics) {
        throw new UnsupportedOperationException("Not supported");
    }

    private static List<CodeAction> getVariableAssignmentCommand(LSDocumentIdentifier document, Diagnostic diagnostic,
                                                                 LSContext context) {
        List<CodeAction> actions = new ArrayList<>();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        String diagnosedContent = getDiagnosedContent(diagnostic, context, document);
        List<Diagnostic> diagnostics = new ArrayList<>();
        Position position = diagnostic.getRange().getStart();

        try {
            context.put(ReferencesKeys.OFFSET_CURSOR_N_TRY_NEXT_BEST, true);
            context.put(ReferencesKeys.ENABLE_FIND_LITERALS, true);
            context.put(ReferencesKeys.DO_NOT_SKIP_NULL_SYMBOLS, true);
            Position afterAliasPos = offsetPositionToInvocation(diagnosedContent, position);
            Reference refAtCursor = getSymbolAtCursor(context, document, afterAliasPos);

            BSymbol symbolAtCursor = refAtCursor.getSymbol();

            boolean isInvocation = symbolAtCursor instanceof BInvokableSymbol;
            boolean isRemoteInvocation =
                    symbolAtCursor != null && (symbolAtCursor.flags & Flags.REMOTE) == Flags.REMOTE;

            boolean hasDefaultInitFunction = false;
            boolean hasCustomInitFunction = false;
            if (refAtCursor.getbLangNode() instanceof BLangInvocation) {
                hasDefaultInitFunction = symbolAtCursor instanceof BObjectTypeSymbol;
                hasCustomInitFunction = symbolAtCursor instanceof BInvokableSymbol &&
                        symbolAtCursor.name.value.endsWith("__init");
            }
            boolean isInitInvocation = hasDefaultInitFunction || hasCustomInitFunction;

            actions.addAll(getCreateVariableCodeActions(context, uri, diagnostics, position, refAtCursor,
                                                        hasDefaultInitFunction, hasCustomInitFunction));
            String commandTitle;

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
                        actions.add(createQuickFixCodeAction(commandTitle, tEdits, uri));
                    }
                }
                // Add ignore return value code action
                if (!hasError) {
                    commandTitle = CommandConstants.IGNORE_RETURN_TITLE;
                    actions.add(createQuickFixCodeAction(commandTitle, getIgnoreCodeActionEdits(position), uri));
                }
            }
        } catch (CompilationFailedException | WorkspaceDocumentException | IOException e) {
            // ignore
        }
        return actions;
    }

    private static List<CodeAction> getCreateVariableCodeActions(LSContext context, String uri,
                                                                 List<Diagnostic> diagnostics, Position position,
                                                                 Reference referenceAtCursor,
                                                                 boolean hasDefaultInitFunction,
                                                                 boolean hasCustomInitFunction) {
        List<CodeAction> actions = new ArrayList<>();


        BLangNode bLangNode = referenceAtCursor.getbLangNode();
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);


        List<TextEdit> importEdits = new ArrayList<>();
        Pair<List<String>, List<String>> typesAndNames = getPossibleTypesAndNames(context, referenceAtCursor,
                                                                                  hasDefaultInitFunction,
                                                                                  hasCustomInitFunction, bLangNode,
                                                                                  importEdits, compilerContext);

        List<String> types = typesAndNames.getLeft();
        List<String> names = typesAndNames.getRight();

        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            String name = names.get(i);
            String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
            if (types.size() > 1) {
                String typeLabel = (type.startsWith("[") && type.endsWith("]") && !type.endsWith("[]") &&
                        type.length() > 10) ? "Tuple" : type;
                commandTitle = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", typeLabel);
            }
            Position insertPos = new Position(position.getLine(), position.getCharacter());
            String edit = type + " " + name + " = ";
            List<TextEdit> edits = Collections.singletonList(new TextEdit(new Range(insertPos, insertPos), edit));
            actions.add(createQuickFixCodeAction(commandTitle, edits, uri));
        }
        return actions;
    }

    private static Pair<List<String>, List<String>> getPossibleTypesAndNames(LSContext context,
                                                                             Reference referenceAtCursor,
                                                                             boolean hasDefaultInitFunction,
                                                                             boolean hasCustomInitFunction,
                                                                             BLangNode bLangNode, List<TextEdit> edits,
                                                                             CompilerContext compilerContext) {
        Set<String> nameEntries = CommonUtil.getAllNameEntries(compilerContext);
        PackageID currentPkgId = bLangNode.pos.src.pkgID;
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);

        List<String> types = new ArrayList<>();
        List<String> names = new ArrayList<>();
        if (hasDefaultInitFunction) {
            BType bType = referenceAtCursor.getSymbol().type;
            String variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bType,
                                                                           context);
            String variableName = CommonUtil.generateVariableName(bType, nameEntries);
            types.add(variableType);
            names.add(variableName);
        } else if (hasCustomInitFunction) {
            BType bType = referenceAtCursor.getSymbol().owner.type;
            String variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bType,
                                                                           context);
            String variableName = CommonUtil.generateVariableName(bType, nameEntries);
            types.add(variableType);
            names.add(variableName);
        } else {
            // Recursively find parent, when it is an indexBasedAccessNode
            while (bLangNode.parent instanceof IndexBasedAccessNode) {
                bLangNode = bLangNode.parent;
            }
            String variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bLangNode,
                                                                           context);
            if (bLangNode instanceof BLangInvocation) {
                BSymbol symbol = ((BLangInvocation) bLangNode).symbol;
                if (symbol instanceof BInvokableSymbol) {
                    variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId,
                                                                            ((BInvokableSymbol) symbol).retType,
                                                                            context);
                }
                String variableName = CommonUtil.generateVariableName(bLangNode, nameEntries);
                types.add(variableType);
                names.add(variableName);
            } else if (bLangNode instanceof BLangFieldBasedAccess) {
                String variableName = CommonUtil.generateVariableName(((BLangFieldBasedAccess) bLangNode).expr.type,
                                                                      nameEntries);
                types.add(variableType);
                names.add(variableName);
            } else if (bLangNode instanceof BLangRecordLiteral) {
                String variableName = CommonUtil.generateName(1, nameEntries);

                // Record
                List<BLangPackage> bLangPackages = context.get(DocumentServiceKeys.BLANG_PACKAGES_CONTEXT_KEY);
                BRecordType matchingRecordType = null;
                Types typesChk = Types.getInstance(compilerContext);
                for (BLangPackage pkg : bLangPackages) {
                    for (TopLevelNode topLevelNode : pkg.topLevelNodes) {
                        if (topLevelNode instanceof BLangTypeDefinition &&
                                ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangRecordTypeNode &&
                                ((BLangTypeDefinition) topLevelNode).typeNode.type instanceof BRecordType) {
                            BRecordType type = (BRecordType) ((BLangTypeDefinition) topLevelNode).typeNode.type;
                            if (typesChk.checkStructEquivalency(bLangNode.type, type) &&
                                    !type.tsymbol.name.value.startsWith("$")) {
                                matchingRecordType = type;
                            }
                        }
                    }
                }

                // Matching Record
                if (matchingRecordType != null) {
                    String recType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId,
                                                                              matchingRecordType, context);
                    types.add(recType);
                    names.add(variableName);
                }

                // Anon Record
                String rType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bLangNode.type,
                                                                        context);
                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) bLangNode;
                types.add((recordLiteral.fields.size() > 0) ? rType : "record {}");
                names.add(variableName);

                // JSON
                types.add("json");
                names.add(variableName);

                // Map
                BType prevType = null;
                boolean isConstrainedMap = true;
                for (RecordLiteralNode.RecordField recordField : recordLiteral.fields) {
                    if (recordField instanceof BLangRecordLiteral.BLangRecordKeyValueField) {
                        BLangRecordLiteral.BLangRecordKeyValueField kvField =
                                (BLangRecordLiteral.BLangRecordKeyValueField) recordField;
                        BType type = kvField.valueExpr.type;
                        if (prevType != null &&
                                !prevType.tsymbol.name.getValue().equals(type.tsymbol.name.getValue())) {
                            isConstrainedMap = false;
                        }
                        prevType = type;
                    }
                }
                if (isConstrainedMap && prevType != null) {
                    String type = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, prevType,
                                                                           context);
                    types.add("map<" + type + ">");
                    names.add(variableName);
                } else {
                    types.add("map<any>");
                    names.add(variableName);
                }
            } else if (bLangNode instanceof BLangListConstructorExpr) {
                String variableName = CommonUtil.generateName(1, nameEntries);
                BLangListConstructorExpr listExpr = (BLangListConstructorExpr) bLangNode;
                if (listExpr.expectedType instanceof BTupleType) {
                    BTupleType tupleType = (BTupleType) listExpr.expectedType;
                    String arrayType = null;
                    String prevType = null;
                    String prevInnerType = null;
                    boolean isArrayCandidate = !tupleType.tupleTypes.isEmpty();
                    StringJoiner tupleJoiner = new StringJoiner(", ");
                    for (BType type : tupleType.tupleTypes) {
                        String newType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, type,
                                                                                  context);
                        if (prevType != null && !prevType.equals(newType)) {
                            isArrayCandidate = false;
                        }
                        if (type instanceof BTupleType && prevInnerType == null) {
                            // Checks inner element's type equality
                            BTupleType nType = (BTupleType) type;
                            boolean isSameInnerType = true;
                            for (BType innerType : nType.tupleTypes) {
                                String newInnerType = FunctionGenerator.generateTypeDefinition(importsAcceptor,
                                                                                               currentPkgId, innerType,
                                                                                               context);
                                if (prevInnerType != null && !prevInnerType.equals(newInnerType)) {
                                    isSameInnerType = false;
                                }
                                prevInnerType = newInnerType;
                            }
                            if (isSameInnerType) {
                                arrayType = prevInnerType + "[]";
                            }
                        }
                        tupleJoiner.add(newType);
                        prevType = newType;
                        if (arrayType == null) {
                            arrayType = newType;
                        }
                    }
                    // Array
                    if (isArrayCandidate) {
                        types.add(arrayType + "[]");
                        names.add(variableName);
                    }
                    // Tuple
                    types.add("[" + tupleJoiner.toString() + "]");
                    names.add(variableName);
                }
            } else if (bLangNode instanceof BLangQueryExpr) {
                BLangQueryExpr queryExpr = (BLangQueryExpr) bLangNode;
                ExpressionNode expression = queryExpr.selectClause.getExpression();
                if (expression instanceof BLangRecordLiteral) {
                    BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expression;
                    return getPossibleTypesAndNames(context, referenceAtCursor, hasDefaultInitFunction,
                                                    hasCustomInitFunction, recordLiteral, edits, compilerContext);
                } else {
                    String variableName = CommonUtil.generateName(1, nameEntries);
                    types.add("var");
                    names.add(variableName);
                }
            } else if (bLangNode instanceof BLangBinaryExpr) {
                BLangBinaryExpr binaryExpr = (BLangBinaryExpr) bLangNode;
                variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, binaryExpr.type,
                                                                        context);
                String variableName = CommonUtil.generateName(1, nameEntries);
                types.add(variableType);
                names.add(variableName);
            } else {
                String variableName = CommonUtil.generateVariableName(bLangNode.type, nameEntries);
                types.add(variableType);
                names.add(variableName);
            }
        }

        // Remove brackets of the unions
        types = types.stream().map(v -> v.replaceAll("^\\((.*)\\)$", "$1")).collect(Collectors.toList());
        edits.addAll(importsAcceptor.getNewImportTextEdits());
        return new ImmutablePair<>(types, names);
    }

    private static List<TextEdit> getTypeGuardCodeActionEdits(LSContext context, String uri,
                                                              Reference referenceAtCursor,
                                                              BUnionType unionType)
            throws WorkspaceDocumentException, IOException {
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
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
            Set<String> nameEntries = CommonUtil.getAllNameEntries(compilerContext);
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

    private static List<TextEdit> getIgnoreCodeActionEdits(Position position) {
        String editText = "_ = ";
        List<TextEdit> edits = new ArrayList<>();
        edits.add(new TextEdit(new Range(position, position), editText));
        return edits;
    }
}

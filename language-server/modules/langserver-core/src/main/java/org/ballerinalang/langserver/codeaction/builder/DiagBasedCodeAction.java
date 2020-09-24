/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.builder;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel.Reference;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.IndexBasedAccessNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Interface for diagnostics based code actions.
 *
 * @since 2.0.0
 */
public interface DiagBasedCodeAction {

    List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context)
            throws LSCodeActionProviderException;


    static Pair<List<String>, List<String>> getPossibleTypesAndNames(LSContext context,
                                                                     Reference referenceAtCursor,
                                                                     boolean hasDefaultInitFunction,
                                                                     boolean hasCustomInitFunction,
                                                                     boolean isAsync, BLangNode bLangNode,
                                                                     List<TextEdit> edits,
                                                                     CompilerContext compilerContext) {
        Set<String> nameEntries = CommonUtil.getAllNameEntries(compilerContext);
        PackageID currentPkgId = bLangNode.pos.src.pkgID;
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);

        List<String> types = new ArrayList<>();
        List<String> names = new ArrayList<>();
        if (isAsync) {
            BType bType = referenceAtCursor.getSymbol().type;
            String variableName = CommonUtil.generateVariableName(bType, nameEntries);
            types.add("var");
            names.add(variableName);
        } else if (hasDefaultInitFunction) {
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
                                                                            ((BLangInvocation) bLangNode).type,
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
                ExpressionNode expression = queryExpr.getSelectClause().getExpression();
                if (expression instanceof BLangRecordLiteral) {
                    BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expression;
                    return getPossibleTypesAndNames(context, referenceAtCursor, hasDefaultInitFunction,
                                                    hasCustomInitFunction, isAsync, recordLiteral, edits,
                                                    compilerContext);
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
}

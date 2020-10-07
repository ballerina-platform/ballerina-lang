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
package org.ballerinalang.langserver.codeaction.impl;

import io.ballerina.compiler.api.symbols.Symbol;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
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
                                                                     Symbol symbol,
                                                                     boolean hasDefaultInitFunction,
                                                                     boolean hasCustomInitFunction,
                                                                     boolean isAsync,
                                                                     List<TextEdit> edits,
                                                                     CompilerContext compilerContext) {
        Set<String> nameEntries = CommonUtil.getAllNameEntries(compilerContext);
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);

        List<String> types = new ArrayList<>();
        List<String> names = new ArrayList<>();
        if (isAsync) {
            String variableName = CommonUtil.generateVariableName(symbol, nameEntries);
            types.add("var");
            names.add(variableName);
        } else if (hasDefaultInitFunction) {
            String variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, symbol, context);
            String variableName = CommonUtil.generateVariableName(symbol, nameEntries);
            types.add(variableType);
            names.add(variableName);
        } else if (hasCustomInitFunction) {
            String variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, symbol, context);
            String variableName = CommonUtil.generateVariableName(symbol, nameEntries);
            types.add(variableType);
            names.add(variableName);
        } else {
            String variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, symbol, context);
            if (symbol instanceof BLangInvocation) {
                String variableName = CommonUtil.generateVariableName(symbol, nameEntries);
                types.add(variableType);
                names.add(variableName);
            } else if (symbol instanceof BLangFieldBasedAccess) {
                String variableName = CommonUtil.generateVariableName(symbol, nameEntries);
                types.add(variableType);
                names.add(variableName);
            } else if (symbol instanceof BLangRecordLiteral) {
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
                            if (typesChk.checkStructEquivalency(((BLangRecordLiteral) symbol).type, type) &&
                                    !type.tsymbol.name.value.startsWith("$")) {
                                matchingRecordType = type;
                            }
                        }
                    }
                }

                // Matching Record
                if (matchingRecordType != null) {
                    String recType = FunctionGenerator.generateTypeDefinition(importsAcceptor,
                                                                              symbol, context);
                    types.add(recType);
                    names.add(variableName);
                }

                // Anon Record
                String rType = FunctionGenerator.generateTypeDefinition(importsAcceptor, symbol,
                                                                        context);
                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) symbol;
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
                    String type = FunctionGenerator.generateTypeDefinition(importsAcceptor, symbol,
                                                                           context);
                    types.add("map<" + type + ">");
                    names.add(variableName);
                } else {
                    types.add("map<any>");
                    names.add(variableName);
                }
            } else if (symbol instanceof BLangListConstructorExpr) {
                String variableName = CommonUtil.generateName(1, nameEntries);
                BLangListConstructorExpr listExpr = (BLangListConstructorExpr) symbol;
                if (listExpr.expectedType instanceof BTupleType) {
                    BTupleType tupleType = (BTupleType) listExpr.expectedType;
                    String arrayType = null;
                    String prevType = null;
                    String prevInnerType = null;
                    boolean isArrayCandidate = !tupleType.tupleTypes.isEmpty();
                    StringJoiner tupleJoiner = new StringJoiner(", ");
                    for (BType type : tupleType.tupleTypes) {
                        String newType = FunctionGenerator.generateTypeDefinition(importsAcceptor, symbol,
                                                                                  context);
                        if (prevType != null && !prevType.equals(newType)) {
                            isArrayCandidate = false;
                        }
                        if (type instanceof BTupleType && prevInnerType == null) {
                            // Checks inner element's type equality
                            BTupleType nType = (BTupleType) type;
                            boolean isSameInnerType = true;
                            for (BType innerType : nType.tupleTypes) {
                                String newInnerType = FunctionGenerator.generateTypeDefinition(importsAcceptor, symbol,
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
            } else if (symbol instanceof BLangQueryExpr) {
                BLangQueryExpr queryExpr = (BLangQueryExpr) symbol;
                ExpressionNode expression = queryExpr.getSelectClause().getExpression();
                if (expression instanceof BLangRecordLiteral) {
                    BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expression;
                    return getPossibleTypesAndNames(context, symbol, hasDefaultInitFunction,
                                                    hasCustomInitFunction, isAsync, edits,
                                                    compilerContext);
                } else {
                    String variableName = CommonUtil.generateName(1, nameEntries);
                    types.add("var");
                    names.add(variableName);
                }
            } else if (symbol instanceof BLangBinaryExpr) {
                BLangBinaryExpr binaryExpr = (BLangBinaryExpr) symbol;
                variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, symbol,
                                                                        context);
                String variableName = CommonUtil.generateName(1, nameEntries);
                types.add(variableType);
                names.add(variableName);
            } else {
                String variableName = CommonUtil.generateVariableName(symbol, nameEntries);
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

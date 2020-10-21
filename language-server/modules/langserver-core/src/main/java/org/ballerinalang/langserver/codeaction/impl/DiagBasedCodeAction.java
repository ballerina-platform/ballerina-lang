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

import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerinalang.langserver.common.ImportsAcceptor;
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


    static List<String> getPossibleTypes(LSContext context,
                                         BallerinaTypeDescriptor typeDescriptor,
                                         List<TextEdit> edits,
                                         CompilerContext compilerContext) {
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);

        List<String> types = new ArrayList<>();

        String variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor, context);
        if (typeDescriptor instanceof BLangInvocation) {
            types.add(variableType);
        } else if (typeDescriptor instanceof BLangFieldBasedAccess) {
            types.add(variableType);
        } else if (typeDescriptor instanceof BLangRecordLiteral) {
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
                        if (typesChk.checkStructEquivalency(((BLangRecordLiteral) typeDescriptor).type, type) &&
                                !type.tsymbol.name.value.startsWith("$")) {
                            matchingRecordType = type;
                        }
                    }
                }
            }

            // Matching Record
            if (matchingRecordType != null) {
                String recType = FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor, context);
                types.add(recType);
            }

            // Anon Record
            String rType = FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor, context);
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) typeDescriptor;
            types.add((recordLiteral.fields.size() > 0) ? rType : "record {}");

            // JSON
            types.add("json");

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
                String type = FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor,
                                                                       context);
                types.add("map<" + type + ">");
            } else {
                types.add("map<any>");
            }
        } else if (typeDescriptor instanceof BLangListConstructorExpr) {
            BLangListConstructorExpr listExpr = (BLangListConstructorExpr) typeDescriptor;
            if (listExpr.expectedType instanceof BTupleType) {
                BTupleType tupleType = (BTupleType) listExpr.expectedType;
                String arrayType = null;
//                String prevType = null;
//                String prevInnerType = null;
                boolean isArrayCandidate = !tupleType.tupleTypes.isEmpty();
                StringJoiner tupleJoiner = new StringJoiner(", ");
                for (BType type : tupleType.tupleTypes) {
                    String newType = FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor,
                                                                              context);
                    // TODO: Fix this
//                    if (prevType != null && !prevType.equals(newType)) {
//                        isArrayCandidate = false;
//                    }
//                    if (type instanceof BTupleType && prevInnerType == null) {
                        // Checks inner element's type equality
//                        BTupleType nType = (BTupleType) type;
//                        boolean isSameInnerType = true;
//                        for (BType innerType : nType.tupleTypes) {
//                            String newInnerType = FunctionGenerator.generateTypeDefinition(importsAcceptor,
//                                                                                           typeDescriptor,
//                                                                                           context);
//                            if (prevInnerType != null && !prevInnerType.equals(newInnerType)) {
//                                isSameInnerType = false;
//                            }
//                            prevInnerType = newInnerType;
//                        }
//                        if (isSameInnerType) {
//                        arrayType = prevInnerType + "[]";
//                        }
//                    }
                    tupleJoiner.add(newType);
//                    prevType = newType;
                    if (arrayType == null) {
                        arrayType = newType;
                    }
                }
                // Array
                if (isArrayCandidate) {
                    types.add(arrayType + "[]");
                }
                // Tuple
                types.add("[" + tupleJoiner.toString() + "]");
            }
        } else if (typeDescriptor instanceof BLangQueryExpr) {
            BLangQueryExpr queryExpr = (BLangQueryExpr) typeDescriptor;
            ExpressionNode expression = queryExpr.getSelectClause().getExpression();
            if (expression instanceof BLangRecordLiteral) {
//                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expression;
                return getPossibleTypes(context, typeDescriptor,
                                        edits,
                                        compilerContext);
            } else {
                types.add("var");
            }
        } else if (typeDescriptor instanceof BLangBinaryExpr) {
//            BLangBinaryExpr binaryExpr = (BLangBinaryExpr) typeDescriptor;
            variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor,
                                                                    context);
            types.add(variableType);
        } else {
            types.add(variableType);
        }

        // Remove brackets of the unions
        types = types.stream().map(v -> v.replaceAll("^\\((.*)\\)$", "$1")).collect(Collectors.toList());
        edits.addAll(importsAcceptor.getNewImportTextEdits());
        return types;
    }
}

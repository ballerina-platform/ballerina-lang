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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.types.FunctionTypeSymbol;
import io.ballerina.compiler.api.types.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceBodyNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
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
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Code Action related Utils.
 *
 * @since 1.0.1
 */
public class CodeActionUtil {

    private CodeActionUtil() {
    }

    /**
     * Get the top level node type at the cursor line.
     *
     * @param syntaxTree {@link SyntaxTree}
     * @param context    {@link LSContext}
     * @return {@link CodeActionNodeType}   Top level node type
     */
    public static Optional<Pair<CodeActionNodeType, NonTerminalNode>> codeActionNodeType(SyntaxTree syntaxTree,
                                                                                         LSContext context) {
        int cursorLine = context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        List<ModuleMemberDeclarationNode> members = modulePartNode.members().stream().collect(Collectors.toList());
        for (ModuleMemberDeclarationNode member : members) {
            boolean isSameLine = member.lineRange().startLine().line() == cursorLine;
            boolean isWithinLines = cursorLine > member.lineRange().startLine().line() &&
                    cursorLine < member.lineRange().endLine().line();
            if (member.kind() == SyntaxKind.SERVICE_DECLARATION) {
                if (isSameLine) {
                    // Cursor on the service
                    return Optional.of(new ImmutablePair<>(CodeActionNodeType.SERVICE, member));
                } else if (isWithinLines) {
                    // Cursor within the service
                    ServiceDeclarationNode serviceDeclrNode = (ServiceDeclarationNode) member;
                    for (Node resourceNode : ((ServiceBodyNode) serviceDeclrNode.serviceBody()).resources()) {
                        boolean isSameResLine = resourceNode.lineRange().startLine().line() == cursorLine;
                        if (isSameResLine && resourceNode.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                            // Cursor on the resource function
                            return Optional.of(new ImmutablePair<>(CodeActionNodeType.RESOURCE, member));
                        }
                    }
                }
            } else if (isSameLine && member.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                return Optional.of(new ImmutablePair<>(CodeActionNodeType.FUNCTION, member));
            } else if (member.kind() == SyntaxKind.TYPE_DEFINITION) {
                TypeDefinitionNode definitionNode = (TypeDefinitionNode) member;
                Node typeDesc = definitionNode.typeDescriptor();
                if (isSameLine) {
                    if (typeDesc.kind() == SyntaxKind.RECORD_TYPE_DESC) {
                        return Optional.of(new ImmutablePair<>(CodeActionNodeType.RECORD, member));
                    } else if (typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                        return Optional.of(new ImmutablePair<>(CodeActionNodeType.OBJECT, member));
                    }
                } else if (isWithinLines && typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                    ObjectTypeDescriptorNode objectTypeDescNode = (ObjectTypeDescriptorNode) typeDesc;
                    for (Node memberNode : objectTypeDescNode.members()) {
                        boolean isSameResLine = memberNode.lineRange().startLine().line() == cursorLine;
                        if (isSameResLine && memberNode.kind() == SyntaxKind.METHOD_DECLARATION) {
                            // Cursor on the object function
                            return Optional.of(new ImmutablePair<>(CodeActionNodeType.OBJECT_FUNCTION, member));
                        }
                    }
                }
            } else if (member.kind() == SyntaxKind.CLASS_DEFINITION) {
                if (isSameLine) {
                    // Cursor on the class
                    return Optional.of(new ImmutablePair<>(CodeActionNodeType.CLASS, member));
                } else if (isWithinLines) {
                    // Cursor within the class
                    ClassDefinitionNode classDefNode = (ClassDefinitionNode) member;
                    for (Node memberNode : classDefNode.members()) {
                        boolean isSameResLine = memberNode.lineRange().startLine().line() == cursorLine;
                        if (isSameResLine && memberNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                            // Cursor on the class function
                            return Optional.of(new ImmutablePair<>(CodeActionNodeType.CLASS_FUNCTION, member));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Translates ballerina diagnostics into lsp4j diagnostics.
     *
     * @param ballerinaDiags a list of {@link Diagnostic}
     * @return a list of {@link Diagnostic}
     */
    public static List<org.eclipse.lsp4j.Diagnostic> toDiagnostics(List<Diagnostic> ballerinaDiags) {
        List<org.eclipse.lsp4j.Diagnostic> lsDiagnostics = new ArrayList<>();
        ballerinaDiags.forEach(diagnostic -> {
            org.eclipse.lsp4j.Diagnostic lsDiagnostic = new org.eclipse.lsp4j.Diagnostic();
            lsDiagnostic.setSeverity(DiagnosticSeverity.Error);
            lsDiagnostic.setMessage(diagnostic.message());
            Range range = new Range();

            Location location = diagnostic.location();
            LineRange lineRange = location.lineRange();
            int startLine = lineRange.startLine().line(); // LSP diagnostics range is 0 based
            int startChar = lineRange.startLine().offset();
            int endLine = lineRange.endLine().line();
            int endChar = lineRange.endLine().offset();

            if (endLine <= 0) {
                endLine = startLine;
            }

            if (endChar <= 0) {
                endChar = startChar + 1;
            }

            range.setStart(new Position(startLine, startChar));
            range.setEnd(new Position(endLine, endChar));
            lsDiagnostic.setRange(range);

            lsDiagnostics.add(lsDiagnostic);
        });

        return lsDiagnostics;
    }

    /**
     * Returns a list of possible types for this type descriptor.
     *
     * @param typeDescriptor  {@link TypeSymbol}
     * @param edits           a list of {@link TextEdit}
     * @param context         {@link LSContext}
     * @param compilerContext {@link CompilerContext}
     * @return a list of possible type list
     */
    public static List<String> getPossibleTypes(TypeSymbol typeDescriptor, List<TextEdit> edits,
                                                LSContext context,
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
                return getPossibleTypes(typeDescriptor, edits, context,
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

    /**
     * Returns position details for this cursor position.
     *
     * @param range
     * @param syntaxTree
     * @param context
     * @return
     */
    public static PositionDetails findCursorDetails(Range range, SyntaxTree syntaxTree, LSContext context) {
        // Find Cursor node
        NonTerminalNode cursorNode = CommonUtil.findNode(range.getStart(), syntaxTree);
        Symbol matchedSymbol = null;
        NonTerminalNode matchedNode = null;
        Optional<TypeSymbol> optTypeDesc = Optional.empty();

        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        String relPath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        SemanticModel semanticModel = new BallerinaSemanticModel(bLangPackage, compilerContext);

        //TODO: Remove this when #26382 is implemented
        Optional<TypeSymbol> type = semanticModel.getType(relPath, cursorNode.lineRange());
//        Optional<TypeSymbol> literalTypeDesc = checkForLiteralTypeDesc(cursorNode, semanticModel, relPath);
        if (type.isPresent()) {
            // If it is a Literal, use the temp type-descriptor
            matchedNode = cursorNode;
            optTypeDesc = type;
        } else {
            // Or else, use the scoped-symbol's type-descriptor
            Optional<Pair<NonTerminalNode, Symbol>> nodeAndSymbol = getMatchedNodeAndSymbol(cursorNode,
                                                                                            range,
                                                                                            semanticModel, relPath);
            if (nodeAndSymbol.isPresent()) {
                matchedNode = nodeAndSymbol.get().getLeft();
                matchedSymbol = nodeAndSymbol.get().getRight();
                optTypeDesc = getTypeDescriptor(matchedSymbol);
            }
        }
        return PositionDetailsImpl.from(matchedNode, matchedSymbol, optTypeDesc.orElse(null));
    }

    private static Optional<Pair<NonTerminalNode, Symbol>> getMatchedNodeAndSymbol(NonTerminalNode cursorNode,
                                                                                   Range range,
                                                                                   SemanticModel semanticModel,
                                                                                   String relPath) {
        // Find invocation position
        ScopedSymbolFinder scopedSymbolFinder = new ScopedSymbolFinder(range);
        scopedSymbolFinder.visit(cursorNode);
        if (scopedSymbolFinder.node().isEmpty() || scopedSymbolFinder.nodeIdentifierPos().isEmpty()) {
            return Optional.empty();
        }
        // Get Symbol of the position
        LinePosition position = scopedSymbolFinder.nodeIdentifierPos().get();
        LinePosition matchedNodePos = LinePosition.from(position.line(), position.offset() + 1);
        Optional<Symbol> optMatchedSymbol = semanticModel.symbol(relPath, matchedNodePos);
        if (optMatchedSymbol.isEmpty()) {
            return Optional.empty();
        }
        // Get TypeDesc of the symbol
        Symbol matchedSymbol = optMatchedSymbol.get();
        NonTerminalNode matchedNode = scopedSymbolFinder.node().get();
        return Optional.of(new ImmutablePair<>(matchedNode, matchedSymbol));
    }

    private static Optional<TypeSymbol> getTypeDescriptor(Symbol matchedSymbol) {
        switch (matchedSymbol.kind()) {
            case FUNCTION: {
                FunctionSymbol functionSymbol = (FunctionSymbol) matchedSymbol;
                FunctionTypeSymbol funTypeDesc = functionSymbol.typeDescriptor();
                return funTypeDesc.returnTypeDescriptor();
            }
            case METHOD: {
                MethodSymbol methodSymbol = (MethodSymbol) matchedSymbol;
                FunctionTypeSymbol funTypeDesc = methodSymbol.typeDescriptor();
                return funTypeDesc.returnTypeDescriptor();
            }
            case VARIABLE: {
                return Optional.of(((VariableSymbol) matchedSymbol).typeDescriptor());
            }
            case TYPE: {
                return Optional.of(((TypeSymbol) matchedSymbol));
            }
        }
        return Optional.empty();
    }
}

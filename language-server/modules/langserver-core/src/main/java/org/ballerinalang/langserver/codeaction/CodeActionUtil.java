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
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.types.FieldSymbol;
import io.ballerina.compiler.api.types.FunctionTypeSymbol;
import io.ballerina.compiler.api.types.RecordTypeSymbol;
import io.ballerina.compiler.api.types.TupleTypeSymbol;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
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
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;
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
            boolean isWithinLines = cursorLine > member.lineRange().startLine().line() &&
                    cursorLine < member.lineRange().endLine().line();
            if (member.kind() == SyntaxKind.SERVICE_DECLARATION) {
                ServiceDeclarationNode serviceDeclrNode = (ServiceDeclarationNode) member;
                boolean isSameLine = serviceDeclrNode.serviceKeyword().lineRange().startLine().line() == cursorLine;
                if (isSameLine) {
                    // Cursor on the service
                    return Optional.of(new ImmutablePair<>(CodeActionNodeType.SERVICE, member));
                } else if (isWithinLines) {
                    // Cursor within the service
                    for (Node resourceNode : ((ServiceBodyNode) serviceDeclrNode.serviceBody()).resources()) {
                        boolean isWithinResLine = cursorLine >= resourceNode.lineRange().startLine().line() &&
                                cursorLine <= resourceNode.lineRange().endLine().line();
                        if (isWithinResLine && resourceNode.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                            // Cursor on the resource function
                            return Optional.of(new ImmutablePair<>(CodeActionNodeType.RESOURCE, member));
                        }
                    }
                }
            } else if (member.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                FunctionDefinitionNode definitionNode = (FunctionDefinitionNode) member;
                boolean isSameLine = definitionNode.functionKeyword().lineRange().startLine().line() == cursorLine;
                if (isSameLine) {
                    return Optional.of(new ImmutablePair<>(CodeActionNodeType.FUNCTION, member));
                }
            } else if (member.kind() == SyntaxKind.TYPE_DEFINITION) {
                TypeDefinitionNode definitionNode = (TypeDefinitionNode) member;
                Node typeDesc = definitionNode.typeDescriptor();
                boolean isSameLine = definitionNode.typeKeyword().lineRange().startLine().line() == cursorLine;
                if (isSameLine) {
                    if (typeDesc.kind() == SyntaxKind.RECORD_TYPE_DESC) {
                        return Optional.of(new ImmutablePair<>(CodeActionNodeType.RECORD, member));
                    } else if (typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                        return Optional.of(new ImmutablePair<>(CodeActionNodeType.OBJECT, member));
                    }
                } else if (isWithinLines && typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                    ObjectTypeDescriptorNode objectTypeDescNode = (ObjectTypeDescriptorNode) typeDesc;
                    for (Node memberNode : objectTypeDescNode.members()) {
                        boolean isWithinResLine = cursorLine >= memberNode.lineRange().startLine().line() &&
                                cursorLine <= memberNode.lineRange().endLine().line();
                        if (isWithinResLine && memberNode.kind() == SyntaxKind.METHOD_DECLARATION) {
                            // Cursor on the object function
                            return Optional.of(new ImmutablePair<>(CodeActionNodeType.OBJECT_FUNCTION, member));
                        }
                    }
                }
            } else if (member.kind() == SyntaxKind.CLASS_DEFINITION) {
                ClassDefinitionNode classDefNode = (ClassDefinitionNode) member;
                boolean isSameLine = classDefNode.classKeyword().lineRange().startLine().line() == cursorLine;
                if (isSameLine) {
                    // Cursor on the class
                    return Optional.of(new ImmutablePair<>(CodeActionNodeType.CLASS, member));
                } else if (isWithinLines) {
                    // Cursor within the class
                    for (Node memberNode : classDefNode.members()) {
                        boolean isWithinResLine = cursorLine >= memberNode.lineRange().startLine().line() &&
                                cursorLine <= memberNode.lineRange().endLine().line();
                        if (isWithinResLine && memberNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
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
     * @param typeDescriptor {@link TypeSymbol}
     * @param edits          a list of {@link TextEdit}
     * @param context        {@link LSContext}
     * @return a list of possible type list
     */
    public static List<String> getPossibleTypes(TypeSymbol typeDescriptor, List<TextEdit> edits, LSContext context) {
        if (typeDescriptor.name().startsWith("$")) {
            typeDescriptor = CommonUtil.getRawType(typeDescriptor);
        }
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);

        List<String> types = new ArrayList<>();
        if (typeDescriptor.typeKind() == TypeDescKind.RECORD) {
            // Handle ambiguous mapping construct types {}

            // Matching Record type
            // TODO: Disabled due to #26789
//            if (matchingRecordType != null) {
//                String recType = FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor, context);
//                types.add(recType);
//            }

            // Anon Record
            String rType = FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor, context);
            RecordTypeSymbol recordLiteral = (RecordTypeSymbol) typeDescriptor;
            types.add((recordLiteral.fieldDescriptors().size() > 0) ? rType : "record {}");

            // JSON
            types.add("json");

            // Map
            TypeSymbol prevType = null;
            boolean isConstrainedMap = true;
            for (FieldSymbol recordField : recordLiteral.fieldDescriptors()) {
                TypeDescKind typeDescKind = recordField.typeDescriptor().typeKind();
                if (prevType != null && typeDescKind != prevType.typeKind()) {
                    isConstrainedMap = false;
                }
                prevType = recordField.typeDescriptor();
            }
            if (isConstrainedMap && prevType != null) {
                String type = FunctionGenerator.generateTypeDefinition(importsAcceptor, prevType, context);
                types.add("map<" + type + ">");
            } else {
                types.add("map<any>");
            }
        } else if (typeDescriptor.typeKind() == TypeDescKind.TUPLE) {
            // Handle ambiguous list construct types []
            TupleTypeSymbol tupleType = (TupleTypeSymbol) typeDescriptor;
            String arrayType = null;
            TypeSymbol prevType = null;
            TypeSymbol prevInnerType = null;
            boolean isArrayCandidate = tupleType.restTypeDescriptor().isEmpty();
            StringJoiner tupleJoiner = new StringJoiner(", ");
            for (TypeSymbol memberType : tupleType.memberTypeDescriptors()) {
                // Here we check previous member-type with current member-type for equality
                // 1. Check type-kind is differs Tuple vs int
                // 2. Check signature differs Tuple(int,string,int) vs Tuple(boolean, string)
                if (prevType != null &&
                        (prevType.typeKind() != memberType.typeKind() ||
                                !prevType.signature().equals(memberType.signature()))) {
                    isArrayCandidate = false;
                }
                if (memberType.typeKind() == TypeDescKind.TUPLE && prevInnerType == null) {
                    // Checks inner element's type equality
                    TupleTypeSymbol nType = (TupleTypeSymbol) memberType;
                    boolean isSameInnerType = true;
                    // Here we check previous inner-member-type with current inner-member-type for equality
                    // 1. Check type-kind is differs Tuple vs int
                    // 2. Check signature differs Tuple(int,string,int) vs Tuple(boolean, string)
                    for (TypeSymbol innerType : nType.memberTypeDescriptors()) {
                        if (prevInnerType != null &&
                                (prevInnerType.typeKind() != innerType.typeKind() ||
                                        !prevInnerType.signature().equals(innerType.signature()))) {
                            isSameInnerType = false;
                        }
                        prevInnerType = innerType;
                    }
                    if (isSameInnerType && prevInnerType != null) {
                        String type = FunctionGenerator.generateTypeDefinition(importsAcceptor, prevInnerType, context);
                        arrayType = type + "[]";
                    }
                }
                String type = FunctionGenerator.generateTypeDefinition(importsAcceptor, memberType, context);
                tupleJoiner.add(type);
                prevType = memberType;
                if (arrayType == null) {
                    arrayType = type;
                }
            }
            // Array
            if (isArrayCandidate) {
                types.add(arrayType + "[]");
            }
            // Tuple
            types.add("[" + tupleJoiner.toString() + "]");
        } else {
            types.add(FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor, context));
        }

        // Remove brackets of the unions
        types = types.stream().map(v -> v.replaceAll("^\\((.*)\\)$", "$1")).collect(Collectors.toList());
        edits.addAll(importsAcceptor.getNewImportTextEdits());
        return types;
    }

    /**
     * Returns position details for this cursor position.
     *
     * @param range      cursor {@link Range}
     * @param syntaxTree {@link SyntaxTree}
     * @param context    {@link LSContext}
     * @return {@link PositionDetails}
     */
    public static PositionDetails findCursorDetails(Range range, SyntaxTree syntaxTree, LSContext context) {
        // Find Cursor node
        NonTerminalNode cursorNode = CommonUtil.findNode(range.getStart(), syntaxTree);

        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        String relPath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        SemanticModel semanticModel = new BallerinaSemanticModel(bLangPackage, compilerContext);

        Optional<Pair<NonTerminalNode, Symbol>> nodeAndSymbol = getMatchedNodeAndSymbol(cursorNode,
                                                                                        range,
                                                                                        semanticModel, relPath);
        Symbol matchedSymbol;
        NonTerminalNode matchedNode;
        Optional<TypeSymbol> typeSymbol;
        if (nodeAndSymbol.isPresent()) {
            matchedNode = nodeAndSymbol.get().getLeft();
            matchedSymbol = nodeAndSymbol.get().getRight();
            typeSymbol = getTypeDescriptor(matchedSymbol);
        } else {
            matchedNode = cursorNode;
            matchedSymbol = null;
            typeSymbol = semanticModel.getType(relPath, largestExpressionNode(cursorNode, range).lineRange());
        }
        return PositionDetailsImpl.from(matchedNode, matchedSymbol, typeSymbol.orElse(null));
    }

    /**
     * Returns largest expression node for this range from bottom-up approach.
     *
     * @param node  starting {@link Node}
     * @param range {@link Range}
     * @return largest possible node
     */
    private static NonTerminalNode largestExpressionNode(NonTerminalNode node, Range range) {
        Predicate<NonTerminalNode> isWithinScope =
                tNode -> tNode != null && !(tNode instanceof ExpressionStatementNode) &&
                        CommonUtil.isWithinLineRange(range.getStart(), tNode.lineRange()) &&
                        CommonUtil.isWithinLineRange(range.getEnd(), tNode.lineRange());
        while (isWithinScope.test(node.parent())) {
            node = node.parent();
        }
        return node;
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
                return Optional.of(((TypeDefinitionSymbol) matchedSymbol).typeDescriptor());
            }
        }
        return Optional.empty();
    }
}

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
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
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
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
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
     * @param node node
     * @return {@link String}   Top level node type
     */
    public static CodeActionNodeType codeActionNodeType(Node node) {
        if (node == null) {
            return CodeActionNodeType.NONE;
        }

        ModulePartNode modulePartNode = syntaxTree.get().rootNode();
        int cursorPosOffset = syntaxTree.get().textDocument().textPositionFrom(LinePosition.from(position.getLine(),
                position.getCharacter()));

        List<NonTerminalNode> members = modulePartNode.members().stream().collect(Collectors.toList());
        modulePartNode.imports().forEach(members::add);
        for (NonTerminalNode member : members) {
            boolean isWithinStartSegment = isWithinStartCodeSegment(member, cursorPosOffset);
            boolean isWithinBody = isWithinBody(member, cursorPosOffset);
            if (!isWithinStartSegment && !isWithinBody) {
                continue;
            }

            if (member.kind() == SyntaxKind.SERVICE_DECLARATION) {
                if (isWithinStartSegment) {
                    // Cursor on the service
                    return Optional.of(new ImmutablePair<>(CodeActionNodeType.SERVICE, member));
                }
//                else {
                // Cursor within the service
//                    ServiceDeclarationNode serviceDeclrNode = (ServiceDeclarationNode) member;
//                    for (Node resourceNode : ((ServiceBodyNode) serviceDeclrNode.serviceBody()).resources()) {
//                        if (resourceNode.kind() == SyntaxKind.FUNCTION_DEFINITION
//                                && isWithinStartCodeSegment(resourceNode, cursorPosOffset)) {
//                            // Cursor on the resource function
//                            return Optional.of(new ImmutablePair<>(CodeActionNodeType.RESOURCE, member));
//                        }
//                    }
//                }
            } else if (isWithinStartSegment && member.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                return Optional.of(new ImmutablePair<>(CodeActionNodeType.FUNCTION, member));
            } else if (member.kind() == SyntaxKind.TYPE_DEFINITION) {
                TypeDefinitionNode definitionNode = (TypeDefinitionNode) member;
                Node typeDesc = definitionNode.typeDescriptor();
                if (isWithinStartSegment) {
                    if (typeDesc.kind() == SyntaxKind.RECORD_TYPE_DESC) {
                        return Optional.of(new ImmutablePair<>(CodeActionNodeType.RECORD, member));
                    } else if (typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                        return Optional.of(new ImmutablePair<>(CodeActionNodeType.OBJECT, member));
                    }
        switch (node.kind()) {
            case SERVICE_DECLARATION:
                return CodeActionNodeType.SERVICE;
            case FUNCTION_DEFINITION:
                if (node.parent().kind() == SyntaxKind.SERVICE_BODY) {
                    return CodeActionNodeType.RESOURCE;
                } else {
                    return CodeActionNodeType.FUNCTION;
                }
            case TYPE_DEFINITION:
                Node typeDesc = ((TypeDefinitionNode) node).typeDescriptor();
                if (typeDesc.kind() == SyntaxKind.RECORD_TYPE_DESC) {
                    return CodeActionNodeType.RECORD;
                } else if (typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                    return CodeActionNodeType.OBJECT;
                }
            case METHOD_DECLARATION:
                return CodeActionNodeType.OBJECT_FUNCTION;
            case CLASS_DEFINITION:
                return CodeActionNodeType.CLASS;
            case OBJECT_METHOD_DEFINITION:
                return CodeActionNodeType.CLASS_FUNCTION;
            case IMPORT_DECLARATION:
                return CodeActionNodeType.IMPORTS;
            default:
                return CodeActionNodeType.NONE;
        }
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
    public static List<String> getPossibleTypes(TypeSymbol typeDescriptor, List<TextEdit> edits,
                                                DocumentServiceContext context) {
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
        } else if (typeDescriptor.typeKind() == TypeDescKind.ARRAY) {
            // Handle ambiguous array element types eg. record[], json[], map[]
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) typeDescriptor;
            return getPossibleTypes(arrayTypeSymbol.memberTypeDescriptor(), edits, context)
                    .stream().map(m -> m + "[]")
                    .collect(Collectors.toList());
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
    public static PositionDetails computePositionDetails(Range range, SyntaxTree syntaxTree,
                                                         CodeActionContext context) {
        // Find Cursor node
        NonTerminalNode cursorNode = CommonUtil.findNode(range, syntaxTree);
        Path fileName = context.filePath().getFileName();
        if (fileName == null) {
            throw new RuntimeException("File path cannot be null");
        }
        String relPath = fileName.toString();
        SemanticModel semanticModel = context.workspace().semanticModel(context.filePath()).orElseThrow();

        Optional<Pair<NonTerminalNode, Symbol>> nodeAndSymbol = getMatchedNodeAndSymbol(cursorNode, range,
                semanticModel, relPath);
        Symbol matchedSymbol;
        NonTerminalNode matchedNode;
        Optional<TypeSymbol> matchedExprTypeSymbol;
        if (nodeAndSymbol.isPresent()) {
            matchedNode = nodeAndSymbol.get().getLeft();
            matchedSymbol = nodeAndSymbol.get().getRight();
        } else {
            matchedNode = cursorNode;
            matchedSymbol = null;
        }
        matchedExprTypeSymbol = semanticModel.type(relPath, largestExpressionNode(cursorNode, range).lineRange());
        return CodeActionPositionDetails.from(matchedNode, matchedSymbol, matchedExprTypeSymbol.orElse(null));
    }

    public static List<TextEdit> getTypeGuardCodeActionEdits(String varName, Range range, UnionTypeSymbol unionType,
                                                             CodeActionContext context) {
        Position startPos = range.getEnd();

        Range newTextRange = new Range(startPos, startPos);

        List<TextEdit> edits = new ArrayList<>();
        String spaces = StringUtils.repeat(' ', range.getStart().getCharacter());
        String padding = LINE_SEPARATOR + LINE_SEPARATOR + spaces;

        boolean hasError = unionType.memberTypeDescriptors().stream().anyMatch(s -> s.typeKind() == TypeDescKind.ERROR);

        List<TypeSymbol> members = new ArrayList<>(unionType.memberTypeDescriptors());
        long errorTypesCount = unionType.memberTypeDescriptors().stream()
                .filter(t -> t.typeKind() == TypeDescKind.ERROR)
                .count();
        if (members.size() == 1) {
            // Skip type guard
            return edits;
        }
        boolean transitiveBinaryUnion = unionType.memberTypeDescriptors().size() - errorTypesCount == 1;
        if (transitiveBinaryUnion) {
            members.removeIf(s -> s.typeKind() == TypeDescKind.ERROR);
        }
        // Check is binary union type with error type
        if ((unionType.memberTypeDescriptors().size() == 2 || transitiveBinaryUnion) && hasError) {
            members.forEach(bType -> {
                if (bType.typeKind() == TypeDescKind.NIL) {
                    // if (foo() is error) {...}
                    String newText = generateIfElseText(varName, spaces, padding, Collections.singletonList("error"));
                    edits.add(new TextEdit(newTextRange, newText));
                } else {
                    // if (foo() is int) {...} else {...}
                    String type = CodeActionUtil.getPossibleType(bType, edits, context).orElseThrow();
                    String newText = generateIfElseText(varName, spaces, padding, Collections.singletonList(type));
                    edits.add(new TextEdit(newTextRange, newText));
                }
            });
        } else {
            boolean addErrorTypeAtEnd;
            List<TypeSymbol> tMembers = new ArrayList<>((unionType).memberTypeDescriptors());
            if (errorTypesCount > 1) {
                // merge all error types into generic `error` type
                tMembers.removeIf(s -> s.typeKind() == TypeDescKind.ERROR);
                addErrorTypeAtEnd = true;
            } else {
                addErrorTypeAtEnd = false;
            }
            List<String> memberTypes = new ArrayList<>();
            for (TypeSymbol tMember : tMembers) {
                memberTypes.add(CodeActionUtil.getPossibleType(tMember, edits, context).orElseThrow());
            }
            if (addErrorTypeAtEnd) {
                memberTypes.add("error");
            }
            edits.add(new TextEdit(newTextRange, generateIfElseText(varName, spaces, padding, memberTypes)));
        }
        return edits;
    }

    public static List<TextEdit> getAddCheckTextEdits(Position pos, CodeActionContext context) {
        Optional<FunctionDefinitionNode> enclosedFunc = getEnclosedFunction(context.positionDetails().matchedNode());
        if (enclosedFunc.isEmpty()) {
            return Collections.emptyList();
        }

        List<TextEdit> edits = new ArrayList<>();
        SemanticModel semanticModel = context.workspace().semanticModel(context.filePath()).orElseThrow();
        Optional<Symbol> optEnclosedFuncSymbol = semanticModel.symbol(context.filePath().getFileName().toString(),
                                                                      enclosedFunc.get().functionName().lineRange()
                                                                              .startLine());
        String returnText = "";
        Range returnRange = null;
        if (optEnclosedFuncSymbol.isPresent() && optEnclosedFuncSymbol.get().kind() == SymbolKind.FUNCTION) {
            FunctionSymbol enclosedFuncSymbol = (FunctionSymbol) optEnclosedFuncSymbol.get();
            boolean hasFuncNodeReturn = enclosedFunc.get().functionSignature().returnTypeDesc().isPresent();
            boolean hasFuncSymbolReturn = enclosedFuncSymbol.typeDescriptor().returnTypeDescriptor().isPresent();
            if (hasFuncNodeReturn && hasFuncSymbolReturn) {
                // Parent function already has a return-type
                TypeSymbol enclosedRetTypeDesc = enclosedFuncSymbol.typeDescriptor().returnTypeDescriptor().get();
                ReturnTypeDescriptorNode enclosedRetTypeDescNode =
                        enclosedFunc.get().functionSignature().returnTypeDesc().get();
                if (enclosedRetTypeDesc.typeKind() == TypeDescKind.UNION) {
                    // Parent function already has a union return-type
                    UnionTypeSymbol parentUnionRetTypeDesc = (UnionTypeSymbol) enclosedRetTypeDesc;
                    boolean hasErrorMember = parentUnionRetTypeDesc.memberTypeDescriptors().stream()
                            .anyMatch(m -> m.typeKind() == TypeDescKind.ERROR);
                    if (!hasErrorMember) {
                        // Union has no error member-type
                        String typeName =
                                CodeActionUtil.getPossibleType(parentUnionRetTypeDesc, edits, context).orElseThrow();
                        returnText = "returns " + typeName + "|error";
                        returnRange = CommonUtil.toRange(enclosedRetTypeDescNode.lineRange());
                    }
                } else {
                    // Parent function already has a other return-type
                    String typeName = CodeActionUtil.getPossibleType(enclosedRetTypeDesc, edits, context).orElseThrow();
                    returnText = "returns " + typeName + "|error";
                    returnRange = CommonUtil.toRange(enclosedRetTypeDescNode.lineRange());
                }
            } else {
                // Parent function has no return
                returnText = " returns error?";
                Position position = CommonUtil.toPosition(
                        enclosedFunc.get().functionSignature().closeParenToken().lineRange().endLine());
                returnRange = new Range(position, position);
            }
        }

        // Add `check` expression text edit
        Position insertPos = new Position(pos.getLine(), pos.getCharacter());
        edits.add(new TextEdit(new Range(insertPos, insertPos), "check "));

        // Add parent function return change text edits
        if (!returnText.isEmpty()) {
            edits.add(new TextEdit(returnRange, returnText));
        }
        return edits;
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
                        CommonUtil.isWithinRange(CommonUtil.toPosition(tNode.lineRange().startLine()), range) &&
                        CommonUtil.isWithinRange(CommonUtil.toPosition(tNode.lineRange().endLine()), range);
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

    private static Optional<FunctionDefinitionNode> getEnclosedFunction(Node matchedNode) {
        FunctionDefinitionNode functionDefNode = null;
        Node parentNode = matchedNode;
        while (parentNode.kind() != SyntaxKind.FUNCTION_DEFINITION || parentNode.kind() != SyntaxKind.MODULE_PART) {
            parentNode = parentNode.parent();
            if (parentNode == null) {
                break;
            }
            if (parentNode.kind() == SyntaxKind.FUNCTION_DEFINITION &&
                    parentNode.parent() != null && parentNode.parent().kind() == SyntaxKind.MODULE_PART) {
                functionDefNode = (FunctionDefinitionNode) parentNode;
                break;
            }
        }
        return Optional.ofNullable(functionDefNode);
    }

    private static String generateIfElseText(String varName, String spaces, String padding,
                                             List<String> memberTypes) {
        if (memberTypes.size() == 1) {
            return LINE_SEPARATOR + String.format("%sif (%s is %s) {%s}", spaces, varName, memberTypes.get(0), padding);
        }
        StringBuilder newTextBuilder = new StringBuilder();
        for (int i = 0; i < memberTypes.size() - 1; i++) {
            String memberType = memberTypes.get(i);
            String prefix = (i == 0) ? spaces : " else ";
            newTextBuilder.append(String.format("%sif (%s is %s) {%s}", prefix, varName, memberType, padding));
        }
        newTextBuilder.append(String.format(" else {%s}%s", padding, LINE_SEPARATOR));
        return LINE_SEPARATOR + newTextBuilder.toString();
    }
}

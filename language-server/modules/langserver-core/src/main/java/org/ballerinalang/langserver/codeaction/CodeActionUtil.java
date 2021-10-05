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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

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
        switch (node.kind()) {
            case SERVICE_DECLARATION:
                return CodeActionNodeType.SERVICE;
            case FUNCTION_DEFINITION:
                return CodeActionNodeType.FUNCTION;
            case RESOURCE_ACCESSOR_DEFINITION:
                return CodeActionNodeType.RESOURCE;
            case TYPE_DEFINITION:
                Node typeDesc = ((TypeDefinitionNode) node).typeDescriptor();
                if (typeDesc.kind() == SyntaxKind.RECORD_TYPE_DESC) {
                    return CodeActionNodeType.RECORD;
                } else if (typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                    return CodeActionNodeType.OBJECT;
                }
                return CodeActionNodeType.NONE;
            case METHOD_DECLARATION:
                return CodeActionNodeType.OBJECT_FUNCTION;
            case CLASS_DEFINITION:
                return CodeActionNodeType.CLASS;
            case OBJECT_METHOD_DEFINITION:
                return CodeActionNodeType.CLASS_FUNCTION;
            case IMPORT_DECLARATION:
                return CodeActionNodeType.IMPORTS;
            case LOCAL_VAR_DECL:
                return CodeActionNodeType.LOCAL_VARIABLE;
            case MODULE_VAR_DECL:
                return CodeActionNodeType.MODULE_VARIABLE;
            case ASSIGNMENT_STATEMENT:
                return CodeActionNodeType.ASSIGNMENT;
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
     * Returns first possible type for this type descriptor.
     *
     * @param typeDescriptor {@link TypeSymbol}
     * @param importEdits    a list of import {@link TextEdit}
     * @param context        {@link CodeActionContext}
     * @return a list of possible type list
     */
    public static Optional<String> getPossibleType(TypeSymbol typeDescriptor, List<TextEdit> importEdits,
                                                   CodeActionContext context) {
        List<String> possibleTypes = getPossibleTypes(typeDescriptor, importEdits, context);
        return possibleTypes.isEmpty() ? Optional.empty() : Optional.of(possibleTypes.get(0));
    }

    /**
     * Returns a list of possible types for this type descriptor.
     *
     * @param typeDescriptor {@link TypeSymbol}
     * @param importEdits    a list of import {@link TextEdit}
     * @param context        {@link CodeActionContext}
     * @return a list of possible type list
     */
    public static List<String> getPossibleTypes(TypeSymbol typeDescriptor, List<TextEdit> importEdits,
                                                CodeActionContext context) {
        if (typeDescriptor.getName().isPresent() && typeDescriptor.getName().get().startsWith("$")) {
            typeDescriptor = CommonUtil.getRawType(typeDescriptor);
        }
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);

        List<String> types = new ArrayList<>();
        if (typeDescriptor.typeKind() == TypeDescKind.RECORD) {
            // Handle ambiguous mapping construct types {}

            // Matching Record type
            for (Symbol symbol : context.visibleSymbols(context.cursorPosition())) {
                if (symbol instanceof TypeDefinitionSymbol &&
                        ((TypeDefinitionSymbol) symbol).typeDescriptor().typeKind() == TypeDescKind.RECORD &&
                        typeDescriptor.assignableTo(((TypeDefinitionSymbol) symbol).typeDescriptor())) {
                    Optional<ModuleSymbol> module = symbol.getModule();
                    String fqPrefix = "";
                    if (module.isPresent() && !(ProjectConstants.ANON_ORG.equals(module.get().id().orgName()))) {
                        ModuleID id = module.get().id();
                        fqPrefix = id.orgName() + "/" + id.moduleName() + ":" + id.version() + ":";
                    }
                    String moduleQualifiedName = fqPrefix + symbol.getName().get();
                    types.add(FunctionGenerator.processModuleIDsInText(importsAcceptor, moduleQualifiedName, context));
                }
            }

            // Anon Record
            String rType = FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor, context);
            RecordTypeSymbol recordLiteral = (RecordTypeSymbol) typeDescriptor;
            types.add((recordLiteral.fieldDescriptors().size() > 0) ? rType : "record {}");

            // JSON
            types.add("json");

            // Map
            TypeSymbol prevType = null;
            boolean isConstrainedMap = true;
            for (RecordFieldSymbol recordField : recordLiteral.fieldDescriptors().values()) {
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
            return getPossibleTypes(arrayTypeSymbol.memberTypeDescriptor(), importEdits, context).stream()
                    .map(m -> {
                        switch (arrayTypeSymbol.memberTypeDescriptor().typeKind()) {
                            case UNION:
                            case FUNCTION:
                                return "(" + m + ")[]";

                            default:
                                return m + "[]";
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            types.add(FunctionGenerator.generateTypeDefinition(importsAcceptor, typeDescriptor, context));
        }

        importEdits.addAll(importsAcceptor.getNewImportTextEdits());
        return types;
    }

    /**
     * Returns position details for this cursor position.
     *
     * @param syntaxTree {@link SyntaxTree}
     * @param diagnostic {@link Diagnostic}
     * @param context    {@link CodeActionContext}
     * @return {@link DiagBasedPositionDetails}
     */
    public static DiagBasedPositionDetails computePositionDetails(SyntaxTree syntaxTree, Diagnostic diagnostic,
                                                                  CodeActionContext context) {
        // Find Cursor node
        Range range = CommonUtil.toRange(diagnostic.location().lineRange());
        NonTerminalNode cursorNode = CommonUtil.findNode(range, syntaxTree);
        Document srcFile = context.currentDocument().orElseThrow();
        SemanticModel semanticModel = context.currentSemanticModel().orElseThrow();

        Optional<Pair<NonTerminalNode, Symbol>> nodeAndSymbol = getMatchedNodeAndSymbol(cursorNode, range,
                semanticModel, srcFile);
        Symbol matchedSymbol;
        NonTerminalNode matchedNode;
        if (nodeAndSymbol.isPresent()) {
            matchedNode = nodeAndSymbol.get().getLeft();
            matchedSymbol = nodeAndSymbol.get().getRight();
        } else {
            matchedNode = cursorNode;
            matchedSymbol = null;
        }
        return DiagBasedPositionDetailsImpl.from(matchedNode, matchedSymbol, diagnostic);
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

    public static List<TextEdit> getAddCheckTextEdits(Position pos, NonTerminalNode matchedNode,
                                                      CodeActionContext context) {
        Optional<FunctionDefinitionNode> enclosedFunc = getEnclosedFunction(matchedNode);
        if (enclosedFunc.isEmpty()) {
            return Collections.emptyList();
        }

        List<TextEdit> edits = new ArrayList<>();
        SemanticModel semanticModel = context.currentSemanticModel().orElseThrow();
        Document document = context.currentDocument().orElseThrow();
        Optional<Symbol> optEnclosedFuncSymbol =
                semanticModel.symbol(document, enclosedFunc.get().functionName().lineRange().startLine());

        String returnText = "";
        Range returnRange = null;

        FunctionSymbol enclosedFuncSymbol = null;
        if (optEnclosedFuncSymbol.isPresent()) {
            Symbol funcSymbol = optEnclosedFuncSymbol.get();
            if (funcSymbol.kind() == SymbolKind.FUNCTION || funcSymbol.kind() == SymbolKind.METHOD ||
                    funcSymbol.kind() == SymbolKind.RESOURCE_METHOD) {
                enclosedFuncSymbol = (FunctionSymbol) optEnclosedFuncSymbol.get();
            }
        }

        if (enclosedFuncSymbol != null) {
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
    public static Node largestExpressionNode(Node node, Range range) {
        Predicate<Node> isWithinScope =
                tNode -> tNode != null && !(tNode instanceof ExpressionStatementNode) &&
                        CommonUtil.isWithinRange(CommonUtil.toPosition(tNode.lineRange().startLine()), range) &&
                        CommonUtil.isWithinRange(CommonUtil.toPosition(tNode.lineRange().endLine()), range);
        while (isWithinScope.test(node.parent())) {
            node = node.parent();
        }
        if (node.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
            return ((AssignmentStatementNode) node).expression();
        } else if (node.kind() == SyntaxKind.MODULE_VAR_DECL) {
            return ((ModuleVariableDeclarationNode) node).typedBindingPattern().typeDescriptor();
        } else if (node.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            return ((VariableDeclarationNode) node).typedBindingPattern().typeDescriptor();
        }
        return node;
    }

    /**
     * Get the top level node type at the cursor line.
     *
     * @param cursorPos  {@link Position}
     * @param syntaxTree {@link SyntaxTree}
     * @return {@link String}   Top level node
     */
    public static Optional<NonTerminalNode> getTopLevelNode(Position cursorPos, SyntaxTree syntaxTree) {
        NonTerminalNode member = CommonUtil.findNode(new Range(cursorPos, cursorPos), syntaxTree);
        LinePosition cursorPosition = LinePosition.from(cursorPos.getLine(), cursorPos.getCharacter());
        int cursorPosOffset = syntaxTree.textDocument().textPositionFrom(cursorPosition);
        while (member != null) {
            boolean isWithinStartSegment = isWithinStartCodeSegment(member, cursorPosOffset);
            boolean isWithinBody = isWithinBody(member, cursorPosOffset);
            if (!isWithinStartSegment && !isWithinBody) {
                member = member.parent();
                continue;
            }

            if (member.kind() == SyntaxKind.SERVICE_DECLARATION && isWithinStartSegment) {
                return Optional.of(member);
            } else if (member.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION && isWithinStartSegment) {
                return Optional.of(member);
            } else if (member.kind() == SyntaxKind.FUNCTION_DEFINITION && isWithinStartSegment) {
                return Optional.of(member);
            } else if (isWithinBody &&
                    (member.kind() == SyntaxKind.LOCAL_VAR_DECL || member.kind() == SyntaxKind.MODULE_VAR_DECL)) {
                return Optional.of(member);
            } else if (member.kind() == SyntaxKind.TYPE_DEFINITION) {
                TypeDefinitionNode definitionNode = (TypeDefinitionNode) member;
                Node typeDesc = definitionNode.typeDescriptor();
                if (isWithinStartSegment) {
                    if (typeDesc.kind() == SyntaxKind.RECORD_TYPE_DESC ||
                            typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                        return Optional.of(member);
                    }
                } else if (typeDesc.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
                    ObjectTypeDescriptorNode objectTypeDescNode = (ObjectTypeDescriptorNode) typeDesc;
                    for (Node memberNode : objectTypeDescNode.members()) {
                        if (memberNode.kind() == SyntaxKind.METHOD_DECLARATION
                                && isWithinStartCodeSegment(memberNode, cursorPosOffset)) {
                            // Cursor on the object function
                            return Optional.of((NonTerminalNode) memberNode);
                        }
                    }
                    return Optional.of(member);
                }
                return Optional.empty();
            } else if (member.kind() == SyntaxKind.RECORD_TYPE_DESC && isWithinBody) {
                // A record type descriptor can be inside a type definition node
                NonTerminalNode parent = member.parent();
                if (parent != null && parent.kind() == SyntaxKind.TYPE_DEFINITION &&
                        (isWithinStartCodeSegment(parent, cursorPosOffset) || isWithinBody(parent, cursorPosOffset))) {
                    return Optional.of(parent);
                }
                return Optional.of(member);
            } else if (member.kind() == SyntaxKind.OBJECT_TYPE_DESC && isWithinStartSegment) {
                return Optional.of(member);
            } else if (member.kind() == SyntaxKind.METHOD_DECLARATION && isWithinStartSegment) {
                return Optional.of(member);
            } else if (member.kind() == SyntaxKind.CLASS_DEFINITION) {
                if (isWithinStartSegment) {
                    // Cursor on the class
                    return Optional.of(member);
                } else {
                    // Cursor within the class
                    ClassDefinitionNode classDefNode = (ClassDefinitionNode) member;
                    for (Node memberNode : classDefNode.members()) {
                        if (memberNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION
                                && isWithinStartCodeSegment(memberNode, cursorPosOffset)) {
                            // Cursor on the class function
                            return Optional.of((NonTerminalNode) memberNode);
                        }
                    }
                    return Optional.of(member);
                }
            } else if (member.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION && isWithinStartSegment) {
                return Optional.of(member);
            } else if (isWithinBody && member.kind() == SyntaxKind.IMPORT_DECLARATION) {
                return Optional.of(member);
            } else if (isWithinBody && member.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
                return Optional.of(member);
            } else if (isWithinBody && member.kind() == SyntaxKind.MARKDOWN_DOCUMENTATION) {
                return Optional.of(member);
            } else {
                member = member.parent();
            }
        }
        return Optional.empty();
    }

    /**
     * Returns if given position's offset is within the code body of give node.
     *
     * @param node           Node in which the code body is considered
     * @param positionOffset Offset of the position
     * @return {@link Boolean} If within the body or not
     */
    private static boolean isWithinBody(Node node, int positionOffset) {
        if (node == null) {
            return false;
        }

        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case RESOURCE_ACCESSOR_DEFINITION:
            case OBJECT_METHOD_DEFINITION:
                TextRange functionBodyTextRange = ((FunctionDefinitionNode) node).functionBody().textRange();
                return isWithinRange(positionOffset, functionBodyTextRange.startOffset(),
                        functionBodyTextRange.endOffset());
            case SERVICE_DECLARATION:
                ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
                return isWithinRange(positionOffset, serviceDeclarationNode.openBraceToken().textRange().startOffset(),
                        serviceDeclarationNode.closeBraceToken().textRange().endOffset());
            case CLASS_DEFINITION:
                ClassDefinitionNode classDefinitionNode = (ClassDefinitionNode) node;
                return isWithinRange(positionOffset, classDefinitionNode.openBrace().textRange().startOffset(),
                        classDefinitionNode.closeBrace().textRange().endOffset());
            case TYPE_DEFINITION:
                TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) node;
                return isWithinRange(positionOffset,
                        typeDefinitionNode.typeDescriptor().textRange().startOffset(),
                        typeDefinitionNode.semicolonToken().textRange().startOffset());
            case RECORD_TYPE_DESC:
                RecordTypeDescriptorNode recordTypeDescNode = (RecordTypeDescriptorNode) node;
                return isWithinRange(positionOffset, recordTypeDescNode.bodyStartDelimiter().textRange().startOffset(),
                        recordTypeDescNode.bodyEndDelimiter().textRange().endOffset());
            case IMPORT_DECLARATION:
                ImportDeclarationNode importDeclarationNode = (ImportDeclarationNode) node;
                return isWithinRange(positionOffset,
                        importDeclarationNode.textRange().startOffset(),
                        importDeclarationNode.semicolon().textRange().startOffset());
            case LOCAL_VAR_DECL:
                VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) node;
                return isWithinRange(positionOffset,
                        variableDeclarationNode.textRange().startOffset(),
                        variableDeclarationNode.semicolonToken().textRange().startOffset());
            case MODULE_VAR_DECL:
                ModuleVariableDeclarationNode moduleVariableDeclarationNode = (ModuleVariableDeclarationNode) node;
                return isWithinRange(positionOffset,
                        moduleVariableDeclarationNode.textRange().startOffset(),
                        moduleVariableDeclarationNode.semicolonToken().textRange().startOffset());
            case ASSIGNMENT_STATEMENT:
                AssignmentStatementNode assignmentStatementNode = (AssignmentStatementNode) node;
                return isWithinRange(positionOffset,
                        assignmentStatementNode.textRange().startOffset(),
                        assignmentStatementNode.semicolonToken().textRange().startOffset());
            case MARKDOWN_DOCUMENTATION:
                MarkdownDocumentationNode markdownDocumentationNode = (MarkdownDocumentationNode) node;
                return isWithinRange(positionOffset,
                        markdownDocumentationNode.textRange().startOffset(),
                        markdownDocumentationNode.textRange().endOffset());
            default:
                return false;
        }
    }

    /**
     * Returns if given position's offset is within the starting code segment of give node.
     *
     * @param node           Node in which the code start segment is considered
     * @param positionOffset Offset of the position
     * @return {@link Boolean} If within the start segment or not
     */
    private static boolean isWithinStartCodeSegment(Node node, int positionOffset) {
        if (node == null) {
            return false;
        }

        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case RESOURCE_ACCESSOR_DEFINITION:
            case OBJECT_METHOD_DEFINITION:
                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
                Optional<MetadataNode> functionMetadata = functionDefinitionNode.metadata();
                int functionStartOffset = functionMetadata.map(metadataNode -> metadataNode.textRange().endOffset())
                        .orElseGet(() -> functionDefinitionNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, functionStartOffset,
                        functionDefinitionNode.functionBody().textRange().startOffset());
            case SERVICE_DECLARATION:
                ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
                Optional<MetadataNode> serviceMetadata = serviceDeclarationNode.metadata();
                int serviceStartOffset = serviceMetadata.map(metadataNode -> metadataNode.textRange().endOffset())
                        .orElseGet(() -> serviceDeclarationNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, serviceStartOffset,
                        serviceDeclarationNode.openBraceToken().textRange().startOffset());
            case METHOD_DECLARATION:
                MethodDeclarationNode methodDeclarationNode = (MethodDeclarationNode) node;
                Optional<MetadataNode> methodMetadata = methodDeclarationNode.metadata();
                int methodStartOffset = methodMetadata.map(metadataNode -> metadataNode.textRange().endOffset())
                        .orElseGet(() -> methodDeclarationNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, methodStartOffset,
                        methodDeclarationNode.semicolon().textRange().endOffset());
            case CLASS_DEFINITION:
                ClassDefinitionNode classDefinitionNode = (ClassDefinitionNode) node;
                Optional<MetadataNode> classMetadata = classDefinitionNode.metadata();
                int classStartOffset = classMetadata.map(metadataNode -> metadataNode.textRange().endOffset())
                        .orElseGet(() -> classDefinitionNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, classStartOffset,
                        classDefinitionNode.openBrace().textRange().endOffset());
            case OBJECT_TYPE_DESC:
                ObjectTypeDescriptorNode objectTypeDescNode = (ObjectTypeDescriptorNode) node;
                return isWithinRange(positionOffset, objectTypeDescNode.textRange().startOffset() - 1,
                        objectTypeDescNode.openBrace().textRange().endOffset());
            case TYPE_DEFINITION:
                TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) node;
                Optional<MetadataNode> typeMetadata = typeDefinitionNode.metadata();
                int typeStartOffset = typeMetadata.map(metadataNode -> metadataNode.textRange().endOffset())
                        .orElseGet(() -> typeDefinitionNode.textRange().startOffset() - 1);
                return isWithinRange(positionOffset, typeStartOffset,
                        typeDefinitionNode.typeDescriptor().textRange().startOffset());
            case IMPORT_DECLARATION:
            case LOCAL_VAR_DECL:
            case MODULE_VAR_DECL:
                // fall off
            default:
                return false;
        }
    }

    /**
     * Returns if given position's offset is within the given range.
     *
     * @param positionOffset Offset of the position
     * @param startOffSet    Offset of start
     * @param endOffset      Offset of end
     * @return {@link Boolean} If within the range or not
     */
    private static boolean isWithinRange(int positionOffset, int startOffSet, int endOffset) {
        return positionOffset > startOffSet && positionOffset < endOffset;
    }

    private static Optional<Pair<NonTerminalNode, Symbol>> getMatchedNodeAndSymbol(NonTerminalNode cursorNode,
                                                                                   Range range,
                                                                                   SemanticModel semanticModel,
                                                                                   Document srcFile) {
        // Find invocation position
        ScopedSymbolFinder scopedSymbolFinder = new ScopedSymbolFinder(range);
        scopedSymbolFinder.visit(cursorNode);
        if (scopedSymbolFinder.node().isEmpty() || scopedSymbolFinder.nodeIdentifierPos().isEmpty()) {
            return Optional.empty();
        }
        // Get Symbol of the position
        LinePosition position = scopedSymbolFinder.nodeIdentifierPos().get();
        LinePosition matchedNodePos = LinePosition.from(position.line(), position.offset() + 1);
        Optional<Symbol> optMatchedSymbol = semanticModel.symbol(srcFile, matchedNodePos);
        if (optMatchedSymbol.isEmpty()) {
            return Optional.empty();
        }
        Symbol matchedSymbol = optMatchedSymbol.get();
        NonTerminalNode matchedNode = scopedSymbolFinder.node().get();
        return Optional.of(new ImmutablePair<>(matchedNode, matchedSymbol));
    }

    /**
     * Given a node, tries to find the {@link FunctionDefinitionNode} which is enclosing the given node. Supports
     * {@link SyntaxKind#FUNCTION_DEFINITION}, {@link SyntaxKind#OBJECT_METHOD_DEFINITION} and
     * {@link SyntaxKind#RESOURCE_ACCESSOR_DEFINITION}s
     *
     * @param matchedNode Node which is enclosed within a function
     * @return Optional function defintion node
     */
    public static Optional<FunctionDefinitionNode> getEnclosedFunction(Node matchedNode) {
        if (matchedNode == null) {
            return Optional.empty();
        }

        FunctionDefinitionNode functionDefNode = null;
        Node parentNode = matchedNode;
        while (parentNode.parent() != null) {
            boolean isFunctionDef = false;
            // A function definition can be within a class, service or in the module part
            if (parentNode.kind() == SyntaxKind.FUNCTION_DEFINITION &&
                    parentNode.parent().kind() == SyntaxKind.MODULE_PART) {
                isFunctionDef = true;
            } else if (parentNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION &&
                    (parentNode.parent().kind() == SyntaxKind.CLASS_DEFINITION
                            || parentNode.parent().kind() == SyntaxKind.SERVICE_DECLARATION)) {
                isFunctionDef = true;
            } else if (parentNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION &&
                    parentNode.parent().kind() == SyntaxKind.SERVICE_DECLARATION) {
                isFunctionDef = true;
            }

            if (isFunctionDef) {
                functionDefNode = (FunctionDefinitionNode) parentNode;
                break;
            }

            parentNode = parentNode.parent();
        }

        return Optional.ofNullable(functionDefNode);
    }

    /**
     * Check if the provided union type contains at least one error member.
     *
     * @param unionTypeSymbol Union type
     * @return true if the union type contains an error member
     */
    public static boolean hasErrorMemberType(UnionTypeSymbol unionTypeSymbol) {
        return unionTypeSymbol.memberTypeDescriptors().stream()
                .anyMatch(member -> member.typeKind() == TypeDescKind.ERROR);
    }

    private static String generateIfElseText(String varName, String spaces, String padding,
                                             List<String> memberTypes) {
        if (memberTypes.size() == 1) {
            return LINE_SEPARATOR + String.format("%sif %s is %s {%s}", spaces, varName, memberTypes.get(0), padding);
        }
        StringBuilder newTextBuilder = new StringBuilder();
        for (int i = 0; i < memberTypes.size() - 1; i++) {
            String memberType = memberTypes.get(i);
            String prefix = (i == 0) ? spaces : " else ";
            newTextBuilder.append(String.format("%sif %s is %s {%s}", prefix, varName, memberType, padding));
        }
        newTextBuilder.append(String.format(" else {%s}%s", padding, LINE_SEPARATOR));
        return LINE_SEPARATOR + newTextBuilder.toString();
    }
}

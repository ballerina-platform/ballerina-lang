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
import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
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
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionData;
import org.ballerinalang.langserver.commons.codeaction.ResolvableCodeAction;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

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
     * Returns first possible type for this type descriptor.
     *
     * @param typeDescriptor  {@link TypeSymbol}
     * @param context         {@link CodeActionContext}
     * @param importsAcceptor imports acceptor
     * @return possible type for given type descriptor
     */
    public static Optional<String> getPossibleType(TypeSymbol typeDescriptor, CodeActionContext context,
                                                   ImportsAcceptor importsAcceptor) {
        List<String> possibleTypes = getPossibleTypes(typeDescriptor, context, importsAcceptor);
        return possibleTypes.isEmpty() ? Optional.empty() : Optional.of(possibleTypes.get(0));
    }

    /**
     * Returns first possible type for this type descriptor.
     *
     * @param typeDescriptor {@link TypeSymbol}
     * @param importEdits    a list of import {@link TextEdit}
     * @param context        {@link CodeActionContext}
     * @return a list of possible type list
     */
    public static List<String> getPossibleTypes(TypeSymbol typeDescriptor, List<TextEdit> importEdits,
                                                CodeActionContext context) {
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        List<String> possibleTypes = getPossibleTypes(typeDescriptor, context, importsAcceptor);
        importEdits.addAll(importsAcceptor.getNewImportTextEdits());
        return possibleTypes;
    }

    /**
     * Returns a list of possible types for this type descriptor.
     *
     * @param typeDescriptor  {@link TypeSymbol}
     * @param context         {@link CodeActionContext}
     * @param importsAcceptor imports acceptor
     * @return a list of possible types
     */
    public static List<String> getPossibleTypes(TypeSymbol typeDescriptor, CodeActionContext context,
                                                ImportsAcceptor importsAcceptor) {
        return new ArrayList<>(getPossibleTypeSymbols(typeDescriptor, context, importsAcceptor).values());
    }

    public static Map<TypeSymbol, String> getPossibleTypeSymbols(TypeSymbol typeDescriptor,
                                                                 List<TextEdit> importEdits,
                                                                 CodeActionContext context) {
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        Map<TypeSymbol, String> possibleTypes = getPossibleTypeSymbols(typeDescriptor, context, importsAcceptor);
        importEdits.addAll(importsAcceptor.getNewImportTextEdits());
        return possibleTypes;
    }

    public static Map<TypeSymbol, String> getPossibleTypeSymbols(TypeSymbol typeDescriptor, CodeActionContext context,
                                                                 ImportsAcceptor importsAcceptor) {
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return Collections.emptyMap();
        }
        if (typeDescriptor.getName().isPresent() && typeDescriptor.getName().get().startsWith("$")) {
            typeDescriptor = CommonUtil.getRawType(typeDescriptor);
        }

        Map<TypeSymbol, String> typesMap = new HashMap<>();
        Types types = semanticModel.get().types();
        TypeBuilder typeBuilder = types.builder();
        if (typeDescriptor.typeKind() == TypeDescKind.RECORD) {
            // Handle ambiguous mapping construct types {}

            Position cursorPosition = new Position(context.range().getStart().getLine(),
                    context.range().getStart().getCharacter());
            List<Symbol> visibleSymbols = context.visibleSymbols(cursorPosition);

            //Record type definitions - Find matching Record type definitions
            for (Symbol symbol : visibleSymbols) {
                if (symbol.kind() == SymbolKind.TYPE_DEFINITION &&
                        ((TypeDefinitionSymbol) symbol).typeDescriptor().typeKind() == TypeDescKind.RECORD &&
                        typeDescriptor.subtypeOf(((TypeDefinitionSymbol) symbol).typeDescriptor())) {
                    Optional<ModuleSymbol> module = symbol.getModule();
                    String fqPrefix = "";
                    if (module.isPresent() && !(ProjectConstants.ANON_ORG.equals(module.get().id().orgName()))) {
                        ModuleID id = module.get().id();
                        fqPrefix = id.orgName() + "/" + id.moduleName() + ":" + id.version() + ":";
                    }
                    String moduleQualifiedName = fqPrefix + symbol.getName().get();
                    typesMap.put(((TypeDefinitionSymbol) symbol).typeDescriptor(),
                            FunctionGenerator.processModuleIDsInText(importsAcceptor, moduleQualifiedName, context));
                }
            }

            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeDescriptor;
            // Anon Record
            String rType = FunctionGenerator.generateTypeSignature(importsAcceptor, typeDescriptor, context);
            typesMap.put(recordTypeSymbol, (recordTypeSymbol.fieldDescriptors().size() > 0) ? rType : "record {}");

            // JSON - Record fields and rest type descriptor should be json subtypes
            boolean jsonSubType = recordTypeSymbol.fieldDescriptors().values().stream()
                    .allMatch(recordFieldSymbol -> isJsonMemberType(recordFieldSymbol.typeDescriptor())) &&
                    recordTypeSymbol.restTypeDescriptor().map(CodeActionUtil::isJsonMemberType).orElse(true);
            if (jsonSubType) {
                typesMap.put(types.JSON, types.JSON.signature());
            }

            // Map
            Optional<TypeSymbol> firstFieldType =
                    recordTypeSymbol.fieldDescriptors().values().stream().findFirst()
                            .map(RecordFieldSymbol::typeDescriptor);
            boolean isConstrainedMap = firstFieldType
                    .map(fieldType -> recordTypeSymbol.fieldDescriptors().values().stream()
                    .map(RecordFieldSymbol::typeDescriptor).allMatch(type ->
                            type.subtypeOf(fieldType) || fieldType.subtypeOf(type))).orElse(false);
            if (isConstrainedMap) {
                String type = FunctionGenerator.generateTypeSignature(importsAcceptor, firstFieldType.get(), context);
                typesMap.put(typeBuilder.MAP_TYPE
                        .withTypeParam(firstFieldType.get()).build(), "map<" + type + ">");
                return typesMap;
            }
            typesMap.put(typeBuilder.MAP_TYPE.withTypeParam(types.ANY).build(), "map<any>");

        } else if (typeDescriptor.typeKind() == TypeDescKind.TUPLE) {
            // Handle ambiguous list construct types []
            TupleTypeSymbol tupleType = (TupleTypeSymbol) typeDescriptor;
            Optional<TypeSymbol> firstMemberType = tupleType.memberTypeDescriptors().stream().findFirst();
            boolean isArrayCandidate = tupleType.restTypeDescriptor().isEmpty()
                    && firstMemberType.map(first -> tupleType.memberTypeDescriptors().stream()
                    .allMatch(typeSymbol -> typeSymbol.subtypeOf(first)
                            || first.subtypeOf(typeSymbol))).orElse(false);

            // Add Array type if valid
            if (isArrayCandidate) {
                getPossibleTypeSymbols(firstMemberType.get(), context, importsAcceptor).entrySet()
                        .forEach(entry -> typesMap.put(typeBuilder.ARRAY_TYPE.withType(entry.getKey()).build(),
                                entry.getValue() + "[]"));
            }
            // Add tuple type
            typesMap.put(tupleType, FunctionGenerator.generateTypeSignature(importsAcceptor, tupleType, context));
        } else if (typeDescriptor.typeKind() == TypeDescKind.ARRAY) {
            // Handle ambiguous array element types eg. record[], json[], map[]
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) typeDescriptor;
            getPossibleTypeSymbols(arrayTypeSymbol.memberTypeDescriptor(), context, importsAcceptor).entrySet()
                    .forEach(entry -> {
                        ArrayTypeSymbol newArrType = typeBuilder.ARRAY_TYPE.withType(entry.getKey()).build();
                        String signature;
                        switch (newArrType.memberTypeDescriptor().typeKind()) {
                            case FUNCTION:
                            case UNION:
                                signature = "(" + newArrType.memberTypeDescriptor().signature() + ")[]";
                                break;
                            default:
                                signature = newArrType.signature();
                        }
                        typesMap.put(newArrType, signature);
                    });
        } else {
            typesMap.put(typeDescriptor, 
                    FunctionGenerator.generateTypeSignature(importsAcceptor, typeDescriptor, context));
        }
        return typesMap;
    }

    /**
     * Check if the provided type symbol is a valid subtype of JSON.
     *
     * @param typeSymbol Type symbol
     * @return True is type is a valid json member type
     */
    public static boolean isJsonMemberType(TypeSymbol typeSymbol) {
        // type json = () | boolean | int | float | decimal | string | json[] | map<json>;
        switch (typeSymbol.typeKind()) {
            case NIL:
            case BOOLEAN:
            case INT:
            case FLOAT:
            case DECIMAL:
            case STRING:
            case JSON:
                return true;
            case ARRAY:
                ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) typeSymbol;
                return isJsonMemberType(arrayTypeSymbol.memberTypeDescriptor());
            case MAP:
                MapTypeSymbol mapTypeSymbol = (MapTypeSymbol) typeSymbol;
                return isJsonMemberType(mapTypeSymbol.typeParam());
            default:
                return false;
        }
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
        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
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

        boolean hasError = CodeActionUtil.hasErrorMemberType(unionType);

        List<TypeSymbol> members = new ArrayList<>(unionType.memberTypeDescriptors());
        long errorTypesCount = unionType.memberTypeDescriptors().stream()
                .map(CommonUtil::getRawType)
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

    public static List<TextEdit> addGettersCodeActionEdits(String varName, Range range, String spaces,
                                                           String typeName) {
        List<TextEdit> edits = new ArrayList<>();
        edits.add(new TextEdit(range, generateGetterFunctionBodyText(varName, typeName, spaces)));
        return edits;
    }

    public static List<TextEdit> addSettersCodeActionEdits(String varName, Range range, String spaces,
                                                           String typeName) {
        List<TextEdit> edits = new ArrayList<>();
        edits.add(new TextEdit(range, generateSetterFunctionBodyText(varName, typeName, spaces)));
        return edits;
    }

    public static List<TextEdit> getAddCheckTextEdits(Position pos, NonTerminalNode matchedNode,
                                                      CodeActionContext context) {
        List<TextEdit> edits = new ArrayList<>();
        Optional<FunctionDefinitionNode> enclosedFunc = getEnclosedFunction(matchedNode);
        String returnText = "";
        Range returnRange = null;
        if (enclosedFunc.isPresent()) {
            SemanticModel semanticModel = context.currentSemanticModel().orElseThrow();
            Document document = context.currentDocument().orElseThrow();
            Optional<Symbol> optEnclosedFuncSymbol =
                    semanticModel.symbol(document, enclosedFunc.get().functionName().lineRange().startLine());
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
                                    CodeActionUtil.getPossibleType(parentUnionRetTypeDesc, edits, context)
                                            .orElseThrow();
                            returnText = "returns " + typeName + "|error";
                            returnRange = PositionUtil.toRange(enclosedRetTypeDescNode.lineRange());
                        }
                    } else if (enclosedRetTypeDesc.typeKind() == TypeDescKind.COMPILATION_ERROR) {
                        String returnType = enclosedRetTypeDescNode.type().toString().replaceAll("\\s+", "");
                        returnText = "returns " + returnType + "|error";
                        returnRange = PositionUtil.toRange(enclosedRetTypeDescNode.lineRange());
                    } else {
                        // Parent function already has another return-type
                        if (enclosedRetTypeDesc.typeKind() != TypeDescKind.ERROR) {
                            String typeName =
                                    CodeActionUtil.getPossibleType(enclosedRetTypeDesc, edits, context).orElseThrow();
                            returnText = "returns " + typeName + "|error";
                        }
                        returnRange = PositionUtil.toRange(enclosedRetTypeDescNode.lineRange());
                    }
                } else {
                    // Parent function has no return
                    returnText = " returns error?";
                    Position position = PositionUtil.toPosition(
                            enclosedFunc.get().functionSignature().closeParenToken().lineRange().endLine());
                    returnRange = new Range(position, position);
                }
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
     * Returns the largest expression node for this range from bottom-up approach.
     *
     * @param node  starting {@link Node}
     * @param range {@link Range}
     * @return largest possible node
     */
    public static Node largestExpressionNode(Node node, Range range) {
        Predicate<Node> isWithinScope =
                tNode -> tNode != null && !(tNode instanceof ExpressionStatementNode) &&
                        PositionUtil.isWithinRange(PositionUtil.toPosition(tNode.lineRange().startLine()), range) &&
                        PositionUtil.isWithinRange(PositionUtil.toPosition(tNode.lineRange().endLine()), range);
        while (isWithinScope.test(node.parent())) {
            node = node.parent();
        }
        if (node.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
            return ((AssignmentStatementNode) node).expression();
        } else if (node.kind() == SyntaxKind.MODULE_VAR_DECL) {
            return ((ModuleVariableDeclarationNode) node).typedBindingPattern().bindingPattern();
        } else if (node.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            return ((VariableDeclarationNode) node).typedBindingPattern().bindingPattern();
        }
        return node;
    }

    /**
     * Get the top level node type at the cursor line.
     *
     * @param range      {@link Range}
     * @param syntaxTree {@link SyntaxTree}
     * @return {@link String}   Top level node
     */
    public static Optional<NonTerminalNode> getTopLevelNode(Range range, SyntaxTree syntaxTree) {
        CodeActionNodeAnalyzer analyzer = CodeActionNodeAnalyzer.analyze(range, syntaxTree);
        return analyzer.getCodeActionNode();
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
                            || parentNode.parent().kind() == SyntaxKind.SERVICE_DECLARATION
                            || parentNode.parent().kind() == SyntaxKind.OBJECT_CONSTRUCTOR)) {
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
                .map(CommonUtil::getRawType)
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

    private static String generateGetterFunctionBodyText(String varName, String typeName, String spaces) {
        StringBuilder newTextBuilder = new StringBuilder();
        String extractedVarName = removeQuotedIdentifier(varName);
        String functionName = extractedVarName.substring(0, 1).toUpperCase(Locale.ROOT)
                + extractedVarName.substring(1);
        newTextBuilder.append(LINE_SEPARATOR).append(LINE_SEPARATOR).append(spaces)
                .append(String.format("public function get%s() returns %s { ", functionName, typeName))
                .append(LINE_SEPARATOR).append(spaces).append(spaces)
                .append(String.format("return self.%s;", varName))
                .append(LINE_SEPARATOR).append(spaces).append("}");
        return newTextBuilder.toString();
    }

    private static String generateSetterFunctionBodyText(String varName, String typeName, String spaces) {

        StringBuilder newTextBuilder = new StringBuilder();
        String extractedVarName = removeQuotedIdentifier(varName);
        String functionName = extractedVarName.substring(0, 1).toUpperCase(Locale.ROOT)
                + extractedVarName.substring(1);
        newTextBuilder.append(LINE_SEPARATOR).append(LINE_SEPARATOR).append(spaces)
                .append(String.format("public function set%s(%s %s) { ", functionName, typeName, extractedVarName))
                .append(LINE_SEPARATOR).append(spaces).append(spaces)
                .append(String.format("self.%s = %s;", varName, extractedVarName))
                .append(LINE_SEPARATOR).append(spaces).append("}");
        return newTextBuilder.toString();
    }

    public static boolean isWithinVarName(CodeActionContext context, ObjectFieldNode objectFieldNode) {
        return objectFieldNode.fieldName().lineRange().startLine().offset() <= context.cursorPosition().getCharacter()
                && context.cursorPosition().getCharacter() <=
                objectFieldNode.fieldName().lineRange().endLine().offset();
    }

    public static List<TextEdit> getGetterSetterCodeEdits(ObjectFieldNode objectFieldNode,
                                                          Optional<FunctionDefinitionNode> initNode,
                                                          String fieldName,
                                                          String typeName,
                                                          String name) {
        int startLine;
        int startOffset;
        int textOffset;
        if (initNode.isEmpty()) {
            LinePosition linePosition = ((ClassDefinitionNode) objectFieldNode.parent()).
                    members().get(((ClassDefinitionNode) objectFieldNode.parent()).members().size() - 1).
                    lineRange().endLine();
            startLine = linePosition.line();
            startOffset = linePosition.offset();
            textOffset = objectFieldNode.lineRange().startLine().offset();
        } else {
            LineRange lineRange = initNode.get().lineRange();
            startLine = lineRange.endLine().line();
            startOffset = lineRange.endLine().offset();
            textOffset = lineRange.startLine().offset();
        }

        Position startPos = new Position(startLine, startOffset);
        Range newTextRange = new Range(startPos, startPos);
        String spaces = StringUtils.repeat(' ', textOffset);
        if (name.equals("Getter")) {
            return CodeActionUtil.addGettersCodeActionEdits(fieldName, newTextRange, spaces, typeName);
        } else if (name.equals("Setter")) {
            return CodeActionUtil.addSettersCodeActionEdits(fieldName, newTextRange, spaces, typeName);
        } else {
            List<TextEdit> edits = CodeActionUtil.addGettersCodeActionEdits(fieldName, newTextRange, spaces, typeName);
            edits.addAll((CodeActionUtil.addSettersCodeActionEdits(fieldName, newTextRange, spaces, typeName)));
            return edits;
        }
    }

    public static Optional<FunctionDefinitionNode> getInitNode(ObjectFieldNode objectFieldNode) {
        FunctionDefinitionNode initNode = null;

        for (Node node : ((ClassDefinitionNode) objectFieldNode.parent()).members()) {
            if (node.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                if (((FunctionDefinitionNode) node).functionName().toString().equals("init")) {
                    initNode = (FunctionDefinitionNode) node;
                }

            }
        }

        return Optional.ofNullable(initNode);
    }

    public static boolean isFunctionDefined(String functionName, ObjectFieldNode objectFieldNode) {
        for (Node node : ((ClassDefinitionNode) objectFieldNode.parent()).members()) {
            if (node.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                if (((FunctionDefinitionNode) node).functionName().text()
                        .equals(removeQuotedIdentifier(functionName))) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Optional<ObjectFieldNode> getObjectFieldNode(CodeActionContext context,
                                                               RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedNode = posDetails.matchedCodeActionNode();
        if (!(matchedNode.kind() == SyntaxKind.OBJECT_FIELD) || matchedNode.hasDiagnostics()) {
            return Optional.empty();
        }

        ObjectFieldNode objectFieldNode = (ObjectFieldNode) matchedNode;
        if (!CodeActionUtil.isWithinVarName(context, objectFieldNode)) {
            return Optional.empty();
        }

        return Optional.of(objectFieldNode);
    }

    public static boolean isImmutableObjectField(ObjectFieldNode objectFieldNode) {
        return objectFieldNode.qualifierList().stream()
                .anyMatch(qualifiers -> qualifiers.toString().strip().equals("final") ||
                        qualifiers.toString().strip().equals("readonly"));
    }

    /**
     * Get the filter function used for filter diagnostic property values.
     *
     * @return Diagnostic property filter function.
     */
    public static <T> Function<List<DiagnosticProperty<?>>,
            Optional<T>> getDiagPropertyFilterFunction(int propertyIndex) {
        Function<List<DiagnosticProperty<?>>, Optional<T>> filterFunction = diagnosticProperties -> {
            if (diagnosticProperties.size() < (propertyIndex + 1)) {
                return Optional.empty();
            }
            DiagnosticProperty<?> diagnosticProperty = diagnosticProperties.get(propertyIndex);
            // Nullable static API used for safety
            return Optional.ofNullable((T) diagnosticProperty.value());
        };
        return filterFunction;
    }

    /**
     * Returns a Code Action for commands.
     *
     * @param commandTitle   title of the code action
     * @param command        command
     * @param codeActionKind kind of the code action
     * @return {@link CodeAction}
     */
    public static CodeAction createCodeAction(String commandTitle, Command command, String codeActionKind) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        CodeAction action = new CodeAction(commandTitle);
        action.setKind(codeActionKind);
        action.setCommand(command);
        action.setDiagnostics(toDiagnostics(diagnostics));
        return action;
    }

    /**
     * Returns a Code action.
     *
     * @param commandTitle title of the code action
     * @param edits        edits to be added in the code action
     * @param uri          uri
     * @return {@link CodeAction}
     */
    public static CodeAction createCodeAction(String commandTitle, List<TextEdit> edits, String uri) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        CodeAction action = new CodeAction(commandTitle);
        action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits)))));
        action.setDiagnostics(toDiagnostics(diagnostics));
        return action;
    }

    /**
     * Returns a Code action.
     *
     * @param commandTitle   title of the code action
     * @param edits          edits to be added in the code action
     * @param uri            uri
     * @param codeActionKind kind of the code action
     * @return {@link CodeAction}
     */
    public static CodeAction createCodeAction(String commandTitle, List<TextEdit> edits, String uri,
                                              String codeActionKind) {
        CodeAction action = createCodeAction(commandTitle, edits, uri);
        action.setKind(codeActionKind);
        return action;
    }

    public static String removeQuotedIdentifier(String identifier) {
        return identifier.startsWith("'") ? identifier.substring(1) : identifier;
    }

    /**
     * Returns a Resolvable code action.
     *
     * @param commandTitle   title of the code action
     * @param codeActionKind kind of the code action
     * @param data           code action data
     * @return {@link ResolvableCodeAction}
     */
    public static ResolvableCodeAction createResolvableCodeAction(String commandTitle, String codeActionKind,
                                                                  CodeActionData data) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        ResolvableCodeAction action = new ResolvableCodeAction(commandTitle);
        action.setDiagnostics(CodeActionUtil.toDiagnostics(diagnostics));
        action.setKind(codeActionKind);
        action.setData(data);
        return action;
    }

    /**
     * Returns if a new line should be appended to a new text edit at module level.
     *
     * @param enclosingNode Node at module level which is enclosing the cursor.
     * @return @link{Boolean} W
     */
    public static boolean addNewLineAtEnd(Node enclosingNode) {
        Iterator<Node> iterator = enclosingNode.parent().children().iterator();
        while (iterator.hasNext()) {
            Node next = iterator.next();
            if (next.textRange().length() > 0
                    && next.lineRange().startLine().line() == enclosingNode.lineRange().endLine().line() + 1) {
                return true;
            }
        }
        return false;
    }
}

/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.signature;

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.SignatureContext;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;

/**
 * Utility functions for the signature help.
 */
public class SignatureHelpUtil {

    private SignatureHelpUtil() {
    }

    /**
     * Get Signature Help for a the invocation node in the given context.
     *
     * @param context Signature Help context.
     * @return {@link SignatureHelp} SignatureHelp for the invocation node.
     */
    public static SignatureHelp getSignatureHelp(SignatureContext context) {
        fillTokenInfoAtCursor(context);
        Optional<NonTerminalNode> sNode = context.getNodeAtCursor();
        // Check for the cancellation after time consuming operation 
        context.checkCancelled();

        if (sNode.isEmpty()) {
            return null; //empty signatureHelp;
        }

        SyntaxKind sKind = sNode.get().kind();
        NonTerminalNode evalNode = sNode.get();

        // Find invocation node
        while (evalNode != null &&
                sKind != SyntaxKind.FUNCTION_CALL &&
                sKind != SyntaxKind.METHOD_CALL &&
                sKind != SyntaxKind.REMOTE_METHOD_CALL_ACTION &&
                sKind != SyntaxKind.IMPLICIT_NEW_EXPRESSION &&
                sKind != SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
            evalNode = evalNode.parent();
            sKind = (evalNode != null) ? evalNode.kind() : null;
        }

        if (evalNode == null) {
            // Could not find a valid invocation node.
            return null;
        }

        ChildNodeList childrenInParen = evalNode.children();
        switch (sKind) {
            case IMPLICIT_NEW_EXPRESSION:
                Optional<ParenthesizedArgList> implicitArgList =
                        ((ImplicitNewExpressionNode) evalNode).parenthesizedArgList();
                if (implicitArgList.isPresent()) {
                    childrenInParen = implicitArgList.get().children();
                }
                break;
            case EXPLICIT_NEW_EXPRESSION:
                childrenInParen = ((ExplicitNewExpressionNode) evalNode).parenthesizedArgList().children();
                break;
            default:
                break;
        }

        // Find parameter index
        int activeParamIndex = 0;
        int cursorPosition = context.getCursorPositionInTree();
        for (Node child : childrenInParen) {
            int childPosition = child.textRange().endOffset();
            if (cursorPosition < childPosition) {
                break;
            }
            if (child.kind() == SyntaxKind.COMMA_TOKEN) {
                activeParamIndex++;
            }
        }

        // Get signature information
        List<SignatureInformation> signatureInformation = SignatureHelpUtil.getSignatureInformation(context);
        List<SignatureInformation> signatures = new ArrayList<>(signatureInformation);
        SignatureHelp signatureHelp = new SignatureHelp();
        signatureHelp.setActiveParameter(activeParamIndex);
        signatureHelp.setActiveSignature(0);
        signatureHelp.setSignatures(signatures);
        return signatureHelp;
    }

    /**
     * Get the signature information for a given context.
     *
     * @param context Lang Server Signature Help Context
     * @return {@link SignatureInformation}     Signature information for the invocation node.
     */
    private static List<SignatureInformation> getSignatureInformation(SignatureContext context) {

        Optional<? extends Symbol> invokableSymbol = getFunctionSymbol(context);
        if (invokableSymbol.isEmpty()) {
            return Collections.emptyList();
        }

        return new SignatureInfoModelBuilder(invokableSymbol.get(), context).build();
    }

    /**
     * Find the token at cursor.
     */
    private static void fillTokenInfoAtCursor(SignatureContext context) {
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            return;
        }
        TextDocument textDocument = document.get().textDocument();
        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) document.get().syntaxTree().rootNode()).findNode(range);

        while (true) {
            if (nonTerminalNode != null && (!withinTextRange(txtPos, nonTerminalNode)
                    || (nonTerminalNode.kind() != SyntaxKind.FUNCTION_CALL
                    && nonTerminalNode.kind() != SyntaxKind.METHOD_CALL)
                    && nonTerminalNode.kind() != SyntaxKind.REMOTE_METHOD_CALL_ACTION
                    && nonTerminalNode.kind() != SyntaxKind.IMPLICIT_NEW_EXPRESSION)
                    && nonTerminalNode.kind() != SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
                nonTerminalNode = nonTerminalNode.parent();
                continue;
            }
            break;
        }

        context.setNodeAtCursor(nonTerminalNode);
    }

    private static boolean withinTextRange(int position, @Nonnull NonTerminalNode node) {
        TextRange rangeWithMinutiae = node.textRangeWithMinutiae();
        TextRange textRange = node.textRange();
        TextRange leadingMinutiaeRange = TextRange.from(rangeWithMinutiae.startOffset(),
                textRange.startOffset() - rangeWithMinutiae.startOffset());
        return leadingMinutiaeRange.endOffset() <= position;
    }

    public static Optional<? extends Symbol> getFunctionSymbol(SignatureContext context) {
        if (context.getNodeAtCursor().isEmpty()) {
            return Optional.empty();
        }
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor().get();
        if (nodeAtCursor.kind() == SyntaxKind.FUNCTION_CALL) {
            return getFunctionSymbol((FunctionCallExpressionNode) nodeAtCursor, context);
        }
        return getFunctionSymbol(nodeAtCursor, context);

    }

    private static Optional<? extends Symbol> getFunctionSymbol(FunctionCallExpressionNode node,
                                                                SignatureContext context) {
        NameReferenceNode nameReferenceNode = node.functionName();
        String funcName;
        Predicate<Symbol> symbolPredicate =
                symbol -> symbol.kind() == FUNCTION || symbol.kind() == VARIABLE || symbol.kind() == PARAMETER;
        List<Symbol> filteredContent;
        if (nameReferenceNode.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nameReferenceNode;
            funcName = (qNameRef).identifier().text();
            filteredContent = QNameRefCompletionUtil.getModuleContent(context, qNameRef,
                    symbolPredicate
                            .and(symbol -> symbol.getName().orElse("")
                                    .equals(funcName)));
        } else {
            funcName = ((SimpleNameReferenceNode) nameReferenceNode).name().text();
            List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
            filteredContent = visibleSymbols.stream()
                    .filter(symbolPredicate.and(symbol -> symbol.getName().isPresent()
                            && symbol.getName().get().equals(funcName)))
                    .collect(Collectors.toList());
        }

        return filteredContent.stream().findAny();
    }

    private static Optional<? extends Symbol> getFunctionSymbol(Node nodeAtCursor, SignatureContext context) {
        Optional<? extends TypeSymbol> typeDesc;
        String methodName;
        if (nodeAtCursor.kind() == SyntaxKind.METHOD_CALL) {
            MethodCallExpressionNode methodCall = (MethodCallExpressionNode) nodeAtCursor;
            typeDesc = getTypeDesc(context, methodCall.expression());
            methodName = ((SimpleNameReferenceNode) methodCall.methodName()).name().text();
        } else if (nodeAtCursor.kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION) {
            RemoteMethodCallActionNode remoteMethodCall = (RemoteMethodCallActionNode) nodeAtCursor;
            typeDesc = getTypeDesc(context, remoteMethodCall.expression());
            methodName = remoteMethodCall.methodName().name().text();
        } else if (nodeAtCursor.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION
                || nodeAtCursor.kind() == SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
            typeDesc = context.currentSemanticModel()
                    .flatMap(semanticModel -> semanticModel.typeOf(nodeAtCursor))
                    .flatMap(typeSymbol -> Optional.of(CommonUtil.getRawType(typeSymbol))).stream().findFirst();
            methodName = Names.USER_DEFINED_INIT_SUFFIX.getValue();
        } else {
            return Optional.empty();
        }
        if (typeDesc.isEmpty()) {
            return Optional.empty();
        }

        return getFunctionSymbolsForTypeDesc(typeDesc.get()).stream()
                .filter(functionSymbol -> functionSymbol.getName().orElse("").equals(methodName))
                .findAny();
    }

    private static Optional<? extends TypeSymbol> getTypeDesc(SignatureContext ctx, ExpressionNode expr) {
        switch (expr.kind()) {
            case SIMPLE_NAME_REFERENCE:
                /*
                Captures the following
                (1) fieldName
                 */
                return getTypeDescForNameRef(ctx, (SimpleNameReferenceNode) expr);
            case FUNCTION_CALL:
                /*
                Captures the following
                (1) functionName()
                 */
                return getTypeDescForFunctionCall(ctx, (FunctionCallExpressionNode) expr);
            case METHOD_CALL: {
                /*
                Address the following
                (1) test.testMethod()
                 */
                return getTypeDescForMethodCall(ctx, (MethodCallExpressionNode) expr);
            }
            case FIELD_ACCESS: {
                /*
                Address the following
                (1) test1.test2
                 */
                return getTypeDescForFieldAccess(ctx, (FieldAccessExpressionNode) expr);
            }

            default:
                return Optional.empty();
        }
    }

    private static Optional<? extends TypeSymbol> getTypeDescForFieldAccess(
            SignatureContext context, FieldAccessExpressionNode node) {
        String fieldName = ((SimpleNameReferenceNode) node.fieldName()).name().text();
        ExpressionNode expressionNode = node.expression();
        Optional<? extends TypeSymbol> typeDescriptor = getTypeDesc(context, expressionNode);

        if (typeDescriptor.isEmpty()) {
            return Optional.empty();
        }

        TypeSymbol rawType = CommonUtil.getRawType(typeDescriptor.get());
        switch (rawType.typeKind()) {
            case OBJECT:
                ObjectFieldSymbol objField = ((ObjectTypeSymbol) rawType).fieldDescriptors().get(fieldName);
                return objField != null ? Optional.of(objField.typeDescriptor()) : Optional.empty();
            case RECORD:
                RecordFieldSymbol recField = ((RecordTypeSymbol) rawType).fieldDescriptors().get(fieldName);
                return recField != null ? Optional.of(recField.typeDescriptor()) : Optional.empty();
            default:
                return Optional.empty();
        }
    }

    private static Optional<? extends TypeSymbol> getTypeDescForNameRef(SignatureContext context,
                                                                        NameReferenceNode referenceNode) {
        if (referenceNode.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String name = ((SimpleNameReferenceNode) referenceNode).name().text();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        Optional<Symbol> symbolRef = visibleSymbols.stream()
                .filter(symbol -> Objects.equals(symbol.getName().orElse(null), name))
                .findFirst();
        if (symbolRef.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(symbolRef.get());
    }

    private static Optional<? extends TypeSymbol> getTypeDescForFunctionCall(
            SignatureContext context, FunctionCallExpressionNode expr) {
        String fName = ((SimpleNameReferenceNode) expr.functionName()).name().text();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        Optional<FunctionSymbol> symbolRef = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION && symbol.getName().get().equals(fName))
                .map(symbol -> (FunctionSymbol) symbol)
                .findFirst();
        if (symbolRef.isEmpty()) {
            return Optional.empty();
        }

        return symbolRef.get().typeDescriptor().returnTypeDescriptor();
    }

    private static Optional<? extends TypeSymbol> getTypeDescForMethodCall(
            SignatureContext context, MethodCallExpressionNode node) {
        String methodName = ((SimpleNameReferenceNode) node.methodName()).name().text();

        Optional<? extends TypeSymbol> fieldTypeDesc = getTypeDesc(context, node.expression());

        if (fieldTypeDesc.isEmpty()) {
            return Optional.empty();
        }

        List<FunctionSymbol> visibleMethods = fieldTypeDesc.get().langLibMethods();
        if (CommonUtil.getRawType(fieldTypeDesc.get()).typeKind() == TypeDescKind.OBJECT) {
            visibleMethods.addAll(((ObjectTypeSymbol) CommonUtil.getRawType(fieldTypeDesc.get())).methods().values());
        }
        Optional<FunctionSymbol> filteredMethod = visibleMethods.stream()
                .filter(methodSymbol -> Objects.equals(methodSymbol.getName().orElse(null), methodName))
                .findFirst();

        if (filteredMethod.isEmpty()) {
            return Optional.empty();
        }

        return filteredMethod.get().typeDescriptor().returnTypeDescriptor();
    }

    private static List<FunctionSymbol> getFunctionSymbolsForTypeDesc(TypeSymbol typeDescriptor) {
        List<FunctionSymbol> functionSymbols = new ArrayList<>();
        TypeSymbol rawType = CommonUtil.getRawType(typeDescriptor);

        if (rawType.typeKind() == TypeDescKind.OBJECT) {
            ObjectTypeSymbol objTypeDesc = (ObjectTypeSymbol) rawType;
            functionSymbols.addAll(objTypeDesc.methods().values());
        }
        if (rawType.kind() == CLASS && ((ClassSymbol) rawType).initMethod().isPresent()) {
            functionSymbols.add(((ClassSymbol) rawType).initMethod().get());
        }
        if (rawType.typeKind() == TypeDescKind.UNION) {
            ((UnionTypeSymbol) rawType).memberTypeDescriptors().stream()
                    .filter(typeSymbol -> CommonUtil.getRawType(typeSymbol).typeKind() == TypeDescKind.OBJECT)
                    .findFirst().ifPresent(objectMember ->
                    functionSymbols.addAll(getFunctionSymbolsForTypeDesc(objectMember)));
        }
        functionSymbols.addAll(typeDescriptor.langLibMethods());

        return functionSymbols;
    }
}

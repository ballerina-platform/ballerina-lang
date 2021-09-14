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
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
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
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.SignatureContext;
import org.ballerinalang.langserver.completions.util.ContextTypeResolver;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SignatureInformationCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
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

        // Search function invocation symbol
        List<SignatureInformation> signatures = new ArrayList<>();
        Optional<SignatureInformation> signatureInfo = SignatureHelpUtil.getSignatureInformation(context);
        signatureInfo.ifPresent(signatures::add);
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
    private static Optional<SignatureInformation> getSignatureInformation(SignatureContext context) {
        Optional<? extends Symbol> invokableSymbol = getFunctionSymbol(context);
        if (invokableSymbol.isEmpty()) {
            return Optional.empty();
        }
        List<ParameterInformation> parameterInformationList = new ArrayList<>();
        SignatureInformation signatureInformation = new SignatureInformation();
        SignatureInfoModel signatureInfoModel = getSignatureInfoModel(invokableSymbol.get(), context);

        // Override label for 'new' constructor
        Optional<String> functionName = invokableSymbol.get().getName();
        Optional<NonTerminalNode> nodeAtCursor = context.getNodeAtCursor();
        if (functionName.isEmpty() || nodeAtCursor.isEmpty()) {
            // Should not come to this point
            return Optional.empty();
        }
        StringBuilder labelBuilder = new StringBuilder();
        SyntaxKind syntaxKind = nodeAtCursor.get().kind();
        if (functionName.get().equals(Names.USER_DEFINED_INIT_SUFFIX.getValue())
                && (syntaxKind == SyntaxKind.IMPLICIT_NEW_EXPRESSION
                || syntaxKind == SyntaxKind.EXPLICIT_NEW_EXPRESSION)) {
            labelBuilder.append(SyntaxKind.NEW_KEYWORD.stringValue());
        } else {
            labelBuilder.append(functionName.get());
        }

        labelBuilder.append("(");
        // Join the function parameters to generate the function's signature
        List<ParameterInfoModel> parameterInfoModels = signatureInfoModel.getParameterInfoModels();
        for (int i = 0; i < parameterInfoModels.size(); i++) {
            ParameterInfoModel paramModel = parameterInfoModels.get(i);
            int labelOffset = labelBuilder.toString().length();
            labelBuilder.append(paramModel.parameter.getType());
            ParameterInformation paramInfo = new ParameterInformation();
            paramInfo.setDocumentation(getParameterDocumentation(paramModel));
            int paramStart = labelOffset;
            int paramEnd = labelOffset + paramModel.parameter.getType().length();
            if (paramModel.parameter.getName().isPresent()) {
                paramStart = paramEnd + 1;
                paramEnd += (paramModel.parameter.getName().get() + " ").length();
                labelBuilder.append(" ").append(paramModel.parameter.getName().get());
            }
            if (i < parameterInfoModels.size() - 1) {
                labelBuilder.append(", ");
            }
            paramInfo.setLabel(Tuple.two(paramStart, paramEnd));

            parameterInformationList.add(paramInfo);
        }
        labelBuilder.append(")");
        signatureInformation.setLabel(labelBuilder.toString());
        signatureInformation.setParameters(parameterInformationList);
        signatureInformation.setDocumentation(signatureInfoModel.signatureDescription);

        return Optional.of(signatureInformation);
    }

    /**
     * Get the required signature information filled model.
     *
     * @param symbol  Invokable symbol
     * @param context Lang Server Signature Help Context
     * @return {@link SignatureInfoModel}       SignatureInfoModel containing signature information
     */
    private static SignatureInfoModel getSignatureInfoModel(Symbol symbol, SignatureContext context) {
        Map<String, String> paramToDesc = new HashMap<>();
        SignatureInfoModel signatureInfoModel = new SignatureInfoModel();
        List<ParameterInfoModel> paramModels = new ArrayList<>();
        Optional<Documentation> documentation =
                symbol instanceof Documentable ? ((Documentable) symbol).documentation() : Optional.empty();
        List<Parameter> parameters = new ArrayList<>();
        // TODO: Handle the error constructor in the next phase
        // Handle error constructors
//        if (functionSymbol.kind() == SymbolKind.ERROR_CONSTRUCTOR) {
//            documentation = functionSymbol.type.tsymbol.markdownDocumentation;
//            if (functionSymbol.type instanceof BErrorType) {
//                BErrorType bErrorType = (BErrorType) functionSymbol.type;
//                boolean isDirectErrorConstructor = functionSymbol.type.tsymbol.kind == null;
//                if (isDirectErrorConstructor) {
//                    // If it is direct error constructor, `reason` is mandatory
//                }
//                // todo: need to support error detail map case
//                if (bErrorType.detailType instanceof BRecordType) {
//                    BRecordType bRecordType = (BRecordType) bErrorType.detailType;
//                    bRecordType.fields.values().forEach(p -> {
//                        BVarSymbol symbol = p.symbol;
//                        parameters.add(
//                                new Parameter(symbol.name.getValue(), symbol.type, Symbols.isOptional(symbol),
//                                false));
//                    });
//                    BType restFieldType = bRecordType.restFieldType;
//                    if (restFieldType != null) {
//                        parameters.add(new Parameter(restFieldType.name.getValue(), restFieldType, false, true));
//                    }
//                }
//            }
//        }
        // Check for documentations of the function and parameters
        if (documentation.isPresent()) {
            if (documentation.get().description().isPresent()) {
                signatureInfoModel.setSignatureDescription(documentation.get().description().get().trim(), context);
            }
            documentation.get().parameterMap().forEach(paramToDesc::put);
        }

        signatureInfoModel.setParameterInfoModels(paramModels);
        Optional<FunctionTypeSymbol> functionType = getFunctionType(symbol);

        if (functionType.isEmpty()) {
            return signatureInfoModel;
        }

        FunctionTypeSymbol fnType = functionType.get();

        // Add parameters and rest params
        List<ParameterSymbol> parameterSymbols = fnType.params().orElse(new ArrayList<>());
        parameters.addAll(parameterSymbols.stream()
                                  .map(param -> new Parameter(param, false, false, context))
                                  .collect(Collectors.toList()));
        Optional<ParameterSymbol> restParam = fnType.restParam();
        restParam.ifPresent(parameter -> parameters.add(new Parameter(parameter, false, true, context)));
        boolean skipFirstParam = symbol.kind() == METHOD
                && CommonUtil.isLangLib(symbol.getModule().get().id());
        // Create a list of param info models
        for (int i = 0; i < parameters.size(); i++) {
            if (i == 0 && skipFirstParam) {
                // If langlib, skip first param
                continue;
            }
            Parameter param = parameters.get(i);
            String desc = "";
            if (param.getName().isPresent() && paramToDesc.containsKey(param.getName().get())) {
                desc = paramToDesc.get(param.getName().get());
            }
            paramModels.add(new ParameterInfoModel(param, desc, context));
        }

        return signatureInfoModel;
    }

    private static MarkupContent getParameterDocumentation(ParameterInfoModel paramInfo) {
        MarkupContent paramDocumentation = new MarkupContent();
        paramDocumentation.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        String type = paramInfo.parameter.getType();
        StringBuilder markupContent = new StringBuilder();

        markupContent.append("**Parameter**")
                .append(CommonUtil.MD_LINE_SEPARATOR)
                .append("**")
                .append((!type.isEmpty()) ? "`" + type + "`" : "");
        if (paramInfo.parameter.getName().isPresent()) {
            markupContent.append(paramInfo.parameter.getName().get());
        }
        markupContent.append("**");
        if (!paramInfo.description.isBlank()) {
            markupContent.append(": ").append(paramInfo.description);
        }
        paramDocumentation.setValue(markupContent.toString());

        return paramDocumentation;
    }

    private static Optional<FunctionTypeSymbol> getFunctionType(Symbol symbol) {
        TypeSymbol type;

        switch (symbol.kind()) {
            case FUNCTION:
            case METHOD:
                return Optional.of(((FunctionSymbol) symbol).typeDescriptor());
            case VARIABLE:
                type = ((VariableSymbol) symbol).typeDescriptor();
                break;
            case PARAMETER:
                type = ((ParameterSymbol) symbol).typeDescriptor();
                break;
            default:
                return Optional.empty();
        }

        if (type.typeKind() == TypeDescKind.FUNCTION) {
            return Optional.of((FunctionTypeSymbol) type);
        }

        return Optional.empty();
    }

    /**
     * Parameter model to hold the parameter information meta data.
     */
    private static class Parameter {

        private final boolean isRestArg;
        private final boolean isOptional;
        private final ParameterSymbol parameterSymbol;
        private final SignatureContext signatureContext;

        public Parameter(ParameterSymbol parameterSymbol,
                         boolean isOptional,
                         boolean isRestArg,
                         SignatureContext signatureContext) {
            this.parameterSymbol = parameterSymbol;
            this.isOptional = isOptional;
            this.isRestArg = isRestArg;
            this.signatureContext = signatureContext;
        }

        public Optional<String> getName() {
            return (parameterSymbol.getName().isPresent() && this.isOptional)
                    ? Optional.of(parameterSymbol.getName().get() + "?") : parameterSymbol.getName();
        }

        public String getType() {
            String type = CommonUtil.getModifiedTypeName(this.signatureContext, parameterSymbol.typeDescriptor());
            if (this.isRestArg && !"".equals(type)) {
                // Rest Arg type sometimes appear as array [], sometimes not eg. 'error()'
                if (type.contains("[]")) {
                    type = type.substring(0, type.length() - 2);
                }
                type += "...";
            }

            return type;
        }

    }

    /**
     * Parameter information model to hold the parameter information meta data.
     */
    private static class ParameterInfoModel {

        private final String description;
        private final Parameter parameter;

        public ParameterInfoModel(Parameter parameter, String desc, SignatureContext signatureContext) {
            this.parameter = parameter;
            this.description = desc;
        }

        @Override
        public String toString() {
            return this.parameter.getType()
                    + (parameter.getName().isPresent() ? (" " + parameter.getName().get()) : "");
        }
    }

    /**
     * Signature information model to collect the info required for the signature.
     */
    private static class SignatureInfoModel {

        private List<ParameterInfoModel> parameterInfoModels;

        private Either<String, MarkupContent> signatureDescription;

        List<ParameterInfoModel> getParameterInfoModels() {
            return parameterInfoModels;
        }

        void setParameterInfoModels(List<ParameterInfoModel> parameterInfoModels) {
            this.parameterInfoModels = parameterInfoModels;
        }

        void setSignatureDescription(String signatureDescription, SignatureContext signatureContext) {
            SignatureInformationCapabilities capabilities = signatureContext.capabilities().getSignatureInformation();
            List<String> documentationFormat = capabilities != null ? capabilities.getDocumentationFormat()
                    : new ArrayList<>();
            if (documentationFormat != null
                    && !documentationFormat.isEmpty()
                    && documentationFormat.get(0).equals(CommonUtil.MARKDOWN_MARKUP_KIND)) {
                MarkupContent signatureMarkupContent = new MarkupContent();
                signatureMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
                signatureMarkupContent.setValue(
                        "**Description**" + CommonUtil.MD_LINE_SEPARATOR + signatureDescription);
                this.signatureDescription = Either.forRight(signatureMarkupContent);
            } else {
                this.signatureDescription = Either.forLeft(
                        "Description" + CommonUtil.LINE_SEPARATOR + signatureDescription);
            }
        }
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
            NameReferenceNode nameReferenceNode = ((FunctionCallExpressionNode) nodeAtCursor).functionName();
            String funcName;
            Predicate<Symbol> symbolPredicate =
                    symbol -> symbol.kind() == FUNCTION || symbol.kind() == VARIABLE || symbol.kind() == PARAMETER;
            List<Symbol> filteredContent;
            if (nameReferenceNode.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nameReferenceNode;
                funcName = (qNameRef).identifier().text();
                filteredContent = QNameReferenceUtil.getModuleContent(context, qNameRef,
                                                                      symbolPredicate
                                                                              .and(symbol -> symbol.getName().orElse("")
                                                                                      .equals(funcName)));
            } else {
                funcName = ((SimpleNameReferenceNode) nameReferenceNode).name().text();
                List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
                filteredContent = visibleSymbols.stream()
                        .filter(symbolPredicate.and(symbol -> symbol.getName().get().equals(funcName)))
                        .collect(Collectors.toList());
            }

            return filteredContent.stream().findAny();
        }
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
            ContextTypeResolver resolver = new ContextTypeResolver(context);
            typeDesc = nodeAtCursor.apply(resolver);
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
        functionSymbols.addAll(typeDescriptor.langLibMethods());

        return functionSymbols;
    }
}

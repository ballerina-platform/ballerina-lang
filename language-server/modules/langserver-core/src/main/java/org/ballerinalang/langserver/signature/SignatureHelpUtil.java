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

import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.api.types.RecordTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SignatureInformationCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;

/**
 * Utility functions for the signature help.
 */
public class SignatureHelpUtil {
    private static final String INIT_SYMBOL = ".init";

    private SignatureHelpUtil() {
    }

    /**
     * Get the signature information for the given Ballerina function.
     *
     * @param context Lang Server Signature Help Context
     * @return {@link SignatureInformation}     Signature information for the function
     */
    public static Optional<SignatureInformation> getSignatureInformation(LSContext context)
            throws WorkspaceDocumentException {
        Optional<FunctionSymbol> functionSymbol = getFunctionSymbol(context);
        if (functionSymbol.isEmpty()) {
            return Optional.empty();
        }
        List<ParameterInformation> parameterInformationList = new ArrayList<>();
        SignatureInformation signatureInformation = new SignatureInformation();
        SignatureInfoModel signatureInfoModel = getSignatureInfoModel(functionSymbol.get(), context);

        // Override label for 'new' constructor
        String label = functionSymbol.get().name();
        int initIndex = label.indexOf(INIT_SYMBOL);
        if (initIndex > -1) {
            label = "new " + label.substring(0, initIndex);
        }

        // Join the function parameters to generate the function's signature
        String paramsJoined = signatureInfoModel.getParameterInfoModels().stream().map(parameterInfoModel -> {
            // For each of the parameters, create a parameter info instance
            parameterInformationList.add(getParameterInformation(parameterInfoModel));

            return parameterInfoModel.toString();
        }).collect(Collectors.joining(", "));

        signatureInformation.setLabel(label + "(" + paramsJoined + ")");
        signatureInformation.setParameters(parameterInformationList);
        signatureInformation.setDocumentation(signatureInfoModel.signatureDescription);

        return Optional.of(signatureInformation);
    }

    /**
     * Get the required signature information filled model.
     *
     * @param functionSymbol Invokable symbol
     * @param context        Lang Server Signature Help Context
     * @return {@link SignatureInfoModel}       SignatureInfoModel containing signature information
     */
    private static SignatureInfoModel getSignatureInfoModel(FunctionSymbol functionSymbol, LSContext context) {
        Map<String, String> paramToDesc = new HashMap<>();
        SignatureInfoModel signatureInfoModel = new SignatureInfoModel();
        List<ParameterInfoModel> paramModels = new ArrayList<>();
        Optional<Documentation> documentation = functionSymbol.docAttachment();
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
        // Add parameters and rest params
        functionSymbol.typeDescriptor().requiredParams().forEach(
                param -> parameters.add(new Parameter(param.name().get(), param.typeDescriptor(), false, false))
        );
        Optional<io.ballerina.compiler.api.types.Parameter> restParam = functionSymbol.typeDescriptor().restParam();
        restParam.ifPresent(parameter
                -> parameters.add(new Parameter(parameter.name().get(), parameter.typeDescriptor(), false, true)));
        boolean skipFirstParam = functionSymbol.kind() == METHOD && CommonUtil.isLangLib(functionSymbol.moduleID());
        // Create a list of param info models
        for (int i = 0; i < parameters.size(); i++) {
            if (i == 0 && skipFirstParam) {
                // If langlib, skip first param
                continue;
            }
            Parameter param = parameters.get(i);
            String name = param.isOptional ? param.name + "?" : param.name;
            String desc = "";
            if (paramToDesc.containsKey(param.name)) {
                desc = paramToDesc.get(param.name);
            }
            String type = param.type.signature();
            if (param.isRestArg && !"".equals(type)) {
                // Rest Arg type sometimes appear as array [], sometimes not eg. 'error()'
                if (type.contains("[]")) {
                    type = type.substring(0, type.length() - 2);
                }
                type += "...";
            }
            paramModels.add(new ParameterInfoModel(name, type, desc));
        }
        signatureInfoModel.setParameterInfoModels(paramModels);
        return signatureInfoModel;
    }

    private static ParameterInformation getParameterInformation(ParameterInfoModel parameterInfoModel) {
        MarkupContent paramDocumentation = new MarkupContent();
        paramDocumentation.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        String type = parameterInfoModel.paramType;
        String markupContent = "**Parameter**" + CommonUtil.MD_LINE_SEPARATOR;
        markupContent += "**" + ((!type.isEmpty()) ? "`" + type + "`" : "");
        markupContent += parameterInfoModel.paramValue + "**: ";
        paramDocumentation.setValue(markupContent + parameterInfoModel.description);
        return new ParameterInformation(parameterInfoModel.toString(), paramDocumentation);
    }

    /**
     * Parameter model to hold the parameter information meta data.
     */
    private static class Parameter {
        private final String name;
        private final BallerinaTypeDescriptor type;
        private final boolean isRestArg;
        private final boolean isOptional;

        public Parameter(String name, BallerinaTypeDescriptor type, boolean isOptional, boolean isRestArg) {
            this.name = name;
            this.type = type;
            this.isOptional = isOptional;
            this.isRestArg = isRestArg;
        }
    }

    /**
     * Parameter information model to hold the parameter information meta data.
     */
    private static class ParameterInfoModel {
        private final String paramValue;
        private final String paramType;
        private final String description;

        public ParameterInfoModel(String name, String type, String desc) {
            this.paramValue = name;
            this.paramType = type;
            this.description = desc;
        }

        @Override
        public String toString() {
            return this.paramType + " " + this.paramValue;
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

        void setSignatureDescription(String signatureDescription, LSContext signatureContext) {
            SignatureInformationCapabilities capabilities = signatureContext
                    .get(SignatureKeys.SIGNATURE_HELP_CAPABILITIES_KEY).getSignatureInformation();
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
     *
     * @throws WorkspaceDocumentException while retrieving the syntax tree from the document manager
     */
    private static Optional<NonTerminalNode> getTokenInfoAtCursor(LSContext context) throws WorkspaceDocumentException {
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(context.get(DocumentServiceKeys.FILE_URI_KEY));
        if (filePath.isEmpty()) {
            return Optional.empty();
        }
        SyntaxTree syntaxTree = docManager.getTree(filePath.get());
        TextDocument textDocument = syntaxTree.textDocument();

        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.put(CompletionKeys.TEXT_POSITION_IN_TREE, txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) syntaxTree.rootNode()).findNode(range);

        while (true) {
            if (!withinTextRange(txtPos, nonTerminalNode) || (nonTerminalNode.kind() != SyntaxKind.FUNCTION_CALL
                    && nonTerminalNode.kind() != SyntaxKind.METHOD_CALL)
                    && nonTerminalNode.kind() != SyntaxKind.REMOTE_METHOD_CALL_ACTION) {
                nonTerminalNode = nonTerminalNode.parent();
                continue;
            }
            break;
        }

        return Optional.of(nonTerminalNode);
    }

    private static boolean withinTextRange(int position, NonTerminalNode node) {
        TextRange rangeWithMinutiae = node.textRangeWithMinutiae();
        TextRange textRange = node.textRange();
        TextRange leadingMinutiaeRange = TextRange.from(rangeWithMinutiae.startOffset(),
                textRange.startOffset() - rangeWithMinutiae.startOffset());
        return leadingMinutiaeRange.endOffset() <= position;
    }

    public static Optional<FunctionSymbol> getFunctionSymbol(LSContext context) throws WorkspaceDocumentException {
        Optional<NonTerminalNode> tokenAtCursor = getTokenInfoAtCursor(context);
        if (tokenAtCursor.isEmpty()) {
            return Optional.empty();
        }

        if (tokenAtCursor.get().kind() == SyntaxKind.FUNCTION_CALL) {
            String funcName = ((SimpleNameReferenceNode) ((FunctionCallExpressionNode) tokenAtCursor.get())
                    .functionName()).name().text();
            List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
            return visibleSymbols.stream().filter(symbol -> symbol.kind() == FUNCTION && symbol.name().equals(funcName))
                    .map(symbol -> (FunctionSymbol) symbol)
                    .findAny();
        }
        Optional<? extends BallerinaTypeDescriptor> typeDesc;
        String methodName;
        if (tokenAtCursor.get().kind() == SyntaxKind.METHOD_CALL) {
            MethodCallExpressionNode methodCall = (MethodCallExpressionNode) tokenAtCursor.get();
            typeDesc = getTypeDesc(context, methodCall.expression());
            methodName = ((SimpleNameReferenceNode) methodCall.methodName()).name().text();
        } else if (tokenAtCursor.get().kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION) {
            RemoteMethodCallActionNode remoteMethodCall = (RemoteMethodCallActionNode) tokenAtCursor.get();
            typeDesc = getTypeDesc(context, remoteMethodCall.expression());
            methodName = remoteMethodCall.methodName().name().text();
        } else {
            return Optional.empty();
        }

        if (typeDesc.isEmpty()) {
            return Optional.empty();
        }

        return getFunctionSymbolsForTypeDesc(typeDesc.get()).stream()
                .filter(functionSymbol -> functionSymbol.name().equals(methodName))
                .findAny();
    }

    private static Optional<? extends BallerinaTypeDescriptor> getTypeDesc(LSContext ctx, ExpressionNode expr) {
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

    private static Optional<? extends BallerinaTypeDescriptor> getTypeDescForFieldAccess(
            LSContext context, FieldAccessExpressionNode node) {
        String fieldName = ((SimpleNameReferenceNode) node.fieldName()).name().text();
        ExpressionNode expressionNode = node.expression();
        Optional<? extends BallerinaTypeDescriptor> typeDescriptor = getTypeDesc(context, expressionNode);

        if (typeDescriptor.isEmpty()) {
            return Optional.empty();
        }

        List<FieldDescriptor> fieldDescriptors = new ArrayList<>();

        if (CommonUtil.getRawType(typeDescriptor.get()).kind() == TypeDescKind.OBJECT) {
            fieldDescriptors.addAll(((ObjectTypeDescriptor) CommonUtil
                    .getRawType(typeDescriptor.get())).fieldDescriptors());
        } else if (CommonUtil.getRawType(typeDescriptor.get()).kind() == TypeDescKind.RECORD) {
            fieldDescriptors.addAll(((RecordTypeDescriptor) CommonUtil
                    .getRawType(typeDescriptor.get())).fieldDescriptors());
        }

        return fieldDescriptors.stream()
                .filter(fieldDescriptor -> fieldDescriptor.name().equals(fieldName))
                .map(FieldDescriptor::typeDescriptor)
                .findAny();
    }

    private static Optional<? extends BallerinaTypeDescriptor> getTypeDescForNameRef(LSContext context,
                                                                                     NameReferenceNode referenceNode) {
        if (referenceNode.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String name = ((SimpleNameReferenceNode) referenceNode).name().text();
        List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        Optional<Symbol> symbolRef = visibleSymbols.stream()
                .filter(symbol -> symbol.name().equals(name))
                .findFirst();
        if (symbolRef.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(symbolRef.get());
    }

    private static Optional<? extends BallerinaTypeDescriptor> getTypeDescForFunctionCall(
            LSContext context, FunctionCallExpressionNode expr) {
        String fName = ((SimpleNameReferenceNode) expr.functionName()).name().text();
        List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        Optional<FunctionSymbol> symbolRef = visibleSymbols.stream()
                .filter(symbol -> symbol.name().equals(fName) && symbol.kind() == SymbolKind.FUNCTION)
                .map(symbol -> (FunctionSymbol) symbol)
                .findFirst();
        if (symbolRef.isEmpty()) {
            return Optional.empty();
        }

        return symbolRef.get().typeDescriptor().returnTypeDescriptor();
    }

    private static Optional<? extends BallerinaTypeDescriptor> getTypeDescForMethodCall(
            LSContext context, MethodCallExpressionNode node) {
        String methodName = ((SimpleNameReferenceNode) node.methodName()).name().text();

        Optional<? extends BallerinaTypeDescriptor> fieldTypeDesc = getTypeDesc(context, node.expression());

        if (fieldTypeDesc.isEmpty()) {
            return Optional.empty();
        }

        List<MethodSymbol> visibleMethods = fieldTypeDesc.get().builtinMethods();
        if (CommonUtil.getRawType(fieldTypeDesc.get()).kind() == TypeDescKind.OBJECT) {
            visibleMethods.addAll(((ObjectTypeDescriptor) CommonUtil.getRawType(fieldTypeDesc.get())).methods());
        }
        Optional<MethodSymbol> filteredMethod = visibleMethods.stream()
                .filter(methodSymbol -> methodSymbol.name().equals(methodName))
                .findFirst();

        if (filteredMethod.isEmpty()) {
            return Optional.empty();
        }

        return filteredMethod.get().typeDescriptor().returnTypeDescriptor();
    }

    private static List<FunctionSymbol> getFunctionSymbolsForTypeDesc(BallerinaTypeDescriptor typeDescriptor) {
        List<FunctionSymbol> functionSymbols = new ArrayList<>();
        if (CommonUtil.getRawType(typeDescriptor).kind() == TypeDescKind.OBJECT) {
            ObjectTypeDescriptor objTypeDesc = (ObjectTypeDescriptor) CommonUtil.getRawType(typeDescriptor);
            functionSymbols.addAll(objTypeDesc.methods());
        }
        functionSymbols.addAll(typeDescriptor.builtinMethods());

        return functionSymbols;
    }
}

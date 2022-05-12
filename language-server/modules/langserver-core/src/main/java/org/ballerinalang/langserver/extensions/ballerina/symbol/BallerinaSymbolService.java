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
package org.ballerinalang.langserver.extensions.ballerina.symbol;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManagerProxy;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Ballerina Symbol Service LS Extension.
 *
 * @since 0.981.2
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("ballerinaSymbol")
public class BallerinaSymbolService implements ExtendedLanguageServerService {

    private WorkspaceManagerProxy workspaceManagerProxy;
    private LSClientLogger clientLogger;
    private LanguageServerContext serverContext;

    @Override
    public void init(LanguageServer langServer, WorkspaceManagerProxy workspaceManagerProxy,
                     LanguageServerContext serverContext) {
        this.workspaceManagerProxy = workspaceManagerProxy;
        this.serverContext = serverContext;
        this.clientLogger = LSClientLogger.getInstance(serverContext);
    }

    @JsonRequest
    public CompletableFuture<BallerinaEndpointsResponse> endpoints() {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaEndpointsResponse response = new BallerinaEndpointsResponse();
            response.setEndpoints(getClientEndpoints());
            return response;
        });
    }

    private List<Endpoint> getClientEndpoints() {
        final List<Endpoint> endpoints = new ArrayList<>();
        // TODO: Implementation Required
        return endpoints;
    }

    @JsonRequest
    public CompletableFuture<ExpressionTypeResponse> type(ExpressionTypeRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            ExpressionTypeResponse expressionTypeResponse = new ExpressionTypeResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
            if (filePath.isEmpty()) {
                return expressionTypeResponse;
            }

            try {
                expressionTypeResponse.setDocumentIdentifier(new TextDocumentIdentifier(fileUri));

                // Get the semantic model.
                Optional<SemanticModel> semanticModel = this.workspaceManagerProxy.get().semanticModel(filePath.get());

                if (semanticModel.isEmpty()) {
                    return expressionTypeResponse;
                }

                Optional<Symbol> symbol = Optional.empty();
                for (int i = 0; i < request.getPosition().offset(); i++) {
                    symbol = semanticModel.get().symbol(workspaceManagerProxy.get().document(filePath.get()).get(),
                            LinePosition.from(request.getPosition().line(),
                                    request.getPosition().offset() - i));
                    if (symbol.isPresent()) {
                        break;
                    }
                }
                symbol.ifPresent(value -> expressionTypeResponse.setTypes(getAllUnionTypes(value)));
                return expressionTypeResponse;

            } catch (Throwable e) {
                String msg = "Operation 'ballerinaSymbol/type' failed!";
                this.clientLogger.logError(SymbolContext.SC_TYPE_API, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
                return expressionTypeResponse;
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SymbolInfoResponse> getSymbol(SymbolInfoRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            SymbolInfoResponse symbolInfoResponse = new SymbolInfoResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);

            if (filePath.isEmpty()) {
                return symbolInfoResponse;
            }

            try {
                Optional<SemanticModel> semanticModel = this.workspaceManagerProxy.get(fileUri).
                        semanticModel(filePath.get());
                if (semanticModel.isEmpty()) {
                    return symbolInfoResponse;
                }

                DocumentServiceContext context = ContextBuilder.buildDocumentServiceContext(fileUri,
                        this.workspaceManagerProxy.get(fileUri),
                        LSContextOperation.SYMBOL_DOCUMENT,
                        this.serverContext);

                Optional<Document> srcFile = context.currentDocument();
                if (srcFile.isEmpty()) {
                    return symbolInfoResponse;
                }
                LinePosition linePosition = LinePosition.from(request.getPosition().getLine(),
                        request.getPosition().getCharacter());
                Optional<? extends Symbol> symbolAtCursor = semanticModel.get().symbol(srcFile.get(), linePosition);

                if (symbolAtCursor.isEmpty()) {
                    return symbolInfoResponse;
                }

                Optional<Documentation> documentation = symbolAtCursor.get() instanceof Documentable ?
                        ((Documentable) symbolAtCursor.get()).documentation() : Optional.empty();

                if (documentation.isEmpty()) {
                    return symbolInfoResponse;
                }

                List<SymbolDocumentation.ParameterInfo> symbolParams = new ArrayList<>();

                if (symbolAtCursor.get() instanceof FunctionSymbol) {
                    FunctionSymbol functionSymbol = (FunctionSymbol) symbolAtCursor.get();

                    if (functionSymbol.typeDescriptor().params().isPresent()) {
                        List<ParameterSymbol> parameterSymbolList = functionSymbol.typeDescriptor().params().get();

                        parameterSymbolList.stream().filter((parameterSymbol) ->
                                parameterSymbol.getName().isPresent())
                                .forEach(parameterSymbol -> {

                                    symbolParams.add(new SymbolDocumentation.ParameterInfo(
                                            parameterSymbol.getName().get(),
                                            documentation.get().parameterMap().get(parameterSymbol.getName().get()),
                                            parameterSymbol.paramKind().name(),
                                            CommonUtil.getModifiedTypeName(context, parameterSymbol.typeDescriptor())));
                                });
                    }

                    Optional<ParameterSymbol> restParam = functionSymbol.typeDescriptor().restParam();
                    if (restParam.isPresent() && restParam.get().getName().isPresent()) {
                        ParameterSymbol restParameter = restParam.get();
                        symbolParams.add(new SymbolDocumentation.ParameterInfo(
                                restParameter.getName().get(),
                                documentation.get().parameterMap().get(restParameter.getName().get()),
                                restParameter.paramKind().name(),
                                CommonUtil.getModifiedTypeName(context, restParameter.typeDescriptor())
                        ));
                    }
                }

                List<SymbolDocumentation.ParameterInfo> deprecatedParams = new ArrayList<>(
                        documentation.get().deprecatedParametersMap().size());
                Map<String, String> deprecatedParamMap = documentation.get().deprecatedParametersMap();
                deprecatedParamMap.forEach((param, desc) -> {
                    deprecatedParams.add(new SymbolDocumentation.ParameterInfo(param, desc));
                });

                SymbolDocumentation symbolDoc = new SymbolDocumentation(documentation.get(), symbolParams,
                        deprecatedParams);

                symbolInfoResponse.setSymbolDocumentation(symbolDoc);
                symbolInfoResponse.setSymbolKind(symbolAtCursor.get().kind());

                return symbolInfoResponse;

            } catch (Throwable e) {
                String msg = "Operation 'ballerinaSymbol/getSymbol' failed!";
                this.clientLogger.logError(SymbolContext.SC_GET_SYMBOL_API, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
                return symbolInfoResponse;
            }
        });
    }

    private List<String> getAllUnionTypes(Symbol symbol) {
        List<String> allTypes = new ArrayList<>();
        if (symbol.kind() == SymbolKind.VARIABLE) {
            VariableSymbol variableSymbol = (VariableSymbol) symbol;
            if (variableSymbol.typeDescriptor().typeKind() == TypeDescKind.UNION) {
                UnionTypeSymbol ballerinaUnionTypeSymbol =
                        (UnionTypeSymbol) variableSymbol.typeDescriptor();
                for (TypeSymbol typeSymbol : ballerinaUnionTypeSymbol.memberTypeDescriptors()) {
                    if (!allTypes.contains(typeSymbol.typeKind().getName())) {
                        allTypes.add(typeSymbol.typeKind().getName());
                    }
                }
            } else if (variableSymbol.typeDescriptor().typeKind() == TypeDescKind.TYPE_REFERENCE) {
                allTypes.add(variableSymbol.typeDescriptor().getName().get());
            } else {
                allTypes.add(variableSymbol.typeDescriptor().typeKind().getName());
            }
        } else if (symbol.kind() == SymbolKind.METHOD) {
            MethodSymbol methodSymbol = (MethodSymbol) symbol;
            TypeSymbol returnTypeSymbol = methodSymbol.typeDescriptor().returnTypeDescriptor().get();
            if (returnTypeSymbol.typeKind() == TypeDescKind.UNION) {
                UnionTypeSymbol ballerinaUnionTypeSymbol = (UnionTypeSymbol) returnTypeSymbol;
                for (TypeSymbol typeSymbol : ballerinaUnionTypeSymbol.memberTypeDescriptors()) {
                    if (!allTypes.contains(typeSymbol.typeKind().getName())) {
                        allTypes.add(typeSymbol.typeKind().getName());
                    }
                }
            } else {
                allTypes.add(returnTypeSymbol.typeKind().getName());
            }
        }

        return allTypes;
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }
}

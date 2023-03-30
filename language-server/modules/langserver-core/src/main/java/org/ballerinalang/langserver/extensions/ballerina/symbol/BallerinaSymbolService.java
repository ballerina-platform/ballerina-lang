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
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.diagramutil.connector.models.connector.Type;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.codeaction.MatchedExpressionNodeResolver;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManagerProxy;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
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
            Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);
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
    public CompletableFuture<TypesFromExpressionResponse> getTypeFromExpression(TypeFromExpressionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            TypesFromExpressionResponse typesResponse = new TypesFromExpressionResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            String[] pathSegments = fileUri.split("/");
            String fileName = pathSegments[pathSegments.length - 1];
            List<ResolvedTypeForExpression> types = new ArrayList<>();
            try {
                Path filePath = PathUtil.getPathFromURI(fileUri).orElseThrow();
                for (LineRange range: request.getExpressionRanges()) {
                    ResolvedTypeForExpression resolvedType = new ResolvedTypeForExpression(range);
                    SemanticModel semanticModel = this.workspaceManagerProxy
                            .get(fileUri)
                            .semanticModel(filePath)
                            .orElseThrow();
                    LinePosition start = range.startLine();
                    LinePosition end = range.endLine();
                    LineRange lineRange = LineRange.from(fileName, start, end);
                    Optional<TypeSymbol> typeSymbol;
                    if (semanticModel.typeOf(lineRange).isPresent()) {
                        typeSymbol = semanticModel.typeOf(lineRange);
                        Type.clearParentSymbols();
                        Type type = typeSymbol.map(Type::fromSemanticSymbol).orElse(null);
                        resolvedType.setType(type);
                        types.add(resolvedType);
                    }
                }
                typesResponse.setTypes(types);
                return typesResponse;
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaSymbol/getTypeFromExpression' failed!";
                this.clientLogger.logError(SymbolContext.SC_GET_TYPE_FROM_EXPRESSION_API, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
                return typesResponse;
            }
        });
    }

    @JsonRequest
    public CompletableFuture<TypesFromSymbolResponse> getTypeFromSymbol(TypeFromSymbolRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            TypesFromSymbolResponse typeFromSymbolResponse = new TypesFromSymbolResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            List<ResolvedTypeForSymbol> types = new ArrayList<>();
            try {
                Path filePath = PathUtil.getPathFromURI(fileUri).orElseThrow();
                for (LinePosition position: request.getPositions()) {
                    ResolvedTypeForSymbol resolvedType = new ResolvedTypeForSymbol(position);
                    SemanticModel semanticModel = this.workspaceManagerProxy
                            .get(fileUri)
                            .semanticModel(filePath)
                            .orElseThrow();
                    Document document = this.workspaceManagerProxy
                            .get(fileUri)
                            .document(filePath)
                            .orElseThrow();
                    LinePosition linePosition = LinePosition.from(position.line(), position.offset());
                    Optional<Symbol> symbol = semanticModel.symbol(document, linePosition);
                    Type.clearParentSymbols();
                    Type type = symbol.map(Type::fromSemanticSymbol).orElse(null);
                    resolvedType.setType(type);
                    types.add(resolvedType);
                }
                typeFromSymbolResponse.setTypes(types);
                return typeFromSymbolResponse;
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaSymbol/getTypeFromSymbol' failed!";
                this.clientLogger.logError(SymbolContext.SC_GET_TYPE_FROM_SYMBOL_API, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
                return typeFromSymbolResponse;
            }
        });
    }

    @JsonRequest
    public CompletableFuture<TypesFromSymbolResponse> getTypesFromFnDefinition(TypesFromFnDefinitionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            TypesFromSymbolResponse typeFromSymbolResponse = new TypesFromSymbolResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            List<ResolvedTypeForSymbol> types = new ArrayList<>();
            try {
                Path filePath = PathUtil.getPathFromURI(fileUri).orElseThrow();
                SemanticModel semanticModel = this.workspaceManagerProxy
                        .get(fileUri)
                        .semanticModel(filePath)
                        .orElseThrow();
                Document document = this.workspaceManagerProxy
                        .get(fileUri)
                        .document(filePath)
                        .orElseThrow();
                LinePosition fnPosition = request.getFnPosition();
                Symbol fnSymbol = semanticModel.symbol(document, fnPosition).orElseThrow();
                if (fnSymbol instanceof FunctionSymbol) {
                    FunctionTypeSymbol fnTypeSymbol = ((FunctionSymbol) fnSymbol).typeDescriptor();

                    Optional<ResolvedTypeForSymbol> returnType =
                            getTypeForReturnTypeDesc(fnTypeSymbol, request.getReturnTypeDescPosition());
                    returnType.ifPresent(types::add);

                    List<ResolvedTypeForSymbol> paramTypes = getTypesForFnParams(fnTypeSymbol);
                    types.addAll(paramTypes);
                }
                typeFromSymbolResponse.setTypes(types);
                return typeFromSymbolResponse;
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaSymbol/getTypesFromFnDefinition' failed!";
                this.clientLogger.logError(SymbolContext.SC_GET_TYPE_FROM_FN_DEFINITION_API, msg, e,
                        request.getDocumentIdentifier(), (Position) null);
                return typeFromSymbolResponse;
            }
        });
    }

    @JsonRequest
    public CompletableFuture<SymbolInfoResponse> getSymbol(SymbolInfoRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            SymbolInfoResponse symbolInfoResponse = new SymbolInfoResponse();
            String fileUri = request.getDocumentIdentifier().getUri();
            Optional<Path> filePath = PathUtil.getPathFromURI(fileUri);

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

                Range nodeRange = new Range(request.getPosition(), request.getPosition());
                NonTerminalNode nodeAtCursor = CommonUtil.findNode(nodeRange, srcFile.get().syntaxTree());

                if (symbolAtCursor.isEmpty()) {
                    if (nodeAtCursor != null) {
                        MatchedExpressionNodeResolver exprResolver = new MatchedExpressionNodeResolver(nodeAtCursor);
                        Optional<ExpressionNode> expr = exprResolver.findExpression(nodeAtCursor);
                        if (expr.isPresent()) {
                            return getDocMetadataForNewExpression(expr.get(), context, symbolInfoResponse,
                                    nodeAtCursor);
                        }
                    }
                    return symbolInfoResponse;
                }

                return getSymbolDocMetadata(symbolAtCursor.get(), symbolInfoResponse, context, nodeAtCursor);

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

    private SymbolInfoResponse getDocMetadataForNewExpression(Node exprNode, DocumentServiceContext context,
                                                              SymbolInfoResponse symbolInfoResponse,
                                                              NonTerminalNode nodeAtCursor) {
        switch (exprNode.kind()) {
            case IMPLICIT_NEW_EXPRESSION:
            case EXPLICIT_NEW_EXPRESSION:
                Optional<TypeSymbol> optionalTypeSymbol = context.currentSemanticModel()
                        .flatMap(semanticModel -> semanticModel.typeOf(exprNode))
                        .map(CommonUtil::getRawType);
                if (optionalTypeSymbol.isEmpty()) {
                    break;
                }

                TypeSymbol typeSymbol = optionalTypeSymbol.get();
                if (typeSymbol.typeKind() == TypeDescKind.UNION) {
                    UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
                    Optional<TypeSymbol> classTypeSymbol = unionTypeSymbol.memberTypeDescriptors().stream()
                            .map(CommonUtil::getRawType)
                            .filter(member -> member.typeKind() != TypeDescKind.ERROR)
                            .findFirst();
                    if (classTypeSymbol.isEmpty()) {
                        break;
                    }
                    typeSymbol = classTypeSymbol.get();
                }

                if (typeSymbol instanceof ClassSymbol) {
                    ClassSymbol classSymbol = (ClassSymbol) typeSymbol;
                    if (classSymbol.initMethod().isEmpty()) {
                        break;
                    }

                    MethodSymbol initMethodSymbol = classSymbol.initMethod().get();
                    return getSymbolDocMetadata(initMethodSymbol, symbolInfoResponse, context, nodeAtCursor);
                }
        }
        return symbolInfoResponse;
    }

    private SymbolInfoResponse getSymbolDocMetadata(Symbol symbolAtCursor, SymbolInfoResponse symbolInfoResponse,
                                                    DocumentServiceContext context, NonTerminalNode nodeAtCursor) {
        Optional<Documentation> documentation = symbolAtCursor instanceof Documentable ?
                ((Documentable) symbolAtCursor).documentation() : Optional.empty();

        if (documentation.isEmpty()) {
            return symbolInfoResponse;
        }

        List<SymbolDocumentation.ParameterInfo> symbolParams = new ArrayList<>();

        if (symbolAtCursor instanceof FunctionSymbol) {
            FunctionSymbol functionSymbol = (FunctionSymbol) symbolAtCursor;

            if (functionSymbol.typeDescriptor().params().isPresent()) {
                List<ParameterSymbol> parameterSymbolList = functionSymbol.typeDescriptor().params().get();

                parameterSymbolList
                        .subList(skipFirstParam(symbolAtCursor, nodeAtCursor) ? 1 : 0, parameterSymbolList.size())
                        .stream().filter((parameterSymbol) -> parameterSymbol.getName().isPresent())
                        .forEach(parameterSymbol -> {

                            symbolParams.add(new SymbolDocumentation.ParameterInfo(
                                    parameterSymbol.getName().get(),
                                    documentation.get().parameterMap().get(parameterSymbol.getName().get()),
                                    parameterSymbol.paramKind().name(),
                                    NameUtil.getModifiedTypeName(context, parameterSymbol.typeDescriptor())));
                        });
            }

            Optional<ParameterSymbol> restParam = functionSymbol.typeDescriptor().restParam();
            if (restParam.isPresent() && restParam.get().getName().isPresent()) {
                ParameterSymbol restParameter = restParam.get();
                symbolParams.add(new SymbolDocumentation.ParameterInfo(
                        restParameter.getName().get(),
                        documentation.get().parameterMap().get(restParameter.getName().get()),
                        restParameter.paramKind().name(),
                        NameUtil.getModifiedTypeName(context, restParameter.typeDescriptor())
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
        symbolInfoResponse.setSymbolKind(symbolAtCursor.kind());

        return symbolInfoResponse;
    }

    private boolean skipFirstParam(Symbol symbolAtCursor, NonTerminalNode nodeAtCursor) {
        return (symbolAtCursor.kind() == SymbolKind.FUNCTION || symbolAtCursor.kind() == SymbolKind.METHOD)
                && (symbolAtCursor.getModule().isPresent() &&
                CommonUtil.isLangLib(symbolAtCursor.getModule().get().id()) &&
                nodeAtCursor.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE);
    }

    private Optional<ResolvedTypeForSymbol> getTypeForReturnTypeDesc(FunctionTypeSymbol functionTypeSymbol,
                                                                     LinePosition typeDescPosition) {
        Optional<TypeSymbol> typeSymbol = functionTypeSymbol.returnTypeDescriptor();
        if (typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        ResolvedTypeForSymbol resolvedType = new ResolvedTypeForSymbol(typeDescPosition);
        Type type = Type.fromSemanticSymbol(typeSymbol.get());
        Type.clearParentSymbols();
        resolvedType.setType(type);
        return Optional.of(resolvedType);
    }

    private List<ResolvedTypeForSymbol> getTypesForFnParams(FunctionTypeSymbol fnTypeSymbol) {
        List<ResolvedTypeForSymbol> types = new ArrayList<>();
        Optional<List<ParameterSymbol>> params = fnTypeSymbol.params();
        if (params.isPresent()) {
            for (ParameterSymbol param: params.get()) {
                Optional<Location> location = param.getLocation();
                if (location.isPresent()) {
                    LinePosition paramPosition = location.get().lineRange().startLine();
                    ResolvedTypeForSymbol resolvedType = new ResolvedTypeForSymbol(paramPosition);
                    Type type = Type.fromSemanticSymbol(param);
                    Type.clearParentSymbols();
                    resolvedType.setType(type);
                    types.add(resolvedType);
                }
            }
        }
        return types;
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }
}

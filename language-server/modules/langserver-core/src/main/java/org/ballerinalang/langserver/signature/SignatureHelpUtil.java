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

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.SourcePruneException;
import org.ballerinalang.langserver.signature.sourceprune.SignatureTokenTraverserFactory;
import org.ballerinalang.langserver.sourceprune.SourcePruner;
import org.ballerinalang.langserver.sourceprune.TokenTraverserFactory;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SignatureInformationCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.common.utils.CommonUtil.getLastItem;
import static org.ballerinalang.langserver.common.utils.FilterUtils.getLangLibScopeEntries;

/**
 * Utility functions for the signature help.
 */
public class SignatureHelpUtil {
    private static final String COMMA = ",";
    private static final String SEMI_COLON = ";";
    private static final String EQUAL = "=";
    private static final String INIT_SYMBOL = ".__init";

    private SignatureHelpUtil() {
    }

    /**
     * Capture the callable item information such as name, package of the item, delimiter (. or :), and etc.
     *
     * @param serviceContext    Text Document service context instance for the signature help operation
     * @return A {@link Pair} of function path and parameter count
     * @throws CompilationFailedException when compilation fails
     */
    public static Pair<Optional<String>, Integer> getFunctionInvocationDetails(LSServiceOperationContext serviceContext)
            throws CompilationFailedException {
        // Generate function invocation from the source pruned text
        Pair<String, Integer> functionInvocationAndParamOffset = extractSourcePrunedInvocationDetails(serviceContext);
        String funcInvocation = functionInvocationAndParamOffset.getLeft();
        int paramIndex = functionInvocationAndParamOffset.getRight();

        // Get right hand side of the assignment
        int equalSymbolIndex = funcInvocation.lastIndexOf(EQUAL);
        String funcInvocationStatement = funcInvocation;
        if (equalSymbolIndex > -1) {
            funcInvocation = funcInvocation.substring(equalSymbolIndex + 1);
        }

        // Get function name using Subrule parser
        String subRuleFormat = "function rightHandExpr() {\n\t%s;\n}".replaceAll("\\n", CommonUtil.LINE_SEPARATOR);
        subRuleFormat += "function wholeStatement() {\n\t%s;\n}".replaceAll("\\n", CommonUtil.LINE_SEPARATOR);
        String subRule = String.format(subRuleFormat, funcInvocation, funcInvocationStatement);

        return new ImmutablePair<>(parseAndGetFunctionInvocationPath(subRule, serviceContext), paramIndex);
    }

    private static Pair<String, Integer> extractSourcePrunedInvocationDetails(LSContext context) {
        int parameterOffset = 0;
        // Collect source-pruned tokens
        List<String> tokenTexts = new ArrayList<>();

        final int[] pendingRParenthesisCount = new int[]{0};
        Consumer<Integer> tokenAcceptor = type -> {
            if (type == BallerinaParser.LEFT_PARENTHESIS) {
                pendingRParenthesisCount[0]--;
            } else if (type == BallerinaParser.RIGHT_PARENTHESIS) {
                pendingRParenthesisCount[0]++;
            }
        };

        // Visit Left-Hand side tokens before cursor
        List<CommonToken> lhsTokens = context.get(CompletionKeys.LHS_TOKENS_KEY);
        if (lhsTokens != null) {
            while (lhsTokens.get(0).getType() == BallerinaParser.COMMA) {
                lhsTokens.remove(0);
            }
            boolean isLastLeftParenthesisProcessed = false;
            for (int i = lhsTokens.size() - 1; i >= 0; i--) {
                Token commonToken = lhsTokens.get(i);
                int tokenType = commonToken.getType();
                if (tokenType == BallerinaParser.LEFT_PARENTHESIS) {
                    // Since we are traversing reverse order, we'll get the last LParenthesis first
                    isLastLeftParenthesisProcessed = true;
                }
                if (tokenType == BallerinaParser.COMMA) {
                    if (i - 1 >= 0 && lhsTokens.get(i - 1).getType() == BallerinaParser.RIGHT_PARENTHESIS) {
                        // eg. func1(func2(10, 10, 5),[cursor]);
                        parameterOffset = 1;
                    } else {
                        tokenTexts.add(commonToken.getText());
                        if (pendingRParenthesisCount[0] == 0 && !isLastLeftParenthesisProcessed) {
                            parameterOffset++;
                        }
                    }
                } else {
                    tokenAcceptor.accept(tokenType);
                    tokenTexts.add(commonToken.getText());
                }
            }
        }

        // Reverse the order of tokens
        Collections.reverse(tokenTexts);

        // Visit Right-Hand side tokens after cursor
        List<CommonToken> rhsTokens = context.get(CompletionKeys.RHS_TOKENS_KEY);
        if (rhsTokens != null && !rhsTokens.isEmpty()) {
            // Remove if any comma[,] when RHS next immediate token is right parenthesis
            int lastIndex = tokenTexts.size() - 1;
            while (lastIndex >= 0 && COMMA.equals(tokenTexts.get(lastIndex))
                    && rhsTokens.get(0).getType() == BallerinaParser.RIGHT_PARENTHESIS) {
                tokenTexts.remove(lastIndex);
                lastIndex--;
            }
            // Collect tokens until we find a Whitespace or Newline
            for (Token commonToken : rhsTokens) {
                int tokenType = commonToken.getType();
                if (tokenType == BallerinaParser.WS || tokenType == BallerinaParser.NEW_LINE) {
                    // Break on Whitespaces or New Lines
                    break;
                }
                tokenAcceptor.accept(commonToken.getType());
                tokenTexts.add(commonToken.getText());
            }
        }

        // Remove if any comma[,] when pendingRParenthesis is less than zero
        int lastIndex = tokenTexts.size() - 1;
        if (COMMA.equals(tokenTexts.get(lastIndex)) && pendingRParenthesisCount[0] <= 0) {
            tokenTexts.remove(lastIndex);
        }

        //  Remove if any semi-colon[;]
        lastIndex = tokenTexts.size() - 1;
        if (SEMI_COLON.equals(tokenTexts.get(lastIndex))) {
            tokenTexts.remove(lastIndex);
        }

        // Add Missing Parenthesis
        while (pendingRParenthesisCount[0] < 0) {
            tokenTexts.add(")");
            pendingRParenthesisCount[0]++;
        }
        return Pair.of(String.join("", tokenTexts), parameterOffset);
    }

    private static Optional<String> parseAndGetFunctionInvocationPath(String subRule, LSServiceOperationContext context)
            throws CompilationFailedException {
        // Replace 'optional field access' with 'field access' to avoid empty top-level nodes
        subRule = subRule.replaceAll("\\?.", ".");
        Optional<BLangPackage> bLangPackage = ExtendedLSCompiler.compileContent(subRule, CompilerPhase.CODE_ANALYZE)
                .getBLangPackage();

        if (!bLangPackage.isPresent()) {
            return Optional.empty();
        }

        List<TopLevelNode> topLevelNodes = bLangPackage.get().getCompilationUnits().get(0).getTopLevelNodes();
        if (topLevelNodes.isEmpty()) {
            return Optional.empty();
        }
        BLangStatement evalStatement = ((BLangFunction) topLevelNodes.get(0)).getBody().stmts.get(0);

        // Handle object new constructor
        if (evalStatement instanceof BLangExpressionStmt
                && ((BLangExpressionStmt) evalStatement).expr instanceof BLangTypeInit && topLevelNodes.size() >= 2) {
            BLangStatement stmt = ((BLangFunction) topLevelNodes.get(1)).getBody().stmts.get(0);
            if (stmt instanceof BLangSimpleVariableDef) {
                BLangSimpleVariableDef varDef = (BLangSimpleVariableDef) stmt;
                if (varDef.var.typeNode instanceof BLangUserDefinedType) {
                    BLangUserDefinedType typeNode = (BLangUserDefinedType) varDef.var.typeNode;
                    return Optional.of(addPackagePrefix(typeNode.pkgAlias, context, typeNode.typeName + INIT_SYMBOL));
                }
                return Optional.of(varDef.var.typeNode.toString() + INIT_SYMBOL);
            } else if (stmt instanceof BLangExpressionStmt &&
                    ((BLangExpressionStmt) stmt).expr instanceof BLangTypeInit) {
                BLangTypeInit bLangTypeInit = (BLangTypeInit) ((BLangExpressionStmt) stmt).expr;
                return Optional.of(addPackagePrefix(bLangTypeInit.userDefinedType.pkgAlias, context,
                                                    bLangTypeInit.userDefinedType.typeName + INIT_SYMBOL));
            }
        }

        return resolveSymbolPath(context, evalStatement, new Stack<>());
    }

    private static Optional<String> resolveSymbolPath(LSServiceOperationContext context,
                                                      BLangNode bLangNode, Stack<String> path) {
        if (bLangNode instanceof BLangInvocation) {
            BLangInvocation invocationNode = (BLangInvocation) bLangNode;
            String value = getFullyQualifiedName(invocationNode, context);
            path.push(value);
            return resolveSymbolPath(context, invocationNode.getExpression(), path);
        } else if (bLangNode instanceof BLangSimpleVarRef) {
            // If there's a object ref
            path.push(((BLangSimpleVarRef) bLangNode).variableName.value);
        } else {
            // If there's a field access
            if (bLangNode instanceof BLangFieldBasedAccess) {
                path.push(((BLangFieldBasedAccess) bLangNode).field.value);
            }
            // Extract inner expression of 'check', 'checkpanic', 'trap' ...etc
            BLangExpression innerExpression = getInnerExpression(bLangNode);
            if (innerExpression != null) {
                return resolveSymbolPath(context, innerExpression, path);
            }
        }
        StringJoiner pathStr = new StringJoiner(".");
        while (!path.empty()) {
            pathStr.add(path.pop());
        }
        return Optional.of(pathStr.toString());
    }

    private static BLangExpression getInnerExpression(BLangNode node) {
        try {
            if (node == null) {
                return null;
            }
            Map<Field, BLangExpression> exprs = new HashMap<>();
            for (Field field : node.getClass().getFields()) {
                Object obj = field.get(node);
                if (obj instanceof BLangExpression && !field.getName().equals("parent")) {
                    exprs.put(field, (BLangExpression) obj);
                }
            }

            if (!exprs.isEmpty()) {
                // Return most suitable BLangExpression
                Comparator<Map.Entry<Field, BLangExpression>> byExpr = Comparator.comparing(
                        e -> "expr".equals(e.getKey().getName())
                );

                Comparator<Map.Entry<Field, BLangExpression>> byStartsWithExpr = Comparator.comparing(
                        e -> e.getKey().getName().startsWith("expr")
                );

                // Priority given to fields as of expr, expression, <some-other-name>
                return exprs.entrySet().stream()
                        .sorted(byStartsWithExpr)
                        .max(byExpr)
                        .map(Map.Entry::getValue)
                        .orElse(null);
            }
            return null;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return null;
        }
    }

    public static Optional<SymbolInfo> getFuncSymbolInfo(LSServiceOperationContext context, String funcName,
                                                         List<SymbolInfo> visibleSymbols) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        Types types = Types.getInstance(compilerContext);

        String[] nameComps = funcName.split("\\.");
        int index = 0;
        Optional<SymbolInfo> searchSymbol = Optional.empty();
        while (index < nameComps.length) {
            String name = nameComps[index];
            int pkgPrefix = name.indexOf(":");
            boolean hasPkgPrefix = pkgPrefix > -1;
            if (!hasPkgPrefix) {
                searchSymbol = visibleSymbols.stream()
                        .filter(s -> name.equals(getLastItem(s.getSymbolName().split("\\."))))
                        .findFirst();
            } else {
                String[] moduleComps = name.substring(0, pkgPrefix).split("/");
                String alias = moduleComps[1].split(" ")[0];

                Optional<SymbolInfo> moduleSymbol = visibleSymbols.stream()
                        .filter(s -> s.getSymbolName().equals(alias))
                        .findFirst();

                visibleSymbols = moduleSymbol.get().getScopeEntry().symbol.scope.entries.entrySet().stream()
                        .map(e -> new SymbolInfo(e.getKey().value, e.getValue()))
                        .collect(Collectors.toList());

                searchSymbol = visibleSymbols.stream()
                        .filter(s -> name.substring(pkgPrefix + 1).equals(getLastItem(s.getSymbolName().split("\\."))))
                        .findFirst();
            }
            // If search symbol not found, return
            if (!searchSymbol.isPresent()) {
                break;
            }
            // The `searchSymbol` found, resolve further
            boolean isInvocation = searchSymbol.get().getScopeEntry().symbol instanceof BInvokableSymbol;
            boolean isObject = searchSymbol.get().getScopeEntry().symbol instanceof BObjectTypeSymbol;
            boolean isVariable = searchSymbol.get().getScopeEntry().symbol instanceof BVarSymbol;
            boolean hasNextNameComp = index + 1 < nameComps.length;
            if (isInvocation && hasNextNameComp) {
                BInvokableSymbol invokableSymbol = (BInvokableSymbol) searchSymbol.get().getScopeEntry().symbol;
                BTypeSymbol returnTypeSymbol = invokableSymbol.getReturnType().tsymbol;
                if (returnTypeSymbol instanceof BObjectTypeSymbol) {
                    BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) returnTypeSymbol;
                    visibleSymbols = objectTypeSymbol.methodScope.entries.entrySet().stream()
                            .map(e -> new SymbolInfo(e.getKey().value, e.getValue()))
                            .collect(Collectors.toList());
                }
            } else if (isObject && hasNextNameComp) {
                BObjectTypeSymbol bObjectTypeSymbol = (BObjectTypeSymbol) searchSymbol.get().getScopeEntry().symbol;
                BTypeSymbol typeSymbol = bObjectTypeSymbol.type.tsymbol;
                if (typeSymbol instanceof BObjectTypeSymbol) {
                    BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) typeSymbol;
                    visibleSymbols = objectTypeSymbol.methodScope.entries.entrySet().stream()
                            .map(e -> new SymbolInfo(e.getKey().value, e.getValue()))
                            .collect(Collectors.toList());
                }
            } else if (isVariable && hasNextNameComp) {
                BVarSymbol bVarSymbol = (BVarSymbol) searchSymbol.get().getScopeEntry().symbol;
                BTypeSymbol typeSymbol = bVarSymbol.type.tsymbol;
                if (typeSymbol.type instanceof BUnionType) {
                    // Check for optional field accesses.
                    BUnionType bUnionType = (BUnionType) typeSymbol.type;
                    List<BType> memberTypes = new ArrayList<>(bUnionType.getMemberTypes());
                    if (memberTypes.size() == 2) {
                        boolean isLeftNil = memberTypes.get(0) instanceof BNilType;
                        boolean isRightNil = memberTypes.get(1) instanceof BNilType;
                        if (isLeftNil || isRightNil) {
                            // either left or right should be NIL
                            typeSymbol = (isLeftNil) ? memberTypes.get(1).tsymbol : memberTypes.get(0).tsymbol;
                        }
                    }
                }
                if (typeSymbol instanceof BObjectTypeSymbol) {
                    BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) typeSymbol;
                    visibleSymbols = objectTypeSymbol.methodScope.entries.entrySet().stream()
                            .map(e -> new SymbolInfo(e.getKey().value, e.getValue()))
                            .collect(Collectors.toList());
                    visibleSymbols.addAll(objectTypeSymbol.scope.entries.entrySet().stream()
                                                  .map(e -> new SymbolInfo(e.getKey().value, e.getValue()))
                                                  .collect(Collectors.toList()));
                } else if (typeSymbol instanceof BRecordTypeSymbol) {
                    BRecordTypeSymbol bRecordTypeSymbol = (BRecordTypeSymbol) typeSymbol;
                    visibleSymbols = bRecordTypeSymbol.scope.entries.entrySet().stream()
                            .map(e -> new SymbolInfo(e.getKey().value, e.getValue()))
                            .collect(Collectors.toList());
                } else {
                    visibleSymbols = getLangLibScopeEntries(typeSymbol.type, symbolTable, types).entrySet().stream()
                            .map(e -> new SymbolInfo(e.getKey().value, e.getValue()))
                            .collect(Collectors.toList());
                }
            }
            index++;
        }
        return searchSymbol;
    }

    /**
     * Get the signature information for the given Ballerina function.
     *
     * @param bInvokableSymbol                  BLang Invokable symbol
     * @param context                      Lang Server Signature Help Context
     * @return {@link SignatureInformation}     Signature information for the function
     */
    public static SignatureInformation getSignatureInformation(BInvokableSymbol bInvokableSymbol, LSContext context) {
        List<ParameterInformation> parameterInformationList = new ArrayList<>();
        SignatureInformation signatureInformation = new SignatureInformation();
        SignatureInfoModel signatureInfoModel = getSignatureInfoModel(bInvokableSymbol, context);

        // Override label for 'new' constructor
        String label = bInvokableSymbol.name.value;
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

        return signatureInformation;
    }

    private static String getFullyQualifiedName(BLangInvocation bLangInvocation, LSServiceOperationContext context) {
        String result = bLangInvocation.getName().getValue();
        // Add module import prefix, if necessary
        return addPackagePrefix(bLangInvocation.getPackageAlias(), context, result);
    }

    private static String addPackagePrefix(IdentifierNode identifierNode, LSServiceOperationContext context,
                                           String nodeName) {
        String pkgAlias = identifierNode.getValue();
        if (!pkgAlias.isEmpty()) {
            Optional<BLangImportPackage> optImport = CommonUtil.getCurrentFileImports(context).stream()
                    .filter(p -> pkgAlias.equals(p.alias.value))
                    .findFirst();
            nodeName = optImport.map(pkg -> pkg.getQualifiedPackageName() + ":").orElse("") + nodeName;
        }
        return nodeName;
    }

    /**
     * Get the required signature information filled model.
     *
     * @param bInvokableSymbol                  Invokable symbol
     * @param signatureCtx                      Lang Server Signature Help Context
     * @return {@link SignatureInfoModel}       SignatureInfoModel containing signature information
     */
    private static SignatureInfoModel getSignatureInfoModel(BInvokableSymbol bInvokableSymbol, LSContext signatureCtx) {
        Map<String, String> paramToDesc = new HashMap<>();
        SignatureInfoModel signatureInfoModel = new SignatureInfoModel();
        List<ParameterInfoModel> paramModels = new ArrayList<>();
        MarkdownDocAttachment docAttachment = bInvokableSymbol.getMarkdownDocAttachment();
        List<Parameter> parameters = new ArrayList<>();
        // Handle error constructors
        if (bInvokableSymbol.kind == SymbolKind.ERROR_CONSTRUCTOR) {
            docAttachment = bInvokableSymbol.type.tsymbol.markdownDocumentation;
            if (bInvokableSymbol.type instanceof BErrorType) {
                BErrorType bErrorType = (BErrorType) bInvokableSymbol.type;
                boolean isDirectErrorConstructor = bInvokableSymbol.type.tsymbol.kind == null;
                if (isDirectErrorConstructor) {
                    // If it is direct error constructor, `reason` is mandatory
                    BType reasonType = bErrorType.reasonType;
                    parameters.add(new Parameter(" ", reasonType, false, false));
                }
                if (bErrorType.detailType instanceof BRecordType) {
                    BRecordType bRecordType = (BRecordType) bErrorType.detailType;
                    bRecordType.fields.forEach(p -> {
                        BVarSymbol symbol = p.symbol;
                        parameters.add(
                                new Parameter(symbol.name.getValue(), symbol.type, Symbols.isOptional(symbol), false));
                    });
                    BType restFieldType = bRecordType.restFieldType;
                    if (restFieldType != null) {
                        parameters.add(new Parameter(restFieldType.name.getValue(), restFieldType, false, true));
                    }
                }
            }
        }
        // Check for documentations of the function and parameters
        if (docAttachment != null) {
            if (docAttachment.description != null) {
                signatureInfoModel.setSignatureDescription(docAttachment.description.trim(), signatureCtx);
            }
            docAttachment.parameters.forEach(
                    attribute -> paramToDesc.put(attribute.getName(), attribute.getDescription())
            );
        }
        // Add parameters and rest params
        bInvokableSymbol.getParameters().forEach(
                p -> parameters.add(new Parameter(p.name.value, p.type, Symbols.isOptional(p), false))
        );
        BVarSymbol restParam = bInvokableSymbol.restParam;
        if (restParam != null) {
            parameters.add(new Parameter(restParam.name.value, restParam.type, false, true));
        }
        // Create a list of param info models
        parameters.forEach(param -> {
            String name = param.isOptional ? param.name + "?" : param.name;
            String desc = "";
            if (paramToDesc.containsKey(param.name)) {
                desc = paramToDesc.get(param.name);
            }
            String type = CommonUtil.getBTypeName(param.type, signatureCtx, true);
            if (param.isRestArg && !"".equals(type)) {
                // Rest Arg type sometimes appear as array [], sometimes not eg. 'error()'
                if (type.contains("[]")) {
                    type = type.substring(0, type.length() - 2);
                }
                type += "...";
            }
            paramModels.add(new ParameterInfoModel(name, type, desc));
        });
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
     * Prune source if syntax errors exists.
     *
     * @param lsContext {@link LSContext}
     * @throws SourcePruneException when file uri is invalid
     * @throws WorkspaceDocumentException when document read error occurs
     */
    public static void pruneSource(LSContext lsContext) throws SourcePruneException, WorkspaceDocumentException {
        WorkspaceDocumentManager documentManager = lsContext.get(CommonKeys.DOC_MANAGER_KEY);
        String uri = lsContext.get(DocumentServiceKeys.FILE_URI_KEY);
        if (uri == null) {
            throw new SourcePruneException("fileUri cannot be null!");
        }

        Path filePath = Paths.get(URI.create(uri));

        TokenTraverserFactory tokenTraverserFactory = new SignatureTokenTraverserFactory(filePath, documentManager,
                                                                                         SourcePruner.newContext());
        SourcePruner.pruneSource(lsContext, tokenTraverserFactory);

        // Update document manager
        documentManager.setPrunedContent(filePath, tokenTraverserFactory.getTokenStream().getText());
    }

    /**
     * Parameter model to hold the parameter information meta data.
     */
    private static class Parameter {
        private final String name;
        private final BType type;
        private final boolean isRestArg;
        private final boolean isOptional;

        public Parameter(String name, BType type, boolean isOptional, boolean isRestArg) {
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
}

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
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.apache.commons.lang3.StringUtils;
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
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SignatureInformationCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.BufferedReader;
import java.io.StringReader;
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
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.common.utils.CommonUtil.getLastItem;
import static org.ballerinalang.langserver.common.utils.FilterUtils.getLangLibScopeEntries;
import static org.ballerinalang.langserver.util.TokensUtil.searchTokenAtCursor;

/**
 * Utility functions for the signature help.
 */
public class SignatureHelpUtil {
    private static final String COMMA = ",";
    private static final String SEMI_COLON = ";";
    private static final String EQUAL = "=";
    private static final String INIT_SYMBOL = ".__init";
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile(".*(?:\\:\\s)([^\\(\\)]+\\(.*\\))[,]?");
    private static final Pattern EMPTY_FUNCTION_PATTERN = Pattern.compile("^(.*\\(\\))[;]{0,1}$", Pattern.DOTALL);

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
        int paramIndex = 0;
        int cursorTokenIndex = serviceContext.get(SourcePruneKeys.CURSOR_TOKEN_INDEX_KEY);

        // Generate function invocation from the source pruned text
        String funcInvocation = String.join("", getSourcePrunedFunctionInvocation(serviceContext));
        boolean isInsideFuncInvocation = COMMA.equals(funcInvocation);
        Matcher m = EMPTY_FUNCTION_PATTERN.matcher(funcInvocation);
        boolean isEmptyFuncInvocation = funcInvocation.isEmpty(); // matches functions like 'io:println('
        boolean isEmptyFuncInovcPattern = m.matches(); // matches functions like 'io:println()'
        if (isEmptyFuncInovcPattern && funcInvocation.endsWith(SEMI_COLON)) {
            // Remove semi-colon(;) if not exists
            funcInvocation = funcInvocation.substring(0, funcInvocation.lastIndexOf(SEMI_COLON));
        }

        // Visit LHS of the tokens to get function invocation statement
        int rightParenthesisCount = (isInsideFuncInvocation || isEmptyFuncInvocation) ? 1 : 0;
        if (isInsideFuncInvocation || isEmptyFuncInvocation) {
            List<Token> tokens = serviceContext.get(SourcePruneKeys.TOKEN_LIST_KEY);
            List<Token> collected = new ArrayList<>();
            int traverser = cursorTokenIndex;
            while (traverser >= 0) {
                Token token = tokens.get(traverser);
                int type = token.getType();
                boolean leftParenthesisPending = rightParenthesisCount > 0;
                boolean isWhiteSpace = token.getChannel() == 1;
                boolean isSpecialToken = type == BallerinaParser.COLON || type == BallerinaParser.DOT ||
                        type == BallerinaParser.NOT || type == BallerinaParser.NEW || type == BallerinaParser.ASSIGN ||
                        type == BallerinaParser.RARROW || type == BallerinaParser.LARROW;
                if (type == BallerinaParser.RIGHT_PARENTHESIS) {
                    rightParenthesisCount++;
                    collected.add(token);
                    traverser--;
                } else if (type == BallerinaParser.LEFT_PARENTHESIS && leftParenthesisPending) {
                    rightParenthesisCount--;
                    collected.add(token);
                    traverser--;
                } else if (isSpecialToken) {
                    collected.add(token);
                    traverser--;
                } else if (type == BallerinaParser.Identifier && !leftParenthesisPending) {
                    collected.add(token);
                    traverser--;
                } else if (type == BallerinaParser.COMMA) {
                    if (!isEmptyFuncInvocation) {
                        paramIndex++;
                        collected.add(token);
                    }
                    traverser--;
                } else if (leftParenthesisPending) {
                    collected.add(token);
                    traverser--;
                } else if (isWhiteSpace) {
                    collected.add(token);
                    traverser--;
                } else {
                    break;
                }
            }
            paramIndex += StringUtils.countMatches(funcInvocation, ',');
            Collections.reverse(collected);
            List<String> flatTokensText = collected.stream().map(Token::getText).collect(Collectors.toList());
            funcInvocation = String.join("", flatTokensText) + ")";
        }

        // Set parameter index
        serviceContext.put(SignatureKeys.PARAMETER_INDEX, paramIndex);

        // If it is a key-value pair, remove key to avoid ambiguity with package prefix
        Matcher matcher = KEY_VALUE_PATTERN.matcher(funcInvocation);
        if (matcher.find()) {
            funcInvocation = matcher.group(1);
            // Try to derive param count when *not* in a nested invocation
            int leftParenthesisCnt = StringUtils.countMatches(funcInvocation, "(");
            int rightParenthesisCnt = StringUtils.countMatches(funcInvocation, ")");
            if (leftParenthesisCnt == 1 && leftParenthesisCnt == rightParenthesisCnt) {
                int leftPos = funcInvocation.indexOf("(");
                int rightPos = funcInvocation.indexOf(")");
                if (rightPos - leftPos > 1) {
                    String substring = funcInvocation.substring(leftPos, rightPos);
                    paramIndex = StringUtils.countMatches(substring, ",");
                    paramIndex++;
                }
            }
        }

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

    private static List<String> getSourcePrunedFunctionInvocation(LSServiceOperationContext serviceContext) {
        // Collect source-pruned tokens
        List<String> tokenText = new ArrayList<>();

        final int[] pendingRParenthesisCount = new int[]{0};
        Consumer<Integer> tokenAcceptor = type -> {
            if (type == BallerinaParser.RIGHT_PARENTHESIS) {
                pendingRParenthesisCount[0]++;
            } else if (type == BallerinaParser.LEFT_PARENTHESIS) {
                pendingRParenthesisCount[0]--;
            }
        };

        // Visit Left-Hand side tokens
        List<CommonToken> lhsTokens = serviceContext.get(CompletionKeys.LHS_TOKENS_KEY);
        if (lhsTokens != null) {
            for (Token commonToken : lhsTokens) {
                tokenAcceptor.accept(commonToken.getType());
                tokenText.add(commonToken.getText());
            }
        }

        // Visit Right-Hand side tokens
        List<CommonToken> rhsTokens = serviceContext.get(CompletionKeys.RHS_TOKENS_KEY);
        if (rhsTokens != null) {
            // Collect tokens until we find a Whitespace or Newline
            for (Token commonToken : rhsTokens) {
                int tokenType = commonToken.getType();
                if (tokenType == BallerinaParser.WS || tokenType == BallerinaParser.NEW_LINE) {
                    // Break on Whitespaces or New Lines
                    break;
                }
                tokenAcceptor.accept(commonToken.getType());
                tokenText.add(commonToken.getText());
            }
        }

        // Add Missing Parenthesis
        while (pendingRParenthesisCount[0] < 0) {
            tokenText.add(")");
            pendingRParenthesisCount[0]++;
        }
        return tokenText;
    }

    private static Optional<String> parseAndGetFunctionInvocationPath(String subRule, LSServiceOperationContext context)
            throws CompilationFailedException {
        Optional<BLangPackage> bLangPackage = ExtendedLSCompiler.compileContent(subRule, CompilerPhase.CODE_ANALYZE)
                .getBLangPackage();

        if (!bLangPackage.isPresent()) {
            return Optional.empty();
        }

        List<TopLevelNode> topLevelNodes = bLangPackage.get().getCompilationUnits().get(0).getTopLevelNodes();
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
            }
        }

        return resolveSymbolPath(context, evalStatement, "");
    }

    private static Optional<String> resolveSymbolPath(LSServiceOperationContext context,
                                                      BLangNode bLangNode, String path) {
        if (bLangNode instanceof BLangInvocation) {
            BLangInvocation invocationNode = (BLangInvocation) bLangNode;
            String value = getFullyQualifiedName(invocationNode, context);
            return resolveSymbolPath(context, invocationNode.getExpression(),
                                     path.isEmpty() ? value : path + "." + value);
        } else {
            // Extract inner expression of 'check', 'checkpanic', 'trap' ...etc
            BLangExpression innerExpression = getInnerExpression(bLangNode);
            if (innerExpression != null) {
                return resolveSymbolPath(context, innerExpression, path);
            }
        }
        return Optional.of(path);
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
                if (typeSymbol instanceof BObjectTypeSymbol) {
                    BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) typeSymbol;
                    visibleSymbols = objectTypeSymbol.methodScope.entries.entrySet().stream()
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
        // Join the function parameters to generate the function's signature
        String paramsJoined = signatureInfoModel.getParameterInfoModels().stream().map(parameterInfoModel -> {
            // For each of the parameters, create a parameter info instance
            parameterInformationList.add(getParameterInformation(parameterInfoModel));

            return parameterInfoModel.toString();
        }).collect(Collectors.joining(", "));

        // Override label for 'new' constructor
        String label = bInvokableSymbol.name.value;
        int initIndex = label.indexOf(INIT_SYMBOL);
        if (initIndex > -1) {
            label = "new " + label.substring(0, initIndex);
        }

        signatureInformation.setLabel(label + "(" + paramsJoined + ")");
        signatureInformation.setParameters(parameterInformationList);
        signatureInformation.setDocumentation(signatureInfoModel.signatureDescription);

        return signatureInformation;
    }

    private static String getFullyQualifiedName(BLangInvocation bLangInvocation, LSServiceOperationContext context) {
        String result = bLangInvocation.getName().getValue();
        // If there's a object ref
        if (bLangInvocation.getExpression() instanceof BLangSimpleVarRef) {
            result = ((BLangSimpleVarRef) bLangInvocation.getExpression()).variableName + "." + result;
        }

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
            nodeName = (optImport.isPresent() ? optImport.get().getQualifiedPackageName() : pkgAlias) + ":" + nodeName;
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
        Map<String, String> paramDescMap = new HashMap<>();
        SignatureInfoModel signatureInfoModel = new SignatureInfoModel();
        List<ParameterInfoModel> paramModels = new ArrayList<>();
        MarkdownDocAttachment docAttachment = bInvokableSymbol.getMarkdownDocAttachment();

        if (docAttachment.description != null) {
            signatureInfoModel.setSignatureDescription(docAttachment.description.trim(), signatureCtx);
        }
        docAttachment.parameters.forEach(attribute ->
                paramDescMap.put(attribute.getName(), attribute.getDescription()));

        List<BVarSymbol> parameters = bInvokableSymbol.getParameters();
        if (bInvokableSymbol.restParam != null) {
            parameters.add(bInvokableSymbol.restParam);
        }

        parameters.forEach(bVarSymbol -> {
            ParameterInfoModel parameterInfoModel = new ParameterInfoModel();
            parameterInfoModel.setParamType(bVarSymbol.getType().toString());
            parameterInfoModel.setParamValue(bVarSymbol.getName().getValue());
            if (paramDescMap.containsKey(bVarSymbol.getName().getValue())) {
                parameterInfoModel.setDescription(paramDescMap.get(bVarSymbol.getName().getValue()));
            }
            paramModels.add(parameterInfoModel);
        });

        signatureInfoModel.setParameterInfoModels(paramModels);

        return signatureInfoModel;
    }

    private static ParameterInformation getParameterInformation(ParameterInfoModel parameterInfoModel) {
        MarkupContent paramDocumentation = new MarkupContent();
        paramDocumentation.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        paramDocumentation.setValue(parameterInfoModel.description);

        return new ParameterInformation(parameterInfoModel.paramValue, paramDocumentation);
    }

    public static void preprocessSourcePrune(LSContext lsContext) throws WorkspaceDocumentException {
        Position cursorPosition = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        WorkspaceDocumentManager documentManager = lsContext.get(CommonKeys.DOC_MANAGER_KEY);
        String uri = lsContext.get(DocumentServiceKeys.FILE_URI_KEY);

        if (cursorPosition == null || documentManager == null || uri == null) {
            throw new WorkspaceDocumentException("Cursor position, docManager and fileUri cannot be null!");
        }

        // Read file content
        Path path = Paths.get(URI.create(uri));
        String documentContent = documentManager.getFileContent(path);

        // Execute Ballerina Parser
        BallerinaParser parser = CommonUtil.prepareParser(documentContent);
        parser.removeErrorListeners();
        parser.compilationUnit();

        // Process tokens
        TokenStream tokenStream = parser.getTokenStream();
        List<Token> tokenList = new ArrayList<>(((CommonTokenStream) tokenStream).getTokens());
        Optional<Token> tokenAtCursor = searchTokenAtCursor(tokenList, cursorPosition.getLine(),
                                                                         cursorPosition.getCharacter(), false);

        if (!tokenAtCursor.isPresent()) {
            return;
        }

        Token token = tokenAtCursor.get();

        // Check cursor token is a Left Parenthesis
        if (token.getType() != BallerinaParser.LEFT_PARENTHESIS) {
            return;
        }

        int currentTokenIndex = tokenList.indexOf(token);
        int pendingRightParenthesis = 0;

        int traversor = currentTokenIndex;

        // Traverse LHS
        while (traversor > -1) {
            switch (tokenList.get(traversor).getType()) {
                case BallerinaParser.LEFT_BRACE:
                    traversor = 0;
                    break;
                case BallerinaParser.RIGHT_PARENTHESIS:
                    pendingRightParenthesis--;
                    break;
                case BallerinaParser.LEFT_PARENTHESIS:
                    pendingRightParenthesis++;
                    break;
            }
            traversor--;
        }

        // Reset to cursor position
        traversor = currentTokenIndex + 1;

        // Traverse RHS
        while (traversor < tokenList.size()) {
            switch (tokenList.get(traversor).getType()) {
                case BallerinaParser.RIGHT_BRACE:
                    traversor = tokenList.size();
                    break;
                case BallerinaParser.RIGHT_PARENTHESIS:
                    pendingRightParenthesis--;
                    break;
                case BallerinaParser.LEFT_PARENTHESIS:
                    pendingRightParenthesis++;
                    break;
            }
            traversor++;
        }

        // Add Missing Right Parenthesis
        if (pendingRightParenthesis == 1) {
            List<String> lines = new BufferedReader(new StringReader(documentContent)).lines().collect(
                    Collectors.toList());

            int lhsLen = 0;
            for (int i = 0; i < lines.size(); i++) {
                if (i == token.getLine() - 1) {
                    break;
                }
                lhsLen += lines.get(i).length() + System.lineSeparator().length();
            }
            lhsLen += token.getCharPositionInLine() + 1;

            // Calculate suffix
            Token nextToken = tokenList.get(currentTokenIndex + 1);
            String insertion = ")";
//            if (nextToken.getType() == BallerinaParser.NEW_LINE) {
//                insertion = ");";
//            }

            // Set new content
            String newContent = documentContent.substring(0, lhsLen) + insertion + documentContent.substring(lhsLen);
            documentManager.setPrunedContent(path, newContent);
        }
    }

    /**
     * Parameter information model to hold the parameter information meta data.
     */
    private static class ParameterInfoModel {

        private String paramValue;

        private String paramType;

        private String description;

        void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

        void setParamType(String paramType) {
            this.paramType = paramType;
        }

        void setDescription(String description) {
            this.description = description;
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
                signatureMarkupContent.setValue(signatureDescription);
                this.signatureDescription = Either.forRight(signatureMarkupContent);
            } else {
                this.signatureDescription = Either.forLeft(signatureDescription);
            }
        }
    }
}

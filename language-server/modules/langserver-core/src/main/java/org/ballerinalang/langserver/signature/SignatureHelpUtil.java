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

import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SignatureInformationCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
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
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
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
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.StringJoiner;
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
    private static final String INIT_SYMBOL = ".init";

    private SignatureHelpUtil() {
    }

    /**
     * Capture the callable item information such as name, package of the item, delimiter (. or :), and etc.
     *
     * @param signatureNode  signature node
     * @param serviceContext Text Document service context instance for the signature help operation
     * @return invocation symbol path
     * @throws CompilationFailedException when compilation fails
     */
    public static Optional<String> getInvocationSymbolPath(NonTerminalNode signatureNode, LSContext serviceContext)
            throws CompilationFailedException {
        boolean isImplicitNew = signatureNode.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION;
        boolean isErrorConstructor = signatureNode.kind() == SyntaxKind.FUNCTION_CALL &&
                "error".equals(((FunctionCallExpressionNode) signatureNode).functionName().toString());

        String funcInvocation = (isImplicitNew || isErrorConstructor) ?
                signatureNode.parent().toSourceCode() :
                signatureNode.toSourceCode();

        // Remove last ;\n
        if (funcInvocation.endsWith(";" + CommonUtil.LINE_SEPARATOR)) {
            funcInvocation = funcInvocation.substring(0, funcInvocation.lastIndexOf(";" + CommonUtil.LINE_SEPARATOR));
        }

        // Get right hand side of the assignment
        int equalSymbolIndex = funcInvocation.lastIndexOf(EQUAL);
        String funcInvocationStatement = funcInvocation;
        if (equalSymbolIndex > -1) {
            funcInvocation = funcInvocation.substring(equalSymbolIndex + 1);
        }

        // Get function name using Subrule parser
        String subRuleFormat = "function rightHandExpr() {\n\t%s;\n}\n".replaceAll("\\n", CommonUtil.LINE_SEPARATOR);
        subRuleFormat += "function wholeStatement() {\n\t%s;\n}\n".replaceAll("\\n", CommonUtil.LINE_SEPARATOR);
        String subRule = String.format(subRuleFormat, funcInvocation, funcInvocationStatement);

        return parseAndGetFunctionInvocationPath(subRule, serviceContext);
    }

    private static Optional<String> parseAndGetFunctionInvocationPath(String subRule, LSContext context)
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
        BLangStatement evalStatement = ((BLangBlockFunctionBody) ((BLangFunction) topLevelNodes.get(0)).body)
                .stmts.get(0);

        // Handle object new constructor
        if (evalStatement instanceof BLangExpressionStmt
                && ((BLangExpressionStmt) evalStatement).expr instanceof BLangTypeInit && topLevelNodes.size() >= 2) {
            BLangStatement stmt = ((BLangBlockFunctionBody) ((BLangFunction) topLevelNodes.get(1)).body)
                    .stmts.get(0);
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
                if (bLangTypeInit.type.tag != TypeTags.STREAM) {
                    BLangUserDefinedType type = (BLangUserDefinedType) bLangTypeInit.userDefinedType;
                    return Optional.of(addPackagePrefix(type.pkgAlias, context, type.typeName + INIT_SYMBOL));
                }
            }
        }

        return resolveSymbolPath(context, evalStatement, new Stack<>());
    }

    private static Optional<String> resolveSymbolPath(LSContext context, BLangNode bLangNode, Stack<String> path) {
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

    public static Optional<Scope.ScopeEntry> getFuncScopeEntry(LSContext context, String pathStr,
                                                               List<Scope.ScopeEntry> visibleSymbols) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        Types types = Types.getInstance(compilerContext);
        Scope.ScopeEntry searchSymbol = null;
        // Resolve package prefixes first
        int pkgPrefix = pathStr.indexOf(":");
        if (pkgPrefix > -1) {
            String packagePart = pathStr.substring(0, pkgPrefix);
            pathStr = pathStr.substring(pkgPrefix + 1);
            String alias = packagePart.split("\\sversion\\s")[0].split("\\sas\\s")[0].split("/")[1];
            Optional<Scope.ScopeEntry> moduleSymbol = visibleSymbols.stream()
                    .filter(entry -> entry.symbol.name.getValue().equals(alias))
                    .findFirst();
            visibleSymbols = new ArrayList<>(moduleSymbol.get().symbol.scope.entries.values());
        }
        // Resolve rest of the path
        String[] nameComps = pathStr.split("\\.");
        int index = 0;
        while (index < nameComps.length) {
            String name = nameComps[index];
            searchSymbol = visibleSymbols.stream()
                    .filter(symbol -> name.equals(getLastItem(symbol.symbol.name.getValue().split("\\."))))
                    .findFirst().orElse(null);
            // If search symbol not found, return
            if (searchSymbol == null) {
                break;
            }
            // The `searchSymbol` found, resolve further
            boolean isInvocation = searchSymbol.symbol instanceof BInvokableSymbol;
            boolean isObject = searchSymbol.symbol instanceof BObjectTypeSymbol;
            boolean isVariable = searchSymbol.symbol instanceof BVarSymbol;
            boolean hasNextNameComp = index + 1 < nameComps.length;
            if (isInvocation && hasNextNameComp) {
                BInvokableSymbol invokableSymbol = (BInvokableSymbol) searchSymbol.symbol;
                BTypeSymbol returnTypeSymbol = invokableSymbol.getReturnType().tsymbol;
                if (returnTypeSymbol instanceof BObjectTypeSymbol) {
                    BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) returnTypeSymbol;
                    visibleSymbols = new ArrayList<>(objectTypeSymbol.methodScope.entries.values());
                }
            } else if (isObject && hasNextNameComp) {
                BObjectTypeSymbol bObjectTypeSymbol = (BObjectTypeSymbol) searchSymbol.symbol;
                BTypeSymbol typeSymbol = bObjectTypeSymbol.type.tsymbol;
                if (typeSymbol instanceof BObjectTypeSymbol) {
                    BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) typeSymbol;
                    visibleSymbols = new ArrayList<>(objectTypeSymbol.methodScope.entries.values());
                }
            } else if (isVariable && hasNextNameComp) {
                BVarSymbol bVarSymbol = (BVarSymbol) searchSymbol.symbol;
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
                    visibleSymbols = new ArrayList<>(objectTypeSymbol.methodScope.entries.values());
                    visibleSymbols.addAll(objectTypeSymbol.scope.entries.values());
                } else if (typeSymbol instanceof BRecordTypeSymbol) {
                    BRecordTypeSymbol bRecordTypeSymbol = (BRecordTypeSymbol) typeSymbol;
                    visibleSymbols = new ArrayList<>(bRecordTypeSymbol.scope.entries.values());
                } else {
                    visibleSymbols = getLangLibScopeEntries(typeSymbol.type, symbolTable, types);
                }
            }
            index++;
        }
        return searchSymbol != null ? Optional.of(searchSymbol) : Optional.empty();
    }

    /**
     * Get the signature information for the given Ballerina function.
     *
     * @param bInvokableSymbol BLang Invokable symbol
     * @param isMethodCall
     * @param context          Lang Server Signature Help Context
     * @return {@link SignatureInformation}     Signature information for the function
     */
    public static SignatureInformation getSignatureInformation(BInvokableSymbol bInvokableSymbol,
                                                               boolean isMethodCall, LSContext context) {
        List<ParameterInformation> parameterInformationList = new ArrayList<>();
        SignatureInformation signatureInformation = new SignatureInformation();
        SignatureInfoModel signatureInfoModel = getSignatureInfoModel(bInvokableSymbol, isMethodCall, context);

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

    private static String getFullyQualifiedName(BLangInvocation bLangInvocation, LSContext context) {
        String result = bLangInvocation.getName().getValue();
        // Add module import prefix, if necessary
        return addPackagePrefix(bLangInvocation.getPackageAlias(), context, result);
    }

    private static String addPackagePrefix(IdentifierNode identifierNode, LSContext context, String nodeName) {
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
     * @param bInvokableSymbol Invokable symbol
     * @param isMethodCall     Whether invoked on an object
     * @param signatureCtx     Lang Server Signature Help Context
     * @return {@link SignatureInfoModel}       SignatureInfoModel containing signature information
     */
    private static SignatureInfoModel getSignatureInfoModel(BInvokableSymbol bInvokableSymbol, boolean isMethodCall,
                                                            LSContext signatureCtx) {
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
                }
                // todo: need to support error detail map case
                if (bErrorType.detailType instanceof BRecordType) {
                    BRecordType bRecordType = (BRecordType) bErrorType.detailType;
                    bRecordType.fields.values().forEach(p -> {
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
        boolean skipFirstParam = isMethodCall && CommonUtil.isLangLibSymbol(bInvokableSymbol);
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
            String type = CommonUtil.getBTypeName(param.type, signatureCtx, true);
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

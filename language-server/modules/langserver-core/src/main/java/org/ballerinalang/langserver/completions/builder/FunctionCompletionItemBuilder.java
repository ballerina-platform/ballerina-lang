/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is being used to build function type completion item.
 *
 * @since 0.983.0
 */
public final class FunctionCompletionItemBuilder {

    private FunctionCompletionItemBuilder() {
    }

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(FunctionSymbol functionSymbol, BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        setMeta(item, functionSymbol, context);
        if (functionSymbol != null && functionSymbol.getName().isPresent()) {
            // Override function signature
            String funcName = functionSymbol.getName().get();
            Pair<String, String> functionSignature = getFunctionInvocationSignature(functionSymbol, funcName, context);
            item.setInsertText(functionSignature.getLeft());
            item.setLabel(functionSignature.getRight());
            item.setFilterText(funcName);
        }
        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem buildFunctionPointer(FunctionSymbol functionSymbol,
                                                      BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        setMeta(item, functionSymbol, context);
        if (functionSymbol != null) {
            // Override function signature
            String funcName = functionSymbol.getName().get();
            item.setInsertText(funcName);
            item.setLabel(funcName);
            item.setFilterText(funcName);
            item.setKind(CompletionItemKind.Variable);
        }
        return item;
    }

    public static CompletionItem build(ClassSymbol typeDesc, InitializerBuildMode mode,
                                       BallerinaCompletionContext ctx) {
        MethodSymbol initMethod = null;
        if (typeDesc.initMethod().isPresent()) {
            initMethod = typeDesc.initMethod().get();
        }
        CompletionItem item = new CompletionItem();
        setMeta(item, initMethod, ctx);
        String functionName;
        if (mode == InitializerBuildMode.EXPLICIT) {
            functionName = getQualifiedFunctionName(typeDesc.getName().get(), ctx, initMethod);
        } else {
            functionName = "new";
        }
        Pair<String, String> functionSignature = getFunctionInvocationSignature(initMethod,
                functionName, ctx);
        item.setInsertText(functionSignature.getLeft());
        item.setLabel(functionSignature.getRight());

        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem buildMethod(@Nonnull FunctionSymbol functionSymbol,
                                             BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        setMeta(item, functionSymbol, context);
        String funcName = functionSymbol.getName().get();
        Pair<String, String> functionSignature = getFunctionInvocationSignature(functionSymbol, funcName, context);
        item.setInsertText("self." + functionSignature.getLeft());
        item.setLabel("self." + functionSignature.getRight());
        item.setFilterText("self." + funcName);
        
        return item;
    }

    private static void setMeta(CompletionItem item, FunctionSymbol functionSymbol, BallerinaCompletionContext ctx) {
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setKind(CompletionItemKind.Function);
        if (functionSymbol != null) {
            FunctionTypeSymbol functionTypeDesc = functionSymbol.typeDescriptor();
            Optional<TypeSymbol> typeSymbol = functionTypeDesc.returnTypeDescriptor();
            typeSymbol.ifPresent(symbol -> item.setDetail(CommonUtil.getModifiedTypeName(ctx, symbol)));
            List<String> funcArguments = getFuncArguments(functionSymbol, ctx);
            if (!funcArguments.isEmpty()) {
                Command cmd = new Command("editor.action.triggerParameterHints", "editor.action.triggerParameterHints");
                item.setCommand(cmd);
            }
            boolean skipFirstParam = skipFirstParam(ctx, functionSymbol);
            if (functionSymbol.documentation().isPresent()) {
                item.setDocumentation(getDocumentation(functionSymbol, skipFirstParam, ctx));
            }
        }
    }

    private static Either<String, MarkupContent> getDocumentation(FunctionSymbol functionSymbol,
                                                                  boolean skipFirstParam,
                                                                  BallerinaCompletionContext ctx) {
        FunctionTypeSymbol functionTypeDesc = functionSymbol.typeDescriptor();

        Optional<Documentation> docAttachment = functionSymbol.documentation();
        String description = docAttachment.isEmpty() || docAttachment.get().description().isEmpty()
                ? "" : docAttachment.get().description().get();
        Map<String, String> docParamsMap = new HashMap<>();
        docAttachment.ifPresent(documentation -> documentation.parameterMap().forEach(docParamsMap::put));

        List<ParameterSymbol> functionParameters = new ArrayList<>();
        List<ParameterSymbol> defaultParams = new ArrayList<>();

        if (functionTypeDesc.params().isPresent()) {
            functionParameters.addAll(functionTypeDesc.params().get());
            defaultParams.addAll(functionParameters.stream()
                    .filter(parameter -> parameter.paramKind() == ParameterKind.DEFAULTABLE)
                    .collect(Collectors.toList()));
        }

        MarkupContent docMarkupContent = new MarkupContent();
        docMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        StringBuilder documentation = new StringBuilder();
        if (functionSymbol.getModule().isPresent()) {
            String moduleId = functionSymbol.getModule().get().id().toString();
            documentation.append("**Package:** _")
                    .append(moduleId).append("_")
                    .append(CommonUtil.MD_LINE_SEPARATOR)
                    .append(CommonUtil.MD_LINE_SEPARATOR);
        }
        documentation.append(description).append(CommonUtil.MD_LINE_SEPARATOR);

        StringJoiner joiner = new StringJoiner(CommonUtil.MD_LINE_SEPARATOR);

        if (functionTypeDesc.restParam().isPresent()) {
            functionParameters.add(functionTypeDesc.restParam().get());
        }
        for (int i = 0; i < functionParameters.size(); i++) {
            ParameterSymbol param = functionParameters.get(i);
            String paramType = CommonUtil.getModifiedTypeName(ctx, param.typeDescriptor());
            if (i == 0 && skipFirstParam) {
                continue;
            }

            Optional<ParameterSymbol> defaultVal = defaultParams.stream()
                    .filter(parameter -> parameter.getName().isPresent() && param.getName().isPresent()
                            && parameter.getName().get().equals(param.getName().get()))
                    .findFirst();
            StringBuilder paramDescription = new StringBuilder("- " + "`" + paramType + "`");
            param.getName().ifPresent(name -> {
                paramDescription.append(" ").append(name);
                if (docParamsMap.containsKey(name)) {
                    paramDescription.append(": ").append(docParamsMap.get(name));
                }
            });
            if (defaultVal.isPresent()) {
                joiner.add(paramDescription + "(Defaultable)");
            } else {
                joiner.add(paramDescription);
            }
        }
        String paramsStr = joiner.toString();
        if (!paramsStr.isEmpty()) {
            documentation.append("**Params**").append(CommonUtil.MD_LINE_SEPARATOR).append(paramsStr);
        }
        if (functionTypeDesc.returnTypeDescriptor().isPresent()
                && functionTypeDesc.returnTypeDescriptor().get().typeKind() != TypeDescKind.NIL) {
            // Sets the return type description only if the return type descriptor is not NIL type
            String desc = "";
            if (docAttachment.isPresent() && docAttachment.get().returnDescription().isPresent()
                    && !docAttachment.get().returnDescription().get().isEmpty()) {
                desc = "- " + CommonUtil.MD_NEW_LINE_PATTERN.matcher(docAttachment.get().returnDescription().get())
                        .replaceAll(CommonUtil.MD_LINE_SEPARATOR) + CommonUtil.MD_LINE_SEPARATOR;
            }
            documentation.append(CommonUtil.MD_LINE_SEPARATOR).append(CommonUtil.MD_LINE_SEPARATOR)
                    .append("**Return**").append(" `")
                    .append(CommonUtil.getModifiedTypeName(ctx, functionTypeDesc.returnTypeDescriptor().get()))
                    .append("` ").append(CommonUtil.MD_LINE_SEPARATOR).append(desc)
                    .append(CommonUtil.MD_LINE_SEPARATOR);
        }
        docMarkupContent.setValue(documentation.toString());

        return Either.forRight(docMarkupContent);
    }

    /**
     * Get the function invocation signature.
     *
     * @param functionSymbol ballerina function instance
     * @param functionName   function name
     * @param ctx            Language Server Operation context
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    private static Pair<String, String> getFunctionInvocationSignature(FunctionSymbol functionSymbol,
                                                                       String functionName,
                                                                       BallerinaCompletionContext ctx) {
        if (functionSymbol == null) {
            return ImmutablePair.of(functionName + "()", functionName + "()");
        }
        StringBuilder signature = new StringBuilder(functionName + "(");
        StringBuilder insertText = new StringBuilder(functionName + "(");
        List<String> funcArguments = getFuncArguments(functionSymbol, ctx);
        if (!funcArguments.isEmpty()) {
            signature.append(String.join(", ", funcArguments));
            insertText.append("${1}");
        }
        signature.append(")");
        insertText.append(")");

        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    private static String getQualifiedFunctionName(String functionName, BallerinaCompletionContext ctx,
                                                   @Nullable FunctionSymbol functionSymbol) {
        if (functionSymbol == null) {
            return functionName;
        }
        boolean onQNameRef = QNameReferenceUtil.onQualifiedNameIdentifier(ctx, ctx.getNodeAtCursor());
        Optional<ModuleSymbol> module = functionSymbol.getModule();
        if (module.isEmpty() || onQNameRef || functionName.equals(SyntaxKind.NEW_KEYWORD.stringValue())) {
            return functionName;
        }
        ModuleID moduleID = module.get().id();
        String modulePrefix = CommonUtil.getModulePrefix(ctx, moduleID.orgName(), moduleID.moduleName());

        if (modulePrefix.isEmpty()) {
            return functionName;
        }

        return modulePrefix + SyntaxKind.COLON_TOKEN.stringValue() + functionName;
    }

    /**
     * Get the list of function arguments from the invokable symbol.
     *
     * @param symbol Invokable symbol to extract the arguments
     * @param ctx    Lang Server Operation context
     * @return {@link List} List of arguments
     */
    private static List<String> getFuncArguments(FunctionSymbol symbol, BallerinaCompletionContext ctx) {
        List<String> args = new ArrayList<>();
        boolean skipFirstParam = skipFirstParam(ctx, symbol);
        FunctionTypeSymbol functionTypeDesc = symbol.typeDescriptor();
        Optional<ParameterSymbol> restParam = functionTypeDesc.restParam();
        List<ParameterSymbol> parameterDefs = new ArrayList<>();
        if (functionTypeDesc.params().isPresent()) {
            parameterDefs.addAll(functionTypeDesc.params().get());
        }
        for (int i = 0; i < parameterDefs.size(); i++) {
            if (i == 0 && skipFirstParam) {
                continue;
            }
            ParameterSymbol param = parameterDefs.get(i);
            if (param.typeDescriptor().typeKind() == TypeDescKind.COMPILATION_ERROR) {
                // Invalid parameters are ignored, but empty string is used to indicate there's a parameter
                args.add("");
            } else {
                args.add(CommonUtil.getModifiedTypeName(ctx, param.typeDescriptor()) + (param.getName().isEmpty() ? ""
                        : " " + param.getName().get()));
            }
        }
        restParam.ifPresent(param -> {
            // Rest param is represented as an array type symbol
            ArrayTypeSymbol typeSymbol = (ArrayTypeSymbol) param.typeDescriptor();
            args.add(CommonUtil.getModifiedTypeName(ctx, typeSymbol.memberTypeDescriptor())
                    + (param.getName().isEmpty() ? "" : "... "
                    + param.getName().get()));
        });
        return (!args.isEmpty()) ? args : new ArrayList<>();
    }

    /**
     * Whether we skip the first parameter being included as a label in the signature.
     * When showing a lang lib invokable symbol over DOT(invocation) we do not show the first param, but when we
     * showing the invocation over package of the langlib with the COLON we show the first param.
     * 
     * When the langlib function is retrieved from the Semantic API, those functions are filtered where the first param
     * type not being same as the langlib type. Hence we need to chek whether the function is from a langlib.
     *
     * @param context        context
     * @param functionSymbol invokable symbol
     * @return {@link Boolean} whether we show the first param or not
     */
    private static boolean skipFirstParam(BallerinaCompletionContext context, FunctionSymbol functionSymbol) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        return CommonUtil.isLangLib(functionSymbol.getModule().get().id())
                && nodeAtCursor.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE;
    }

    /**
     * Build mode, either explicit or implicit initializer.
     *
     * @since 2.0.0
     */
    public enum InitializerBuildMode {
        EXPLICIT,
        IMPLICIT
    }
}

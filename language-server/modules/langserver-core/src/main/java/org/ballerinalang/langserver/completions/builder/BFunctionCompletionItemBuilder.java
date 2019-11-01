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

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * This class is being used to build function type completion item.
 *
 * @since 0.983.0
 */
public final class BFunctionCompletionItemBuilder {
    private BFunctionCompletionItemBuilder() {
    }

    /**
     * Creates and returns a completion item.
     *
     * @param bSymbol    BSymbol or null
     * @param label      label
     * @param insertText insert text
     * @param context {@link LSContext}
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(BInvokableSymbol bSymbol, String label, String insertText, LSContext context) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        setMeta(item, bSymbol, context);
        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param bSymbol BSymbol
     * @param context LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(BInvokableSymbol bSymbol, LSContext context) {
        CompletionItem item = new CompletionItem();
        setMeta(item, bSymbol, context);
        if (bSymbol != null) {
            // Override function signature
            String functionName = CommonUtil.getFunctionNameFromSymbol(bSymbol);
            Pair<String, String> functionSignature = CommonUtil.getFunctionInvocationSignature(bSymbol,
                    functionName, context);
            item.setInsertText(functionSignature.getLeft());
            item.setLabel(functionSignature.getRight());
        }
        return item;
    }

    private static void setMeta(CompletionItem item, BInvokableSymbol bSymbol, LSContext ctx) {
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        item.setKind(CompletionItemKind.Function);
        if (bSymbol != null) {
            List<String> funcArguments = FunctionGenerator.getFuncArguments(bSymbol, ctx);
            if (!funcArguments.isEmpty()) {
                Command cmd = new Command("editor.action.triggerParameterHints", "editor.action.triggerParameterHints");
                item.setCommand(cmd);
            }
            int invocationType = (ctx == null || ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY) == null) ? -1
                    : ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
            boolean skipFirstParam = CommonUtil.skipFirstParam(bSymbol, invocationType);
            if (bSymbol.markdownDocumentation != null) {
                item.setDocumentation(getDocumentation(bSymbol, skipFirstParam, ctx));
            }
        }
    }

    private static Either<String, MarkupContent> getDocumentation(BInvokableSymbol bInvokableSymbol,
                                                                  boolean skipFirstParam, LSContext ctx) {
        String pkgID = bInvokableSymbol.pkgID.toString();

        MarkdownDocAttachment docAttachment = bInvokableSymbol.getMarkdownDocAttachment();
        String description = docAttachment.description == null ? "" : docAttachment.description;
        Map<String, String> docParamsMap = new HashMap<>();
        for (MarkdownDocAttachment.Parameter parameter : docAttachment.parameters) {
            docParamsMap.put(parameter.name, parameter.description);
        }
        List<BVarSymbol> defaultParams = bInvokableSymbol.getParameters().stream()
                .filter(varSymbol -> varSymbol.defaultableParam).collect(Collectors.toList());

        MarkupContent docMarkupContent = new MarkupContent();
        docMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        String documentation = "**Package:** " + "_" + pkgID + "_" + CommonUtil.MD_LINE_SEPARATOR
                + CommonUtil.MD_LINE_SEPARATOR + description + CommonUtil.MD_LINE_SEPARATOR;
        StringJoiner joiner = new StringJoiner(CommonUtil.MD_LINE_SEPARATOR);
        List<BVarSymbol> functionParameters = new ArrayList<>(bInvokableSymbol.params);
        if (bInvokableSymbol.restParam != null) {
            functionParameters.add(bInvokableSymbol.restParam);
        }
        for (int i = 0; i < functionParameters.size(); i++) {
            BVarSymbol paramSymbol = functionParameters.get(i);
            String paramType = CommonUtil.getBTypeName(paramSymbol.type, ctx, false);
            if (i == 0 && skipFirstParam) {
                continue;
            }

            Optional<BVarSymbol> defaultVal = defaultParams.stream()
                    .filter(bVarSymbol -> bVarSymbol.getName().getValue().equals(paramSymbol.name.value))
                    .findFirst();
            String paramDescription = "- " + "`" + paramType + "` " + paramSymbol.getName().getValue();
            if (docParamsMap.containsKey(paramSymbol.name.value)) {
                paramDescription += ": " + docParamsMap.get(paramSymbol.name.value);
            }
            if (defaultVal.isPresent()) {
                joiner.add(paramDescription + "(Defaultable)");
            } else {
                joiner.add(paramDescription);
            }
        }
        String paramsStr = joiner.toString();
        if (!paramsStr.isEmpty()) {
            documentation += "**Params**" + CommonUtil.MD_LINE_SEPARATOR + paramsStr;
        }
        if (!(bInvokableSymbol.retType instanceof BNilType)
                && bInvokableSymbol.retType != null
                && bInvokableSymbol.retType.tsymbol != null) {
            String desc = "";
            if (docAttachment.returnValueDescription != null && !docAttachment.returnValueDescription.isEmpty()) {
                desc = "- " + CommonUtil.MD_NEW_LINE_PATTERN.matcher(docAttachment.returnValueDescription).replaceAll(
                        CommonUtil.MD_LINE_SEPARATOR) + CommonUtil.MD_LINE_SEPARATOR;
            }
            documentation += CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_LINE_SEPARATOR + "**Returns**"
                    + " `" + CommonUtil.getBTypeName(bInvokableSymbol.retType, ctx, false) + "` " +
                    CommonUtil.MD_LINE_SEPARATOR + desc + CommonUtil.MD_LINE_SEPARATOR;
        }
        docMarkupContent.setValue(documentation);

        return Either.forRight(docMarkupContent);
    }
}

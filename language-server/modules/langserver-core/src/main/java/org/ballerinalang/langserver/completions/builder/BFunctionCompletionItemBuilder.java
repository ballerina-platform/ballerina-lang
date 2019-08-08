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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.io.BufferedReader;
import java.io.StringReader;
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
        List<String> funcArguments = FunctionGenerator.getFuncArguments(bSymbol, ctx);
        if (bSymbol != null && !funcArguments.isEmpty()) {
            Command cmd = new Command("editor.action.triggerParameterHints", "editor.action.triggerParameterHints");
            item.setCommand(cmd);
        }
        int invocationType = (ctx == null || ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY) == null) ? -1
                : ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        boolean skipFirstParam = CommonUtil.skipFirstParam(bSymbol, invocationType);
        if (bSymbol != null && bSymbol.markdownDocumentation != null) {
            item.setDocumentation(getDocumentation(bSymbol, skipFirstParam, ctx));
        }
    }

    private static Either<String, MarkupContent> getDocumentation(BInvokableSymbol bInvokableSymbol,
                                                                  boolean skipFirstParam, LSContext ctx) {
        String pkgID = bInvokableSymbol.pkgID.toString();

        MarkdownDocAttachment docAttachment = bInvokableSymbol.getMarkdownDocAttachment();
        String description = docAttachment.description == null ? "" : docAttachment.description;
        List<MarkdownDocAttachment.Parameter> parameters = docAttachment.parameters;
        List<BVarSymbol> defaultParams = bInvokableSymbol.getParameters().stream()
                .filter(varSymbol -> varSymbol.defaultableParam).collect(Collectors.toList());

        Map<String, BType> types = new HashMap<>();
        for (BVarSymbol param : bInvokableSymbol.params) {
            types.put(param.name.value, param.type);
        }

        MarkupContent docMarkupContent = new MarkupContent();
        docMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        String documentation = "**Package:** " + "_" + pkgID + "_" + CommonUtil.MD_LINE_SEPARATOR
                + CommonUtil.MD_LINE_SEPARATOR + description + CommonUtil.MD_LINE_SEPARATOR;
        StringJoiner joiner = new StringJoiner(CommonUtil.MD_LINE_SEPARATOR);
        for (int i = 0; i < parameters.size(); i++) {
            MarkdownDocAttachment.Parameter parameter = parameters.get(i);
            if (i == 0 && skipFirstParam) {
                continue;
            }
            Optional<BVarSymbol> defaultVal = defaultParams.stream()
                    .filter(bVarSymbol -> bVarSymbol.getName().getValue().equals(parameter.getName()))
                    .findFirst();
            String type = (!types.isEmpty() && types.get(parameter.name) != null) ?
                    "`" + CommonUtil.getBTypeName(types.get(parameter.name), ctx) + "` " : "";
            String paramDescription = "- " + type + parameter.getName() + ": " + parameter.getDescription();
            if (defaultVal.isPresent()) {
                joiner.add(paramDescription + "(Defaultable)");
            }
            joiner.add(paramDescription);
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
                BufferedReader reader = new BufferedReader(new StringReader(docAttachment.returnValueDescription));
                desc = "- " + reader.lines().map(String::trim)
                        .collect(Collectors.joining(CommonUtil.MD_LINE_SEPARATOR)) + CommonUtil.MD_LINE_SEPARATOR;
            }
            documentation += CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_LINE_SEPARATOR + "**Returns**"
                    + " `" + CommonUtil.getBTypeName(bInvokableSymbol.retType, ctx) + "` " +
                    CommonUtil.MD_LINE_SEPARATOR + desc + CommonUtil.MD_LINE_SEPARATOR;
        }
        docMarkupContent.setValue(documentation);

        return Either.forRight(docMarkupContent);
    }
}

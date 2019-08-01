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
import org.ballerinalang.langserver.compiler.LSContext;
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

import java.util.List;
import java.util.Optional;
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
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(BInvokableSymbol bSymbol, String label, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        setMeta(item, bSymbol);
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
        setMeta(item, bSymbol);
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

    private static void setMeta(CompletionItem item, BInvokableSymbol bSymbol) {
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        item.setKind(CompletionItemKind.Function);
        item.setCommand(new Command("editor.action.triggerParameterHints", "editor.action.triggerParameterHints"));
        if (bSymbol != null && bSymbol.markdownDocumentation != null) {
            item.setDocumentation(getDocumentation(bSymbol));
        }
    }

    private static Either<String, MarkupContent> getDocumentation(BInvokableSymbol bInvokableSymbol) {
        String pkgID = bInvokableSymbol.pkgID.toString();

        MarkdownDocAttachment markdownDocAttachment = bInvokableSymbol.getMarkdownDocAttachment();
        String description = markdownDocAttachment.description == null ? "" : markdownDocAttachment.description;
        List<MarkdownDocAttachment.Parameter> parameters = markdownDocAttachment.parameters;
        List<BVarSymbol> defaultParams = bInvokableSymbol.getParameters().stream()
                .filter(varSymbol -> varSymbol.defaultableParam).collect(Collectors.toList());

        MarkupContent docMarkupContent = new MarkupContent();

        docMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        String documentation = "**Package:** " + "_" + pkgID + "_" + CommonUtil.MD_LINE_SEPARATOR
                + CommonUtil.MD_LINE_SEPARATOR + description + CommonUtil.MD_LINE_SEPARATOR
                + CommonUtil.MD_LINE_SEPARATOR + "---  " + CommonUtil.MD_LINE_SEPARATOR + "**Parameters**"
                + CommonUtil.MD_LINE_SEPARATOR
                + parameters.stream()
                .map(parameter -> {
                    Optional<BVarSymbol> defaultVal = defaultParams.stream()
                            .filter(bVarSymbol -> bVarSymbol.getName().getValue().equals(parameter.getName()))
                            .findFirst();
                    String paramDescription = "- _" + parameter.getName() + "_" + CommonUtil.MD_LINE_SEPARATOR
                            + "    " + parameter.getDescription() + CommonUtil.MD_LINE_SEPARATOR;
                    if (defaultVal.isPresent()) {
                        return paramDescription + "(Default Parameter)";
                    }
                    return paramDescription;
                })
                .collect(Collectors.joining(CommonUtil.MD_LINE_SEPARATOR));

        if (!(bInvokableSymbol.retType instanceof BNilType)
                && bInvokableSymbol.retType != null
                && bInvokableSymbol.retType.tsymbol != null) {
            documentation = documentation + CommonUtil.MD_LINE_SEPARATOR
                    + CommonUtil.MD_LINE_SEPARATOR + "**Return**" + CommonUtil.MD_LINE_SEPARATOR
                    + bInvokableSymbol.retType.toString();
        }
        docMarkupContent.setValue(documentation);

        return Either.forRight(docMarkupContent);
    }
}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.hover;

import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for Hover functionality of language server.
 */
public class HoverUtil {
    /**
     * Get Hover from documentation attachment.
     *
     * @param docAttachment Documentation attachment
     * @param symbol        hovered symbol
     * @param ctx           LS Context
     * @return {@link Hover}    hover object.
     */
    public static Hover getHoverFromDocAttachment(MarkdownDocAttachment docAttachment, BSymbol symbol, LSContext ctx) {
        MarkupContent hoverMarkupContent = new MarkupContent();
        if (docAttachment == null) {
            Hover hover = new Hover();
            List<Either<String, MarkedString>> contents = new ArrayList<>();
            contents.add(Either.forLeft(""));
            hover.setContents(contents);
            return hover;
        }
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        StringBuilder content = new StringBuilder();
        Map<String, List<MarkdownDocAttachment.Parameter>> filterAttributes =
                filterDocumentationAttributes(docAttachment, symbol);

        if (docAttachment.description != null && !docAttachment.description.isEmpty()) {
            String description =
                    CommonUtil.MD_LINE_SEPARATOR + docAttachment.description.trim() + CommonUtil.MD_LINE_SEPARATOR;
            content.append(getFormattedHoverDocContent(ContextConstants.DESCRIPTION, description));
        }

        if (filterAttributes.get(ContextConstants.DOC_PARAM) != null) {
            String docAttributes = getDocAttributes(filterAttributes.get(ContextConstants.DOC_PARAM), symbol, ctx);
            if (!docAttributes.isEmpty()) {
                content.append(getFormattedHoverDocContent(ContextConstants.PARAM_TITLE, docAttributes));
            }
        }

        if (filterAttributes.get(ContextConstants.DOC_FIELD) != null) {
            String docAttributes = getDocAttributes(filterAttributes.get(ContextConstants.DOC_FIELD), symbol, ctx);
            if (!docAttributes.isEmpty()) {
                content.append(getFormattedHoverDocContent(ContextConstants.FIELD_TITLE, docAttributes));
            }
        }

        if (docAttachment.returnValueDescription != null && !docAttachment.returnValueDescription.isEmpty()) {
            String returnType = "";
            if (symbol instanceof BInvokableSymbol) {
                // Get type information
                BInvokableSymbol invokableSymbol = (BInvokableSymbol) symbol;
                // Fixme in the next phase
//                returnType = " `" + CommonUtil.getBTypeName(invokableSymbol.retType, ctx, false) + "`";
            }
            content.append(getFormattedHoverDocContent(ContextConstants.RETURN_TITLE, returnType,
                    getReturnValueDescription(
                            docAttachment.returnValueDescription)));
        }

        hoverMarkupContent.setValue(content.toString());

        return new Hover(hoverMarkupContent);
    }

    /**
     * Get the default hover object.
     *
     * @return {@link Hover} hover default hover object.
     */
    public static Hover getDefaultHoverObject() {
        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue("");
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    /**
     * Get the markdown doc attachment for the symbol.
     * For the variable symbol direct markdown content is empty, hence consider the type symbol.
     *
     * @param bSymbol BSymbol to evaluate
     * @return {@link MarkdownDocAttachment} Doc Attachment
     */
    public static MarkdownDocAttachment getMarkdownDocForSymbol(BSymbol bSymbol) {
        SymbolKind symbolKind = bSymbol.kind == null ? bSymbol.type.tsymbol.kind : bSymbol.kind;
        if (symbolKind == null) {
            return bSymbol.markdownDocumentation;
        }
        MarkdownDocAttachment markdownDocAttachment = null;

        switch (symbolKind) {
            case RECORD:
            case OBJECT:
                markdownDocAttachment = bSymbol.type.tsymbol.markdownDocumentation;
                break;
            case ANNOTATION:
                markdownDocAttachment = ((BAnnotationSymbol) bSymbol.type.tsymbol).attachedType.markdownDocumentation;
                break;
            case FUNCTION:
                markdownDocAttachment = bSymbol.markdownDocumentation;
                break;
            default:
                break;
        }

        return markdownDocAttachment;
    }

    /**
     * Filter documentation attributes to each tags.
     *
     * @param docAttachment documentation node
     * @return {@link Map}      filtered content map
     */
    private static Map<String, List<MarkdownDocAttachment.Parameter>> filterDocumentationAttributes(
            MarkdownDocAttachment docAttachment, BSymbol symbol) {
        Map<String, List<MarkdownDocAttachment.Parameter>> filteredAttributes = new HashMap<>();
        String paramType = "";
        SymbolKind symbolKind = symbol.kind == null ? symbol.type.tsymbol.kind : symbol.kind;
        if (symbolKind == null) {
            return filteredAttributes;
        }
        switch (symbolKind) {
            case FUNCTION:
                paramType = ContextConstants.DOC_PARAM;
                break;
            case OBJECT:
            case RECORD:
            case TYPE_DEF:
            case ANNOTATION:
                paramType = ContextConstants.DOC_FIELD;
                break;
            default:
                break;
        }

        for (MarkdownDocAttachment.Parameter parameter : docAttachment.parameters) {
            if (filteredAttributes.get(paramType) == null) {
                filteredAttributes.put(paramType, new ArrayList<>());
                filteredAttributes.get(paramType).add(parameter);
            } else {
                filteredAttributes.get(paramType).add(parameter);
            }
        }

        return filteredAttributes;
    }

    /**
     * Get the doc annotation attributes.
     *
     * @param parameters parameters to be extracted
     * @param symbol     symbol
     * @param ctx        LS Context
     * @return {@link String }  extracted content of annotation
     */
    private static String getDocAttributes(List<MarkdownDocAttachment.Parameter> parameters, BSymbol symbol,
                                           LSContext ctx) {
        Map<String, BSymbol> paramSymbols = new HashMap<>();
        if (symbol instanceof BVarSymbol && !(symbol instanceof BInvokableSymbol)) {
            symbol = ((BVarSymbol) symbol).type.tsymbol;
        }
        boolean skipFirstParam = false;
        if (symbol instanceof BInvokableSymbol) {
            // If it is a parameters set of a function invocation
            BInvokableSymbol invokableSymbol = (BInvokableSymbol) symbol;
            List<BVarSymbol> params = invokableSymbol.params;
            skipFirstParam = skipFirstParam(ctx, invokableSymbol);
            for (int i = 0; i < params.size(); i++) {
                if (i == 0 && skipFirstParam) {
                    continue;
                }
                BVarSymbol param = params.get(i);
                paramSymbols.put(param.name.value, param);
            }
        } else if (symbol instanceof BStructureTypeSymbol || symbol instanceof BAnnotationSymbol) {
            Map<Name, Scope.ScopeEntry> entries;
            if (symbol instanceof BStructureTypeSymbol) {
                // If it is a field set of a object or a record
                BStructureTypeSymbol objectTypeSymbol = (BStructureTypeSymbol) symbol;
                entries = objectTypeSymbol.scope.entries;
            } else {
                entries = ((BAnnotationSymbol) symbol).attachedType.scope.entries;
            }
            entries.values()
                    .stream()
                    .filter(s -> s.symbol instanceof BVarSymbol && s.symbol.getFlags().contains(Flag.PUBLIC))
                    .forEach(s -> paramSymbols.put(s.symbol.name.value, s.symbol));
        }
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            if (i == 0 && skipFirstParam) {
                continue;
            }
            MarkdownDocAttachment.Parameter parameter = parameters.get(i);
            boolean isOptional = false;
            String type = "";
            if (!paramSymbols.isEmpty() && paramSymbols.get(parameter.name) != null) {
                isOptional = ((paramSymbols.get(parameter.name).flags & Flags.OPTIONAL) == Flags.OPTIONAL);
                // Fixme in the next phase
//                type = "`" + CommonUtil.getBTypeName(paramSymbols.get(parameter.name).type, ctx, false) + "` ";
            }
            value.append("- ")
                    .append(type).append("**")
                    .append(parameter.name.trim())
                    .append(isOptional ? "?" : "")
                    .append("**")
                    .append(": ")
                    .append(parameter.description.trim()).append(CommonUtil.MD_LINE_SEPARATOR);
        }
        return value.toString();
    }

    private static String getReturnValueDescription(String returnVal) {
        return "- " + CommonUtil.MD_NEW_LINE_PATTERN.matcher(returnVal).replaceAll(CommonUtil.MD_LINE_SEPARATOR) +
                CommonUtil.MD_LINE_SEPARATOR;
    }

    /**
     * get the formatted string with markdowns.
     *
     * @param header header.
     * @return {@link String} formatted string using markdown.
     */
    private static String getFormattedHoverDocContent(String header, String content) {
        return getFormattedHoverDocContent(header, "", content);
    }

    private static String getFormattedHoverDocContent(String header, String subHeader, String content) {
        return "**" + header + "**" + subHeader + CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_NEW_LINE_PATTERN.matcher(
                content)
                .replaceAll(CommonUtil.MD_LINE_SEPARATOR) + CommonUtil.MD_LINE_SEPARATOR;
    }

    private static boolean skipFirstParam(LSContext context, BInvokableSymbol invokableSymbol) {
        // Fixme in the next phase
//        NonTerminalNode evalNode = context.get(CompletionKeys.TOKEN_AT_CURSOR_KEY).parent();
//        return CommonUtil.isLangLibSymbol(invokableSymbol) && evalNode.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE;
        return false;
    }
}

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
package org.ballerinalang.langserver.hover.util;

import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for Hover functionality of language server.
 */
public class HoverUtil {
    /**
     * check whether given position matches the given node's position.
     *
     * @param nodePosition position of the current node.
     * @param textPosition position to be matched.
     * @return {@link Boolean} return true if position are a match else return false.
     */
    public static boolean isMatchingPosition(DiagnosticPos nodePosition, Position textPosition) {
        boolean isCorrectPosition = false;
        if (nodePosition.sLine == textPosition.getLine()
                && nodePosition.eLine >= textPosition.getLine()
                && nodePosition.sCol <= textPosition.getCharacter()
                && nodePosition.eCol >= textPosition.getCharacter()) {
            isCorrectPosition = true;
        }
        return isCorrectPosition;
    }

    /**
     * Get Hover from documentation attachment.
     *
     * @param docAttachment     Documentation attachment
     * @param symbol hovered symbol
     * @param ctx   LS Context
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
                returnType = " `" + CommonUtil.getBTypeName(invokableSymbol.retType, ctx, false) + "`";
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
     * Calculate and returns identifier position of this BLangFunction.
     *
     * @param bLangFunction {@link BLangFunction}
     * @return position
     */
    public static DiagnosticPos getIdentifierPosition(BLangFunction bLangFunction) {
        DiagnosticPos funcPosition = bLangFunction.getPosition();
        DiagnosticPos position = new DiagnosticPos(funcPosition.src, funcPosition.sLine, funcPosition.eLine,
                                                   funcPosition.sCol,
                                                   funcPosition.eCol);
        Set<Whitespace> wsSet = bLangFunction.getWS();
        if (wsSet != null && wsSet.size() > 4) {
            Whitespace[] wsArray = new Whitespace[wsSet.size()];
            wsSet.toArray(wsArray);
            Arrays.sort(wsArray);
            int lengthToNameStart = 0;
            if (wsArray[0].getPrevious().equals(CommonKeys.RESOURCE_KEYWORD_KEY)) {
                lengthToNameStart += wsArray[0].getPrevious().length()
                        + wsArray[1].getPrevious().length() + wsArray[1].getWs().length() + wsArray[2].getWs().length();
            } else {
                lengthToNameStart += wsArray[0].getPrevious().length() + wsArray[1].getWs().length();
                if (wsArray[2].getPrevious().equals("::")) {
                    lengthToNameStart += wsArray[1].getPrevious().length() + "::".length();
                }
            }
            position.sCol += lengthToNameStart;
            position.eCol = position.sCol + bLangFunction.name.value.length();
            if (!bLangFunction.annAttachments.isEmpty()) {
                int lastAnnotationEndline = CommonUtil.getLastItem(bLangFunction.annAttachments).pos.eLine;
                position.sLine = lastAnnotationEndline +
                        wsArray[0].getWs().split(CommonUtil.LINE_SEPARATOR_SPLIT).length - 1;
            }
        }
        return position;
    }

    /**
     * Calculate and returns identifier position of this BLangService.
     *
     * @param serviceNode {@link BLangService}
     * @return position
     */
    public static DiagnosticPos getIdentifierPosition(BLangService serviceNode) {
        DiagnosticPos servPosition = serviceNode.getPosition();
        DiagnosticPos position = new DiagnosticPos(servPosition.src, servPosition.sLine, servPosition.eLine,
                                                   servPosition.sCol,
                                                   servPosition.eCol);
        Set<Whitespace> wsSet = serviceNode.getWS();
        if (wsSet != null && wsSet.size() > 4) {
            Whitespace[] wsArray = new Whitespace[wsSet.size()];
            wsSet.toArray(wsArray);
            Arrays.sort(wsArray);
            int serviceKeywordLength = wsArray[0].getPrevious().length() +
                    wsArray[1].getPrevious().length() + wsArray[1].getWs().length() +
                    wsArray[2].getPrevious().length() + wsArray[2].getWs().length() + wsArray[3].getWs().length();
            int serviceTypeLength = 0;
            position.sCol += (serviceTypeLength + serviceKeywordLength);
            position.eCol = position.sCol + serviceNode.name.value.length();
        }
        return position;
    }

    /**
     * Calculate and returns identifier position of this BlangVariable.
     *
     * @param varNode BLangSimpleVariable
     * @return position
     */
    public static DiagnosticPos getIdentifierPosition(BLangSimpleVariable varNode) {
        DiagnosticPos position = varNode.getPosition();
        Set<Whitespace> wsSet = varNode.getWS();
        if (wsSet != null && wsSet.size() > 0) {
            BLangType typeNode = varNode.getTypeNode();
            int beforeIdentifierWSLength = getLowestIndexedWS(wsSet).getWs().length();
            if (varNode.symbol.type != null && varNode.symbol.type.tsymbol != null) {
                BTypeSymbol bTypeSymbol = varNode.symbol.type.tsymbol;
                PackageID pkgID = bTypeSymbol.pkgID;
                int packagePrefixLen = (pkgID != PackageID.DEFAULT
                        && !pkgID.name.value.startsWith("lang.")
                        && pkgID.name != Names.DEFAULT_PACKAGE)
                        ? (pkgID.name.value + ":").length()
                        : 0;
                if (typeNode instanceof BLangConstrainedType) {
                    int typeSpecifierSymbolLength = 2;
                    int typeSpecifierLength = typeSpecifierSymbolLength + getTotalWhitespaceLen(typeNode.getWS());
                    position.sCol +=
                            packagePrefixLen + ((BLangConstrainedType) typeNode).type.type.tsymbol.name.value.length() +
                                    ((BLangConstrainedType) typeNode).constraint.type.tsymbol.name.value.length() +
                                    typeSpecifierLength + beforeIdentifierWSLength;
                } else {
                    position.sCol += packagePrefixLen + bTypeSymbol.name.value.length() + beforeIdentifierWSLength;
                }
            } else if (typeNode instanceof BLangArrayType && typeNode.type instanceof BArrayType) {
                int arraySpecifierSymbolLength = 2;
                int arraySpecifierLength = arraySpecifierSymbolLength + getTotalWhitespaceLen(typeNode.getWS());
                position.sCol += ((BArrayType) typeNode.type).eType.tsymbol.name.value.length() + arraySpecifierLength +
                        beforeIdentifierWSLength;
            }
            position.eCol = position.sCol + varNode.symbol.name.value.length();
        }
        return position;
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

    private static int getTotalWhitespaceLen(Set<Whitespace> wsSet) {
        Whitespace[] ws = new Whitespace[wsSet.size()];
        wsSet.toArray(ws);
        int length = 0;
        for (Whitespace whitespace : ws) {
            length += whitespace.getWs().length();
        }
        return length;
    }

    private static Whitespace getLowestIndexedWS(Set<Whitespace> wsSet) {
        Whitespace[] ws = new Whitespace[wsSet.size()];
        wsSet.toArray(ws);
        Whitespace sWhitespace = ws[0];
        int sIndex = sWhitespace.getIndex();
        for (Whitespace whitespace : ws) {
            if (sIndex > whitespace.getIndex()) {
                sIndex = whitespace.getIndex();
                sWhitespace = whitespace;
            }
        }
        return sWhitespace;
    }

    /**
     * Filter documentation attributes to each tags.
     *
     * @param docAttachment     documentation node
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
     * @param parameters        parameters to be extracted
     * @param symbol            symbol
     * @param ctx               LS Context
     * @return {@link String }  extracted content of annotation
     */
    private static String getDocAttributes(List<MarkdownDocAttachment.Parameter> parameters, BSymbol symbol,
                                           LSContext ctx) {
        Map<String, BType> types = new HashMap<>();
        if (symbol instanceof BVarSymbol && !(symbol instanceof BInvokableSymbol)) {
            symbol = ((BVarSymbol) symbol).type.tsymbol;
        }
        boolean skipFirstParam = false;
        if (symbol instanceof BInvokableSymbol) {
            // If it is a parameters set of a function invocation
            BInvokableSymbol invokableSymbol = (BInvokableSymbol) symbol;
            List<BVarSymbol> params = invokableSymbol.params;
            int invocationType = (ctx == null || ctx.get(NodeContextKeys.INVOCATION_TOKEN_TYPE_KEY) == null) ? -1
                    : ctx.get(NodeContextKeys.INVOCATION_TOKEN_TYPE_KEY);
            skipFirstParam = CommonUtil.skipFirstParam(invokableSymbol, invocationType);
            for (int i = 0; i < params.size(); i++) {
                if (i == 0 && skipFirstParam) {
                    continue;
                }
                BVarSymbol param = params.get(i);
                types.put(param.name.value, param.type);
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
                    .forEach(s -> types.put(s.symbol.name.value, s.symbol.type));
        }
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            if (i == 0 && skipFirstParam) {
                continue;
            }
            MarkdownDocAttachment.Parameter parameter = parameters.get(i);
            String type = "";
            if (!types.isEmpty() && types.get(parameter.name) != null) {
                type = "`" + CommonUtil.getBTypeName(types.get(parameter.name), ctx, false) + "` ";
            }
            value.append("- ")
                    .append(type).append("**").append(parameter.name.trim()).append("**")
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
}

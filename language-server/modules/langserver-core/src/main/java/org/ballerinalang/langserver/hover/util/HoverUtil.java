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

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for Hover functionality of language server.
 */
public class HoverUtil {

    /**
     * Get the hover information for the given hover context.
     *
     * @param bPackageSymbol    resolved BPackageSymbol for the hover context.
     * @param hoverContext      context of the hover.
     * @return {@link Hover}    hover content.
     */
    private static Hover getHoverInformation(BPackageSymbol bPackageSymbol, LSServiceOperationContext hoverContext) {
        BSymbol filteredBSymbol;
        switch (hoverContext.get(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY)) {
            case ContextConstants.FUNCTION: {
                BInvokableSymbol bInvokableSymbol = bPackageSymbol.scope.entries.entrySet()
                        .stream()
                        .filter(entry -> {
                            String[] symbolNameComponents = entry.getValue().symbol.getName().getValue().split("\\.");
                            return entry.getValue().symbol instanceof BInvokableSymbol
                                    && symbolNameComponents.length > 0
                                    && symbolNameComponents[symbolNameComponents.length - 1]
                                    .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY));
                        })
                        .map(entry -> (BInvokableSymbol) entry.getValue().symbol).findFirst().orElse(null);

                if (bInvokableSymbol == null) {
                    // Check within the attached functions of the objects
                    List<BObjectTypeSymbol> objectTypeSymbols = bPackageSymbol.scope.entries.entrySet().stream()
                            .filter(entry -> entry.getValue().symbol instanceof BObjectTypeSymbol)
                            .map(entry -> (BObjectTypeSymbol) entry.getValue().symbol)
                            .collect(Collectors.toList());
                    bInvokableSymbol = getMatchingObjectFunction(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY),
                            objectTypeSymbols, hoverContext);
                }

                filteredBSymbol = bInvokableSymbol;
                break;
            }
            case ContextConstants.OBJECT:
            case ContextConstants.RECORD:
            case ContextConstants.TYPE_DEF: {
                filteredBSymbol = bPackageSymbol.scope.entries.entrySet().stream()
                        .filter(entry -> entry.getValue().symbol instanceof BTypeSymbol
                                && entry.getValue().symbol.getName().getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .map(entry -> (BTypeSymbol) entry.getValue().symbol)
                        .findFirst().orElse(null);
                break;
            }
            case ContextConstants.ENDPOINT: {
                filteredBSymbol = bPackageSymbol.scope.entries.entrySet().stream()
                        .filter(entry -> entry.getValue().symbol instanceof BEndpointVarSymbol
                                && entry.getValue().symbol.getName().getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .map(entry -> (BEndpointVarSymbol) entry.getValue().symbol)
                        .findFirst().orElse(null);
                break;
            }
            case ContextConstants.VARIABLE: {
                filteredBSymbol = bPackageSymbol.scope.entries.entrySet().stream()
                        .filter(entry -> entry.getValue().symbol instanceof BVarSymbol
                                && entry.getValue().symbol.getName().getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .map(entry -> (BVarSymbol) entry.getValue().symbol)
                        .findFirst().orElse(null);
                break;
            }
            default:
                filteredBSymbol = null;
                break;
        }

        return filteredBSymbol != null
                ? getHoverFromDocAttachment(filteredBSymbol.getMarkdownDocAttachment(), hoverContext)
                : getDefaultHoverObject();
    }

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
     * get current hover content.
     *
     * @param hoverContext        text document context for the hover provider.
     * @param currentBLangPackage package which currently user working on.
     * @return {@link Hover} return Hover object.
     */
    public static Hover getHoverContent(LSServiceOperationContext hoverContext, BLangPackage currentBLangPackage) {
        PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(hoverContext);
        currentBLangPackage.accept(positionTreeVisitor);
        Hover hover;

        // If the cursor is on a node of the current package go inside, else check builtin and native packages.
        if (hoverContext.get(NodeContextKeys.PACKAGE_OF_NODE_KEY) != null) {
            BPackageSymbol packageSymbol = LSPackageLoader
                    .getPackageSymbolById(hoverContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY),
                            hoverContext.get(NodeContextKeys.PACKAGE_OF_NODE_KEY));
            hover = getHoverInformation(packageSymbol, hoverContext);
        } else {
            hover = getDefaultHoverObject();
        }
        return hover;
    }

    /**
     * Get the doc annotation attributes.
     *
     * @param parameters        parameters to be extracted
     * @return {@link String }  extracted content of annotation
     */
    private static String getDocAttributes(List<MarkdownDocAttachment.Parameter> parameters) {
        StringBuilder value = new StringBuilder();
        for (MarkdownDocAttachment.Parameter parameter : parameters) {
            value.append("- ")
                    .append(parameter.name.trim())
                    .append(":")
                    .append(parameter.description.trim()).append("\r\n");
        }

        return value.toString();
    }
    
    private static String getReturnValueDescription(String returnVal) {
        return "- " + returnVal.trim() + "\r\n";
    }

    /**
     * get the formatted string with markdowns.
     *
     * @param header header.
     * @return {@link String} formatted string using markdown.
     */
    private static String getFormattedHoverDocContent(String header, String content) {
        return "**" + header + "**\r\n" + content + "\r\n";
    }

    /**
     * Get Hover from documentation attachment.
     *
     * @param docAttachment     Documentation attachment
     * @return {@link Hover}    hover object.
     */
    private static Hover getHoverFromDocAttachment(MarkdownDocAttachment docAttachment, LSContext context) {
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        StringBuilder content = new StringBuilder();
        Map<String, List<MarkdownDocAttachment.Parameter>> filterAttributes =
                filterDocumentationAttributes(docAttachment, context);

        if (!docAttachment.description.isEmpty()) {
            String description = "\r\n" + docAttachment.description.trim() + "\r\n";
            content.append(getFormattedHoverDocContent(ContextConstants.DESCRIPTION, description));
        }

        if (filterAttributes.get(ContextConstants.DOC_PARAM) != null) {
            content.append(getFormattedHoverDocContent(ContextConstants.PARAM_TITLE,
                    getDocAttributes(filterAttributes.get(ContextConstants.DOC_PARAM))));
        }

        if (filterAttributes.get(ContextConstants.DOC_FIELD) != null) {
            content.append(getFormattedHoverDocContent(ContextConstants.FIELD_TITLE,
                    getDocAttributes(filterAttributes.get(ContextConstants.DOC_FIELD))));
        }

        if (docAttachment.returnValueDescription != null && !docAttachment.returnValueDescription.isEmpty()) {
            content.append(getFormattedHoverDocContent(ContextConstants.RETURN_TITLE,
                    getReturnValueDescription(docAttachment.returnValueDescription)));
        }

        hoverMarkupContent.setValue(content.toString());

        return new Hover(hoverMarkupContent);
    }

    /**
     * Filter documentation attributes to each tags.
     *
     * @param docAttachment     documentation node
     * @return {@link Map}      filtered content map
     */
    private static Map<String, List<MarkdownDocAttachment.Parameter>> filterDocumentationAttributes(
            MarkdownDocAttachment docAttachment, LSContext context) {
        Map<String, List<MarkdownDocAttachment.Parameter>> filteredAttributes = new HashMap<>();
        String paramType = "";
        switch (context.get((NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY))) {
            case ContextConstants.FUNCTION:
                paramType = ContextConstants.DOC_PARAM;
                break;
            case ContextConstants.OBJECT:
            case ContextConstants.RECORD:
            case ContextConstants.TYPE_DEF:
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
     * Get the default hover object.
     *
     * @return {@link Hover} hover default hover object.
     */
    private static Hover getDefaultHoverObject() {
        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue("");
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    /**
     * Get the matching function in the object for given name.
     *
     * @param name                          name of the function
     * @param objects                       objects to be searched
     * @return {@link BInvokableSymbol}     matching function | null
     */
    private static BInvokableSymbol getMatchingObjectFunction(String name, List<BObjectTypeSymbol> objects,
                                                              LSContext ctx) {
        BInvokableSymbol bInvokableSymbol = null;
        outOfLoop:
        for (BObjectTypeSymbol objectTypeSymbol : objects) {
            for (BAttachedFunction attachedFunc : objectTypeSymbol.attachedFuncs) {
                if (attachedFunc.funcName.getValue().equals(name)
                        && objectTypeSymbol.getName().getValue().equals(ctx.get(NodeContextKeys.NODE_OWNER_KEY))) {
                    bInvokableSymbol = attachedFunc.symbol;
                    break outOfLoop;
                }
            }
        }
        return bInvokableSymbol;
    }

    /**
     * Calculate and returns identifier position of this BLangEndpoint.
     *
     * @param endpointNode {@link BLangEndpoint}
     * @return position
     */
    public static DiagnosticPos getIdentifierPosition(BLangEndpoint endpointNode) {
        DiagnosticPos epPosition = endpointNode.getPosition();
        DiagnosticPos position = new DiagnosticPos(epPosition.src, epPosition.sLine, epPosition.eLine, epPosition.sCol,
                                                   epPosition.eCol);
        Set<Whitespace> wsSet = endpointNode.getWS();
        if (wsSet != null && wsSet.size() > 1) {
            Whitespace[] wsArray = new Whitespace[wsSet.size()];
            wsSet.toArray(wsArray);
            Arrays.sort(wsArray);
            int endpointKeywordLength = wsArray[0].getPrevious().length();
            int beforeIdentifierWSLength = wsArray[1].getWs().length();
            if (endpointNode.symbol.type != null && endpointNode.symbol.type.tsymbol != null) {
                BTypeSymbol bTypeSymbol = endpointNode.symbol.type.tsymbol;
                PackageID pkgID = bTypeSymbol.pkgID;
                int packagePrefixLen = (pkgID != PackageID.DEFAULT
                        && pkgID.name != Names.BUILTIN_PACKAGE
                        && pkgID.name != Names.DEFAULT_PACKAGE)
                        ? (pkgID.name.value + ":").length()
                        : 0;
                position.sCol += (beforeIdentifierWSLength + packagePrefixLen + bTypeSymbol.name.value.length() +
                        endpointKeywordLength);
            }
            position.eCol += position.sCol + endpointNode.symbol.name.value.length();
        }
        return position;
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
            if (wsArray[0].getPrevious().equals(UtilSymbolKeys.RESOURCE_KEYWORD_KEY)) {
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
// TODO: 11/28/18 Fix with the latest Service Changes
//            Set<Whitespace> ws = serviceNode.getServiceTypeStruct().getWS();
//            for (Whitespace w : ws) {
//                serviceTypeLength += w.getPrevious().length() + w.getWs().length();
//            }
            position.sCol += (serviceTypeLength + serviceKeywordLength);
            position.eCol = position.sCol + serviceNode.name.value.length();
        }
        return position;
    }

    /**
     * Calculate and returns identifier position of this BLangResource.
     *
     * @param resource {@link BLangResource}
     * @return position
     */
    public static DiagnosticPos getIdentifierPosition(BLangResource resource) {
        DiagnosticPos resPosition = resource.getPosition();
        DiagnosticPos position = new DiagnosticPos(resPosition.src, resPosition.sLine, resPosition.eLine,
                                                   resPosition.sCol,
                                                   resPosition.eCol);
        int maxELine = 0;
        List<BLangAnnotationAttachment> annotations = resource.getAnnotationAttachments();
        for (BLangAnnotationAttachment annotation : annotations) {
            maxELine = Math.max(annotation.pos.eLine, maxELine);
        }
        position.sLine = Math.max(maxELine + 1, position.sLine);
        position.eCol += resource.symbol.name.value.length();
        return position;
    }

    /**
     * Calculate and returns identifier position of this BlangVariable.
     *
     * @param varNode BLangSimpleVariable
     * @return position
     */
    public static DiagnosticPos getIdentifierPosition(BLangSimpleVariable varNode, DiagnosticPos position) {
        Set<Whitespace> wsSet = varNode.getWS();
        if (wsSet != null && wsSet.size() > 0) {
            BLangType typeNode = varNode.getTypeNode();
            int beforeIdentifierWSLength = getLowestIndexedWS(wsSet).getWs().length();
            if (varNode.symbol.type != null && varNode.symbol.type.tsymbol != null) {
                BTypeSymbol bTypeSymbol = varNode.symbol.type.tsymbol;
                PackageID pkgID = bTypeSymbol.pkgID;
                int packagePrefixLen = (pkgID != PackageID.DEFAULT
                        && pkgID.name != Names.BUILTIN_PACKAGE
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
            } else if (typeNode != null && typeNode instanceof BLangArrayType && typeNode.type instanceof BArrayType) {
                int arraySpecifierSymbolLength = 2;
                int arraySpecifierLength = arraySpecifierSymbolLength + getTotalWhitespaceLen(typeNode.getWS());
                position.sCol += ((BArrayType) typeNode.type).eType.tsymbol.name.value.length() + arraySpecifierLength +
                        beforeIdentifierWSLength;
            }
            position.eCol = position.sCol + varNode.symbol.name.value.length();
        }
        return position;
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
}

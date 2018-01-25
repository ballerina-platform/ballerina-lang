/**
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

import org.ballerinalang.langserver.BLangPackageContext;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.hover.HoverKeys;
import org.ballerinalang.langserver.hover.HoverTreeVisitor;
import org.ballerinalang.langserver.hover.constants.HoverConstants;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for Hover functionality of language server.
 */
public class HoverUtil {

    /**
     * Get the hover information for the given hover context.
     *
     * @param bLangPackage resolved bLangPackage for the hover context.
     * @param hoverContext context of the hover.
     * @return hover content.
     */
    public static Hover getHoverInformation(BLangPackage bLangPackage, TextDocumentServiceContext hoverContext) {
        Hover hover = null;
        switch (hoverContext.get(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY).name()) {
            case HoverConstants.FUNCTION:
                BLangFunction bLangFunction = bLangPackage.functions.stream()
                        .filter(function -> function.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                if (bLangFunction != null) {
                    hover = getAnnotationContent(bLangFunction.annAttachments);
                } else {
                    hover = getDefaultHoverObject();
                }

                break;
            case HoverConstants.STRUCT:
                BLangStruct bLangStruct = bLangPackage.structs.stream()
                        .filter(struct -> struct.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                if (bLangStruct != null) {
                    hover = getAnnotationContent(bLangStruct.annAttachments);
                } else {
                    hover = getDefaultHoverObject();
                }

                break;
            case HoverConstants.ENUM:
                BLangEnum bLangEnum = bLangPackage.enums.stream()
                        .filter(bEnum -> bEnum.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                if (bLangEnum != null) {
                    hover = getAnnotationContent(bLangEnum.annAttachments);
                } else {
                    hover = getDefaultHoverObject();
                }

                break;
            case HoverConstants.TRANSFORMER:
                BLangTransformer bLangTransformer = bLangPackage.transformers.stream()
                        .filter(bTransformer -> bTransformer.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                if (bLangTransformer != null) {
                    hover = getAnnotationContent(bLangTransformer.annAttachments);
                } else {
                    hover = getDefaultHoverObject();
                }

                break;

            case HoverConstants.CONNECTOR:
                BLangConnector bLangConnector = bLangPackage.connectors.stream()
                        .filter(bConnector -> bConnector.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                if (bLangConnector != null) {
                    hover = getAnnotationContent(bLangConnector.annAttachments);
                } else {
                    hover = getDefaultHoverObject();
                }

                break;
            case HoverConstants.ACTION:
                BLangAction bLangAction = bLangPackage.connectors.stream()
                        .filter(bConnector -> bConnector.name.getValue()
                                .equals(((BLangInvocation) hoverContext.get(HoverKeys.PREVIOUSLY_VISITED_NODE_KEY))
                                        .symbol.owner.name.getValue()))
                        .flatMap(connector -> connector.actions.stream())
                        .filter(bAction -> bAction.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                if (bLangAction != null) {
                    hover = getAnnotationContent(bLangAction.annAttachments);
                } else {
                    hover = getDefaultHoverObject();
                }

                break;
            default:
                hover = new Hover();
                List<Either<String, MarkedString>> contents = new ArrayList<>();
                contents.add(Either.forLeft(""));
                hover.setContents(contents);
                break;
        }
        return hover;
    }

    /**
     * check whether given position matches the given node's position.
     *
     * @param nodePosition position of the current node.
     * @param textPosition position to be matched.
     * @return return true if position are a match else return false.
     */
    public static boolean isMatchingPosition(DiagnosticPos nodePosition, Position textPosition) {
        boolean isCorrectPosition = false;
        if (nodePosition.sLine <= textPosition.getLine()
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
     * @return return Hover object.
     */
    public static Hover getHoverContent(TextDocumentServiceContext hoverContext, BLangPackage currentBLangPackage,
                                        BLangPackageContext packageContext) {
        HoverTreeVisitor hoverTreeVisitor = new HoverTreeVisitor(hoverContext);
        currentBLangPackage.accept(hoverTreeVisitor);
        Hover hover;

        // If the cursor is on a node of the current package go inside, else check builtin and native packages.
        if (hoverContext.get(HoverKeys.PACKAGE_OF_HOVER_NODE_KEY) != null) {
            hover = getHoverInformation(packageContext
                    .getPackageByName(hoverContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY),
                            hoverContext.get(HoverKeys.PACKAGE_OF_HOVER_NODE_KEY).name), hoverContext);
        } else {
            hover = new Hover();
            List<Either<String, MarkedString>> contents = new ArrayList<>();
            contents.add(Either.forLeft(""));
            hover.setContents(contents);
        }
        return hover;
    }

    /**
     * get annotation value for a given annotation.
     *
     * @param annotationName        annotation name.
     * @param annotationAttachments available annotation attachments.
     * @return concatenated string with annotation value.
     */
    private static String getAnnotationValue(String annotationName,
                                             List<BLangAnnotationAttachment> annotationAttachments) {
        StringBuilder value = new StringBuilder();
        for (BLangAnnotationAttachment annotationAttachment : annotationAttachments) {
            if (annotationAttachment.annotationName.getValue().equals(annotationName)) {
                value.append(getAnnotationAttributes("value", annotationAttachment.attributes))
                        .append("\r\n");
            }
        }
        return value.toString();
    }

    /**
     * get annotation attribute value.
     *
     * @param attributeName             annotation attribute name.
     * @param annotAttachmentAttributes available attributes.
     * @return concatenated string with annotation attribute value.
     */
    private static String getAnnotationAttributes(String attributeName,
                                                  List<BLangAnnotAttachmentAttribute> annotAttachmentAttributes) {
        String value = "";
        for (BLangAnnotAttachmentAttribute attribute : annotAttachmentAttributes) {
            if (attribute.name.getValue().equals(attributeName)) {
                value = ((BLangLiteral) attribute.value.value).getValue().toString();
                break;
            }
        }
        return value;
    }

    /**
     * get the formatted string with markdowns.
     *
     * @param header  header.
     * @param content content.
     * @return string formatted using markdown.
     */
    private static String getFormattedHoverContent(String header, String content) {
        return String.format("**%s**\r%n```\r%n%s\r%n```\r%n", header, content);
    }

    /**
     * get concatenated annotation value.
     *
     * @param annAttachments annotation attachments list
     * @return Hover object with hover content.
     */
    private static Hover getAnnotationContent(List<BLangAnnotationAttachment> annAttachments) {
        Hover hover = new Hover();
        StringBuilder content = new StringBuilder();
        if (!getAnnotationValue(HoverConstants.DESCRIPTION, annAttachments).isEmpty()) {
            content.append(getFormattedHoverContent(HoverConstants.DESCRIPTION,
                    getAnnotationValue(HoverConstants.DESCRIPTION, annAttachments)));
        }

        if (!getAnnotationValue(HoverConstants.PARAM, annAttachments).isEmpty()) {
            content.append(getFormattedHoverContent(HoverConstants.PARAM,
                    getAnnotationValue(HoverConstants.PARAM, annAttachments)));
        }

        if (!getAnnotationValue(HoverConstants.FIELD, annAttachments).isEmpty()) {
            content.append(getFormattedHoverContent(HoverConstants.FIELD,
                    getAnnotationValue(HoverConstants.FIELD, annAttachments)));
        }

        if (!getAnnotationValue(HoverConstants.RETURN, annAttachments).isEmpty()) {
            content.append(getFormattedHoverContent(HoverConstants.RETURN,
                    getAnnotationValue(HoverConstants.RETURN, annAttachments)));
        }

        List<Either<String, MarkedString>> contents = new ArrayList<>();
        contents.add(Either.forLeft(content.toString()));
        hover.setContents(contents);

        return hover;
    }

    /**
     * Get the default hover object.
     *
     * @return hover default hover object.
     */
    private static Hover getDefaultHoverObject() {
        Hover hover = new Hover();
        List<Either<String, MarkedString>> contents = new ArrayList<>();
        contents.add(Either.forLeft(""));
        hover.setContents(contents);

        return hover;
    }
}

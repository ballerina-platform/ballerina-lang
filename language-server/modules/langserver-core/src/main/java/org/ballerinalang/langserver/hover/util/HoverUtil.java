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

import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for Hover functionality of language server.
 */
public class HoverUtil {

    /**
     * Get the hover information for the given hover context.
     *
     * @param bLangPackage resolved bLangPackage for the hover context.
     * @param hoverContext context of the hover.
     * @return {@link Hover} hover content.
     */
    private static Hover getHoverInformation(BLangPackage bLangPackage, LSServiceOperationContext hoverContext) {
        Hover hover;
        switch (hoverContext.get(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY)) {
            case ContextConstants.FUNCTION:
                BLangFunction bLangFunction = bLangPackage.functions.stream()
                        .filter(function -> function.name.getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .findAny().orElse(null);

                bLangFunction = bLangFunction == null ?
                        getMatchingObjectFunctions(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY),
                                bLangPackage.objects) : bLangFunction;

                hover = bLangFunction != null
                        ? getHoverResultForGivenDocs(bLangFunction.docAttachments, bLangFunction.annAttachments)
                        : getDefaultHoverObject();
                break;
            case ContextConstants.STRUCT:
                BLangStruct bLangStruct = bLangPackage.structs.stream()
                        .filter(struct -> struct.name.getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .findAny().orElse(null);
                hover = bLangStruct != null
                        ? getHoverResultForGivenDocs(bLangStruct.docAttachments, bLangStruct.annAttachments)
                        : getDefaultHoverObject();
                break;
            case ContextConstants.OBJECT:
                BLangObject bLangObject = bLangPackage.objects.stream()
                        .filter(object -> object.name.getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .findAny().orElse(null);
                hover = bLangObject != null
                        ? getHoverResultForGivenDocs(bLangObject.docAttachments, bLangObject.annAttachments)
                        : getDefaultHoverObject();
                break;
            case ContextConstants.RECORD:
                BLangRecord bLangRecord = bLangPackage.records.stream()
                        .filter(record -> record.name.getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .findAny().orElse(null);
                hover = bLangRecord != null ?
                        getHoverResultForGivenDocs(bLangRecord.docAttachments, bLangRecord.annAttachments)
                        : getDefaultHoverObject();
                break;
            case ContextConstants.TYPE_DEF:
                BLangTypeDefinition bLangTypeDefinition = bLangPackage.typeDefinitions.stream()
                        .filter(typeDef -> typeDef.name.getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .findAny()
                        .orElse(null);
                hover = bLangTypeDefinition != null ?
                        getHoverResultForGivenDocs(bLangTypeDefinition.docAttachments,
                                bLangTypeDefinition.annAttachments)
                        : getDefaultHoverObject();
                break;
            case ContextConstants.TRANSFORMER:
                BLangTransformer bLangTransformer = bLangPackage.transformers.stream()
                        .filter(bTransformer -> bTransformer.name.getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .findAny().orElse(null);
                hover = bLangTransformer != null
                        ? getHoverResultForGivenDocs(bLangTransformer.docAttachments, bLangTransformer.annAttachments)
                        : getDefaultHoverObject();
                break;

            case ContextConstants.CONNECTOR:
                BLangConnector bLangConnector = bLangPackage.connectors.stream()
                        .filter(bConnector -> bConnector.name.getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .findAny().orElse(null);
                hover = bLangConnector != null
                        ? getHoverResultForGivenDocs(bLangConnector.docAttachments, bLangConnector.annAttachments)
                        : getDefaultHoverObject();

                break;
            case ContextConstants.ACTION:
                BLangAction bLangAction = bLangPackage.connectors.stream()
                        .filter(bConnector -> bConnector.name.getValue()
                                .equals(((BLangInvocation) hoverContext
                                        .get(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY))
                                        .symbol.owner.name.getValue()))
                        .flatMap(connector -> connector.actions.stream())
                        .filter(bAction -> bAction.name.getValue()
                                .equals(hoverContext.get(NodeContextKeys.NAME_OF_NODE_KEY)))
                        .findAny().orElse(null);
                hover = bLangAction != null
                        ? getHoverResultForGivenDocs(bLangAction.docAttachments, bLangAction.annAttachments)
                        : getDefaultHoverObject();
                break;
            case ContextConstants.ENDPOINT:
                BLangEndpoint bLangEndpoint = bLangPackage.globalEndpoints.stream()
                        .filter(globalEndpoint -> globalEndpoint.name.value
                                .equals(hoverContext.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY)))
                        .findAny().orElse(null);
                hover = bLangEndpoint != null
                        ? getAnnotationContent(bLangEndpoint.annAttachments)
                        : getDefaultHoverObject();
                break;
            case ContextConstants.VARIABLE:
                BLangVariable bLangVariable = bLangPackage.globalVars.stream()
                        .filter(globalVar -> globalVar.name.getValue()
                                .equals(hoverContext.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY)))
                        .findAny().orElse(null);

                hover = bLangVariable != null
                        ? getHoverResultForGivenDocs(bLangVariable.docAttachments, bLangVariable.annAttachments)
                        : getDefaultHoverObject();
                break;
            default:
                hover = getDefaultHoverObject();
                break;
        }
        return hover;
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
     * @return {@link Hover} return Hover object.
     */
    public static Hover getHoverContent(LSServiceOperationContext hoverContext, BLangPackage currentBLangPackage) {
        PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(hoverContext);
        currentBLangPackage.accept(positionTreeVisitor);
        Hover hover;

        // If the cursor is on a node of the current package go inside, else check builtin and native packages.
        if (hoverContext.get(NodeContextKeys.PACKAGE_OF_NODE_KEY) != null) {
            hover = getHoverInformation(LSPackageLoader.getPackageById(
                    hoverContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY),
                            hoverContext.get(NodeContextKeys.PACKAGE_OF_NODE_KEY)), hoverContext);
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
     * @return {@link String} concatenated string with annotation value.
     */
    private static String getAnnotationValue(String annotationName,
                                             List<BLangAnnotationAttachment> annotationAttachments) {
        StringBuilder value = new StringBuilder();
        for (BLangAnnotationAttachment annotationAttachment : annotationAttachments) {
            if (annotationAttachment.annotationName.getValue().equals(annotationName)) {
                value.append(getAnnotationAttributes(((BLangRecordLiteral) annotationAttachment.expr).keyValuePairs))
                        .append("\r\n");
            }
        }
        return value.toString();
    }

    /**
     * get annotation attribute value.
     *
     * @param annotAttachmentAttributes available attributes.
     * @return {@link String} concatenated string with annotation attribute value.
     */
    private static String getAnnotationAttributes(List<BLangRecordLiteral
            .BLangRecordKeyValue> annotAttachmentAttributes) {
        String value = "";
        for (BLangRecordLiteral.BLangRecordKeyValue attribute : annotAttachmentAttributes) {
            if (((BLangSimpleVarRef) attribute.key.expr).getVariableName().getValue().equals("value")) {
                value = ((BLangLiteral) attribute.valueExpr).getValue().toString();
                break;
            }
        }
        return value;
    }

    /**
     * Get the doc annotation attributes.
     *
     * @param attributes attributes to be extracted
     * @return {@link String } extracted content of annotation
     */
    private static String getDocAttributes(List<BLangDocumentationAttribute> attributes) {
        StringBuilder value = new StringBuilder();
        for (BLangDocumentationAttribute attribute : attributes) {
            value.append("- ")
                    .append(attribute.documentationField.getValue().replace("\n", ""))
                    .append(":")
                    .append(attribute.documentationText.replaceAll("\n", "")).append("\r\n");
        }

        return value.toString();
    }

    /**
     * get the formatted string with markdowns.
     *
     * @param header  header.
     * @param content content.
     * @return {@link String} string formatted using markdown.
     */
    private static String getFormattedHoverContent(String header, String content) {
        return "**" + header + "**\r\n```\r\n" + content + "\r\n```\r\n";
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
     * get concatenated annotation value.
     *
     * @param annAttachments annotation attachments list
     * @return {@link Hover} Hover object with hover content.
     */
    private static Hover getAnnotationContent(List<BLangAnnotationAttachment> annAttachments) {
        Hover hover = new Hover();
        StringBuilder content = new StringBuilder();
        if (!getAnnotationValue(ContextConstants.DESCRIPTION, annAttachments).isEmpty()) {
            content.append(getFormattedHoverContent(ContextConstants.DESCRIPTION,
                    getAnnotationValue(ContextConstants.DESCRIPTION, annAttachments)));
        }

        if (!getAnnotationValue(ContextConstants.PARAM, annAttachments).isEmpty()) {
            content.append(getFormattedHoverContent(ContextConstants.PARAM,
                    getAnnotationValue(ContextConstants.PARAM, annAttachments)));
        }

        if (!getAnnotationValue(ContextConstants.FIELD, annAttachments).isEmpty()) {
            content.append(getFormattedHoverContent(ContextConstants.FIELD,
                    getAnnotationValue(ContextConstants.FIELD, annAttachments)));
        }

        if (!getAnnotationValue(ContextConstants.RETURN, annAttachments).isEmpty()) {
            content.append(getFormattedHoverContent(ContextConstants.RETURN,
                    getAnnotationValue(ContextConstants.RETURN, annAttachments)));
        }

        List<Either<String, MarkedString>> contents = new ArrayList<>();
        contents.add(Either.forLeft(content.toString()));
        hover.setContents(contents);

        return hover;
    }

    /**
     * Get documentation content.
     *
     * @param docAnnotation list of doc annotation
     * @return {@link Hover} hover object.
     */
    private static Hover getDocumentationContent(List<BLangDocumentation> docAnnotation) {
        Hover hover = new Hover();
        StringBuilder content = new StringBuilder();
        BLangDocumentation bLangDocumentation = docAnnotation.get(0);
        Map<String, List<BLangDocumentationAttribute>> filterAttributes =
                filterDocumentationAttributes(docAnnotation.get(0));

        if (!bLangDocumentation.documentationText.isEmpty()) {
            content.append(getFormattedHoverDocContent(ContextConstants.DESCRIPTION,
                    bLangDocumentation.documentationText));
        }

        if (filterAttributes.get(ContextConstants.DOC_RECEIVER) != null) {
            content.append(getFormattedHoverDocContent(ContextConstants.DOC_RECEIVER,
                    getDocAttributes(filterAttributes.get(ContextConstants.DOC_RECEIVER))));
        }

        if (filterAttributes.get(ContextConstants.DOC_PARAM) != null) {
            content.append(getFormattedHoverDocContent(ContextConstants.DOC_PARAM,
                    getDocAttributes(filterAttributes.get(ContextConstants.DOC_PARAM))));
        }

        if (filterAttributes.get(ContextConstants.DOC_FIELD) != null) {
            content.append(getFormattedHoverDocContent(ContextConstants.DOC_FIELD,
                    getDocAttributes(filterAttributes.get(ContextConstants.DOC_FIELD))));
        }

        if (filterAttributes.get(ContextConstants.DOC_RETURN) != null) {
            content.append(getFormattedHoverDocContent(ContextConstants.DOC_RETURN,
                    getDocAttributes(filterAttributes.get(ContextConstants.DOC_RETURN))));
        }

        if (filterAttributes.get(ContextConstants.DOC_VARIABLE) != null) {
            content.append(getFormattedHoverDocContent(ContextConstants.DOC_VARIABLE,
                    getDocAttributes(filterAttributes.get(ContextConstants.DOC_VARIABLE))));
        }

        List<Either<String, MarkedString>> contents = new ArrayList<>();
        contents.add(Either.forLeft(content.toString()));
        hover.setContents(contents);

        return hover;
    }

    /**
     * Filter documentation attributes to each tags.
     *
     * @param bLangDocumentation documentation node
     * @return {@link Map} filtered content map
     */
    private static Map<String, List<BLangDocumentationAttribute>> filterDocumentationAttributes(
            BLangDocumentation bLangDocumentation) {
        Map<String, List<BLangDocumentationAttribute>> filteredAttributes = new HashMap<>();
        for (BLangDocumentationAttribute bLangDocumentationAttribute : bLangDocumentation.attributes) {
            if (filteredAttributes.get(bLangDocumentationAttribute.docTag.name()) == null) {
                filteredAttributes.put(bLangDocumentationAttribute.docTag.name(), new ArrayList<>());
                filteredAttributes.get(bLangDocumentationAttribute.docTag.name()).add(bLangDocumentationAttribute);
            } else {
                filteredAttributes.get(bLangDocumentationAttribute.docTag.name()).add(bLangDocumentationAttribute);
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
        List<Either<String, MarkedString>> contents = new ArrayList<>();
        contents.add(Either.forLeft(""));
        hover.setContents(contents);

        return hover;
    }

    /**
     * Get the matching function in the object for given name.
     *
     * @param name    name of the function
     * @param objects objects to be searched
     * @return {@link BLangFunction} matching function | null
     */
    private static BLangFunction getMatchingObjectFunctions(String name, List<BLangObject> objects) {
        BLangFunction bLangFunction = null;
        outOfLoop:
        for (BLangObject bLangObject : objects) {
            for (BLangFunction function : bLangObject.functions) {
                if (function.name.getValue().equals(name)) {
                    bLangFunction = function;
                    break outOfLoop;
                }
            }
        }
        return bLangFunction;
    }

    /**
     * Get the hover result for given docs.
     *
     * @param mdDocs         Markdown documentation list
     * @param annotationDocs Annotated documentation list
     * @return {@link Hover} hover result
     */
    private static Hover getHoverResultForGivenDocs(List<BLangDocumentation> mdDocs,
                                                    List<BLangAnnotationAttachment> annotationDocs) {
        return (mdDocs.size() > 0) ? getDocumentationContent(mdDocs)
                : getAnnotationContent(annotationDocs);
    }
}

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
package org.ballerinalang.langserver.command.docs;

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides functionality for generating documentation.
 *
 * @since 0.985.0
 */
public class DocumentationGenerator {

    private DocumentationGenerator() {
    }

    /**
     * Get the documentation edit attachment info for a given particular node.
     *
     * @param node Node given
     * @return Doc Attachment Info
     */
    public static DocAttachmentInfo getDocumentationEditForNode(Node node) {
        DocAttachmentInfo docAttachmentInfo = null;
        switch (node.getKind()) {
            case FUNCTION:
                if (((BLangFunction) node).markdownDocumentationAttachment == null) {
                    docAttachmentInfo = getFunctionNodeDocumentation((BLangFunction) node);
                }
                break;
            case TYPE_DEFINITION:
                if (((BLangTypeDefinition) node).markdownDocumentationAttachment == null
                        && (((BLangTypeDefinition) node).typeNode instanceof BLangRecordTypeNode
                        || ((BLangTypeDefinition) node).typeNode instanceof BLangObjectTypeNode)) {
                    docAttachmentInfo = getRecordOrObjectDocumentation((BLangTypeDefinition) node);
                }
                break;
            case SERVICE:
                if (((BLangService) node).markdownDocumentationAttachment == null) {
                    BLangService bLangService = (BLangService) node;
                    docAttachmentInfo = getServiceNodeDocumentation(bLangService);
                }
                break;
            default:
                break;
        }

        return docAttachmentInfo;
    }

    /**
     * Get Documentation edit for node at a given position.
     *
     * @param topLevelNodeType  top level node type
     * @param bLangPkg          BLang package
     * @param line              position to be compared with
     * @param context           Language server context
     * @return Document attachment info
     */
    public static DocAttachmentInfo getDocumentationEditForNodeByPosition(String topLevelNodeType,
                                                                          BLangPackage bLangPkg, int line,
                                                                          LSContext context) {
        DocAttachmentInfo docAttachmentInfo = null;
        switch (topLevelNodeType) {
            case UtilSymbolKeys.FUNCTION_KEYWORD_KEY:
                docAttachmentInfo = getFunctionDocumentationByPosition(bLangPkg, line, context);
                break;
            case UtilSymbolKeys.SERVICE_KEYWORD_KEY:
                docAttachmentInfo = getServiceDocumentationByPosition(bLangPkg, line, context);
                break;
            case UtilSymbolKeys.RESOURCE_KEYWORD_KEY:
                docAttachmentInfo = getResourceDocumentationByPosition(bLangPkg, line, context);
                break;
            case UtilSymbolKeys.RECORD_KEYWORD_KEY:
            case UtilSymbolKeys.OBJECT_KEYWORD_KEY:
                docAttachmentInfo = getTypeNodeDocumentationByPosition(bLangPkg, line, context);
                break;
            default:
                break;
        }

        return docAttachmentInfo;
    }

    private static DocAttachmentInfo getServiceNodeDocumentation(BLangService bLangService) {
        DiagnosticPos servicePos = CommonUtil.toZeroBasedPosition(bLangService.getPosition());
        List<BLangAnnotationAttachment> annotations = bLangService.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(bLangService.getPosition(), annotations);
        return new DocAttachmentInfo(getDocumentationAttachment(null, servicePos.getStartColumn()), docStart);
    }

    /**
     * Get the Documentation attachment for the function.
     *
     * @param pkg           BLangPackage built
     * @param line          Start line of the function in the source
     * @param ctx           Language server operation context
     * @return {@link DocAttachmentInfo}   Documentation attachment for the function
     */
    private static DocAttachmentInfo getFunctionDocumentationByPosition(BLangPackage pkg, int line, LSContext ctx) {
        List<BLangFunction> filteredFunctions = new ArrayList<>();
        for (TopLevelNode topLevelNode : CommonUtil.getCurrentFileTopLevelNodes(pkg, ctx)) {
            if (topLevelNode instanceof BLangFunction) {
                filteredFunctions.add((BLangFunction) topLevelNode);
            } else if (topLevelNode instanceof BLangTypeDefinition
                    && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangObjectTypeNode) {
                filteredFunctions
                        .addAll(((BLangObjectTypeNode) (((BLangTypeDefinition) topLevelNode).typeNode)).getFunctions());
            }
        }

        for (BLangFunction filteredFunction : filteredFunctions) {
            DiagnosticPos functionPos = CommonUtil.toZeroBasedPosition(filteredFunction.getPosition());
            int functionStart = functionPos.getStartLine();
            if (functionStart == line) {
                return getFunctionNodeDocumentation(filteredFunction);
            }
        }

        return null;
    }

    /**
     * Get resource documentation by position.
     *
     * @param pkg           Current BLangPackage
     * @param line          cursor line
     * @param ctx           Language server context
     * @return {@link DocAttachmentInfo} generated doc attachment information
     */
    private static DocAttachmentInfo getResourceDocumentationByPosition(BLangPackage pkg, int line, LSContext ctx) {
        List<BLangFunction> filteredFunctions = new ArrayList<>();
        CommonUtil.getCurrentFileTopLevelNodes(pkg, ctx).stream()
                .filter(topLevelNode -> topLevelNode instanceof BLangService)
                .map(topLevelNode -> (BLangService) topLevelNode)
                .forEach(bLangService -> {
                    BLangObjectTypeNode serviceType = (BLangObjectTypeNode) bLangService.serviceTypeDefinition
                            .getTypeNode();
                    filteredFunctions.addAll(serviceType.getFunctions());
                });
        for (BLangFunction bLangFunction : filteredFunctions) {
            List<BLangAnnotationAttachment> annotations = bLangFunction.annAttachments;
            int firstAnnotationStart = -1;
            DiagnosticPos resourcePosition = CommonUtil.toZeroBasedPosition(bLangFunction.getPosition());
            if (!annotations.isEmpty()) {
                firstAnnotationStart = CommonUtil
                        .toZeroBasedPosition(annotations.get(0).getPosition()).getStartLine();
            }

            if ((annotations.isEmpty() && line == resourcePosition.getStartLine())
                    || (!annotations.isEmpty() && line >= firstAnnotationStart
                    && line <= resourcePosition.getEndLine())) {
                return getFunctionNodeDocumentation(bLangFunction);
            }
        }
        return null;
    }

    private static DocAttachmentInfo getFunctionNodeDocumentation(BLangFunction bLangFunction) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos functionPos = CommonUtil.toZeroBasedPosition(bLangFunction.getPosition());
        List<BLangAnnotationAttachment> annotations = bLangFunction.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(bLangFunction.getPosition(), annotations);
        int offset = functionPos.getStartColumn();

        List<BLangVariable> params = new ArrayList<>(bLangFunction.getParameters());
        if (bLangFunction.getRestParameters() != null) {
            params.add((BLangVariable) bLangFunction.getRestParameters());
        }
        params.addAll(bLangFunction.getDefaultableParameters()
                              .stream()
                              .map(bLangVariableDef -> bLangVariableDef.var)
                              .collect(Collectors.toList()));
        params.sort(new FunctionArgsComparator());

        params.forEach(param -> attributes.add(getDocAttributeFromBLangVariable((BLangSimpleVariable) param, offset)));
        if (bLangFunction.symbol.retType.getKind() != TypeKind.NIL) {
            attributes.add(getReturnFieldDescription(offset));
        }

        return new DocAttachmentInfo(getDocumentationAttachment(attributes, functionPos.getStartColumn()), docStart);
    }

    private static DocAttachmentInfo getRecordOrObjectDocumentation(BLangTypeDefinition typeDef) {
        List<String> attributes = new ArrayList<>();
        List<BLangSimpleVariable> fields = new ArrayList<>();
        DiagnosticPos structPos = CommonUtil.toZeroBasedPosition(typeDef.getPosition());
        List<BLangAnnotationAttachment> annotations = typeDef.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(typeDef.getPosition(), annotations);

        if (typeDef.typeNode instanceof BLangObjectTypeNode) {
            fields.addAll(((BLangObjectTypeNode) typeDef.typeNode).fields);
        } else if (typeDef.typeNode instanceof BLangRecordTypeNode) {
            fields.addAll(((BLangRecordTypeNode) typeDef.typeNode).fields);
        }
        int offset = structPos.getStartColumn();
        fields.forEach(bLangVariable ->
                               attributes.add(getDocAttributeFromBLangVariable(bLangVariable, offset)));
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, structPos.getStartColumn()), docStart);
    }

    /**
     * Get the Documentation attachment for the service.
     *
     * @param pkg           BLangPackage built
     * @param line          Start line of the service in the source
     * @param ctx           Language server context
     * @return {@link DocAttachmentInfo}   Documentation attachment for the service
     */
    private static DocAttachmentInfo getServiceDocumentationByPosition(BLangPackage pkg, int line, LSContext ctx) {
        for (TopLevelNode topLevelNode : CommonUtil.getCurrentFileTopLevelNodes(pkg, ctx)) {
            if (topLevelNode instanceof BLangService && topLevelNode.getPosition().getStartLine() - 1 == line) {
                BLangService serviceNode = (BLangService) topLevelNode;
                return getServiceNodeDocumentation(serviceNode);
            }
        }

        return null;
    }

    /**
     * Get the Documentation attachment for the type nodes.
     *
     * @param pkg           BLangPackage built
     * @param line          Start line of the type node in the source
     * @param ctx           Language server context
     * @return {@link DocAttachmentInfo}   Documentation attachment for the type node
     */
    private static DocAttachmentInfo getTypeNodeDocumentationByPosition(BLangPackage pkg, int line, LSContext ctx) {
        for (TopLevelNode topLevelNode : CommonUtil.getCurrentFileTopLevelNodes(pkg, ctx)) {
            if (!(topLevelNode instanceof BLangTypeDefinition)) {
                continue;
            }
            BLangTypeDefinition typeDef = (BLangTypeDefinition) topLevelNode;
            DiagnosticPos typeNodePos = CommonUtil.toZeroBasedPosition(typeDef.getPosition());
            if ((typeDef.symbol.kind == SymbolKind.OBJECT || typeDef.symbol.kind == SymbolKind.RECORD)
                    && typeNodePos.getStartLine() == line) {
                return getTypeNodeDocumentation((BLangTypeDefinition) topLevelNode);
            }
        }

        return null;
    }

    private static DocAttachmentInfo getTypeNodeDocumentation(BLangTypeDefinition typeNode) {
        List<String> attributes = new ArrayList<>();
        DiagnosticPos typeNodePos = CommonUtil.toZeroBasedPosition(typeNode.getPosition());
        int offset = typeNodePos.getStartColumn();
        List<BLangAnnotationAttachment> annotations = typeNode.getAnnotationAttachments();
        Position docStart = getDocumentationStartPosition(typeNode.getPosition(), annotations);
        List<VariableNode> publicFields = new ArrayList<>();
        if (typeNode.symbol.kind == SymbolKind.OBJECT) {
            publicFields.addAll(((BLangObjectTypeNode) typeNode.typeNode).getFields()
                                        .stream()
                                        .filter(field -> field.getFlags().contains(Flag.PUBLIC))
                                        .collect(Collectors.toList()));

        } else if (typeNode.symbol.kind == SymbolKind.RECORD) {
            publicFields.addAll(((BLangRecordTypeNode) typeNode.typeNode).getFields());
        }

        publicFields.forEach(variableNode ->
                                     attributes.add(getDocAttributeFromBLangVariable((BLangSimpleVariable) variableNode,
                                                                                     offset)));

        return new DocAttachmentInfo(getDocumentationAttachment(attributes, offset), docStart);
    }

    private static String getDocAttributeFromBLangVariable(BLangSimpleVariable bLangVariable, int offset) {
        return getDocumentationAttribute(bLangVariable.getName().getValue(), offset);
    }

    private static String getDocumentationAttribute(String field, int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        return String.format("%s# + %s - %s Parameter Description", offsetStr, field, field);
    }

    private static String getReturnFieldDescription(int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        return String.format("%s# + return - Return Value Description", offsetStr);
    }

    private static String getDocumentationAttachment(List<String> attributes, int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        if (attributes == null || attributes.isEmpty()) {
            return String.format("# Description%n%s", offsetStr);
        }

        String joinedList = String.join(" \r\n", attributes);
        return String.format("# Description%n%s#%n%s%n%s", offsetStr, joinedList, offsetStr);
    }

    private static Position getDocumentationStartPosition(DiagnosticPos nodePos,
                                                          List<BLangAnnotationAttachment> annotations) {
        DiagnosticPos startPos;
        if (annotations.isEmpty()) {
            startPos = CommonUtil.toZeroBasedPosition(nodePos);
        } else {
            startPos = CommonUtil.toZeroBasedPosition(annotations.get(0).getPosition());
        }

        return new Position(startPos.getStartLine(), startPos.getStartColumn());
    }

    private static class FunctionArgsComparator implements Serializable, Comparator<BLangVariable> {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(BLangVariable arg1, BLangVariable arg2) {
            return arg1.getPosition().getStartColumn() - arg2.getPosition().getStartColumn();
        }
    }
}

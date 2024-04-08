/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.xmltorecordconverter;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.xmltorecordconverter.diagnostic.DiagnosticMessage;
import io.ballerina.xmltorecordconverter.diagnostic.DiagnosticUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.options.ForceFormattingOptions;
import org.ballerinalang.formatter.core.options.FormattingOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static io.ballerina.xmltorecordconverter.util.ConverterUtils.escapeIdentifier;
import static io.ballerina.xmltorecordconverter.util.ConverterUtils.extractTypeDescriptorNodes;
import static io.ballerina.xmltorecordconverter.util.ConverterUtils.extractUnionTypeDescNode;
import static io.ballerina.xmltorecordconverter.util.ConverterUtils.getPrimitiveTypeName;
import static io.ballerina.xmltorecordconverter.util.ConverterUtils.sortTypeDescriptorNodes;

/**
 * APIs for conversion from XML to Ballerina records.
 *
 * @since 2201.7.2
 */
public class XMLToRecordConverter {

    private XMLToRecordConverter() {}

    private static final String XMLNS_PREFIX = "xmlns";
    private static final String XMLDATA = "xmldata";
    private static final String COLON = ":";

    public static XMLToRecordResponse convertXMLToRecord(String xmlValue, boolean isRecordTypeDesc, boolean isClosed,
                                                         boolean forceFormatRecordFields,
                                                         String textFieldName, boolean withNameSpaces) {
        Map<String, NonTerminalNode> recordToTypeDescNodes = new LinkedHashMap<>();
        Map<String, AnnotationNode> recordToAnnotationNodes = new LinkedHashMap<>();
        Map<String, Element> recordToElementNodes = new LinkedHashMap<>();
        List<DiagnosticMessage> diagnosticMessages = new ArrayList<>();
        XMLToRecordResponse response = new XMLToRecordResponse();

        try {
            // Convert the XML string to an InputStream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlValue.getBytes(StandardCharsets.UTF_8));

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputStream);

            Element rootElement = doc.getDocumentElement();
            generateRecords(rootElement, isClosed, recordToTypeDescNodes, recordToAnnotationNodes,
                    recordToElementNodes, diagnosticMessages, textFieldName, withNameSpaces);
        } catch (ParserConfigurationException parserConfigurationException) {
            DiagnosticMessage message = DiagnosticMessage.xmlToRecordConverter100(null);
            diagnosticMessages.add(message);
            return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
        } catch (SAXException saxException) {
            DiagnosticMessage message = DiagnosticMessage.xmlToRecordConverter101(null);
            diagnosticMessages.add(message);
            return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
        } catch (IOException | IllegalArgumentException e) {
            DiagnosticMessage message = DiagnosticMessage.xmlToRecordConverter102(null);
            diagnosticMessages.add(message);
            return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
        }

        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        List<Map.Entry<String, NonTerminalNode>> recordToTypeDescNodeEntries
                = new ArrayList<>(recordToTypeDescNodes.entrySet());
        List<TypeDefinitionNode> typeDefNodes = recordToTypeDescNodeEntries.stream()
                .map(entry -> {
                    List<AnnotationNode> annotations = new ArrayList<>();
                    String recordName = entry.getKey();
                    String recordTypeName = getRecordName(recordName);

                    if ((recordToTypeDescNodeEntries.indexOf(entry) == recordToTypeDescNodeEntries.size() - 1) &&
                            !recordName.equals(recordTypeName)) {
                        String annotNameValue = recordToElementNodes.get(recordName).getLocalName();
                        annotations.add(getXMLNameNode(annotNameValue));
                    }
                    if (recordToAnnotationNodes.containsKey(recordName)) {
                        annotations.add(recordToAnnotationNodes.get(recordName));
                    }
                    NodeList<AnnotationNode> annotationNodes = NodeFactory.createNodeList(annotations);

                    MetadataNode metadata = NodeFactory.createMetadataNode(null, annotationNodes);
                    Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
                    IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(recordTypeName);
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    return NodeFactory.createTypeDefinitionNode(metadata, null, typeKeyWord, typeName,
                            entry.getValue(), semicolon);
                }).toList();

        NodeList<ModuleMemberDeclarationNode> moduleMembers =
                AbstractNodeFactory.createNodeList(new ArrayList<>(typeDefNodes));

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
        try {
            ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder()
                    .setForceFormatRecordFields(forceFormatRecordFields).build();
            FormattingOptions formattingOptions = FormattingOptions.builder()
                    .setForceFormattingOptions(forceFormattingOptions).build();
            response.setCodeBlock(Formatter.format(modulePartNode.syntaxTree(), formattingOptions).toSourceCode());
        } catch (FormatterException e) {
            DiagnosticMessage message = DiagnosticMessage.xmlToRecordConverter103(null);
            diagnosticMessages.add(message);
        }
        return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
    }

    /**
     * This method converts the provided XML value to a record.
     *
     * @param xmlValue The XML value to be converted to a record.
     * @param isRecordTypeDesc Whether the record is a type descriptor.
     * @param isClosed Whether the record is closed or not.
     * @param textFieldName Whether to force format the result.
     * @return {@link XMLToRecordResponse} The response object containing the converted record.
     */
    public static XMLToRecordResponse convert(String xmlValue, boolean isRecordTypeDesc, boolean isClosed,
                                              boolean textFieldName) {
        return convertXMLToRecord(xmlValue, isRecordTypeDesc, isClosed, textFieldName, null, true);
    }

    private static void generateRecords(Element xmlElement, boolean isClosed,
                                        Map<String, NonTerminalNode> recordToTypeDescNodes,
                                        Map<String, AnnotationNode> recordToAnnotationsNodes,
                                        Map<String, Element> recordToElementNodes,
                                        List<DiagnosticMessage> diagnosticMessages,
                                        String textFieldName, boolean withNameSpace) {
        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
        Token bodyStartDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.OPEN_BRACE_PIPE_TOKEN :
                SyntaxKind.OPEN_BRACE_TOKEN);

        String xmlNodeName = xmlElement.getNodeName();

        List<Node> recordFields = getRecordFieldsForXMLElement(xmlElement, isClosed, recordToTypeDescNodes,
                recordToAnnotationsNodes, recordToElementNodes, diagnosticMessages, textFieldName,
                withNameSpace);
        if (recordToTypeDescNodes.containsKey(xmlNodeName)) {
            RecordTypeDescriptorNode previousRecordTypeDescriptorNode =
                    (RecordTypeDescriptorNode) recordToTypeDescNodes.get(xmlNodeName);
            List<RecordFieldNode> previousRecordFields = previousRecordTypeDescriptorNode.fields().stream()
                    .map(node -> (RecordFieldNode) node).toList();
            Map<String, RecordFieldNode> previousRecordFieldToNodes = previousRecordFields.stream()
                    .collect(Collectors.toMap(node -> node.fieldName().text(), Function.identity(),
                            (val1, val2) -> val1, LinkedHashMap::new));

            Map<String, RecordFieldNode> newRecordFieldToNodes = recordFields.stream()
                    .collect(Collectors.toMap(node -> ((RecordFieldNode) node).fieldName().text(),
                            node -> (RecordFieldNode) node, (e1, e2) -> e1, LinkedHashMap::new));

            recordFields.clear();
            recordFields = updateRecordFields(previousRecordFieldToNodes, newRecordFieldToNodes);
        }
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFields);
        Token bodyEndDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.CLOSE_BRACE_PIPE_TOKEN :
                SyntaxKind.CLOSE_BRACE_TOKEN);
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        recordToElementNodes.put(xmlNodeName, xmlElement);
        recordToTypeDescNodes.put(xmlNodeName, recordTypeDescriptorNode);
    }

    private static List<Node> getRecordFieldsForXMLElement(Element xmlElement, boolean isClosed,
                                                           Map<String, NonTerminalNode> recordToTypeDescNodes,
                                                           Map<String, AnnotationNode> recordToAnnotationNodes,
                                                           Map<String, Element> recordToElementNodes,
                                                           List<DiagnosticMessage> diagnosticMessages,
                                                           String textFieldName, boolean withNameSpace) {
        List<Node> recordFields = new ArrayList<>();

        String xmlNodeName = xmlElement.getNodeName();
        org.w3c.dom.NodeList xmlNodeList = xmlElement.getChildNodes();
        for (int i = 0; i < xmlNodeList.getLength(); i++) {
            org.w3c.dom.Node xmlNode = xmlNodeList.item(i);

            if (xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element xmlElementNode = (Element) xmlNode;
                boolean isLeafXMLElementNode = isLeafXMLElementNode(xmlElementNode);
                if (!isLeafXMLElementNode || xmlElementNode.getAttributes().getLength() > 0) {
                    generateRecords(xmlElementNode, isClosed, recordToTypeDescNodes, recordToAnnotationNodes,
                            recordToElementNodes, diagnosticMessages, textFieldName, withNameSpace);
                }
                RecordFieldNode recordField = getRecordField(xmlElementNode, false, withNameSpace);
                if (recordFields.stream().anyMatch(recField -> ((RecordFieldNode) recField).fieldName().text()
                        .equals(recordField.fieldName().text()))) {
                    int indexOfRecordFieldNode = IntStream.range(0, recordFields.size())
                            .filter(j -> ((RecordFieldNode) recordFields.get(j)).fieldName().text()
                                    .equals(recordField.fieldName().text())).findFirst().orElse(-1);
                    if (indexOfRecordFieldNode == -1) {
                        recordFields.add(recordField);
                    } else {
                        RecordFieldNode existingRecordField =
                                (RecordFieldNode) recordFields.remove(indexOfRecordFieldNode);
                        RecordFieldNode updatedRecordField = mergeRecordFields(existingRecordField, recordField);
                        recordFields.add(indexOfRecordFieldNode, updatedRecordField);
                    }
                } else {
                    recordFields.add(recordField);
                }
            }
        }
        NamedNodeMap xmlAttributesMap = xmlElement.getAttributes();
        Set<String> elementNames = new HashSet<>();
        for (int j = 0; j < xmlNodeList.getLength(); j++) {
            elementNames.add(xmlNodeList.item(j).getNodeName());
        }
        for (int i = 0; i < xmlAttributesMap.getLength(); i++) {
            org.w3c.dom.Node xmlNode = xmlAttributesMap.item(i);
            if (xmlNode.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE) {
                if ((xmlNode.getPrefix() == null &&
                        XMLNS_PREFIX.equals(xmlNode.getLocalName())) || (XMLNS_PREFIX.equals(xmlNode.getPrefix()) &&
                                xmlNode.getLocalName().equals(xmlElement.getPrefix())) && withNameSpace) {
                    String prefix = null;
                    if (xmlElement.getPrefix() != null && xmlElement.getPrefix().equals(xmlNode.getLocalName())) {
                        prefix = xmlNode.getLocalName();
                    }
                    AnnotationNode xmlNSNode = getXMLNamespaceNode(prefix, xmlNode.getNodeValue());
                    recordToAnnotationNodes.put(xmlNodeName, xmlNSNode);
                } else if (!isLeafXMLElementNode(xmlElement) && !XMLNS_PREFIX.equals(xmlNode.getPrefix())) {
                    if (elementNames.contains(xmlNode.getNodeName())) {
                        continue;
                    }
                    Node recordField = getRecordField(xmlNode);
                    recordFields.add(recordField);
                }
            }
        }
        if (isLeafXMLElementNode(xmlElement) && xmlElement.getAttributes().getLength() > 0) {
            Token fieldType = AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);
            IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(textFieldName == null ?
                    escapeIdentifier("#content") : textFieldName);
            Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
            RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null, fieldType,
                    fieldName, null, semicolon);
            recordFields.add(recordFieldNode);
            for (int j = 0; j < xmlElement.getAttributes().getLength(); j++) {
                org.w3c.dom.Node xmlAttributeNode = xmlElement.getAttributes().item(j);
                if (xmlAttributeNode.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE) {
                    RecordFieldNode recordField = (RecordFieldNode) getRecordField(xmlAttributeNode);
                    recordFields.add(recordField);
                }
            }
        }

        return recordFields;
    }

    /**
     * This method returns a List of RecordFieldNodes that are updated if already a Record with same name exists.
     *
     * @param previousRecordFieldToNodes    RecordFieldNodes of already existing Record of the same name
     * @param newRecordFieldToNodes         RecordFieldNodes of the current Record of the same name
     * @return {@link List<Node>} List of updated RecordFieldNodes.
     */
    private static List<Node> updateRecordFields(Map<String, RecordFieldNode> previousRecordFieldToNodes,
                                                 Map<String, RecordFieldNode> newRecordFieldToNodes) {
        List<Node> updatedRecordFields = new ArrayList<>();

        for (Map.Entry<String, RecordFieldNode> previousRecordFieldToNode : previousRecordFieldToNodes.entrySet()) {
            RecordFieldNode prevRecordFieldNode = previousRecordFieldToNode.getValue();
            RecordFieldNode newRecordFieldNode = newRecordFieldToNodes.get(previousRecordFieldToNode.getKey());
            if (newRecordFieldToNodes.containsKey(previousRecordFieldToNode.getKey())) {
                TypeDescriptorNode prevTypeDescNode = (TypeDescriptorNode) prevRecordFieldNode.typeName();
                TypeDescriptorNode newTypeDescNode = (TypeDescriptorNode) newRecordFieldNode.typeName();
                MetadataNode metadataNode = prevRecordFieldNode.metadata()
                        .orElse(newRecordFieldNode.metadata().orElse(null));
                Token optionalToken = prevRecordFieldNode.questionMarkToken()
                        .orElse(newRecordFieldNode.questionMarkToken().orElse(null));
                if (prevTypeDescNode.toSourceCode().equals(newTypeDescNode.toSourceCode())) {
                    RecordFieldNode updatedRecordFieldNode = prevRecordFieldNode.modify()
                            .withMetadata(metadataNode)
                            .withQuestionMarkToken(optionalToken)
                            .apply();
                    updatedRecordFields.add(updatedRecordFieldNode);
                } else {
                    List<TypeDescriptorNode> typeDescNodesSorted =
                            sortTypeDescriptorNodes(extractTypeDescriptorNodes(List.of(prevTypeDescNode,
                                    newTypeDescNode)));
                    TypeDescriptorNode unionTypeDescNode = createUnionTypeDescriptorNode(typeDescNodesSorted);

                    RecordFieldNode updatedRecordFieldNode = prevRecordFieldNode.modify()
                            .withMetadata(metadataNode)
                            .withTypeName(unionTypeDescNode)
                            .withQuestionMarkToken(optionalToken)
                            .apply();
                    updatedRecordFields.add(updatedRecordFieldNode);
                }
            } else {
                Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
                RecordFieldNode updatedRecordFieldNode = prevRecordFieldNode.modify()
                        .withQuestionMarkToken(questionMarkToken)
                        .apply();
                updatedRecordFields.add(updatedRecordFieldNode);
            }
        }

        for (int i = 0; i < newRecordFieldToNodes.size(); i++) {
            List<String> updatedRecordFieldNames = updatedRecordFields.stream()
                    .map(node -> ((RecordFieldNode) node).fieldName().toSourceCode()).toList();
            String newRecordFieldName = newRecordFieldToNodes.keySet().stream().toList().get(i);
            if (!updatedRecordFieldNames.contains(newRecordFieldName)) {
                int insertIndex = -1;
                for (String newRecordFieldToNodeKey : newRecordFieldToNodes.keySet().stream().toList()
                        .subList(i + 1, newRecordFieldToNodes.size())) {
                    insertIndex =
                            (previousRecordFieldToNodes.keySet().stream().toList().indexOf(newRecordFieldToNodeKey));
                    if (insertIndex != -1) {
                        break;
                    }
                }
                Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
                RecordFieldNode updatedRecordFieldNode = newRecordFieldToNodes
                        .get(newRecordFieldToNodes.keySet().stream().toList().get(i)).modify()
                        .withQuestionMarkToken(questionMarkToken).apply();
                if (insertIndex != -1) {
                    updatedRecordFields.add(insertIndex, updatedRecordFieldNode);
                } else {
                    updatedRecordFields.add(updatedRecordFieldNode);
                }
            }
        }
        return updatedRecordFields;
    }

    private static RecordFieldNode getRecordField(Element xmlElementNode, boolean isOptionalField,
                                                  boolean withNameSpace) {
        Token typeName;
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        IdentifierToken fieldName =
                AbstractNodeFactory.createIdentifierToken(escapeIdentifier(xmlElementNode.getLocalName().trim()));
        Token optionalFieldToken = isOptionalField ? questionMarkToken : null;
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        if (isLeafXMLElementNode(xmlElementNode) && xmlElementNode.getAttributes().getLength() == 0) {
            typeName = getPrimitiveTypeName(xmlElementNode.getFirstChild().getNodeValue());
        } else {
            // At the moment all are considered as Objects here
            String elementKey = xmlElementNode.getNodeName().trim();
            String type = getRecordName(elementKey);
            typeName = AbstractNodeFactory.createIdentifierToken(type);
        }
        List<AnnotationNode> xmlNameNode = List.of(getXMLNamespaceNode(xmlElementNode.getPrefix(),
                xmlElementNode.getNamespaceURI()));
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createNodeList(xmlNameNode);
        MetadataNode metadataNode = NodeFactory.createMetadataNode(null, annotationNodes);
        TypeDescriptorNode fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);

        if (!withNameSpace) {
            return NodeFactory.createRecordFieldNode(null, null, fieldTypeName, fieldName, optionalFieldToken,
                    semicolonToken);
        }
        return xmlElementNode.getNamespaceURI() == null ? NodeFactory.createRecordFieldNode(null, null, fieldTypeName,
                fieldName, optionalFieldToken, semicolonToken) : NodeFactory.createRecordFieldNode(
                        metadataNode, null, fieldTypeName, fieldName, optionalFieldToken, semicolonToken);
    }

    private static Node getRecordField(org.w3c.dom.Node xmlAttributeNode) {
        Token typeName = AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);
        TypeDescriptorNode fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);
        IdentifierToken fieldName =
                AbstractNodeFactory.createIdentifierToken(escapeIdentifier(xmlAttributeNode.getLocalName()));
        Token equalToken = AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN);
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createNodeList(getXMLAttributeNode());
        MetadataNode metadataNode = NodeFactory.createMetadataNode(null, annotations);

        if (xmlAttributeNode.getPrefix() != null &&
                xmlAttributeNode.getPrefix().equals(XMLNS_PREFIX)) {
            MinutiaeList emptyMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();
            LiteralValueToken literalToken = NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                    String.format("\"%s\"", xmlAttributeNode.getTextContent()), emptyMinutiaeList, emptyMinutiaeList);
            BasicLiteralNode valueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, literalToken);
            return NodeFactory.createRecordFieldWithDefaultValueNode(metadataNode, null, fieldTypeName,
                    fieldName, equalToken, valueExpr, semicolonToken);
        }
        return NodeFactory.createRecordFieldNode(metadataNode, null, fieldTypeName, fieldName, null, semicolonToken);
    }

    private static RecordFieldNode mergeRecordFields(RecordFieldNode existingRecordFieldNode,
                                                     RecordFieldNode newRecordFieldNode) {
        TypeDescriptorNode existingTypeName = (TypeDescriptorNode) existingRecordFieldNode.typeName();
        TypeDescriptorNode newTypeName = (TypeDescriptorNode) newRecordFieldNode.typeName();

        if (existingTypeName.kind().equals(SyntaxKind.ARRAY_TYPE_DESC)) {
            TypeDescriptorNode memberTypeDescNode = ((ArrayTypeDescriptorNode) existingTypeName).memberTypeDesc();
            if (memberTypeDescNode.toSourceCode().strip().equals(newTypeName.toSourceCode().strip())) {
                return existingRecordFieldNode;
            } else {
                Token openParenToken = NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
                Token closeParenToken = NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

                List<TypeDescriptorNode> extractedTypeDescNodes = extractUnionTypeDescNode(memberTypeDescNode);
                if (extractedTypeDescNodes.stream().filter(node -> node.toSourceCode()
                        .equals(newTypeName.toSourceCode())).findFirst().isEmpty()) {
                    extractedTypeDescNodes.add(newTypeName);
                }
                List<TypeDescriptorNode> sortedTypeDescNodes = sortTypeDescriptorNodes(extractedTypeDescNodes);
                TypeDescriptorNode unionTypeDescNode = joinToUnionTypeDescriptorNode(sortedTypeDescNodes);
                ParenthesisedTypeDescriptorNode parenTypeDescNode =
                        NodeFactory.createParenthesisedTypeDescriptorNode(openParenToken, unionTypeDescNode,
                                closeParenToken);
                ArrayTypeDescriptorNode updatedTypeName = getArrayTypeDesc(parenTypeDescNode);
                return existingRecordFieldNode.modify().withTypeName(updatedTypeName).apply();
            }
        } else {
            if (existingTypeName.toSourceCode().strip().equals(newTypeName.toSourceCode().strip())) {
                ArrayTypeDescriptorNode updatedTypeName = getArrayTypeDesc(existingTypeName);
                return existingRecordFieldNode.modify().withTypeName(updatedTypeName).apply();
            } else {
                Token openParenToken = NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
                Token closeParenToken = NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

                List<TypeDescriptorNode> sortedTypeDescNodes =
                        sortTypeDescriptorNodes(List.of(existingTypeName, newTypeName));
                TypeDescriptorNode unionTypeDescNode = joinToUnionTypeDescriptorNode(sortedTypeDescNodes);
                ParenthesisedTypeDescriptorNode parenTypeDescNode =
                        NodeFactory.createParenthesisedTypeDescriptorNode(openParenToken, unionTypeDescNode,
                                closeParenToken);
                ArrayTypeDescriptorNode updatedTypeName = getArrayTypeDesc(parenTypeDescNode);
                return existingRecordFieldNode.modify().withTypeName(updatedTypeName).apply();
            }
        }
    }

    private static ArrayTypeDescriptorNode getArrayTypeDesc(TypeDescriptorNode typeDescNode) {
        Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        ArrayDimensionNode arrayDimension = NodeFactory.createArrayDimensionNode(openSBracketToken, null,
                closeSBracketToken);
        NodeList<ArrayDimensionNode> arrayDimensions = NodeFactory.createNodeList(arrayDimension);

        return NodeFactory.createArrayTypeDescriptorNode(typeDescNode, arrayDimensions);
    }

    private static TypeDescriptorNode joinToUnionTypeDescriptorNode(List<TypeDescriptorNode> typeNames) {
        Token pipeToken = NodeFactory.createToken(SyntaxKind.PIPE_TOKEN);

        TypeDescriptorNode unionTypeDescNode = typeNames.get(0);
        for (int i = 1; i < typeNames.size(); i++) {
            unionTypeDescNode =
                    NodeFactory.createUnionTypeDescriptorNode(unionTypeDescNode, pipeToken, typeNames.get(i));
        }
        return unionTypeDescNode;
    }

    private static TypeDescriptorNode createUnionTypeDescriptorNode(List<TypeDescriptorNode> typeNames) {
        if (typeNames.isEmpty()) {
            Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANYDATA_KEYWORD);
            return NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);
        } else if (typeNames.size() == 1) {
            return typeNames.get(0);
        }
        TypeDescriptorNode unionTypeDescNode = joinToUnionTypeDescriptorNode(typeNames);
        Token openParenToken = NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        Token closeParenToken = NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        return NodeFactory.createParenthesisedTypeDescriptorNode(openParenToken, unionTypeDescNode, closeParenToken);
    }

    private static boolean isLeafXMLElementNode(Element xmlElement) {
        return xmlElement.getChildNodes().getLength() == 1 &&
                xmlElement.getChildNodes().item(0).getNodeType() == org.w3c.dom.Node.TEXT_NODE;
    }

    private static String getRecordName(String xmlElementNodeName) {
        if (xmlElementNodeName.contains(COLON)) {
            return Arrays.stream(xmlElementNodeName.split(COLON))
                    .map(val -> escapeIdentifier(StringUtils.capitalize(val))).collect(Collectors.joining("_"));
        }
        return escapeIdentifier(StringUtils.capitalize(xmlElementNodeName));
    }

    /**
     * This method returns xmldata:Name AnnotationNode with provided value.
     *
     * @param value String value to be passed as xmldata:Name annotation's value
     * @return {@link AnnotationNode} xmldata:Name AnnotationNode
     */
    private static AnnotationNode getXMLNameNode(String value) {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken(XMLDATA);
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Name");
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        Token openBrace = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
        Token closeBrace = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken("value");
        MinutiaeList emptyMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();
        LiteralValueToken literalToken =
                NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, String.format("\"%s\"", value),
                        emptyMinutiaeList, emptyMinutiaeList);
        BasicLiteralNode valueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, literalToken);
        MappingFieldNode mappingField =
                NodeFactory.createSpecificFieldNode(null, fieldName, colon, valueExpr);
        SeparatedNodeList<MappingFieldNode> mappingFields = AbstractNodeFactory.createSeparatedNodeList(mappingField);
        MappingConstructorExpressionNode annotValue =
                NodeFactory.createMappingConstructorExpressionNode(openBrace, mappingFields, closeBrace);

        return NodeFactory.createAnnotationNode(atToken, annotReference, annotValue);
    }

    /**
     * This method returns xmldata:Namespace AnnotationNode.
     *
     * @return {@link AnnotationNode} xmldata:Namespace AnnotationNode
     */
    private static AnnotationNode getXMLNamespaceNode(String prefix, String uri) {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken(XMLDATA);
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Namespace");
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        List<Node> mappingFields = new ArrayList<>();
        Token openBrace = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
        Token closeBrace = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        MinutiaeList emptyMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();

        if (prefix != null) {
            IdentifierToken prefixFieldName = AbstractNodeFactory.createIdentifierToken("prefix");
            LiteralValueToken prefixLiteralToken =
                    NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                            String.format("\"%s\"", prefix), emptyMinutiaeList, emptyMinutiaeList);
            BasicLiteralNode prefixValueExpr =
                    NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, prefixLiteralToken);
            MappingFieldNode prefixMappingField =
                    NodeFactory.createSpecificFieldNode(null, prefixFieldName, colon, prefixValueExpr);
            mappingFields.add(prefixMappingField);
            mappingFields.add(NodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }

        IdentifierToken uriFieldName = AbstractNodeFactory.createIdentifierToken("uri");
        LiteralValueToken uriLiteralToken =
                NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, String.format("\"%s\"", uri),
                        emptyMinutiaeList, emptyMinutiaeList);
        BasicLiteralNode uriValueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, uriLiteralToken);
        MappingFieldNode uriMappingField =
                NodeFactory.createSpecificFieldNode(null, uriFieldName, colon, uriValueExpr);
        mappingFields.add(uriMappingField);

        SeparatedNodeList<MappingFieldNode> mappingFieldNodes =
                AbstractNodeFactory.createSeparatedNodeList(mappingFields);
        MappingConstructorExpressionNode annotValue =
                NodeFactory.createMappingConstructorExpressionNode(openBrace, mappingFieldNodes, closeBrace);

        return NodeFactory.createAnnotationNode(atToken, annotReference, annotValue);
    }

    /**
     * This method returns xmldata:Attribute AnnotationNode.
     *
     * @return {@link AnnotationNode} xmldata:Attribute AnnotationNode
     */
    private static AnnotationNode getXMLAttributeNode() {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken(XMLDATA);
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Attribute");
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        return NodeFactory.createAnnotationNode(atToken, annotReference, null);
    }
}

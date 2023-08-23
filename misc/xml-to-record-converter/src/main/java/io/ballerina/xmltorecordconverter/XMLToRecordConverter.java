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
import org.ballerinalang.formatter.core.ForceFormattingOptions;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.FormattingOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static io.ballerina.xmltorecordconverter.util.ConverterUtils.escapeIdentifier;
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

    public static XMLToRecordResponse convert(String xmlValue, boolean isRecordTypeDesc, boolean isClosed,
                                              boolean forceFormatRecordFields) {
        Map<String, NonTerminalNode> recordToTypeDescNodes = new LinkedHashMap<>();
        Map<String, List<AnnotationNode>> recordToAnnotationsNodes = new LinkedHashMap<>();
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
            generateRecords(rootElement, isClosed, recordToTypeDescNodes, recordToAnnotationsNodes,
                    recordToElementNodes, diagnosticMessages);
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
                    String recordTypeName = escapeIdentifier(StringUtils.capitalize(recordName));

                    if ((recordToTypeDescNodeEntries.indexOf(entry) == recordToTypeDescNodeEntries.size() - 1) &&
                            !recordName.equals(recordTypeName)) {
                        String annotNameValue = recordToElementNodes.get(recordName).getLocalName();
                        annotations.add(getXMLNameNode(annotNameValue));
                    }
                    if (recordToAnnotationsNodes.containsKey(recordName)) {
                        annotations.addAll(recordToAnnotationsNodes.get(recordName));
                    }
                    NodeList<AnnotationNode> annotationNodes = NodeFactory.createNodeList(annotations);

                    MetadataNode metadata = NodeFactory.createMetadataNode(null, annotationNodes);
                    Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
                    IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(recordTypeName);
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    return NodeFactory.createTypeDefinitionNode(metadata, null, typeKeyWord, typeName,
                            entry.getValue(), semicolon);
                }).collect(Collectors.toList());

        NodeList<ModuleMemberDeclarationNode> moduleMembers =
                AbstractNodeFactory.createNodeList(new ArrayList<>(typeDefNodes));;

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

    private static void generateRecords(Element xmlElement, boolean isClosed,
                                        Map<String, NonTerminalNode> recordToTypeDescNodes,
                                        Map<String, List<AnnotationNode>> recordToAnnotationsNodes,
                                        Map<String, Element> recordToElementNodes,
                                        List<DiagnosticMessage> diagnosticMessages) {
        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
        Token bodyStartDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.OPEN_BRACE_PIPE_TOKEN :
                SyntaxKind.OPEN_BRACE_TOKEN);

        List<Node> recordFields = new ArrayList<>();

        String xmlNodeName = xmlElement.getNodeName();
        if (recordToTypeDescNodes.containsKey(xmlNodeName)) {
            // Process differently
        } else {
            org.w3c.dom.NodeList xmlNodeList = xmlElement.getChildNodes();
            for (int i = 0; i < xmlNodeList.getLength(); i++) {
                org.w3c.dom.Node xmlNode = xmlNodeList.item(i);

                if (xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element xmlElementNode = (Element) xmlNode;
                    boolean isLeafXMLElementNode = isLeafXMLElementNode(xmlElementNode);
                    if (!isLeafXMLElementNode) {
                        generateRecords(xmlElementNode, isClosed, recordToTypeDescNodes, recordToAnnotationsNodes,
                                recordToElementNodes, diagnosticMessages);
                    }
                    RecordFieldNode recordField = getRecordField(xmlElementNode, null, null, false);
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
            org.w3c.dom.NamedNodeMap xmlAttributesMap = xmlElement.getAttributes();
            for (int i = 0; i < xmlAttributesMap.getLength(); i++) {
                org.w3c.dom.Node xmlNode = xmlAttributesMap.item(i);
                if (xmlNode.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE) {
                    if (xmlNode.getPrefix() != null && xmlNode.getPrefix().equals(XMLNS_PREFIX)) {
                        List<AnnotationNode> annotationNodes = new ArrayList<>();
                        if (recordToAnnotationsNodes.containsKey(xmlNodeName)) {
                            annotationNodes = recordToAnnotationsNodes.get(xmlNodeName);
                            annotationNodes.add(getXMLNamespaceNode(xmlNode.getLocalName(), xmlNode.getNodeValue()));
                        } else {
                            annotationNodes.add(getXMLNamespaceNode(xmlNode.getLocalName(), xmlNode.getNodeValue()));
                        }
                        recordToAnnotationsNodes.put(xmlNodeName, annotationNodes);
                    } else {
                        RecordFieldNode recordField = getRecordField(xmlNode);
                        recordFields.add(recordField);
                    }
                }
            }
        }
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFields);
        Token bodyEndDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.CLOSE_BRACE_PIPE_TOKEN :
                SyntaxKind.CLOSE_BRACE_TOKEN);
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        if (!recordToTypeDescNodes.containsKey(xmlNodeName)) {
            recordToElementNodes.put(xmlNodeName, xmlElement);
            recordToTypeDescNodes.put(xmlNodeName, recordTypeDescriptorNode);
        }
    }

    private static RecordFieldNode getRecordField(Element xmlElementNode, List<String> existingFieldNames,
                                                  Map<String, String> updatedFieldNames, boolean isOptionalField) {
        Token typeName;
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        TypeDescriptorNode fieldTypeName;
        IdentifierToken fieldName =
                AbstractNodeFactory.createIdentifierToken(escapeIdentifier(xmlElementNode.getNodeName().trim()));
        Token optionalFieldToken = isOptionalField ? questionMarkToken : null;
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        RecordFieldNode recordFieldNode;

        if (isLeafXMLElementNode(xmlElementNode)) {
            typeName = getPrimitiveTypeName(xmlElementNode.getFirstChild().getNodeValue());
        } else {
            // At the moment all are considered as Objects here
            String elementKey = xmlElementNode.getNodeName().trim();
            String type = escapeIdentifier(StringUtils.capitalize(elementKey));
            typeName = AbstractNodeFactory.createIdentifierToken(type);
        }
        fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);
        recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                fieldTypeName, fieldName,
                optionalFieldToken, semicolonToken);
        return recordFieldNode;
    }

    private static RecordFieldNode getRecordField(org.w3c.dom.Node xmlAttributeNode) {
        Token typeName = AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);;
        TypeDescriptorNode fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);
        IdentifierToken fieldName =
                AbstractNodeFactory.createIdentifierToken(escapeIdentifier(xmlAttributeNode.getNodeName()));
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createNodeList(getXMLAttributeNode());
        MetadataNode metadataNode = NodeFactory.createMetadataNode(null, annotations);

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

    private static boolean isLeafXMLElementNode(Element xmlElementNode) {
        return xmlElementNode.getChildNodes().getLength() == 1 &&
                xmlElementNode.getChildNodes().item(0).getNodeType() == org.w3c.dom.Node.TEXT_NODE;
    }

    private static AnnotationNode getXMLNameNode(String value) {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken("xmldata");
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

    private static AnnotationNode getXMLNamespaceNode(String prefix, String uri) {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken("xmldata");
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Namespace");
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        List<Node> mappingFields = new ArrayList<>();
        Token openBrace = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
        Token closeBrace = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        MinutiaeList emptyMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();

        IdentifierToken prefixFieldName = AbstractNodeFactory.createIdentifierToken("prefix");
        LiteralValueToken prefixLiteralToken =
                NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, String.format("\"%s\"", prefix),
                        emptyMinutiaeList, emptyMinutiaeList);
        BasicLiteralNode prefixValueExpr =
                NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, prefixLiteralToken);
        MappingFieldNode prefixMappingField =
                NodeFactory.createSpecificFieldNode(null, prefixFieldName, colon, prefixValueExpr);
        mappingFields.add(prefixMappingField);
        mappingFields.add(NodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
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

    private static AnnotationNode getXMLAttributeNode() {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken("xmldata");
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Attribute");
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        return NodeFactory.createAnnotationNode(atToken, annotReference, null);
    }
}

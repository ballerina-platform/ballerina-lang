/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
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

package io.ballerina.wsdltoballerina.recordgenerator;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.BasicField;
import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.ComplexField;
import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.EnumConstraint;
import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.Field;
import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.FieldConstraint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.wsdltoballerina.GeneratorConstants.ATTR_NAME;
import static io.ballerina.wsdltoballerina.GeneratorConstants.ATTR_VALUE;
import static io.ballerina.wsdltoballerina.GeneratorConstants.XML_DATA;
import static io.ballerina.wsdltoballerina.XMLSchemaParserUtils.escapeIdentifier;
import static io.ballerina.wsdltoballerina.XMLSchemaParserUtils.getBallerinaType;
import static io.ballerina.wsdltoballerina.XMLSchemaParserUtils.getBallerinaTypeToken;
import static io.ballerina.wsdltoballerina.recordgenerator.util.GeneratorUtils.updateField;

public class RecordGenerator {

    private boolean usesXmlData = false;

    public Map<String, NonTerminalNode> generateBallerinaTypes(List<Field> fields) {
        final Map<String, NonTerminalNode> typeToTypeDescNodes = new LinkedHashMap<>();
        for (Field field : fields) {
            if (field instanceof BasicField) {
                generateTypeDescriptor((BasicField) field, typeToTypeDescNodes);
            }
            if (field instanceof ComplexField) {
                generateRecordTypeDescriptors((ComplexField) field, typeToTypeDescNodes);
            }
        }
        return typeToTypeDescNodes;
    }

    public boolean hasXmlDataUsage() {
        return usesXmlData;
    }

    private void generateTypeDescriptor(BasicField basicField, Map<String, NonTerminalNode> typeToTypeDescNodes) {
        String type = basicField.getName();
        EnumConstraint enumConstraint = null;
        for (FieldConstraint constraint : basicField.getConstraints()) {
            if (constraint instanceof EnumConstraint) {
                enumConstraint = (EnumConstraint) constraint;
            }
        }

        if (enumConstraint != null && !enumConstraint.getEnumValues().isEmpty()) {
            TypeDescriptorNode enumTypeDescNode = generateEnumTypeDescriptor(enumConstraint.getEnumValues());
            typeToTypeDescNodes.put(type, enumTypeDescNode);
            return;
        }
        Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANYDATA_KEYWORD);
        NameReferenceNode anyDataNode = NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);
        TypeDescriptorNode anyDataTypeDescNode = NodeFactory.createTypeReferenceTypeDescNode(anyDataNode);
        typeToTypeDescNodes.put(type, anyDataTypeDescNode);
    }

    private void generateRecordTypeDescriptors(ComplexField complexField,
                                                 Map<String, NonTerminalNode> typeToTypeDescNodes) {
        if (complexField.isCyclicDep()) {
            return;
        }
        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
        Token bodyStartDelimiter = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);

        String type = complexField.getType();

        List<Node> recordFields = getRecordFields(complexField, typeToTypeDescNodes);
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFields);
        Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        typeToTypeDescNodes.put(type, recordTypeDescriptorNode);
    }

    private List<Node> getRecordFields(ComplexField complexField, Map<String, NonTerminalNode> typeToTypeDescNodes) {
        List<Field> fields = complexField.getFields();
        List<Node> recordFields = new ArrayList<>();

        // TODO: Have to handle when conflicting names are present properly, this is just a small fix,
        //  which will only handle if attribute field comes later,
        for (Field field : fields) {
            if (field instanceof BasicField basicFieldInstance) {
                List<String> collectedRecordFieldNames = recordFields.stream()
                        .map(node -> (RecordFieldNode) node)
                        .map(recNode -> recNode.fieldName().text())
                        .toList();
                String alternateName = null;
                if (collectedRecordFieldNames.contains(basicFieldInstance.getName())) {
                    alternateName = updateField(basicFieldInstance.getName());
                }
                RecordFieldNode recordFieldNode = getRecordFieldForBasicField(basicFieldInstance, alternateName);
                recordFields.add(recordFieldNode);
            } else if (field instanceof ComplexField complexFieldInstance) {
                RecordFieldNode recordFieldNode =
                        getRecordFieldForComplexField(complexFieldInstance, typeToTypeDescNodes);
                recordFields.add(recordFieldNode);
            }
        }

        String includedType = complexField.getIncludedType();
        if (includedType != null) {
            Token asteriskToken = AbstractNodeFactory.createToken(SyntaxKind.ASTERISK_TOKEN);
            Token typeName = AbstractNodeFactory.createIdentifierToken(includedType);
            Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
            TypeReferenceNode typeReferenceNode =
                    NodeFactory.createTypeReferenceNode(asteriskToken, typeName, semicolonToken);
            recordFields.add(typeReferenceNode);
        }
        return recordFields;
    }

    private RecordFieldNode getRecordFieldForBasicField(BasicField field, String alternateName) {
        List<AnnotationNode> annotations = new ArrayList<>();
        if (alternateName != null) {
            annotations.add(getXMLNameNode(field.getName()));
        }
        annotations.add(getXMLAttributeNode());
        NodeList<AnnotationNode> annotationNodes = AbstractNodeFactory.createNodeList(annotations);
        MetadataNode metadataNode = null;
        if (field.isAttributeField()) {
            metadataNode = NodeFactory.createMetadataNode(null, annotationNodes);
            this.usesXmlData = true;
        }
        String fieldRawType = getBallerinaType(field.getType());
        TypeDescriptorNode fieldType = getBallerinaTypeToken(fieldRawType);
        if (field.isArray()) {
            fieldType = getArrayTypeDescNodeFor(fieldType);
        }
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        TypeDescriptorNode optionalFieldType =
                NodeFactory.createOptionalTypeDescriptorNode(fieldType, questionMarkToken);
        IdentifierToken fieldName = AbstractNodeFactory
                .createIdentifierToken(escapeIdentifier(alternateName == null ? field.getName() : alternateName));
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createRecordFieldNode(metadataNode, null,
                field.isNullable() ? optionalFieldType : fieldType, fieldName,
                field.isRequired() ? null : questionMarkToken, semicolonToken);
    }

    private RecordFieldNode getRecordFieldForComplexField(ComplexField field,
                                                          Map<String, NonTerminalNode> typeToTypeDescNodes) {
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        String fieldRawType = field.getType();
        Token fieldTypeToken = AbstractNodeFactory.createIdentifierToken(fieldRawType);
        BasicLiteralNode fieldTypeLiteralNode =
                NodeFactory.createBasicLiteralNode(SyntaxKind.IDENTIFIER_TOKEN, fieldTypeToken);
        TypeDescriptorNode fieldType = NodeFactory.createSingletonTypeDescriptorNode(fieldTypeLiteralNode);
        if (field.isArray()) {
            fieldType = getArrayTypeDescNodeFor(fieldType);
        }
        TypeDescriptorNode optionalFieldType =
                NodeFactory.createOptionalTypeDescriptorNode(fieldType, questionMarkToken);
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(field.getName()));
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        generateRecordTypeDescriptors(field, typeToTypeDescNodes);
        return NodeFactory.createRecordFieldNode(null, null,
                field.isNullable() ? optionalFieldType : fieldType, fieldName,
                field.isRequired() ? null : questionMarkToken, semicolonToken);
    }

    private TypeDescriptorNode generateEnumTypeDescriptor(List<String> enumValues) {
        if (enumValues.size() == 1) {
            return getEnumValueTypeDescNode(enumValues.get(0));
        }

        Token pipeToken = NodeFactory.createToken(SyntaxKind.PIPE_TOKEN);
        TypeDescriptorNode leftTypeDescNode = getEnumValueTypeDescNode(enumValues.get(0));
        for (int i = 1; i < enumValues.size(); i++) {
            TypeDescriptorNode rightTypeDescNode = getEnumValueTypeDescNode(enumValues.get(i));
            leftTypeDescNode =
                    NodeFactory.createUnionTypeDescriptorNode(leftTypeDescNode, pipeToken, rightTypeDescNode);
        }
        return leftTypeDescNode;
    }

    private TypeDescriptorNode getEnumValueTypeDescNode(String enumValue) {
        Token valueToken =
                NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, "\"" + enumValue + "\"",
                        AbstractNodeFactory.createEmptyMinutiaeList(), AbstractNodeFactory.createEmptyMinutiaeList());
        BasicLiteralNode valueNode = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, valueToken);
        return NodeFactory.createSingletonTypeDescriptorNode(valueNode);
    }

    private ArrayTypeDescriptorNode getArrayTypeDescNodeFor(TypeDescriptorNode typeDescNode) {
        Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        ArrayDimensionNode arrayDimension = NodeFactory.createArrayDimensionNode(openSBracketToken, null,
                closeSBracketToken);
        NodeList<ArrayDimensionNode> arrayDimensions = NodeFactory.createNodeList(arrayDimension);

        return NodeFactory.createArrayTypeDescriptorNode(typeDescNode, arrayDimensions);
    }

    private AnnotationNode getXMLNameNode(String value) {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken(XML_DATA);
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken(ATTR_NAME);
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        Token openBrace = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
        Token closeBrace = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(ATTR_VALUE);
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

    private AnnotationNode getXMLAttributeNode() {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken(XML_DATA);
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Attribute");
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        return NodeFactory.createAnnotationNode(atToken, annotReference, null);
    }
}

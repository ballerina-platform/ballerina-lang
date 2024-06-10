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

package io.ballerina.xmltorecordconverter.util;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.identifier.Utils.escapeSpecialCharacters;
import static io.ballerina.identifier.Utils.unescapeUnicodeCodepoints;

/**
 * Util methods for XML to record converter.
 *
 * @since 2201.7.2
 */
public class ConverterUtils {

    private ConverterUtils() {}

    private static final String QUOTED_IDENTIFIER_PREFIX = "'";
    private static final String ESCAPE_NUMERIC_PATTERN = "\\b\\d.*";
    private static final List<String> KEYWORDS = SyntaxInfo.keywords();

    /**
     * This method returns the identifiers with special characters.
     *
     * @param identifier Identifier name.
     * @return {@link String} Special characters escaped identifier.
     */
    public static String escapeIdentifier(String identifier) {
        if (KEYWORDS.stream().anyMatch(identifier::equals)) {
            return "'" + identifier;
        } else {
            if (identifier.startsWith(QUOTED_IDENTIFIER_PREFIX)) {
                identifier = identifier.substring(1);
            }
            identifier = unescapeUnicodeCodepoints(identifier);
            identifier = escapeSpecialCharacters(identifier);
            if (identifier.matches(ESCAPE_NUMERIC_PATTERN)) {
                identifier = "\\" + identifier;
            }
            return identifier;
        }
    }

    /**
     * This method returns the SyntaxToken corresponding to the JsonPrimitive.
     *
     * @param value JsonPrimitive that has to be classified.
     * @return {@link Token} Classified Syntax Token.
     */
    public static Token getPrimitiveTypeName(String value) {
        if (isBoolean(value)) {
            return AbstractNodeFactory.createToken(SyntaxKind.BOOLEAN_KEYWORD);
        } else if (isInteger(value)) {
            return AbstractNodeFactory.createToken(SyntaxKind.INT_KEYWORD);
        } else if (isDouble(value)) {
            return AbstractNodeFactory.createToken(SyntaxKind.DECIMAL_KEYWORD);
        }
        return AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);
    }

    /**
     * This method extracts TypeDescriptorNodes within any UnionTypeDescriptorNodes or ParenthesisedTypeDescriptorNode.
     *
     * @param typeDescNodes List of Union and Parenthesised TypeDescriptorNodes
     * @return {@link List<TypeDescriptorNode>} Extracted SimpleNameReferenceNodes.
     */
    public static List<TypeDescriptorNode> extractTypeDescriptorNodes(List<TypeDescriptorNode> typeDescNodes) {
        List<TypeDescriptorNode> extractedTypeNames = new ArrayList<>();
        for (TypeDescriptorNode typeDescNode : typeDescNodes) {
            TypeDescriptorNode extractedTypeDescNode = extractParenthesisedTypeDescNode(typeDescNode);
            if (extractedTypeDescNode instanceof UnionTypeDescriptorNode unionTypeDescriptorNode) {
                List<TypeDescriptorNode> childTypeDescNodes =
                        List.of(unionTypeDescriptorNode.leftTypeDesc(),
                                unionTypeDescriptorNode.rightTypeDesc());
                addIfNotExist(extractedTypeNames, extractTypeDescriptorNodes(childTypeDescNodes));
            } else {
                addIfNotExist(extractedTypeNames, List.of(extractedTypeDescNode));
            }
        }
        return extractedTypeNames;
    }

    /**
     * This method returns the sorted TypeDescriptorNode list.
     *
     * @param typeDescriptorNodes List of TypeDescriptorNodes has to be sorted.
     * @return {@link List<TypeDescriptorNode>} The sorted TypeDescriptorNode list.
     */
    public static List<TypeDescriptorNode> sortTypeDescriptorNodes(List<TypeDescriptorNode> typeDescriptorNodes) {
        List<TypeDescriptorNode> nonArrayNodes = typeDescriptorNodes.stream()
                .filter(node -> !(node instanceof ArrayTypeDescriptorNode)).collect(Collectors.toList());
        List<TypeDescriptorNode> arrayNodes = typeDescriptorNodes.stream()
                .filter(node -> (node instanceof ArrayTypeDescriptorNode)).collect(Collectors.toList());
        List<TypeDescriptorNode> membersOfArrayNodes = arrayNodes.stream()
                .map(node -> extractArrayTypeDescNode((ArrayTypeDescriptorNode) node)).toList();
        nonArrayNodes.removeIf(node ->
                membersOfArrayNodes.stream().map(Node::toSourceCode).toList().contains(node.toSourceCode()));
        nonArrayNodes.sort(Comparator.comparing(TypeDescriptorNode::toSourceCode));
        arrayNodes.sort((node1, node2) -> {
            ArrayTypeDescriptorNode arrayNode1 = (ArrayTypeDescriptorNode) node1;
            ArrayTypeDescriptorNode arrayNode2 = (ArrayTypeDescriptorNode) node2;
            return getNumberOfDimensions(arrayNode1).equals(getNumberOfDimensions(arrayNode2)) ?
                    (arrayNode1).memberTypeDesc().toSourceCode()
                            .compareTo((arrayNode2).memberTypeDesc().toSourceCode()) :
                    getNumberOfDimensions(arrayNode1) - getNumberOfDimensions(arrayNode2);
        });
        return Stream.concat(nonArrayNodes.stream(), arrayNodes.stream()).collect(Collectors.toList());
    }

    /**
     * This method returns a list of TypeDescriptorNodes extracted from a UnionTypeDescriptorNode.
     *
     * @param typeDescNode UnionTypeDescriptorNode for which that has to be extracted.
     * @return {@link List<TypeDescriptorNode>} The list of extracted TypeDescriptorNodes.
     */
    public static List<TypeDescriptorNode> extractUnionTypeDescNode(TypeDescriptorNode typeDescNode) {
        List<TypeDescriptorNode> extractedTypeDescNodes = new ArrayList<>();
        TypeDescriptorNode extractedTypeDescNode = typeDescNode;
        if (typeDescNode.kind().equals(SyntaxKind.PARENTHESISED_TYPE_DESC)) {
            extractedTypeDescNode = extractParenthesisedTypeDescNode(typeDescNode);
        }
        if (extractedTypeDescNode.kind().equals(SyntaxKind.UNION_TYPE_DESC)) {
            UnionTypeDescriptorNode unionTypeDescNode = (UnionTypeDescriptorNode) extractedTypeDescNode;
            TypeDescriptorNode leftTypeDescNode = unionTypeDescNode.leftTypeDesc();
            TypeDescriptorNode rightTypeDescNode = unionTypeDescNode.rightTypeDesc();
            extractedTypeDescNodes.addAll(extractUnionTypeDescNode(leftTypeDescNode));
            extractedTypeDescNodes.addAll(extractUnionTypeDescNode(rightTypeDescNode));
        } else {
            extractedTypeDescNodes.add(extractedTypeDescNode);
        }
        return extractedTypeDescNodes;
    }

    /**
     * This method returns the number of dimensions of an ArrayTypeDescriptorNode.
     *
     * @param arrayNode ArrayTypeDescriptorNode for which the no. of dimensions has to be calculated.
     * @return {@link Integer} The total no. of dimensions of the ArrayTypeDescriptorNode.
     */
    public static Integer getNumberOfDimensions(ArrayTypeDescriptorNode arrayNode) {
        int totalDimensions = arrayNode.dimensions().size();
        if (arrayNode.memberTypeDesc() instanceof ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
            totalDimensions += getNumberOfDimensions(arrayTypeDescriptorNode);
        }
        return totalDimensions;
    }

    private static TypeDescriptorNode extractArrayTypeDescNode(ArrayTypeDescriptorNode arrayTypeDescNode) {
        if (arrayTypeDescNode.memberTypeDesc() instanceof ArrayTypeDescriptorNode arrayTypeDescriptorNode) {
            return extractArrayTypeDescNode(arrayTypeDescriptorNode);
        }
        return arrayTypeDescNode.memberTypeDesc();
    }

    private static TypeDescriptorNode extractParenthesisedTypeDescNode(TypeDescriptorNode typeDescNode) {
        if (typeDescNode instanceof ParenthesisedTypeDescriptorNode parenthesisedTypeDescriptorNode) {
            return extractParenthesisedTypeDescNode(parenthesisedTypeDescriptorNode.typedesc());
        }
        return typeDescNode;
    }

    private static void addIfNotExist(List<TypeDescriptorNode> typeDescNodes,
                                      List<TypeDescriptorNode> typeDescNodesToBeInserted) {
        for (TypeDescriptorNode typeDescNodeToBeInserted : typeDescNodesToBeInserted) {
            if (typeDescNodes.stream().noneMatch(typeDescNode -> typeDescNode.toSourceCode()
                    .equals(typeDescNodeToBeInserted.toSourceCode()))) {
                typeDescNodes.add(typeDescNodeToBeInserted);
            }
        }
    }

    private static boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private static boolean isInteger(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

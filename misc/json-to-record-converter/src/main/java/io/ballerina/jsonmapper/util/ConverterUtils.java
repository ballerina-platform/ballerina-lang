/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.jsonmapper.util;

import com.google.gson.JsonPrimitive;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.identifier.Utils.escapeSpecialCharacters;
import static io.ballerina.identifier.Utils.unescapeUnicodeCodepoints;

/**
 * Util methods for JSON to record direct converter.
 *
 * @since 2201.2.0
 */
public final class ConverterUtils {

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
            // TODO: Escape Special Character does not escapes backslashes. Refer - https://github.com/ballerina-platform/ballerina-lang/issues/36912
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
    public static Token getPrimitiveTypeName(JsonPrimitive value) {
        if (value.isString()) {
            return AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);
        } else if (value.isBoolean()) {
            return AbstractNodeFactory.createToken(SyntaxKind.BOOLEAN_KEYWORD);
        } else if (value.isNumber()) {
            String strValue = value.getAsNumber().toString();
            if (strValue.contains(".")) {
                return AbstractNodeFactory.createToken(SyntaxKind.DECIMAL_KEYWORD);
            } else {
                return AbstractNodeFactory.createToken(SyntaxKind.INT_KEYWORD);
            }
        }
        return AbstractNodeFactory.createToken(SyntaxKind.ANYDATA_KEYWORD);
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
     * This method returns the number of dimensions of an ArrayTypeDescriptorNode.
     *
     * @param arrayNode ArrayTypeDescriptorNode for which the no. of dimensions has to be calculated.
     * @return {@link Integer} The total no. of dimensions of the ArrayTypeDescriptorNode.
     */
    private static Integer getNumberOfDimensions(ArrayTypeDescriptorNode arrayNode) {
        int totalDimensions = arrayNode.dimensions().size();
        if (arrayNode.memberTypeDesc() instanceof ArrayTypeDescriptorNode) {
            totalDimensions += getNumberOfDimensions((ArrayTypeDescriptorNode) arrayNode.memberTypeDesc());
        }
        return totalDimensions;
    }
}

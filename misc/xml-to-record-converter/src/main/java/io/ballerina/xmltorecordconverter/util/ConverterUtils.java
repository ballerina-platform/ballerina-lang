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

package io.ballerina.xmltorecordconverter.util;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.ballerina.identifier.Utils.escapeSpecialCharacters;
import static io.ballerina.identifier.Utils.unescapeUnicodeCodepoints;

/**
 * Util methods for XML to record converter.
 *
 * @since 2201.2.0
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

    public static List<TypeDescriptorNode> sortTypeDescNodes(List<TypeDescriptorNode> typeDescNodes) {
        typeDescNodes.sort(Comparator.comparing(TypeDescriptorNode::toSourceCode));
        return typeDescNodes;
    }

    public static List<TypeDescriptorNode> extractUnionTypeDescNode(UnionTypeDescriptorNode unionTypeDescNode) {
        List<TypeDescriptorNode> extractedTypeDescNodes = new ArrayList<>();
        TypeDescriptorNode leftTypeDescNode = unionTypeDescNode.leftTypeDesc();
        TypeDescriptorNode rightTypeDescNode = unionTypeDescNode.rightTypeDesc();
        if (leftTypeDescNode.kind().equals(SyntaxKind.UNION_TYPE_DESC)) {
            extractedTypeDescNodes.addAll(extractUnionTypeDescNode((UnionTypeDescriptorNode) leftTypeDescNode));
        } else {
            extractedTypeDescNodes.add(leftTypeDescNode);
        }
        if (rightTypeDescNode.kind().equals(SyntaxKind.UNION_TYPE_DESC)) {
            extractedTypeDescNodes.addAll(extractUnionTypeDescNode((UnionTypeDescriptorNode) rightTypeDescNode));
        } else {
            extractedTypeDescNodes.add(rightTypeDescNode);
        }
        return extractedTypeDescNodes;
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

    private static boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
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

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

package io.ballerina.wsdltoballerina;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.identifier.Utils.escapeSpecialCharacters;
import static io.ballerina.identifier.Utils.unescapeUnicodeCodepoints;

public class XMLSchemaParserUtils {
    private static final String QUOTED_IDENTIFIER_PREFIX = "'";
    private static final String ESCAPE_NUMERIC_PATTERN = "\\b\\d.*";
    private static final List<String> KEYWORDS = SyntaxInfo.keywords();
    private static final Map<String, String> xsdToBallerinaMap = new HashMap<>();

    static {
        xsdToBallerinaMap.put("byte", "int");
        xsdToBallerinaMap.put("decimal", "decimal");
        xsdToBallerinaMap.put("int", "int");
        xsdToBallerinaMap.put("integer", "int");
        xsdToBallerinaMap.put("long", "int");
        xsdToBallerinaMap.put("negativeInteger", "int");
        xsdToBallerinaMap.put("nonNegativeInteger", "int");
        xsdToBallerinaMap.put("nonPositiveInteger", "int");
        xsdToBallerinaMap.put("positiveInteger", "int");
        xsdToBallerinaMap.put("short", "int");
        xsdToBallerinaMap.put("unsignedLong", "int");
        xsdToBallerinaMap.put("unsignedInt", "int");
        xsdToBallerinaMap.put("unsignedShort", "int");
        xsdToBallerinaMap.put("unsignedByte", "int");

        xsdToBallerinaMap.put("ENTITIES", "string");
        xsdToBallerinaMap.put("ENTITY", "string");
        xsdToBallerinaMap.put("ID", "string");
        xsdToBallerinaMap.put("IDREF", "string");
        xsdToBallerinaMap.put("IDREFS", "string");
        xsdToBallerinaMap.put("language", "string");
        xsdToBallerinaMap.put("Name", "string");
        xsdToBallerinaMap.put("NCName", "string");
        xsdToBallerinaMap.put("NMTOKEN", "string");
        xsdToBallerinaMap.put("NMTOKENS", "string");
        xsdToBallerinaMap.put("normalizedString", "string");
        xsdToBallerinaMap.put("QName", "string");
        xsdToBallerinaMap.put("string", "string");
        xsdToBallerinaMap.put("token", "string");

        xsdToBallerinaMap.put("date", "string");
        xsdToBallerinaMap.put("dateTime", "string");
        xsdToBallerinaMap.put("duration", "string");
        xsdToBallerinaMap.put("gDay", "string");
        xsdToBallerinaMap.put("gMonth", "string");
        xsdToBallerinaMap.put("gMonthDay", "string");
        xsdToBallerinaMap.put("gYear", "string");
        xsdToBallerinaMap.put("gYearMonth", "string");
        xsdToBallerinaMap.put("time", "string");

        // TODO: Enable commented types later.
        xsdToBallerinaMap.put("anyURI", "string");
        xsdToBallerinaMap.put("base64Binary", "byte[]");
        xsdToBallerinaMap.put("boolean", "boolean");
        xsdToBallerinaMap.put("double", "float");
        xsdToBallerinaMap.put("float", "float");
        xsdToBallerinaMap.put("hexBinary", "byte[]");
        xsdToBallerinaMap.put("NOTATION", "string");
        xsdToBallerinaMap.put("QName", "string");
    }

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

    public static boolean isXSDDataType(String dataType) {
        return xsdToBallerinaMap.containsKey(dataType);
    }

    public static String getBallerinaType(String xsdType) {
        return xsdToBallerinaMap.getOrDefault(xsdType, xsdType);
    }

    public static TypeDescriptorNode getBallerinaTypeToken(String typeName) {
        Token typeToken;
        BasicLiteralNode basicLiteralNode;
        TypeDescriptorNode typeDescNode;
        switch (typeName) {
            case "int":
                typeToken = AbstractNodeFactory.createToken(SyntaxKind.INT_KEYWORD);
                basicLiteralNode = NodeFactory.createBasicLiteralNode(SyntaxKind.INT_KEYWORD, typeToken);
                typeDescNode = NodeFactory.createSingletonTypeDescriptorNode(basicLiteralNode);
                break;
            case "decimal":
                typeToken = AbstractNodeFactory.createToken(SyntaxKind.DECIMAL_KEYWORD);
                basicLiteralNode = NodeFactory.createBasicLiteralNode(SyntaxKind.DECIMAL_KEYWORD, typeToken);
                typeDescNode = NodeFactory.createSingletonTypeDescriptorNode(basicLiteralNode);
                break;
            case "boolean":
                typeToken = AbstractNodeFactory.createToken(SyntaxKind.BOOLEAN_KEYWORD);
                basicLiteralNode = NodeFactory.createBasicLiteralNode(SyntaxKind.BOOLEAN_KEYWORD, typeToken);
                typeDescNode = NodeFactory.createSingletonTypeDescriptorNode(basicLiteralNode);
                break;
            case "float":
                typeToken = AbstractNodeFactory.createToken(SyntaxKind.FLOAT_KEYWORD);
                basicLiteralNode = NodeFactory.createBasicLiteralNode(SyntaxKind.FLOAT_KEYWORD, typeToken);
                typeDescNode = NodeFactory.createSingletonTypeDescriptorNode(basicLiteralNode);
                break;
            case "string":
                typeToken = AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);
                basicLiteralNode = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_KEYWORD, typeToken);
                typeDescNode = NodeFactory.createSingletonTypeDescriptorNode(basicLiteralNode);
                break;
            case "byte[]":
                typeToken = AbstractNodeFactory.createToken(SyntaxKind.BYTE_KEYWORD);
                basicLiteralNode = NodeFactory.createBasicLiteralNode(SyntaxKind.BYTE_KEYWORD, typeToken);
                typeDescNode = NodeFactory.createSingletonTypeDescriptorNode(basicLiteralNode);
                Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
                Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                ArrayDimensionNode arrayDimension = NodeFactory.createArrayDimensionNode(openSBracketToken, null,
                        closeSBracketToken);
                NodeList<ArrayDimensionNode> arrayDimensions = NodeFactory.createNodeList(arrayDimension);
                return NodeFactory.createArrayTypeDescriptorNode(typeDescNode, arrayDimensions);
        default:
            typeToken = AbstractNodeFactory.createIdentifierToken(typeName);
            basicLiteralNode = NodeFactory.createBasicLiteralNode(SyntaxKind.IDENTIFIER_TOKEN, typeToken);
            typeDescNode = NodeFactory.createSingletonTypeDescriptorNode(basicLiteralNode);
            break;
        };
        return typeDescNode;
    }
}

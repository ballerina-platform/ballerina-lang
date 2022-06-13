/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.converters;

import com.google.gson.JsonPrimitive;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.converters.util.ConverterUtils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.*;
import java.util.concurrent.Callable;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * API for converting JSON string to Ballerina Records directly.
 *
 * @since 2.0.0
 */
public class JsonToRecordDirectConverter {

    private JsonToRecordDirectConverter() {}

    public static JsonToRecordResponse convert(String jsonString) throws FormatterException {
        Map<String, NonTerminalNode> typeDefinitionNodes = new LinkedHashMap<>();
        generateRecords(JsonParser.parseString(jsonString).getAsJsonObject(), null, typeDefinitionNodes);
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        JsonToRecordResponse response = new JsonToRecordResponse();

        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
                new ArrayList(typeDefinitionNodes.values()));
        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
        response.setCodeBlock(Formatter.format(modulePartNode.syntaxTree()).toSourceCode());

        return response;
    }

    private static void generateRecords(JsonObject jsonObject, String recordName,
                                        Map<String, NonTerminalNode> typeDefinitionNodes) {
        Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
        IdentifierToken typeName =
                AbstractNodeFactory.createIdentifierToken(recordName == null ? "NewRecord" : recordName);
        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
        Token bodyStartDelimiter = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);

        List<Node> recordFieldList = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            Node recordField = getRecordField(entry);
            recordFieldList.add(recordField);

            if (entry.getValue().isJsonObject()) {
                String elementKey = entry.getKey();
                String type = elementKey.substring(0, 1).toUpperCase() + elementKey.substring(1);
                generateRecords(entry.getValue().getAsJsonObject(), type, typeDefinitionNodes);
            } else if (entry.getValue().isJsonArray()) {
                Iterator<JsonElement> iterator = entry.getValue().getAsJsonArray().iterator();
                while (iterator.hasNext()) {
                    JsonElement element = iterator.next();
                    if (element.isJsonObject()) {
                        String elementKey = entry.getKey();
                        String type = elementKey.substring(0, 1).toUpperCase() + elementKey.substring(1) + "Item";
                        generateRecords(element.getAsJsonObject(), type, typeDefinitionNodes);
                    }
                    break;
                }
            }
        }

        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
        Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);

        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                null, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
        typeDefinitionNodes.put(recordName, typeDefinitionNode);
    }

    private static Node getRecordField(Map.Entry<String, JsonElement> entry) {
        TypeDescriptorNode fieldTypeName;

        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(entry.getKey().trim());
        Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        if (entry.getValue().isJsonArray()) {
            Iterator<JsonElement> iterator = entry.getValue().getAsJsonArray().iterator();
            Token typeName = null;
            while (iterator.hasNext()) {
                JsonElement element = iterator.next();
                if (element.isJsonPrimitive()) {
                    Token tempTypeName = ConverterUtils.getPrimitiveTypeName(element.getAsJsonPrimitive());
                    if (typeName == null) {
                        typeName = tempTypeName;
                    } else if (typeName.equals(tempTypeName)) {
                        typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
                    }
                } else if (element.isJsonNull()) {
                    typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
                } else if (element.isJsonObject()) {
                    if (typeName == null) {
                        String elementKey = entry.getKey();
                        String type = elementKey.substring(0, 1).toUpperCase() + elementKey.substring(1) + "Item";
                        typeName = AbstractNodeFactory.createIdentifierToken(type);
                    }
                }
            }
            NodeList<ArrayDimensionNode> arrayDimensions = NodeFactory.createEmptyNodeList();

            fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
            ArrayDimensionNode arrayDimension = NodeFactory.createArrayDimensionNode(openSBracketToken, null,
                    closeSBracketToken);
            arrayDimensions = arrayDimensions.add(arrayDimension);

            ArrayTypeDescriptorNode arrayTypeName =
                    NodeFactory.createArrayTypeDescriptorNode(fieldTypeName, arrayDimensions);

            RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null,
                    null, arrayTypeName, fieldName, null, semicolonToken);

            return recordFieldNode;
        } else {
            if (entry.getValue().isJsonPrimitive()) {
                Token typeName = ConverterUtils.getPrimitiveTypeName(entry.getValue().getAsJsonPrimitive());
                fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
            } else if (entry.getValue().isJsonObject()) {
                String elementKey = entry.getKey();
                String type = elementKey.substring(0, 1).toUpperCase() + elementKey.substring(1);
                Token typeName = AbstractNodeFactory.createIdentifierToken(type);
                fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
            } else {
                Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
                fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
            }

            RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    fieldTypeName, fieldName, entry.getValue().isJsonNull() ? questionMarkToken : null, semicolonToken);

            return recordFieldNode;
        }
    }
}

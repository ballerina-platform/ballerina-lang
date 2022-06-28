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

package io.ballerina.converters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.converters.diagnostic.DiagnosticMessages;
import io.ballerina.converters.diagnostic.JsonToRecordDirectConverterDiagnostic;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.ballerina.converters.diagnostic.DiagnosticUtils.getDiagnosticResponse;
import static io.ballerina.converters.util.ConverterUtils.escapeIdentifier;
import static io.ballerina.converters.util.ConverterUtils.getPrimitiveTypeName;
import static io.ballerina.converters.util.ConverterUtils.sortTypeDescriptorNodes;
import static io.ballerina.converters.util.ListOperationUtils.difference;
import static io.ballerina.converters.util.ListOperationUtils.intersection;

/**
 * API for converting JSON string to Ballerina Records directly.
 *
 * @since 2.0.0
 */
public class JsonToRecordDirectConverter {

    private JsonToRecordDirectConverter() {}

    public static JsonToRecordResponse convert(String jsonString, String recordName, boolean isRecordTypeDesc,
                                               boolean isClosed) {
        Map<String, NonTerminalNode> recordToTypeDescNodes = new LinkedHashMap<>();
        Map<String, JsonElement> jsonFieldToElements = new LinkedHashMap<>();
        List<JsonToRecordDirectConverterDiagnostic> diagnostics = new ArrayList<>();
        JsonToRecordResponse response = new JsonToRecordResponse();

        try {
            JsonElement parsedJson = JsonParser.parseString(jsonString);
            if (parsedJson.isJsonObject()) {
                generateRecords(parsedJson.getAsJsonObject(), recordName, isClosed, isRecordTypeDesc,
                        recordToTypeDescNodes, jsonFieldToElements);
            } else if (parsedJson.isJsonArray()) {
                JsonObject object = new JsonObject();
                object.add(recordName == null ? "newRecord" : StringUtils.uncapitalize(recordName), parsedJson);
                generateRecords(object, recordName, isClosed, isRecordTypeDesc,
                        recordToTypeDescNodes, jsonFieldToElements);
            } else {
                DiagnosticMessages message = DiagnosticMessages.jsonToRecordConverter101(null);
                return getDiagnosticResponse(message, diagnostics, response, null);
            }
        } catch (JsonSyntaxException e) {
            DiagnosticMessages message = DiagnosticMessages.jsonToRecordConverter100(new String[]{e.getMessage()});
            return getDiagnosticResponse(message, diagnostics, response, null);
        }

        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        List<TypeDefinitionNode> typeDefNodes = recordToTypeDescNodes.entrySet().stream()
                .map(entry -> {
                    Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
                    IdentifierToken typeName = AbstractNodeFactory
                            .createIdentifierToken(escapeIdentifier(entry.getKey()));
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    return NodeFactory.createTypeDefinitionNode(null, null, typeKeyWord, typeName,
                            entry.getValue(), semicolon);
                }).collect(Collectors.toList());
        TypeDefinitionNode lastTypeDefNode = typeDefNodes.get(typeDefNodes.size() - 1);
        NodeList<ModuleMemberDeclarationNode> moduleMembers = isRecordTypeDesc ?
                AbstractNodeFactory.createNodeList(lastTypeDefNode) :
                AbstractNodeFactory.createNodeList(new ArrayList<>(typeDefNodes));

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
        try {
            response.setCodeBlock(Formatter.format(modulePartNode.syntaxTree()).toSourceCode());
            return response;
        } catch (FormatterException e) {
            DiagnosticMessages message = DiagnosticMessages.jsonToRecordConverter102(null);
            return getDiagnosticResponse(message, diagnostics, response, null);
        }
    }

    private static void generateRecords(JsonObject jsonObject, String recordName, boolean isClosed,
                                        boolean isRecordTypeDesc, Map<String, NonTerminalNode> recordToTypeDescNodes,
                                        Map<String, JsonElement> jsonNodes) {
        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
        Token bodyStartDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.OPEN_BRACE_PIPE_TOKEN :
                SyntaxKind.OPEN_BRACE_TOKEN);

        List<Node> recordFieldList = new ArrayList<>();
        if (recordToTypeDescNodes.containsKey(recordName)) {
            RecordTypeDescriptorNode lastRecordTypeDescriptorNode =
                    (RecordTypeDescriptorNode) recordToTypeDescNodes.get(recordName);
            List<RecordFieldNode> lastRecordFieldList = lastRecordTypeDescriptorNode.fields().stream()
                    .map(node -> (RecordFieldNode) node).collect(Collectors.toList());
            Map<String, RecordFieldNode> lastRecordFieldMap = lastRecordFieldList.stream()
                    .collect(Collectors.toMap(node -> node.fieldName().text(), Function.identity()));

            Map<String, RecordFieldNode> newRecordFieldMap = jsonObject.entrySet().stream()
                    .map(entry -> (RecordFieldNode) getRecordField(entry, recordToTypeDescNodes, isRecordTypeDesc,
                            null))
                    .collect(Collectors.toList()).stream()
                    .collect(Collectors.toMap(node -> node.fieldName().text(), Function.identity()));

            Map<String, RecordFieldNode> intersectingRecordFields = intersection(lastRecordFieldMap, newRecordFieldMap);
            Map<String, RecordFieldNode> differencingRecordFields = difference(lastRecordFieldMap, newRecordFieldMap);

            for (Map.Entry<String, RecordFieldNode> entry : intersectingRecordFields.entrySet()) {

                Boolean isOptional = entry.getValue().questionMarkToken().isPresent();
                Map.Entry<String, JsonElement> jsonEntry =
                        new AbstractMap.SimpleEntry<>(entry.getKey(), jsonNodes.get(entry.getKey()));
                Node recordField = getRecordField(jsonEntry, recordToTypeDescNodes, isRecordTypeDesc, isOptional);
                recordFieldList.add(recordField);
            }

            for (Map.Entry<String, RecordFieldNode> entry : differencingRecordFields.entrySet()) {
                Map.Entry<String, JsonElement> jsonEntry =
                        new AbstractMap.SimpleEntry<>(entry.getKey(), jsonNodes.get(entry.getKey()));
                Node recordField = getRecordField(jsonEntry, recordToTypeDescNodes, isRecordTypeDesc, true);
                recordFieldList.add(recordField);
            }
        } else {
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    String elementKey = entry.getKey();
                    String type = StringUtils.capitalize(elementKey);
                    generateRecords(entry.getValue().getAsJsonObject(), type, isClosed, isRecordTypeDesc,
                            recordToTypeDescNodes, jsonNodes);
                } else if (entry.getValue().isJsonArray()) {
                    for (JsonElement element : entry.getValue().getAsJsonArray()) {
                        if (element.isJsonObject()) {
                            String elementKey = entry.getKey();
                            String type = StringUtils.capitalize(elementKey) + "Item";
                            generateRecords(element.getAsJsonObject(), type, isClosed, isRecordTypeDesc,
                                    recordToTypeDescNodes, jsonNodes);
                            break;
                        }
                    }
                }

                jsonNodes.put(entry.getKey(), entry.getValue());
                Node recordField = getRecordField(entry, recordToTypeDescNodes, isRecordTypeDesc, null);
                recordFieldList.add(recordField);
            }
        }


        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);

        Token bodyEndDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.CLOSE_BRACE_PIPE_TOKEN :
                SyntaxKind.CLOSE_BRACE_TOKEN);

        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        recordToTypeDescNodes.put(recordName == null ? "NewRecord" : recordName, recordTypeDescriptorNode);
    }

    private static Node getRecordField(Map.Entry<String, JsonElement> entry,
                                       Map<String, NonTerminalNode> recordToTypeDescNodes, boolean isRecordTypeDesc,
                                       Boolean optionalField) {
        Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
        TypeDescriptorNode fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(entry.getKey().trim()));
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        Token optionalFieldToken = optionalField != null ? optionalField ? questionMarkToken : null : null;
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                fieldTypeName, fieldName, questionMarkToken, semicolonToken);


        if (entry.getValue().isJsonPrimitive()) {
            typeName = getPrimitiveTypeName(entry.getValue().getAsJsonPrimitive());
            fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
            recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    fieldTypeName, fieldName,
                    optionalFieldToken, semicolonToken);
        } else if (entry.getValue().isJsonObject()) {
            String elementKey = entry.getKey();
            String type = StringUtils.capitalize(elementKey);
            typeName = AbstractNodeFactory.createIdentifierToken(type);
            fieldTypeName = isRecordTypeDesc ? (TypeDescriptorNode) recordToTypeDescNodes.get(type) :
                    NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
            recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    fieldTypeName, fieldName,
                    optionalFieldToken, semicolonToken);
        } else if (entry.getValue().isJsonArray()) {
            Map.Entry<String, JsonArray> jsonArrayEntry =
                    new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getAsJsonArray());
            ArrayTypeDescriptorNode arrayTypeName =
                    createArrayTypeDescriptorNode(jsonArrayEntry, recordToTypeDescNodes, isRecordTypeDesc);

            recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    arrayTypeName, fieldName,
                    optionalFieldToken, semicolonToken);
        }
        return recordFieldNode;
    }

    private static ArrayTypeDescriptorNode createArrayTypeDescriptorNode(
            Map.Entry<String, JsonArray> entry, Map<String, NonTerminalNode> recordToTypeDescNodes,
            boolean isRecordTypeDesc) {
        Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);

        Iterator<JsonElement> iterator = entry.getValue().iterator();
        List<TypeDescriptorNode> typeDescriptorNodes = new ArrayList<>();
        while (iterator.hasNext()) {
            JsonElement element = iterator.next();
            if (element.isJsonPrimitive()) {
                Token tempTypeName = getPrimitiveTypeName(element.getAsJsonPrimitive());
                TypeDescriptorNode tempTypeNode = NodeFactory.createBuiltinSimpleNameReferenceNode(null, tempTypeName);
                if (!typeDescriptorNodes.stream().map(Node::toSourceCode)
                        .collect(Collectors.toList()).contains(tempTypeNode.toSourceCode())) {
                    typeDescriptorNodes.add(tempTypeNode);
                }
            } else if (element.isJsonNull()) {
                Token tempTypeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
                TypeDescriptorNode tempTypeNode = NodeFactory.createBuiltinSimpleNameReferenceNode(null, tempTypeName);
                if (!typeDescriptorNodes.stream().map(Node::toSourceCode)
                        .collect(Collectors.toList()).contains(tempTypeNode.toSourceCode())) {
                    typeDescriptorNodes.add(tempTypeNode);
                }
            } else if (element.isJsonObject()) {
                String elementKey = entry.getKey();
                String type = StringUtils.capitalize(elementKey) + "Item";
                Token tempTypeName = AbstractNodeFactory.createIdentifierToken(type);

                TypeDescriptorNode tempTypeNode = isRecordTypeDesc ?
                        (TypeDescriptorNode) recordToTypeDescNodes.get(type) :
                        NodeFactory.createBuiltinSimpleNameReferenceNode(null, tempTypeName);
                if (!typeDescriptorNodes.stream().map(Node::toSourceCode)
                        .collect(Collectors.toList()).contains(tempTypeNode.toSourceCode())) {
                    typeDescriptorNodes.add(tempTypeNode);
                }
            } else if (element.isJsonArray()) {
                Map.Entry<String, JsonArray> arrayEntry =
                        new AbstractMap.SimpleEntry<>(entry.getKey(), element.getAsJsonArray());
                TypeDescriptorNode tempTypeNode =
                        createArrayTypeDescriptorNode(arrayEntry, recordToTypeDescNodes, isRecordTypeDesc);
                if (!typeDescriptorNodes.stream().map(Node::toSourceCode)
                        .collect(Collectors.toList()).contains(tempTypeNode.toSourceCode())) {
                    typeDescriptorNodes.add(tempTypeNode);
                }
            }
        }

        List<TypeDescriptorNode> typeDescriptorNodesSorted = sortTypeDescriptorNodes(typeDescriptorNodes);

        TypeDescriptorNode fieldTypeName = createUnionTypeDescriptorNode(typeDescriptorNodesSorted);

        NodeList<ArrayDimensionNode> arrayDimensions = NodeFactory.createEmptyNodeList();
        ArrayDimensionNode arrayDimension = NodeFactory.createArrayDimensionNode(openSBracketToken, null,
                closeSBracketToken);
        arrayDimensions = arrayDimensions.add(arrayDimension);

        return NodeFactory.createArrayTypeDescriptorNode(fieldTypeName, arrayDimensions);
    }

    private static TypeDescriptorNode createUnionTypeDescriptorNode(List<TypeDescriptorNode> typeNames) {
        if (typeNames.size() == 0) {
            Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
            return NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
        } else if (typeNames.size() == 1) {
            return typeNames.get(0);
        }
        Token pipeToken = NodeFactory.createToken(SyntaxKind.PIPE_TOKEN);
        UnionTypeDescriptorNode unionTypeDescriptorNode = null;
        for (int i = 0; i < typeNames.size() - 1; i++) {
            TypeDescriptorNode leftTypeDesc =
                    unionTypeDescriptorNode == null ? typeNames.get(i) : unionTypeDescriptorNode;
            TypeDescriptorNode rightTypeDesc = typeNames.get(i + 1);
            unionTypeDescriptorNode =
                    NodeFactory.createUnionTypeDescriptorNode(leftTypeDesc, pipeToken, rightTypeDesc);
        }
        Token openParenToken = NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        Token closeParenToken = NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        return NodeFactory.createParenthesisedTypeDescriptorNode(openParenToken,
                unionTypeDescriptorNode, closeParenToken);
    }
}

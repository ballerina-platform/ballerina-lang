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

package io.ballerina.jsonmapper;

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
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.jsonmapper.diagnostic.DiagnosticMessage;
import io.ballerina.jsonmapper.diagnostic.DiagnosticUtils;
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

import static io.ballerina.jsonmapper.util.ConverterUtils.escapeIdentifier;
import static io.ballerina.jsonmapper.util.ConverterUtils.getPrimitiveTypeName;
import static io.ballerina.jsonmapper.util.ConverterUtils.sortTypeDescriptorNodes;
import static io.ballerina.jsonmapper.util.ListOperationUtils.difference;
import static io.ballerina.jsonmapper.util.ListOperationUtils.intersection;

/**
 * APIs for direct conversion from JSON strings to Ballerina records.
 *
 * @since 2201.2.0
 */
public class JsonToRecordMapper {

    private JsonToRecordMapper() {}

    private static final String NEW_RECORD_NAME = "NewRecord";
    private static final String ARRAY_RECORD_SUFFIX = "Item";

    /**
     * This method returns the Ballerina code for the provided JSON value or the diagnostics.
     *
     * @param jsonString JSON string of the JSON value to be converted to Ballerina record
     * @param recordName Name of the generated record
     * @param isRecordTypeDesc To denote final record, a record type descriptor (In line records)
     * @param isClosed To denote whether the response record is closed or not
     * @return {@link JsonToRecordResponse} Ballerina code block or the Diagnostics
     */
    public static JsonToRecordResponse convert(String jsonString, String recordName, boolean isRecordTypeDesc,
                                               boolean isClosed) {
        Map<String, NonTerminalNode> recordToTypeDescNodes = new LinkedHashMap<>();
        Map<String, JsonElement> jsonFieldToElements = new LinkedHashMap<>();
        List<DiagnosticMessage> diagnosticMessages = new ArrayList<>();
        JsonToRecordResponse response = new JsonToRecordResponse();

        try {
            JsonElement parsedJson = JsonParser.parseString(jsonString);
            if (parsedJson.isJsonObject()) {
                generateRecords(parsedJson.getAsJsonObject(), recordName, isRecordTypeDesc, isClosed,
                        recordToTypeDescNodes, jsonFieldToElements, diagnosticMessages);
            } else if (parsedJson.isJsonArray()) {
                JsonObject object = new JsonObject();
                object.add(((recordName == null) || recordName.equals("")) ? StringUtils.uncapitalize(NEW_RECORD_NAME) :
                        StringUtils.uncapitalize(recordName), parsedJson);
                generateRecords(object, recordName, isRecordTypeDesc, isClosed,
                        recordToTypeDescNodes, jsonFieldToElements, diagnosticMessages);
            } else {
                DiagnosticMessage message = DiagnosticMessage.jsonToRecordConverter101(null);
                diagnosticMessages.add(message);
                return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
            }
        } catch (JsonSyntaxException e) {
            DiagnosticMessage message = DiagnosticMessage.jsonToRecordConverter100(new String[]{e.getMessage()});
            diagnosticMessages.add(message);
            return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
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
        } catch (FormatterException e) {
            DiagnosticMessage message = DiagnosticMessage.jsonToRecordConverter102(null);
            diagnosticMessages.add(message);
        }
        return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
    }

    /**
     * This method generates the TypeDescriptorNodes for the parsed JSON value.
     *
     * @param jsonObject JSON object node that has to be generated as Ballerina record
     * @param recordName Name of the generated record
     * @param isRecordTypeDesc To denote final record, a record type descriptor (In line records)
     * @param isClosed To denote whether the response record is closed or not
     * @param recordToTypeDescNodes The map of recordNames and the TypeDescriptorNodes already generated
     * @param jsonNodes The map of JSON field names and the JSON nodes for already created TypeDescriptorNodes
     * @param diagnosticMessages The list of diagnostic messages generated by the method
     */
    private static void generateRecords(JsonObject jsonObject, String recordName, boolean isRecordTypeDesc,
                                        boolean isClosed, Map<String, NonTerminalNode> recordToTypeDescNodes,
                                        Map<String, JsonElement> jsonNodes,
                                        List<DiagnosticMessage> diagnosticMessages) {
        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
        Token bodyStartDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.OPEN_BRACE_PIPE_TOKEN :
                SyntaxKind.OPEN_BRACE_TOKEN);

        List<Node> recordFields = new ArrayList<>();
        if (recordToTypeDescNodes.containsKey(recordName)) {
            RecordTypeDescriptorNode lastRecordTypeDescriptorNode =
                    (RecordTypeDescriptorNode) recordToTypeDescNodes.get(recordName);
            List<RecordFieldNode> lastRecordFields = lastRecordTypeDescriptorNode.fields().stream()
                    .map(node -> (RecordFieldNode) node).collect(Collectors.toList());
            Map<String, RecordFieldNode> previousRecordFieldToNodes = lastRecordFields.stream()
                    .collect(Collectors.toMap(node -> node.fieldName().text(), Function.identity()));
            Map<String, RecordFieldNode> newRecordFieldToNodes = jsonObject.entrySet().stream()
                    .map(entry -> (RecordFieldNode) getRecordField(entry, isRecordTypeDesc, false,
                            recordToTypeDescNodes))
                    .collect(Collectors.toList()).stream()
                    .collect(Collectors.toMap(node -> node.fieldName().text(), Function.identity()));
            updateRecordFields(jsonObject, isRecordTypeDesc, recordToTypeDescNodes, jsonNodes, diagnosticMessages,
                    recordFields, previousRecordFieldToNodes, newRecordFieldToNodes);
        } else {
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    String elementKey = entry.getKey();
                    String type = StringUtils.capitalize(elementKey);
                    generateRecords(entry.getValue().getAsJsonObject(), type, isRecordTypeDesc, isClosed,
                            recordToTypeDescNodes, jsonNodes, diagnosticMessages);
                } else if (entry.getValue().isJsonArray()) {
                    for (JsonElement element : entry.getValue().getAsJsonArray()) {
                        if (element.isJsonObject()) {
                            String elementKey = entry.getKey();
                            String type = StringUtils.capitalize(elementKey) + ARRAY_RECORD_SUFFIX;
                            generateRecords(element.getAsJsonObject(), type, isRecordTypeDesc, isClosed,
                                    recordToTypeDescNodes, jsonNodes, diagnosticMessages);
                            break;
                        }
                    }
                }
                jsonNodes.put(entry.getKey(), entry.getValue());
                Node recordField = getRecordField(entry, isRecordTypeDesc, false, recordToTypeDescNodes);
                recordFields.add(recordField);
            }
        }
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFields);
        Token bodyEndDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.CLOSE_BRACE_PIPE_TOKEN :
                SyntaxKind.CLOSE_BRACE_TOKEN);
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        recordToTypeDescNodes.put(((recordName == null) || recordName.equals("")) ? NEW_RECORD_NAME : recordName,
                recordTypeDescriptorNode);
    }

    /**
     * This method updates the record fields already generated, if the fields are optional.
     *
     * @param jsonObject JSON object node that has to be generated as Ballerina record
     * @param isRecordTypeDesc To denote final record, a record type descriptor (In line records)
     * @param recordToTypeDescNodes The map of recordNames and the TypeDescriptorNodes already generated
     * @param jsonNodes The map of JSON field names and the JSON nodes for already created TypeDescriptorNodes
     * @param diagnosticMessages The list of diagnostic messages generated by the method
     * @param recordFields The list generated record fields
     * @param previousRecordFieldToNodes The list of already generated field nodes
     * @param newRecordFieldToNodes The list of newly generated field nodes for the same record
     */
    private static void updateRecordFields(JsonObject jsonObject, boolean isRecordTypeDesc,
                                           Map<String, NonTerminalNode> recordToTypeDescNodes,
                                           Map<String, JsonElement> jsonNodes,
                                           List<DiagnosticMessage> diagnosticMessages, List<Node> recordFields,
                                           Map<String, RecordFieldNode> previousRecordFieldToNodes,
                                           Map<String, RecordFieldNode> newRecordFieldToNodes) {
        Map<String, RecordFieldNode> intersectingRecordFields =
                intersection(previousRecordFieldToNodes, newRecordFieldToNodes);
        Map<String, RecordFieldNode> differencingRecordFields =
                difference(previousRecordFieldToNodes, newRecordFieldToNodes);

        for (Map.Entry<String, RecordFieldNode> entry : intersectingRecordFields.entrySet()) {
            boolean isOptional = entry.getValue().questionMarkToken().isPresent();
            Map.Entry<String, JsonElement> jsonEntry =
                    new AbstractMap.SimpleEntry<>(entry.getKey(), jsonNodes.get(entry.getKey()));
            Node recordField = getRecordField(jsonEntry, isRecordTypeDesc, isOptional, recordToTypeDescNodes);
            recordFields.add(recordField);
        }

        for (Map.Entry<String, RecordFieldNode> entry : differencingRecordFields.entrySet()) {
            String jsonField = entry.getKey();
            JsonElement jsonElement = jsonNodes.get(jsonField);
            Map.Entry<String, JsonElement> jsonEntry = jsonElement != null ?
                    new AbstractMap.SimpleEntry<>(jsonField, jsonElement) :
                    jsonObject.entrySet().stream().filter(elementEntry -> elementEntry.getKey().equals(jsonField))
                            .findFirst().orElse(null);
            if (jsonEntry != null) {
                Node recordField = getRecordField(jsonEntry, isRecordTypeDesc, true, recordToTypeDescNodes);
                recordFields.add(recordField);
            } else {
                /*
                  This else bloc is unreachable as, there is no way jsonEntry can become null. But since it is an
                  optional field this else bloc is written to capture error and store in diagnostics.
                 */
                DiagnosticMessage message = DiagnosticMessage.jsonToRecordConverter103(new String[]{jsonField});
                diagnosticMessages.add(message);
            }
        }
    }

    /**
     * This method generates the record fields for the corresponding JSON fields.
     *
     * @param entry Map entry of a JSON field name and the corresponding JSON element
     * @param isRecordTypeDesc To denote final record, a record type descriptor (In line records)
     * @param optionalField To denote whether the record field is optional or not
     * @param recordToTypeDescNodes The map of recordNames and the TypeDescriptorNodes already generated
     * @return {@link Node} Record field node for the corresponding JSON field
     */
    private static Node getRecordField(Map.Entry<String, JsonElement> entry, boolean isRecordTypeDesc,
                                       boolean optionalField, Map<String, NonTerminalNode> recordToTypeDescNodes) {
        Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
        TypeDescriptorNode fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(entry.getKey().trim()));
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        Token optionalFieldToken = optionalField ? questionMarkToken : null;
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
                    getArrayTypeDescriptorNode(jsonArrayEntry, isRecordTypeDesc, recordToTypeDescNodes);

            recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    arrayTypeName, fieldName,
                    optionalFieldToken, semicolonToken);
        }
        return recordFieldNode;
    }

    /**
     * This method generates the record fields for the corresponding JSON fields it it's an array.
     *
     * @param entry Map entry of a JSON field name and the corresponding JSON element
     * @param isRecordTypeDesc To denote final record, a record type descriptor (In line records)
     * @param recordToTypeDescNodes The map of recordNames and the TypeDescriptorNodes already generated
     * @return {@link ArrayTypeDescriptorNode} Record field node for the corresponding JSON array field
     */
    private static ArrayTypeDescriptorNode getArrayTypeDescriptorNode(
            Map.Entry<String, JsonArray> entry, boolean isRecordTypeDesc,
            Map<String, NonTerminalNode> recordToTypeDescNodes) {
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
                String type = StringUtils.capitalize(elementKey) + ARRAY_RECORD_SUFFIX;
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
                        getArrayTypeDescriptorNode(arrayEntry, isRecordTypeDesc, recordToTypeDescNodes);
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

    /**
     * This method generates the Union of all provided TypeDescriptorNodes.
     *
     * @param typeNames List of TypeDescriptorNodes to be unionized
     * @return {@link TypeDescriptorNode} Union TypeDescriptorNode of provided TypeDescriptorNodes
     */
    private static TypeDescriptorNode createUnionTypeDescriptorNode(List<TypeDescriptorNode> typeNames) {
        if (typeNames.size() == 0) {
            Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
            return NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
        } else if (typeNames.size() == 1) {
            return typeNames.get(0);
        }
        Token pipeToken = NodeFactory.createToken(SyntaxKind.PIPE_TOKEN);
        String unionTypeDescNodeSource = typeNames.stream().map(TypeDescriptorNode::toSourceCode)
                .collect(Collectors.joining(pipeToken.toSourceCode()));
        TypeDescriptorNode unionTypeDescNode = NodeParser.parseTypeDescriptor(unionTypeDescNodeSource);
        Token openParenToken = NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        Token closeParenToken = NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        return NodeFactory.createParenthesisedTypeDescriptorNode(openParenToken,
                unionTypeDescNode, closeParenToken);
    }
}

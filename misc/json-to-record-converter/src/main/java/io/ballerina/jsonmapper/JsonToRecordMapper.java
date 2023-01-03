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
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.jsonmapper.diagnostic.DiagnosticMessage;
import io.ballerina.jsonmapper.diagnostic.DiagnosticUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.formatter.core.ForceFormattingOptions;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.FormattingOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.javatuples.Pair;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.ballerina.jsonmapper.util.ConverterUtils.escapeIdentifier;
import static io.ballerina.jsonmapper.util.ConverterUtils.extractArrayTypeDescNode;
import static io.ballerina.jsonmapper.util.ConverterUtils.extractTypeDescriptorNodes;
import static io.ballerina.jsonmapper.util.ConverterUtils.extractUnionTypeDescNode;
import static io.ballerina.jsonmapper.util.ConverterUtils.getAndUpdateFieldNames;
import static io.ballerina.jsonmapper.util.ConverterUtils.getExistingTypeNames;
import static io.ballerina.jsonmapper.util.ConverterUtils.getNumberOfDimensions;
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
     * @deprecated
     * This method returns the Ballerina code for the provided JSON value or the diagnostics.
     *
     * <p> Use {@link JsonToRecordMapper#convert(String, String, boolean, boolean, boolean, String, WorkspaceManager)}}
     * instead.
     *
     * @param jsonString JSON string of the JSON value to be converted to Ballerina record
     * @param recordName Name of the generated record
     * @param isRecordTypeDesc To denote final record, a record type descriptor (In line records)
     * @param isClosed To denote whether the response record is closed or not
     * @return {@link JsonToRecordResponse} Ballerina code block or the Diagnostics
     */
    @Deprecated
    public static JsonToRecordResponse convert(String jsonString, String recordName, boolean isRecordTypeDesc,
                                               boolean isClosed) {
        return convert(jsonString, recordName, isRecordTypeDesc, isClosed, false, null, null);
    }

    /**
     * This method returns the Ballerina code for the provided JSON value or the diagnostics.
     *
     * @param jsonString JSON string of the JSON value to be converted to Ballerina record
     * @param recordName Name of the generated record
     * @param isRecordTypeDesc To denote final record, a record type descriptor (In line records)
     * @param isClosed To denote whether the response record is closed or not
     * @param forceFormatRecordFields To denote whether the inline records to be formatted for multi-line or in-line
     * @param filePathUri FilePath URI of the/a file in a singleFileProject or module
     * @param workspaceManager Workspace manager instance
     * @return {@link JsonToRecordResponse} Ballerina code block or the Diagnostics
     */
    public static JsonToRecordResponse convert(String jsonString, String recordName, boolean isRecordTypeDesc,
                                               boolean isClosed, boolean forceFormatRecordFields, String filePathUri,
                                               WorkspaceManager workspaceManager) {
        List<String> existingFieldNames = getExistingTypeNames(workspaceManager, filePathUri);
        Map<String, String> updatedFieldNames = new HashMap<>();
        Map<String, NonTerminalNode> recordToTypeDescNodes = new LinkedHashMap<>();
        Map<String, JsonElement> jsonFieldToElements = new LinkedHashMap<>();
        List<DiagnosticMessage> diagnosticMessages = new ArrayList<>();
        JsonToRecordResponse response = new JsonToRecordResponse();

        if (existingFieldNames.contains(recordName)) {
            DiagnosticMessage message = DiagnosticMessage.jsonToRecordConverter105(new String[]{recordName});
            diagnosticMessages.add(message);
            return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
        }
        try {
            JsonElement parsedJson = JsonParser.parseString(jsonString);
            if (parsedJson.isJsonObject()) {
                generateRecords(parsedJson.getAsJsonObject(), null, isClosed, recordToTypeDescNodes, null,
                        jsonFieldToElements, existingFieldNames, updatedFieldNames, diagnosticMessages);
            } else if (parsedJson.isJsonArray()) {
                JsonObject object = new JsonObject();
                object.add(((recordName == null) || recordName.equals("")) ? StringUtils.uncapitalize(NEW_RECORD_NAME) :
                        StringUtils.uncapitalize(recordName), parsedJson);
                generateRecords(object, null, isClosed, recordToTypeDescNodes, null, jsonFieldToElements,
                        existingFieldNames, updatedFieldNames, diagnosticMessages);
            } else {
                DiagnosticMessage message = DiagnosticMessage.jsonToRecordConverter101(null);
                diagnosticMessages.add(message);
                return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
            }
            if (recordName != null && recordToTypeDescNodes.containsKey(recordName)) {
                DiagnosticMessage message = DiagnosticMessage.jsonToRecordConverter104(new String[]{recordName});
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
                    String recordTypeName = entry.getKey() == null ?
                            (recordName == null || recordName.equals("")) ?
                                    getAndUpdateFieldNames(NEW_RECORD_NAME, false,
                                            existingFieldNames, updatedFieldNames)
                                    : escapeIdentifier(StringUtils.capitalize(recordName)) : entry.getKey();
                    IdentifierToken typeName = AbstractNodeFactory
                            .createIdentifierToken(recordTypeName);
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    return NodeFactory.createTypeDefinitionNode(null, null, typeKeyWord, typeName,
                            entry.getValue(), semicolon);
                }).collect(Collectors.toList());
        TypeDefinitionNode lastTypeDefNode = convertToInlineRecord(typeDefNodes);
        NodeList<ModuleMemberDeclarationNode> moduleMembers = isRecordTypeDesc ?
                AbstractNodeFactory.createNodeList(lastTypeDefNode) :
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
            DiagnosticMessage message = DiagnosticMessage.jsonToRecordConverter102(null);
            diagnosticMessages.add(message);
        }
        if (!updatedFieldNames.isEmpty()) {
            updatedFieldNames.forEach((oldFieldName, updateFieldName) -> {
                DiagnosticMessage message =
                        DiagnosticMessage.jsonToRecordConverter106(new String[]{oldFieldName, updateFieldName});
                diagnosticMessages.add(message);
            });
        }
        return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
    }

    /**
     * This method generates the TypeDescriptorNodes for the parsed JSON value.
     *
     * @param jsonObject JSON object node that has to be generated as Ballerina record
     * @param recordName Name of the generated record
     * @param isClosed To denote whether the response record is closed or not
     * @param recordToTypeDescNodes The map of recordNames and the TypeDescriptorNodes already generated
     * @param moveBefore To move generated TypeDescriptorNode before specified TypeDescriptorNode
     * @param jsonNodes The map of JSON field names and the JSON nodes for already created TypeDescriptorNodes
     * @param existingFieldNames The list of already existing record names in the ModulePartNode
     * @param updatedFieldNames The map of updated record names for already existing record names in the ModulePartNode
     * @param diagnosticMessages The list of diagnostic messages generated by the method
     */
    private static void generateRecords(JsonObject jsonObject, String recordName, boolean isClosed,
                                        Map<String, NonTerminalNode> recordToTypeDescNodes, String moveBefore,
                                        Map<String, JsonElement> jsonNodes,
                                        List<String> existingFieldNames,
                                        Map<String, String> updatedFieldNames,
                                        List<DiagnosticMessage> diagnosticMessages) {
        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
        Token bodyStartDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.OPEN_BRACE_PIPE_TOKEN :
                SyntaxKind.OPEN_BRACE_TOKEN);

        List<Node> recordFields = new ArrayList<>();
        if (recordToTypeDescNodes.containsKey(recordName)) {
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray()) {
                    generateRecordForObjAndArray(entry.getValue(), entry.getKey(), isClosed, recordToTypeDescNodes,
                            recordName, jsonNodes, existingFieldNames, updatedFieldNames, diagnosticMessages, false);
                }
                jsonNodes.put(entry.getKey(), entry.getValue());
            }
            RecordTypeDescriptorNode previousRecordTypeDescriptorNode =
                    (RecordTypeDescriptorNode) recordToTypeDescNodes.get(recordName);
            List<RecordFieldNode> previousRecordFields = previousRecordTypeDescriptorNode.fields().stream()
                    .map(node -> (RecordFieldNode) node).collect(Collectors.toList());
            Map<String, RecordFieldNode> previousRecordFieldToNodes = previousRecordFields.stream()
                    .collect(Collectors.toMap(node -> node.fieldName().text(), Function.identity(),
                            (val1, val2) -> val1, LinkedHashMap::new));
            Map<String, RecordFieldNode> newRecordFieldToNodes = jsonObject.entrySet().stream()
                    .map(entry ->
                            (RecordFieldNode) getRecordField(entry, existingFieldNames, updatedFieldNames, false))
                    .collect(Collectors.toList()).stream()
                    .collect(Collectors.toMap(node -> node.fieldName().text(), Function.identity(),
                            (val1, val2) -> val1, LinkedHashMap::new));
            updateRecordFields(jsonObject, jsonNodes, diagnosticMessages, recordFields, existingFieldNames,
                    updatedFieldNames, previousRecordFieldToNodes, newRecordFieldToNodes);
        } else {
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray()) {
                    generateRecordForObjAndArray(entry.getValue(), entry.getKey(), isClosed, recordToTypeDescNodes,
                            null, jsonNodes, existingFieldNames, updatedFieldNames, diagnosticMessages, false);
                }
                jsonNodes.put(entry.getKey(), entry.getValue());
                Node recordField = getRecordField(entry, existingFieldNames, updatedFieldNames, false);
                recordFields.add(recordField);
            }
        }
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFields);
        Token bodyEndDelimiter = AbstractNodeFactory.createToken(isClosed ? SyntaxKind.CLOSE_BRACE_PIPE_TOKEN :
                SyntaxKind.CLOSE_BRACE_TOKEN);
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        if (moveBefore == null) {
            recordToTypeDescNodes.put(recordName, recordTypeDescriptorNode);
        } else {
            List<Map.Entry<String, NonTerminalNode>> typeDescNodes = new ArrayList<>(recordToTypeDescNodes.entrySet());
            List<String> recordNames = typeDescNodes.stream().map(Map.Entry::getKey).collect(Collectors.toList());
            Map.Entry<String, NonTerminalNode> mapEntry =
                    new AbstractMap.SimpleEntry<>(recordName, recordTypeDescriptorNode);
            typeDescNodes.add(recordNames.indexOf(moveBefore), mapEntry);
            recordToTypeDescNodes.clear();
            typeDescNodes.forEach(node -> recordToTypeDescNodes.put(node.getKey(), node.getValue()));
        }
    }

    private static void generateRecordForObjAndArray(JsonElement jsonElement, String elementKey, boolean isClosed,
                                                     Map<String, NonTerminalNode> recordToTypeDescNodes,
                                                     String moveBefore, Map<String, JsonElement> jsonNodes,
                                                     List<String> existingFieldNames,
                                                     Map<String, String> updatedFieldNames,
                                                     List<DiagnosticMessage> diagnosticMessages,
                                                     boolean arraySuffixAdded) {
        if (jsonElement.isJsonObject()) {
            String type = escapeIdentifier(StringUtils.capitalize(elementKey));
            String updatedType = getAndUpdateFieldNames(type, arraySuffixAdded, existingFieldNames, updatedFieldNames);
            generateRecords(jsonElement.getAsJsonObject(), updatedType, isClosed, recordToTypeDescNodes,
                    moveBefore, jsonNodes, existingFieldNames, updatedFieldNames, diagnosticMessages);
        } else if (jsonElement.isJsonArray()) {
            for (JsonElement element : jsonElement.getAsJsonArray()) {
                String arrayElementKey = elementKey + (arraySuffixAdded ? "" : ARRAY_RECORD_SUFFIX);
                generateRecordForObjAndArray(element, arrayElementKey, isClosed, recordToTypeDescNodes, moveBefore,
                        jsonNodes, existingFieldNames, updatedFieldNames, diagnosticMessages, true);
            }
        }
    }

    /**
     * This method updates the record fields already generated, if the fields are optional.
     *
     * @param jsonObject JSON object node that has to be generated as Ballerina record
     * @param jsonNodes The map of JSON field names and the JSON nodes for already created TypeDescriptorNodes
     * @param diagnosticMessages The list of diagnostic messages generated by the method
     * @param recordFields The list generated record fields
     * @param existingFieldNames The list of already existing record names in the ModulePartNode
     * @param updatedFieldNames The map of updated record names for already existing record names in the ModulePartNode
     * @param previousRecordFieldToNodes The list of already generated field nodes
     * @param newRecordFieldToNodes The list of newly generated field nodes for the same record
     */
    private static void updateRecordFields(JsonObject jsonObject, Map<String, JsonElement> jsonNodes,
                                           List<DiagnosticMessage> diagnosticMessages, List<Node> recordFields,
                                           List<String> existingFieldNames,
                                           Map<String, String> updatedFieldNames,
                                           Map<String, RecordFieldNode> previousRecordFieldToNodes,
                                           Map<String, RecordFieldNode> newRecordFieldToNodes) {
        Map<String, Pair<RecordFieldNode, RecordFieldNode>> intersectingRecordFields =
                intersection(previousRecordFieldToNodes, newRecordFieldToNodes);
        Map<String, RecordFieldNode> differencingRecordFields =
                difference(previousRecordFieldToNodes, newRecordFieldToNodes);

        for (Map.Entry<String, Pair<RecordFieldNode, RecordFieldNode>> entry : intersectingRecordFields.entrySet()) {
            boolean isOptional = entry.getValue().getValue0().questionMarkToken().isPresent();
            Map<String, String> jsonEscapedFieldToFields = jsonNodes.entrySet().stream()
                    .collect(Collectors.toMap(jsonEntry -> escapeIdentifier(jsonEntry.getKey()), Map.Entry::getKey));
            Map.Entry<String, JsonElement> jsonEntry = new AbstractMap.SimpleEntry<>(jsonEscapedFieldToFields
                    .get(entry.getKey()), jsonNodes.get(jsonEscapedFieldToFields.get(entry.getKey())));
            if (!entry.getValue().getValue0().typeName().toSourceCode()
                    .equals(entry.getValue().getValue1().typeName().toSourceCode())) {
                TypeDescriptorNode node1 = (TypeDescriptorNode) entry.getValue().getValue0().typeName();
                TypeDescriptorNode node2 = (TypeDescriptorNode) entry.getValue().getValue1().typeName();

                TypeDescriptorNode nonAnyDataNode = null;
                boolean alreadyOptionalTypeDesc = false;

                if (node1.kind().equals(SyntaxKind.OPTIONAL_TYPE_DESC)) {
                    OptionalTypeDescriptorNode optionalTypeDescNode = (OptionalTypeDescriptorNode) node1;
                    node1 = (TypeDescriptorNode) optionalTypeDescNode.typeDescriptor();
                    alreadyOptionalTypeDesc = true;
                } else if (node2.kind().equals(SyntaxKind.OPTIONAL_TYPE_DESC)) {
                    OptionalTypeDescriptorNode optionalTypeDescNode = (OptionalTypeDescriptorNode) node2;
                    node2 = (TypeDescriptorNode) optionalTypeDescNode.typeDescriptor();
                    alreadyOptionalTypeDesc = true;
                } else {
                    Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
                    if (node1.kind().equals(SyntaxKind.ANYDATA_KEYWORD)) {
                        nonAnyDataNode =
                                NodeParser.parseTypeDescriptor(node2.toSourceCode() + questionMarkToken.text());
                    } else if (node2.kind().equals(SyntaxKind.ANYDATA_KEYWORD)) {
                        nonAnyDataNode =
                                NodeParser.parseTypeDescriptor(node1.toSourceCode() + questionMarkToken.text());
                    }
                }

                List<TypeDescriptorNode> typeDescNodesSorted =
                        sortTypeDescriptorNodes(extractTypeDescriptorNodes(List.of(node1, node2)));
                TypeDescriptorNode unionTypeDescNode =
                        createUnionTypeDescriptorNode(typeDescNodesSorted, alreadyOptionalTypeDesc);

                RecordFieldNode recordField =
                        (RecordFieldNode) getRecordField(jsonEntry, existingFieldNames, updatedFieldNames, isOptional);
                recordField = recordField.modify()
                        .withTypeName(nonAnyDataNode == null ? unionTypeDescNode : nonAnyDataNode).apply();
                recordFields.add(recordField);
            } else {
                Node recordField = getRecordField(jsonEntry, existingFieldNames, updatedFieldNames, isOptional);
                recordFields.add(recordField);
            }
        }

        for (Map.Entry<String, RecordFieldNode> entry : differencingRecordFields.entrySet()) {
            String jsonField = entry.getKey();
            Map<String, String> jsonEscapedFieldToFields = jsonNodes.entrySet().stream()
                    .collect(Collectors.toMap(jsonEntry -> escapeIdentifier(jsonEntry.getKey()), Map.Entry::getKey));
            JsonElement jsonElement = jsonNodes.get(jsonEscapedFieldToFields.get(jsonField));
            Map.Entry<String, JsonElement> jsonEntry = jsonElement != null ?
                    new AbstractMap.SimpleEntry<>(jsonEscapedFieldToFields.get(jsonField), jsonElement) :
                    jsonObject.entrySet().stream().filter(elementEntry -> escapeIdentifier(elementEntry.getKey())
                            .equals(jsonField)).findFirst().orElse(null);
            if (jsonEntry != null) {
                Node recordField = getRecordField(jsonEntry, existingFieldNames, updatedFieldNames, true);
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
     * @param existingFieldNames The list of already existing record names in the ModulePartNode
     * @param updatedFieldNames The map of updated record names for already existing record names in the ModulePartNode
     * @param isOptionalField To denote whether the record field is optional or not
     * @return {@link Node} Record field node for the corresponding JSON field
     */
    private static Node getRecordField(Map.Entry<String, JsonElement> entry, List<String> existingFieldNames,
                                       Map<String, String> updatedFieldNames,
                                       boolean isOptionalField) {
        Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANYDATA_KEYWORD);
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        TypeDescriptorNode fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(entry.getKey().trim()));
        Token optionalFieldToken = isOptionalField ? questionMarkToken : null;
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                fieldTypeName, fieldName, optionalFieldToken, semicolonToken);

        if (entry.getValue().isJsonPrimitive()) {
            typeName = getPrimitiveTypeName(entry.getValue().getAsJsonPrimitive());
            fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);
            recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    fieldTypeName, fieldName,
                    optionalFieldToken, semicolonToken);
        } else if (entry.getValue().isJsonObject()) {
            String elementKey = entry.getKey().trim();
            String type = escapeIdentifier(StringUtils.capitalize(elementKey));
            String updatedType = getAndUpdateFieldNames(type, false, existingFieldNames, updatedFieldNames);
            typeName = AbstractNodeFactory.createIdentifierToken(updatedType);
            fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);
            recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    fieldTypeName, fieldName,
                    optionalFieldToken, semicolonToken);
        } else if (entry.getValue().isJsonArray()) {
            Map.Entry<String, JsonArray> jsonArrayEntry =
                    new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getAsJsonArray());
            ArrayTypeDescriptorNode arrayTypeName =
                    getArrayTypeDescriptorNode(jsonArrayEntry, existingFieldNames, updatedFieldNames);
            recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    arrayTypeName, fieldName,
                    optionalFieldToken, semicolonToken);
        }
        return recordFieldNode;
    }

    /**
     * This method converts the list of TypeDefinitionNodes into single inline TypeDefinitionNode.
     *
     * @param typeDefNodes List of TypeDefinitionNodes that has to be converted into inline TypeDefinitionNode.
     * @return {@link TypeDefinitionNode} The converted inline TypeDefinitionNode.
     */
    private static TypeDefinitionNode convertToInlineRecord(List<TypeDefinitionNode> typeDefNodes) {
        Map<String, RecordTypeDescriptorNode> visitedRecordTypeDescNodeTypeToNodes = new LinkedHashMap<>();
        for (TypeDefinitionNode typeDefNode : typeDefNodes) {
            RecordTypeDescriptorNode recordTypeDescNode = (RecordTypeDescriptorNode) typeDefNode.typeDescriptor();
            List<RecordFieldNode> recordFieldNodes = recordTypeDescNode.fields().stream()
                    .map(node -> (RecordFieldNode) node).collect(Collectors.toList());
            List<Node> intermediateRecordFieldNodes = new ArrayList<>();
            for (RecordFieldNode recordFieldNode : recordFieldNodes) {
                TypeDescriptorNode fieldTypeName = (TypeDescriptorNode) recordFieldNode.typeName();
                TypeDescriptorNode converted =
                        convertUnionTypeToInline(fieldTypeName, visitedRecordTypeDescNodeTypeToNodes);
                Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                RecordFieldNode updatedRecordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                        converted, recordFieldNode.fieldName(),
                        recordFieldNode.questionMarkToken().orElse(null), semicolonToken);
                intermediateRecordFieldNodes.add(updatedRecordFieldNode);
            }
            NodeList<Node> updatedRecordFieldNodes = AbstractNodeFactory.createNodeList(intermediateRecordFieldNodes);
            RecordTypeDescriptorNode updatedRecordTypeDescNode =
                    recordTypeDescNode.modify().withFields(updatedRecordFieldNodes).apply();
            visitedRecordTypeDescNodeTypeToNodes.put(typeDefNode.typeName().toSourceCode(), updatedRecordTypeDescNode);
        }

        List<Map.Entry<String, RecordTypeDescriptorNode>> visitedRecordTypeDescNodes =
                new ArrayList<>(visitedRecordTypeDescNodeTypeToNodes.entrySet());
        Map.Entry<String, RecordTypeDescriptorNode> lastRecordTypeDescNode =
                visitedRecordTypeDescNodes.get(visitedRecordTypeDescNodes.size() - 1);
        Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
        IdentifierToken typeName =
                AbstractNodeFactory.createIdentifierToken(escapeIdentifier(lastRecordTypeDescNode.getKey()));
        Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        return NodeFactory.createTypeDefinitionNode(null, null, typeKeyWord, typeName,
                lastRecordTypeDescNode.getValue(), semicolon);
    }

    /**
     * This method generates the record fields for the corresponding JSON fields if it's an array.
     *
     * @param entry Map entry of a JSON field name and the corresponding JSON element
     * @param existingFieldNames The list of already existing record names in the ModulePartNode
     * @param updatedFieldNames The map of updated record names for already existing record names in the ModulePartNode
     * @return {@link ArrayTypeDescriptorNode} Record field node for the corresponding JSON array field
     */
    private static ArrayTypeDescriptorNode getArrayTypeDescriptorNode(Map.Entry<String, JsonArray> entry,
                                                                      List<String> existingFieldNames,
                                                                      Map<String, String> updatedFieldNames) {
        Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);

        Iterator<JsonElement> iterator = entry.getValue().iterator();
        List<TypeDescriptorNode> typeDescriptorNodes = new ArrayList<>();
        while (iterator.hasNext()) {
            JsonElement element = iterator.next();
            if (element.isJsonPrimitive()) {
                Token tempTypeName = getPrimitiveTypeName(element.getAsJsonPrimitive());
                TypeDescriptorNode tempTypeNode =
                        NodeFactory.createBuiltinSimpleNameReferenceNode(tempTypeName.kind(), tempTypeName);
                if (!typeDescriptorNodes.stream().map(Node::toSourceCode)
                        .collect(Collectors.toList()).contains(tempTypeNode.toSourceCode())) {
                    typeDescriptorNodes.add(tempTypeNode);
                }
            } else if (element.isJsonNull()) {
                Token tempTypeName = AbstractNodeFactory.createToken(SyntaxKind.ANYDATA_KEYWORD);
                TypeDescriptorNode tempTypeNode =
                        NodeFactory.createBuiltinSimpleNameReferenceNode(tempTypeName.kind(), tempTypeName);
                if (!typeDescriptorNodes.stream().map(Node::toSourceCode)
                        .collect(Collectors.toList()).contains(tempTypeNode.toSourceCode())) {
                    typeDescriptorNodes.add(tempTypeNode);
                }
            } else if (element.isJsonObject()) {
                String elementKey = entry.getKey();
                String type = escapeIdentifier(StringUtils.capitalize(elementKey) + ARRAY_RECORD_SUFFIX);
                String updatedType = getAndUpdateFieldNames(type, true, existingFieldNames, updatedFieldNames);
                Token tempTypeName = AbstractNodeFactory.createIdentifierToken(updatedType);

                TypeDescriptorNode tempTypeNode =
                        NodeFactory.createBuiltinSimpleNameReferenceNode(tempTypeName.kind(), tempTypeName);
                if (!typeDescriptorNodes.stream().map(Node::toSourceCode)
                        .collect(Collectors.toList()).contains(tempTypeNode.toSourceCode())) {
                    typeDescriptorNodes.add(tempTypeNode);
                }
            } else if (element.isJsonArray()) {
                Map.Entry<String, JsonArray> arrayEntry =
                        new AbstractMap.SimpleEntry<>(entry.getKey(), element.getAsJsonArray());
                TypeDescriptorNode tempTypeNode =
                        getArrayTypeDescriptorNode(arrayEntry, existingFieldNames, updatedFieldNames);
                if (!typeDescriptorNodes.stream().map(Node::toSourceCode)
                        .collect(Collectors.toList()).contains(tempTypeNode.toSourceCode())) {
                    typeDescriptorNodes.add(tempTypeNode);
                }
            }
        }

        List<TypeDescriptorNode> typeDescriptorNodesSorted = sortTypeDescriptorNodes(typeDescriptorNodes);
        TypeDescriptorNode fieldTypeName = createUnionTypeDescriptorNode(typeDescriptorNodesSorted, false);
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
    private static TypeDescriptorNode createUnionTypeDescriptorNode(List<TypeDescriptorNode> typeNames,
                                                                    boolean isOptional) {
        if (typeNames.size() == 0) {
            Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANYDATA_KEYWORD);
            return NodeFactory.createBuiltinSimpleNameReferenceNode(typeName.kind(), typeName);
        } else if (typeNames.size() == 1) {
            return typeNames.get(0);
        }
        Token pipeToken = NodeFactory.createToken(SyntaxKind.PIPE_TOKEN);
        String unionTypeDescNodeSource = typeNames.stream().map(TypeDescriptorNode::toSourceCode)
                .collect(Collectors.joining(pipeToken.toSourceCode()));
        TypeDescriptorNode unionTypeDescNode = NodeParser.parseTypeDescriptor(unionTypeDescNodeSource);
        Token openParenToken = NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);
        Token closeParenToken = NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);
        Token questionMarkToken = NodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);

        ParenthesisedTypeDescriptorNode parenTypeDescNode =
                NodeFactory.createParenthesisedTypeDescriptorNode(openParenToken, unionTypeDescNode, closeParenToken);

        return isOptional ?
                NodeFactory.createOptionalTypeDescriptorNode(parenTypeDescNode, questionMarkToken) : parenTypeDescNode;
    }

    /**
     * This method converts UnionTypeDescriptorNode with IDENTIFIER_TOKENS, to its relevant TypeDescriptorNodes.
     *
     * @param typeDescNode UnionTypeDescriptorNode which has to be converted UnionTypeDescriptorNode with inline
     *                     RecordTypeDescriptorNode
     * @param visitedRecordTypeDescNodeTypeToNodes Already analyzed RecordTypeDescriptorNodeType and Nodes.
     * @return {@link TypeDescriptorNode} Converted UnionTypeDescriptorNode.
     */
    private static TypeDescriptorNode convertUnionTypeToInline(TypeDescriptorNode typeDescNode,
                                                          Map<String, RecordTypeDescriptorNode>
                                                                  visitedRecordTypeDescNodeTypeToNodes) {
        List<TypeDescriptorNode> extractedTypeDescNodes =
                extractUnionTypeDescNode(extractArrayTypeDescNode(typeDescNode));
        List<TypeDescriptorNode> updatedTypeDescNodes = new ArrayList<>();

        if (extractedTypeDescNodes.size() == 1) {
            TypeDescriptorNode arrayExtractedNode = extractArrayTypeDescNode(typeDescNode);
            String fieldTypeNameText = arrayExtractedNode.toSourceCode();
            SyntaxKind fieldKind = arrayExtractedNode.kind();
            if (extractArrayTypeDescNode(typeDescNode).kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                SimpleNameReferenceNode fieldNameRefNode =
                        (SimpleNameReferenceNode) extractArrayTypeDescNode(typeDescNode);
                fieldTypeNameText = fieldNameRefNode.name().toSourceCode();
                fieldKind = fieldNameRefNode.name().kind();
            }

            if (fieldKind.equals(SyntaxKind.IDENTIFIER_TOKEN)) {
                arrayExtractedNode = visitedRecordTypeDescNodeTypeToNodes.get(fieldTypeNameText);
            }
            updatedTypeDescNodes.add(arrayExtractedNode);
        } else {
            for (TypeDescriptorNode extractedTypeDescNode : extractedTypeDescNodes) {
                TypeDescriptorNode updatedTypeDescNode =
                        convertUnionTypeToInline(extractedTypeDescNode, visitedRecordTypeDescNodeTypeToNodes);
                updatedTypeDescNodes.add(updatedTypeDescNode);
            }
        }

        List<TypeDescriptorNode> typeDescNodesSorted = sortTypeDescriptorNodes(updatedTypeDescNodes);
        TypeDescriptorNode unionTypeDescNode = createUnionTypeDescriptorNode(typeDescNodesSorted, false);
        if (typeDescNode.kind().equals(SyntaxKind.ARRAY_TYPE_DESC)) {
            Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
            Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
            NodeList<ArrayDimensionNode> arrayDimensions = NodeFactory.createEmptyNodeList();
            ArrayDimensionNode arrayDimension = NodeFactory.createArrayDimensionNode(openSBracketToken, null,
                    closeSBracketToken);
            int numberOfDimensions = getNumberOfDimensions((ArrayTypeDescriptorNode) typeDescNode);
            for (int i = 0; i < numberOfDimensions; i++) {
                arrayDimensions = arrayDimensions.add(arrayDimension);
            }
            unionTypeDescNode = NodeFactory.createArrayTypeDescriptorNode(unionTypeDescNode, arrayDimensions);
        }
        return unionTypeDescNode;
    }
}

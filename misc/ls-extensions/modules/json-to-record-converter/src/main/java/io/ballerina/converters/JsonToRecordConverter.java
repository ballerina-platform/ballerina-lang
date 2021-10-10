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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
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
import io.ballerina.converters.exception.JsonToRecordConverterException;
import io.ballerina.converters.util.Constants;
import io.ballerina.converters.util.ErrorMessages;
import io.ballerina.converters.util.SchemaGenerator;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.util.*;

import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.converters.util.ConverterUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.converters.util.ConverterUtils.escapeIdentifier;
import static io.ballerina.converters.util.ConverterUtils.extractReferenceType;

/**
 * Implements functionality to convert Json (object or schema) to a ballerina record.
 *
 * The conversion is done by using openAPI schema as an intermediary.
 *
 * @since 2.0.0
 */
public class JsonToRecordConverter {

    private JsonToRecordConverter() {
        // not called
    }

    /**
     * This method takes in a json string and returns the Ballerina code block.
     *
     * @param jsonString Json string for the schema
     * @return {@link String} Ballerina code block
     * @throws IOException In case of Json parse error
     * @throws JsonToRecordConverterException In case of invalid schema
     * @throws FormatterException In case of invalid syntax
     */
    public static JsonToRecordResponse convert(String jsonString, String recordName, boolean isRecordTypeDesc,
                                               boolean isClosed) throws IOException,
            JsonToRecordConverterException, FormatterException {
        String name = ((recordName != null) && !recordName.equals("")) ? recordName : "NewRecord";
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAPI model;
        JsonNode inputJson = objectMapper.readTree(jsonString);
        if (inputJson.has("$schema")) {
            model = parseJSONSchema(jsonString, name);
        } else {
            Map<String, Object> schema = SchemaGenerator.generate(inputJson);
            String schemaJson = objectMapper.writeValueAsString(schema);
            model = parseJSONSchema(schemaJson, name);
        }
        JsonToRecordField typeDescField = new JsonToRecordField(name, SyntaxKind.RECORD_KEYWORD.stringValue(),
                false, new ArrayList<>());
        typeDescField.setIsRequired(true);
        ArrayList<NonTerminalNode> typeDefinitionNodeList = generateRecords(model, typeDescField,
                isRecordTypeDesc, isClosed);
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        JsonToRecordResponse response = new JsonToRecordResponse();
        response.setFields(typeDescField);

        if (isRecordTypeDesc) {
            // Sets generated type descriptor code block
            response.setCodeBlock(Formatter.format(typeDefinitionNodeList.get(0).toSourceCode()));
        } else {
            // Sets generated type definition code block
            NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
                    typeDefinitionNodeList.toArray(new TypeDefinitionNode[0]));
            Token eofToken = AbstractNodeFactory.createIdentifierToken("");
            ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
            response.setCodeBlock(Formatter.format(modulePartNode.syntaxTree()).toSourceCode());
        }

        return response;
    }

    /**
     * Generates Ballerina Record nodes for given OpenAPI model.
     *
     * @param openApi OpenAPI model
     * @return {@link ArrayList}  List of Record Nodes
     * @throws JsonToRecordConverterException In case of bad record fields
     */
    private static ArrayList<NonTerminalNode> generateRecords(OpenAPI openApi, JsonToRecordField typeDescField,
                                                              boolean isRecordTypeDescriptor, boolean isClosedRecord)
            throws JsonToRecordConverterException {
        List<NonTerminalNode> typeDefinitionNodeList = new LinkedList<>();

        if (openApi.getComponents() == null) {
            return new ArrayList<>(typeDefinitionNodeList);
        }

        Components components = openApi.getComponents();

        if (components.getSchemas() == null) {
            return new ArrayList<>(typeDefinitionNodeList);
        }

        Map<String, Schema> schemas = components.getSchemas();
        for (Map.Entry<String, Schema> schema: schemas.entrySet()) {
            List<String> required = schema.getValue().getRequired();

            Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);

            IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(
                    escapeIdentifier(schema.getKey().trim()));

            Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);

            Token bodyStartDelimiter = isClosedRecord ?
                    AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_PIPE_TOKEN) :
                    AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);

            List<Node> recordFieldList = new ArrayList<>();
            Schema<?> schemaValue = schema.getValue();
            String schemaType = schemaValue.getType();
            if (schema.getValue().getProperties() != null || schema.getValue() instanceof ObjectSchema) {
                Map<String, Schema> fields = schema.getValue().getProperties();
                if (fields != null) {
                    for (Map.Entry<String, Schema> field : fields.entrySet()) {
                        String type;
                        JsonToRecordField nestedTypeDescField;
                        if (field.getValue().getType().equals("object")) {
                            type = SyntaxKind.RECORD_KEYWORD.stringValue();
                            nestedTypeDescField = new JsonToRecordField(field.getKey(), type,
                                    false, new ArrayList<>());
                        } else {
                            type = field.getValue().getType();
                            nestedTypeDescField = new JsonToRecordField(field.getKey(),
                                    convertOpenAPITypeToBallerina(type), false, new ArrayList<>());
                        }
                        typeDescField.getFields().add(nestedTypeDescField);
                        addRecordFields(required, recordFieldList, field, typeDefinitionNodeList,
                                nestedTypeDescField, isRecordTypeDescriptor);
                    }
                }
                NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
                Token bodyEndDelimiter = isClosedRecord ?
                        AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_PIPE_TOKEN) :
                        AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
                RecordTypeDescriptorNode recordTypeDescriptorNode =
                        NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                                fieldNodes, null, bodyEndDelimiter);
                if (isRecordTypeDescriptor) {
                    typeDefinitionNodeList.add(recordTypeDescriptorNode);
                } else {
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                            null, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
                    typeDefinitionNodeList.add(typeDefinitionNode);
                }
            } else if (schemaType.equals("array")) {
                if (schemaValue instanceof ArraySchema) {
                    ArraySchema arraySchema = (ArraySchema) schemaValue;
                    Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
                    Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                    IdentifierToken fieldName =
                            AbstractNodeFactory.createIdentifierToken(escapeIdentifier(
                                    schema.getKey().trim().toLowerCase(Locale.ENGLISH)) + "list");
                    Token semicolonToken = AbstractNodeFactory.createIdentifierToken(";");
                    typeDescField.setIsArray(true);
                    TypeDescriptorNode fieldTypeName;
                    if (arraySchema.getItems() != null) {
                        fieldTypeName = extractOpenApiSchema(arraySchema.getItems(), schema.getKey(),
                                typeDefinitionNodeList, typeDescField, isRecordTypeDescriptor);
                    } else {
                        Token type =
                                AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);
                        fieldTypeName =  NodeFactory.createBuiltinSimpleNameReferenceNode(null, type);
                    }
                    ArrayTypeDescriptorNode arrayField =
                            NodeFactory.createArrayTypeDescriptorNode(fieldTypeName, openSBracketToken,
                                    null, closeSBracketToken);
                    RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null,
                            null, arrayField, fieldName, null, semicolonToken);
                    NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldNode);
                    Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
                    RecordTypeDescriptorNode recordTypeDescriptorNode =
                            NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                                    fieldNodes, null, bodyEndDelimiter);
                    if (isRecordTypeDescriptor) {
                        typeDefinitionNodeList.add(recordTypeDescriptorNode);
                    } else {
                        Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                        TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                                null, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
                        typeDefinitionNodeList.add(typeDefinitionNode);
                    }
                }
            }
        }
        return new ArrayList<>(typeDefinitionNodeList);
    }

    /**
     * Method for generating record fields with given schema properties.
     *
     * @param required List of required parameters
     * @param recordFieldList Record field list to which the field will be added
     * @param field Schema entry of the field
     * @param typeDefinitionNodeList List of type definition nodes to be updated in case of object type fields
     * @throws JsonToRecordConverterException In case of bad schema entries
     */
    private static void addRecordFields(List<String> required, List<Node> recordFieldList,
                                        Map.Entry<String, Schema> field, List<NonTerminalNode> typeDefinitionNodeList,
                                        JsonToRecordField typeDescField, boolean isRecordTypeDescriptor)
            throws JsonToRecordConverterException {

        TypeDescriptorNode fieldTypeName = extractOpenApiSchema(field.getValue(), field.getKey(),
                typeDefinitionNodeList, typeDescField, isRecordTypeDescriptor);
        IdentifierToken fieldName =
                AbstractNodeFactory.createIdentifierToken(escapeIdentifier(field.getKey().trim()));
        Token questionMarkToken = (required != null && required.contains(field.getKey().trim()))
                ? null
                : AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        typeDescField.setIsRequired(required != null && required.contains(field.getKey().trim()));
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                fieldTypeName, fieldName, questionMarkToken, semicolonToken);

        recordFieldList.add(recordFieldNode);
    }

    /**
     * Common method to extract OpenApi Schema type objects in to Ballerina type compatible schema objects.
     *
     * @param schema OpenApi Schema
     * @param name Name of the field
     * @param typeDefinitionNodeList List of type definition nodes to be updated in case of object type fields
     * @return {@link TypeDescriptorNode} Type descriptor for record field
     * @throws JsonToRecordConverterException In case of invalid schema
     */
    private static TypeDescriptorNode extractOpenApiSchema(Schema<?> schema, String name,
                                                           List<NonTerminalNode> typeDefinitionNodeList,
                                                           JsonToRecordField typeDescField, boolean isRecordTypeDescriptor)
            throws JsonToRecordConverterException {

        if (schema.getType() != null || schema.getProperties() != null) {
            String schemaType = schema.getType();
            if ((schemaType.equals("integer") || schemaType.equals("number"))
                    || schemaType.equals("string") || schemaType.equals("boolean")) {
                String type = convertOpenAPITypeToBallerina(schemaType.trim());
                Token typeName = AbstractNodeFactory.createIdentifierToken(type);
                return createBuiltinSimpleNameReferenceNode(null, typeName);
            } else if (schemaType.equals("array") && schema instanceof ArraySchema) {
                final ArraySchema arraySchema = (ArraySchema) schema;

                if (arraySchema.getItems() != null) {
                    typeDescField.setIsArray(true);
                    Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
                    Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                    String type;
                    Token typeName;
                    TypeDescriptorNode memberTypeDesc;
                    if (arraySchema.getItems().getType().equals("object")) {
                        type = StringUtils.capitalize(name) + "Item";
                        typeName = AbstractNodeFactory.createIdentifierToken(type);
                        if (isRecordTypeDescriptor) {
                            memberTypeDesc = extractOpenApiSchema(arraySchema.getItems(), type, typeDefinitionNodeList,
                                    typeDescField, isRecordTypeDescriptor);
                        } else {
                            memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, typeName);
                            extractOpenApiSchema(arraySchema.getItems(), type, typeDefinitionNodeList, typeDescField,
                                    isRecordTypeDescriptor);
                        }
                    } else if (arraySchema.getItems() instanceof ArraySchema) {
                        memberTypeDesc = extractOpenApiSchema(arraySchema.getItems(), name, typeDefinitionNodeList,
                                typeDescField, isRecordTypeDescriptor);
                    } else {
                        type = arraySchema.getItems().getType();
                        typeName = AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(type));
                        memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, typeName);
                        typeDescField.setType(convertOpenAPITypeToBallerina(type));
                    }
                    return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openSBracketToken, null,
                            closeSBracketToken);
                }
            } else if (schemaType.equals("object") && schema.getProperties() != null) {
                Map<String, Schema> properties = schema.getProperties();
                List<String> required = schema.getRequired();
                Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
                IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(
                        escapeIdentifier(StringUtils.capitalize(name)));
                Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
                Token bodyStartDelimiter = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
                Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
                List<Node> recordFList = new ArrayList<>();
                for (Map.Entry<String, Schema> property: properties.entrySet()) {
                    String fieldType;
                    JsonToRecordField nestedTypeDescField;
                    if (property.getValue().getType().equals("object")) {
                        fieldType = SyntaxKind.RECORD_KEYWORD.stringValue();
                        nestedTypeDescField = new JsonToRecordField(property.getKey(), fieldType,
                                false, new ArrayList<>());
                    } else {
                        fieldType = property.getValue().getType();
                        nestedTypeDescField = new JsonToRecordField(property.getKey(),
                                convertOpenAPITypeToBallerina(fieldType), false, new ArrayList<>());
                    }
                    typeDescField.getFields().add(nestedTypeDescField);
                    addRecordFields(required, recordFList, property, typeDefinitionNodeList, nestedTypeDescField,
                            isRecordTypeDescriptor);
                }
                NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFList);
                TypeDescriptorNode typeDescriptorNode = NodeFactory.createRecordTypeDescriptorNode(recordKeyWord,
                        bodyStartDelimiter, fieldNodes, null, bodyEndDelimiter);

                if (isRecordTypeDescriptor) {
                    return typeDescriptorNode;
                } else {
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                            null, typeKeyWord, typeName, typeDescriptorNode, semicolon);
                    typeDefinitionNodeList.add(typeDefinitionNode);
                    Token refTypeName = AbstractNodeFactory.createIdentifierToken(StringUtils.capitalize(name));
                    return createBuiltinSimpleNameReferenceNode(null, refTypeName);
                }

            } else {
                Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
                return createBuiltinSimpleNameReferenceNode(null, typeName);
            }
        } else if (schema.get$ref() != null) {
            Token typeName = AbstractNodeFactory.createIdentifierToken(extractReferenceType(schema.get$ref()));
            return createBuiltinSimpleNameReferenceNode(null, typeName);
        } else {
            //This contains a fallback to Ballerina common type `any` if the OpenApi specification type is not defined
            // or not compatible with any of the current Ballerina types.
            Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
            return createBuiltinSimpleNameReferenceNode(null, typeName);
        }
        Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
        return createBuiltinSimpleNameReferenceNode(null, typeName);
    }

    /**
     * Parse and get the {@link OpenAPI} for the given json Schema String contract.
     *
     * @param schemaString     Json Schema as a string
     * @return {@link OpenAPI}  OpenAPI model
     * @throws JsonToRecordConverterException In case of invalid schema
     * @throws IOException In case of Json parse error
     */
    private static OpenAPI parseJSONSchema(String schemaString, String recordName) throws
            JsonToRecordConverterException, IOException {
        final String prefix = "{\n" +
                "  \"openapi\" : \"3.0.1\",\n" +
                "  \"info\" : {\n" +
                "    \"title\" : \" payloadV\",\n" +
                "    \"version\" : \"1.0.0\"\n" +
                "  },\n" +
                "  \"servers\" : [],\n" +
                "  \"paths\" : {},\n" +
                "  \"components\" : {\n" +
                "    \"schemas\" : {\n" +
                "      \"" + recordName + "\" : ";

        final String suffix = "\n" +
                "    }\n" +
                "  }\n" +
                "}";

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(schemaString, new TypeReference<>() { });
        Map<String, Object> cleanedMap = cleanSchema(jsonMap);
        String openAPISchemaString = objectMapper.writeValueAsString(cleanedMap);

        String openAPIFileContent = prefix + openAPISchemaString + suffix;
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent);
        if (!parseResult.getMessages().isEmpty()) {
            throw new JsonToRecordConverterException(ErrorMessages.parserException(schemaString));
        }
        return parseResult.getOpenAPI();
    }

    /**
     * Take a schema and recursively clean unsupported keywords.
     *
     * @param schemaMap   Json Schema as a map
     * @return {@link Map}  Cleaned json schema
     * @throws JsonToRecordConverterException In case of multiple types
     */
    private static Map<String, Object> cleanSchema(Map<String, Object> schemaMap)
            throws JsonToRecordConverterException {
        Map<String, Object> cleanedMap = removeUnsupportedKeywords(schemaMap);
        // check for multiple or null types
        if (!(cleanedMap.get("type") instanceof String)) {
            throw new JsonToRecordConverterException(ErrorMessages.multipleTypes(cleanedMap.toString()));
        }

        if (cleanedMap.get("type").equals("object")) {
            Map<String, Object> properties = (Map<String, Object>) cleanedMap.get("properties");
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                Map<String, Object> property = (Map<String, Object>) entry.getValue();
                properties.replace(entry.getKey(), cleanSchema(property));
            }
            cleanedMap.replace("properties", properties);
        } else if (cleanedMap.get("type").equals("array")) {
            Map<String, Object> itemsSchema = (Map<String, Object>) cleanedMap.get("items");
            cleanedMap.replace("items", cleanSchema(itemsSchema));
        }

        return cleanedMap;
    }

    /**
     * Remove the unsupported keywords in json Schema.
     *
     * Some keywords like $schema are not supported by OpenApi
     * Others like anyOf/allOf are redundant when converting to Ballerina records
     *
     * @param jsonMap   Json Schema as a map
     * @return {@link Map}  Cleaned json schema
     */
    private static Map<String, Object> removeUnsupportedKeywords(Map<String, Object> jsonMap) {
        for (String keyword : Constants.OPEN_API_UNSUPPORTED_KEYWORDS) {
            jsonMap.remove(keyword);
        }
        return jsonMap;
    }
}

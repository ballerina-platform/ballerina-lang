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
import org.ballerinalang.formatter.core.options.ForceFormattingOptions;
import org.ballerinalang.formatter.core.options.FormattingOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
     * @deprecated
     * This method takes in a json string and returns the Ballerina code block.
     * 
     * <p> Use {@link JsonToRecordConverter#convert(String, String, boolean, boolean, boolean)}} instead.
     *
     * @param jsonString Json string for the schema
     * @param recordName Name of the generated record
     * @param isRecordTypeDesc To denote final record, a record type descriptor
     * @param isClosed To denote the whether the response record is closed
     * @return {@link String} Ballerina code block
     * @throws IOException In case of Json parse error
     * @throws JsonToRecordConverterException In case of invalid schema
     * @throws FormatterException In case of invalid syntax
     */
    @Deprecated
    public static JsonToRecordResponse convert(String jsonString, String recordName, boolean isRecordTypeDesc,
                                               boolean isClosed) throws IOException,
            JsonToRecordConverterException, FormatterException {
        return convert(jsonString, recordName, isRecordTypeDesc, isClosed, false);
    }

    /**
     * This method takes in a json string and returns the Ballerina code block.
     *
     * @param jsonString Json string for the schema
     * @param recordName Name of the generated record
     * @param isRecordTypeDesc To denote final record, a record type descriptor
     * @param isClosed To denote the whether the response record is closed
     * @param forceFormatRecordFields To denote whether the inline records to be formatted for multi-line or in-line
     * @return {@link String} Ballerina code block
     * @throws IOException In case of Json parse error
     * @throws JsonToRecordConverterException In case of invalid schema
     * @throws FormatterException In case of invalid syntax
     */
    public static JsonToRecordResponse convert(String jsonString, String recordName, boolean isRecordTypeDesc,
                                               boolean isClosed, boolean forceFormatRecordFields) throws IOException,
            JsonToRecordConverterException, FormatterException {
        String name = ((recordName != null) && !recordName.isEmpty()) ? recordName : "NewRecord";
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

        Map<String, NonTerminalNode> typeDefinitionNodes = generateRecords(model, isRecordTypeDesc, isClosed);
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        JsonToRecordResponse response = new JsonToRecordResponse();

        ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder()
                .setForceFormatRecordFields(forceFormatRecordFields).build();
        FormattingOptions formattingOptions = FormattingOptions.builder()
                .setForceFormattingOptions(forceFormattingOptions).build();
        if (isRecordTypeDesc) {
            // Sets generated type definition code block when sub field of type descriptor kind
            RecordTypeDescriptorNode typeDescriptorNode = (RecordTypeDescriptorNode) typeDefinitionNodes.entrySet()
                    .iterator().next().getValue();
            Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
            Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
            IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(
                    escapeIdentifier(name));
            TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                    null, typeKeyWord, typeName, typeDescriptorNode, semicolon);
            NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.
                    createNodeList(typeDefinitionNode);
            Token eofToken = AbstractNodeFactory.createIdentifierToken("");
            ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
            response.setCodeBlock(Formatter.format(modulePartNode.syntaxTree(), formattingOptions).toSourceCode());
        } else {
            // Sets generated type definition code block
            NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
                    new ArrayList(typeDefinitionNodes.values()));
            Token eofToken = AbstractNodeFactory.createIdentifierToken("");
            ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
            response.setCodeBlock(Formatter.format(modulePartNode.syntaxTree(), formattingOptions).toSourceCode());
        }

        return response;
    }

    /**
     * Generates Ballerina Record nodes for given OpenAPI model.
     *
     * @param openApi OpenAPI model
     * @param isRecordTypeDescriptor To denote the record, a type descriptor
     * @param isClosedRecord Denotes the record, a closed record
     * @return {@link Map}  Map of Record Nodes
     * @throws JsonToRecordConverterException In case of bad record fields
     */
    private static Map<String, NonTerminalNode> generateRecords(OpenAPI openApi, boolean isRecordTypeDescriptor,
                                                              boolean isClosedRecord)
            throws JsonToRecordConverterException {
        Map<String, NonTerminalNode> typeDefinitionNodes = new LinkedHashMap<>();

        Components components = openApi.getComponents();

        if (components.getSchemas() == null || openApi.getComponents() == null) {
            return new LinkedHashMap<>(typeDefinitionNodes);
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
                        addRecordFields(required, recordFieldList, field, typeDefinitionNodes,
                                isRecordTypeDescriptor);
                    }
                }
                NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
                Token bodyEndDelimiter = isClosedRecord ?
                        AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_PIPE_TOKEN) :
                        AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
                RecordTypeDescriptorNode recordTypeDescriptorNode =
                        NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                                fieldNodes, null, bodyEndDelimiter);
                String key = schema.getKey().trim();
                if (isRecordTypeDescriptor) {
                    typeDefinitionNodes.put(key, recordTypeDescriptorNode);
                } else {
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                            null, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
                    typeDefinitionNodes.put(key, typeDefinitionNode);
                }
            } else if (schemaType.equals("array")) {
                if (schemaValue instanceof ArraySchema arraySchema) {
                    Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
                    Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                    IdentifierToken fieldName =
                            AbstractNodeFactory.createIdentifierToken(escapeIdentifier(
                                    schema.getKey().trim().toLowerCase(Locale.ENGLISH)) + "list");
                    Token semicolonToken = AbstractNodeFactory.createIdentifierToken(";");
                    TypeDescriptorNode fieldTypeName;
                    if (arraySchema.getItems() != null) {
                        fieldTypeName = extractOpenApiSchema(arraySchema.getItems(), schema.getKey(),
                                typeDefinitionNodes, isRecordTypeDescriptor);
                    } else {
                        Token type =
                                AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);
                        fieldTypeName =  NodeFactory.createBuiltinSimpleNameReferenceNode(null, type);
                    }
                    
                    ArrayTypeDescriptorNode arrayField = createArrayTypeDesc(fieldTypeName, openSBracketToken,
                                    null, closeSBracketToken);
                    
                    RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null,
                            null, arrayField, fieldName, null, semicolonToken);
                    
                    NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldNode);
                    Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
                    RecordTypeDescriptorNode recordTypeDescriptorNode =
                            NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                                    fieldNodes, null, bodyEndDelimiter);
                    if (isRecordTypeDescriptor) {
                        typeDefinitionNodes.put(schema.getKey().trim() + "List", recordTypeDescriptorNode);
                    } else {
                        Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                        IdentifierToken arrayIdentifier = AbstractNodeFactory.createIdentifierToken(
                                escapeIdentifier(schema.getKey().trim() + "List"));
                        TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                                null, typeKeyWord, arrayIdentifier, recordTypeDescriptorNode, semicolon);
                        typeDefinitionNodes.put(schema.getKey().trim() + "List", typeDefinitionNode);
                    }
                }
            }
        }
        return new LinkedHashMap<>(typeDefinitionNodes);
    }

    /**
     * Method for generating record fields with given schema properties.
     *
     * @param required List of required parameters
     * @param recordFieldList Record field list to which the field will be added
     * @param field Schema entry of the field
     * @param isRecordTypeDescriptor To denote the record, a type descriptor
     * @param typeDefinitionNodes Map of type definition nodes to be updated in case of object type fields
     * @throws JsonToRecordConverterException In case of bad schema entries
     */
    private static void addRecordFields(List<String> required, List<Node> recordFieldList,
                                        Map.Entry<String, Schema> field,
                                        Map<String, NonTerminalNode> typeDefinitionNodes,
                                        boolean isRecordTypeDescriptor)
            throws JsonToRecordConverterException {

        TypeDescriptorNode fieldTypeName = extractOpenApiSchema(field.getValue(), field.getKey(),
                typeDefinitionNodes, isRecordTypeDescriptor);
        IdentifierToken fieldName =
                AbstractNodeFactory.createIdentifierToken(escapeIdentifier(field.getKey().trim()));
        Token questionMarkToken = (required != null && required.contains(field.getKey().trim()))
                ? null
                : AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
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
     * @param typeDefinitionNodes Map of type definition nodes to be updated in case of object type fields
     * @param isRecordTypeDescriptor To denote the record, a type descriptor
     * @return {@link TypeDescriptorNode} Type descriptor for record field
     * @throws JsonToRecordConverterException In case of invalid schema
     */
    private static TypeDescriptorNode extractOpenApiSchema(Schema<?> schema, String name,
                                                           Map<String, NonTerminalNode> typeDefinitionNodes,
                                                           boolean isRecordTypeDescriptor)
            throws JsonToRecordConverterException {

        if (schema.getType() != null || schema.getProperties() != null) {
            String schemaType = schema.getType();
            if ((schemaType.equals("integer") || schemaType.equals("number"))
                    || schemaType.equals("string") || schemaType.equals("boolean")) {
                String type = convertOpenAPITypeToBallerina(schemaType.trim());
                Token typeName = AbstractNodeFactory.createIdentifierToken(type);
                return createBuiltinSimpleNameReferenceNode(null, typeName);
            } else if (schemaType.equals("array") && schema instanceof ArraySchema arraySchema) {

                if (arraySchema.getItems() != null) {
                    Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
                    Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                    String type;
                    Token typeName;
                    TypeDescriptorNode memberTypeDesc;
                    if (arraySchema.getItems().getType() != null && arraySchema.getItems().getType().equals("object")) {
                        type = StringUtils.capitalize(name) + "Item";
                        typeName = AbstractNodeFactory.createIdentifierToken(type);
                        if (isRecordTypeDescriptor) {
                            memberTypeDesc = extractOpenApiSchema(arraySchema.getItems(), type, typeDefinitionNodes,
                                    isRecordTypeDescriptor);
                        } else {
                            memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, typeName);
                            extractOpenApiSchema(arraySchema.getItems(), type, typeDefinitionNodes,
                                    isRecordTypeDescriptor);
                        }
                    } else if (arraySchema.getItems() instanceof ArraySchema) {
                        memberTypeDesc = extractOpenApiSchema(arraySchema.getItems(), name, typeDefinitionNodes,
                                isRecordTypeDescriptor);
                    } else {
                        type = arraySchema.getItems().getType();
                        typeName = AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(type));
                        memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, typeName);
                    }
                    return createArrayTypeDesc(memberTypeDesc, openSBracketToken, null,
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
                    addRecordFields(required, recordFList, property, typeDefinitionNodes,
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
                    typeDefinitionNodes.put(StringUtils.capitalize(name), typeDefinitionNode);
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
    
    private static ArrayTypeDescriptorNode createArrayTypeDesc(TypeDescriptorNode memberTypeDesc, 
                                                               Token openBracketToken, Node arrayLengthNode, 
                                                               Token closeBracketToken) {
        NodeList<ArrayDimensionNode> arrayDimensions = NodeFactory.createEmptyNodeList();
        if (memberTypeDesc.kind() == SyntaxKind.ARRAY_TYPE_DESC) {
            ArrayTypeDescriptorNode innerArrayType = (ArrayTypeDescriptorNode) memberTypeDesc;
            arrayDimensions = innerArrayType.dimensions();
            memberTypeDesc = innerArrayType.memberTypeDesc();
        }

        ArrayDimensionNode arrayDimension = NodeFactory.createArrayDimensionNode(openBracketToken, arrayLengthNode,
                closeBracketToken);
        arrayDimensions = arrayDimensions.add(arrayDimension);

        return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, arrayDimensions);
    }

    /**
     * Parse and get the {@link OpenAPI} for the given json Schema String contract.
     *
     * @param schemaString     Json Schema as a string
     * @param recordName Name of the record
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

        final String suffix = """

                    }
                  }
                }""";

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
            Map<String, Object> cleanedSchema = (itemsSchema != null && !itemsSchema.isEmpty()) ?
                    cleanSchema(itemsSchema) : itemsSchema;
            cleanedMap.replace("items", cleanedSchema);
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

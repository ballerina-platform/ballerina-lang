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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.converters.exception.ConverterException;
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

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
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
    private static final PrintStream outStream = System.err;
    private static List<TypeDefinitionNode> typeDefinitionNodeList;

    /**
     * This method takes in a json Schema and returns the Ballerina record nodes.
     *
     * @param jsonSchema json string for the schema
     * @return {@link ArrayList<TypeDefinitionNode>} arraylist of type nodes
     * @throws IOException in case of Json parse error
     * @throws ConverterException in case of invalid schema
     */
    public static ArrayList<TypeDefinitionNode> fromSchema(String jsonSchema) throws IOException,
            ConverterException {
        OpenAPI model = parseJSONSchema(jsonSchema);
        return generateRecords(model);
    }

    /**
     * This method takes in a json object and returns the Ballerina record nodes.
     *
     * @param jsonString json object string
     * @return {@link ArrayList<TypeDefinitionNode>} arraylist of type nodes
     * @throws IOException in case of Json parse error
     * @throws ConverterException in case of invalid schema
     */
    public static ArrayList<TypeDefinitionNode> fromJSON(String jsonString) throws ConverterException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode inputJsonObject = objectMapper.readTree(jsonString);
        Map<String, Object> schema = SchemaGenerator.generate(inputJsonObject);
        String schemaJson = objectMapper.writeValueAsString(schema);
        OpenAPI model = parseJSONSchema(schemaJson);
        return generateRecords(model);
    }

    /**
     * Generates Ballerina Record nodes for given OpenAPI model.
     *
     * @param openApi OpenAPI model
     * @return {@link ArrayList<TypeDefinitionNode>}  List of Record Nodes
     * @throws ConverterException in case of bad record fields
     */
    public static ArrayList<TypeDefinitionNode> generateRecords(OpenAPI openApi) throws ConverterException {
        // TypeDefinitionNodes their
        typeDefinitionNodeList = new LinkedList<>();

        if (openApi.getComponents() == null) {
            return new ArrayList<>(typeDefinitionNodeList);
        }

        //Create typeDefinitionNode
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

            Token bodyStartDelimiter = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);

            //Generate RecordFiled
            List<Node> recordFieldList = new ArrayList<>();
            Schema schemaValue = schema.getValue();
            String schemaType = schemaValue.getType();
            if (schema.getValue().getProperties() != null || schema.getValue() instanceof ObjectSchema) {
                Map<String, Schema> fields = schema.getValue().getProperties();
                if (fields != null) {
                    for (Map.Entry<String, Schema> field : fields.entrySet()) {
                        addRecordFields(required, recordFieldList, field);
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
                typeDefinitionNodeList.add(typeDefinitionNode);
            } else if (schemaType.equals("array")) {
                if (schemaValue instanceof ArraySchema) {
                    ArraySchema arraySchema = (ArraySchema) schemaValue;
                    Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
                    Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                    IdentifierToken fieldName =
                            AbstractNodeFactory.createIdentifierToken(escapeIdentifier(
                                    schema.getKey().trim().toLowerCase(Locale.ENGLISH)) + "list");
                    Token semicolonToken = AbstractNodeFactory.createIdentifierToken(";");
                    TypeDescriptorNode fieldTypeName;
                    if (arraySchema.getItems() != null) {
                        //Generate RecordFiled
                        //FiledName
                        fieldTypeName = extractOpenApiSchema(arraySchema.getItems(), schema.getKey());
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
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                            null, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
                    typeDefinitionNodeList.add(typeDefinitionNode);
                }
            }
        }


        return new ArrayList<>(typeDefinitionNodeList);
    }

    /**
     * method for generating record fields with given schema properties.
     *
     * @param field schema entry of the field
     * @param recordFieldList record field list
     * @param required is it a required field.
     * @throws ConverterException in case of bad schema entries
     */
    private static void addRecordFields(List<String> required, List<Node> recordFieldList,
                                        Map.Entry<String, Schema> field) throws ConverterException {

        RecordFieldNode recordFieldNode;
        //FiledName
        IdentifierToken fieldName =
                AbstractNodeFactory.createIdentifierToken(escapeIdentifier(field.getKey().trim()));

        TypeDescriptorNode fieldTypeName = extractOpenApiSchema(field.getValue(), field.getKey());
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        Token questionMarkToken = (required != null && required.contains(field.getKey().trim()))
                ? null
                : AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);

        recordFieldNode = NodeFactory.createRecordFieldNode(null, null, fieldTypeName, fieldName,
                questionMarkToken, semicolonToken);

        recordFieldList.add(recordFieldNode);
    }

    /**
     * Common method to extract OpenApi Schema type objects in to Ballerina type compatible schema objects.
     *
     * @param schema OpenApi Schema
     * @param name Name of the field
     * @throws ConverterException in case of invalid schema
     */
    private static TypeDescriptorNode extractOpenApiSchema(Schema schema, String name) throws
            ConverterException {

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
                    //single array
                    Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
                    Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                    String type;
                    Token typeName;
                    TypeDescriptorNode memberTypeDesc;
                    if (arraySchema.getItems().getType().equals("object")) {
                        type = StringUtils.capitalize(name) + "Item";
                        typeName = AbstractNodeFactory.createIdentifierToken(type);
                        memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, typeName);
                        extractOpenApiSchema(arraySchema.getItems(), type);
                        return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openSBracketToken,
                                null, closeSBracketToken);
                    } else if (arraySchema.getItems() instanceof ArraySchema) {
                        memberTypeDesc = extractOpenApiSchema(arraySchema.getItems(), name);
                        return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openSBracketToken,
                                null, closeSBracketToken);
                    } else {
                        type = arraySchema.getItems().getType();
                        typeName = AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(type));
                        memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, typeName);
                        return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openSBracketToken,
                                null, closeSBracketToken);
                    }
                }
            } else if (schemaType.equals("object") && schema.getProperties() != null) {
                Map<String, Schema> properties = schema.getProperties();
                List<String> required = schema.getRequired();
                //1.typeKeyWord
                Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
                //2.typeName
                IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(
                        escapeIdentifier(StringUtils.capitalize(name)));
                Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
                Token bodyStartDelimiter = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
                Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
                List<Node> recordFList = new ArrayList<>();
                for (Map.Entry<String, Schema> property: properties.entrySet()) {
                    addRecordFields(required, recordFList, property);
                }
                NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFList);
                TypeDescriptorNode typeDescriptorNode = NodeFactory.createRecordTypeDescriptorNode(recordKeyWord,
                        bodyStartDelimiter, fieldNodes, null, bodyEndDelimiter);
                Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                        null, typeKeyWord, typeName, typeDescriptorNode, semicolon);
                typeDefinitionNodeList.add(typeDefinitionNode);
                Token refTypeName = AbstractNodeFactory.createIdentifierToken(StringUtils.capitalize(name));
                return createBuiltinSimpleNameReferenceNode(null, refTypeName);

            } else {
                outStream.println("Encountered an unsupported type. Type `any` would be used for the field.");
                Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
                return createBuiltinSimpleNameReferenceNode(null, typeName);
            }
        } else if (schema.get$ref() != null) {
            Token typeName = AbstractNodeFactory.createIdentifierToken(extractReferenceType(schema.get$ref()));
            return createBuiltinSimpleNameReferenceNode(null, typeName);
        } else {
            //This contains a fallback to Ballerina common type `any` if the OpenApi specification type is not defined
            // or not compatible with any of the current Ballerina types.
            outStream.println("Encountered an unsupported type. Type `any` would be used for the field.");
            Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
            return createBuiltinSimpleNameReferenceNode(null, typeName);
        }
        Token typeName = AbstractNodeFactory.createToken(SyntaxKind.ANY_KEYWORD);
        return createBuiltinSimpleNameReferenceNode(null, typeName);
    }

    /**
     * Parse and get the {@link OpenAPI} for the given json Schema String contract.
     *
     * @param schemaString     json Schema as a string
     * @return {@link OpenAPI}  OpenAPI model
     * @throws ConverterException in case of invalid schema
     * @throws IOException in case of Json parse error
     */
    public static OpenAPI parseJSONSchema(String schemaString) throws ConverterException, IOException {
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
                "      \"NewRecord\" : ";

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
            throw new ConverterException(ErrorMessages.parserException(schemaString));
        }
        return parseResult.getOpenAPI();
    }

    /**
     * Take a schema and recursively clean unsupported keywords.
     *
     * @param schemaMap   Json Schema as a map
     * @return {@link Map}  cleaned json schema
     * @throws ConverterException in case of multiple types
     */
    public static Map<String, Object> cleanSchema(Map<String, Object> schemaMap) throws ConverterException {
        Map<String, Object> cleanedMap = removeUnsupportedKeywords(schemaMap);
        // check for multiple or null types
        if (!(cleanedMap.get("type") instanceof String)) {
            throw new ConverterException(ErrorMessages.multipleTypes(cleanedMap.toString()));
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
     * @return {@link Map}  cleaned json schema
     */
    public static Map<String, Object> removeUnsupportedKeywords(Map<String, Object> jsonMap) {
        jsonMap.remove("$schema");
        jsonMap.remove("$id");
        jsonMap.remove("id");
        jsonMap.remove("additionalItems");
        jsonMap.remove("const");
        jsonMap.remove("contains");
        jsonMap.remove("dependencies");
        jsonMap.remove("patternProperties");
        jsonMap.remove("propertyNames");

        jsonMap.remove("format");
        jsonMap.remove("examples");
        jsonMap.remove("title");
        jsonMap.remove("description");
        jsonMap.remove("allOf");
        jsonMap.remove("oneOf");
        jsonMap.remove("anyOf");
        jsonMap.remove("not");

        return jsonMap;
    }
}

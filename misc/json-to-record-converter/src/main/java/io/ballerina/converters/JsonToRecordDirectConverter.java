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


import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.*;

//import com.google.gson.JsonParser;

/**
 * API for converting JSON string to Ballerina Records directly.
 *
 * @since 2.0.0
 */
public class JsonToRecordDirectConverter {

    private JsonToRecordDirectConverter() {}

    public static JsonToRecordResponse convert(String jsonString) throws FormatterException {
        Map<String, NonTerminalNode> typeDefinitionNodes = generateRecords();
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        JsonToRecordResponse response = new JsonToRecordResponse();

        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
                new ArrayList(typeDefinitionNodes.values()));
        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
        response.setCodeBlock(Formatter.format(modulePartNode.syntaxTree()).toSourceCode());

        return response;
    }

    private static Map<String, NonTerminalNode> generateRecords() {
        Map<String, NonTerminalNode> typeDefinitionNodes = new LinkedHashMap<>();

        Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);

        IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken("Test");

        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);

        Token bodyStartDelimiter = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);

        List<Node> recordFieldList = new ArrayList<>();
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);

        Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);

        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                null, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
        typeDefinitionNodes.put("key", typeDefinitionNode);


//        for (Map.Entry<String, Schema> schema: schemas.entrySet()) {
//            List<String> required = schema.getValue().getRequired();
//
//            Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
//
//            IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(
//                    escapeIdentifier(schema.getKey().trim()));
//
//            Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
//
//            Token bodyStartDelimiter = isClosedRecord ?
//                    AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_PIPE_TOKEN) :
//                    AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
//
//            List<Node> recordFieldList = new ArrayList<>();
//            Schema<?> schemaValue = schema.getValue();
//            String schemaType = schemaValue.getType();
//            if (schema.getValue().getProperties() != null || schema.getValue() instanceof ObjectSchema) {
//                Map<String, Schema> fields = schema.getValue().getProperties();
//                if (fields != null) {
//                    for (Map.Entry<String, Schema> field : fields.entrySet()) {
//                        addRecordFields(required, recordFieldList, field, typeDefinitionNodes,
//                                isRecordTypeDescriptor);
//                    }
//                }
//                NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
//                Token bodyEndDelimiter = isClosedRecord ?
//                        AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_PIPE_TOKEN) :
//                        AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
//                RecordTypeDescriptorNode recordTypeDescriptorNode =
//                        NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
//                                fieldNodes, null, bodyEndDelimiter);
//                String key = schema.getKey().trim();
//                if (isRecordTypeDescriptor) {
//                    typeDefinitionNodes.put(key, recordTypeDescriptorNode);
//                } else {
//                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
//                    TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
//                            null, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
//                    typeDefinitionNodes.put(key, typeDefinitionNode);
//                }
//            } else if (schemaType.equals("array")) {
//                if (schemaValue instanceof ArraySchema) {
//                    ArraySchema arraySchema = (ArraySchema) schemaValue;
//                    Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
//                    Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
//                    IdentifierToken fieldName =
//                            AbstractNodeFactory.createIdentifierToken(escapeIdentifier(
//                                    schema.getKey().trim().toLowerCase(Locale.ENGLISH)) + "list");
//                    Token semicolonToken = AbstractNodeFactory.createIdentifierToken(";");
//                    TypeDescriptorNode fieldTypeName;
//                    if (arraySchema.getItems() != null) {
//                        fieldTypeName = extractOpenApiSchema(arraySchema.getItems(), schema.getKey(),
//                                typeDefinitionNodes, isRecordTypeDescriptor);
//                    } else {
//                        Token type =
//                                AbstractNodeFactory.createToken(SyntaxKind.STRING_KEYWORD);
//                        fieldTypeName =  NodeFactory.createBuiltinSimpleNameReferenceNode(null, type);
//                    }
//
//                    ArrayTypeDescriptorNode arrayField = createArrayTypeDesc(fieldTypeName, openSBracketToken,
//                            null, closeSBracketToken);
//
//                    RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null,
//                            null, arrayField, fieldName, null, semicolonToken);
//
//                    NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldNode);
//                    Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
//                    RecordTypeDescriptorNode recordTypeDescriptorNode =
//                            NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
//                                    fieldNodes, null, bodyEndDelimiter);
//                    if (isRecordTypeDescriptor) {
//                        typeDefinitionNodes.put(schema.getKey().trim() + "List", recordTypeDescriptorNode);
//                    } else {
//                        Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
//                        IdentifierToken arrayIdentifier = AbstractNodeFactory.createIdentifierToken(
//                                escapeIdentifier(schema.getKey().trim() + "List"));
//                        TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
//                                null, typeKeyWord, arrayIdentifier, recordTypeDescriptorNode, semicolon);
//                        typeDefinitionNodes.put(schema.getKey().trim() + "List", typeDefinitionNode);
//                    }
//                }
//            }
//        }
        return new LinkedHashMap<>(typeDefinitionNodes);
    }
}

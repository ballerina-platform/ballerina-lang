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
        Map<String, NonTerminalNode> typeDefinitionNodes = generateRecords(JsonParser.parseString(jsonString).getAsJsonObject());
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        JsonToRecordResponse response = new JsonToRecordResponse();

        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
                new ArrayList(typeDefinitionNodes.values()));
        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
        response.setCodeBlock(Formatter.format(modulePartNode.syntaxTree()).toSourceCode());

        return response;
    }

    private static Map<String, NonTerminalNode> generateRecords(JsonObject jsonObject) {
        Map<String, NonTerminalNode> typeDefinitionNodes = new LinkedHashMap<>();

        Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);

        IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken("NewRecord");

        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);

        Token bodyStartDelimiter = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);

        List<Node> recordFieldList = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            recordFieldList.add(getRecordField(ConverterUtils.getPrimitiveTypeName(entry.getValue().getAsJsonPrimitive()), entry));
        }
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);

        Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);

        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null,
                null, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
        typeDefinitionNodes.put("key", typeDefinitionNode);

        return new LinkedHashMap<>(typeDefinitionNodes);
    }

    private static Node getRecordField(Token typeName, Map.Entry<String, JsonElement> element) {

        TypeDescriptorNode fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(element.getKey().trim());
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                fieldTypeName, fieldName, null, semicolonToken);

        return recordFieldNode;
    }

//    private void traverse(JsonObject object, Callable<Integer> func) {
//        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
//            if (entry.getValue().isJsonObject()) {
//                traverse(entry.getValue().getAsJsonObject(), null);
//            }
//        }
//    }
}

package io.ballerina.generators.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.generators.MetadataToRecordSchemaResponse;
import io.ballerina.generators.exceptions.MetadataToRecordGeneratorException;
import io.ballerina.generators.pojo.SalesforceFieldItem;
import io.ballerina.generators.pojo.SalesforceRecord;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldWithDefaultValueNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.generators.utils.Constants.SF_WHITESPACE_TYPE;
import static io.ballerina.generators.utils.ConverterUtils.convertSalesforceTypeToBallerina;
import static io.ballerina.generators.utils.ConverterUtils.escapeIdentifier;

/**
 * Converts the JSON object containing metadata of a Salesforce sObject to a Ballerina record.
 */
public class SalesforceMetadataConverter implements MetadataConverter {
    @Override
    public MetadataToRecordSchemaResponse convert(String metadata) throws MetadataToRecordGeneratorException {
        try {
            SalesforceRecord recordData = salesforceRecordExtractor(metadata);
            String name = ((recordData.getRecordName() != null) && !recordData.getRecordName().equals("")) ?
                    recordData.getRecordName() : "NewRecord";
            MetadataToRecordSchemaResponse response = new MetadataToRecordSchemaResponse();
            NodeList<ImportDeclarationNode> imports = createEmptyNodeList();
            List<Node> recordFieldList = new ArrayList<>();
            recordData.getFields().forEach((fieldData) -> {
                RecordFieldNode recordFieldNode = null;
                RecordFieldWithDefaultValueNode recordFieldNodeWithDefaultValue = null;

                String dataType = convertSalesforceTypeToBallerina(fieldData.getType());
                String fieldName = escapeIdentifier(fieldData.getName());
                boolean isNillable = fieldData.isNillable();
                if (fieldData.isUpdateable() || fieldData.isCreateable()) {
                    if (isNillable) {
                        recordFieldNodeWithDefaultValue = createRecordFieldWithDefaultValueNode(null, null,
                                createIdentifierToken(dataType +
                                        createToken(QUESTION_MARK_TOKEN) + SF_WHITESPACE_TYPE),
                                createIdentifierToken(fieldName),
                                createToken(EQUAL_TOKEN),
                                createSimpleNameReferenceNode(createIdentifierToken("()")),
                                createToken(SEMICOLON_TOKEN));
                        recordFieldList.add(recordFieldNodeWithDefaultValue);
                    } else {
                        recordFieldNode = createRecordFieldNode(null, null,
                                createIdentifierToken(dataType + SF_WHITESPACE_TYPE),
                                createIdentifierToken(fieldName),
                                createToken(QUESTION_MARK_TOKEN),
                                createToken(SEMICOLON_TOKEN));
                        recordFieldList.add(recordFieldNode);
                    }
                } else {
                    if (isNillable) {
                        recordFieldNode = createRecordFieldNode(null, null,
                                createIdentifierToken(dataType +
                                        createToken(QUESTION_MARK_TOKEN) + SF_WHITESPACE_TYPE),
                                createIdentifierToken(fieldName),
                                createToken(QUESTION_MARK_TOKEN),
                                createToken(SEMICOLON_TOKEN));
                        recordFieldList.add(recordFieldNode);
                    } else {
                        recordFieldNode = createRecordFieldNode(null, null,
                                createIdentifierToken(dataType + SF_WHITESPACE_TYPE),
                                createIdentifierToken(fieldName),
                                createToken(QUESTION_MARK_TOKEN),
                                createToken(SEMICOLON_TOKEN));
                        recordFieldList.add(recordFieldNode);
                    }
                }
            });

            NodeList<Node> recordFilds = createNodeList(recordFieldList);
            RecordTypeDescriptorNode customRecord = createRecordTypeDescriptorNode(
                    createToken(RECORD_KEYWORD),
                    createToken(OPEN_BRACE_TOKEN), recordFilds, null,
                    createToken(CLOSE_BRACE_TOKEN));
            TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
                    createToken(PUBLIC_KEYWORD),
                    createToken(TYPE_KEYWORD),
                    createIdentifierToken(name),
                    customRecord,
                    createToken(SEMICOLON_TOKEN));
            NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.
                    createNodeList(typeDefinitionNode);
            Token eofToken = AbstractNodeFactory.createIdentifierToken("");
            ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
            response.setCodeBlock(Formatter.format(modulePartNode.syntaxTree()).toSourceCode());
            return response;
        } catch (JsonProcessingException | FormatterException e) {
            throw new MetadataToRecordGeneratorException(e.getMessage());
        }
    }
    public static SalesforceRecord salesforceRecordExtractor(String metadata) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SalesforceRecord record = new SalesforceRecord();
        ObjectNode node = objectMapper.readValue(metadata, ObjectNode.class);
        if (node.has("name")) {
            record.setRecordName(node.get("name").textValue());
        }
        JsonNode fields = objectMapper.readTree(metadata).at("/fields");
        List<SalesforceFieldItem> fieldItemObjects =  new ArrayList<>();
        if (fields.isArray()) {
            ArrayNode items = (ArrayNode) fields;
            for (JsonNode item : items) {
                SalesforceFieldItem fieldItemObject = null;
                fieldItemObject = objectMapper.readValue(item.toString(), SalesforceFieldItem.class);
                fieldItemObjects.add(fieldItemObject);
            }
            record.setFields(fieldItemObjects);
        }
        return record;
    }
}

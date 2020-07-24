/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
 */
package org.ballerinalang.datamapper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.datamapper.utils.HttpClientRequest;
import org.ballerinalang.datamapper.utils.HttpResponse;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * Automatic data mapping code action related utils.
 */
class AIDataMapperCodeActionUtil {

    private static final int MAXIMUM_CACHE_SIZE = 100;
    private static Cache<Integer, String> mappingCache =
            CacheBuilder.newBuilder().maximumSize(MAXIMUM_CACHE_SIZE).build();

    /**
     * Returns the workspace edits for the automatic data mapping code action
     *
     * @param context     {@link LSContext}
     * @param refAtCursor {@link SymbolReferencesModel.Reference}
     * @param diagnostic  {@link Diagnostic}
     * @return edits for the data mapper code action
     * @throws IOException                throws if error occurred when getting generatedRecordMappingFunction
     * @throws WorkspaceDocumentException throws if error occurred when reading file content
     */
    static List<TextEdit> getAIDataMapperCodeActionEdits(LSContext context,
                                                         SymbolReferencesModel.Reference refAtCursor,
                                                         Diagnostic diagnostic)
            throws IOException, WorkspaceDocumentException {

        List<TextEdit> fEdits = new ArrayList<>();
        String diagnosticMessage = diagnostic.getMessage();
        Matcher matcher = CommandConstants.INCOMPATIBLE_TYPE_PATTERN.matcher(diagnosticMessage);

        if (!(matcher.find() && matcher.groupCount() > 1)) {
            return fEdits;
        }
        String foundTypeLeft = matcher.group(1)
                .replaceAll(refAtCursor.getSymbol().type.tsymbol.pkgID.toString() + ":",
                        "");  // variable at left side of the equal sign
        String foundTypeRight = matcher.group(2)
                .replaceAll(refAtCursor.getSymbol().type.tsymbol.pkgID.toString() + ":",
                        "");  // variable at right side of the equal sign

        // Insert function call in the code where error is found
        BLangNode bLangNode = refAtCursor.getbLangNode();
        Position startPos = new Position(bLangNode.pos.sLine - 1, bLangNode.pos.sCol - 1);
        Position endPosWithSemiColon = new Position(bLangNode.pos.eLine - 1, bLangNode.pos.eCol);
        Range newTextRange = new Range(startPos, endPosWithSemiColon);

        BSymbol symbolAtCursor = refAtCursor.getSymbol();
        String generatedFunctionName =
                String.format("map%sTo%s(%s);", foundTypeRight, foundTypeLeft, symbolAtCursor.name.value);
        fEdits.add(new TextEdit(newTextRange, generatedFunctionName));

        // Insert function declaration at the bottom of the file
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(context.get(DocumentServiceKeys.FILE_URI_KEY));
        if (filePath.isPresent()) {
            String fileContent = docManager.getFileContent(Paths.get(String.valueOf(filePath.get())));
            String functionName = String.format("map%sTo%s (%s", foundTypeRight, foundTypeLeft, foundTypeRight);
            if (!fileContent.contains(functionName)) {
                int numberOfLinesInFile = fileContent.split("\n").length;
                Position startPosOfLastLine = new Position(numberOfLinesInFile + 2, 0);
                Position endPosOfLastLine = new Position(numberOfLinesInFile + 2, 1);
                Range newFunctionRange = new Range(startPosOfLastLine, endPosOfLastLine);
                String generatedRecordMappingFunction =
                        getGeneratedRecordMappingFunction(bLangNode, symbolAtCursor, foundTypeLeft, foundTypeRight);
                fEdits.add(new TextEdit(newFunctionRange, generatedRecordMappingFunction));
            }
        }
        return fEdits;
    }

    /**
     * Given two record types, this returns a function with mapped schemas
     *
     * @param bLangNode      {@link BLangNode}
     * @param symbolAtCursor {@link BSymbol}
     * @param foundTypeLeft  {@link String}
     * @param foundTypeRight {@link String}
     * @return function string with mapped schemas
     * @throws IOException throws if error occurred when getting mapped function
     */
    private static String getGeneratedRecordMappingFunction(BLangNode bLangNode, BSymbol symbolAtCursor,
                                                            String foundTypeLeft, String foundTypeRight)
            throws IOException {

        JsonObject rightRecordJSON = new JsonObject();
        JsonObject leftRecordJSON = new JsonObject();

        // Schema 1
        BType variableTypeMappingFrom = symbolAtCursor.type;
        if (variableTypeMappingFrom instanceof BRecordType) {
            List<BField> rightSchemaFields = new ArrayList<>(((BRecordType) variableTypeMappingFrom).fields.values());
            JsonObject rightSchema = (JsonObject) recordToJSON(rightSchemaFields);

            rightRecordJSON.addProperty("schema", foundTypeRight);
            rightRecordJSON.addProperty("id", "dummy_id");
            rightRecordJSON.addProperty("type", "object");
            rightRecordJSON.add("properties", rightSchema);
        }
        // Schema 2
        BType variableTypeMappingTo = ((BLangSimpleVarRef) bLangNode).expectedType;
        if (variableTypeMappingTo instanceof BRecordType) {
            List<BField> leftSchemaFields = new ArrayList<>(((BRecordType) variableTypeMappingTo).fields.values());
            JsonObject leftSchema = (JsonObject) recordToJSON(leftSchemaFields);

            leftRecordJSON.addProperty("schema", foundTypeLeft);
            leftRecordJSON.addProperty("id", "dummy_id");
            leftRecordJSON.addProperty("type", "object");
            leftRecordJSON.add("properties", leftSchema);
        }

        JsonArray schemas = new JsonArray();
        schemas.add(leftRecordJSON);
        schemas.add(rightRecordJSON);
        return getMapping(schemas);
    }

    /**
     * For a give array of schemas, return a mapping function
     *
     * @param schemas {@link JsonArray}
     * @return mapped function
     * @throws IOException throws if an error occurred in HTTP request
     */
    private static String getMapping(JsonArray schemas) throws IOException {

        int hashCode = schemas.hashCode();
        if (mappingCache.asMap().containsKey(hashCode)) {
            return mappingCache.asMap().get(hashCode);
        }

        AIDataMapperNetworkUtil backEndService = new AIDataMapperNetworkUtil();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; utf-8");
        headers.put("Accept", "application/json");
        backEndService.setHeaders(headers);
        backEndService.setEndpoint(LSClientConfigHolder.getInstance().getConfig().getDataMapper().getUrl());
        backEndService.setDataToSend(schemas);
        try {
            String mappedFunction = AIDataMapperNetworkUtil.getMapping();
            mappingCache.put(hashCode, mappedFunction);
            return mappedFunction;
        } catch (IOException e){
            throw new IOException("Error connecting the AI service" + e.getMessage(), e);
        }
    }

    private static JsonElement recordToJSON(List<BField> schemaFields) {

        JsonObject properties = new JsonObject();
        for (BField attribute : schemaFields) {
            JsonObject fieldDetails = new JsonObject();
            fieldDetails.addProperty("id", "dummy_id");
            /* TODO: Do we need to go to lower levels?*/
            /* TODO: consider unifying the schema*/
            if (attribute.type instanceof BArrayType) {
                BType attributeEType = ((BArrayType) attribute.type).eType;
                if (attributeEType instanceof BRecordType) {
                    fieldDetails.addProperty("type", "ballerina_type");
                    fieldDetails.add("properties",
                            recordToJSON(new ArrayList<>(((BRecordType) attributeEType).fields.values())));
                } else {
                    fieldDetails.addProperty("type", String.valueOf(attribute.type));
                }
            } else if (attribute.type instanceof BRecordType) {
                fieldDetails.addProperty("type", "ballerina_type");
                fieldDetails.add("properties",
                        recordToJSON(new ArrayList<>(((BRecordType) attribute.type).fields.values())));
            } else {
                fieldDetails.addProperty("type", String.valueOf(attribute.type));
            }
            properties.add(String.valueOf(attribute.name), fieldDetails);
        }
        return properties;
    }
}

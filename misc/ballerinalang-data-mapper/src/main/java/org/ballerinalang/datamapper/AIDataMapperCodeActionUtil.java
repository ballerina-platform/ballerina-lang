/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    private static final int HTTP_200_OK = 200;
    private static final int HTTP_422_UN_PROCESSABLE_ENTITY = 422;
    private static final int HTTP_500_INTERNAL_SERVER_ERROR = 500;
    private static final int MAXIMUM_CACHE_SIZE = 100;
    private static Cache<Integer, String> mappingCache =
            CacheBuilder.newBuilder().maximumSize(MAXIMUM_CACHE_SIZE).build();

    private static final String SCHEMA = "schema";
    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String PROPERTIES = "properties";


    /**
     * Returns the workspace edits for the automatic data mapping code action.
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
     * Given two record types, this returns a function with mapped schemas.
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

            rightRecordJSON.addProperty(SCHEMA, foundTypeRight);
            rightRecordJSON.addProperty(ID, "dummy_id");
            rightRecordJSON.addProperty(TYPE, "object");
            rightRecordJSON.add(PROPERTIES, rightSchema);
        }
        // Schema 2
        BType variableTypeMappingTo = ((BLangSimpleVarRef) bLangNode).expectedType;
        if (variableTypeMappingTo instanceof BRecordType) {
            List<BField> leftSchemaFields = new ArrayList<>(((BRecordType) variableTypeMappingTo).fields.values());
            JsonObject leftSchema = (JsonObject) recordToJSON(leftSchemaFields);

            leftRecordJSON.addProperty(SCHEMA, foundTypeLeft);
            leftRecordJSON.addProperty(ID, "dummy_id");
            leftRecordJSON.addProperty(TYPE, "object");
            leftRecordJSON.add(PROPERTIES, leftSchema);
        }

        JsonArray schemas = new JsonArray();
        schemas.add(leftRecordJSON);
        schemas.add(rightRecordJSON);
        return getMapping(schemas);
    }

    /**
     * For a give array of schemas, return a mapping function.
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
        try {
            String mappedFunction = getMappingFromServer(schemas);
            mappingCache.put(hashCode, mappedFunction);
            return mappedFunction;
        } catch (IOException e) {
            throw new IOException("Error connecting the AI service" + e.getMessage(), e);
        }
    }

    private static JsonElement recordToJSON(List<BField> schemaFields) {
        JsonObject properties = new JsonObject();
        for (BField attribute : schemaFields) {
            JsonObject fieldDetails = new JsonObject();
            fieldDetails.addProperty(ID, "dummy_id");
            if (attribute.type instanceof BArrayType) {
                BType attributeEType = ((BArrayType) attribute.type).eType;
                if (attributeEType instanceof BRecordType) {
                    fieldDetails.addProperty(TYPE, "ballerina_type");
                    fieldDetails.add(PROPERTIES,
                            recordToJSON(new ArrayList<>(((BRecordType) attributeEType).fields.values())));
                } else {
                    fieldDetails.addProperty(TYPE, String.valueOf(attribute.type));
                }
            } else if (attribute.type instanceof BRecordType) {
                fieldDetails.addProperty(TYPE, "ballerina_type");
                fieldDetails.add(PROPERTIES,
                        recordToJSON(new ArrayList<>(((BRecordType) attribute.type).fields.values())));
            } else {
                fieldDetails.addProperty(TYPE, String.valueOf(attribute.type));
            }
            properties.add(String.valueOf(attribute.name), fieldDetails);
        }
        return properties;
    }

    private static String getMappingFromServer(JsonArray dataToSend) throws IOException {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json; utf-8");
            headers.put("Accept", "application/json");
            String url = LSClientConfigHolder.getInstance().getConfig().getDataMapper().getUrl() + "/map/1.0.0";
            HttpResponse response = HttpClientRequest.doPost(url, dataToSend.toString(), headers);
            int responseCode = response.getResponseCode();
            if (responseCode != HTTP_200_OK) {
                if (responseCode == HTTP_422_UN_PROCESSABLE_ENTITY) {
                    throw new IOException("Error: Un-processable data");
                } else if (responseCode == HTTP_500_INTERNAL_SERVER_ERROR) {
                    throw new IOException("Error: AI service error");
                }
            }
            JsonParser parser = new JsonParser();
            return parser.parse(response.getData()).getAsJsonObject().get("answer").getAsString();
        } catch (IOException e) {
            throw new IOException("Error connecting the AI service" + e.getMessage(), e);
        }
    }
}

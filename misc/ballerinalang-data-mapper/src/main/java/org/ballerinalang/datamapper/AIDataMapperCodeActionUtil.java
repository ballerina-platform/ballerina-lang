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
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import org.ballerinalang.datamapper.config.ClientExtendedConfigImpl;
import org.ballerinalang.datamapper.utils.HttpClientRequest;
import org.ballerinalang.datamapper.utils.HttpResponse;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * @param context    {@link CodeActionContext}
     * @param diagnostic {@link Diagnostic}
     * @return edits for the data mapper code action
     * @throws IOException throws if error occurred when getting generatedRecordMappingFunction
     */
    static List<TextEdit> getAIDataMapperCodeActionEdits(CodeActionContext context, Diagnostic diagnostic)
            throws IOException {
        List<TextEdit> fEdits = new ArrayList<>();
        String diagnosticMessage = diagnostic.getMessage();
        Matcher matcher = CommandConstants.INCOMPATIBLE_TYPE_PATTERN.matcher(diagnosticMessage);

        if (!(matcher.find() && matcher.groupCount() > 1)) {
            return fEdits;
        }

        String foundTypeLeft = matcher.group(1);
        String foundTypeRight = matcher.group(2);

        // Get the semantic model
        SemanticModel semanticModel = context.workspace().semanticModel(context.filePath()).orElseThrow();

        // To restrict the code action from appearing for handled cases.
        List<Symbol> fileContentSymbols = semanticModel.moduleLevelSymbols();
        boolean foundLeft = fileContentSymbols.stream().anyMatch(p -> p.name().contains(foundTypeLeft));
        boolean foundRight = fileContentSymbols.stream().anyMatch(p -> p.name().contains(foundTypeRight));

        if (!(foundRight && foundLeft)) {
            return fEdits;
        }

        // Insert function call in the code where error is found
        Range newTextRange = diagnostic.getRange();

        String filePath = context.filePath().getFileName().toString();
        LinePosition linePosition = LinePosition.from(context.cursorPosition().getLine(), context.cursorPosition().
                getCharacter());
        String symbolAtCursor = semanticModel.symbol(filePath, linePosition).get().name();

        String generatedFunctionName =
                String.format("map%sTo%s(%s)", foundTypeRight, foundTypeLeft, symbolAtCursor);
        fEdits.add(new TextEdit(newTextRange, generatedFunctionName));

        // Insert function declaration at the bottom of the file
        String functionName = String.format("map%sTo%s", foundTypeRight, foundTypeLeft);

        boolean found = fileContentSymbols.stream().anyMatch(p -> p.name().contains(functionName));
        if (!found) {
            TextDocument fileContentTextDocument = context.workspace().syntaxTree(context.filePath()).get().
                    textDocument();
            int numberOfLinesInFile = fileContentTextDocument.toString().split("\n").length;
            Position startPosOfLastLine = new Position(numberOfLinesInFile + 2, 0);
            Position endPosOfLastLine = new Position(numberOfLinesInFile + 2, 1);
            Range newFunctionRange = new Range(startPosOfLastLine, endPosOfLastLine);
            String generatedRecordMappingFunction =
                    getGeneratedRecordMappingFunction(context.positionDetails(), context, foundTypeLeft,
                            foundTypeRight);
            fEdits.add(new TextEdit(newFunctionRange, generatedRecordMappingFunction));
        }
        return fEdits;
    }

    /**
     * Given two record types, this returns a function with mapped schemas.
     *
     * @param positionDetails {@link PositionDetails}
     * @param context         {@link CodeActionContext}
     * @param foundTypeLeft   {@link String}
     * @param foundTypeRight  {@link String}
     * @return function string with mapped schemas
     * @throws IOException throws if error occurred when getting mapped function
     */
    private static String getGeneratedRecordMappingFunction(PositionDetails positionDetails,
                                                            CodeActionContext context,
                                                            String foundTypeLeft, String foundTypeRight)
            throws IOException {
        JsonObject rightRecordJSON = new JsonObject();
        JsonObject leftRecordJSON = new JsonObject();


        // Schema 1
        List<FieldSymbol> rightSchemaFields = SymbolUtil.getTypeDescForRecordSymbol(context.workspace().
                semanticModel(context.filePath()).get().symbol(context.filePath().getFileName().toString(),
                LinePosition.from(context.cursorPosition().getLine(), context.cursorPosition().getCharacter())).
                get()).fieldDescriptors();
        JsonObject rightSchema = (JsonObject) recordToJSON(rightSchemaFields);

        rightRecordJSON.addProperty(SCHEMA, foundTypeRight);
        rightRecordJSON.addProperty(ID, "dummy_id");
        rightRecordJSON.addProperty(TYPE, "object");
        rightRecordJSON.add(PROPERTIES, rightSchema);

        // Schema 2
        List<FieldSymbol> leftSchemaFields = SymbolUtil.getTypeDescForRecordSymbol(positionDetails.matchedSymbol()).
                fieldDescriptors();
        JsonObject leftSchema = (JsonObject) recordToJSON(leftSchemaFields);

        leftRecordJSON.addProperty(SCHEMA, foundTypeLeft);
        leftRecordJSON.addProperty(ID, "dummy_id");
        leftRecordJSON.addProperty(TYPE, "object");
        leftRecordJSON.add(PROPERTIES, leftSchema);

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

    /**
     * Convert record type symbols to json objects.
     *
     * @param schemaFields {@link List<FieldSymbol>}
     * @return Field symbol properties
     */
    private static JsonElement recordToJSON(List<FieldSymbol> schemaFields) {
        JsonObject properties = new JsonObject();
        for (FieldSymbol attribute : schemaFields) {
            JsonObject fieldDetails = new JsonObject();
            fieldDetails.addProperty(ID, "dummy_id");
            TypeSymbol attributeType = CommonUtil.getRawType(attribute.typeDescriptor());
            if (attributeType.typeKind() == TypeDescKind.RECORD) {
                List<FieldSymbol> recordFields = ((RecordTypeSymbol) attributeType).fieldDescriptors();
                fieldDetails.addProperty(TYPE, "ballerina_type");
                fieldDetails.add(PROPERTIES, recordToJSON(recordFields));
            } else {
                fieldDetails.addProperty(TYPE, attributeType.typeKind().toString());
            }
            properties.add(attribute.name(), fieldDetails);
        }
        return properties;
    }

    /**
     * Get the mapping from the Data Mapper service.
     *
     * @param dataToSend - payload to the service
     * @return - response data from the Data Mapper service
     * @throws IOException If an error occurs in the Data Mapper service
     */
    private static String getMappingFromServer(JsonArray dataToSend) throws IOException {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json; utf-8");
            headers.put("Accept", "application/json");
            String url = LSClientConfigHolder.getInstance().getConfigAs(ClientExtendedConfigImpl.class).getDataMapper()
                    .getUrl() + "/map/1.0.0";
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

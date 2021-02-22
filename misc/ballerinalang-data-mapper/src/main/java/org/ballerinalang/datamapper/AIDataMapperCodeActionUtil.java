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
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.properties.DiagnosticProperty;
import io.ballerina.tools.diagnostics.properties.DiagnosticPropertyKind;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import org.ballerinalang.datamapper.config.ClientExtendedConfigImpl;
import org.ballerinalang.datamapper.utils.HttpClientRequest;
import org.ballerinalang.datamapper.utils.HttpResponse;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
    private static final int RIGHT_SYMBOL_INDEX = 1;
    private static final int LEFT_SYMBOL_INDEX = 0;
    private static Cache<Integer, String> mappingCache =
            CacheBuilder.newBuilder().maximumSize(MAXIMUM_CACHE_SIZE).build();

    private static final String SCHEMA = "schema";
    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String PROPERTIES = "properties";


    /**
     * Returns the workspace edits for the automatic data mapping code action.
     *
     * @param positionDetails {@link DiagBasedPositionDetails}
     * @param context         {@link CodeActionContext}
     * @param diagnostic      {@link Diagnostic}
     * @return edits for the data mapper code action
     * @throws IOException throws if error occurred when getting generatedRecordMappingFunction
     */
    static List<TextEdit> getAIDataMapperCodeActionEdits(DiagBasedPositionDetails positionDetails,
                                                         CodeActionContext context, Diagnostic diagnostic)
            throws IOException {
        List<TextEdit> fEdits = new ArrayList<>();
        String diagnosticMessage = diagnostic.message();
        Matcher matcher = CommandConstants.INCOMPATIBLE_TYPE_PATTERN.matcher(diagnosticMessage);

        if (!(matcher.find() && matcher.groupCount() > 1)) {
            return fEdits;
        }

        Optional<Document> srcFile = context.workspace().document(context.filePath());
        if (srcFile.isEmpty()) {
            return fEdits;
        }

        List<DiagnosticProperty<?>> props = diagnostic.properties();
        TypeDescKind leftSymbolType = ((TypeSymbol) props.get(LEFT_SYMBOL_INDEX).value()).typeKind();

        if (props.size() != 2 || props.get(RIGHT_SYMBOL_INDEX).kind() != DiagnosticPropertyKind.SYMBOLIC ||
                props.get(LEFT_SYMBOL_INDEX).kind() != DiagnosticPropertyKind.SYMBOLIC ||
                ((leftSymbolType != TypeDescKind.TYPE_REFERENCE) && (leftSymbolType != TypeDescKind.RECORD) &&
                (leftSymbolType != TypeDescKind.UNION))) {
            return fEdits;
        } else {
            Symbol lftTypeSymbol = (Symbol) props.get(LEFT_SYMBOL_INDEX).value();
            Symbol rhsTypeSymbol = (Symbol) props.get(RIGHT_SYMBOL_INDEX).value();

            String foundTypeLeft = lftTypeSymbol.getName().orElse("");
            String foundTypeRight = rhsTypeSymbol.getName().orElse("");

            // Get the semantic model
            SemanticModel semanticModel = context.workspace().semanticModel(context.filePath()).orElseThrow();
            List<Symbol> fileContentSymbols = semanticModel.moduleSymbols();

            // Find the location to insert function call in the code where error is found
            Range newTextRange = CommonUtil.toRange(diagnostic.location().lineRange());
            LinePosition linePosition = diagnostic.location().lineRange().startLine();

            // get the symbol at cursor type
            Symbol symbolAtCursor = semanticModel.symbol(srcFile.get(), linePosition).get();
            String symbolAtCursorName = symbolAtCursor.getName().orElse("");
            String symbolAtCursorType = "";
            // Check if function return a record type in multi-module project
            if (SymbolUtil.getTypeDescriptor(symbolAtCursor).isEmpty()) {
                symbolAtCursorType = symbolAtCursor.kind().name();
            } else {
                symbolAtCursorType = SymbolUtil.getTypeDescriptor(symbolAtCursor).get().typeKind().toString();
            }

            // Generate the function name
            String generatedFunctionName;
            switch (symbolAtCursorType) {
                case "RECORD":
                case "TYPE_REFERENCE":
                    generatedFunctionName =
                            String.format("map%sTo%s(%s)", foundTypeRight, foundTypeLeft, symbolAtCursorName);
                    fEdits.add(new TextEdit(newTextRange, generatedFunctionName));
                    break;

                case "FUNCTION":
                case "MODULE":
                    boolean foundErrorLeft = false;
                    boolean foundErrorRight = false;

                    // If the function is returning a record | error
                    if ("".equals(foundTypeLeft)) {
                        List<TypeSymbol> leftTypeSymbols = ((UnionTypeSymbol) lftTypeSymbol).memberTypeDescriptors();
                        lftTypeSymbol = findSymbol(leftTypeSymbols);
                        foundTypeLeft = lftTypeSymbol.getName().orElse("");
                        foundErrorLeft = true;
                    }
                    // If the check or checkpanic is used
                    if ("".equals(foundTypeRight)) {
                        List<TypeSymbol> rightTypeSymbols = ((UnionTypeSymbol) rhsTypeSymbol).memberTypeDescriptors();
                        rhsTypeSymbol = findSymbol(rightTypeSymbols);
                        foundTypeRight = rhsTypeSymbol.getName().orElse("");
                        foundErrorRight = true;
                    }


                    String functionCall = positionDetails.matchedNode().toString();
                    if (foundErrorRight && !foundErrorLeft) {
                        symbolAtCursorName = functionCall.split("[=;]")[1].trim();
                        generatedFunctionName =
                                String.format("map%sTo%s(check %s)", foundTypeRight, foundTypeLeft, symbolAtCursorName);
                        fEdits.add(new TextEdit(newTextRange, generatedFunctionName));
                    } else if (foundErrorLeft && foundErrorRight) {
                        // get the information about the line positions
                        newTextRange = CommonUtil.toRange(positionDetails.matchedNode().lineRange());
                        generatedFunctionName =
                                String.format("map%sTo%s(%s)", foundTypeRight, foundTypeLeft, functionCall);
                        fEdits.add(new TextEdit(newTextRange, generatedFunctionName));
                    } else {
                        symbolAtCursorName = functionCall.split("[=;]")[1].trim();
                        generatedFunctionName =
                                String.format("map%sTo%s(%s)", foundTypeRight, foundTypeLeft, symbolAtCursorName);
                        fEdits.add(new TextEdit(newTextRange, generatedFunctionName));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + symbolAtCursorType);
            }

            // Insert function declaration at the bottom of the file
            String functionName = String.format("map%sTo%s", foundTypeRight, foundTypeLeft);
            boolean found = fileContentSymbols.stream().anyMatch(p -> p.getName().get().contains(functionName));
            if (found) {
                return fEdits;
            } else {
                // Get the last line of the file
                TextDocument fileContentTextDocument = context.workspace().syntaxTree(context.filePath()).get().
                        textDocument();
                int numberOfLinesInFile = fileContentTextDocument.toString().split("\n").length;
                Position startPosOfLastLine = new Position(numberOfLinesInFile + 2, 0);
                Position endPosOfLastLine = new Position(numberOfLinesInFile + 2, 1);
                Range newFunctionRange = new Range(startPosOfLastLine, endPosOfLastLine);

                // Get the generated record mapping function
                String mappingFromServer =
                        getGeneratedRecordMapping(context, foundTypeLeft,
                                foundTypeRight, lftTypeSymbol, rhsTypeSymbol);

                // To handle the multi-module projects
                String rightModule = null;
                String leftModule = null;
                Optional<Project> project = context.workspace().project(context.filePath());
                if (project.get().kind() == ProjectKind.BUILD_PROJECT) {
                    String moduleName = srcFile.get().module().moduleId().moduleName();

                    ModuleID rhsModule =
                            rhsTypeSymbol.getModule().isPresent() ? rhsTypeSymbol.getModule().get().id() : null;
                    ModuleID lftModule =
                            lftTypeSymbol.getModule().isPresent() ? lftTypeSymbol.getModule().get().id() : null;

                    if (rhsModule != null && !moduleName.equals(rhsModule.moduleName())) {
                        rightModule = rhsModule.modulePrefix();
                    }
                    if (lftModule != null && !moduleName.equals(lftModule.moduleName())) {
                        leftModule = lftModule.modulePrefix();
                    }
                }
                String generatedRecordMappingFunction = generateMappingFunction(mappingFromServer, foundTypeLeft,
                        foundTypeRight, leftModule, rightModule);
                fEdits.add(new TextEdit(newFunctionRange, generatedRecordMappingFunction));
                return fEdits;
            }
        }
    }

    /**
     * Check if a symbol is in the module level, and if so return the symbol.
     *
     * @param fileContentSymbols {@link List}
     * @return return the symbol
     */
    public static Symbol findSymbol(List<TypeSymbol> fileContentSymbols) {
        for (Symbol symbol : fileContentSymbols) {
            if (!"ERROR".equals(symbol.getName().get())) {
                return symbol;
            }
        }
        return null;
    }

    /**
     * Given two record types, this returns a function with mapped schemas.
     *
     * @param context
     * @param foundTypeLeft  {@link String}
     * @param foundTypeRight {@link String}
     * @return function string with mapped schemas
     * @throws IOException throws if error occurred when getting mapped function
     */
    private static String getGeneratedRecordMapping(CodeActionContext context, String foundTypeLeft,
                                                    String foundTypeRight,
                                                    Symbol lftTypeSymbol, Symbol rhsTypeSymbol)

            throws IOException {
        JsonObject rightRecordJSON = new JsonObject();
        JsonObject leftRecordJSON = new JsonObject();

        // Schema 1
        TypeSymbol rhsSymbol = ((TypeReferenceTypeSymbol) rhsTypeSymbol).typeDescriptor();
        if ("RECORD".equals(rhsSymbol.typeKind().name())) {
            RecordTypeSymbol rightSymbol = (RecordTypeSymbol) rhsSymbol;
            Map<String, RecordFieldSymbol> rightSchemaFields = rightSymbol.fieldDescriptors();
            JsonObject rightSchema = (JsonObject) recordToJSON(rightSchemaFields.values());

            rightRecordJSON.addProperty(SCHEMA, foundTypeRight);
            rightRecordJSON.addProperty(ID, "dummy_id");
            rightRecordJSON.addProperty(TYPE, "object");
            rightRecordJSON.add(PROPERTIES, rightSchema);
        }

        // Schema 2
        TypeSymbol lftSymbol = ((TypeReferenceTypeSymbol) lftTypeSymbol).typeDescriptor();
        if ("RECORD".equals(lftSymbol.typeKind().name())) {
            RecordTypeSymbol leftSymbol = (RecordTypeSymbol) lftSymbol;
            Map<String, RecordFieldSymbol> leftSchemaFields = leftSymbol.fieldDescriptors();

            JsonObject leftSchema = (JsonObject) recordToJSON(leftSchemaFields.values());
            leftRecordJSON.addProperty(SCHEMA, foundTypeLeft);
            leftRecordJSON.addProperty(ID, "dummy_id");
            leftRecordJSON.addProperty(TYPE, "object");
            leftRecordJSON.add(PROPERTIES, leftSchema);
        }


        JsonArray schemas = new JsonArray();
        schemas.add(leftRecordJSON);
        schemas.add(rightRecordJSON);
        return getMapping(schemas, context, foundTypeLeft, foundTypeRight);
    }

    /**
     * For a give array of schemas, return a mapping function.
     *
     * @param schemas        {@link JsonArray}
     * @param foundTypeLeft  {@link String}
     * @param foundTypeRight {@link String}
     * @return mapped function
     * @throws IOException throws if an error occurred in HTTP request
     */
    private static String getMapping(JsonArray schemas, CodeActionContext context, String foundTypeLeft,
                                     String foundTypeRight) throws IOException {
        int hashCode = schemas.hashCode();
        if (mappingCache.asMap().containsKey(hashCode)) {
            return mappingCache.asMap().get(hashCode);
        }
        try {
            String mappingFromServer = getMappingFromServer(schemas, context.languageServercontext());
            mappingCache.put(hashCode, mappingFromServer);
            return mappingFromServer;
        } catch (IOException e) {
            throw new IOException("Error connecting the AI service" + e.getMessage(), e);
        }
    }

    /**
     * Convert record type symbols to json objects.
     *
     * @param schemaFields {@link List<RecordFieldSymbol>}
     * @return Field symbol properties
     */
    private static JsonElement recordToJSON(Collection<RecordFieldSymbol> schemaFields) {
        JsonObject properties = new JsonObject();
        for (RecordFieldSymbol attribute : schemaFields) {
            JsonObject fieldDetails = new JsonObject();
            fieldDetails.addProperty(ID, "dummy_id");
            TypeSymbol attributeType = CommonUtil.getRawType(attribute.typeDescriptor());
            if (attributeType.typeKind() == TypeDescKind.RECORD) {
                Map<String, RecordFieldSymbol> recordFields = ((RecordTypeSymbol) attributeType).fieldDescriptors();
                fieldDetails.addProperty(TYPE, "ballerina_type");
                fieldDetails.add(PROPERTIES, recordToJSON(recordFields.values()));
            } else {
                fieldDetails.addProperty(TYPE, attributeType.typeKind().toString());
            }
            properties.add(attribute.getName().get(), fieldDetails);
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
    private static String getMappingFromServer(JsonArray dataToSend,
                                               LanguageServerContext serverContext) throws IOException {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json; utf-8");
            headers.put("Accept", "application/json");
            String url = LSClientConfigHolder.getInstance(serverContext)
                    .getConfigAs(ClientExtendedConfigImpl.class).getDataMapper()
                    .getUrl() + "/map/2.0.0";
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
            return parser.parse(response.getData()).getAsJsonObject().get("answer").toString();
        } catch (IOException e) {
            throw new IOException("Error connecting the AI service" + e.getMessage(), e);
        }
    }

    private static String generateMappingFunction(String mappingFromServer, String foundTypeLeft,
                                                  String foundTypeRight, String leftModule, String rightModule) {

        String leftType = foundTypeLeft;
        String rightType = foundTypeRight;

        mappingFromServer = mappingFromServer.replaceAll("\"", "");
        mappingFromServer = mappingFromServer.replaceAll(",", ", ");
        mappingFromServer = mappingFromServer.replaceAll(":", ": ");

        if (leftModule != null) {
            leftType = leftModule + ":" + foundTypeLeft;
        }
        if (rightModule != null) {
            rightType = rightModule + ":" + foundTypeRight;
        }

        String mappingFunction = "\nfunction map" + foundTypeRight + "To" + foundTypeLeft + " (" + rightType + " " +
                foundTypeRight.toLowerCase() + ") returns " + leftType + " {" +
                "\n// Some record fields might be missing in the AI based mapping." +
                "\n\t" + leftType + " " + foundTypeLeft.toLowerCase() + " = " + mappingFromServer + ";" +
                "\n\treturn " + foundTypeLeft.toLowerCase() + ";\n}\n";

        return mappingFunction;
    }
}

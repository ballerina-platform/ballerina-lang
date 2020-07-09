package org.ballerinalang.datamapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

class AIDataMapperCodeActionUtil {

    private static final int MAXIMUM_CACHE_SIZE = 100;
    private static Cache<Integer, String> mappingCache =
            CacheBuilder.newBuilder().maximumSize(MAXIMUM_CACHE_SIZE).build();

    static List<TextEdit> getAIDataMapperCodeActionEdits(LSContext context,
                                                         SymbolReferencesModel.Reference refAtCursor,
                                                         Diagnostic diagnostic)
            throws IOException, WorkspaceDocumentException {

        List<TextEdit> fEdits = new ArrayList<>();
        String diagnosticMessage = diagnostic.getMessage();
        Matcher matcher = CommandConstants.INCOMPATIBLE_TYPE_PATTERN.matcher(diagnosticMessage);

        if (matcher.find() && matcher.groupCount() > 1) {
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
            String variableAtCursor = symbolAtCursor.name.value;
            String generatedFunctionName =
                    String.format("map%sTo%s(%s);", foundTypeRight, foundTypeLeft, variableAtCursor);

            TextEdit functionNameEdit = new TextEdit(newTextRange, generatedFunctionName);
            fEdits.add(functionNameEdit);

            // Insert function declaration at the bottom of the file
            WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            Optional<Path> filePath = CommonUtil.getPathFromURI(context.get(DocumentServiceKeys.FILE_URI_KEY));
            if (filePath.isPresent()) {
                String fileContent = docManager.getFileContent(Paths.get(String.valueOf(filePath.get())));
                String functionName = String.format("map%sTo%s (%s", foundTypeRight, foundTypeLeft, foundTypeRight);
                int indexOfFunctionName = fileContent.indexOf(functionName);
                if (indexOfFunctionName == -1) {
                    int numberOfLinesInFile = fileContent.split("\n").length;
                    Position startPosOfLastLine = new Position(numberOfLinesInFile + 3, 0);
                    Position endPosOfLastLine = new Position(numberOfLinesInFile + 3, 1);
                    Range newFunctionRange = new Range(startPosOfLastLine, endPosOfLastLine);
                    String generatedRecordMappingFunction =
                            getGeneratedRecordMappingFunction(bLangNode, symbolAtCursor, foundTypeLeft, foundTypeRight);
                    TextEdit functionEdit = new TextEdit(newFunctionRange, generatedRecordMappingFunction);
                    fEdits.add(functionEdit);
                }
            }
        }
        return fEdits;
    }

    private static String getGeneratedRecordMappingFunction(BLangNode bLangNode, BSymbol symbolAtCursor,
                                                            String foundTypeLeft, String foundTypeRight)
            throws IOException {
        JsonObject rightRecordJSON = new JsonObject();
        JsonObject leftRecordJSON = new JsonObject();

        // Schema 1
        BType variableTypeMappingFrom = symbolAtCursor.type;
        if (variableTypeMappingFrom instanceof BRecordType) {
//            List<BField> rightSchemaFields = (List<BField>) ((BRecordType) variableTypeMappingFrom).fields;
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
//            List<BField> leftSchemaFields = (List<BField>) ((BRecordType) variableTypeMappingTo).fields;
            List<BField> leftSchemaFields =new ArrayList<>(((BRecordType) variableTypeMappingTo).fields.values());
            JsonObject leftSchema = (JsonObject) recordToJSON(leftSchemaFields);

            leftRecordJSON.addProperty("schema", foundTypeLeft);
            leftRecordJSON.addProperty("id", "dummy_id");
            leftRecordJSON.addProperty("type", "object");
            leftRecordJSON.add("properties", leftSchema);
        }

        JsonArray schemas = new JsonArray();
        schemas.add(leftRecordJSON);
        schemas.add(rightRecordJSON);
        if (mappingCache.asMap().containsKey(schemas.hashCode())) {
            return mappingCache.asMap().get(schemas.hashCode());
        } else {
            String schemasToSend = schemas.toString();

            URL url = new URL(LSClientConfigHolder.getInstance().getConfig().getDataMapper().getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(schemasToSend.getBytes(StandardCharsets.UTF_8));
                try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                    Map response = new ObjectMapper().readValue(inputStream, Map.class);
                    String mappedFunction = (String) response.get("answer");
                    mappingCache.put(schemas.hashCode(), mappedFunction);
                    connection.disconnect();
                    return mappedFunction;
                }
            }
        }
    }

    private static JsonElement recordToJSON(List<BField> schemaFields) {

        JsonObject properties = new JsonObject();
        for (BField attribute : schemaFields) {
            JsonObject fieldDetails = new JsonObject();
            fieldDetails.addProperty("id", "dummy_id");
            /* TODO: Do we need to go to lower levels? */
            if (attribute.type instanceof BArrayType) {
                BType attributeEType = ((BArrayType) attribute.type).eType;
                if (attributeEType instanceof BRecordType) {
                    fieldDetails.addProperty("type", "ballerina_type");
//                    fieldDetails.add("properties", recordToJSON((List<BField>)((BRecordType) attributeEType).fields));
                    fieldDetails.add("properties",
                            recordToJSON(new ArrayList<>(((BRecordType) attributeEType).fields.values())));
                } else {
                    fieldDetails.addProperty("type", String.valueOf(attribute.type));
                }
            } else if (attribute.type instanceof BRecordType) {
                fieldDetails.addProperty("type", "ballerina_type");
//                fieldDetails.add("properties", recordToJSON((List<BField>)((BRecordType) attribute.type).fields));
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

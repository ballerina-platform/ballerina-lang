package org.ballerinalang.datamapper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import org.ballerinalang.datamapper.utils.HttpClientRequest;
import org.ballerinalang.datamapper.utils.HttpResponse;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;


import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.datamapper.utils.DefaultValueGenerator.generateDefaultValues;

/**
 * Utils related to the command executor of data mapper code action.
 */
public class AIDataMapperExecutorUtil {

    private static final int HTTP_200_OK = 200;
    private static final int HTTP_422_UN_PROCESSABLE_ENTITY = 422;
    private static final int HTTP_500_INTERNAL_SERVER_ERROR = 500;
    private static final int MAXIMUM_CACHE_SIZE = 100;
    private Cache<Integer, String> mappingCache =
            CacheBuilder.newBuilder().maximumSize(MAXIMUM_CACHE_SIZE).build();
    private HashMap<String, Map<String, RecordFieldSymbol>> spreadFieldMap = new HashMap<>();
    private HashMap<String, String> isOptionalMap = new HashMap<>();
    private HashMap<String, String> leftFieldMap = new HashMap<>();
    private HashMap<String, String> responseFieldMap = new HashMap<>();
    private HashMap<String, String> restFieldMap = new HashMap<>();
    private HashMap<String, String> spreadFieldResponseMap = new HashMap<>();
    private ArrayList<String> leftReadOnlyFields = new ArrayList<>();
    private ArrayList<String> rightSpecificFieldList = new ArrayList<>();
    private ArrayList<String> optionalRightRecordFields = new ArrayList<>();

    public AIDataMapperExecutorUtil() {
    }

    public List<TextEdit> generateMappingEdits(ExecuteCommandContext context, JsonArray parameters, Path filePath,
                                               Range range) throws IOException {
        List<TextEdit> fEdits = new ArrayList<>();

        fEdits.add(new TextEdit(range, parameters.get(3).getAsString()));
        JsonArray schemas = (JsonArray) parameters.get(0);
        String url = parameters.get(1).getAsString();
        SyntaxTree syntaxTree = context.workspace().syntaxTree(filePath).orElseThrow();

        // Get the last line of the file
        TextDocument fileContentTextDocument = syntaxTree.textDocument();
        int numberOfLinesInFile = fileContentTextDocument.toString().split("\n").length;
        Position startPosOfLastLine = new Position(numberOfLinesInFile + 2, 0);
        Position endPosOfLastLine = new Position(numberOfLinesInFile + 2, 1);
        Range newFunctionRange = new Range(startPosOfLastLine, endPosOfLastLine);

        // Get the generated record mapping function
        String mappingFromServer = getMapping(schemas, url);

        //Read property values
        JsonElement backgroudObject = parameters.get(2);
        String generatedRecordMappingFunction = generateMappingFunction(mappingFromServer,
                (JsonObject) backgroudObject);
        fEdits.add(new TextEdit(newFunctionRange, generatedRecordMappingFunction));
        return fEdits;
    }

    /**
     * For a give array of schemas, return a mapping function.
     *
     * @param schemas {@link JsonArray}
     * @return mapped function
     * @throws IOException throws if an error occurred in HTTP request
     */
    private String getMapping(JsonArray schemas, String url) throws IOException {
        int hashCode = schemas.hashCode();
        if (this.mappingCache.asMap().containsKey(hashCode)) {
            return this.mappingCache.asMap().get(hashCode);
        }
        try {
            String mappingFromServer = getMappingFromServer(schemas, url);
            this.mappingCache.put(hashCode, mappingFromServer);
            return mappingFromServer;
        } catch (IOException e) {
            throw new IOException("Error connecting the AI service" + e.getMessage(), e);
        }
    }

    /**
     * Get the mapping from the Data Mapper service.
     *
     * @param dataToSend - payload to the service
     * @return - response data from the Data Mapper service
     * @throws IOException If an error occurs in the Data Mapper service
     */
    private String getMappingFromServer(JsonArray dataToSend,
                                        String url) throws IOException {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json; utf-8");
            headers.put("Accept", "application/json");
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

    private String generateResponseWithDefaultValues(String key, String defaultValue, String mappingFromServer) {
        if (key.contains(".")) {
            StringBuilder insertString = new StringBuilder();
            String[] keyArray = key.split("\\.");
            boolean foundKeyValue = false;
            int insertLocation = 0;
            for (String keyValue : keyArray) {
                if (mappingFromServer.contains(keyValue)) {
                    foundKeyValue = true;
                    insertLocation = mappingFromServer.indexOf(keyValue) + keyValue.length() + 3;
                } else {
                    if (keyValue == keyArray[keyArray.length - 1]) {
                        if (foundKeyValue) {
                            if (defaultValue.isEmpty()) {
                                insertString.append(keyValue).append(": ").append("\"\"");
                            } else {
                                insertString.append(keyValue).append(": ").append(defaultValue);
                            }
                            mappingFromServer = mappingFromServer.substring(0, insertLocation) + insertString +
                                    ", " + mappingFromServer.substring(insertLocation);
                        } else {
                            int lastIndex = mappingFromServer.lastIndexOf("}");
                            if (defaultValue.isEmpty()) {
                                insertString.append(keyValue).append(": ").append("\"\"");
                            } else {
                                insertString.append(keyValue).append(": ").append(defaultValue);
                            }
                            mappingFromServer = mappingFromServer.substring(0, lastIndex) + ", " +
                                    insertString + "}";
                        }
                    } else {
                        insertString.append(keyValue).append(": {");
                    }
                }
            }
        } else {
            int lastIndex = mappingFromServer.lastIndexOf("}");
            if (defaultValue.isEmpty()) {
                mappingFromServer = mappingFromServer.substring(0, lastIndex) + ", " + key + ": " + "\"\"" + "}";
            } else {
                mappingFromServer = mappingFromServer.substring(0, lastIndex) + ", " + key + ": " + defaultValue + "}";
            }
        }
        return mappingFromServer;
    }

    /**
     * To generate the spread field detail map.
     *
     * @param key          {@link String}
     * @param schemaFields {@link List<RecordFieldSymbol>}
     */
    private void getSpreadFieldDetails(String key, Collection<RecordFieldSymbol> schemaFields) {
        Iterator iterator = schemaFields.iterator();
        while (iterator.hasNext()) {
            RecordFieldSymbol attribute = (RecordFieldSymbol) iterator.next();
            TypeSymbol attributeType = CommonUtil.getRawType(attribute.typeDescriptor());

            if (!key.isEmpty()) {
                if (!(key.charAt(key.length() - 1) == '.')) {
                    key = key + ".";
                }
            }

            if (attributeType.typeKind() == TypeDescKind.RECORD) {
                Map<String, RecordFieldSymbol> recordFields = ((RecordTypeSymbol) attributeType).fieldDescriptors();
                key = key + attribute.getName().get();
                getSpreadFieldDetails(key, recordFields.values());
            } else if (attributeType.typeKind() == TypeDescKind.INTERSECTION) {
                // To get the fields of a readonly record type
                List<TypeSymbol> memberTypeList = ((IntersectionTypeSymbol) attribute.typeDescriptor()).
                        memberTypeDescriptors();
                for (TypeSymbol attributeTypeReference : memberTypeList) {
                    if (attributeTypeReference.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                        TypeSymbol attributeTypeRecord = ((TypeReferenceTypeSymbol) attributeTypeReference).
                                typeDescriptor();
                        if (attributeTypeRecord.typeKind() == TypeDescKind.RECORD) {
                            Map<String, RecordFieldSymbol> recordFields = ((RecordTypeSymbol) attributeTypeRecord).
                                    fieldDescriptors();
                            key = key + attribute.getName().get();
                            getSpreadFieldDetails(key, recordFields.values());
                        }
                    }
                }
            } else {
                this.spreadFieldResponseMap.put(key + attribute.getName().get(), attributeType.typeKind().getName());
                if (!iterator.hasNext()) {
                    key = "";
                }
            }
        }
    }

    /**
     * Create the mapping function.
     *
     * @param mappingFromServer {@link String}
     *                          //     * @param foundTypeLeft     {@link String}
     *                          //     * @param foundTypeRight    {@link String}
     *                          //     * @param leftModule        {@link String}
     *                          //     * @param rightModule       {@link String}
     *                          //     * @param rhsSignature      {@link String}
     * @return - Generated mapping Function
     */
    private String generateMappingFunction(String mappingFromServer, JsonObject backgroundElements) {


        String foundTypeLeft = backgroundElements.has("foundTypeLeft") ? backgroundElements.get
                ("foundTypeLeft").getAsString() : null;
        String foundTypeRight = backgroundElements.has("foundTypeRight") ? backgroundElements.get
                ("foundTypeRight").getAsString() : null;
        String leftModule = backgroundElements.has("leftModule") ? backgroundElements.get
                ("leftModule").getAsString() : null;
        String rightModule = backgroundElements.has("rightModule") ? backgroundElements.get
                ("rightModule").getAsString() : null;
        String rhsSignature = backgroundElements.has("rhsSignature") ? backgroundElements.get
                ("rhsSignature").getAsString() : null;

        String leftType = foundTypeLeft;
        String rightType;
        // To add the rhs signature to parameter of the mapping function
        if (rhsSignature.isEmpty()) {
            rightType = foundTypeRight;
        } else {
            rightType = rhsSignature;
        }

        mappingFromServer = mappingFromServer.replaceAll("\"", "");
        mappingFromServer = mappingFromServer.replaceAll(",", ", ");
        mappingFromServer = mappingFromServer.replaceAll(":", ": ");


        // change the generated mapping function to be compatible with spread fields.
        if (!this.spreadFieldMap.isEmpty()) {
            for (Map.Entry<String, Map<String, RecordFieldSymbol>> field : this.spreadFieldMap.entrySet()) {
                if (mappingFromServer.contains("." + field.getKey() + ".")) {
                    getSpreadFieldDetails("", field.getValue().values());

                    String commonString = foundTypeRight.toLowerCase() + "." + field.getKey() + ".";

                    for (Map.Entry<String, String> spreadField : this.spreadFieldResponseMap.entrySet()) {
                        String targetString = commonString + spreadField.getKey();
                        String replaceString = "<" + spreadField.getValue() + "> " + foundTypeRight.toLowerCase() +
                                "[\"" + spreadField.getKey() + "\"]";
                        mappingFromServer = mappingFromServer.replace(targetString, replaceString);
                    }
                }
                this.spreadFieldResponseMap.clear();
            }
            this.spreadFieldMap.clear();
        }

        // change the generated mapping function to be compatible with optional fields.
        if (!this.isOptionalMap.isEmpty()) {
            for (Map.Entry<String, String> field : this.isOptionalMap.entrySet()) {
                if (mappingFromServer.contains(field.getKey() + ",") ||
                        mappingFromServer.contains(field.getKey() + "}")) {

                    String replacement = "";

                    int recordFieldIndex = checkForOptionalRecordField(field.getKey());
                    // check if there is a record field
                    if (recordFieldIndex > 0) {
                        String firstPrat = field.getKey().substring(0, recordFieldIndex - 1);
                        String[] splitKey = (field.getKey().substring(recordFieldIndex - 1)).split("\\.");
                        replacement = firstPrat;
                        for (String key : splitKey) {
                            if (!key.isEmpty()) {
                                replacement = replacement + "?." + key;
                            }
                        }
                    } else {
                        // optional specific fields modification
                        int i = field.getKey().lastIndexOf(".");
                        String[] splitKey = {field.getKey().substring(0, i), field.getKey().substring(i)};
                        replacement = splitKey[0] + "?" + splitKey[1];
                    }
                    mappingFromServer = mappingFromServer.replace(field.getKey(), "<" +
                            field.getValue() + "> " + replacement);
                }
            }
        }

        // change the generated mapping function to be compatible with rest fields.
        if (!this.restFieldMap.isEmpty()) {
            for (Map.Entry<String, String> field : this.restFieldMap.entrySet()) {
                if (mappingFromServer.contains(field.getKey() + ",") ||
                        mappingFromServer.contains(field.getKey() + "}")) {
                    int i = field.getKey().lastIndexOf(".");
                    String[] splitKey = {field.getKey().substring(0, i), field.getKey().substring(i)};
                    String replacement = splitKey[0] + "[\"" + splitKey[1].replace(".", "") + "\"]";
                    mappingFromServer = mappingFromServer.replace(field.getKey(), "<" +
                            field.getValue() + "> " + replacement);
                }
            }
            this.restFieldMap.clear();
        }

        if (!this.leftReadOnlyFields.isEmpty()) {
            for (String readOnlyField : this.leftReadOnlyFields) {
                if (mappingFromServer.contains(readOnlyField + ":")) {
                    String[] splitArray = mappingFromServer.split(readOnlyField + ":");
                    int inputIndex = splitArray[1].indexOf(",");
                    mappingFromServer = splitArray[0] + readOnlyField + ":" + splitArray[1].substring(0, inputIndex)
                            + ".cloneReadOnly()" + splitArray[1].substring(inputIndex);
                    int i = 0;
                }
            }
            this.leftReadOnlyFields.clear();
        }

        //To generate the default values
        try {
            Map<String, Object> responseMap = new Gson().fromJson(
                    new JsonParser().parse(mappingFromServer).getAsJsonObject(),
                    new TypeToken<HashMap<String, Object>>() {
                    }.getType());
            getResponseKeys(responseMap, "");
            HashSet<String> unionKeys = new HashSet<>(this.responseFieldMap.keySet());
            unionKeys.addAll(this.leftFieldMap.keySet());
            unionKeys.removeAll(this.responseFieldMap.keySet());
            if (!unionKeys.isEmpty()) {
                for (String key : unionKeys) {
                    String defaultValue = generateDefaultValues(this.leftFieldMap.get(key)).toString();
                    mappingFromServer = generateResponseWithDefaultValues(key, defaultValue, mappingFromServer);
                }
            }
        } catch (Exception e) {
            // Safe to ignore
        }

        this.leftFieldMap.clear();
        this.responseFieldMap.clear();
        this.optionalRightRecordFields.clear();
        this.rightSpecificFieldList.clear();
        this.isOptionalMap.clear();

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

    private int checkForOptionalRecordField(String optionalKey) {
        for (String recordRecordField : this.optionalRightRecordFields) {
            if (optionalKey.contains(recordRecordField)) {
                return optionalKey.indexOf(recordRecordField);
            }
        }
        return -1;
    }


    private void getResponseKeys(Map<String, Object> leftSchemaMap, String keyName) {
        for (Map.Entry<String, Object> field : leftSchemaMap.entrySet()) {
            StringBuilder fieldKey = new StringBuilder();
            Map treeMap = null;
            if (!keyName.isEmpty()) {
                fieldKey.append(keyName).append(".");
            }
            try {
                treeMap = (Map) field.getValue();
            } catch (Exception e) {
                //ignore
            }
            fieldKey.append(field.getKey());
            if (treeMap != null) {
                getResponseKeys((Map<String, Object>) treeMap, fieldKey.toString());
            } else {
                this.responseFieldMap.put(fieldKey.toString(), field.getValue().toString());
            }
        }
    }
}

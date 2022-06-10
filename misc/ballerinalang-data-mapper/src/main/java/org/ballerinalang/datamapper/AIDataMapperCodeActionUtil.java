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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.DiagnosticPropertyKind;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.datamapper.config.ClientExtendedConfigImpl;
import org.ballerinalang.datamapper.utils.HttpClientRequest;
import org.ballerinalang.datamapper.utils.HttpResponse;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.ballerinalang.datamapper.utils.DefaultValueGenerator.generateDefaultValues;

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
    private Cache<Integer, String> mappingCache =
            CacheBuilder.newBuilder().maximumSize(MAXIMUM_CACHE_SIZE).build();
    private HashMap<String, String> isOptionalMap = new HashMap<>();
    private HashMap<String, String> leftFieldMap = new HashMap<>();
    private HashMap<String, String> responseFieldMap = new HashMap<>();
    private HashMap<String, String> restFieldMap = new HashMap<>();
    private HashMap<String, Map<String, RecordFieldSymbol>> spreadFieldMap = new HashMap<>();
    private HashMap<String, String> spreadFieldResponseMap = new HashMap<>();
    private ArrayList<String> leftReadOnlyFields = new ArrayList<>();
    private ArrayList<String> rightSpecificFieldList = new ArrayList<>();
    private ArrayList<String> optionalRightRecordFields = new ArrayList<>();

    private static final String SCHEMA = "schema";
    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String PROPERTIES = "properties";
    private static final String OPTIONAL = "optional";
    private static final String READONLY = "readonly";
    private static final String SIGNATURE = "signature";

    private static AIDataMapperCodeActionUtil dataMapperCodeAction;

    private AIDataMapperCodeActionUtil() {
    }

    public static AIDataMapperCodeActionUtil getInstance() {
        if (dataMapperCodeAction == null) {
            dataMapperCodeAction = new AIDataMapperCodeActionUtil();
        }
        return dataMapperCodeAction;
    }

    /**
     * Returns the workspace edits for the automatic data mapping code action.
     *
     * @param positionDetails {@link DiagBasedPositionDetails}
     * @param context         {@link CodeActionContext}
     * @param diagnostic      {@link Diagnostic}
     * @return edits for the data mapper code action
     * @throws IOException throws if error occurred when getting generatedRecordMappingFunction
     */
    public List<TextEdit> getAIDataMapperCodeActionEdits(DiagBasedPositionDetails positionDetails,
                                                         CodeActionContext context, Diagnostic diagnostic)
            throws IOException {
        List<TextEdit> fEdits = new ArrayList<>();

        Optional<Document> srcFile = context.workspace().document(context.filePath());
        if (srcFile.isEmpty()) {
            return fEdits;
        }

        List<DiagnosticProperty<?>> props = diagnostic.properties();

        //To check if the left is a symbol
        if (props.get(LEFT_SYMBOL_INDEX).kind() != DiagnosticPropertyKind.SYMBOLIC) {
            return fEdits;
        }

        TypeDescKind leftSymbolType = ((TypeSymbol) props.get(LEFT_SYMBOL_INDEX).value()).typeKind();

        if (props.size() != 2 || props.get(RIGHT_SYMBOL_INDEX).kind() != DiagnosticPropertyKind.SYMBOLIC ||
                props.get(LEFT_SYMBOL_INDEX).kind() != DiagnosticPropertyKind.SYMBOLIC ||
                ((leftSymbolType != TypeDescKind.TYPE_REFERENCE) && (leftSymbolType != TypeDescKind.RECORD) &&
                        (leftSymbolType != TypeDescKind.UNION))) {
            return fEdits;
        } else {

            Symbol lftTypeSymbol;
            Symbol rhsTypeSymbol;

            String foundTypeLeft;
            String foundTypeRight;

            // Get the semantic model
            SemanticModel semanticModel = context.workspace().semanticModel(context.filePath()).orElseThrow();
            List<Symbol> fileContentSymbols = semanticModel.moduleSymbols();

            // Find the location to insert function call in the code where error is found
            Range newTextRange = PositionUtil.toRange(diagnostic.location().lineRange());
            LinePosition linePosition = diagnostic.location().lineRange().startLine();

            SyntaxTree syntaxTree = context.workspace().syntaxTree(context.filePath()).get();

            // get the symbol at cursor type
            Symbol symbolAtCursor;
            Optional<Symbol> symbolFromModel = semanticModel.symbol(srcFile.get(), linePosition);
            if (symbolFromModel.isPresent()) {
                symbolAtCursor = symbolFromModel.get();
                lftTypeSymbol = (Symbol) props.get(LEFT_SYMBOL_INDEX).value();
                rhsTypeSymbol = (Symbol) props.get(RIGHT_SYMBOL_INDEX).value();

                foundTypeLeft = lftTypeSymbol.getName().orElse("");
                foundTypeRight = rhsTypeSymbol.getName().orElse("");
            } else {
                // to get the symbol when type casting is done
                Optional<Node> nodeAtCursor = findExpressionInTypeCastNode(newTextRange, syntaxTree);
                if (nodeAtCursor.isEmpty()) {
                    return fEdits;
                }
                symbolFromModel = semanticModel.symbol(nodeAtCursor.get());

                if (symbolFromModel.isEmpty()) {
                    return fEdits;
                }

                symbolAtCursor = symbolFromModel.get();
                lftTypeSymbol = (Symbol) props.get(RIGHT_SYMBOL_INDEX).value();
                rhsTypeSymbol = (Symbol) props.get(LEFT_SYMBOL_INDEX).value();

                foundTypeLeft = lftTypeSymbol.getName().orElse("");
                foundTypeRight = rhsTypeSymbol.getName().orElse("");

            }
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
                    //Check if the rhs is defined with var
                    if ("".equals(foundTypeRight)) {
                        generatedFunctionName =
                                String.format("map%sTo%s(%s)", symbolAtCursorName, foundTypeLeft, symbolAtCursorName);
                        foundTypeRight = symbolAtCursorName;
                    } else {
                        generatedFunctionName =
                                String.format("map%sTo%s(%s)", foundTypeRight, foundTypeLeft, symbolAtCursorName);
                    }
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

                    NonTerminalNode matchedNode = null;
                    if (positionDetails.matchedNode().kind() == SyntaxKind.FUNCTION_CALL) {
                        matchedNode = positionDetails.matchedNode();
                        if (matchedNode.parent().kind() == SyntaxKind.CHECK_EXPRESSION) {
                            matchedNode = matchedNode.parent();
                        }
                    } else if ((positionDetails.matchedNode().kind() == SyntaxKind.TYPE_CAST_EXPRESSION) &&
                            (((TypeCastExpressionNode) positionDetails.matchedNode()).expression().kind() ==
                                    SyntaxKind.FUNCTION_CALL)) {
                        matchedNode = ((TypeCastExpressionNode) positionDetails.matchedNode()).expression();
                    }

                    if (matchedNode == null) {
                        return fEdits;
                    }

                    String functionCall = matchedNode.toString();
                    if (foundErrorRight && !foundErrorLeft) {
                        symbolAtCursorName = functionCall.trim();
                        generatedFunctionName =
                                String.format("map%sTo%s(check %s)", foundTypeRight, foundTypeLeft, symbolAtCursorName);
                        fEdits.add(new TextEdit(newTextRange, generatedFunctionName));
                    } else if (foundErrorLeft && foundErrorRight) {
                        // get the information about the line positions
                        newTextRange = PositionUtil.toRange(matchedNode.lineRange());
                        generatedFunctionName =
                                String.format("map%sTo%s(%s)", foundTypeRight, foundTypeLeft, functionCall);
                        fEdits.add(new TextEdit(newTextRange, generatedFunctionName));
                    } else if (foundErrorLeft) {
                        Collection<ChildNodeEntry> childEntries = matchedNode.childEntries();
                        for (ChildNodeEntry childEntry : childEntries) {
                            if (childEntry.name().equals("expression")) {
                                functionCall = childEntry.node().isPresent() ?
                                        childEntry.node().get().toString() : null;
                            }
                        }
                        if (functionCall == null) {
                            return fEdits;
                        }
                        symbolAtCursorName = functionCall.trim();
                        generatedFunctionName = String.format("map%sTo%s(%s)",
                                foundTypeRight, foundTypeLeft, symbolAtCursorName);
                        fEdits.add(new TextEdit(newTextRange, generatedFunctionName));
                    } else {
                        symbolAtCursorName = functionCall.trim();
                        generatedFunctionName =
                                String.format("map%sTo%s(%s)", foundTypeRight, foundTypeLeft, symbolAtCursorName);
                        fEdits.add(new TextEdit(newTextRange, generatedFunctionName));
                    }
                    break;
                default:
                    return fEdits;
            }

            // Insert function declaration at the bottom of the file
            String functionName = String.format("map%sTo%s", foundTypeRight, foundTypeLeft);
            boolean found = fileContentSymbols.stream().anyMatch(p -> p.getName().get().contains(functionName));
            if (found) {
                return fEdits;
            } else {
                // Get the last line of the file
                syntaxTree = context.workspace().syntaxTree(context.filePath()).get();
                TextDocument fileContentTextDocument = syntaxTree.textDocument();
                int numberOfLinesInFile = fileContentTextDocument.toString().split("\n").length;
                Position startPosOfLastLine = new Position(numberOfLinesInFile + 2, 0);
                Position endPosOfLastLine = new Position(numberOfLinesInFile + 2, 1);
                Range newFunctionRange = new Range(startPosOfLastLine, endPosOfLastLine);

                // Get the generated record mapping function
                String mappingFromServer =
                        getGeneratedRecordMapping(context, foundTypeLeft, foundTypeRight, lftTypeSymbol,
                                rhsTypeSymbol, syntaxTree, semanticModel);

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

                //To get the signature of the rhsSymbol to the generated Function - Var solution
                String rhsSignature = "";
                if (symbolAtCursorName.equals(foundTypeRight)) {
                    rhsSignature = SymbolUtil.getTypeDescriptor(symbolAtCursor).get().signature();
                }

                String generatedRecordMappingFunction = generateMappingFunction(mappingFromServer, foundTypeLeft,
                        foundTypeRight, leftModule, rightModule, rhsSignature, syntaxTree);
                fEdits.add(new TextEdit(newFunctionRange, generatedRecordMappingFunction));
                return fEdits;
            }
        }
    }

    private Optional<Node> findExpressionInTypeCastNode(Range range, SyntaxTree syntaxTree) {
        TextDocument textDocument = syntaxTree.textDocument();
        Position rangeStart = range.getStart();
        Position rangeEnd = range.getEnd();
        int start = textDocument.textPositionFrom(LinePosition.from(rangeStart.getLine(), rangeStart.getCharacter()));
        int end = textDocument.textPositionFrom(LinePosition.from(rangeEnd.getLine(), rangeEnd.getCharacter()));
        NonTerminalNode nonTerminalNode = ((ModulePartNode) syntaxTree.rootNode()).
                findNode(TextRange.from(start, end - start), true);
        if (nonTerminalNode.kind() == SyntaxKind.TYPE_CAST_EXPRESSION) {
            return Optional.of(((TypeCastExpressionNode) nonTerminalNode).expression());
        }
        return Optional.empty();
    }

    /**
     * Check if a symbol is in the module level, and if so return the symbol.
     *
     * @param fileContentSymbols {@link List}
     * @return return the symbol
     */
    public Symbol findSymbol(List<TypeSymbol> fileContentSymbols) {
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
     * @param syntaxTree
     * @param semanticModel
     * @return function string with mapped schemas
     * @throws IOException throws if error occurred when getting mapped function
     */
    private String getGeneratedRecordMapping(CodeActionContext context, String foundTypeLeft,
                                             String foundTypeRight,
                                             Symbol lftTypeSymbol, Symbol rhsTypeSymbol,
                                             SyntaxTree syntaxTree, SemanticModel semanticModel)

            throws IOException {
        JsonObject rightRecordJSON = new JsonObject();
        JsonObject leftRecordJSON = new JsonObject();

        List<RecordTypeSymbol> symbolList = checkMappingCapability(lftTypeSymbol, rhsTypeSymbol);
        if (symbolList.size() == 2) {
            // Schema 1

            // To get the rest field details
            DataMapperNodeVisitor nodeVisitor = new DataMapperNodeVisitor(foundTypeRight);
            nodeVisitor.setModel(semanticModel);
            syntaxTree.rootNode().accept(nodeVisitor);
            this.restFieldMap = nodeVisitor.restFields;
            this.spreadFieldMap = nodeVisitor.spreadFields;
            this.rightSpecificFieldList = nodeVisitor.specificFieldList;


            Map<String, RecordFieldSymbol> rightSchemaFields = symbolList.get(0).fieldDescriptors();
            JsonObject rightSchema = (JsonObject) rightRecordToJSON(rightSchemaFields.values());

            if (!this.restFieldMap.isEmpty()) {
                rightSchema = insertRestFields(rightSchema, foundTypeRight);
            }

            if (!this.spreadFieldMap.isEmpty()) {
                for (Map.Entry<String, Map<String, RecordFieldSymbol>> field : this.spreadFieldMap.entrySet()) {
                    JsonObject spreadFieldDetails = new JsonObject();
                    spreadFieldDetails.addProperty(ID, "id");
                    spreadFieldDetails.addProperty(TYPE, "ballerina_type");
                    spreadFieldDetails.add(PROPERTIES, rightRecordToJSON(field.getValue().values()));
                    rightSchema.add(field.getKey(), spreadFieldDetails);
                }
            }

            rightRecordJSON.addProperty(SCHEMA, foundTypeRight);
            rightRecordJSON.addProperty(ID, "id");
            rightRecordJSON.addProperty(TYPE, "object");
            rightRecordJSON.add(PROPERTIES, rightSchema);

            Map<String, Object> rightSchemaMap = new Gson().fromJson(rightSchema,
                    new TypeToken<HashMap<String, Object>>() {
                    }.getType());
            this.isOptionalMap.clear();
            generateOptionalMap(rightSchemaMap, foundTypeRight);


            // Schema 2
            Map<String, RecordFieldSymbol> leftSchemaFields = symbolList.get(1).fieldDescriptors();
            JsonObject leftSchema = (JsonObject) leftRecordToJSON(leftSchemaFields.values());
            leftRecordJSON.addProperty(SCHEMA, foundTypeLeft);
            leftRecordJSON.addProperty(ID, "id");
            leftRecordJSON.addProperty(TYPE, "object");
            leftRecordJSON.add(PROPERTIES, leftSchema);

            Map<String, Object> leftSchemaMap = new Gson().fromJson(leftSchema,
                    new TypeToken<HashMap<String, Object>>() {
                    }.getType());
            this.leftFieldMap.clear();
            getLeftFields(leftSchemaMap, "");
        }
        JsonArray schemas = new JsonArray();
        schemas.add(leftRecordJSON);
        schemas.add(rightRecordJSON);
        return getMapping(schemas, context);
    }

    private JsonObject insertRestFields(JsonObject rightSchema, String foundTypeRight) {
        // Added to get the rest fields
        HashMap<String, String> tempRestFieldMap = new HashMap<>(this.restFieldMap);
        Set<Map.Entry<String, String>> entrySet = tempRestFieldMap.entrySet();
        for (Map.Entry<String, String> restField : entrySet) {
            JsonObject fieldDetails = new JsonObject();
            fieldDetails.addProperty(ID, "id");
            fieldDetails.addProperty(TYPE, restField.getValue());
            fieldDetails.addProperty(OPTIONAL, false);
            rightSchema.add(restField.getKey(), fieldDetails);
            this.restFieldMap.put(foundTypeRight.toLowerCase() + "." + restField.getKey(),
                    this.restFieldMap.remove(restField.getKey()));
        }
        return rightSchema;
    }


    private void generateOptionalMap(Map<String, Object> rightSchemaMap, String foundTypeRight) {
        for (Map.Entry<String, Object> field : rightSchemaMap.entrySet()) {
            StringBuilder optionalKey = new StringBuilder(foundTypeRight.toLowerCase());
            if (!(((Map) field.getValue()).containsKey(OPTIONAL))) {
                optionalKey.append(".").append(field.getKey());
                generateOptionalMap((Map<String, Object>) ((Map) field.getValue()).get(PROPERTIES),
                        optionalKey.toString());
            } else if ((boolean) ((Map) field.getValue()).get(OPTIONAL) |
                    (checkForOptionalRecordField(optionalKey.toString()) > 0)) {
                optionalKey.append(".").append(field.getKey());
                this.isOptionalMap.put(optionalKey.toString(), ((Map) field.getValue()).get(SIGNATURE).
                        toString());
            }
        }
    }


    private int checkForOptionalRecordField(String optionalKey) {
        for (String recordRecordField : this.optionalRightRecordFields) {
            if (optionalKey.contains(recordRecordField)) {
                return optionalKey.indexOf(recordRecordField);
            }
        }
        return -1;
    }

    private void getLeftFields(Map<String, Object> leftSchemaMap, String fieldName) {
        for (Map.Entry<String, Object> field : leftSchemaMap.entrySet()) {
            StringBuilder fieldKey = new StringBuilder();
            if (!fieldName.isEmpty()) {
                fieldKey.append(fieldName).append(".");
            }
            if (!(((Map) field.getValue()).containsKey(READONLY))) {
                fieldKey.append(field.getKey());
                getLeftFields((Map<String, Object>) ((Map) field.getValue()).get(PROPERTIES),
                        fieldKey.toString());
            } else {
                fieldKey.append(field.getKey());
                this.leftFieldMap.put(fieldKey.toString(), ((Map) field.getValue()).get(TYPE).toString());
                if ((((Map) field.getValue()).get(READONLY)).toString().contains("true")) {
                    this.leftReadOnlyFields.add(field.getKey());
                }
            }
        }
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

    /**
     * For a give array of schemas, return a mapping function.
     *
     * @param schemas {@link JsonArray}
     * @return mapped function
     * @throws IOException throws if an error occurred in HTTP request
     */
    private String getMapping(JsonArray schemas, CodeActionContext context) throws IOException {
        int hashCode = schemas.hashCode();
        if (this.mappingCache.asMap().containsKey(hashCode)) {
            return this.mappingCache.asMap().get(hashCode);
        }
        try {
            String mappingFromServer = getMappingFromServer(schemas, context.languageServercontext());
            this.mappingCache.put(hashCode, mappingFromServer);
            return mappingFromServer;
        } catch (IOException e) {
            throw new IOException("Error connecting the AI service" + e.getMessage(), e);
        }
    }

    /**
     * Convert right record type symbols to json objects.
     *
     * @param schemaFields {@link List<RecordFieldSymbol>}
     * @return Field symbol properties
     */
    private JsonElement rightRecordToJSON(Collection<RecordFieldSymbol> schemaFields) {
        JsonObject properties = new JsonObject();
        for (RecordFieldSymbol attribute : schemaFields) {
            JsonObject fieldDetails = new JsonObject();
            // check if an optional field doesn't have a value
            if (attribute.isOptional() && !this.rightSpecificFieldList.contains(attribute.getName().get())) {
                continue;
            }
            fieldDetails.addProperty(ID, "id");
            TypeSymbol attributeType = CommonUtil.getRawType(attribute.typeDescriptor());
            if (attributeType.typeKind() == TypeDescKind.RECORD) {
                if (attribute.isOptional()) {
                    this.optionalRightRecordFields.add(attribute.getName().get());
                }
                Map<String, RecordFieldSymbol> recordFields = ((RecordTypeSymbol) attributeType).fieldDescriptors();
                fieldDetails.addProperty(TYPE, "ballerina_type");
                fieldDetails.add(PROPERTIES, rightRecordToJSON(recordFields.values()));
            } else if (attributeType.typeKind() == TypeDescKind.INTERSECTION) {
                // To get the fields of a readonly record type
                List<TypeSymbol> memberTypeList = ((IntersectionTypeSymbol) attribute.typeDescriptor()).
                        memberTypeDescriptors();
                for (TypeSymbol attributeTypeReference : memberTypeList) {
                    if (attributeTypeReference.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                        TypeSymbol attributeTypeRecord = ((TypeReferenceTypeSymbol) attributeTypeReference).
                                typeDescriptor();
                        if (attributeTypeRecord.typeKind() == TypeDescKind.RECORD) {
                            if (attribute.isOptional()) {
                                this.optionalRightRecordFields.add(attribute.getName().get());
                            }
                            Map<String, RecordFieldSymbol> recordFields = ((RecordTypeSymbol) attributeTypeRecord).
                                    fieldDescriptors();
                            fieldDetails.addProperty(TYPE, "ballerina_type");
                            fieldDetails.add(PROPERTIES, rightRecordToJSON(recordFields.values()));
                        }
                    }
                }
            } else {
                fieldDetails.addProperty(TYPE, attributeType.typeKind().getName());
                fieldDetails.addProperty(OPTIONAL, attribute.isOptional());
                fieldDetails.addProperty(SIGNATURE, attributeType.signature());
                // to check for readonly values of left schema
            }
            properties.add(attribute.getName().get(), fieldDetails);
        }
        return properties;
    }

    /**
     * Convert left record type symbols to json objects.
     *
     * @param schemaFields {@link List<RecordFieldSymbol>}
     * @return Field symbol properties
     */
    private JsonElement leftRecordToJSON(Collection<RecordFieldSymbol> schemaFields) {
        JsonObject properties = new JsonObject();
        for (RecordFieldSymbol attribute : schemaFields) {
            JsonObject fieldDetails = new JsonObject();
            fieldDetails.addProperty(ID, "id");
            TypeSymbol attributeType = CommonUtil.getRawType(attribute.typeDescriptor());
            if (attributeType.typeKind() == TypeDescKind.RECORD) {
                Map<String, RecordFieldSymbol> recordFields = ((RecordTypeSymbol) attributeType).fieldDescriptors();
                fieldDetails.addProperty(TYPE, "ballerina_type");
                fieldDetails.add(PROPERTIES, leftRecordToJSON(recordFields.values()));
            } else if (attributeType.typeKind() == TypeDescKind.INTERSECTION) {
                // To get the fields of a readonly record field type
                List<TypeSymbol> memberTypeList = ((IntersectionTypeSymbol) attribute.typeDescriptor()).
                        memberTypeDescriptors();
                for (TypeSymbol attributeTypeReference : memberTypeList) {
                    if (attributeTypeReference.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                        TypeSymbol attributeTypeRecord = ((TypeReferenceTypeSymbol) attributeTypeReference).
                                typeDescriptor();
                        if (attributeTypeRecord.typeKind() == TypeDescKind.RECORD) {
                            Map<String, RecordFieldSymbol> recordFields = ((RecordTypeSymbol) attributeTypeRecord).
                                    fieldDescriptors();
                            fieldDetails.addProperty(TYPE, "ballerina_type");
                            fieldDetails.add(PROPERTIES, leftRecordToJSON(recordFields.values()));
                        }
                    }
                }
            } else {
                fieldDetails.addProperty(TYPE, attributeType.typeKind().getName());
                // to check for readonly values of left schema
                boolean readonlyCheck = false;
                if (attribute.qualifiers().size() > 0) {
                    for (Qualifier qualifier : attribute.qualifiers()) {
                        readonlyCheck = qualifier.getValue().contains("readonly");
                        if (readonlyCheck) {
                            TypeDescKind attributeTypeKind = attributeType.typeKind();
                            if ((attributeTypeKind == TypeDescKind.XML) | (attributeTypeKind == TypeDescKind.ARRAY) |
                                    (attributeTypeKind == TypeDescKind.MAP)
                                    | (attributeTypeKind == TypeDescKind.TABLE) |
                                    (attributeTypeKind == TypeDescKind.OBJECT) |
                                    (attributeTypeKind == TypeDescKind.TUPLE)) {
                                fieldDetails.addProperty(READONLY, true);
                                break;
                            } else {
                                readonlyCheck = false;
                            }
                        }
                    }
                }
                if (!readonlyCheck) {
                    fieldDetails.addProperty(READONLY, false);
                }
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
    private String getMappingFromServer(JsonArray dataToSend,
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

    /**
     * Create the mapping function.
     *
     * @param mappingFromServer {@link String}
     * @param foundTypeLeft     {@link String}
     * @param foundTypeRight    {@link String}
     * @param leftModule        {@link String}
     * @param rightModule       {@link String}
     * @param rhsSignature      {@link String}
     * @param syntaxTree
     * @return - Generated mapping Function
     */
    private String generateMappingFunction(String mappingFromServer, String foundTypeLeft,
                                           String foundTypeRight, String leftModule, String rightModule,
                                           String rhsSignature, SyntaxTree syntaxTree) {

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

    /**
     * Properly type caste the symbols.
     *
     * @param lftTypeSymbol {@link Symbol}
     * @param rhsTypeSymbol {@link String}
     * @return - Type casted symbol list
     */
    private List<RecordTypeSymbol> checkMappingCapability(Symbol lftTypeSymbol, Symbol rhsTypeSymbol) {

        ArrayList<RecordTypeSymbol> symbolArrayList = new ArrayList<>();
        // rhs Schema
        RecordTypeSymbol rightSymbol;
        if (rhsTypeSymbol.getName().isEmpty()) {
            rightSymbol = (RecordTypeSymbol) rhsTypeSymbol;
            symbolArrayList.add(rightSymbol);
        } else {
            TypeSymbol rhsSymbol = ((TypeReferenceTypeSymbol) rhsTypeSymbol).typeDescriptor();
            if ("RECORD".equals(rhsSymbol.typeKind().name())) {
                rightSymbol = (RecordTypeSymbol) rhsSymbol;
                symbolArrayList.add(rightSymbol);
            }
        }

        // lfs Schema
        TypeSymbol lftSymbol = ((TypeReferenceTypeSymbol) lftTypeSymbol).typeDescriptor();
        RecordTypeSymbol leftSymbol;
        if ("RECORD".equals(lftSymbol.typeKind().name())) {
            leftSymbol = (RecordTypeSymbol) lftSymbol;
            symbolArrayList.add(leftSymbol);
        }

        return symbolArrayList;
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
}

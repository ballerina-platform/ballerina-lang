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
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Automatic data mapping code action related utils.
 */
class AIDataMapperCodeActionUtil {
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
    public ArrayList getAIDataMapperCodeActionEdits(DiagBasedPositionDetails positionDetails,
                                                  CodeActionContext context, Diagnostic diagnostic)
            throws IOException {
        ArrayList outputArray = new ArrayList();
        JsonArray outputJson = new JsonArray();

        Optional<Document> srcFile = context.workspace().document(context.filePath());
        if (srcFile.isEmpty()) {
            return outputArray;
        }

        List<DiagnosticProperty<?>> props = diagnostic.properties();

        //To check if the left is a symbol
        if (props.get(LEFT_SYMBOL_INDEX).kind() != DiagnosticPropertyKind.SYMBOLIC) {
            return outputArray;
        }

        TypeDescKind leftSymbolType = ((TypeSymbol) props.get(LEFT_SYMBOL_INDEX).value()).typeKind();

        if (props.size() != 2 || props.get(RIGHT_SYMBOL_INDEX).kind() != DiagnosticPropertyKind.SYMBOLIC ||
                props.get(LEFT_SYMBOL_INDEX).kind() != DiagnosticPropertyKind.SYMBOLIC ||
                ((leftSymbolType != TypeDescKind.TYPE_REFERENCE) && (leftSymbolType != TypeDescKind.RECORD) &&
                        (leftSymbolType != TypeDescKind.UNION))) {
            return outputArray;
        } else {

            Symbol lftTypeSymbol;
            Symbol rhsTypeSymbol;

            String foundTypeLeft;
            String foundTypeRight;

            // Get the semantic model
            SemanticModel semanticModel = context.workspace().semanticModel(context.filePath()).orElseThrow();
            List<Symbol> fileContentSymbols = semanticModel.moduleSymbols();

            // Find the location to insert function call in the code where error is found
            Range newTextRange = CommonUtil.toRange(diagnostic.location().lineRange());
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
                    return outputArray;
                }
                symbolFromModel = semanticModel.symbol(nodeAtCursor.get());

                if (symbolFromModel.isEmpty()) {
                    return outputArray;
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
                        return outputArray;
                    }

                    String functionCall = matchedNode.toString();
                    if (foundErrorRight && !foundErrorLeft) {
                        symbolAtCursorName = functionCall.trim();
                        generatedFunctionName =
                                String.format("map%sTo%s(check %s)", foundTypeRight, foundTypeLeft, symbolAtCursorName);
                    } else if (foundErrorLeft && foundErrorRight) {
                        // get the information about the line positions
                        newTextRange = CommonUtil.toRange(matchedNode.lineRange());
                        generatedFunctionName =
                                String.format("map%sTo%s(%s)", foundTypeRight, foundTypeLeft, functionCall);
                    } else if (foundErrorLeft) {
                        Collection<ChildNodeEntry> childEntries = matchedNode.childEntries();
                        for (ChildNodeEntry childEntry : childEntries) {
                            if (childEntry.name().equals("expression")) {
                                functionCall = childEntry.node().isPresent() ?
                                        childEntry.node().get().toString() : null;
                            }
                        }
                        if (functionCall == null) {
                            return outputArray;
                        }
                        symbolAtCursorName = functionCall.trim();
                        generatedFunctionName = String.format("map%sTo%s(%s)",
                                foundTypeRight, foundTypeLeft, symbolAtCursorName);
                    } else {
                        symbolAtCursorName = functionCall.trim();
                        generatedFunctionName =
                                String.format("map%sTo%s(%s)", foundTypeRight, foundTypeLeft, symbolAtCursorName);
                    }
                    break;
                default:
                    return outputArray;
            }

            // Insert function declaration at the bottom of the file
            String functionName = String.format("map%sTo%s", foundTypeRight, foundTypeLeft);
            boolean found = fileContentSymbols.stream().anyMatch(p -> p.getName().get().contains(functionName));
            if (!found) {
                syntaxTree = context.workspace().syntaxTree(context.filePath()).get();

                JsonArray schemas =
                        getGeneratedRecordMapping(foundTypeLeft, foundTypeRight, lftTypeSymbol,
                                rhsTypeSymbol, syntaxTree, semanticModel);
                String url = LSClientConfigHolder.getInstance(context.languageServercontext())
                        .getConfigAs(ClientExtendedConfigImpl.class).getDataMapper()
                        .getUrl() + "/map/2.0.0";
                outputArray.add(0, schemas);
                outputArray.add(1, url);

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

                JsonObject backgroundInfo = new JsonObject();
                backgroundInfo.addProperty("foundTypeLeft", foundTypeLeft);
                backgroundInfo.addProperty("foundTypeRight", foundTypeRight);
                backgroundInfo.addProperty("leftModule", leftModule);
                backgroundInfo.addProperty("rightModule", rightModule);
                backgroundInfo.addProperty("rhsSignature", rhsSignature);

                outputArray.add(2, backgroundInfo);
                outputArray.add(3, generatedFunctionName);
            }
            return outputArray;
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
     * @param foundTypeLeft  {@link String}
     * @param foundTypeRight {@link String}
     * @param syntaxTree
     * @param semanticModel
     * @return function string with mapped schemas
     * @throws IOException throws if error occurred when getting mapped function
     */
    private JsonArray getGeneratedRecordMapping(String foundTypeLeft,
                                             String foundTypeRight,
                                             Symbol lftTypeSymbol, Symbol rhsTypeSymbol,
                                             SyntaxTree syntaxTree, SemanticModel semanticModel) {
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
        return schemas;
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
}

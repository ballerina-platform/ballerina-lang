/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.common.utils;

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.langserver.index.LSIndexException;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.ballerinalang.langserver.index.dao.BPackageSymbolDAO;
import org.ballerinalang.langserver.index.dao.DAOType;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.ConstrainedType;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Common utils to be reuse in language server implementation.
 */
public class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static final String MD_LINE_SEPARATOR = "  " + System.lineSeparator();

    public static final String LINE_SEPARATOR = System.lineSeparator();

    public static final String FILE_SEPARATOR = File.separator;

    public static final String LINE_SEPARATOR_SPLIT = "\\r?\\n";

    public static final Pattern MD_NEW_LINE_PATTERN = Pattern.compile("\\s\\s\\r\\n?|\\s\\s\\n|\\r\\n?|\\n");

    public static final boolean LS_DEBUG_ENABLED;

    public static final boolean LS_TRACE_ENABLED;

    public static final String BALLERINA_HOME;

    public static final String BALLERINA_CMD;

    public static final String MARKDOWN_MARKUP_KIND = "markdown";

    static {
        LS_DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("ballerina.debugLog"));
        LS_TRACE_ENABLED = Boolean.parseBoolean(System.getProperty("ballerina.traceLog"));
        BALLERINA_HOME = System.getProperty("ballerina.home");
        BALLERINA_CMD = BALLERINA_HOME + File.separator + "bin" + File.separator + "ballerina" +
                (SystemUtils.IS_OS_WINDOWS ? ".bat" : "");
    }

    private CommonUtil() {
    }

    /**
     * Get the package URI to the given package name.
     *
     * @param pkgName        Name of the package that need the URI for
     * @param pkgPath        String URI of the current package
     * @param currentPkgName Name of the current package
     * @return String URI for the given path.
     */
    public static String getPackageURI(String pkgName, String pkgPath, String currentPkgName) {
        String newPackagePath;
        // If current package path is not null and current package is not default package continue,
        // else new package path is same as the current package path.
        if (pkgPath != null && !currentPkgName.equals(".")) {
            int indexOfCurrentPkgName = pkgPath.lastIndexOf(currentPkgName);
            if (indexOfCurrentPkgName >= 0) {
                newPackagePath = pkgPath.substring(0, indexOfCurrentPkgName);
            } else {
                newPackagePath = pkgPath;
            }

            if (pkgName.equals(".")) {
                newPackagePath = Paths.get(newPackagePath).toString();
            } else {
                newPackagePath = Paths.get(newPackagePath, pkgName).toString();
            }
        } else {
            newPackagePath = pkgPath;
        }
        return newPackagePath;
    }

    /**
     * Calculate the user defined type position.
     *
     * @param position position of the node
     * @param name     name of the user defined type
     * @param pkgAlias package alias name of the user defined type
     */
    public static void calculateEndColumnOfGivenName(DiagnosticPos position, String name, String pkgAlias) {
        position.eCol = position.sCol + name.length() + (!pkgAlias.isEmpty() ? (pkgAlias + ":").length() : 0);
    }

    /**
     * Convert the diagnostic position to a zero based positioning diagnostic position.
     *
     * @param diagnosticPos - diagnostic position to be cloned
     * @return {@link DiagnosticPos} converted diagnostic position
     */
    public static DiagnosticPos toZeroBasedPosition(DiagnosticPos diagnosticPos) {
        int startLine = diagnosticPos.getStartLine() - 1;
        int endLine = diagnosticPos.getEndLine() - 1;
        int startColumn = diagnosticPos.getStartColumn() - 1;
        int endColumn = diagnosticPos.getEndColumn() - 1;
        return new DiagnosticPos(diagnosticPos.getSource(), startLine, endLine, startColumn, endColumn);
    }

    /**
     * Clone the diagnostic position given.
     *
     * @param diagnosticPos - diagnostic position to be cloned
     * @return {@link DiagnosticPos} cloned diagnostic position
     */
    public static DiagnosticPos clonePosition(DiagnosticPos diagnosticPos) {
        int startLine = diagnosticPos.getStartLine();
        int endLine = diagnosticPos.getEndLine();
        int startColumn = diagnosticPos.getStartColumn();
        int endColumn = diagnosticPos.getEndColumn();
        return new DiagnosticPos(diagnosticPos.getSource(), startLine, endLine, startColumn, endColumn);
    }

    /**
     * Get the previous default token from the given start index.
     *
     * @param tokenStream Token Stream
     * @param startIndex  Start token index
     * @return {@link Optional}      Previous default token
     */
    public static Optional<Token> getPreviousDefaultToken(TokenStream tokenStream, int startIndex) {
        return getDefaultTokenToLeftOrRight(tokenStream, startIndex, -1);
    }

    /**
     * Get the next default token from the given start index.
     *
     * @param tokenStream Token Stream
     * @param startIndex  Start token index
     * @return {@link Optional}      Previous default token
     */
    public static Optional<Token> getNextDefaultToken(TokenStream tokenStream, int startIndex) {
        return getDefaultTokenToLeftOrRight(tokenStream, startIndex, 1);
    }

    /**
     * Get n number of default tokens from a given start index.
     *
     * @param tokenStream Token Stream
     * @param n           number of tokens to extract
     * @param startIndex  Start token index
     * @return {@link List}     List of tokens extracted
     */
    public static List<Token> getNDefaultTokensToLeft(TokenStream tokenStream, int n, int startIndex) {
        List<Token> tokens = new ArrayList<>();
        Optional<Token> token;
        while (n > 0) {
            token = getDefaultTokenToLeftOrRight(tokenStream, startIndex, -1);
            if (!token.isPresent()) {
                return new ArrayList<>();
            }
            tokens.add(token.get());
            n--;
            startIndex = token.get().getTokenIndex();
        }

        return Lists.reverse(tokens);
    }

    private static Optional<Token> getDefaultTokenToLeftOrRight(TokenStream tokenStream, int startIndex,
                                                                int direction) {
        Token token = null;
        while (true) {
            startIndex += direction;
            if (startIndex < 0 || startIndex == tokenStream.size()) {
                break;
            }
            token = tokenStream.get(startIndex);
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                break;
            }
        }
        return Optional.ofNullable(token);
    }

    /**
     * Get the Annotation completion Item.
     *
     * @param packageID        Package Id
     * @param annotationSymbol BLang annotation to extract the completion Item
     * @param ctx              LS Service operation context, in this case completion context
     * @param pkgAlias         LS Service operation context, in this case completion context
     * @return {@link CompletionItem} Completion item for the annotation
     */
    public static CompletionItem getAnnotationCompletionItem(PackageID packageID, BAnnotationSymbol annotationSymbol,
                                                             LSContext ctx, CommonToken pkgAlias,
                                                             Map<String, String> pkgAliasMap) {
        PackageID currentPkgID = ctx.get(DocumentServiceKeys.CURRENT_PACKAGE_ID_KEY);
        String currentProjectOrgName = currentPkgID == null ? "" : currentPkgID.orgName.value;

        String aliasComponent = "";
        if (pkgAliasMap.containsKey(packageID.toString())) {
            // Check if the imported packages contains the particular package with the alias
            aliasComponent = pkgAliasMap.get(packageID.toString());
        } else if (!currentPkgID.name.value.equals(packageID.name.value)) {
            aliasComponent = CommonUtil.getLastItem(packageID.getNameComps()).getValue();
        }

        boolean withAlias = (pkgAlias == null && !aliasComponent.isEmpty());

        String label = getAnnotationLabel(aliasComponent, annotationSymbol, withAlias);
        String insertText = getAnnotationInsertText(aliasComponent, annotationSymbol, withAlias);
        CompletionItem annotationItem = new CompletionItem();
        annotationItem.setLabel(label);
        annotationItem.setInsertText(insertText);
        annotationItem.setInsertTextFormat(InsertTextFormat.Snippet);
        annotationItem.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
        annotationItem.setKind(CompletionItemKind.Property);
        if (currentPkgID != null && currentPkgID.name.value.equals(packageID.name.value)) {
            // If the annotation resides within the current package, no need to set the additional text edits
            return annotationItem;
        }
        List<BLangImportPackage> imports = ctx.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY);
        Optional pkgImport = imports.stream()
                .filter(bLangImportPackage -> {
                    String orgName = bLangImportPackage.orgName.value;
                    String importPkgName = (orgName.equals("") ? currentProjectOrgName : orgName) + "/"
                            + CommonUtil.getPackageNameComponentsCombined(bLangImportPackage);
                    String annotationPkgOrgName = packageID.orgName.getValue();
                    String annotationPkgName = annotationPkgOrgName + "/"
                            + packageID.nameComps.stream()
                            .map(Name::getValue)
                            .collect(Collectors.joining("."));
                    return importPkgName.equals(annotationPkgName);
                })
                .findAny();
        // if the particular import statement not available we add the additional text edit to auto import
        if (!pkgImport.isPresent()) {
            annotationItem.setAdditionalTextEdits(getAutoImportTextEdits(packageID.orgName.getValue(),
                    packageID.name.getValue(), ctx));
        }
        return annotationItem;
    }

    /**
     * Get the Annotation completion Item.
     *
     * @param packageID        Package Id
     * @param annotationSymbol BLang annotation to extract the completion Item
     * @param ctx              LS Service operation context, in this case completion context
     * @param pkgAliasMap      Package alias map for the file
     * @return {@link CompletionItem} Completion item for the annotation
     */
    public static CompletionItem getAnnotationCompletionItem(PackageID packageID, BAnnotationSymbol annotationSymbol,
                                                             LSContext ctx, Map<String, String> pkgAliasMap) {
        return getAnnotationCompletionItem(packageID, annotationSymbol, ctx, null, pkgAliasMap);
    }

    /**
     * Get the text edit for an auto import statement.
     * Here we do not check whether the package is not already imported. Particular check should be done before usage
     *
     * @param orgName package org name
     * @param pkgName package name
     * @param context Language server context
     * @return {@link List}     List of Text Edits to apply
     */
    public static List<TextEdit> getAutoImportTextEdits(String orgName, String pkgName, LSContext context) {
        List<BLangImportPackage> currentFileImports = context.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY);
        Position start = new Position(0, 0);
        if (currentFileImports != null && !currentFileImports.isEmpty()) {
            BLangImportPackage last = CommonUtil.getLastItem(currentFileImports);
            int endLine = last.getPosition().getEndLine();
            start = new Position(endLine, 0);
        }
        String pkgNameComponent;
        // Check for the lang lib module insert text
        if ("ballerina".equals(orgName) && pkgName.startsWith("lang.")) {
            pkgNameComponent = pkgName.replace(".", ".'");
        } else {
            pkgNameComponent = pkgName;
        }
        String importStatement = ItemResolverConstants.IMPORT + " "
                + orgName + CommonKeys.SLASH_KEYWORD_KEY + pkgNameComponent + CommonKeys.SEMI_COLON_SYMBOL_KEY
                + CommonUtil.LINE_SEPARATOR;
        return Collections.singletonList(new TextEdit(new Range(start, start), importStatement));
    }

    /**
     * Fill the completion items extracted from LS Index db with the auto import text edits.
     * Here the Completion Items are mapped against the respective package ID.
     *
     * @param completionsMap Completion Map to evaluate
     * @param ctx            Lang Server Operation Context
     * @return {@link List} List of modified completion items
     */
    public static List<CompletionItem> fillCompletionWithPkgImport(HashMap<Integer, ArrayList<CompletionItem>>
                                                                           completionsMap, LSContext ctx) {
        LSIndexImpl lsIndex = ctx.get(LSGlobalContextKeys.LS_INDEX_KEY);
        List<CompletionItem> returnList = new ArrayList<>();
        completionsMap.forEach((integer, completionItems) -> {
            try {
                BPackageSymbolDTO dto = ((BPackageSymbolDAO) lsIndex.getDaoFactory().get(DAOType.PACKAGE_SYMBOL))
                        .get(integer);
                completionItems.forEach(completionItem -> {
                    List<TextEdit> textEdits = CommonUtil.getAutoImportTextEdits(dto.getOrgName(), dto.getName(), ctx);
                    completionItem.setAdditionalTextEdits(textEdits);
                    returnList.add(completionItem);
                });
            } catch (LSIndexException e) {
                logger.error("Error While retrieving Package Symbol for text edits");
            }
        });

        return returnList;
    }

    /**
     * Populate the given map with the completion item.
     *
     * @param map            ID to completion item map
     * @param id             pkg id in index
     * @param completionItem completion item to populate
     */
    public static void populateIdCompletionMap(HashMap<Integer, ArrayList<CompletionItem>> map, int id,
                                               CompletionItem completionItem) {
        if (map.containsKey(id)) {
            map.get(id).add(completionItem);
        } else {
            map.put(id, new ArrayList<>(Collections.singletonList(completionItem)));
        }
    }

    /**
     * Get the annotation Insert text.
     *
     * @param aliasComponent   Package ID
     * @param annotationSymbol Annotation to get the insert text
     * @param withAlias        insert text with alias
     * @return {@link String} Insert text
     */
    private static String getAnnotationInsertText(String aliasComponent, BAnnotationSymbol annotationSymbol,
                                                  boolean withAlias) {
        StringBuilder annotationStart = new StringBuilder();
        if (withAlias) {
            annotationStart.append(aliasComponent).append(CommonKeys.PKG_DELIMITER_KEYWORD);
        }
        if (annotationSymbol.attachedType != null) {
            annotationStart.append(annotationSymbol.getName().getValue());
            BType attachedType = annotationSymbol.attachedType.type;
            BType resultType = attachedType instanceof BArrayType ? ((BArrayType) attachedType).eType : attachedType;
            if (resultType instanceof BRecordType || resultType instanceof BMapType) {
                List<BField> requiredFields = new ArrayList<>();
                annotationStart.append(" ").append(CommonKeys.OPEN_BRACE_KEY).append(LINE_SEPARATOR);
                if (resultType instanceof BRecordType) {
                    requiredFields.addAll(getRecordRequiredFields(((BRecordType) resultType)));
                }
                List<String> insertTexts = new ArrayList<>();
                requiredFields.forEach(field -> {
                    String fieldInsertionText = "\t" + getRecordFieldCompletionInsertText(field, 1);
                    insertTexts.add(fieldInsertionText);
                });
                annotationStart.append(String.join("," + LINE_SEPARATOR, insertTexts));
                if (requiredFields.isEmpty()) {
                    annotationStart.append("\t").append("${1}");
                }
                annotationStart.append(LINE_SEPARATOR).append(CommonKeys.CLOSE_BRACE_KEY);
            }
        } else {
            annotationStart.append(annotationSymbol.getName().getValue());
        }

        return annotationStart.toString();
    }

    /**
     * Get the completion Label for the annotation.
     *
     * @param aliasComponent package alias
     * @param annotation     BLang annotation
     * @param withAlias      label with alias
     * @return {@link String} Label string
     */
    public static String getAnnotationLabel(String aliasComponent, BAnnotationSymbol annotation, boolean withAlias) {
        String pkgComponent = withAlias ? aliasComponent + CommonKeys.PKG_DELIMITER_KEYWORD : "";
        return pkgComponent + annotation.getName().getValue();
    }

    /**
     * Get the default value for the given BType.
     *
     * @param bType BType to get the default value
     * @return {@link String}   Default value as a String
     */
    public static String getDefaultValueForType(BType bType) {
        String typeString;
        if (bType == null) {
            return "()";
        }
        switch (bType.getKind()) {
            case INT:
                typeString = Integer.toString(0);
                break;
            case FLOAT:
                typeString = Float.toString(0);
                break;
            case STRING:
                typeString = "\"\"";
                break;
            case BOOLEAN:
                typeString = Boolean.toString(false);
                break;
            case ARRAY:
            case BLOB:
                typeString = "[]";
                break;
            case RECORD:
            case MAP:
                typeString = "{}";
                break;
            case OBJECT:
                typeString = "new()";
                break;
            case FINITE:
                List<BLangExpression> valueSpace = new ArrayList<>(((BFiniteType) bType).valueSpace);
                String value = valueSpace.get(0).toString();
                BType type = valueSpace.get(0).type;
                typeString = value;
                if (type.toString().equals("string")) {
                    typeString = "\"" + typeString + "\"";
                }
                break;
            case UNION:
                List<BType> memberTypes = new ArrayList<>(((BUnionType) bType).getMemberTypes());
                typeString = getDefaultValueForType(memberTypes.get(0));
                break;
            case STREAM:
            default:
                typeString = "()";
                break;
        }
        return typeString;
    }

    /**
     * Check whether a given symbol is client object or not.
     *
     * @param bSymbol BSymbol to evaluate
     * @return {@link Boolean}  Symbol evaluation status
     */
    public static boolean isClientObject(BSymbol bSymbol) {
        return bSymbol.type != null && bSymbol.type.tsymbol != null
                && SymbolKind.OBJECT.equals(bSymbol.type.tsymbol.kind)
                && (bSymbol.type.tsymbol.flags & Flags.CLIENT) == Flags.CLIENT;
    }

    /**
     * Check whether the symbol is a listener object.
     *
     * @param bSymbol Symbol to evaluate
     * @return {@link Boolean}  whether listener or not
     */
    public static boolean isListenerObject(BSymbol bSymbol) {
        if (!(bSymbol instanceof BObjectTypeSymbol)) {
            return false;
        }
        List<String> attachedFunctions = ((BObjectTypeSymbol) bSymbol).attachedFuncs.stream()
                .map(function -> function.funcName.getValue())
                .collect(Collectors.toList());
        return attachedFunctions.contains("__start") && attachedFunctions.contains("__immediateStop")
                && attachedFunctions.contains("__immediateStop") && attachedFunctions.contains("__attach");
    }

    /**
     * Check whether the packages list contains a given package.
     *
     * @param pkg     Package to check
     * @param pkgList List of packages to check against
     * @return {@link Boolean}  Check status of the package
     */
    public static boolean listContainsPackage(String pkg, List<BallerinaPackage> pkgList) {
        return pkgList.stream().anyMatch(ballerinaPackage -> ballerinaPackage.getFullPackageNameAlias().equals(pkg));
    }

    /**
     * Get completion items list for struct fields.
     *
     * @param fields List of struct fields
     * @return {@link List}     List of completion items for the struct fields
     */
    public static List<CompletionItem> getRecordFieldCompletionItems(List<BField> fields) {
        List<CompletionItem> completionItems = new ArrayList<>();
        fields.forEach(field -> {
            String insertText = getRecordFieldCompletionInsertText(field, 0);
            CompletionItem fieldItem = new CompletionItem();
            fieldItem.setInsertText(insertText);
            fieldItem.setInsertTextFormat(InsertTextFormat.Snippet);
            fieldItem.setLabel(field.getName().getValue());
            fieldItem.setDetail(ItemResolverConstants.FIELD_TYPE);
            fieldItem.setKind(CompletionItemKind.Field);
            fieldItem.setSortText(Priority.PRIORITY120.toString());
            completionItems.add(fieldItem);
        });

        return completionItems;
    }

    /**
     * Get the completion item to fill all the struct fields.
     *
     * @param fields List of struct fields
     * @return {@link CompletionItem}   Completion Item to fill all the options
     */
    public static CompletionItem getFillAllStructFieldsItem(List<BField> fields) {
        List<String> fieldEntries = new ArrayList<>();

        for (BField bStructField : fields) {
            String defaultFieldEntry = bStructField.getName().getValue()
                    + CommonKeys.PKG_DELIMITER_KEYWORD + " " + getDefaultValueForType(bStructField.getType());
            fieldEntries.add(defaultFieldEntry);
        }

        String insertText = String.join(("," + LINE_SEPARATOR), fieldEntries);
        String label = "Add All Attributes";

        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(label);
        completionItem.setInsertText(insertText);
        completionItem.setDetail(ItemResolverConstants.NONE);
        completionItem.setKind(CompletionItemKind.Property);
        completionItem.setSortText(Priority.PRIORITY110.toString());

        return completionItem;
    }

    /**
     * Get the BType name as string.
     *
     * @param bType BType to get the name
     * @param ctx   LS Operation Context
     * @param doSimplify Simplifies the types eg. Errors
     * @return {@link String}   BType Name as String
     */
    public static String getBTypeName(BType bType, LSContext ctx, boolean doSimplify) {
        if (bType instanceof ConstrainedType) {
            return getConstrainedTypeName(bType, ctx, doSimplify);
        }
        if (bType instanceof BUnionType) {
            return getUnionTypeName((BUnionType) bType, ctx, doSimplify);
        }
        if (bType instanceof BTupleType) {
            return getTupleTypeName((BTupleType) bType, ctx, doSimplify);
        }
        if (bType instanceof BFiniteType || bType instanceof BInvokableType || bType instanceof BNilType) {
            return bType.toString();
        }
        if (bType instanceof BArrayType) {
            return getArrayTypeName((BArrayType) bType, ctx, doSimplify);
        }
        if (bType instanceof BRecordType) {
            return getRecordTypeName((BRecordType) bType, ctx, doSimplify);
        }
        return getShallowBTypeName(bType, ctx);
    }

    private static String getShallowBTypeName(BType bType, LSContext ctx) {
        if (bType.tsymbol == null) {
            return bType.toString();
        }
        if (bType instanceof BArrayType) {
            return getShallowBTypeName(((BArrayType) bType).eType, ctx) + "[]";
        }
        if (bType.tsymbol.pkgID == null) {
            return bType.tsymbol.name.getValue();
        }
        PackageID pkgId = bType.tsymbol.pkgID;
        // split to remove the $ symbol appended type name. (For the service types)
        String[] nameComponents = bType.tsymbol.name.value.split("\\$")[0].split(":");
        if (ctx != null) {
            PackageID currentPkgId = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY).packageID;
            if (pkgId.toString().equals(currentPkgId.toString())
                    || pkgId.getName().getValue().startsWith("lang.")) {
                return nameComponents[nameComponents.length - 1];
            }
        }
        if (pkgId.getName().getValue().startsWith("lang.")) {
            return nameComponents[nameComponents.length - 1];
        }
        return pkgId.getName().getValue().replaceAll(".*\\.", "") + CommonKeys.PKG_DELIMITER_KEYWORD
                + nameComponents[nameComponents.length - 1];
    }

    private static String getUnionTypeName(BUnionType unionType, LSContext ctx, boolean doSimplify) {
        List<BType> nonErrorTypes = new ArrayList<>();
        List<BType> errorTypes = new ArrayList<>();
        StringBuilder unionName = new StringBuilder("(");
        unionType.getMemberTypes().forEach(bType -> {
            if (bType instanceof BErrorType) {
                errorTypes.add(bType);
            } else {
                nonErrorTypes.add(bType);
            }
        });
        String nonErrorsName = nonErrorTypes.stream()
                .map(bType -> getBTypeName(bType, ctx, doSimplify))
                .collect(Collectors.joining("|"));
        unionName.append(nonErrorsName);
        if (errorTypes.size() > 3 && doSimplify) {
            if (nonErrorTypes.isEmpty()) {
                unionName.append("error");
            } else {
                unionName.append("|error");
            }
        } else if (!errorTypes.isEmpty()) {
            String errorsName = errorTypes.stream()
                    .map(bType -> getBTypeName(bType, ctx, doSimplify))
                    .collect(Collectors.joining("|"));

            if (nonErrorTypes.isEmpty()) {
                unionName.append(errorsName);
            } else {
                unionName.append("|").append(errorsName);
            }
        }
        unionName.append(")");
        return unionName.toString();
    }

    private static String getTupleTypeName(BTupleType tupleType, LSContext ctx, boolean doSimplify) {
        return "[" + tupleType.getTupleTypes().stream()
                .map(bType -> getBTypeName(bType, ctx, doSimplify))
                .collect(Collectors.joining(",")) + "]";
    }

    private static String getRecordTypeName(BRecordType recordType, LSContext ctx, boolean doSimplify) {
        if (recordType.tsymbol.kind == SymbolKind.RECORD && recordType.tsymbol.name.value.contains("$anonType")) {
            StringBuilder recordTypeName = new StringBuilder("record {");
            recordTypeName.append(CommonUtil.LINE_SEPARATOR);
            String fieldsList = recordType.fields.stream()
                    .map(field -> getBTypeName(field.type, ctx, doSimplify) + " " + field.name.getValue() + ";")
                    .collect(Collectors.joining(CommonUtil.LINE_SEPARATOR));
            recordTypeName.append(fieldsList).append(CommonUtil.LINE_SEPARATOR).append("}");
            return recordTypeName.toString();
        }

        return getShallowBTypeName(recordType, ctx);
    }

    private static String getArrayTypeName(BArrayType arrayType, LSContext ctx, boolean doSimplify) {
        return getBTypeName(arrayType.eType, ctx, doSimplify) + "[]";
    }

    /**
     * Get the constraint type name.
     *
     * @param bType   BType to evaluate
     * @param context Language server operation context
     * @param doSimplify
     * @return {@link StringBuilder} constraint type name
     */
    private static String getConstrainedTypeName(BType bType, LSContext context, boolean doSimplify) {

        if (!(bType instanceof ConstrainedType)) {
            return "";
        }
        BType constraint = getConstraintType(bType);
        StringBuilder constraintName = new StringBuilder(getShallowBTypeName(bType, context));
        constraintName.append("<");

        if (constraint.tsymbol != null && constraint.tsymbol.kind == SymbolKind.RECORD
                && constraint.tsymbol.name.value.contains("$anonType")) {
            constraintName.append("record {}");
        } else {
            constraintName.append(getBTypeName(constraint, context, doSimplify));
        }

        constraintName.append(">");

        return constraintName.toString();
    }

    private static BType getConstraintType(BType bType) {
        if (bType instanceof BFutureType) {
            return ((BFutureType) bType).constraint;
        }
        if (bType instanceof BMapType) {
            return ((BMapType) bType).constraint;
        }
        if (bType instanceof BStreamType) {
            return ((BStreamType) bType).constraint;
        }
        if (bType instanceof BTypedescType) {
            return ((BTypedescType) bType).constraint;
        }
        return ((BTableType) bType).constraint;

    }

    /**
     * Get the last item of the List.
     *
     * @param list List to get the Last Item
     * @param <T>  List content Type
     * @return Extracted last Item
     */
    public static <T> T getLastItem(List<T> list) {
        return (list.size() == 0) ? null : list.get(list.size() - 1);
    }

    /**
     * Get the last item of the Array.
     *
     * @param list Array to get the Last Item
     * @param <T>  Array content Type
     * @return Extracted last Item
     */
    public static <T> T getLastItem(T[] list) {
        return (list.length == 0) ? null : list[list.length - 1];
    }

    /**
     * Check whether the source is a test source.
     *
     * @param relativeFilePath source path relative to the package
     * @return {@link Boolean}  Whether a test source or not
     */
    public static boolean isTestSource(String relativeFilePath) {
        return relativeFilePath.startsWith("tests" + FILE_SEPARATOR);
    }

    /**
     * Get the Source's owner BLang package, this can be either the parent package or the testable BLang package.
     *
     * @param relativePath Relative source path
     * @param parentPkg    parent package
     * @return {@link BLangPackage} Resolved BLangPackage
     */
    public static BLangPackage getSourceOwnerBLangPackage(String relativePath, BLangPackage parentPkg) {
        return isTestSource(relativePath) ? parentPkg.getTestablePkg() : parentPkg;
    }

    /**
     * Check whether the symbol is a valid invokable symbol.
     *
     * @param symbol Symbol to be evaluated
     * @return {@link Boolean}  valid status
     */
    public static boolean isValidInvokableSymbol(BSymbol symbol) {
        if (!(symbol instanceof BInvokableSymbol)) {
            return false;
        }

        BInvokableSymbol bInvokableSymbol = (BInvokableSymbol) symbol;
        return ((bInvokableSymbol.kind == null
                && (SymbolKind.RECORD.equals(bInvokableSymbol.owner.kind)
                || SymbolKind.FUNCTION.equals(bInvokableSymbol.owner.kind)))
                || SymbolKind.FUNCTION.equals(bInvokableSymbol.kind)) &&
                (!(bInvokableSymbol.name.value.endsWith(BLangConstants.INIT_FUNCTION_SUFFIX)
                        || bInvokableSymbol.name.value.endsWith(BLangConstants.START_FUNCTION_SUFFIX)
                        || bInvokableSymbol.name.value.endsWith(BLangConstants.STOP_FUNCTION_SUFFIX)));
    }

    /**
     * Get the current module's imports.
     *
     * @param ctx LS Operation Context
     * @return {@link List}     List of imports in the current file
     */
    public static List<BLangImportPackage> getCurrentFileImports(LSContext ctx) {
        return getCurrentModuleImports(ctx).stream()
                .filter(importInCurrentFilePredicate(ctx))
                .collect(Collectors.toList());
    }

    public static boolean isInvalidSymbol(BSymbol symbol) {
        return ("_".equals(symbol.name.getValue())
                || symbol instanceof BAnnotationSymbol
                || symbol instanceof BOperatorSymbol
                || symbolContainsInvalidChars(symbol));
    }

    /**
     * Check whether the given node is a worker derivative node.
     *
     * @param node Node to be evaluated
     * @return {@link Boolean}  whether a worker derivative
     */
    public static boolean isWorkerDereivative(BLangNode node) {
        return (node instanceof BLangSimpleVariableDef)
                && ((BLangSimpleVariableDef) node).var.expr != null
                && ((BLangSimpleVariableDef) node).var.expr.type instanceof BFutureType
                && ((BFutureType) ((BLangSimpleVariableDef) node).var.expr.type).workerDerivative;
    }

    /**
     * Get the TopLevel nodes of the current file.
     *
     * @param pkgNode Current Package node
     * @param ctx     Service Operation context
     * @return {@link List}     List of Top Level Nodes
     */
    public static List<TopLevelNode> getCurrentFileTopLevelNodes(BLangPackage pkgNode, LSContext ctx) {
        String relativeFilePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangCompilationUnit filteredCUnit = pkgNode.compUnits.stream()
                .filter(cUnit ->
                                cUnit.getPosition().getSource().cUnitName.replace("/", FILE_SEPARATOR)
                                        .equals(relativeFilePath))
                .findAny().orElse(null);
        List<TopLevelNode> topLevelNodes = filteredCUnit == null
                ? new ArrayList<>()
                : new ArrayList<>(filteredCUnit.getTopLevelNodes());

        // Filter out the lambda functions from the top level nodes
        return topLevelNodes.stream()
                .filter(topLevelNode -> !(topLevelNode instanceof BLangFunction
                        && ((BLangFunction) topLevelNode).flagSet.contains(Flag.LAMBDA))
                        && !(topLevelNode instanceof BLangSimpleVariable
                        && ((BLangSimpleVariable) topLevelNode).flagSet.contains(Flag.SERVICE))
                        && !(topLevelNode instanceof BLangImportPackage && topLevelNode.getWS() == null))
                .collect(Collectors.toList());
    }

    /**
     * Get the package name components combined.
     *
     * @param importPackage BLangImportPackage node
     * @return {@link String}   Combined package name
     */
    public static String getPackageNameComponentsCombined(BLangImportPackage importPackage) {
        return importPackage.pkgNameComps.stream()
                .map(id -> id.value)
                .collect(Collectors.joining("."));
    }

    /**
     * Convert the Snippet to a plain text snippet by removing the place holders.
     *
     * @param snippet Snippet string to alter
     * @return {@link String}   Converted Snippet
     */
    public static String getPlainTextSnippet(String snippet) {
        return snippet
                .replaceAll("\\$\\{\\d+:([^\\{^\\}]*)\\}", "$1")
                .replaceAll("(\\$\\{\\d+\\})", "");
    }

    public static BallerinaParser prepareParser(String content) {
        CommonTokenStream commonTokenStream = getTokenStream(content);
        BallerinaParser parser = new BallerinaParser(commonTokenStream);
        parser.removeErrorListeners();
        return parser;
    }

    static CommonTokenStream getTokenStream(String content) {
        ANTLRInputStream inputStream = new ANTLRInputStream(content);
        BallerinaLexer lexer = new BallerinaLexer(inputStream);
        lexer.removeErrorListeners();
        return new CommonTokenStream(lexer);
    }

    public static List<Token> getTokenList(String content) {
        CommonTokenStream tokenStream = getTokenStream(content);
        tokenStream.fill();
        return new ArrayList<>(tokenStream.getTokens());
    }

    public static boolean symbolContainsInvalidChars(BSymbol bSymbol) {
        List<String> symbolNameComponents = Arrays.asList(bSymbol.getName().getValue().split("\\."));
        String symbolName = CommonUtil.getLastItem(symbolNameComponents);

        return symbolName != null && (symbolName.contains(CommonKeys.LT_SYMBOL_KEY)
                || symbolName.contains(CommonKeys.GT_SYMBOL_KEY)
                || symbolName.contains(CommonKeys.DOLLAR_SYMBOL_KEY)
                || symbolName.equals("main")
                || symbolName.endsWith(".new")
                || symbolName.startsWith("0"));
    }

    /**
     * Get the function name from the Invokable symbol.
     *
     * @param bInvokableSymbol symbol
     * @return {@link String} Function name
     */
    public static String getFunctionNameFromSymbol(BInvokableSymbol bInvokableSymbol) {
        String[] funcNameComponents = bInvokableSymbol.getName().getValue().split("\\.");
        String functionName = funcNameComponents[funcNameComponents.length - 1];

        // If there is a receiver symbol, then the name comes with the package name and struct name appended.
        // Hence we need to remove it
        if (bInvokableSymbol.receiverSymbol != null) {
            String receiverType = bInvokableSymbol.receiverSymbol.getType().toString();
            functionName = functionName.replace(receiverType + ".", "");
        }

        return functionName;
    }

    /**
     * Get the function invocation signature.
     *
     * @param symbol       ballerina function instance
     * @param functionName function name
     * @param ctx          Language Server Operation context
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    public static Pair<String, String> getFunctionInvocationSignature(BInvokableSymbol symbol, String functionName,
                                                                      LSContext ctx) {
        StringBuilder signature = new StringBuilder(functionName + "(");
        StringBuilder insertText = new StringBuilder(functionName + "(");
        List<String> funcArguments = FunctionGenerator.getFuncArguments(symbol, ctx);
        if (!funcArguments.isEmpty()) {
            signature.append(String.join(", ", funcArguments));
            insertText.append("${1}");
        }
        signature.append(")");
        insertText.append(")");
        if (symbol.type.getReturnType() == null || symbol.type.getReturnType() instanceof BNilType) {
            insertText.append(";");
        }
        String initString = "(";
        String endString = ")";

        BType returnType = symbol.type.getReturnType();
        if (returnType != null && !(returnType instanceof BNilType)) {
            signature.append(initString).append(CommonUtil.getBTypeName(returnType, ctx, false));
            signature.append(endString);
        }

        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    /**
     * Get visible worker symbols from context.
     *
     * @param context Language Server operation conext
     * @return {@link List} filtered visible symbols
     */
    public static List<SymbolInfo> getWorkerSymbols(LSContext context) {
        List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        return visibleSymbols.stream().filter(symbolInfo -> {
            BType bType = symbolInfo.getScopeEntry().symbol.type;
            return bType instanceof BFutureType && ((BFutureType) bType).workerDerivative;
        }).collect(Collectors.toList());
    }

    private static List<BField> getRecordRequiredFields(BRecordType recordType) {
        return recordType.fields.stream()
                .filter(field -> (field.symbol.flags & Flags.REQUIRED) == Flags.REQUIRED)
                .collect(Collectors.toList());
    }

    /**
     * Get the completion item insert text for a BField.
     *
     * @param bField BField to evaluate
     * @return {@link String} Insert text
     */
    private static String getRecordFieldCompletionInsertText(BField bField, int tabOffset) {
        BType fieldType = bField.getType();
        StringBuilder insertText = new StringBuilder(bField.getName().getValue() + ": ");
        if (fieldType instanceof BRecordType) {
            List<BField> requiredFields = getRecordRequiredFields((BRecordType) fieldType);
            if (requiredFields.isEmpty()) {
                insertText.append("{").append("${1}}");
                return insertText.toString();
            }
            insertText.append("{").append(LINE_SEPARATOR);
            int tabCount = tabOffset;
            List<String> requiredFieldInsertTexts = new ArrayList<>();
            for (BField requiredField : requiredFields) {
                String fieldText = String.join("", Collections.nCopies(tabCount + 1, "\t")) +
                        getRecordFieldCompletionInsertText(requiredField, tabCount) +
                        String.join("", Collections.nCopies(tabCount, "\t"));
                requiredFieldInsertTexts.add(fieldText);
                tabCount++;
            }
            insertText.append(String.join(CommonUtil.LINE_SEPARATOR, requiredFieldInsertTexts));
            insertText.append(LINE_SEPARATOR)
                    .append(String.join("", Collections.nCopies(tabOffset, "\t")))
                    .append("}");
        } else if (fieldType instanceof BArrayType) {
            insertText.append("[").append("${1}").append("]");
        } else if (fieldType.tsymbol != null && fieldType.tsymbol.name.getValue().equals("string")) {
            insertText.append("\"").append("${1}").append("\"");
        } else {
            insertText.append("${1:").append(getDefaultValueForType(bField.getType())).append("}");
        }

        return insertText.toString();
    }

    /**
     * Get the current module's imports.
     *
     * @param ctx LS Operation Context
     * @return {@link List}     List of imports in the current file
     */
    public static List<BLangImportPackage> getCurrentModuleImports(LSContext ctx) {
        String relativePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangPackage currentPkg = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        BLangPackage ownerPkg = getSourceOwnerBLangPackage(relativePath, currentPkg);
        return ownerPkg.imports;
    }

    ///////////////////////////////
    /////      Predicates     /////
    ///////////////////////////////

    /**
     * Predicate to check for the invalid symbols.
     *
     * @return {@link Predicate}    Predicate for the check
     */
    public static Predicate<SymbolInfo> invalidSymbolsPredicate() {
        return symbolInfo -> !symbolInfo.isCustomOperation()
                && symbolInfo.getScopeEntry() != null
                && isInvalidSymbol(symbolInfo.getScopeEntry().symbol);
    }

    /**
     * Predicate to check for the invalid symbols.
     *
     * @return {@link Predicate}    Predicate for the check
     */
    public static Predicate<BLangImportPackage> importInCurrentFilePredicate(LSContext ctx) {
        String currentFile = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        return importPkg -> importPkg.pos.getSource().cUnitName.replace("/", FILE_SEPARATOR).equals(currentFile)
                && importPkg.getWS() != null;
    }

    /**
     * Predicate to check for the invalid type definitions.
     *
     * @return {@link Predicate}    Predicate for the check
     */
    public static Predicate<TopLevelNode> checkInvalidTypesDefs() {
        return topLevelNode -> {
            if (topLevelNode instanceof BLangTypeDefinition) {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) topLevelNode;
                return !(typeDefinition.flagSet.contains(Flag.SERVICE) ||
                        typeDefinition.flagSet.contains(Flag.RESOURCE));
            }
            return true;
        };
    }

    /**
     * Generate variable code.
     *
     * @param variableName variable name
     * @param variableType variable type
     * @return {@link String}       generated function signature
     */
    public static String createVariableDeclaration(String variableName, String variableType) {
        return variableType + " " + variableName + " = ";
    }

    /**
     * Generates a random name.
     *
     * @param value    index of the argument
     * @param argNames argument set
     * @return random argument name
     */
    public static String generateName(int value, Set<String> argNames) {
        StringBuilder result = new StringBuilder();
        int index = value;
        while (--index >= 0) {
            result.insert(0, (char) ('a' + index % 26));
            index /= 26;
        }
        while (argNames.contains(result.toString())) {
            result = new StringBuilder(generateName(++value, argNames));
        }
        return result.toString();
    }

    /**
     * Generates a variable name.
     *
     * @param bLangNode {@link BLangNode}
     * @return random argument name
     */
    public static String generateVariableName(BLangNode bLangNode, Set<String> names) {
        String newName = generateName(1, names);
        if (bLangNode instanceof BLangInvocation) {
            return generateVariableName(1, ((BLangInvocation) bLangNode).name.value, names);
        }
        return newName;
    }

    /**
     * Generates a variable name.
     *
     * @param bType {@link BType}
     * @return random argument name
     */
    public static String generateVariableName(BType bType, Set<String> names) {
        String value = bType.name.getValue();
        if (value.isEmpty() && bType.tsymbol != null) {
            value = bType.tsymbol.name.value;
        }
        return generateVariableName(1, value, names);
    }

    private static String generateVariableName(int value, String name, Set<String> names) {
        String newName = generateName(value, names);
        if (value == 1 && !name.isEmpty()) {
            newName = name;
            BiFunction<String, String, String> replacer = (search, text) ->
                    (text.startsWith(search)) ? text.replaceFirst(search, "") : text;
            // Replace common prefixes
            newName = replacer.apply("get", newName);
            newName = replacer.apply("put", newName);
            newName = replacer.apply("delete", newName);
            newName = replacer.apply("update", newName);
            newName = replacer.apply("set", newName);
            newName = replacer.apply("add", newName);
            newName = replacer.apply("create", newName);
            // Remove '_' underscores
            while (newName.contains("_")) {
                String[] parts = newName.split("_");
                List<String> restParts = Arrays.stream(parts, 1, parts.length).collect(Collectors.toList());
                newName = parts[0] + StringUtils.capitalize(String.join("", restParts));
            }
            // If empty, revert back to original name
            if (newName.isEmpty()) {
                newName = name;
            }
            // Lower first letter
            newName = newName.substring(0, 1).toLowerCase(Locale.getDefault()) + newName.substring(1);
            // if already available, try appending 'Result'
            Iterator<String> iterator = names.iterator();
            boolean alreadyExists = false;
            boolean appendResult = true;
            boolean appendOut = true;
            String suffixResult = "Result";
            String suffixOut = "Out";
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (next.equals(newName)) {
                    alreadyExists = true;
                } else if (next.equals(newName + suffixResult)) {
                    appendResult = false;
                } else if (next.equals(newName + suffixOut)) {
                    appendOut = false;
                }
            }
            // if already available, try appending 'Result' or 'Out'
            if (alreadyExists && appendResult) {
                newName = newName + suffixResult;
            } else if (alreadyExists && appendOut) {
                newName = newName + suffixOut;
            }
            // if still already available, try a random letter
            while (names.contains(newName)) {
                newName = generateVariableName(++value, name, names);
            }
        }
        return newName;
    }

    public static BLangPackage getPackageNode(BLangNode bLangNode) {
        BLangNode parent = bLangNode.parent;
        if (parent != null) {
            return (parent instanceof BLangPackage) ? (BLangPackage) parent : getPackageNode(parent);
        }
        return null;
    }

    public static String getPackagePrefix(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                          PackageID typePkgId) {
        String pkgPrefix = "";
        if (!typePkgId.equals(currentPkgId) &&
                !(typePkgId.orgName.value.equals("ballerina") && typePkgId.name.value.startsWith("lang."))) {
            pkgPrefix = typePkgId.name.value.replaceAll(".*\\.", "") + ":";
            if (importsAcceptor != null) {
                importsAcceptor.accept(typePkgId.orgName.value, typePkgId.name.value);
            }
        }
        return pkgPrefix;
    }

    /**
     * Node comparator to compare the nodes by position.
     */
    public static class BLangNodeComparator implements Comparator<BLangNode> {
        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(BLangNode node1, BLangNode node2) {
            return node1.getPosition().getStartLine() - node2.getPosition().getStartLine();
        }
    }

    /**
     * Whether we skip the first parameter being included as a label in the signature.
     * When showing a lang lib invokable symbol over DOT(invocation) we do not show the first param, but when we
     * showing the invocation over package of the langlib with the COLON we show the first param
     *
     * @param invokableSymbol invokable symbol
     * @param invocationType  delimiter
     * @return {@link Boolean} whether we show the first param or not
     */
    public static boolean skipFirstParam(BInvokableSymbol invokableSymbol, int invocationType) {
        return isLangLibSymbol(invokableSymbol) && invocationType != BallerinaParser.COLON;
    }

    /**
     * Get all available name entries.
     *
     * @param bLangNode {@link BLangNode}
     * @param context   {@link CompilerContext}
     * @return set of strings
     */
    public static Set<String> getAllNameEntries(BLangNode bLangNode, CompilerContext context) {
        Set<String> strings = new HashSet<>();
        BLangPackage packageNode = null;
        BLangNode parent = bLangNode.parent;
        // Retrieve package node
        while (parent != null) {
            if (parent instanceof BLangPackage) {
                packageNode = (BLangPackage) parent;
                break;
            }
            if (parent instanceof BLangFunction) {
                BLangFunction bLangFunction = (BLangFunction) parent;
                bLangFunction.requiredParams.forEach(var -> strings.add(var.name.value));
            }
            parent = parent.parent;
        }

        if (packageNode != null) {
            packageNode.getGlobalVariables().forEach(globalVar -> strings.add(globalVar.name.value));
            packageNode.getGlobalEndpoints().forEach(endpoint -> strings.add(endpoint.getName().getValue()));
            packageNode.getServices().forEach(service -> strings.add(service.name.value));
            packageNode.getFunctions().forEach(func -> strings.add(func.name.value));
        }
        // Retrieve block stmt
        parent = bLangNode.parent;
        while (parent != null && !(parent instanceof BLangBlockStmt)) {
            parent = parent.parent;
        }
        if (parent != null && packageNode != null) {
            SymbolResolver symbolResolver = SymbolResolver.getInstance(context);
            SymbolTable symbolTable = SymbolTable.getInstance(context);
            BLangBlockStmt blockStmt = (BLangBlockStmt) parent;
            SymbolEnv symbolEnv = symbolTable.pkgEnvMap.get(packageNode.symbol);
            SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockStmt, symbolEnv);
            Map<Name, List<Scope.ScopeEntry>> entries = symbolResolver
                    .getAllVisibleInScopeSymbols(blockEnv);
            entries.forEach((name, scopeEntries) ->
                    scopeEntries.forEach(scopeEntry -> strings.add(scopeEntry.symbol.name.value)));
        }
        return strings;
    }

    /**
     * Check whether the given symbol is a symbol within the langlib.
     *
     * @param symbol BSymbol to evaluate
     * @return {@link Boolean} whether langlib symbol or not
     */
    public static boolean isLangLibSymbol(BSymbol symbol) {
        return ((symbol.flags & Flags.LANG_LIB) == Flags.LANG_LIB);
    }

    public static BPackageSymbolDTO getPackageSymbolDTO(LSContext ctx, String pkgName) {
        Optional bLangImport = getCurrentFileImports(ctx).stream()
                .filter(importPkg -> importPkg.getAlias().getValue().equals(pkgName))
                .findFirst();
        String realPkgName;
        String realOrgName;

        if (bLangImport.isPresent()) {
            // There is an added import statement.
            realPkgName = CommonUtil.getPackageNameComponentsCombined(((BLangImportPackage) bLangImport.get()));
            realOrgName = ((BLangImportPackage) bLangImport.get()).getOrgName().getValue();
        } else {
            realPkgName = pkgName;
            realOrgName = "";
        }

        return new BPackageSymbolDTO.BPackageSymbolDTOBuilder()
                .setName(realPkgName)
                .setOrgName(realOrgName)
                .build();
    }

    /**
     * Get the path from given string URI.
     *
     * @param uri file uri
     * @return {@link Optional} Path from the URI
     */
    public static Optional<Path> getPathFromURI(String uri) {
        try {
            return Optional.ofNullable(Paths.get(new URL(uri).toURI()));
        } catch (URISyntaxException | MalformedURLException e) {
            // ignore
        }
        return Optional.empty();
    }
}

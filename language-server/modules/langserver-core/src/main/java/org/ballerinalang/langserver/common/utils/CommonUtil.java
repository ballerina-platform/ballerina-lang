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
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.index.LSIndexException;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.ballerinalang.langserver.index.dao.BPackageSymbolDAO;
import org.ballerinalang.langserver.index.dao.DAOType;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.BLangConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;
import static org.ballerinalang.util.BLangConstants.CONSTRUCTOR_FUNCTION_SUFFIX;

/**
 * Common utils to be reuse in language server implementation.
 */
public class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static final String MD_LINE_SEPARATOR = "  " + System.lineSeparator();

    public static final String LINE_SEPARATOR = System.lineSeparator();

    public static final String FILE_SEPARATOR = File.separator;

    public static final String LINE_SEPARATOR_SPLIT = "\\r?\\n";

    public static final boolean LS_DEBUG_ENABLED;

    public static final String BALLERINA_HOME;

    public static final String MARKDOWN_MARKUP_KIND = "markdown";

    static {
        String debugLogStr = System.getProperty("ballerina.debugLog");
        LS_DEBUG_ENABLED = Boolean.parseBoolean(debugLogStr);
        BALLERINA_HOME = System.getProperty("ballerina.home");
    }

    private CommonUtil() {
    }

    /**
     * Get the package URI to the given package name.
     *
     * @param pkgName        Name of the package that need the URI for
     * @param pkgPath String URI of the current package
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
     * Replace and returns a diagnostic position with a new position.
     *
     * @param oldPos old position
     * @param newPos new position
     */
    public static void replacePosition(DiagnosticPos oldPos, DiagnosticPos newPos) {
        oldPos.sLine = newPos.sLine;
        oldPos.eLine = newPos.eLine;
        oldPos.sCol = newPos.sCol;
        oldPos.eCol = newPos.eCol;
    }

    /**
     * Get the previous default token from the given start index.
     *
     * @param tokenStream                   Token Stream
     * @param startIndex                    Start token index
     * @return {@link Optional}      Previous default token
     */
    public static Optional<Token> getPreviousDefaultToken(TokenStream tokenStream, int startIndex) {
        return getDefaultTokenToLeftOrRight(tokenStream, startIndex, -1);
    }

    /**
     * Get the next default token from the given start index.
     *
     * @param tokenStream                   Token Stream
     * @param startIndex                    Start token index
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

    /**
     * Get the Nth Default token to the left of current token index.
     *
     * @param tokenStream                   Token Stream to traverse
     * @param startIndex                    Start position of the token stream
     * @param offset                        Number of tokens to traverse left
     * @return {@link Optional}      Nth Token
     */
    public static Optional<Token> getNthDefaultTokensToLeft(TokenStream tokenStream, int startIndex, int offset) {
        Optional<Token> token = Optional.empty();
        int indexCounter = startIndex;
        for (int i = 0; i < offset; i++) {
            token = getPreviousDefaultToken(tokenStream, indexCounter);
            if (!token.isPresent()) {
                break;
            }
            indexCounter = token.get().getTokenIndex();
        }

        return token;
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
     * Get the top level node type at the cursor line.
     *
     * @param identifier Document Identifier
     * @param cursorLine Cursor line
     * @param docManager Workspace document manager
     * @return {@link String}   Top level node type
     */
    public static String topLevelNodeInLine(TextDocumentIdentifier identifier, int cursorLine,
                                            WorkspaceDocumentManager docManager) {
        List<String> topLevelKeywords = Arrays.asList("function", "service", "resource", "endpoint");
        LSDocument document = new LSDocument(identifier.getUri());

        try {
            Path filePath = document.getPath();
            Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath);
            String fileContent = docManager.getFileContent(compilationPath);
            String[] splitedFileContent = fileContent.split(LINE_SEPARATOR_SPLIT);
            if ((splitedFileContent.length - 1) >= cursorLine) {
                String lineContent = splitedFileContent[cursorLine];
                List<String> alphaNumericTokens = new ArrayList<>(Arrays.asList(lineContent.split("[^\\w']+")));

                ListIterator<String> iterator = alphaNumericTokens.listIterator();
                int tokenCounter = 0;
                while (iterator.hasNext()) {
                    String topLevelKeyword = iterator.next();

                    boolean validTypeDef = (topLevelKeyword.equals(CommonKeys.RECORD_KEYWORD_KEY)
                            || topLevelKeyword.equals(CommonKeys.OBJECT_KEYWORD_KEY))
                            && tokenCounter > 1
                            && alphaNumericTokens.get(tokenCounter - 2).equals("type");

                    if (validTypeDef || (topLevelKeywords.contains(topLevelKeyword) &&
                            (!iterator.hasNext() || !CONSTRUCTOR_FUNCTION_SUFFIX.equals(iterator.next())))) {
                        return topLevelKeyword;
                    }
                    tokenCounter++;
                }


            }
            return null;
        } catch (WorkspaceDocumentException e) {
            logger.error("Error occurred while reading content of file: " + document.toString());
            return null;
        }
    }

    /**
     * Get current package by given file name.
     *
     * @param packages list of packages to be searched
     * @param fileUri  string file URI
     * @return {@link BLangPackage} current package
     */
    public static BLangPackage getCurrentPackageByFileName(List<BLangPackage> packages, String fileUri) {
        Path filePath = new LSDocument(fileUri).getPath();
        String currentModule = LSCompilerUtil.getCurrentModulePath(filePath).getFileName().toString();
        try {
            for (BLangPackage bLangPackage : packages) {
                if (bLangPackage.packageID.sourceFileName != null &&
                        bLangPackage.packageID.sourceFileName.value.equals(filePath.getFileName().toString())) {
                    return bLangPackage;
                } else if (currentModule.equals(bLangPackage.packageID.name.value)) {
                    return bLangPackage;
                }
            }
        } catch (NullPointerException e) {
            return packages.get(0);
        }
        return null;
    }

    /**
     * Get the Annotation completion Item.
     *
     * @param packageID Package Id
     * @param annotationSymbol BLang annotation to extract the completion Item
     * @param ctx LS Service operation context, in this case completion context
     * @param pkgAlias LS Service operation context, in this case completion context
     * @return {@link CompletionItem} Completion item for the annotation
     */
    public static CompletionItem getAnnotationCompletionItem(PackageID packageID, BAnnotationSymbol annotationSymbol,
                                                             LSContext ctx, CommonToken pkgAlias) {
        boolean withAlias = pkgAlias == null;
        BLangPackage bLangPackage = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        Map<String, String> pkgAliasMap = CommonUtil.getCurrentFileImports(bLangPackage, ctx).stream()
                .collect(Collectors.toMap(pkg -> pkg.symbol.pkgID.toString(), pkg -> pkg.alias.value));
        
        String aliasComponent = "";
        if (pkgAliasMap.containsKey(packageID.toString())) {
            // Check if the imported packages contains the particular package with the alias
            aliasComponent = pkgAliasMap.get(packageID.toString());
        } else if (!packageID.getName().getValue().equals(Names.BUILTIN_PACKAGE.getValue()) && withAlias) {
            aliasComponent = CommonUtil.getLastItem(packageID.getNameComps()).getValue();
        }
        
        String label = getAnnotationLabel(aliasComponent, annotationSymbol, withAlias);
        String insertText = getAnnotationInsertText(aliasComponent, annotationSymbol, withAlias);
        CompletionItem annotationItem = new CompletionItem();
        annotationItem.setLabel(label);
        annotationItem.setInsertText(insertText);
        annotationItem.setInsertTextFormat(InsertTextFormat.Snippet);
        annotationItem.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
        annotationItem.setKind(CompletionItemKind.Property);
        String relativePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangPackage pkg = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativePath, pkg);
        List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(srcOwnerPkg, ctx);
        Optional currentPkgImport = imports.stream()
                .filter(bLangImportPackage -> {
                    String pkgName = bLangImportPackage.orgName + "/"
                            + CommonUtil.getPackageNameComponentsCombined(bLangImportPackage);
                    String evalPkgName = packageID.orgName + "/" + packageID.nameComps.stream()
                            .map(Name::getValue).collect(Collectors.joining("."));
                    return pkgName.equals(evalPkgName);
                })
                .findAny();
        // if the particular import statement not available we add the additional text edit to auto import
        if (!currentPkgImport.isPresent()) {
            annotationItem.setAdditionalTextEdits(getAutoImportTextEdits(ctx, packageID.orgName.getValue(),
                    packageID.name.getValue()));
        }
        return annotationItem;
    }

    /**
     * Get the text edit for an auto import statement.
     *
     * @param ctx               Service operation context
     * @param orgName           package org name
     * @param pkgName           package name
     * @return {@link List}     List of Text Edits to apply
     */
    public static List<TextEdit> getAutoImportTextEdits(LSContext ctx, String orgName, String pkgName) {
        if (CommonKeys.BALLERINA_KW.equals(orgName) && CommonKeys.BUILTIN_KW.equals(pkgName)) {
            return new ArrayList<>();
        }
        String relativePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangPackage pkg = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        if (relativePath == null || pkg == null) {
            return new ArrayList<>();
        }
        BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativePath, pkg);
        List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(srcOwnerPkg, ctx);
        for (BLangImportPackage importPackage : imports) {
            if (importPackage.orgName.value.equals(orgName) && importPackage.alias.value.equals(pkgName)) {
                return new ArrayList<>();
            }
        }
        Position start = new Position(0, 0);
        if (!imports.isEmpty()) {
            BLangImportPackage last = CommonUtil.getLastItem(imports);
            int endLine = last.getPosition().getEndLine();
            start = new Position(endLine, 0);
        }

        String importStatement = ItemResolverConstants.IMPORT + " "
                + orgName + CommonKeys.SLASH_KEYWORD_KEY + pkgName + CommonKeys.SEMI_COLON_SYMBOL_KEY
                + CommonUtil.LINE_SEPARATOR;
        return Collections.singletonList(new TextEdit(new Range(start, start), importStatement));
    }

    /**
     * Fill the completion items extracted from LS Index db with the auto import text edits.
     * Here the Completion Items are mapped against the respective package ID.
     * @param completionsMap    Completion Map to evaluate
     * @param ctx               Lang Server Operation Context
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
                    List<TextEdit> textEdits = CommonUtil.getAutoImportTextEdits(ctx, dto.getOrgName(), dto.getName());
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
     * @param map               ID to completion item map
     * @param id                pkg id in index
     * @param completionItem    completion item to populate
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
     * @param aliasComponent Package ID
     * @param annotationSymbol Annotation to get the insert text
     * @param withAlias insert text with alias
     * @return {@link String} Insert text
     */
    private static String getAnnotationInsertText(String aliasComponent, BAnnotationSymbol annotationSymbol,
                                                  boolean withAlias) {
        StringBuilder annotationStart = new StringBuilder();
        if (withAlias) {
            annotationStart.append(aliasComponent).append(CommonKeys.PKG_DELIMITER_KEYWORD);
        }
        if (annotationSymbol.attachedType != null) {
            annotationStart.append(annotationSymbol.getName().getValue()).append(" ").append(CommonKeys.OPEN_BRACE_KEY)
                    .append(LINE_SEPARATOR);
            List<BField> requiredFields = new ArrayList<>();
            if (annotationSymbol.attachedType.type instanceof BRecordType) {
                requiredFields = getRecordRequiredFields(((BRecordType) annotationSymbol.attachedType.type));
                List<String> insertTexts = new ArrayList<>();
                requiredFields.forEach(field -> {
                    String fieldInsertionText = "\t" + getRecordFieldCompletionInsertText(field, 1);
                    insertTexts.add(fieldInsertionText);
                });
                annotationStart.append(String.join("," + LINE_SEPARATOR, insertTexts));
            }
            if (requiredFields.isEmpty()) {
                annotationStart.append("\t").append("${1}").append(LINE_SEPARATOR);
            }
            annotationStart.append(CommonKeys.CLOSE_BRACE_KEY);
        } else {
            annotationStart.append(annotationSymbol.getName().getValue());
        }

        return annotationStart.toString();
    }

    /**
     * Get the completion Label for the annotation.
     *
     * @param aliasComponent package alias
     * @param annotation BLang annotation
     * @param withAlias label with alias
     * @return {@link String} Label string
     */
    private static String getAnnotationLabel(String aliasComponent, BAnnotationSymbol annotation, boolean withAlias) {
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
     * @param bSymbol           Symbol to evaluate
     * @return {@link Boolean}  whether listener or not
     */
    public static boolean isListenerObject(BSymbol bSymbol) {
        if (!(bSymbol instanceof BObjectTypeSymbol)) {
            return false;
        }
        List<String> attachedFunctions = ((BObjectTypeSymbol) bSymbol).attachedFuncs.stream()
                .map(function -> function.funcName.getValue())
                .collect(Collectors.toList());
        return attachedFunctions.contains("__start") && attachedFunctions.contains("__stop")
                && attachedFunctions.contains("__attach");
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

        for (int i = 0; i < fields.size(); i++) {
            BField bStructField = fields.get(i);
            String defaultFieldEntry = bStructField.getName().getValue()
                    + CommonKeys.PKG_DELIMITER_KEYWORD + " " + getDefaultValueForType(bStructField.getType());
            if (bStructField.getType() instanceof BFiniteType || bStructField.getType() instanceof BUnionType) {
                defaultFieldEntry += (i < fields.size() - 1 ? "," : "") +
                        getFiniteAndUnionTypesComment(bStructField.type);
            }
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
     * @return {@link String}   BType Name as String
     */
    public static String getBTypeName(BType bType, LSContext ctx) {
        if (bType.tsymbol == null || bType.tsymbol.pkgID == null) {
            return bType.toString();
        }
        PackageID pkgId = bType.tsymbol.pkgID;
        PackageID currentPkgId = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY).packageID;
        // split to remove the $ symbol appended type name. (For the service types)
        String[] nameComponents = bType.toString().split("\\$")[0].split(":");
        if (pkgId.toString().equals(currentPkgId.toString()) || pkgId.getName().getValue().equals("builtin")) {
            return nameComponents[nameComponents.length - 1];
        } else {
            return pkgId.getName().getValue() + CommonKeys.PKG_DELIMITER_KEYWORD
                    + nameComponents[nameComponents.length - 1];
        }
    }

    /**
     * Get the last item of the List.
     *
     * @param list  List to get the Last Item
     * @param <T>   List content Type
     * @return      Extracted last Item
     */
    public static <T> T getLastItem(List<T> list) {
        return (list.size() == 0) ? null : list.get(list.size() - 1);
    }

    /**
     * Get the last item of the Array.
     *
     * @param list  Array to get the Last Item
     * @param <T>   Array content Type
     * @return      Extracted last Item
     */
    public static <T> T getLastItem(T[] list) {
        return (list.length == 0) ? null : list[list.length - 1];
    }

    /**
     * Check whether the source is a test source.
     *
     * @param relativeFilePath  source path relative to the package
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

    static void populateIterableAndBuiltinFunctions(BType bType, List<SymbolInfo> symbolInfoList,
                                                    LSContext context) {
        if (iterableType(bType)) {
            SymbolInfo itrForEach = getIterableOpSymbolInfo(Snippet.ITR_FOREACH.get(), bType,
                    ItemResolverConstants.ITR_FOREACH_LABEL, context);
            SymbolInfo itrMap = getIterableOpSymbolInfo(Snippet.ITR_MAP.get(), bType,
                    ItemResolverConstants.ITR_MAP_LABEL, context);
            SymbolInfo itrFilter = getIterableOpSymbolInfo(Snippet.ITR_FILTER.get(), bType,
                    ItemResolverConstants.ITR_FILTER_LABEL, context);
            SymbolInfo itrCount = getIterableOpSymbolInfo(Snippet.ITR_COUNT.get(), bType,
                    ItemResolverConstants.ITR_COUNT_LABEL, context);
            symbolInfoList.addAll(Arrays.asList(itrForEach, itrMap, itrFilter, itrCount));
            
            if (bType.tag == TypeTags.TABLE) {
                SymbolInfo itrSelect = getIterableOpSymbolInfo(Snippet.ITR_SELECT.get(), bType,
                        ItemResolverConstants.ITR_SELECT_LABEL, context);
                symbolInfoList.add(itrSelect);
            }

            if (aggregateFunctionsAllowed(bType)) {
                SymbolInfo itrMin = getIterableOpSymbolInfo(Snippet.ITR_MIN.get(), bType,
                        ItemResolverConstants.ITR_MIN_LABEL, context);
                SymbolInfo itrMax = getIterableOpSymbolInfo(Snippet.ITR_MAX.get(), bType,
                        ItemResolverConstants.ITR_MAX_LABEL, context);
                SymbolInfo itrAvg = getIterableOpSymbolInfo(Snippet.ITR_AVERAGE.get(), bType,
                        ItemResolverConstants.ITR_AVERAGE_LABEL, context);
                SymbolInfo itrSum = getIterableOpSymbolInfo(Snippet.ITR_SUM.get(), bType,
                        ItemResolverConstants.ITR_SUM_LABEL, context);
                symbolInfoList.addAll(Arrays.asList(itrMin, itrMax, itrAvg, itrSum));
            }

            // TODO: Add support for Table and Tuple collection
        }

        if (builtinLengthFunctionAllowed(bType)) {
            // For the iterable types, add the length builtin function
            SymbolInfo lengthSymbolInfo = getIterableOpSymbolInfo(Snippet.BUILTIN_LENGTH.get(), bType,
                    ItemResolverConstants.BUILTIN_LENGTH_LABEL, context);
            symbolInfoList.add(lengthSymbolInfo);
        }

        if (builtinFreezeFunctionAllowed(context, bType)) {
            // For the any data value type, add the freeze, isFrozen builtin function
            SymbolInfo freeze = getIterableOpSymbolInfo(Snippet.BUILTIN_FREEZE.get(), bType,
                                                        ItemResolverConstants.BUILTIN_FREEZE_LABEL, context);
            SymbolInfo isFrozen = getIterableOpSymbolInfo(Snippet.BUILTIN_IS_FROZEN.get(), bType,
                                                          ItemResolverConstants.BUILTIN_IS_FROZEN_LABEL, context);
            symbolInfoList.addAll(Arrays.asList(freeze, isFrozen));
        }

        if (isAnyData(context, bType)) {
            // For the any data value type, add the stamp,clone,create builtin functions
            SymbolInfo stamp = getIterableOpSymbolInfo(Snippet.BUILTIN_STAMP.get(), bType,
                                                       ItemResolverConstants.BUILTIN_STAMP_LABEL, context);
            SymbolInfo clone = getIterableOpSymbolInfo(Snippet.BUILTIN_CLONE.get(), bType,
                                                       ItemResolverConstants.BUILTIN_CLONE_LABEL, context);
            SymbolInfo convert = getIterableOpSymbolInfo(Snippet.BUILTIN_CONVERT.get(), bType,
                                                        ItemResolverConstants.BUILTIN_CONVERT_LABEL, context);
            symbolInfoList.addAll(Arrays.asList(stamp, clone, convert));
        }

        // Populate the Builtin Functions
        if (bType.tag == TypeTags.FLOAT) {
            SymbolInfo isNaN = getIterableOpSymbolInfo(Snippet.BUILTIN_IS_NAN.get(), bType,
                    ItemResolverConstants.BUILTIN_IS_NAN_LABEL, context);
            SymbolInfo isFinite = getIterableOpSymbolInfo(Snippet.BUILTIN_IS_FINITE.get(), bType,
                    ItemResolverConstants.BUILTIN_IS_FINITE_LABEL, context);
            SymbolInfo isInfinite = getIterableOpSymbolInfo(Snippet.BUILTIN_IS_INFINITE.get(), bType,
                    ItemResolverConstants.BUILTIN_IS_INFINITE_LABEL, context);
            symbolInfoList.addAll(Arrays.asList(isNaN, isFinite, isInfinite));
        }
        
        if (bType.tag == TypeTags.ERROR) {
            SymbolInfo detail = getIterableOpSymbolInfo(Snippet.BUILTIN_DETAIL.get(), bType,
                    ItemResolverConstants.BUILTIN_DETAIL_LABEL, context);
            SymbolInfo reason = getIterableOpSymbolInfo(Snippet.BUILTIN_REASON.get(), bType,
                    ItemResolverConstants.BUILTIN_REASON_LABEL, context);
            symbolInfoList.addAll(Arrays.asList(detail, reason));
        }
        
        if (bType.tag == TypeTags.MAP) {
            SymbolInfo hasKey = getIterableOpSymbolInfo(Snippet.BUILTIN_HAS_KEY.get(), bType,
                    ItemResolverConstants.BUILTIN_HASKEY_LABEL, context);
            SymbolInfo remove = getIterableOpSymbolInfo(Snippet.BUILTIN_REMOVE.get(), bType,
                    ItemResolverConstants.BUILTIN_REMOVE_LABEL, context);
            SymbolInfo clear = getIterableOpSymbolInfo(Snippet.BUILTIN_CLEAR.get(), bType,
                    ItemResolverConstants.BUILTIN_CLEAR_LABEL, context);
            SymbolInfo keys = getIterableOpSymbolInfo(Snippet.BUILTIN_KEYS.get(), bType,
                    ItemResolverConstants.BUILTIN_KEYS_LABEL, context);
            SymbolInfo values = getIterableOpSymbolInfo(Snippet.BUILTIN_VALUES.get(), bType,
                    ItemResolverConstants.BUILTIN_GET_VALUES_LABEL, context);
            symbolInfoList.addAll(Arrays.asList(hasKey, remove, clear, keys, values));
        }
        
        
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
     * Get the current file's imports.
     *
     * @param pkg               BLangPackage to extract content from
     * @param ctx               LS Operation Context
     * @return {@link List}     List of imports in the current file
     */
    public static List<BLangImportPackage> getCurrentFileImports(BLangPackage pkg, LSContext ctx) {
        String currentFile = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        return getCurrentFileTopLevelNodes(pkg, ctx).stream()
                .filter(topLevelNode -> topLevelNode instanceof BLangImportPackage)
                .map(topLevelNode -> (BLangImportPackage) topLevelNode)
                .filter(bLangImportPackage ->
                        bLangImportPackage.pos.getSource().cUnitName.replace("/", FILE_SEPARATOR).equals(currentFile)
                        && !(bLangImportPackage.getOrgName().getValue().equals("ballerina")
                        && getPackageNameComponentsCombined(bLangImportPackage).equals("transaction")))
                .collect(Collectors.toList());
    }

    public static boolean isInvalidSymbol(BSymbol symbol) {

        return ("_".equals(symbol.name.getValue())
                || "runtime".equals(symbol.getName().getValue())
                || "transactions".equals(symbol.getName().getValue())
                || symbol instanceof BAnnotationSymbol
                || symbol instanceof BOperatorSymbol
                || symbolContainsInvalidChars(symbol));
    }

    /**
     * Check whether the given node is a worker derivative node.
     *
     * @param node              Node to be evaluated
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
     * @param pkgNode           Current Package node
     * @param ctx               Service Operation context
     * @return {@link List}     List of Top Level Nodes
     */
    public static List<TopLevelNode> getCurrentFileTopLevelNodes(BLangPackage pkgNode, LSContext ctx) {
        String relativeFilePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangCompilationUnit filteredCUnit = pkgNode.compUnits.stream()
                .filter(cUnit ->
                        cUnit.getPosition().getSource().cUnitName.replace("/", FILE_SEPARATOR).equals(relativeFilePath))
                .findAny().orElse(null);
        List<TopLevelNode> topLevelNodes = filteredCUnit == null
                ? new ArrayList<>()
                : new ArrayList<>(filteredCUnit.getTopLevelNodes());

        // Filter out the lambda functions from the top level nodes
        return topLevelNodes.stream()
                .filter(topLevelNode -> !(topLevelNode instanceof BLangFunction
                        && ((BLangFunction) topLevelNode).flagSet.contains(Flag.LAMBDA))
                        && !(topLevelNode instanceof BLangSimpleVariable
                        && ((BLangSimpleVariable) topLevelNode).flagSet.contains(Flag.SERVICE)))
                .collect(Collectors.toList());
    }

    /**
     * Get the package name components combined.
     * 
     * @param importPackage     BLangImportPackage node
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
     * @param snippet           Snippet string to alter
     * @return {@link String}   Converted Snippet
     */
    public static String getPlainTextSnippet(String snippet) {
        return snippet
                .replaceAll("(\\$\\{\\d:)([a-zA-Z]*:*[a-zA-Z]*)(\\})", "$2")
                .replaceAll("(\\$\\{\\d\\})", "");
    }

    public static BallerinaParser prepareParser(String content, boolean removeErrorListener) {
        ANTLRInputStream inputStream = new ANTLRInputStream(content);
        BallerinaLexer lexer = new BallerinaLexer(inputStream);
        if (removeErrorListener) {
            lexer.removeErrorListeners();
        }
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);

        return new BallerinaParser(commonTokenStream);
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
            for (BField requiredField : requiredFields) {
                insertText.append(String.join("", Collections.nCopies(tabCount + 1, "\t")))
                        .append(getRecordFieldCompletionInsertText(requiredField, tabCount))
                        .append(String.join("", Collections.nCopies(tabCount, "\t")))
                        .append(LINE_SEPARATOR);
                tabCount++;
            }
            insertText.append(String.join("", Collections.nCopies(tabOffset, "\t")))
                    .append("}").append(LINE_SEPARATOR);
        } else if (fieldType instanceof BArrayType) {
            insertText.append("[").append("${1}").append("]");
        } else if (fieldType.tsymbol != null && fieldType.tsymbol.name.getValue().equals("string")) {
            insertText.append("\"").append("${1}").append("\"");
        } else {
            insertText.append("${1:").append(getDefaultValueForType(bField.getType())).append("}");
            if (bField.getType() instanceof BFiniteType || bField.getType() instanceof BUnionType) {
                insertText.append(getFiniteAndUnionTypesComment(bField.getType()));
            }
        }

        return insertText.toString();
    }

    private static SymbolInfo getIterableOpSymbolInfo(SnippetBlock operation, @Nullable BType bType, String label,
                                                      LSContext context) {
        String signature;
        SymbolInfo.CustomOperationSignature customOpSignature;
        SymbolInfo iterableOperation = new SymbolInfo();
        switch (operation.getLabel()) {
            case ItemResolverConstants.ITR_FOREACH_LABEL: {
                String params = getIterableOpLambdaParam(bType, context);
                signature = operation.getString()
                        .replace(CommonKeys.ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN, params);
                break;
            }
            case ItemResolverConstants.ITR_MAP_LABEL: {
                String params = getIterableOpLambdaParam(bType, context);
                signature = operation.getString()
                        .replace(CommonKeys.ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN, params);
                break;
            }
            case ItemResolverConstants.ITR_FILTER_LABEL: {
                String params = getIterableOpLambdaParam(bType, context);
                signature = operation.getString()
                        .replace(CommonKeys.ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN, params);
                break;
            }
            default: {
                signature = operation.getString();
                break;
            }
        }

        customOpSignature = new SymbolInfo.CustomOperationSignature(label, signature);
        iterableOperation.setCustomOperation(true);
        iterableOperation.setCustomOperationSignature(customOpSignature);
        return iterableOperation;
    }

    private static String getIterableOpLambdaParam(BType bType, LSContext context) {
        String params = "";
        PackageID currentPkgId = context.get(DocumentServiceKeys.CURRENT_PACKAGE_ID_KEY);
        if (bType instanceof BMapType) {
            BMapType bMapType = (BMapType) bType;
            String valueType = FunctionGenerator.generateTypeDefinition(null, currentPkgId, bMapType.constraint);
            params = Snippet.ITR_ON_MAP_PARAMS.get().getString()
                    .replace(CommonKeys.ITR_OP_LAMBDA_KEY_REPLACE_TOKEN, "string")
                    .replace(CommonKeys.ITR_OP_LAMBDA_VALUE_REPLACE_TOKEN, valueType);
        } else if (bType instanceof BArrayType) {
            BArrayType bArrayType = (BArrayType) bType;
            String valueType = FunctionGenerator.generateTypeDefinition(null, currentPkgId, bArrayType.eType);
            params = valueType + " value";
        } else if (bType instanceof BJSONType) {
            params = Snippet.ITR_ON_JSON_PARAMS.get().getString();
        } else if (bType instanceof BXMLType) {
            params = Snippet.ITR_ON_XML_PARAMS.get().getString();
        }

        return params;
    }

    private static boolean iterableType(BType bType) {
        switch (bType.tag) {
            case TypeTags.ARRAY:
            case TypeTags.MAP:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.TABLE:
            case TypeTags.INTERMEDIATE_COLLECTION:
                return true;
        }
        return false;
    }

    private static boolean aggregateFunctionsAllowed(BType bType) {
        return bType instanceof BArrayType && (((BArrayType) bType).eType.toString().equals("int")
                || ((BArrayType) bType).eType.toString().equals("float"));
    }

    private static boolean symbolContainsInvalidChars(BSymbol bSymbol) {
        List<String> symbolNameComponents = Arrays.asList(bSymbol.getName().getValue().split("\\."));
        String symbolName = CommonUtil.getLastItem(symbolNameComponents);

        return symbolName.contains(CommonKeys.LT_SYMBOL_KEY)
                || symbolName.contains(CommonKeys.GT_SYMBOL_KEY)
                || symbolName.contains(CommonKeys.DOLLAR_SYMBOL_KEY)
                || symbolName.equals("main")
                || symbolName.endsWith(".new")
                || symbolName.startsWith("0");
    }

    private static boolean builtinLengthFunctionAllowed(BType bType) {
        switch (bType.tag) {
            case TypeTags.ARRAY:
            case TypeTags.MAP:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.TABLE:
            case TypeTags.TUPLE:
            case TypeTags.RECORD:
                return true;
        }
        return false;
    }

    private static boolean builtinFreezeFunctionAllowed(LSContext context, BType bType) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        if (compilerContext != null) {
            Types types = Types.getInstance(compilerContext);
            return types.isLikeAnydataOrNotNil(bType);
        }
        return false;
    }

    private static boolean isAnyData(LSContext context, BType bType) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        if (compilerContext != null) {
            Types types = Types.getInstance(compilerContext);
            return types.isAnydata(bType);
        }
        return false;
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
                !(typePkgId.orgName.value.equals("ballerina") && typePkgId.name.value.equals("builtin"))) {
            pkgPrefix = typePkgId.name.value + ":";
            if (importsAcceptor != null) {
                importsAcceptor.accept(typePkgId.orgName.value, typePkgId.name.value);
            }
        }
        return pkgPrefix;
    }

    private static String getFiniteAndUnionTypesComment(BType bType) {
        if (bType instanceof BFiniteType) {
            List<BLangExpression> valueSpace = new ArrayList<>(((BFiniteType) bType).valueSpace);
            return " // Values allowed: " + valueSpace.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("|"));
        } else if (bType instanceof BUnionType) {
            List<BType> memberTypes = new ArrayList<>(((BUnionType) bType).getMemberTypes());
            return " // Values allowed: " + memberTypes.stream()
                    .map(BType::toString)
                    .collect(Collectors.joining("|"));
        }

        return "";
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
}

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
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.LSGlobalContextKeys;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.command.testgen.TestGenerator.TestFunctionGenerator;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.CompletionKeys;
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
import org.ballerinalang.model.types.FiniteType;
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
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntermediateCollectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
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
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;
import static org.ballerinalang.util.BLangConstants.CONSTRUCTOR_FUNCTION_SUFFIX;

/**
 * Common utils to be reuse in language server implementation.
 */
public class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static final String LINE_SEPARATOR = System.lineSeparator();

    public static final String FILE_SEPARATOR = File.separator;

    public static final String LINE_SEPARATOR_SPLIT = "\\r?\\n";

    public static final boolean LS_DEBUG_ENABLED;

    public static final String BALLERINA_HOME;

    static {
        String debugLogStr = System.getProperty("ballerina.debugLog");
        LS_DEBUG_ENABLED = debugLogStr != null && Boolean.parseBoolean(debugLogStr);
        BALLERINA_HOME = System.getProperty("ballerina.home");
    }

    private CommonUtil() {
    }

    /**
     * Get the package URI to the given package name.
     *
     * @param pkgName        Name of the package that need the URI for
     * @param currentPkgPath String URI of the current package
     * @param currentPkgName Name of the current package
     * @return String URI for the given path.
     */
    public static String getPackageURI(String pkgName, String currentPkgPath, String currentPkgName) {
        String newPackagePath;
        // If current package path is not null and current package is not default package continue,
        // else new package path is same as the current package path.
        if (currentPkgPath != null && !currentPkgName.equals(".")) {
            int indexOfCurrentPkgName = currentPkgPath.lastIndexOf(currentPkgName);
            if (indexOfCurrentPkgName >= 0) {
                newPackagePath = currentPkgPath.substring(0, indexOfCurrentPkgName);
            } else {
                newPackagePath = currentPkgPath;
            }

            if (pkgName.equals(".")) {
                newPackagePath = Paths.get(newPackagePath).toString();
            } else {
                newPackagePath = Paths.get(newPackagePath, pkgName).toString();
            }
        } else {
            newPackagePath = currentPkgPath;
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
     * @param tokenStream Token Stream
     * @param startIndex  Start token index
     * @return {@link Token}    Previous default token
     */
    public static Token getPreviousDefaultToken(TokenStream tokenStream, int startIndex) {
        return getDefaultTokenToLeftOrRight(tokenStream, startIndex, -1);
    }

    /**
     * Get the next default token from the given start index.
     *
     * @param tokenStream Token Stream
     * @param startIndex  Start token index
     * @return {@link Token}    Previous default token
     */
    public static Token getNextDefaultToken(TokenStream tokenStream, int startIndex) {
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
        Token t;
        while (n > 0) {
            t = getDefaultTokenToLeftOrRight(tokenStream, startIndex, -1);
            if (t == null) {
                return new ArrayList<>();
            }
            tokens.add(t);
            n--;
            startIndex = t.getTokenIndex();
        }

        return Lists.reverse(tokens);
    }

    /**
     * Get the Nth Default token to the left of current token index.
     *
     * @param tokenStream Token Stream to traverse
     * @param startIndex  Start position of the token stream
     * @param offset      Number of tokens to traverse left
     * @return {@link Token}    Nth Token
     */
    public static Token getNthDefaultTokensToLeft(TokenStream tokenStream, int startIndex, int offset) {
        Token token = null;
        int indexCounter = startIndex;
        for (int i = 0; i < offset; i++) {
            token = getPreviousDefaultToken(tokenStream, indexCounter);
            indexCounter = token.getTokenIndex();
        }

        return token;
    }

    /**
     * Get the current token index from the token stream.
     *
     * @param context LSServiceOperationContext
     * @return {@link Integer}      token index
     */
    public static int getCurrentTokenFromTokenStream(LSContext context) {
        TokenStream tokenStream = context.get(CompletionKeys.TOKEN_STREAM_KEY);
        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        Token lastToken = null;
        int line = position.getLine();
        int col = position.getCharacter();
        int tokenLine;
        int tokenCol;
        int index = 0;

        if (tokenStream == null) {
            return -1;
        }

        while (true) {
            Token token = tokenStream.get(index);
            tokenLine = token.getLine() - 1;
            tokenCol = token.getCharPositionInLine();
            if (tokenLine > line || (tokenLine == line && tokenCol >= col)) {
                break;
            }
            index++;
            lastToken = token;
        }

        return lastToken == null ? -1 : lastToken.getTokenIndex();
    }

    /**
     * Pop n number of Elements from the stack and return as a List.
     * <p>
     * Note: If n is greater than stack, then all the elements of list will be returned
     *
     * @param itemList Item Stack to pop elements from
     * @param n         number of elements to pop
     * @param <T>       Type of the Elements
     * @return {@link List}     List of popped Items
     */
    public static <T> List<T> popNFromList(List<T> itemList, int n) {
        List<T> poppedList = new ArrayList<>(itemList);
        if (n > poppedList.size()) {
            return poppedList;
        }

        return itemList.subList(itemList.size() - n, itemList.size());
    }

    private static Token getDefaultTokenToLeftOrRight(TokenStream tokenStream, int startIndex, int direction) {
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
        return token;
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
        List<String> topLevelKeywords = Arrays.asList("function", "service", "resource", "endpoint", "object",
                                                      "record");
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
                while (iterator.hasNext()) {
                    String topLevelKeyword = iterator.next();
                    if (topLevelKeywords.contains(topLevelKeyword) &&
                            (!iterator.hasNext() || !CONSTRUCTOR_FUNCTION_SUFFIX.equals(iterator.next()))) {
                        return topLevelKeyword;
                    }
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
     * @param packageID        Package Id
     * @param annotationSymbol BLang annotation to extract the completion Item
     * @param ctx                       LS Service operation context, in this case completion context
     * @return {@link CompletionItem}   Completion item for the annotation
     */
    public static CompletionItem getAnnotationCompletionItem(PackageID packageID, BAnnotationSymbol annotationSymbol,
                                                             LSContext ctx) {
        String label = getAnnotationLabel(packageID, annotationSymbol);
        String insertText = getAnnotationInsertText(packageID, annotationSymbol);
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
                .filter(bLangImportPackage -> bLangImportPackage.symbol.pkgID.equals(packageID))
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
        if (UtilSymbolKeys.BALLERINA_KW.equals(orgName) && UtilSymbolKeys.BUILTIN_KW.equals(pkgName)) {
            return null;
        }
        String relativePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangPackage pkg = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativePath, pkg);
        List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(srcOwnerPkg, ctx);
        Position start = new Position(0, 0);
        if (!imports.isEmpty()) {
            BLangImportPackage last = CommonUtil.getLastItem(imports);
            int endLine = last.getPosition().getEndLine();
            start = new Position(endLine, 0);
        }

        String importStatement = ItemResolverConstants.IMPORT + " "
                + orgName + UtilSymbolKeys.SLASH_KEYWORD_KEY + pkgName + UtilSymbolKeys.SEMI_COLON_SYMBOL_KEY
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
     * Get the annotation Insert text.
     *
     * @param packageID        Package ID
     * @param annotationSymbol Annotation to get the insert text
     * @return {@link String}   Insert text
     */
    private static String getAnnotationInsertText(PackageID packageID, BAnnotationSymbol annotationSymbol) {
        String pkgAlias = CommonUtil.getLastItem(packageID.getNameComps()).getValue();
        StringBuilder annotationStart = new StringBuilder();
        if (!packageID.getName().getValue().equals(Names.BUILTIN_PACKAGE.getValue())) {
            annotationStart.append(pkgAlias).append(UtilSymbolKeys.PKG_DELIMITER_KEYWORD);
        }
        if (annotationSymbol.attachedType != null) {
            annotationStart.append(annotationSymbol.getName().getValue()).append(" ")
                    .append(UtilSymbolKeys.OPEN_BRACE_KEY).append(LINE_SEPARATOR)
                    .append("\t").append("${1}").append(LINE_SEPARATOR)
                    .append(UtilSymbolKeys.CLOSE_BRACE_KEY);
        } else {
            annotationStart.append(annotationSymbol.getName().getValue());
        }

        return annotationStart.toString();
    }

    /**
     * Get the completion Label for the annotation.
     *
     * @param packageID  Package ID
     * @param annotation BLang annotation
     * @return {@link String}          Label string
     */
    private static String getAnnotationLabel(PackageID packageID, BAnnotationSymbol annotation) {
        String pkgComponent = "";
        if (!packageID.getName().getValue().equals(Names.BUILTIN_PACKAGE.getValue())) {
            pkgComponent = CommonUtil.getLastItem(packageID.getNameComps()).getValue()
                    + UtilSymbolKeys.PKG_DELIMITER_KEYWORD;
        }

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
                typeString = "{}";
                break;
            case FINITE:
                List<String> types = new ArrayList<>();
                ((FiniteType) bType).getValueSpace().forEach(typeEntry -> types.add(typeEntry.toString()));
                types.sort(Comparator.naturalOrder());
                typeString = String.join("|", types);
                break;
            case UNION:
                String[] typeNameComps = bType.toString().split(UtilSymbolKeys.PKG_DELIMITER_KEYWORD);
                typeString = typeNameComps[typeNameComps.length - 1];
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
     * Given an Object type, extract the non-remote functions.
     *
     * @param objectTypeSymbol  Object Symbol
     * @return {@link Map}      Map of filtered scope entries
     */
    public static Map<Name, Scope.ScopeEntry> getObjectFunctions(BObjectTypeSymbol objectTypeSymbol) {
        return objectTypeSymbol.methodScope.entries.entrySet().stream()
                .filter(entry -> (entry.getValue().symbol.flags & Flags.REMOTE) != Flags.REMOTE)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
     * @param structFields List of struct fields
     * @return {@link List}     List of completion items for the struct fields
     */
    public static List<CompletionItem> getStructFieldCompletionItems(List<BField> structFields) {
        List<CompletionItem> completionItems = new ArrayList<>();
        structFields.forEach(bStructField -> {
            StringBuilder insertText = new StringBuilder(bStructField.getName().getValue() + ": ");
            if (bStructField.getType() instanceof BStructureType) {
                insertText.append("{").append(LINE_SEPARATOR).append("\t${1}").append(LINE_SEPARATOR).append("}");
            } else {
                insertText.append("${1:").append(getDefaultValueForType(bStructField.getType())).append("}");
            }
            CompletionItem fieldItem = new CompletionItem();
            fieldItem.setInsertText(insertText.toString());
            fieldItem.setInsertTextFormat(InsertTextFormat.Snippet);
            fieldItem.setLabel(bStructField.getName().getValue());
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

        fields.forEach(bStructField -> {
            String defaultFieldEntry = bStructField.getName().getValue()
                    + UtilSymbolKeys.PKG_DELIMITER_KEYWORD + " " + getDefaultValueForType(bStructField.getType());
            fieldEntries.add(defaultFieldEntry);
        });

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
        PackageID pkgId = bType.tsymbol.pkgID;
        PackageID currentPkgId = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY).packageID;
        String[] nameComponents = bType.toString().split(":");
        if (pkgId.toString().equals(currentPkgId.toString()) || pkgId.getName().getValue().equals("builtin")) {
            return nameComponents[nameComponents.length - 1];
        } else {
            return pkgId.getName().getValue() + UtilSymbolKeys.PKG_DELIMITER_KEYWORD
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
        return list.get(list.size() - 1);
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

    /**
     * Get the string values list of forced consumed tokens, from the LSContext.
     *
     * @param ctx               Language Server context
     * @return {@link List}     Token string list
     */
    public static List<String> getPoppedTokenStrings(LSContext ctx) {
        return ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY).stream()
                .map(Token::getText)
                .collect(Collectors.toList());
    }

    static void populateIterableAndBuiltinFunctions(SymbolInfo variable, List<SymbolInfo> symbolInfoList,
                                                    LSContext context) {
        BType bType = variable.getScopeEntry().symbol.getType();
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
                || symbol instanceof BServiceSymbol
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

    public static boolean isWithinWorkerDeclaration(BLangNode bLangNode) {
        return bLangNode instanceof BLangBlockStmt && bLangNode.parent instanceof BLangLambdaFunction
                && ((BLangLambdaFunction) bLangNode.parent).function.flagSet.contains(Flag.WORKER);
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
        return String.join(".", importPackage.pkgNameComps.stream()
                .map(id -> id.value)
                .collect(Collectors.toList()));
    }

    private static SymbolInfo getIterableOpSymbolInfo(SnippetBlock operation, @Nullable BType bType, String label,
                                                      LSContext context) {
        boolean isSnippet = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem().getSnippetSupport();
        String signature = "";
        SymbolInfo.CustomOperationSignature customOpSignature;
        SymbolInfo iterableOperation = new SymbolInfo();
        switch (operation.getLabel()) {
            case ItemResolverConstants.ITR_FOREACH_LABEL: {
                String params = getIterableOpLambdaParam(bType, context);
                signature = operation.getString(isSnippet)
                        .replace(UtilSymbolKeys.ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN, params);
                break;
            }
            case ItemResolverConstants.ITR_MAP_LABEL: {
                String params = getIterableOpLambdaParam(bType, context);
                signature = operation.getString(isSnippet)
                        .replace(UtilSymbolKeys.ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN, params);
                break;
            }
            case ItemResolverConstants.ITR_FILTER_LABEL: {
                String params = getIterableOpLambdaParam(bType, context);
                signature = operation.getString(isSnippet)
                        .replace(UtilSymbolKeys.ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN, params);
                break;
            }
            default: {
                signature = operation.getString(isSnippet);
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
        boolean isSnippet = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem().getSnippetSupport();
        PackageID currentPkgId = context.get(DocumentServiceKeys.CURRENT_PACKAGE_ID_KEY);
        if (bType instanceof BMapType) {
            BMapType bMapType = (BMapType) bType;
            String valueType = FunctionGenerator.generateTypeDefinition(null, currentPkgId, bMapType.constraint);
            params = Snippet.ITR_ON_MAP_PARAMS.get().getString(isSnippet)
                    .replace(UtilSymbolKeys.ITR_OP_LAMBDA_KEY_REPLACE_TOKEN, "string")
                    .replace(UtilSymbolKeys.ITR_OP_LAMBDA_VALUE_REPLACE_TOKEN, valueType);
        } else if (bType instanceof BArrayType) {
            BArrayType bArrayType = (BArrayType) bType;
            String valueType = FunctionGenerator.generateTypeDefinition(null, currentPkgId, bArrayType.eType);
            params = valueType + " value";
        } else if (bType instanceof BJSONType) {
            params = Snippet.ITR_ON_JSON_PARAMS.get().getString(isSnippet);
        } else if (bType instanceof BXMLType) {
            params = Snippet.ITR_ON_XML_PARAMS.get().getString(isSnippet);
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
        return bSymbol.getName().getValue().contains(UtilSymbolKeys.LT_SYMBOL_KEY)
                || bSymbol.getName().getValue().contains(UtilSymbolKeys.GT_SYMBOL_KEY)
                || bSymbol.getName().getValue().contains(UtilSymbolKeys.DOLLAR_SYMBOL_KEY)
                || bSymbol.getName().getValue().equals("main")
                || bSymbol.getName().getValue().endsWith(".new");
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

    /**
     * Inner class for generating function code.
     */
    public static class FunctionGenerator {

        /**
         * Generate function code.
         *
         * @param name               function name
         * @param args               Function arguments
         * @param returnType         return type
         * @param returnDefaultValue default return value
         * @return {@link String}       generated function signature
         */
        public static String createFunction(String name, String args, String returnType, String returnDefaultValue) {
            String funcBody = CommonUtil.LINE_SEPARATOR;
            String funcReturnSignature = "";
            if (returnType != null) {
                funcBody = returnDefaultValue + funcBody;
                funcReturnSignature = " returns " + returnType + " ";
            }
            return CommonUtil.LINE_SEPARATOR + CommonUtil.LINE_SEPARATOR + "function " + name + "(" + args + ")"
                    + funcReturnSignature + "{" + CommonUtil.LINE_SEPARATOR + funcBody + "}"
                    + CommonUtil.LINE_SEPARATOR;
        }

        /**
         * Generate function call.
         *
         * @param name               function name
         * @param args               Function arguments
         * @param returnType         return type
         * @param returnDefaultValue default return value
         * @return {@link String}       generated function signature
         */
        public static String createFunctionCall(String name, String args, String returnType,
                                                String returnDefaultValue) {
            String funcBody = CommonUtil.LINE_SEPARATOR;
            String funcReturnSignature = "";
            if (returnType != null) {
                funcBody = returnDefaultValue + funcBody;
                funcReturnSignature = " returns " + returnType + " ";
            }
            return CommonUtil.LINE_SEPARATOR + CommonUtil.LINE_SEPARATOR + "function " + name + "(" + args + ")"
                    + funcReturnSignature + "{" + CommonUtil.LINE_SEPARATOR + funcBody + "}"
                    + CommonUtil.LINE_SEPARATOR;
        }

        /**
         * Get the default function return statement.
         *
         * @param importsAcceptor imports acceptor
         * @param currentPkgId    current package id
         * @param bLangNode       BLangNode to evaluate
         * @param template        return statement to modify
         * @return {@link String}   Default return statement
         */
        public static String generateReturnValue(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                                 BLangNode bLangNode, String template) {
            if (bLangNode.type == null && bLangNode instanceof BLangTupleDestructure) {
                // Check for tuple assignment eg. (int, int)
                List<String> list = new ArrayList<>();
                for (BLangExpression bLangExpression : ((BLangTupleDestructure) bLangNode).varRef.expressions) {
                    if (bLangExpression.type != null) {
                        list.add(generateReturnValue(importsAcceptor, currentPkgId, bLangExpression.type, "{%1}"));
                    }
                }
                return template.replace("{%1}", "(" + String.join(", ", list) + ")");
            } else if (bLangNode instanceof BLangLiteral) {
                return template.replace("{%1}", ((BLangLiteral) bLangNode).getValue().toString());
            } else if (bLangNode instanceof BLangAssignment) {
                return template.replace("{%1}", "0");
            }
            return (bLangNode.type != null)
                    ? generateReturnValue(importsAcceptor, currentPkgId, bLangNode.type, template)
                    : null;
        }

        private static String generateReturnValue(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                                  BType bType,
                                                  String template) {
            if (bType.tsymbol == null && bType instanceof BArrayType) {
                return template.replace("{%1}", "[" +
                        generateReturnValue(((BArrayType) bType).eType.tsymbol, "") + "]");
            } else if (bType instanceof BFiniteType) {
                // Check for finite set assignment
                BFiniteType bFiniteType = (BFiniteType) bType;
                Set<BLangExpression> valueSpace = bFiniteType.valueSpace;
                if (!valueSpace.isEmpty()) {
                    return generateReturnValue(importsAcceptor, currentPkgId, valueSpace.stream().findFirst().get(),
                                               template);
                }
            } else if (bType instanceof BMapType && ((BMapType) bType).constraint != null) {
                // Check for constrained map assignment eg. map<Student>
                BType constraintType = ((BMapType) bType).constraint;
                String mapDef = "{key: " + generateReturnValue(importsAcceptor, currentPkgId, constraintType, "{%1}") +
                        "}";
                return template.replace("{%1}", mapDef);
            } else if (bType instanceof BUnionType) {
                BUnionType bUnionType = (BUnionType) bType;
                Set<BType> memberTypes = bUnionType.memberTypes;
                if (memberTypes.size() == 2 && memberTypes.stream().anyMatch(bType1 -> bType1 instanceof BNilType)) {
                    Optional<BType> type = memberTypes.stream()
                            .filter(bType1 -> !(bType1 instanceof BNilType)).findFirst();
                    if (type.isPresent()) {
                        return generateReturnValue(importsAcceptor, currentPkgId, type.get(), "{%1}?");
                    }
                }
                if (!memberTypes.isEmpty()) {
                    BType firstBType = memberTypes.stream().findFirst().get();
                    return generateReturnValue(importsAcceptor, currentPkgId, firstBType, template);
                }
            } else if (bType instanceof BTupleType) {
                BTupleType bTupleType = (BTupleType) bType;
                List<BType> tupleTypes = bTupleType.tupleTypes;
                List<String> list = new ArrayList<>();
                for (BType type : tupleTypes) {
                    list.add(generateReturnValue(importsAcceptor, currentPkgId, type, "{%1}"));
                }
                return template.replace("{%1}", "(" + String.join(", ", list) + ")");
            } else if (bType instanceof BObjectType && ((BObjectType) bType).tsymbol instanceof BObjectTypeSymbol) {
                BObjectTypeSymbol bStruct = (BObjectTypeSymbol) ((BObjectType) bType).tsymbol;
                List<String> list = new ArrayList<>();
                for (BVarSymbol param : bStruct.initializerFunc.symbol.params) {
                    list.add(generateReturnValue(param.type.tsymbol, "{%1}"));
                }
                String pkgPrefix = getPackagePrefix(importsAcceptor, currentPkgId, bStruct.pkgID);
                String paramsStr = String.join(", ", list);
                String newObjStr = "new " + pkgPrefix + bStruct.name.getValue() + "(" + paramsStr + ")";
                return template.replace("{%1}", newObjStr);
            }
            return (bType.tsymbol != null) ? generateReturnValue(bType.tsymbol, template) :
                    template.replace("{%1}", "()");
        }

        private static String generateReturnValue(BTypeSymbol tSymbol, String template) {
            String result;
            switch (tSymbol.name.getValue()) {
                case "int":
                case "any":
                    result = "0";
                    break;
                case "string":
                    result = "\"\"";
                    break;
                case "float":
                    result = "0.0";
                    break;
                case "json":
                    result = "{}";
                    break;
                case "map":
                    result = "<map>{}";
                    break;
                case "boolean":
                    result = "false";
                    break;
                case "xml":
                    result = "xml ` `";
                    break;
                case "byte":
                    result = "0";
                    break;
                default:
                    result = "()";
                    break;
            }
            return template.replace("{%1}", result);
        }

        /**
         * Returns signature of the return type.
         *
         * @param importsAcceptor imports acceptor
         * @param currentPkgId    current package id
         * @param bLangNode       {@link BLangNode}
         * @return return type signature
         */
        public static String generateTypeDefinition(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                                    BLangNode bLangNode) {
            if (bLangNode.type == null && bLangNode instanceof BLangTupleDestructure) {
                // Check for tuple assignment eg. (int, int)
                List<String> list = new ArrayList<>();
                for (BLangExpression bLangExpression : ((BLangTupleDestructure) bLangNode).varRef.expressions) {
                    if (bLangExpression.type != null) {
                        list.add(generateTypeDefinition(importsAcceptor, currentPkgId, bLangExpression.type));
                    }
                }
                return "(" + String.join(", ", list) + ")";
            } else if (bLangNode instanceof BLangAssignment) {
                if (((BLangAssignment) bLangNode).declaredWithVar) {
                    return "any";
                }
            } else if (bLangNode instanceof BLangFunctionTypeNode) {
                BLangFunctionTypeNode funcType = (BLangFunctionTypeNode) bLangNode;
                TestFunctionGenerator generator = new TestFunctionGenerator(importsAcceptor, currentPkgId, funcType);
                String[] typeSpace = generator.getTypeSpace();
                String[] nameSpace = generator.getNamesSpace();
                StringJoiner params = new StringJoiner(", ");
                IntStream.range(0, typeSpace.length - 1).forEach(index -> {
                    String type = typeSpace[index];
                    String name = nameSpace[index];
                    params.add(type + " " + name);
                });
                return "function (" + params.toString() + ") returns (" + typeSpace[typeSpace.length - 1] + ")";
            }
            return (bLangNode.type != null) ? generateTypeDefinition(importsAcceptor, currentPkgId, bLangNode.type) :
                    null;
        }

        /**
         * Returns signature of the return type.
         *
         * @param importsAcceptor imports acceptor
         * @param currentPkgId    current package id
         * @param bType           {@link BType}
         * @return return type signature
         */
        public static String generateTypeDefinition(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                                    BType bType) {
            if ((bType.tsymbol == null || bType.tsymbol.name.value.isEmpty()) && bType instanceof BArrayType) {
                // Check for array assignment eg.  int[]
                return generateTypeDefinition(importsAcceptor, currentPkgId, ((BArrayType) bType).eType.tsymbol) + "[]";
            } else if (bType instanceof BMapType && ((BMapType) bType).constraint != null) {
                // Check for constrained map assignment eg. map<Student>
                BTypeSymbol tSymbol = ((BMapType) bType).constraint.tsymbol;
                if (tSymbol != null) {
                    String constraint = generateTypeDefinition(importsAcceptor, currentPkgId, tSymbol);
                    return ("any".equals(constraint)) ? "map" : "map<" + constraint + ">";
                }
            } else if (bType instanceof BUnionType) {
                // Check for union type assignment eg. int | string
                List<String> list = new ArrayList<>();
                Set<BType> memberTypes = ((BUnionType) bType).memberTypes;
                if (memberTypes.size() == 2 && memberTypes.stream().anyMatch(bType1 -> bType1 instanceof BNilType)) {
                    Optional<BType> type = memberTypes.stream()
                            .filter(bType1 -> !(bType1 instanceof BNilType)).findFirst();
                    if (type.isPresent()) {
                        return generateTypeDefinition(importsAcceptor, currentPkgId, type.get()) + "?";
                    }
                }
                for (BType memberType : memberTypes) {
                    list.add(generateTypeDefinition(importsAcceptor, currentPkgId, memberType));
                }
                return "(" + String.join("|", list) + ")";
            } else if (bType instanceof BTupleType) {
                // Check for tuple type assignment eg. int, string
                List<String> list = new ArrayList<>();
                for (BType memberType : ((BTupleType) bType).tupleTypes) {
                    list.add(generateTypeDefinition(importsAcceptor, currentPkgId, memberType));
                }
                return "(" + String.join(", ", list) + ")";
            } else if (bType instanceof BNilType) {
                return "()";
            } else if (bType instanceof BIntermediateCollectionType) {
                // TODO: 29/11/2018 fix this. A hack to infer type definition
                // We assume;
                // 1. Tuple of <key(string), value(string)> as a map(though it can be a record as well)
                // 2. Tuple of <index(int), value(string)> as an array
                BIntermediateCollectionType collectionType = (BIntermediateCollectionType) bType;
                List<String> list = new ArrayList<>();
                List<BType> tupleTypes = collectionType.tupleType.tupleTypes;
                if (tupleTypes.size() == 2) {
                    BType leftType = tupleTypes.get(0);
                    BType rightType = tupleTypes.get(1);
                    switch (leftType.tsymbol.name.value) {
                        case "int":
                            return generateTypeDefinition(importsAcceptor, currentPkgId, rightType) + "[]";
                        case "string":
                        default:
                            return "map<" + generateTypeDefinition(importsAcceptor, currentPkgId, rightType) + ">";
                    }
                }
                for (BType memberType : tupleTypes) {
                    list.add(generateTypeDefinition(importsAcceptor, currentPkgId, memberType));
                }
                return "(" + String.join(", ", list) + ")[]";
            }
            return (bType.tsymbol != null) ? generateTypeDefinition(importsAcceptor, currentPkgId, bType.tsymbol) :
                    "any";
        }

        private static String generateTypeDefinition(BiConsumer<String, String> importsAcceptor,
                                                     PackageID currentPkgId, BTypeSymbol tSymbol) {
            if (tSymbol != null) {
                String pkgPrefix = getPackagePrefix(importsAcceptor, currentPkgId, tSymbol.pkgID);
                return pkgPrefix + tSymbol.name.getValue();
            }
            return "any";
        }

        public static List<String> getFuncArguments(BiConsumer<String, String> importsAcceptor,
                                                    PackageID currentPkgId, BLangNode parent) {
            List<String> list = new ArrayList<>();
            if (parent instanceof BLangInvocation) {
                BLangInvocation bLangInvocation = (BLangInvocation) parent;
                if (bLangInvocation.argExprs.isEmpty()) {
                    return null;
                }
                int argCounter = 1;
                Set<String> argNames = new HashSet<>();
                for (BLangExpression bLangExpression : bLangInvocation.argExprs) {
                    if (bLangExpression instanceof BLangSimpleVarRef) {
                        BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) bLangExpression;
                        String varName = simpleVarRef.variableName.value;
                        String argType = lookupVariableReturnType(importsAcceptor, currentPkgId, varName, parent);
                        list.add(argType + " " + varName);
                        argNames.add(varName);
                    } else if (bLangExpression instanceof BLangInvocation) {
                        BLangInvocation invocation = (BLangInvocation) bLangExpression;
                        String functionName = invocation.name.value;
                        String argType = lookupFunctionReturnType(functionName, parent);
                        String argName = generateName(argCounter++, argNames);
                        list.add(argType + " " + argName);
                        argNames.add(argName);
                    } else {
                        String argName = generateName(argCounter++, argNames);
                        list.add("any " + argName);
                        argNames.add(argName);
                    }
                }
            }
            return (!list.isEmpty()) ? list : null;
        }

        public static List<String> getFuncArguments(BInvokableSymbol bInvokableSymbol) {
            List<String> list = new ArrayList<>();
            if (bInvokableSymbol.type instanceof BInvokableType) {
                BInvokableType bInvokableType = (BInvokableType) bInvokableSymbol.type;
                if (bInvokableType.paramTypes.isEmpty()) {
                    return list;
                }
                int argCounter = 1;
                Set<String> argNames = new HashSet<>();
                for (BType bType : bInvokableType.getParameterTypes()) {
                    String argName = generateName(argCounter++, argNames);
                    String argType = generateTypeDefinition(null, bInvokableSymbol.pkgID, bType);
                    list.add(argType + " " + argName);
                    argNames.add(argName);
                }
            }
            return (!list.isEmpty()) ? list : new ArrayList<>();
        }

        private static String lookupVariableReturnType(BiConsumer<String, String> importsAcceptor,
                                                       PackageID currentPkgId,
                                                       String variableName, BLangNode parent) {
            if (parent instanceof BLangBlockStmt) {
                BLangBlockStmt blockStmt = (BLangBlockStmt) parent;
                Scope scope = blockStmt.scope;
                if (scope != null) {
                    for (Map.Entry<Name, Scope.ScopeEntry> entry : scope.entries.entrySet()) {
                        String key = entry.getKey().getValue();
                        BSymbol symbol = entry.getValue().symbol;
                        if (variableName.equals(key) && symbol instanceof BVarSymbol) {
                            return generateTypeDefinition(importsAcceptor, currentPkgId, symbol.type);
                        }
                    }
                }
            }
            return (parent != null && parent.parent != null)
                    ? lookupVariableReturnType(importsAcceptor, currentPkgId, variableName, parent.parent)
                    : "any";
        }

        private static String lookupFunctionReturnType(String functionName, BLangNode parent) {
            if (parent instanceof BLangPackage) {
                BLangPackage blockStmt = (BLangPackage) parent;
                List<BLangFunction> functions = blockStmt.functions;
                for (BLangFunction function : functions) {
                    if (functionName.equals(function.name.getValue())) {
                        return generateTypeDefinition(null, ((BLangPackage) parent).packageID, function.returnTypeNode);
                    }
                }
            }
            return (parent != null && parent.parent != null)
                    ? lookupFunctionReturnType(functionName, parent.parent) : "any";
        }
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

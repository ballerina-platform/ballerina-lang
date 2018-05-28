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

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.types.FiniteType;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntermediateCollectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common utils to be reuse in language server implementation.
 */
public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    private static final String OPEN_BRACKET_KEY_WORD = "(";
    
    public static final String LINE_SEPARATOR = System.lineSeparator();

    public static final String LINE_SEPARATOR_SPLIT = "\\r?\\n";

    public static final boolean LS_DEBUG_ENABLED;

    static {
        String debugLogStr = System.getProperty("ballerina.debugLog");
        LS_DEBUG_ENABLED =  debugLogStr != null && Boolean.parseBoolean(debugLogStr);
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
     * Common utility to get a Path from the given uri string.
     *
     * @param document LSDocument object of the file
     * @return {@link Path}     Path of the uri
     */
    public static Path getPath(LSDocument document) {
        Path path = null;
        try {
            path = document.getPath();
        } catch (URISyntaxException | MalformedURLException e) {
            // Do Nothing
        }

        return path;
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
     * Get the Nth Default token to the right of current token index.
     *
     * @param tokenStream Token Stream to traverse
     * @param startIndex  Start position of the token stream
     * @param offset      Number of tokens to traverse right
     * @return {@link Token}    Nth Token
     */
    public static Token getNthDefaultTokensToRight(TokenStream tokenStream, int startIndex, int offset) {
        Token token = null;
        int indexCounter = startIndex;
        for (int i = 0; i < offset; i++) {
            token = getNextDefaultToken(tokenStream, indexCounter);
            indexCounter = token.getTokenIndex();
        }

        return token;
    }

    private static Token getDefaultTokenToLeftOrRight(TokenStream tokenStream, int startIndex, int direction) {
        Token token = null;
        while (true) {
            startIndex += direction;
            if (startIndex < 0) {
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
     * Check whether the given cursor position is within the brackets.
     *
     * @param context        Text document context
     * @param terminalTokens List of terminal tokens
     * @return {@link Boolean}  Whether the cursor is within the brackets or not
     */
    public static boolean isWithinBrackets(LSServiceOperationContext context, List<String> terminalTokens) {
        int currentTokenIndex = context.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        Token previousToken = tokenStream.get(currentTokenIndex);
        Token currentToken;
        while (true) {
            if (currentTokenIndex < 0) {
                break;
            }
            currentToken = CommonUtil.getPreviousDefaultToken(tokenStream, currentTokenIndex);
            if (terminalTokens.contains(currentToken.getText())) {
                break;
            }
            previousToken = currentToken;
            currentTokenIndex = currentToken.getTokenIndex();
        }

        if (previousToken != null && previousToken.getText().equals(OPEN_BRACKET_KEY_WORD)) {
            Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
            Token closeBracket = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY)
                    .get(context.get(DocumentServiceKeys.TOKEN_INDEX_KEY));
            int cursorLine = position.getLine();
            int cursorCol = position.getCharacter();
            int startBracketLine = previousToken.getLine() - 1;
            int startBracketCol = previousToken.getCharPositionInLine();
            int closeBracketLine = closeBracket.getLine() - 1;
            int closeBracketCol = closeBracket.getCharPositionInLine();

            return (cursorLine >= startBracketLine && cursorLine < closeBracketLine)
                    || (cursorLine > startBracketLine && cursorLine <= closeBracketLine)
                    || (cursorLine == startBracketLine && cursorLine == closeBracketLine
                    && cursorCol > startBracketCol && cursorCol <= closeBracketCol);
        }

        return false;
    }

    /**
     * Get the top level node type in the line.
     *
     * @param identifier    Document Identifier
     * @param startPosition Start position
     * @param docManager    Workspace document manager
     * @return {@link String}   Top level node type
     */
    public static String topLevelNodeTypeInLine(TextDocumentIdentifier identifier, Position startPosition,
                                                WorkspaceDocumentManager docManager) {
        // TODO: Need to support service and resources as well.
        List<String> topLevelKeywords = Arrays.asList("function", "service", "resource", "endpoint", "type");
        LSDocument document = new LSDocument(identifier.getUri());
        String fileContent = docManager.getFileContent(getPath(document));
        String[] splitedFileContent = fileContent.split(LINE_SEPARATOR_SPLIT);
        if ((splitedFileContent.length - 1) >= startPosition.getLine()) {
            String lineContent = splitedFileContent[startPosition.getLine()];
            List<String> alphaNumericTokens = new ArrayList<>(Arrays.asList(lineContent.split("[^\\w']+")));

            for (String topLevelKeyword : topLevelKeywords) {
                if (alphaNumericTokens.contains(topLevelKeyword)) {
                    return topLevelKeyword;
                }
            }
        }

        return null;
    }

    /**
     * Get current package by given file name.
     *
     * @param packages list of packages to be searched
     * @param fileUri  string file URI
     * @return {@link BLangPackage} current package
     */
    public static BLangPackage getCurrentPackageByFileName(List<BLangPackage> packages, String fileUri) {
        LSDocument document = new LSDocument(fileUri);
        Path filePath = getPath(document);
        Path fileNamePath = filePath.getFileName();
        BLangPackage currentPackage = null;
        try {
            found:
            for (BLangPackage bLangPackage : packages) {
                for (BLangCompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
                    if (compilationUnit.name.equals(fileNamePath.getFileName().toString())) {
                        currentPackage = bLangPackage;
                        break found;
                    }
                }
            }
        } catch (NullPointerException e) {
            currentPackage = packages.get(0);
        }
        return currentPackage;
    }

    /**
     * Get the Annotation completion Item.
     *
     * @param packageID  Package Id
     * @param annotation BLang annotation to extract the completion Item
     * @return {@link CompletionItem}   Completion item for the annotation
     */
    public static CompletionItem getAnnotationCompletionItem(PackageID packageID, BLangAnnotation annotation) {
        String label = getAnnotationLabel(packageID, annotation);
        String insertText = getAnnotationInsertText(packageID, annotation);
        CompletionItem annotationItem = new CompletionItem();
        annotationItem.setLabel(label);
        annotationItem.setInsertText(insertText);
        annotationItem.setInsertTextFormat(InsertTextFormat.Snippet);
        annotationItem.setDetail(ItemResolverConstants.ANNOTATION_TYPE);

        return annotationItem;
    }

    /**
     * Get the annotation Insert text.
     *
     * @param packageID  Package ID
     * @param annotation Annotation to get the insert text
     * @return {@link String}   Insert text
     */
    private static String getAnnotationInsertText(PackageID packageID, BLangAnnotation annotation) {
        String pkgAlias = packageID.getNameComps().get(packageID.getNameComps().size() - 1).getValue();
        StringBuilder annotationStart = new StringBuilder();
        if (!packageID.getName().getValue().equals(Names.BUILTIN_PACKAGE.getValue())) {
            annotationStart.append(pkgAlias).append(UtilSymbolKeys.PKG_DELIMITER_KEYWORD);
        }
        annotationStart.append(annotation.getName().getValue()).append(" ").append(UtilSymbolKeys.OPEN_BRACE_KEY);

        // Note: Code has been commented on purpose since the implementation can be revert back
//        if (annotation.typeNode.type instanceof BStructType) {
//            ((BStructType) annotation.typeNode.type).fields.forEach(bStructField -> {
//                String defaultFieldEntry = System.lineSeparator() + "\t" + bStructField.getName().getValue()
//                        + UtilSymbolKeys.PKG_DELIMITER_KEYWORD + getDefaultValueForType(bStructField.getType());
//                fieldEntries.add(defaultFieldEntry);
//            });
//        }

        annotationStart.append(LINE_SEPARATOR).append("\t").append("${1}").append(LINE_SEPARATOR)
                .append(UtilSymbolKeys.CLOSE_BRACE_KEY);
        
        return annotationStart.toString();
    }

    /**
     * Get the completion Label for the annotation.
     *
     * @param packageID  Package ID
     * @param annotation BLang annotation
     * @return {@link String}          Label string
     */
    private static String getAnnotationLabel(PackageID packageID, BLangAnnotation annotation) {
        String pkgComponent = "";
        if (!packageID.getName().getValue().equals(Names.BUILTIN_PACKAGE.getValue())) {

            pkgComponent += packageID.getNameComps().get(packageID.getNameComps().size() - 1).getValue()
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
            case STRUCT:
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
     * Get the variable symbol info by the name.
     *
     * @param name    name of the variable
     * @param symbols list of symbol info
     * @return {@link SymbolInfo}   Symbol Info extracted
     */
    private static SymbolInfo getVariableByName(String name, List<SymbolInfo> symbols) {
        return symbols.stream()
                .filter(symbolInfo -> symbolInfo.getSymbolName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the invocations and fields against an identifier (functions, struct fields and types including the enums).
     *
     * @param context        Text Document Service context (Completion Context)
     * @param delimiterIndex delimiter index (index of either . or :)
     * @return {@link ArrayList}    List of filtered symbol info
     */
    public static ArrayList<SymbolInfo> invocationsAndFieldsOnIdentifier(LSServiceOperationContext context,
                                                                         int delimiterIndex) {
        ArrayList<SymbolInfo> actionFunctionList = new ArrayList<>();
        String lineSegment = context.get(CompletionKeys.CURRENT_LINE_SEGMENT_KEY);
        String variableName;
        String delimiter;
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        if (tokenStream == null) {
            variableName = CompletionUtil.getPreviousTokenFromLineSegment(lineSegment, delimiterIndex);
            delimiter = CompletionUtil.getDelimiterTokenFromLineSegment(context, lineSegment);
        } else {
            variableName = CommonUtil.getPreviousDefaultToken(tokenStream, delimiterIndex).getText();
            delimiter = tokenStream.get(delimiterIndex).getText();
        }
        List<SymbolInfo> symbols = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        SymbolTable symbolTable = context.get(DocumentServiceKeys.SYMBOL_TABLE_KEY);
        SymbolInfo variable = CommonUtil.getVariableByName(variableName, symbols);
        String builtinPkgName = symbolTable.builtInPackageSymbol.pkgID.name.getValue();
        Map<Name, Scope.ScopeEntry> entries = new HashMap<>();
        String currentPkgName = context.get(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY);

        if (variable == null) {
            return actionFunctionList;
        }

        String packageID;
        BType bType = variable.getScopeEntry().symbol.getType();
        String bTypeValue;

        
        if (variable.getScopeEntry().symbol instanceof BEndpointVarSymbol) {
            BType getClientFuncType = ((BEndpointVarSymbol) variable.getScopeEntry().symbol)
                    .getClientFunction.type;
            if (!UtilSymbolKeys.ACTION_INVOCATION_SYMBOL_KEY.equals(delimiter)
                    || !(getClientFuncType instanceof BInvokableType)) {
                return actionFunctionList;
            }
            
            actionFunctionList.addAll(getActionsOfEndpoint((BEndpointVarSymbol) variable.getScopeEntry().symbol));
        } else {
            if (bType instanceof BArrayType) {
                packageID = ((BArrayType) bType).eType.tsymbol.pkgID.getName().getValue();
                bTypeValue = bType.toString();
            } else {
                packageID = bType.tsymbol.pkgID.getName().getValue();
                bTypeValue = bType.toString();
            }

            // Extract the package symbol. This is used to extract the entries of the particular package
            SymbolInfo packageSymbolInfo = symbols.stream().filter(item -> {
                Scope.ScopeEntry scopeEntry = item.getScopeEntry();
                return (scopeEntry.symbol instanceof BPackageSymbol)
                        && scopeEntry.symbol.pkgID.name.getValue().equals(packageID);
            }).findFirst().orElse(null);

            if (packageID.equals(builtinPkgName)) {
                // If the packageID is ballerina.builtin, we extract entries of builtin package
                entries = symbolTable.builtInPackageSymbol.scope.entries;
            } else if (packageSymbolInfo == null && packageID.equals(currentPkgName)) {
                entries = getScopeEntries(bType, context);
            } else if (packageSymbolInfo != null) {
                // If the package exist, we extract particular entries from package
                entries = packageSymbolInfo.getScopeEntry().symbol.scope.entries;
            }

            entries.forEach((name, scopeEntry) -> {
                if (scopeEntry.symbol instanceof BInvokableSymbol
                        && ((BInvokableSymbol) scopeEntry.symbol).receiverSymbol != null) {
                    String symbolBoundedName = ((BInvokableSymbol) scopeEntry.symbol)
                            .receiverSymbol.getType().toString();

                    if (symbolBoundedName.equals(bTypeValue)) {
                        // TODO: Need to handle the name in a proper manner
                        String[] nameComponents = name.toString().split("\\.");
                        SymbolInfo actionFunctionSymbol =
                                new SymbolInfo(nameComponents[nameComponents.length - 1], scopeEntry);
                        actionFunctionList.add(actionFunctionSymbol);
                    }
                } else if ((scopeEntry.symbol instanceof BTypeSymbol)
                        && (SymbolKind.OBJECT.equals(scopeEntry.symbol.kind)
                        || SymbolKind.RECORD.equals(scopeEntry.symbol.kind))
                        && bTypeValue.equals(scopeEntry.symbol.type.toString())) {
                    // Get the struct fields
                    Map<Name, Scope.ScopeEntry> fields = scopeEntry.symbol.scope.entries;
                    fields.forEach((fieldName, fieldScopeEntry) -> {
                        actionFunctionList.add(new SymbolInfo(fieldName.getValue(), fieldScopeEntry));
                    });
                }
            });

            // Populate possible iterable operators over the variable
            populateIterableOperations(variable, actionFunctionList);
        }

        return actionFunctionList;
    }

    /**
     * Check whether a given symbol is an endpoint object or not.
     * @param bSymbol           BSymbol to evaluate
     * @return {@link Boolean}  Symbol evaluation status
     */
    public static boolean isEndpointObject(BSymbol bSymbol) {
        if (SymbolKind.OBJECT.equals(bSymbol.kind) && bSymbol instanceof BStructSymbol) {
            List<BStructSymbol.BAttachedFunction> attachedFunctions = ((BStructSymbol) bSymbol).attachedFuncs;
            for (BStructSymbol.BAttachedFunction attachedFunction : attachedFunctions) {
                if (attachedFunction.funcName.getValue().equals(UtilSymbolKeys.EP_OBJECT_IDENTIFIER)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Check whether the packages list contains a given package.
     * @param pkg               Package to check
     * @param pkgList           List of packages to check against
     * @return {@link Boolean}  Check status of the package
     */
    public static boolean listContainsPackage(String pkg, List<BallerinaPackage> pkgList) {
        return pkgList.stream().anyMatch(ballerinaPackage -> ballerinaPackage.getFullPackageNameAlias().equals(pkg));
    }

    /**
     * Get completion items list for struct fields.
     * 
     * @param structFields      List of struct fields
     * @return {@link List}     List of completion items for the struct fields
     */
    public static List<CompletionItem> getStructFieldPopulateCompletionItems(List<BStructType.BStructField>
                                                                                     structFields) {
        List<CompletionItem> completionItems = new ArrayList<>();
        structFields.forEach(bStructField -> {
            StringBuilder insertText = new StringBuilder(bStructField.getName().getValue() + ": ");
            if (bStructField.getType() instanceof BStructType) {
                insertText.append("{").append(LINE_SEPARATOR).append("\t${1}").append(LINE_SEPARATOR).append("}");
            } else {
                insertText.append("${1:").append(getDefaultValueForType(bStructField.getType())).append("}");
            }
            CompletionItem fieldItem = new CompletionItem();
            fieldItem.setInsertText(insertText.toString());
            fieldItem.setInsertTextFormat(InsertTextFormat.Snippet);
            fieldItem.setLabel(bStructField.getName().getValue());
            fieldItem.setDetail(ItemResolverConstants.FIELD_TYPE);
            fieldItem.setSortText(Priority.PRIORITY120.toString());
            completionItems.add(fieldItem);
        });

        return completionItems;
    }

    /**
     * Get the completion item to fill all the struct fields.
     * @param fields                    List of struct fields
     * @return {@link CompletionItem}   Completion Item to fill all the options
     */
    public static CompletionItem getFillAllStructFieldsItem(List<BStructType.BStructField> fields) {
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
        completionItem.setSortText(Priority.PRIORITY110.toString());

        return completionItem;
    }

    /**
     * Get the actions defined over and endpoint.
     * @param bEndpointVarSymbol    Endpoint variable symbol to evaluate
     * @return {@link List}         List of extracted actions as Symbol Info
     */
    public static List<SymbolInfo> getActionsOfEndpoint(BEndpointVarSymbol bEndpointVarSymbol) {
        List<SymbolInfo> endpointActions = new ArrayList<>();
        BType getClientFuncType = bEndpointVarSymbol.getClientFunction.type;
        BType boundType = ((BInvokableType) getClientFuncType).retType;
        boundType.tsymbol.scope.entries.forEach((name, scopeEntry) -> {
            if (scopeEntry.symbol instanceof BInvokableSymbol
                    && !scopeEntry.symbol.getName().getValue().equals(UtilSymbolKeys.NEW_KEYWORD_KEY)) {
                String[] nameComponents = name.toString().split("\\.");
                SymbolInfo actionFunctionSymbol =
                        new SymbolInfo(nameComponents[nameComponents.length - 1], scopeEntry);
                endpointActions.add(actionFunctionSymbol);
            }
        });

        return endpointActions;
    }

    // Private Methods

    private static void populateIterableOperations(SymbolInfo variable, List<SymbolInfo> symbolInfoList) {
        BType bType = variable.getScopeEntry().symbol.getType();

        if (bType instanceof BArrayType || bType instanceof BMapType || bType instanceof BJSONType
                || bType instanceof BXMLType || bType instanceof BTableType
                || bType instanceof BIntermediateCollectionType) {
            fillForeachIterableOperation(bType, symbolInfoList);
            fillMapIterableOperation(bType, symbolInfoList);
            fillFilterIterableOperation(bType, symbolInfoList);
            fillCountIterableOperation(symbolInfoList);
            if (bType instanceof BArrayType && (((BArrayType) bType).eType.toString().equals("int")
                    || ((BArrayType) bType).eType.toString().equals("float"))) {
                fillMinIterableOperation(symbolInfoList);
                fillMaxIterableOperation(symbolInfoList);
                fillAverageIterableOperation(symbolInfoList);
                fillSumIterableOperation(symbolInfoList);
            }

            // TODO: Add support for Table and Tuple collection
        }
    }

    private static void fillForeachIterableOperation(BType bType, List<SymbolInfo> symbolInfoList) {
        String params = getIterableOpLambdaParam(bType);

        String lambdaSignature =
                Snippet.ITR_FOREACH.toString().replace(UtilSymbolKeys.ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN, params);
        SymbolInfo.IterableOperationSignature signature =
                new SymbolInfo.IterableOperationSignature(ItemResolverConstants.ITR_FOREACH_LABEL, lambdaSignature);
        SymbolInfo forEachSymbolInfo = new SymbolInfo();
        forEachSymbolInfo.setIterableOperation(true);
        forEachSymbolInfo.setIterableOperationSignature(signature);
        symbolInfoList.add(forEachSymbolInfo);
    }

    private static void fillMapIterableOperation(BType bType, List<SymbolInfo> symbolInfoList) {
        String params = getIterableOpLambdaParam(bType);

        String lambdaSignature
                = Snippet.ITR_MAP.toString().replace(UtilSymbolKeys.ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN, params);
        SymbolInfo.IterableOperationSignature signature =
                new SymbolInfo.IterableOperationSignature(ItemResolverConstants.ITR_MAP_LABEL, lambdaSignature);
        SymbolInfo forEachSymbolInfo = new SymbolInfo();
        forEachSymbolInfo.setIterableOperation(true);
        forEachSymbolInfo.setIterableOperationSignature(signature);
        symbolInfoList.add(forEachSymbolInfo);
    }

    private static void fillFilterIterableOperation(BType bType, List<SymbolInfo> symbolInfoList) {
        String params = getIterableOpLambdaParam(bType);

        String lambdaSignature
                = Snippet.ITR_FILTER.toString().replace(UtilSymbolKeys.ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN, params);
        SymbolInfo.IterableOperationSignature signature =
                new SymbolInfo.IterableOperationSignature(ItemResolverConstants.ITR_FILTER_LABEL, lambdaSignature);
        SymbolInfo forEachSymbolInfo = new SymbolInfo();
        forEachSymbolInfo.setIterableOperation(true);
        forEachSymbolInfo.setIterableOperationSignature(signature);
        symbolInfoList.add(forEachSymbolInfo);
    }

    private static void fillCountIterableOperation(List<SymbolInfo> symbolInfoList) {
        String lambdaSignature = Snippet.ITR_COUNT.toString();
        SymbolInfo.IterableOperationSignature signature =
                new SymbolInfo.IterableOperationSignature(ItemResolverConstants.ITR_COUNT_LABEL, lambdaSignature);
        SymbolInfo forEachSymbolInfo = new SymbolInfo();
        forEachSymbolInfo.setIterableOperation(true);
        forEachSymbolInfo.setIterableOperationSignature(signature);
        symbolInfoList.add(forEachSymbolInfo);
    }

    private static void fillMinIterableOperation(List<SymbolInfo> symbolInfoList) {
        String lambdaSignature = Snippet.ITR_MIN.toString();
        SymbolInfo.IterableOperationSignature signature =
                new SymbolInfo.IterableOperationSignature(ItemResolverConstants.ITR_MIN_LABEL, lambdaSignature);
        SymbolInfo forEachSymbolInfo = new SymbolInfo();
        forEachSymbolInfo.setIterableOperation(true);
        forEachSymbolInfo.setIterableOperationSignature(signature);
        symbolInfoList.add(forEachSymbolInfo);
    }

    private static void fillMaxIterableOperation(List<SymbolInfo> symbolInfoList) {
        String lambdaSignature = Snippet.ITR_MAX.toString();
        SymbolInfo.IterableOperationSignature signature =
                new SymbolInfo.IterableOperationSignature(ItemResolverConstants.ITR_MAX_LABEL, lambdaSignature);
        SymbolInfo forEachSymbolInfo = new SymbolInfo();
        forEachSymbolInfo.setIterableOperation(true);
        forEachSymbolInfo.setIterableOperationSignature(signature);
        symbolInfoList.add(forEachSymbolInfo);
    }

    private static void fillAverageIterableOperation(List<SymbolInfo> symbolInfoList) {
        String lambdaSignature = Snippet.ITR_AVERAGE.toString();
        SymbolInfo.IterableOperationSignature signature =
                new SymbolInfo.IterableOperationSignature(ItemResolverConstants.ITR_AVERAGE_LABEL, lambdaSignature);
        SymbolInfo forEachSymbolInfo = new SymbolInfo();
        forEachSymbolInfo.setIterableOperation(true);
        forEachSymbolInfo.setIterableOperationSignature(signature);
        symbolInfoList.add(forEachSymbolInfo);
    }

    private static void fillSumIterableOperation(List<SymbolInfo> symbolInfoList) {
        String lambdaSignature = Snippet.ITR_SUM.toString();
        SymbolInfo.IterableOperationSignature signature =
                new SymbolInfo.IterableOperationSignature(ItemResolverConstants.ITR_SUM_LABEL, lambdaSignature);
        SymbolInfo forEachSymbolInfo = new SymbolInfo();
        forEachSymbolInfo.setIterableOperation(true);
        forEachSymbolInfo.setIterableOperationSignature(signature);
        symbolInfoList.add(forEachSymbolInfo);
    }

    private static String getIterableOpLambdaParam(BType bType) {
        String params = "";
        if (bType instanceof BMapType) {
            params = Snippet.ITR_ON_MAP_PARAMS.toString();
        } else if (bType instanceof BArrayType) {
            params = ((BArrayType) bType).eType.toString() + " v";
        } else if (bType instanceof BJSONType) {
            params = Snippet.ITR_ON_JSON_PARAMS.toString();
        } else if (bType instanceof BXMLType) {
            params = Snippet.ITR_ON_XML_PARAMS.toString();
        }

        return params;
    }

    /**
     * Get the scope entries.
     *
     * @param bType         BType
     * @param completionCtx Completion context
     * @return {@link Map} Scope entries map
     */
    private static Map<Name, Scope.ScopeEntry> getScopeEntries(BType bType, LSServiceOperationContext completionCtx) {
        HashMap<Name, Scope.ScopeEntry> returnMap = new HashMap<>();
        completionCtx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                .forEach(symbolInfo -> {
                    if ((symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol
                            && symbolInfo.getScopeEntry().symbol.getType() != null
                            && symbolInfo.getScopeEntry().symbol.getType().toString().equals(bType.toString()))
                            || symbolInfo.getScopeEntry().symbol instanceof BInvokableSymbol) {
                        returnMap.put(symbolInfo.getScopeEntry().symbol.getName(), symbolInfo.getScopeEntry());
                    }
                });

        return returnMap;
    }
}

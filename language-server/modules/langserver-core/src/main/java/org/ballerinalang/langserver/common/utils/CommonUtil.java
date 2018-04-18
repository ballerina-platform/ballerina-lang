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

import com.google.common.base.CaseFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.CollectDiagnosticListener;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.LSDocument;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.workspace.repository.NullSourceDirectory;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
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
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Common utils to be reuse in language server implementation.
 */
public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(TextDocumentFormatUtil.class);

    private static final String SYMBOL_TYPE = "symbolType";

    private static final String INVOCATION_TYPE = "invocationType";

    private static final String UNESCAPED_VALUE = "unescapedValue";

    private static final String OPEN_BRACKET_KEY_WORD = "(";

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
            int indexOfCurrentPkgName = currentPkgPath.indexOf(currentPkgName);
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
     * Generate json representation for the given node.
     *
     * @param node        Node to get the json representation
     * @param anonStructs Map of anonymous structs
     * @return {@link JsonElement}          Json Representation of the node
     */
    public static JsonElement generateJSON(Node node, Map<String, Node> anonStructs) {
        if (node == null) {
            return JsonNull.INSTANCE;
        }
        Set<Method> methods = ClassUtils.getAllInterfaces(node.getClass()).stream()
                .flatMap(aClass -> Arrays.stream(aClass.getMethods()))
                .collect(Collectors.toSet());
        JsonObject nodeJson = new JsonObject();

        JsonArray wsJsonArray = new JsonArray();
        Set<Whitespace> ws = node.getWS();
        if (ws != null && !ws.isEmpty()) {
            for (Whitespace whitespace : ws) {
                JsonObject wsJson = new JsonObject();
                wsJson.addProperty("ws", whitespace.getWs());
                wsJson.addProperty("i", whitespace.getIndex());
                wsJson.addProperty("text", whitespace.getPrevious());
                wsJson.addProperty("static", whitespace.isStatic());
                wsJsonArray.add(wsJson);
            }
            nodeJson.add("ws", wsJsonArray);
        }
        org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition position = node.getPosition();
        if (position != null) {
            JsonObject positionJson = new JsonObject();
            positionJson.addProperty("startColumn", position.getStartColumn());
            positionJson.addProperty("startLine", position.getStartLine());
            positionJson.addProperty("endColumn", position.getEndColumn());
            positionJson.addProperty("endLine", position.getEndLine());
            nodeJson.add("position", positionJson);
        }

        JsonArray type = getType(node);
        if (type != null) {
            nodeJson.add(SYMBOL_TYPE, type);
        }
        if (node.getKind() == NodeKind.INVOCATION) {
            assert node instanceof BLangInvocation : node.getClass();
            BLangInvocation invocation = (BLangInvocation) node;
            if (invocation.symbol != null && invocation.symbol.kind != null) {
                nodeJson.addProperty(INVOCATION_TYPE, invocation.symbol.kind.toString());
            }
        }

        for (Method m : methods) {
            String name = m.getName();

            if (name.equals("getWS") || name.equals("getPosition")) {
                continue;
            }

            String jsonName;
            if (name.startsWith("get")) {
                jsonName = toJsonName(name, 3);
            } else if (name.startsWith("is")) {
                jsonName = toJsonName(name, 2);
            } else {
                continue;
            }

            Object prop = null;
            try {
                prop = m.invoke(node);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("Error while serializing source to JSON: [" + e.getMessage() + "]");
            }

            /* Literal class - This class is escaped in backend to address cases like "ss\"" and 8.0 and null */
            if (node.getKind() == NodeKind.LITERAL && "value".equals(jsonName)) {
                if (prop instanceof String) {
                    nodeJson.addProperty(jsonName, '"' + StringEscapeUtils.escapeJava((String) prop) + '"');
                    nodeJson.addProperty(UNESCAPED_VALUE, String.valueOf(prop));
                } else {
                    nodeJson.addProperty(jsonName, String.valueOf(prop));
                }
                continue;
            }

            if (node.getKind() == NodeKind.USER_DEFINED_TYPE && jsonName.equals("typeName")) {
                IdentifierNode typeNode = (IdentifierNode) prop;
                Node structNode;
                if (typeNode.getValue().startsWith("$anonStruct$") &&
                        (structNode = anonStructs.remove(typeNode.getValue())) != null) {
                    JsonObject anonStruct = generateJSON(structNode, anonStructs).getAsJsonObject();
                    anonStruct.addProperty("anonStruct", true);
                    nodeJson.add("anonStruct", anonStruct);
                    continue;
                }
            }

            if (prop instanceof List && jsonName.equals("types")) {
                // Currently we don't need any Symbols for the UI. So skipping for now.
                continue;
            }

            /* Node classes */
            if (prop instanceof Node) {
                nodeJson.add(jsonName, generateJSON((Node) prop, anonStructs));
            } else if (prop instanceof List) {
                List listProp = (List) prop;
                JsonArray listPropJson = new JsonArray();
                nodeJson.add(jsonName, listPropJson);
                for (Object listPropItem : listProp) {
                    if (listPropItem instanceof Node) {
                        /* Remove top level anon func and struct */
                        if (node.getKind() == NodeKind.COMPILATION_UNIT) {
                            if (listPropItem instanceof BLangStruct && ((BLangStruct) listPropItem).isAnonymous) {
                                anonStructs.put(((BLangStruct) listPropItem).getName().getValue(),
                                        ((BLangStruct) listPropItem));
                                continue;
                            }
                            if (listPropItem instanceof BLangFunction
                                    && (((BLangFunction) listPropItem)).name.value.startsWith("$lambda$")) {
                                continue;
                            }
                        }
                        listPropJson.add(generateJSON((Node) listPropItem, anonStructs));
                    } else {
                        logger.debug("Can't serialize " + jsonName + ", has a an array of " + listPropItem);
                    }
                }
                /* Runtime model classes */
            } else if (prop instanceof Set && jsonName.equals("flags")) {
                Set flags = (Set) prop;
                for (Flag flag : Flag.values()) {
                    nodeJson.addProperty(StringUtils.lowerCase(flag.toString()), flags.contains(flag));
                }
            } else if (prop instanceof Set) {
                // TODO : limit this else if to getInputs getOutputs of transform.
                Set vars = (Set) prop;
                JsonArray listVarJson = new JsonArray();
                nodeJson.add(jsonName, listVarJson);
                for (Object obj : vars) {
                    listVarJson.add(obj.toString());
                }
            } else if (prop instanceof NodeKind) {
                String kindName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, prop.toString());
                nodeJson.addProperty(jsonName, kindName);
            } else if (prop instanceof OperatorKind) {
                nodeJson.addProperty(jsonName, prop.toString());
                /* Generic classes */
            } else if (prop instanceof String) {
                nodeJson.addProperty(jsonName, (String) prop);
            } else if (prop instanceof Number) {
                nodeJson.addProperty(jsonName, (Number) prop);
            } else if (prop instanceof Boolean) {
                nodeJson.addProperty(jsonName, (Boolean) prop);
            } else if (prop instanceof Enum) {
                nodeJson.addProperty(jsonName, StringUtils.lowerCase(((Enum) prop).name()));
            } else if (prop != null) {
                nodeJson.addProperty(jsonName, prop.toString());
                String message = "Node " + node.getClass().getSimpleName() +
                        " contains unknown type prop: " + jsonName + " of type " + prop.getClass();
                logger.error(message);
            }
        }
        return nodeJson;
    }

    /**
     * Convert given name to the Json object name.
     *
     * @param name      Name to be converted
     * @param prefixLen Length of prefix
     * @return {@link String}   Converted value
     */
    private static String toJsonName(String name, int prefixLen) {
        return Character.toLowerCase(name.charAt(prefixLen)) + name.substring(prefixLen + 1);
    }

    /**
     * Get Type of the node as an Json Array.
     *
     * @param node Node to get the types
     * @return {@link JsonArray}    Converted array value
     */
    public static JsonArray getType(Node node) {
        if (node instanceof BLangNode) {
            BType type = ((BLangNode) node).type;
            if (node instanceof BLangInvocation) {
                JsonArray jsonElements = new JsonArray();
                jsonElements.add(((BLangInvocation) node).type.getKind().typeName());
                return jsonElements;
            } else if (type != null) {
                JsonArray jsonElements = new JsonArray();
                jsonElements.add(type.getKind().typeName());
                return jsonElements;
            }
        }

        return null;
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
        List<String> topLevelKeywords = Arrays.asList("function", "service", "resource", "struct", "enum",
                "transformer", "object");
        LSDocument document = new LSDocument(identifier.getUri());
        String fileContent = docManager.getFileContent(getPath(document));
        String[] splitedFileContent = fileContent.split("\\n|\\r\\n|\\r");
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
     * Prepare a new compiler context.
     * @deprecated use LSContextManager.createTempCompilerContext() instead. If you need global singleton compiler
     * context please use LSContextManager.getBuiltInPackagesCompilerContext()
     * @return {@link CompilerContext} Prepared compiler context
     */
    @Deprecated
    public static CompilerContext prepareTempCompilerContext() {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, "");
        options.put(COMPILER_PHASE, CompilerPhase.DESUGAR.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        context.put(SourceDirectory.class, new NullSourceDirectory());
        List<org.ballerinalang.util.diagnostic.Diagnostic> balDiagnostics = new ArrayList<>();
        CollectDiagnosticListener diagnosticListener = new CollectDiagnosticListener(balDiagnostics);
        context.put(DiagnosticListener.class, diagnosticListener);

        return context;
    }

    /**
     * Get the Annotation completion Item.
     * @param packageID                 Package Id
     * @param annotation                BLang annotation to extract the completion Item
     * @return {@link CompletionItem}   Completion item for the annotation
     */
    public static CompletionItem getAnnotationCompletionItem(PackageID packageID, BLangAnnotation annotation) {
        String label = getAnnotationLabel(packageID, annotation);
        String insertText = getAnnotationInsertText(packageID, annotation);
        CompletionItem annotationItem = new CompletionItem();
        annotationItem.setLabel(label);
        annotationItem.setInsertText(insertText);
        annotationItem.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
        
        return annotationItem;
    }

    /**
     * Get the annotation Insert text.
     * @param packageID         Package ID
     * @param annotation        Annotation to get the insert text
     * @return {@link String}   Insert text
     */
    private static String getAnnotationInsertText(PackageID packageID, BLangAnnotation annotation) {
        String pkgAlias = packageID.getNameComps().get(packageID.getNameComps().size() - 1).getValue();
        String annotationStart = "";
        if (!packageID.getName().getValue().equals(Names.BUILTIN_PACKAGE.getValue())) {
            annotationStart = pkgAlias + UtilSymbolKeys.PKG_DELIMITER_KEYWORD;
        }
        annotationStart += annotation.getName().getValue() + " " + UtilSymbolKeys.OPEN_BRACE_KEY;
        List<String> fieldEntries = new ArrayList<>();

        if (annotation.typeNode.type instanceof BStructType) {
            ((BStructType) annotation.typeNode.type).fields.forEach(bStructField -> {
                String defaultFieldEntry = System.lineSeparator() + "\t" + bStructField.getName().getValue()
                        + UtilSymbolKeys.PKG_DELIMITER_KEYWORD + getDefaultValueForType(bStructField.getType());
                fieldEntries.add(defaultFieldEntry);
            });
        }
        
        return annotationStart + String.join(",", fieldEntries)
                + System.lineSeparator() + UtilSymbolKeys.CLOSE_BRACE_KEY;
    }

    /**
     * Get the completion Label for the annotation.
     * @param packageID         Package ID
     * @param annotation        BLang annotation
     * @return {@link String}          Label string
     */
    private static String getAnnotationLabel(PackageID packageID, BLangAnnotation annotation) {
        String pkgComponent = UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY;
        if (!packageID.getName().getValue().equals(Names.BUILTIN_PACKAGE.getValue())) {
            
            pkgComponent += packageID.getNameComps().get(packageID.getNameComps().size() - 1).getValue()
                    + UtilSymbolKeys.PKG_DELIMITER_KEYWORD;
        }
        
        return pkgComponent + annotation.getName().getValue();
    }

    /**
     * Get the default value for the given BType.
     * @param bType             BType to get the default value
     * @return {@link String}   Default value as a String
     */
    public static String getDefaultValueForType(BType bType) {
        String typeString;
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
                typeString = bType.toString();
                break;
            case UNION:
                String[] typeNameComps = bType.toString().split(UtilSymbolKeys.PKG_DELIMITER_KEYWORD);
                typeString = typeNameComps[typeNameComps.length - 1];
                break;
            case STREAM:
            default:
                typeString = "null";
                break;
        }
        return typeString;
    }

    /**
     * Get the base indentation of a given node.
     * @param token             Token to be evaluated
     * @return {@link String}   Calculated base indentation
     */
    public static String getBaseIndentationFromNode(Token token) {
        int tokenCharPosition = token.getCharPositionInLine();
        return String.join("", Collections.nCopies(tokenCharPosition, " "));
    }



    /**
     * Get the variable symbol info by the name.
     * @param name      name of the variable
     * @param symbols   list of symbol info
     * @return {@link SymbolInfo}   Symbol Info extracted
     */
    public static SymbolInfo getVariableByName(String name, List<SymbolInfo> symbols) {
        return symbols.stream()
                .filter(symbolInfo -> symbolInfo.getSymbolName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the invocations and fields against an identifier (functions, struct fields and types including the enums).
     * @param context     Text Document Service context (Completion Context)
     * @param delimiterIndex        delimiter index (index of either . or :)
     * @return {@link ArrayList}    List of filtered symbol info
     */
    public static ArrayList<SymbolInfo> invocationsAndFieldsOnIdentifier(LSServiceOperationContext context,
                                                                   int delimiterIndex) {
        ArrayList<SymbolInfo> actionFunctionList = new ArrayList<>();
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        List<SymbolInfo> symbols = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        SymbolTable symbolTable = context.get(DocumentServiceKeys.SYMBOL_TABLE_KEY);
        String variableName = CommonUtil.getPreviousDefaultToken(tokenStream, delimiterIndex).getText();
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
            if (!UtilSymbolKeys.ACTION_INVOCATION_SYMBOL_KEY.equals(tokenStream.get(delimiterIndex).getText())
                    || !(getClientFuncType instanceof BInvokableType)) {
                return actionFunctionList;
            }

            BType boundType = ((BInvokableType) getClientFuncType).retType;
            boundType.tsymbol.scope.entries.forEach((name, scopeEntry) -> {
                if (scopeEntry.symbol instanceof BInvokableSymbol
                        && !scopeEntry.symbol.getName().getValue().equals(UtilSymbolKeys.NEW_KEYWORD_KEY)) {
                    String[] nameComponents = name.toString().split("\\.");
                    SymbolInfo actionFunctionSymbol =
                            new SymbolInfo(nameComponents[nameComponents.length - 1], scopeEntry);
                    actionFunctionList.add(actionFunctionSymbol);
                }
            });
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
     * @param bType             BType 
     * @param completionCtx     Completion context
     * @return                  {@link Map} Scope entries map
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

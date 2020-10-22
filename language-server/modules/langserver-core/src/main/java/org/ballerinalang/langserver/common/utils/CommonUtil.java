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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.compiler.api.types.RecordTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import io.ballerina.compiler.api.types.UnionTypeDescriptor;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.FieldCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.langserver.exception.LSStdlibCacheException;
import org.ballerinalang.langserver.util.definition.LSStandardLibCache;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;

/**
 * Common utils to be reuse in language server implementation.
 */
public class CommonUtil {
    private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    public static final String MD_LINE_SEPARATOR = "  " + System.lineSeparator();

    public static final String LINE_SEPARATOR = System.lineSeparator();

    public static final String FILE_SEPARATOR = File.separator;

    public static final String LINE_SEPARATOR_SPLIT = "\\r?\\n";

    public static final Pattern MD_NEW_LINE_PATTERN = Pattern.compile("\\s\\s\\r\\n?|\\s\\s\\n|\\r\\n?|\\n");

    public static final String BALLERINA_HOME;

    public static final String BALLERINA_CMD;

    public static final String MARKDOWN_MARKUP_KIND = "markdown";

    public static final String BALLERINA_ORG_NAME = "ballerina";

    public static final String BALLERINAX_ORG_NAME = "ballerinax";

    public static final String SDK_VERSION = System.getProperty("ballerina.version");

    private static final String BUILT_IN_PACKAGE_PREFIX = "lang.annotations";

    public static final Path LS_STDLIB_CACHE_DIR = TEMP_DIR.resolve("ls_stdlib_cache").resolve(SDK_VERSION);

    public static final Path LS_CONNECTOR_CACHE_DIR = TEMP_DIR.resolve("ls_connector_cache").resolve(SDK_VERSION);

    static {
        BALLERINA_HOME = System.getProperty("ballerina.home");
        BALLERINA_CMD = BALLERINA_HOME + File.separator + "bin" + File.separator + "ballerina" +
                (SystemUtils.IS_OS_WINDOWS ? ".bat" : "");
    }

    private CommonUtil() {
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
     * Convert the diagnostic range to a zero based positioning diagnostic range.
     *
     * @param lineRange - diagnostic position to be cloned
     * @return {@link DiagnosticPos} converted diagnostic position
     */
    public static Range toRange(LineRange lineRange) {
        int startLine = lineRange.startLine().line();
        int endLine = lineRange.endLine().line();
        int startColumn = lineRange.startLine().offset();
        int endColumn = lineRange.endLine().offset();
        return new Range(new Position(startLine, startColumn), new Position(endLine, endColumn));
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
     * Get the default value for the given BType.
     *
     * @param bType Type descriptor to get the default value
     * @return {@link String}   Default value as a String
     */
    public static String getDefaultValueForType(BallerinaTypeDescriptor bType) {
        String typeString;
        if (bType == null) {
            return "()";
        }
        switch (bType.kind()) {
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
            case RECORD:
            case MAP:
                typeString = "{}";
                break;
            case OBJECT:
                typeString = "new()";
                break;
            // Fixme
//            case FINITE:
//                List<BLangExpression> valueSpace = new ArrayList<>(((BFiniteType) bType).getValueSpace());
//                String value = valueSpace.get(0).toString();
//                BType type = valueSpace.get(0).type;
//                typeString = value;
//                if (type.toString().equals("string")) {
//                    typeString = "\"" + typeString + "\"";
//                }
//                break;
            case UNION:
                List<BallerinaTypeDescriptor> members =
                        new ArrayList<>(((UnionTypeDescriptor) bType).memberTypeDescriptors());
                typeString = getDefaultValueForType(members.get(0));
                break;
            case STREAM:
//            case TABLE:
            default:
                typeString = "()";
                break;
        }
        return typeString;
    }

    /**
     * Get completion items list for struct fields.
     *
     * @param context Language server operation context
     * @param fields  List of field descriptors
     * @return {@link List}     List of completion items for the struct fields
     */
    public static List<LSCompletionItem> getRecordFieldCompletionItems(LSContext context,
                                                                       List<FieldDescriptor> fields) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        fields.forEach(field -> {
            String insertText = getRecordFieldCompletionInsertText(field, 0);
            CompletionItem fieldItem = new CompletionItem();
            fieldItem.setInsertText(insertText);
            fieldItem.setInsertTextFormat(InsertTextFormat.Snippet);
            fieldItem.setLabel(field.name());
            fieldItem.setDetail(ItemResolverConstants.FIELD_TYPE);
            fieldItem.setKind(CompletionItemKind.Field);
            fieldItem.setSortText(Priority.PRIORITY120.toString());
            completionItems.add(new FieldCompletionItem(context, field, fieldItem));
        });

        return completionItems;
    }

    /**
     * Get the completion item to fill all the struct fields.
     *
     * @param context Language Server Operation Context
     * @param fields  List of fields
     * @return {@link LSCompletionItem}   Completion Item to fill all the options
     */
    public static LSCompletionItem getFillAllStructFieldsItem(LSContext context, List<FieldDescriptor> fields) {
        List<String> fieldEntries = new ArrayList<>();

        for (FieldDescriptor fieldDescriptor : fields) {
            String defaultFieldEntry = fieldDescriptor.name()
                    + CommonKeys.PKG_DELIMITER_KEYWORD + " " + getDefaultValueForType(fieldDescriptor.typeDescriptor());
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

        return new StaticCompletionItem(context, completionItem, StaticCompletionItem.Kind.OTHER);
    }

    /**
     * Get the completion Item for the error type.
     *
     * @param context LS Operation context
     * @return {@link LSCompletionItem} generated for error type
     */
    public static LSCompletionItem getErrorTypeCompletionItem(LSContext context) {
        CompletionItem errorTypeCItem = new CompletionItem();
        errorTypeCItem.setInsertText(ItemResolverConstants.ERROR);
        errorTypeCItem.setLabel(ItemResolverConstants.ERROR);
        errorTypeCItem.setDetail(ItemResolverConstants.ERROR);
        errorTypeCItem.setInsertTextFormat(InsertTextFormat.Snippet);
        errorTypeCItem.setKind(CompletionItemKind.Event);

        return new StaticCompletionItem(context, errorTypeCItem, StaticCompletionItem.Kind.TYPE);
    }

    /**
     * Get the Symbol Name.
     *
     * @param bSymbol BSymbol to evaluate
     * @return captured symbol name
     */
    public static String getSymbolName(BSymbol bSymbol) {
        String nameValue = bSymbol.name.getValue();
        String[] split = nameValue.split("\\.");
        return split[split.length - 1];
    }

    /**
     * Filter a type in the module by the name.
     *
     * @param context  language server operation context
     * @param alias    module alias
     * @param typeName type name to be filtered against
     * @return {@link Optional} type found
     */
    public static Optional<TypeSymbol> getTypeFromModule(LSContext context, String alias, String typeName) {
        Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, alias);
        if (module.isEmpty()) {
            return Optional.empty();
        }
        for (TypeSymbol typeSymbol : module.get().typeDefinitions()) {
            if (typeSymbol.name().equals(typeName)) {
                return Optional.of(typeSymbol);
            }
        }

        return Optional.empty();
    }

    /**
     * Get the module symbol associated with the given alias.
     *
     * @param context Language server operation context
     * @param alias   alias value
     * @return {@link Optional} scope entry for the module symbol
     */
    public static Optional<ModuleSymbol> searchModuleForAlias(LSContext context, String alias) {
        List<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        for (Symbol symbol : visibleSymbols) {
            if (symbol.kind() == MODULE && symbol.name().equals(alias)) {
                return Optional.of((ModuleSymbol) symbol);
            }
        }

        return Optional.empty();
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
    public static boolean isWorkerDereivative(StatementNode node) {
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
     * Get the function invocation signature.
     *
     * @param functionSymbol ballerina function instance
     * @param functionName   function name
     * @param ctx            Language Server Operation context
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    public static Pair<String, String> getFunctionInvocationSignature(FunctionSymbol functionSymbol,
                                                                      String functionName,
                                                                      LSContext ctx) {
        if (functionSymbol == null) {
            return ImmutablePair.of(functionName + "();", functionName + "()");
        }
        FunctionTypeDescriptor functionTypeDesc = functionSymbol.typeDescriptor();
        StringBuilder signature = new StringBuilder(functionName + "(");
        StringBuilder insertText = new StringBuilder(functionName + "(");
        List<String> funcArguments = FunctionGenerator.getFuncArguments(functionSymbol, ctx);
        if (!funcArguments.isEmpty()) {
            signature.append(String.join(", ", funcArguments));
            insertText.append("${1}");
        }
        signature.append(")");
        insertText.append(")");
        Optional<BallerinaTypeDescriptor> returnType = functionTypeDesc.returnTypeDescriptor();
        if (returnType.isEmpty() || returnType.get().kind() == TypeDescKind.NIL) {
            insertText.append(";");
        }
        String initString = "(";
        String endString = ")";

        if (returnType.isPresent() && returnType.get().kind() != TypeDescKind.NIL) {
            signature.append(initString).append(returnType.get().signature());
            signature.append(endString);
        }

        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    /**
     * Extract the required fields from the records.
     *
     * @param recordType record type descriptor to evaluate
     * @return {@link List} of required fields captured
     */
    public static List<FieldDescriptor> getMandatoryRecordFields(RecordTypeDescriptor recordType) {
        return recordType.fieldDescriptors().stream()
                .filter(field -> !field.hasDefaultValue() && !field.isOptional())
                .collect(Collectors.toList());
    }

    /**
     * Get the completion item insert text for a BField.
     *
     * @param bField BField to evaluate
     * @return {@link String} Insert text
     */
    public static String getRecordFieldCompletionInsertText(FieldDescriptor bField, int tabOffset) {
        BallerinaTypeDescriptor fieldType = bField.typeDescriptor();
        StringBuilder insertText = new StringBuilder(bField.name() + ": ");
        if (fieldType.kind() == TypeDescKind.RECORD) {
            List<FieldDescriptor> requiredFields = getMandatoryRecordFields((RecordTypeDescriptor) fieldType);
            if (requiredFields.isEmpty()) {
                insertText.append("{").append("${1}}");
                return insertText.toString();
            }
            insertText.append("{").append(LINE_SEPARATOR);
            int tabCount = tabOffset;
            List<String> requiredFieldInsertTexts = new ArrayList<>();
            for (FieldDescriptor field : requiredFields) {
                String fieldText = String.join("", Collections.nCopies(tabCount + 1, "\t")) +
                        getRecordFieldCompletionInsertText(field, tabCount) +
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
        } else if (fieldType.kind() == TypeDescKind.STRING) {
            insertText.append("\"").append("${1}").append("\"");
        } else {
            insertText.append("${1:").append(getDefaultValueForType(bField.typeDescriptor())).append("}");
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
     * Predicate to check for the imports in the current file.
     *
     * @return {@link Predicate}    Predicate for the check
     */
    public static Predicate<BLangImportPackage> importInCurrentFilePredicate(LSContext ctx) {
        String currentFile = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        return importPkg -> importPkg.pos.getSource().cUnitName.replace("/", FILE_SEPARATOR).equals(currentFile);
    }

    /**
     * Predicate to check for the standard library imports in the current file which aren't cached already.
     *
     * @return {@link Predicate}    Predicate for the check
     */
    public static Predicate<BLangImportPackage> stdLibImportsNotCachedPredicate(LSContext ctx) {
        String currentFile = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        return importPkg -> importPkg.pos.getSource().cUnitName.replace("/", FILE_SEPARATOR).equals(currentFile)
                && importPkg.getWS() != null && (importPkg.orgName.value.equals(BALLERINA_ORG_NAME)
                || importPkg.orgName.value.equals(BALLERINAX_ORG_NAME));
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
     * @param name {@link BLangNode}
     * @return random argument name
     */
    public static String generateVariableName(String name, Set<String> names) {
        return generateVariableName(1, name, names);
    }

    /**
     * Generates a variable name.
     *
     * @param symbol {@link Symbol}
     * @return random argument name
     */
    public static String generateVariableName(Symbol symbol, Set<String> names) {
        return generateVariableName(1, symbol.kind().name(), names);
    }

    /**
     * Whether the given module is a langlib module.
     *
     * @param moduleID Module ID to evaluate
     * @return {@link Boolean} whether langlib or not
     */
    public static boolean isLangLib(ModuleID moduleID) {
        return moduleID.orgName().equals("ballerina")
                && moduleID.moduleName().startsWith("lang.");
    }

    private static String generateVariableName(int suffix, String name, Set<String> names) {
        name = name.replaceAll(".+[\\:\\.]", "");
        String newName = generateName(suffix, names);
        if (suffix == 1 && !name.isEmpty()) {
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
            newName = replacer.apply("to", newName);
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
                newName = generateVariableName(++suffix, name, names);
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

    /**
     * Returns module prefix and process imports required.
     *
     * @param importsAcceptor   import acceptor
     * @param currentModuleId   current module id
     * @param moduleID      module id
     * @param context   {@link LSContext}
     * @return  module prefix
     */
    public static String getModulePrefix(ImportsAcceptor importsAcceptor, ModuleID currentModuleId,
                                         ModuleID moduleID, LSContext context) {
        String pkgPrefix = "";
        if (!moduleID.equals(currentModuleId) && !BUILT_IN_PACKAGE_PREFIX.equals(moduleID.moduleName())) {
            String moduleName = escapeModuleName(context, moduleID.orgName() + "/" + moduleID.moduleName());
            String[] moduleParts = moduleName.split("/");
            String orgName = moduleParts[0];
            String alias = moduleParts[1];
            pkgPrefix = alias.replaceAll(".*\\.", "") + ":";
            if (importsAcceptor != null) {
                importsAcceptor.getAcceptor().accept(orgName, alias);
            }
        }
        return pkgPrefix;
    }

    public static ModuleID createModuleID(String orgName, String moduleName, String version) {
        return new ModuleID() {
            @Override
            public String orgName() {
                return orgName;
            }

            @Override
            public String moduleName() {
                return moduleName;
            }

            @Override
            public String version() {
                return version;
            }

            @Override
            public String modulePrefix() {
                List<String> names = Arrays.stream(moduleName.split("\\.")).collect(Collectors.toList());
                return names.get(names.size() - 1);
            }
        };
    }

    public static String escapeModuleName(LSContext context, String fullPackageNameAlias) {
        Set<String> names = new HashSet<>();
        Predicate<Scope.ScopeEntry> nonPkgNames = scopeEntry -> !(scopeEntry.symbol instanceof BPackageSymbol);
        try {
            names = CommonUtil.getAllNameEntries(context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY), nonPkgNames);
        } catch (Exception e) {
            // ignore
        }

        String[] moduleNameParts = fullPackageNameAlias.split("/");
        String moduleName = moduleNameParts[0];
        if (moduleNameParts.length > 1) {
            String alias = moduleNameParts[1];
            String[] aliasParts = moduleNameParts[1].split("\\.");
            if (aliasParts.length > 1) {
                String aliasPart1 = aliasParts[0];
                String aliasPart2 = aliasParts[1];
                if (names.contains(aliasPart2)) {
                    aliasPart2 = "'" + aliasPart2;
                }
                alias = aliasPart1 + "." + aliasPart2;
            } else {
                if (names.contains(alias)) {
                    alias = "'" + alias;
                }
            }
            moduleName = moduleName + "/" + alias;
        }
        return moduleName;
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
            // TODO: Fix?
            if (node1.getPosition() == null || node2.getPosition() == null) {
                return -1;
            }
            return node1.getPosition().getStartLine() - node2.getPosition().getStartLine();
        }
    }

    /**
     * Whether we skip the first parameter being included as a label in the signature.
     * When showing a lang lib invokable symbol over DOT(invocation) we do not show the first param, but when we
     * showing the invocation over package of the langlib with the COLON we show the first param
     *
     * @param context        context
     * @param functionSymbol invokable symbol
     * @return {@link Boolean} whether we show the first param or not
     */
    public static boolean skipFirstParam(LSContext context, FunctionSymbol functionSymbol) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        return isLangLib(functionSymbol.moduleID()) && nodeAtCursor.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE;
    }

    /**
     * Get all available name entries.
     *
     * @param context {@link CompilerContext}
     * @return set of strings
     */
    public static Set<String> getAllNameEntries(CompilerContext context) {
        return getAllNameEntries(context, null);
    }

    /**
     * Get all available name entries.
     *
     * @param context {@link CompilerContext}
     * @return set of strings
     */
    public static Set<String> getAllNameEntries(CompilerContext context, Predicate<Scope.ScopeEntry> predicate) {
        Set<String> strings = new HashSet<>();
        SymbolTable symbolTable = SymbolTable.getInstance(context);
        Map<BPackageSymbol, SymbolEnv> pkgEnvMap = symbolTable.pkgEnvMap;
        pkgEnvMap.values().forEach(env -> env.scope.entries.forEach((key, value) -> {
            if (predicate != null) {
                if (predicate.test(value)) {
                    strings.add(key.value);
                }
            } else {
                strings.add(key.value);
            }
        }));
        return strings;
    }

    /**
     * Get the path from given string URI.
     *
     * @param uri file uri
     * @return {@link Optional} Path from the URI
     */
    public static Optional<Path> getPathFromURI(String uri) {
        try {
            return Optional.of(Paths.get(new URL(uri).toURI()));
        } catch (URISyntaxException | MalformedURLException e) {
            // ignore
        }
        return Optional.empty();
    }

    /**
     * Update the standard library cache.
     *
     * @param context Language Server operation context
     */
    public static void updateStdLibCache(LSContext context) throws LSStdlibCacheException {
        Boolean enabled = context.get(DocumentServiceKeys.ENABLE_STDLIB_DEFINITION_KEY);
        if (enabled == null || !enabled) {
            return;
        }
        List<BLangImportPackage> stdLibImports = getCurrentModuleImports(context).stream()
                .filter(stdLibImportsNotCachedPredicate(context))
                .collect(Collectors.toList());

        LSStandardLibCache.getInstance().updateCache(stdLibImports);
    }

    /**
     * Check whether the file is a cached file entry.
     *
     * @param fileUri file URI to evaluate
     * @return whether the file is a cached entry or not
     */
    public static boolean isCachedExternalSource(String fileUri) {
        try {
            Path path = Paths.get(new URI(fileUri));
            return path.toAbsolutePath().toString().startsWith(LS_STDLIB_CACHE_DIR.toAbsolutePath().toString());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static NonTerminalNode findNode(LSContext context, Position position, Path path)
            throws WorkspaceDocumentException {
        WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        SyntaxTree syntaxTree = documentManager.getTree(path);
        TextDocument textDocument = syntaxTree.textDocument();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        return ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(txtPos, 0));
    }

    public static boolean isWithinLineRange(Position pos, LineRange lineRange) {
        int sLine = lineRange.startLine().line();
        int sCol = lineRange.startLine().offset();
        int eLine = lineRange.endLine().line();
        int eCol = lineRange.endLine().offset();
        return ((sLine == eLine && pos.getLine() == sLine) &&
                (pos.getCharacter() >= sCol && pos.getCharacter() <= eCol)
        ) || ((sLine != eLine) && (pos.getLine() > sLine && pos.getLine() < eLine ||
                pos.getLine() == eLine && pos.getCharacter() <= eCol ||
                pos.getLine() == sLine && pos.getCharacter() >= sCol
        ));
    }

    /**
     * Get the raw type of the type descriptor. If the type descriptor is a type reference then return the associated
     * type descriptor.
     *
     * @param typeDescriptor type descriptor to evaluate
     * @return {@link BallerinaTypeDescriptor} extracted type descriptor
     */
    public static BallerinaTypeDescriptor getRawType(BallerinaTypeDescriptor typeDescriptor) {
        return typeDescriptor.kind() == TypeDescKind.TYPE_REFERENCE
                ? ((TypeReferenceTypeDescriptor) typeDescriptor).typeDescriptor() : typeDescriptor;
    }
}

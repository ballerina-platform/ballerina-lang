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
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.codeaction.CodeActionModuleId;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.PatternConstants;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.completions.RecordFieldCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static org.ballerinalang.langserver.common.utils.CommonKeys.PKG_DELIMITER_KEYWORD;
import static org.ballerinalang.langserver.common.utils.CommonKeys.SEMI_COLON_SYMBOL_KEY;
import static org.ballerinalang.langserver.common.utils.CommonKeys.SLASH_KEYWORD_KEY;

/**
 * Common utils to be reused in language server implementation.
 */
public class CommonUtil {

    public static final String MD_LINE_SEPARATOR = "  " + System.lineSeparator();

    public static final String LINE_SEPARATOR = System.lineSeparator();

    public static final String FILE_SEPARATOR = File.separator;

    public static final Pattern MD_NEW_LINE_PATTERN = Pattern.compile("\\s\\s\\r\\n?|\\s\\s\\n|\\r\\n?|\\n");

    public static final String BALLERINA_HOME;

    public static final boolean COMPILE_OFFLINE;

    public static final String BALLERINA_CMD;

    public static final String URI_SCHEME_BALA = "bala";
    public static final String URI_SCHEME_EXPR = "expr";
    public static final String URI_SCHEME_FILE = "file";
    public static final String LANGUAGE_ID_BALLERINA = "ballerina";
    public static final String LANGUAGE_ID_TOML = "toml";

    public static final String MARKDOWN_MARKUP_KIND = "markdown";

    public static final String BALLERINA_ORG_NAME = "ballerina";

    public static final String SDK_VERSION = System.getProperty("ballerina.version");

    public static final String EXPR_SCHEME = "expr";

    public static final List<String> PRE_DECLARED_LANG_LIBS = Arrays.asList("lang.boolean", "lang.decimal",
            "lang.error", "lang.float", "lang.future", "lang.int", "lang.map", "lang.object", "lang.stream",
            "lang.string", "lang.table", "lang.transaction", "lang.typedesc", "lang.xml");

    public static final List<String> BALLERINA_KEYWORDS;

    public static final Set<SyntaxKind> QUALIFIER_KINDS = Set.of(SyntaxKind.SERVICE_KEYWORD,
            SyntaxKind.CLIENT_KEYWORD, SyntaxKind.ISOLATED_KEYWORD, SyntaxKind.TRANSACTIONAL_KEYWORD,
            SyntaxKind.PUBLIC_KEYWORD, SyntaxKind.PRIVATE_KEYWORD);

    public static final String SELF_KW = "self";

    private static final Pattern TYPE_NAME_DECOMPOSE_PATTERN = Pattern.compile("([\\w_.]*)/([\\w._]*):([\\w.-]*)");

    private static final int MAX_DEPTH = 1;

    static {
        BALLERINA_HOME = System.getProperty("ballerina.home");
        String onlineCompilation = System.getProperty("ls.compilation.online");
        COMPILE_OFFLINE = !Boolean.parseBoolean(onlineCompilation);
        BALLERINA_CMD = BALLERINA_HOME + File.separator + "bin" + File.separator + "bal" +
                (SystemUtils.IS_OS_WINDOWS ? ".bat" : "");
        BALLERINA_KEYWORDS = getBallerinaKeywords();
    }

    private CommonUtil() {
    }

    /**
     * Convert the syntax-node line range into a lsp4j range.
     *
     * @param lineRange - line range
     * @return {@link Range} converted range
     */
    public static Range toRange(LineRange lineRange) {
        return new Range(toPosition(lineRange.startLine()), toPosition(lineRange.endLine()));
    }

    /**
     * Converts syntax-node line position into a lsp4j position.
     *
     * @param linePosition - line position
     * @return {@link Position} converted position
     */
    public static Position toPosition(LinePosition linePosition) {
        return new Position(linePosition.line(), linePosition.offset());
    }

    /**
     * Get the text edit for an auto import statement.
     * Here we do not check whether the package is not already imported or a predeclared lang-lib, Particular
     * check should be done before usage
     *
     * @param orgName package org name
     * @param pkgName package name
     * @param context Language server context
     * @return {@link List}     List of Text Edits to apply
     */
    public static List<TextEdit> getAutoImportTextEdits(@Nonnull String orgName, String pkgName,
                                                        DocumentServiceContext context) {
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = context.currentDocImportsMap();
        Optional<ImportDeclarationNode> last = CommonUtil.getLastItem(new ArrayList<>(currentDocImports.keySet()));
        int endLine = last.map(node -> node.lineRange().endLine().line()).orElse(0);
        Position start = new Position(endLine, 0);
        String importStatement = ItemResolverConstants.IMPORT + " "
                + (!orgName.isEmpty() ? orgName + SLASH_KEYWORD_KEY : orgName)
                + pkgName + SEMI_COLON_SYMBOL_KEY
                + CommonUtil.LINE_SEPARATOR;

        return Collections.singletonList(new TextEdit(new Range(start, start), importStatement));
    }

    /**
     * Get the text edit for an auto import statement.
     * Here we do not check whether the package is not already imported or a predeclared lang-lib, Particular
     * check should be done before usage
     *
     * @param orgName package org name
     * @param pkgName package name
     * @param alias   import alias
     * @param context Language server context
     * @return {@link List}     List of Text Edits to apply
     */
    public static List<TextEdit> getAutoImportTextEdits(@Nonnull String orgName, String pkgName, String alias,
                                                        DocumentServiceContext context) {
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = context.currentDocImportsMap();
        Optional<ImportDeclarationNode> last = CommonUtil.getLastItem(new ArrayList<>(currentDocImports.keySet()));
        int endLine = last.map(node -> node.lineRange().endLine().line()).orElse(0);
        Position start = new Position(endLine, 0);

        StringBuilder builder = new StringBuilder(ItemResolverConstants.IMPORT + " "
                + (!orgName.isEmpty() ? orgName + SLASH_KEYWORD_KEY : orgName)
                + pkgName);
        if (!alias.isEmpty()) {
            builder.append(" as ").append(alias).append(" ");
        }
        builder.append(SEMI_COLON_SYMBOL_KEY).append(CommonUtil.LINE_SEPARATOR);

        return Collections.singletonList(new TextEdit(new Range(start, start), builder.toString()));
    }

    /**
     * Get the default value for the given BType.
     *
     * @param bType Type descriptor to get the default value
     * @return {@link String}   Default value as a String
     */
    public static Optional<String> getDefaultValueForType(TypeSymbol bType) {
        return getDefaultValueForType(bType, 1);
    }

    private static Optional<String> getDefaultValueForType(TypeSymbol bType, int depth) {
        String typeString;

        if (bType == null) {
            return Optional.empty();
        }

        TypeSymbol rawType = getRawType(bType);
        TypeDescKind typeKind = rawType.typeKind();
        switch (typeKind) {
            case TUPLE:
                TupleTypeSymbol tupleType = (TupleTypeSymbol) rawType;
                String memberTypes = tupleType.memberTypeDescriptors().stream()
                        .map(member -> getDefaultValueForType(member, depth + 1).orElse(""))
                        .collect(Collectors.joining(", "));
                typeString = "[" + memberTypes + "]";
                break;
            case ARRAY:
                // Filler value of an array is []
                ArrayTypeSymbol arrayType = (ArrayTypeSymbol) rawType;
                if (arrayType.memberTypeDescriptor().typeKind() == TypeDescKind.ARRAY) {
                    typeString = "[" + getDefaultValueForType(arrayType.memberTypeDescriptor(), depth + 1).orElse("")
                            + "]";
                } else {
                    typeString = "[]";
                }
                break;
            case RECORD:
                if (depth > MAX_DEPTH) {
                    return Optional.of("{}");
                }
                // TODO: Here we have disregarded the formatting of the record fields. Need to consider that in future
                RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) rawType;
                typeString = "{";
                typeString += getMandatoryRecordFields(recordTypeSymbol).stream()
                        .filter(recordFieldSymbol -> recordFieldSymbol.getName().isPresent())
                        .map(recordFieldSymbol -> recordFieldSymbol.getName().get() + ": " +
                                getDefaultValueForType(recordFieldSymbol.typeDescriptor(), depth + 1).orElse(""))
                        .collect(Collectors.joining(", "));
                typeString += "}";
                break;
            case OBJECT:
                if (depth > MAX_DEPTH) {
                    return Optional.of("");
                }
                ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) rawType;
                if (objectTypeSymbol.kind() == SymbolKind.CLASS) {
                    ClassSymbol classSymbol = (ClassSymbol) objectTypeSymbol;
                    if (classSymbol.initMethod().isPresent()) {
                        List<ParameterSymbol> params = classSymbol.initMethod().get().typeDescriptor().params().get();
                        String text = params.stream()
                                .map(param -> getDefaultValueForType(param.typeDescriptor(), depth + 1).orElse(""))
                                .collect(Collectors.joining(", "));
                        typeString = "new (" + text + ")";
                    } else {
                        typeString = "new ()";
                    }
                } else {
                    typeString = "object {}";
                }
                break;
            case UNION:
                if (depth > MAX_DEPTH) {
                    return Optional.of("");
                }
                List<TypeSymbol> members =
                        new ArrayList<>(((UnionTypeSymbol) rawType).memberTypeDescriptors());
                List<TypeSymbol> nilMembers = members.stream()
                        .filter(member -> member.typeKind() == TypeDescKind.NIL).collect(Collectors.toList());
                if (nilMembers.isEmpty()) {
                    typeString = getDefaultValueForType(members.get(0), depth + 1).orElse("");
                } else {
                    return Optional.of("()");
                }
                break;
            case INTERSECTION:
                TypeSymbol effectiveType = ((IntersectionTypeSymbol) rawType).effectiveTypeDescriptor();
                effectiveType = getRawType(effectiveType);
                if (effectiveType.typeKind() == TypeDescKind.INTERSECTION) {
                    // Right now, intersection types can only have readonly and another type only. Therefore, not doing 
                    // further checks here. Get the member type from intersection which is not readonly and get its 
                    // default value
                    typeString = "()";
                    Optional<TypeSymbol> memberType = ((IntersectionTypeSymbol) effectiveType)
                            .memberTypeDescriptors().stream()
                            .filter(typeSymbol -> typeSymbol.typeKind() != TypeDescKind.READONLY)
                            .findAny();
                    if (memberType.isPresent()) {
                        typeString = getDefaultValueForType(memberType.get(), depth + 1).orElse("");
                    }
                } else {
                    typeString = getDefaultValueForType(effectiveType, depth + 1).orElse("");
                }
                break;
            case TABLE:
                TypeSymbol rowType = ((TableTypeSymbol) rawType).rowTypeParameter();
                typeString = "table [" + getDefaultValueForType(rowType, depth + 1).orElse("") + "]";
                break;
            case ERROR:
                TypeSymbol errorType = CommonUtil.getRawType(((ErrorTypeSymbol) rawType).detailTypeDescriptor());
                StringBuilder errorString = new StringBuilder("error (\"\"");
                if (errorType.typeKind() == TypeDescKind.RECORD) {
                    errorString.append(", ");
                    errorString.append(getMandatoryRecordFields((RecordTypeSymbol) errorType).stream()
                            .map(recordFieldSymbol -> recordFieldSymbol.getName().get()
                                    + " = " + getDefaultValueForType(recordFieldSymbol.typeDescriptor(), depth + 1)
                                    .orElse(""))
                            .collect(Collectors.joining(", ")));
                }
                errorString.append(")");
                typeString = errorString.toString();
                break;
            case MAP:
            case FLOAT:
            case BOOLEAN:
            case STREAM:
            case XML:
            case DECIMAL:
            default:
                return getDefaultValueForTypeDescKind(typeKind);
        }

        return Optional.of(typeString);
    }

    /**
     * Used to get the default values for a {@link TypeDescKind}. {@link #getDefaultValueForType(TypeSymbol)}
     * is preferred over this function. This function should be used as a compliment to .
     *
     * @param typeKind Type desc kind
     * @return Optional default value
     * @see #getDefaultValueForType(TypeSymbol)
     */
    public static Optional<String> getDefaultValueForTypeDescKind(TypeDescKind typeKind) {
        String defaultValue = null;
        switch (typeKind) {
            case FLOAT:
                defaultValue = Float.toString(0);
                break;
            case BOOLEAN:
                defaultValue = Boolean.toString(false);
                break;
            case MAP:
                defaultValue = "{}";
                break;
            case STREAM:
                defaultValue = "new ()";
                break;
            case XML:
                defaultValue = "xml ``";
                break;
            case DECIMAL:
                defaultValue = Integer.toString(0);
                break;
            default:
                if (typeKind.isIntegerType()) {
                    defaultValue = Integer.toString(0);
                    break;
                }

                if (typeKind.isStringType()) {
                    defaultValue = "\"\"";
                    break;
                }
        }
        return Optional.ofNullable(defaultValue);
    }

    /**
     * Get completion items list for struct fields.
     *
     * @param context Language server operation context
     * @param fields  Map of field descriptors
     * @param symbol  Pair of Raw TypeSymbol and broader TypeSymbol
     * @return {@link List} List of completion items for the struct fields
     */
    public static List<LSCompletionItem> getRecordFieldCompletionItems(BallerinaCompletionContext context,
                                                                       Map<String, RecordFieldSymbol> fields,
                                                                       Pair<TypeSymbol, TypeSymbol> symbol) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        AtomicInteger fieldCounter = new AtomicInteger();
        fields.forEach((name, field) -> {
            fieldCounter.getAndIncrement();
            String insertText =
                    getRecordFieldCompletionInsertText(field, Collections.emptyList(), 0, fieldCounter.get());

            String detail;
            if (symbol.getLeft().getName().isPresent()) {
                detail = getModifiedTypeName(context, symbol.getLeft()) + "." + name;
            } else if (symbol.getRight().getName().isPresent()) {
                detail = getModifiedTypeName(context, symbol.getRight()) + "." + name;
            } else {
                detail = "(" + symbol.getLeft().signature() + ")." + name;
            }
            CompletionItem fieldItem = new CompletionItem();
            fieldItem.setInsertText(insertText);
            fieldItem.setInsertTextFormat(InsertTextFormat.Snippet);
            fieldItem.setLabel(name);
            fieldItem.setKind(CompletionItemKind.Field);
            fieldItem.setSortText(Priority.PRIORITY120.toString());
            completionItems.add(new RecordFieldCompletionItem(context, field, fieldItem, detail));
        });

        return completionItems;
    }

    /**
     * Get the completion item to fill all the struct fields.
     *
     * @param context Language Server Operation Context
     * @param fields  A non empty map of fields
     * @param symbol  Pair of Raw TypeSymbol and broader TypeSymbol
     * @return {@link LSCompletionItem}   Completion Item to fill all the options
     */
    public static LSCompletionItem getFillAllStructFieldsItem(BallerinaCompletionContext context,
                                                              Map<String, RecordFieldSymbol> fields,
                                                              Pair<TypeSymbol, TypeSymbol> symbol) {

        List<String> fieldEntries = new ArrayList<>();

        Map<String, RecordFieldSymbol> requiredFields = new HashMap<>();
        for (Map.Entry<String, RecordFieldSymbol> entry : fields.entrySet()) {
            if (!entry.getValue().isOptional()) {
                requiredFields.put(entry.getKey(), entry.getValue());
            }
        }

        String label;
        String detail;
        if (symbol.getLeft().getName().isPresent()) {
            detail = getModifiedTypeName(context, symbol.getLeft());
        } else if (symbol.getRight().getName().isPresent()) {
            detail = getModifiedTypeName(context, symbol.getRight());
        } else {
            detail = symbol.getLeft().signature();
        }
        if (!requiredFields.isEmpty()) {
            label = "Fill " + detail + " Required Fields";
            for (Map.Entry<String, RecordFieldSymbol> entry : requiredFields.entrySet()) {
                String fieldEntry = entry.getKey()
                        + PKG_DELIMITER_KEYWORD + " "
                        + getDefaultValueForType(entry.getValue().typeDescriptor()).orElse(" ");
                fieldEntries.add(fieldEntry);
            }
        } else {
            label = "Fill " + detail + " Optional Fields";
            for (Map.Entry<String, RecordFieldSymbol> entry : fields.entrySet()) {
                String fieldEntry = entry.getKey()
                        + PKG_DELIMITER_KEYWORD + " "
                        + getDefaultValueForType(entry.getValue().typeDescriptor()).orElse(" ");
                fieldEntries.add(fieldEntry);
            }
        }

        String insertText = String.join(("," + LINE_SEPARATOR), fieldEntries);
        CompletionItem completionItem = new CompletionItem();
        completionItem.setFilterText("fill");
        completionItem.setLabel(label);
        completionItem.setInsertText(insertText);
        completionItem.setDetail(detail);
        completionItem.setKind(CompletionItemKind.Property);
        completionItem.setSortText(Priority.PRIORITY110.toString());

        return new StaticCompletionItem(context, completionItem, StaticCompletionItem.Kind.OTHER);
    }

    /**
     * Filter a type in the module by the name.
     *
     * @param context  language server operation context
     * @param alias    module alias
     * @param typeName type name to be filtered against
     * @return {@link Optional} type found
     */
    public static Optional<TypeSymbol> getTypeFromModule(BallerinaCompletionContext context, String alias,
                                                         String typeName) {
        Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, alias);
        if (module.isEmpty()) {
            return Optional.empty();
        }

        ModuleSymbol moduleSymbol = module.get();
        for (TypeDefinitionSymbol typeDefinitionSymbol : moduleSymbol.typeDefinitions()) {
            if (typeDefinitionSymbol.getName().isPresent() && typeDefinitionSymbol.getName().get().equals(typeName)) {
                return Optional.of(typeDefinitionSymbol.typeDescriptor());
            }
        }

        for (ClassSymbol clazz : moduleSymbol.classes()) {
            if (clazz.getName().isPresent() && clazz.getName().get().equals(typeName)) {
                return Optional.of(clazz);
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
    public static Optional<ModuleSymbol> searchModuleForAlias(PositionedOperationContext context, String alias) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        for (Symbol symbol : visibleSymbols) {
            if (symbol.kind() == MODULE && Objects.equals(symbol.getName().orElse(null), alias)) {
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
    public static <T> Optional<T> getLastItem(List<T> list) {
        return (list.size() == 0) ? Optional.empty() : Optional.of(list.get(list.size() - 1));
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
     * Get the package name components combined.
     *
     * @param importNode {@link ImportDeclarationNode}
     * @return {@link String}   Combined package name
     */
    public static String getPackageNameComponentsCombined(ImportDeclarationNode importNode) {
        return importNode.moduleName().stream()
                .map(Token::text)
                .collect(Collectors.joining("."));
    }

    /**
     * Extract the required fields from the records.
     *
     * @param recordType record type descriptor to evaluate
     * @return {@link List} of required fields captured
     */
    public static List<RecordFieldSymbol> getMandatoryRecordFields(RecordTypeSymbol recordType) {
        return recordType.fieldDescriptors().values().stream()
                .filter(field -> !field.hasDefaultValue() && !field.isOptional())
                .collect(Collectors.toList());
    }

    /**
     * Get the completion item insert text for a BField.
     *
     * @param bField  BField to evaluate
     * @param parents Parent record field symbols
     * @return {@link String} Insert text
     */
    public static String getRecordFieldCompletionInsertText(RecordFieldSymbol bField,
                                                            List<RecordFieldSymbol> parents,
                                                            int tabOffset, int fieldId) {
        TypeSymbol fieldType = CommonUtil.getRawType(bField.typeDescriptor());
        StringBuilder insertText = new StringBuilder(bField.getName().get() + ": ");
        if (fieldType.typeKind() == TypeDescKind.RECORD) {
            List<RecordFieldSymbol> requiredFields = getMandatoryRecordFields((RecordTypeSymbol) fieldType);
            if (requiredFields.isEmpty()) {
                insertText.append("{").append("${").append(fieldId).append("}}");
                return insertText.toString();
            }
            insertText.append("{").append(LINE_SEPARATOR);
            List<String> requiredFieldInsertTexts = new ArrayList<>();

            for (int i = 0; i < requiredFields.size(); i++) {
                // If the field refers to the same type as bField or a parent of bField, 
                // it results in a stack overflow error. Avoiding that using the following check
                RecordFieldSymbol field = requiredFields.get(i);
                if (!parents.contains(field)) {
                    List<RecordFieldSymbol> newParentsList = new ArrayList<>(parents);
                    newParentsList.add(field);
                    String fieldText = String.join("", Collections.nCopies(tabOffset + 1, "\t")) +
                            getRecordFieldCompletionInsertText(field, newParentsList, tabOffset + 1, i + 1);
                    requiredFieldInsertTexts.add(fieldText);
                } else {
                    return bField.getName().get() + ": {}";
                }
            }
            insertText.append(String.join("," + CommonUtil.LINE_SEPARATOR, requiredFieldInsertTexts));
            insertText.append(LINE_SEPARATOR)
                    .append(String.join("", Collections.nCopies(tabOffset, "\t")))
                    .append("}");
        } else if (fieldType.typeKind() == TypeDescKind.ARRAY) {
            insertText.append("[").append("${").append(fieldId).append("}").append("]");
        } else if (fieldType.typeKind().isStringType()) {
            insertText.append("\"").append("${").append(fieldId).append("}").append("\"");
        } else {
            insertText.append("${").append(fieldId).append(":")
                    .append(getDefaultValueForType(bField.typeDescriptor()).orElse(" ")).append("}");
        }

        return insertText.toString();
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
     * @param symbol {@link Symbol}
     * @return random argument name
     */
    public static String generateVariableName(Symbol symbol, TypeSymbol typeSymbol, Set<String> names) {
        // In some scenarios the compiler sends the symbol name as empty string. Hence, add the check
        if (symbol != null && symbol.getName().isPresent() && !symbol.getName().get().isEmpty()) {
            // Start naming with symbol-name
            return generateVariableName(1, symbol.getName().get(), names);
        } else if (typeSymbol != null) {
            // If symbol is null, try typeSymbol
            String name;
            if (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE && typeSymbol.getName().isPresent()
                    && !typeSymbol.getName().get().startsWith("$")) {
                name = typeSymbol.getName().get();
            } else {
                TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);
                switch (rawType.typeKind()) {
                    case RECORD:
                        name = "mappingResult";
                        break;
                    case TUPLE:
                    case ARRAY:
                        name = "listResult";
                        break;
                    default:
                        name = rawType.typeKind().getName() + "Result";
                        break;
                }
            }
            return generateVariableName(1, name, names);
        } else {
            return generateName(1, names);
        }
    }

    private static String generateVariableName(int suffix, String name, Set<String> names) {
        names.addAll(BALLERINA_KEYWORDS);
        String newName = name.replaceAll(".+[\\:\\.]", "");
        if (suffix == 1 && !name.isEmpty()) {
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
            // If empty, revert to original name
            if (newName.isEmpty()) {
                newName = name;
            }
            // Lower first letter
            newName = newName.substring(0, 1).toLowerCase(Locale.getDefault()) + newName.substring(1);
            // if already available, try appending 'Result', 'Out', 'Value'
            boolean alreadyExists = false;
            String[] specialSuffixes = new String[]{"Result", "Out", "Value"};
            boolean[] flagSpecialSuffixes = new boolean[specialSuffixes.length];
            boolean addNoSpecialSuffix = false;
            // If any of special suffix already found in new-name, don't use any special suffix
            for (String currentSuffix : specialSuffixes) {
                if (newName.endsWith(currentSuffix)) {
                    addNoSpecialSuffix = true;
                    break;
                }
            }
            for (String nextName : names) {
                if (nextName.equals(newName)) {
                    // If new-name already exists
                    alreadyExists = true;
                } else if (!addNoSpecialSuffix) {
                    // Check a particular special suffix and new-name combination already exists
                    for (int i = 0; i < specialSuffixes.length; i++) {
                        String currentSuffix = specialSuffixes[i];
                        if (nextName.equals(newName + currentSuffix)) {
                            flagSpecialSuffixes[i] = true;
                        }
                    }
                }
            }
            // if already available, try appending 'Result' or 'Out'
            if (alreadyExists) {
                if (!addNoSpecialSuffix) {
                    for (int i = 0; i < flagSpecialSuffixes.length; i++) {
                        if (!flagSpecialSuffixes[i]) {
                            newName = newName + specialSuffixes[i];
                            break;
                        }
                    }
                } else {
                    return generateVariableName(++suffix, newName, names);
                }
            }
        } else {
            newName = newName + suffix;
        }
        // if still already available, try a random letter
        while (names.contains(newName)) {
            newName = generateName(++suffix, names);
        }
        return newName;
    }

    /**
     * Generates a parameter name.
     *
     * @param arg          Argument name.
     * @param position     Argument position.
     * @param type         Type symbol of the argument.
     * @param visibleNames Visible symbol names.
     * @return
     */
    public static String generateParameterName(String arg, int position, TypeSymbol type, Set<String> visibleNames) {
        String newName;
        if (arg.isEmpty() || !isValidIdentifier(arg)) {
            String typeName = type != null ? type.typeKind().getName() : "";
            if (!typeName.isEmpty()) {
                newName = typeName.substring(0, 1).toLowerCase(Locale.getDefault());
                return toCamelCase(getValidatedSymbolName(visibleNames, newName));
            } else {
                return generateName(position, visibleNames);
            }
        } else {
            return toCamelCase(getValidatedSymbolName(visibleNames, arg));
        }
    }

    /**
     * Get a validated symbol name against the visible symbols.
     * This method can be used to auto generate the symbol names without conflicting with the existing symbol names
     *
     * @param visibleNames visible symbol names in the context.
     * @param symbolName   raw symbol name to modify with the numbered suffix
     * @return {@link String} modified symbol name
     */
    public static String getValidatedSymbolName(Set<String> visibleNames, String symbolName) {
        if (!visibleNames.contains(symbolName)) {
            return symbolName;
        }
        List<Integer> suffixList = visibleNames.parallelStream().map(sName -> {
            if (sName == null) {
                return -2;
            }
            if (sName.equals(symbolName)) {
                return 0;
            }
            String modifiedName = sName.replaceFirst(symbolName, "");

            if (!modifiedName.isEmpty() && modifiedName.chars().allMatch(Character::isDigit)) {
                return Integer.parseInt(modifiedName);
            }

            return -3;
        }).filter(integer -> integer >= 0).sorted().collect(Collectors.toList());

        for (int i = 0; i < suffixList.size(); i++) {
            Integer suffix = suffixList.get(i);
            if (i == suffixList.size() - 1 || (suffix + 1) != suffixList.get(i + 1)) {
                return symbolName + (suffix + 1);
            }
        }
        return symbolName;
    }

    /**
     * Get the validated symbol name against the visible symbols.
     * This method can be used to auto generate the symbol names without conflicting with the existing symbol names
     *
     * @param context    completion context
     * @param symbolName raw symbol name to modify with the numbered suffix
     * @return {@link String} modified symbol name
     */
    public static String getValidatedSymbolName(PositionedOperationContext context, String symbolName) {
        List<Symbol> symbols = context.visibleSymbols(context.getCursorPosition());
        Set<String> visibleSymbolNames = symbols.stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        return getValidatedSymbolName(visibleSymbolNames, symbolName);
    }

    /**
     * Coverts a given text to camel case.
     *
     * @param text text to be converted.
     * @return {@link String} converted string.
     */
    private static String toCamelCase(String text) {
        String[] words = text.split("[\\W_]+");
        StringBuilder result = new StringBuilder();
        if (words.length == 1) {
            if (!StringUtils.isAllUpperCase(words[0])) {
                String word = words[0];
                word = Character.toLowerCase(word.charAt(0)) + word.substring(1);
                return word;
            }
            return words[0];
        }
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) {
                continue;
            }
            if (i == 0) {
                word = word.toLowerCase();
            } else {
                word = Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            }
            result.append(word);
        }
        return result.toString();
    }

    /**
     * Whether the given module is a langlib module.
     * public static String generateParameterName(String arg, Set<String> visibleNames) {
     * visibleNames.addAll(BALLERINA_KEYWORDS);
     * String newName = arg.replaceAll(".+[\\:\\.]", "");
     * <p>
     * <p>
     * }
     *
     * @param moduleID Module ID to evaluate
     * @return {@link Boolean} whether langlib or not
     */
    public static boolean isLangLib(ModuleID moduleID) {
        return isLangLib(moduleID.orgName(), moduleID.moduleName());
    }

    public static boolean isLangLib(String orgName, String moduleName) {
        return orgName.equals("ballerina") && moduleName.startsWith("lang.");
    }

    /**
     * Checks if the provided identifier is valid as per the ballerina specification.
     *
     * @param identifier Identifier to be checked for validity
     * @return True, if the identifier is valid as per the ballerina specification
     */
    public static boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        return identifier.matches(PatternConstants.IDENTIFIER_PATTERN);
    }

    /**
     * Escapes the escape characters present in an identifier.
     *
     * @param identifier Identifier
     * @return The identifier with escape characters escaped
     */
    public static String escapeEscapeCharsInIdentifier(String identifier) {
        return identifier.replaceAll("\\\\", "\\\\\\\\");
    }

    /**
     * Returns module prefix and process imports required.
     *
     * @param importsAcceptor import acceptor
     * @param currentModuleId current module id
     * @param moduleID        module id
     * @param context         {@link DocumentServiceContext}
     * @return module prefix
     */
    public static String getModulePrefix(ImportsAcceptor importsAcceptor, ModuleID currentModuleId,
                                         ModuleID moduleID, DocumentServiceContext context) {
        String pkgPrefix = "";
        if (!moduleID.equals(currentModuleId)) {
            boolean preDeclaredLangLib = moduleID.orgName().equals(BALLERINA_ORG_NAME) &&
                    PRE_DECLARED_LANG_LIBS.contains(moduleID.moduleName());
            String moduleName = escapeModuleName(moduleID.orgName() + "/" + moduleID.moduleName());
            String[] moduleParts = moduleName.split("/");
            String orgName = moduleParts[0];
            String alias = moduleParts[1];

            pkgPrefix = alias.replaceAll(".*\\.", "") + ":";
            pkgPrefix = (!preDeclaredLangLib && BALLERINA_KEYWORDS.contains(pkgPrefix)) ? "'" + pkgPrefix : pkgPrefix;

            // See if an alias (ex: import project.module1 as mod1) is used
            List<ImportDeclarationNode> existingModuleImports = context.currentDocImportsMap().keySet().stream()
                    .filter(importDeclarationNode ->
                            CodeActionModuleId.from(importDeclarationNode).moduleName().equals(moduleID.moduleName()))
                    .collect(Collectors.toList());

            if (existingModuleImports.size() == 1) {
                ImportDeclarationNode importDeclarationNode = existingModuleImports.get(0);
                if (importDeclarationNode.prefix().isPresent()) {
                    pkgPrefix = importDeclarationNode.prefix().get().prefix().text() + ":";
                }
            }

            if (importsAcceptor != null && !preDeclaredLangLib) {
                importsAcceptor.getAcceptor(context).accept(orgName, alias);
            }
        }
        return pkgPrefix;
    }

    public static String escapeModuleName(String qualifiedModuleName) {
        String[] moduleNameParts = qualifiedModuleName.split("/");
        if (moduleNameParts.length > 1) {
            String orgName = moduleNameParts[0];
            String alias = moduleNameParts[1];
            String[] aliasParts = moduleNameParts[1].split("\\.");
            boolean preDeclaredLangLib = BALLERINA_ORG_NAME.equals(orgName) && PRE_DECLARED_LANG_LIBS.contains(alias);
            if (aliasParts.length > 1) {
                String aliasLastPart = aliasParts[aliasParts.length - 1];
                if (CommonUtil.BALLERINA_KEYWORDS.contains(aliasLastPart) && !preDeclaredLangLib) {
                    aliasLastPart = "'" + aliasLastPart;
                }
                String aliasPart = Arrays.stream(aliasParts, 0, aliasParts.length - 1).collect(Collectors.joining());
                alias = aliasPart + "." + aliasLastPart;
            } else {
                if (CommonUtil.BALLERINA_KEYWORDS.contains(alias) && !preDeclaredLangLib) {
                    alias = "'" + alias;
                }
            }
            return orgName + "/" + alias;
        }
        return qualifiedModuleName;
    }

    /**
     * Get the path from given string URI. Even if the given URI's scheme is expr, we convert it to file scheme and
     * provide a valid Path
     *
     * @param uri file uri
     * @return {@link Optional} Path from the URI
     */
    public static Optional<Path> getPathFromURI(String uri) {
        URI fileUri = URI.create(uri);
        if (fileUri.getScheme().equals(EXPR_SCHEME)) {
            String newUri = fileUri.toString().replace(EXPR_SCHEME + ":", "file:");
            try {
                return Optional.of(Paths.get(new URL(newUri).toURI()));
            } catch (URISyntaxException | MalformedURLException e) {
                return Optional.empty();
            }
        }
        return Optional.of(Paths.get(fileUri));
    }

    /**
     * Check if the provided path should be readonly. Paths residing in ballerina home and home repo are considered as
     * such.
     *
     * @param filePath Path to be checked
     * @return True if the provided path should be readonly
     */
    public static boolean isWriteProtectedPath(Path filePath) {
        Path homeReposPath = RepoUtils.createAndGetHomeReposPath();
        Path ballerinaHome = CommonUtil.BALLERINA_HOME != null ? Paths.get(CommonUtil.BALLERINA_HOME) : null;

        return filePath.startsWith(homeReposPath) || ballerinaHome != null && filePath.startsWith(ballerinaHome);
    }

    /**
     * Check and convert the URI scheme of the provided fileUri from bala (if it's bala) to file.
     *
     * @param fileUri URI to be converted.
     * @return URI with file scheme
     * @throws URISyntaxException URI parsing errors
     */
    public static String convertUriSchemeFromBala(String fileUri) throws URISyntaxException {
        URI uri = new URI(fileUri);
        if (URI_SCHEME_BALA.equals(uri.getScheme())) {
            URI converted = new URI(URI_SCHEME_FILE, uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment());
            return converted.toString();
        }
        return fileUri;
    }

    /**
     * Get the URI with bala scheme for provided path. This method checks if the LS client supports bala scheme first.
     *
     * @param serverContext Language server context
     * @param filePath      File path
     * @return URI with bala scheme
     * @throws URISyntaxException URI creation errors
     */
    public static String getBalaUriForPath(LanguageServerContext serverContext,
                                           Path filePath) throws URISyntaxException {
        LSClientCapabilities clientCapabilities = serverContext.get(LSClientCapabilities.class);
        if (clientCapabilities.getInitializationOptions().isBalaSchemeSupported()) {
            return getBalaUriForPath(filePath);
        }
        return filePath.toUri().toString();
    }

    /**
     * Returns the URI with bala scheme for the provided file path.
     *
     * @param filePath File path
     * @return URI with bala scheme
     * @throws URISyntaxException URI parsing errors
     */
    private static String getBalaUriForPath(Path filePath) throws URISyntaxException {
        URI uri = filePath.toUri();
        uri = new URI(URI_SCHEME_BALA, uri.getUserInfo(), uri.getHost(), uri.getPort(),
                uri.getPath(), uri.getQuery(), uri.getFragment());
        return uri.toString();
    }

    /**
     * Find node of this range.
     *
     * @param range      {@link Range}
     * @param syntaxTree {@link SyntaxTree}
     * @return {@link NonTerminalNode}
     */
    public static NonTerminalNode findNode(Range range, SyntaxTree syntaxTree) {
        TextDocument textDocument = syntaxTree.textDocument();
        Position rangeStart = range.getStart();
        Position rangeEnd = range.getEnd();
        int start = textDocument.textPositionFrom(LinePosition.from(rangeStart.getLine(), rangeStart.getCharacter()));
        int end = textDocument.textPositionFrom(LinePosition.from(rangeEnd.getLine(), rangeEnd.getCharacter()));
        return ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(start, end - start), true);
    }

    /**
     * Find node of this symbol.
     *
     * @param symbol     {@link Symbol}
     * @param syntaxTree {@link SyntaxTree}
     * @return {@link NonTerminalNode}
     */
    public static Optional<NonTerminalNode> findNode(Symbol symbol, SyntaxTree syntaxTree) {
        if (symbol.getLocation().isEmpty()) {
            return Optional.empty();
        }

        TextDocument textDocument = syntaxTree.textDocument();
        LineRange symbolRange = symbol.getLocation().get().lineRange();
        int start = textDocument.textPositionFrom(symbolRange.startLine());
        int end = textDocument.textPositionFrom(symbolRange.endLine());
        return Optional.ofNullable(((ModulePartNode) syntaxTree.rootNode())
                .findNode(TextRange.from(start, end - start), true));
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
     * Check if the provided line range is within the enclosing line range.
     *
     * @param lineRange      Line range to be checked for inclusion
     * @param enclosingRange Enclosing line range in which the #lineRange reside
     * @return True if the provided line range resides within the provided enclosing line range
     */
    public static boolean isWithinLineRange(LineRange lineRange, LineRange enclosingRange) {
        Position start = CommonUtil.toPosition(lineRange.startLine());
        Position end = CommonUtil.toPosition(lineRange.endLine());
        return CommonUtil.isWithinLineRange(start, enclosingRange) && CommonUtil.isWithinLineRange(end, enclosingRange);
    }

    /**
     * Returns whether the position is within the range.
     *
     * @param pos   position
     * @param range range
     * @return True if within range, False otherwise
     */
    public static boolean isWithinRange(Position pos, Range range) {
        int sLine = range.getStart().getLine();
        int sCol = range.getStart().getCharacter();
        int eLine = range.getEnd().getLine();
        int eCol = range.getEnd().getCharacter();
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
     * @return {@link TypeSymbol} extracted type descriptor
     */
    public static TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        if (typeDescriptor.typeKind() == TypeDescKind.INTERSECTION) {
            return getRawType(((IntersectionTypeSymbol) typeDescriptor).effectiveTypeDescriptor());
        }
        if (typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeDescriptor;
            if (typeRef.typeDescriptor().typeKind() == TypeDescKind.INTERSECTION) {
                return getRawType(((IntersectionTypeSymbol) typeRef.typeDescriptor()).effectiveTypeDescriptor());
            }
            return typeRef.typeDescriptor();
        }
        return typeDescriptor;
    }

    /**
     * Get the completion item label for a given package.
     *
     * @param pkg {@link Package} package instance to evaluate
     * @return {@link String} label computed
     */
    public static String getPackageLabel(Package pkg) {
        String orgName = "";
        if (pkg.packageOrg().value() != null && !pkg.packageOrg().value().equals(Names.ANON_ORG.getValue())) {
            orgName = pkg.packageOrg().value() + "/";
        }

        return orgName + pkg.packageName().value();
    }

    /**
     * Whether the package is already imported in the current document.
     *
     * @param context completion context
     * @param pkg     Package to be evaluated against
     * @return {@link Optional}
     */
    public static Optional<ImportDeclarationNode> matchingImportedModule(CompletionContext context, Package pkg) {
        String name = pkg.packageName().value();
        String orgName = pkg.packageOrg().value();
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = context.currentDocImportsMap();
        return currentDocImports.keySet().stream()
                .filter(importPkg -> importPkg.orgName().isPresent()
                        && importPkg.orgName().get().orgName().text().equals(orgName)
                        && CommonUtil.getPackageNameComponentsCombined(importPkg).equals(name))
                .findFirst();
    }

    /**
     * Whether the package is already imported in the current document.
     *
     * @param context service operation context
     * @param orgName organization name
     * @param modName module name
     * @return {@link Optional}
     */
    public static Optional<ImportDeclarationNode> matchingImportedModule(DocumentServiceContext context, String orgName,
                                                                         String modName) {
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = context.currentDocImportsMap();
        return currentDocImports.keySet().stream()
                .filter(importPkg -> (importPkg.orgName().isEmpty()
                        || importPkg.orgName().get().orgName().text().equals(orgName))
                        && CommonUtil.getPackageNameComponentsCombined(importPkg).equals(modName))
                .findFirst();
    }

    public static String getModifiedTypeName(DocumentServiceContext context, TypeSymbol typeSymbol) {
        String typeSignature = typeSymbol.signature();
        Matcher matcher = TYPE_NAME_DECOMPOSE_PATTERN.matcher(typeSignature);
        while (matcher.find()) {
            String orgName = matcher.group(1);
            String moduleName = matcher.group(2);
            String matchedString = matcher.group();
            String modulePrefix = getModulePrefix(context, orgName, moduleName);
            String replaceText = modulePrefix.isEmpty() ? matchedString + Names.VERSION_SEPARATOR : matchedString;
            typeSignature = typeSignature.replace(replaceText, modulePrefix);
        }

        return typeSignature;
    }

    public static String getModulePrefix(DocumentServiceContext context, String orgName, String modName) {
        Project project = context.workspace().project(context.filePath()).orElseThrow();
        String currentProjectOrg = project.currentPackage().packageOrg().value();
        boolean isCurrentOrg = currentProjectOrg.equals(orgName);
        Optional<Module> currentModule = context.currentModule();
        String evalOrgName = isCurrentOrg ? "" : orgName;
        Optional<ImportDeclarationNode> matchedImport = matchingImportedModule(context, evalOrgName, modName);

        if (currentModule.isPresent() && modName.equals(getQualifiedModuleName(currentModule.get()))) {
            // If the module name is same as the current module, then return empty
            return "";
        }
        if (matchedImport.isPresent()) {
            Optional<ImportPrefixNode> prefix = matchedImport.get().prefix();
            if (prefix.isPresent()) {
                return prefix.get().prefix().text();
            }
            SeparatedNodeList<IdentifierToken> moduleComponents = matchedImport.get().moduleName();
            return moduleComponents.get(moduleComponents.size() - 1).text();
        }

        String[] modNameComponents = modName.split("\\.");
        return modNameComponents[modNameComponents.length - 1];
    }

    public static boolean isKeyword(String token) {
        return CommonUtil.BALLERINA_KEYWORDS.contains(token);
    }

    /**
     * Escape a given value.
     *
     * @param value to be escape
     * @return {@link String}
     */
    public static String escapeReservedKeyword(String value) {
        if (isKeyword(value)) {
            return "'" + value;
        }

        return value;
    }

    /**
     * Get the predicate to filter the variables.
     * These variables include
     * (1) any variable defined
     * (2) Function Parameters
     * (3) Service/ resource path parameters
     *
     * @return {@link Predicate<Symbol>}
     */
    public static Predicate<Symbol> getVariableFilterPredicate() {
        return symbol -> (symbol instanceof VariableSymbol || symbol.kind() == PARAMETER
                || symbol.kind() == SymbolKind.PATH_PARAMETER)
                && !symbol.getName().orElse("").equals(Names.ERROR.getValue());
    }

    private static String getQualifiedModuleName(Module module) {
        if (module.isDefaultModule()) {
            return module.moduleName().packageName().value();
        }
        return module.moduleName().packageName().value() + Names.DOT.getValue() + module.moduleName().moduleNamePart();
    }

    private static List<String> getBallerinaKeywords() {
        // NOTE: This is a temporary fix to retrieve lexer defined keywords until we come up with a proper api.
        // Related discussion can be found in https://github.com/ballerina-platform/ballerina-lang/discussions/28827
        try {
            Class<?> aClass = Class.forName("io.ballerina.compiler.internal.parser.LexerTerminals");
            return Arrays.stream(aClass.getDeclaredFields())
                    .filter(field -> field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                            && (field.getType() == String.class))
                    .map(field -> {
                        try {
                            return field.get(null).toString();
                        } catch (IllegalAccessException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Positioned operation context information.
     * @param node               Function call expression node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           PositionedOperationContext ctx,
                                                                           FunctionCallExpressionNode node) {
        return resolveParameterSymbol(functionTypeSymbol, ctx, node.arguments());
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Positioned operation context information.
     * @param node               Remote method call action node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           PositionedOperationContext ctx,
                                                                           RemoteMethodCallActionNode node) {
        return resolveParameterSymbol(functionTypeSymbol, ctx, node.arguments());
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Positioned operation context information.
     * @param node               Method call expression node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           PositionedOperationContext ctx,
                                                                           MethodCallExpressionNode node) {
        return resolveParameterSymbol(functionTypeSymbol, ctx, node.arguments());
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Positioned operation context information.
     * @param node               Implicit new expression node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           PositionedOperationContext ctx,
                                                                           ImplicitNewExpressionNode node) {
        Optional<ParenthesizedArgList> args = node.parenthesizedArgList();
        if (args.isEmpty()) {
            return Optional.empty();
        }
        return resolveParameterSymbol(functionTypeSymbol, ctx, args.get().arguments());
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Positioned operation context information.
     * @param node               Explicit new expression node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           PositionedOperationContext ctx,
                                                                           ExplicitNewExpressionNode node) {
        ParenthesizedArgList args = node.parenthesizedArgList();
        return resolveParameterSymbol(functionTypeSymbol, ctx, args.arguments());
    }

    /**
     * Check if the cursor is positioned in a function call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node FunctionCallExpressionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInFunctionCallParameterContext(PositionedOperationContext ctx,
                                                           FunctionCallExpressionNode node) {
        return isWithinParenthesis(ctx, node.openParenToken(), node.closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node MethodCallExpressionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInMethodCallParameterContext(PositionedOperationContext ctx,
                                                         MethodCallExpressionNode node) {
        return isWithinParenthesis(ctx, node.openParenToken(), node.closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node RemoteMethodCallActionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInMethodCallParameterContext(PositionedOperationContext ctx,
                                                         RemoteMethodCallActionNode node) {
        return isWithinParenthesis(ctx, node.openParenToken(), node.closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node RemoteMethodCallActionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInNewExpressionParameterContext(PositionedOperationContext ctx,
                                                            ImplicitNewExpressionNode node) {
        Optional<ParenthesizedArgList> argList = node.parenthesizedArgList();
        if (argList.isEmpty()) {
            return false;
        }
        return isWithinParenthesis(ctx, argList.get().openParenToken(), argList.get().closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node RemoteMethodCallActionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInNewExpressionParameterContext(PositionedOperationContext ctx,
                                                            ExplicitNewExpressionNode node) {
        ParenthesizedArgList argList = node.parenthesizedArgList();
        return isWithinParenthesis(ctx, argList.openParenToken(), argList.closeParenToken());
    }

    /**
     * Checks if the provided location (interpreted relatively to the provided module) is location in the same file
     * provided.
     *
     * @param module   Module where the location resides
     * @param location Location
     * @param filePath File path to check against
     * @return True if the location resides in the provided file path
     * @throws IOException On IO errors
     */
    public static boolean isLocationInFile(Module module, Location location, Path filePath) throws IOException {
        Path symbolPath;
        if (module.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            symbolPath = module.project().sourceRoot();
        } else if (module.isDefaultModule()) {
            symbolPath = module.project().sourceRoot().resolve(location.lineRange().filePath());
        } else {
            symbolPath = module.project().sourceRoot()
                    .resolve("modules")
                    .resolve(module.moduleName().moduleNamePart())
                    .resolve(filePath);
        }

        return Files.isSameFile(symbolPath, filePath);
    }

    /**
     * Check if the symbol is a class symbol with self as the name.
     *
     * @param symbol               Symbol
     * @param context              PositionedOperationContext
     * @param enclosedModuleMember ModuleMemberDeclarationNode
     * @return {@link Boolean} whether the symbol is a self class symbol.
     */
    public static boolean isSelfClassSymbol(Symbol symbol, PositionedOperationContext context,
                                            @Nonnull ModuleMemberDeclarationNode enclosedModuleMember) {
        Optional<String> name = symbol.getName();
        if (enclosedModuleMember.kind() != SyntaxKind.CLASS_DEFINITION || symbol.kind() != SymbolKind.VARIABLE
                || name.isEmpty() || !name.get().equals(SELF_KW)) {
            return false;
        }

        Optional<Symbol> memberSymbol = context.workspace().semanticModel(context.filePath())
                .flatMap(semanticModel -> semanticModel.symbol(enclosedModuleMember));

        if (memberSymbol.isEmpty() || memberSymbol.get().kind() != SymbolKind.CLASS) {
            return false;
        }
        ClassSymbol classSymbol = (ClassSymbol) memberSymbol.get();
        VariableSymbol selfSymbol = (VariableSymbol) symbol;
        TypeSymbol varTypeSymbol = CommonUtil.getRawType(selfSymbol.typeDescriptor());

        return classSymbol.equals(varTypeSymbol);
    }

    /**
     * Check if the cursor is positioned in a lock statement node context.
     *
     * @param context Completion context.
     * @return {@link Boolean} Whether the cursor is in lock statement node context.
     */
    public static Boolean withinLockStatementNode(BallerinaCompletionContext context) {
        NonTerminalNode evalNode = context.getNodeAtCursor();
        do {
            if (evalNode.kind() == SyntaxKind.LOCK_STATEMENT) {
                return true;
            }
            evalNode = evalNode.parent();
        }
        while (evalNode != null);
        return false;
    }

    /**
     * Get the common predicate to filter the types.
     *
     * @return {@link Predicate}
     */
    public static Predicate<Symbol> typesFilter() {
        // Specifically remove the error type, since this is covered with langlib suggestion and type builtin types
        return symbol -> (symbol.kind() == SymbolKind.TYPE_DEFINITION ||
                symbol.kind() == SymbolKind.CLASS || symbol.kind() == SymbolKind.ENUM
                || symbol.kind() == SymbolKind.ENUM_MEMBER || symbol.kind() == SymbolKind.CONSTANT)
                && !Names.ERROR.getValue().equals(symbol.getName().orElse(""));
    }

    /**
     * Provided a set of arguments and parameters, returns the list of argument names that has been already defined.
     *
     * @param context          Completion context.
     * @param params           List of expected parameter symbols.
     * @param argumentNodeList Argument list.
     * @return {@link List<String>} already defined argument names.
     */
    public static List<String> getDefinedArgumentNames(BallerinaCompletionContext context,
                                                       List<ParameterSymbol> params,
                                                       SeparatedNodeList<FunctionArgumentNode> argumentNodeList) {
        List<String> existingArgNames = new ArrayList<>();
        int cursorPosition = context.getCursorPositionInTree();
        int index = 1;
        for (Node child : argumentNodeList) {
            TextRange textRange = child.textRange();
            int startOffset = textRange.startOffset();
            int endOffset = textRange.endOffset();
            if ((startOffset > cursorPosition || endOffset < cursorPosition)) {
                if (child.kind() == SyntaxKind.NAMED_ARG) {
                    existingArgNames.add(((NamedArgumentNode) child).argumentName().name().text());
                } else if (child.kind() == SyntaxKind.POSITIONAL_ARG && index - 1 < params.size()) {
                    ParameterSymbol parameterSymbol = params.get(index - 1);
                    existingArgNames.add(parameterSymbol.getName().orElse(""));
                }
            }
            index++;
        }
        return existingArgNames;
    }

    /**
     * Provided a node, returns the list of possible qualifiers of that node.
     *
     * @param node node.
     * @return {@link List<Token>} qualifiers list.
     */
    public static List<Token> getQualifiersOfNode(BallerinaCompletionContext context, Node node) {
        List<Token> qualifiers = new ArrayList<>();
        switch (node.kind()) {
            case FUNCTION_TYPE_DESC:
                ((FunctionTypeDescriptorNode) node).qualifierList().stream().forEach(qualifiers::add);
                break;
            case OBJECT_TYPE_DESC:
                ((ObjectTypeDescriptorNode) node).objectTypeQualifiers().stream().forEach(qualifiers::add);
                break;
            case OBJECT_FIELD:
                ObjectFieldNode objectFieldNode = (ObjectFieldNode) node;
                objectFieldNode.visibilityQualifier().ifPresent(qualifiers::add);
                objectFieldNode.qualifierList().stream().forEach(qualifiers::add);
                break;
            case MODULE_VAR_DECL:
                ModuleVariableDeclarationNode moduleVar = (ModuleVariableDeclarationNode) node;
                Optional<Token> visibilityQualifier = moduleVar.visibilityQualifier();
                visibilityQualifier.ifPresent(qualifiers::add);
                moduleVar.qualifiers().forEach(qualifiers::add);
                Set<SyntaxKind> qualKinds = qualifiers.stream().map(Node::kind).collect(Collectors.toSet());
                getQualifiersAtCursor(context).stream()
                        .filter(qual -> !qualKinds.contains(qual.kind())).forEach(qualifiers::add);

                //Add leading invalid tokens of type binding pattern if there are no visible qualifiers.
                if (qualifiers.isEmpty()) {
                    moduleVar.typedBindingPattern().leadingInvalidTokens().stream()
                            .filter(token -> QUALIFIER_KINDS.contains(token.kind())).forEach(qualifiers::add);
                }
                break;
            case MODULE_PART:
                List<Token> qualsAtCursor = getQualifiersAtCursor(context);
                Set<SyntaxKind> foundQuals = qualifiers.stream().map(Node::kind).collect(Collectors.toSet());
                context.getNodeAtCursor().leadingInvalidTokens().stream()
                        .filter(token -> QUALIFIER_KINDS.contains(token.kind())
                                && !foundQuals.contains(token.kind())).forEach(qualifiers::add);
                qualifiers.addAll(qualsAtCursor);
                return qualifiers;
            default:
        }
        //Qualifiers are identified as invalid tokens by the parser in some cases.
        Set<SyntaxKind> qualKinds = qualifiers.stream().map(Node::kind).collect(Collectors.toSet());
        node.leadingInvalidTokens().stream()
                .filter(token -> QUALIFIER_KINDS.contains(token.kind())
                        && !qualKinds.contains(token.kind())).forEach(qualifiers::add);
        return qualifiers;
    }

    /**
     * Get the qualifiers of the module part context node.
     *
     * @param context completion context.
     * @return {@link List<Token> } the list of qualifiers.
     */
    public static List<Token> getQualifiersAtCursor(BallerinaCompletionContext context) {
        List<Token> qualifiers = new ArrayList<>();
        Token tokenAtCursor = context.getTokenAtCursor();
        if (CommonUtil.QUALIFIER_KINDS.contains(tokenAtCursor.kind())) {
            qualifiers.add(tokenAtCursor);
            return qualifiers;
        }
        List<Minutiae> tokensFromMinutiae = new ArrayList<>();
        context.getTokenAtCursor().leadingMinutiae().forEach(minutiae -> {
            if (minutiae.kind() != SyntaxKind.WHITESPACE_MINUTIAE
                    && minutiae.kind() != SyntaxKind.END_OF_LINE_MINUTIAE) {
                tokensFromMinutiae.add(minutiae);
            }
        });
        if (tokensFromMinutiae.isEmpty()) {
            return qualifiers;
        }
        Minutiae tokenValueAtCursor = tokensFromMinutiae.get(tokensFromMinutiae.size() - 1);
        tokenValueAtCursor.invalidTokenMinutiaeNode().ifPresent(invalidTokenMinutiaeNode -> {
            Token token = invalidTokenMinutiaeNode.invalidToken();
            if (CommonUtil.QUALIFIER_KINDS.contains(token.kind())) {
                qualifiers.add(token);
            }
        });
        return qualifiers;
    }

    private static boolean isWithinParenthesis(PositionedOperationContext ctx, Token openParen, Token closedParen) {
        int cursorPosition = ctx.getCursorPositionInTree();
        return (!openParen.isMissing())
                && (openParen.textRange().endOffset() <= cursorPosition)
                && (!closedParen.isMissing())
                && (cursorPosition <= closedParen.textRange().startOffset());
    }

    private static Optional<ParameterSymbol> resolveParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                    PositionedOperationContext ctx,
                                                                    SeparatedNodeList<FunctionArgumentNode> arguments) {
        int cursorPosition = ctx.getCursorPositionInTree();
        int argIndex = -1;
        for (Node child : arguments) {
            if (child.textRange().endOffset() < cursorPosition) {
                argIndex += 1;
            }
        }
        Optional<List<ParameterSymbol>> params = functionTypeSymbol.params();
        if (params.isEmpty() || params.get().size() < argIndex + 2) {
            return Optional.empty();
        }
        return Optional.of(params.get().get(argIndex + 1));
    }

    /**
     * Check if the cursor is positioned in call expression context so that named arg
     * completions can be suggested.
     *
     * @param context          completion context.
     * @param argumentNodeList argument node list.
     * @return {@link Boolean} whether the cursor is positioned so that the named arguments can  be suggested.
     */
    public static boolean isValidNamedArgContext(BallerinaCompletionContext context,
                                                 SeparatedNodeList<FunctionArgumentNode> argumentNodeList) {
        int cursorPosition = context.getCursorPositionInTree();
        for (Node child : argumentNodeList) {
            TextRange textRange = child.textRange();
            int startOffset = textRange.startOffset();
            if (startOffset > cursorPosition
                    && child.kind() == SyntaxKind.POSITIONAL_ARG || child.kind() == SyntaxKind.REST_ARG) {
                return false;
            }
        }
        return true;
    }
    
    public static String getModifiedUri(WorkspaceManager workspaceManager, String uri) {
        URI original = URI.create(uri);
        try {
            return new URI(workspaceManager.uriScheme(),
                    original.getSchemeSpecificPart(),
                    original.getFragment()).toString();
        } catch (URISyntaxException e) {
            return uri;
        }
    }
}

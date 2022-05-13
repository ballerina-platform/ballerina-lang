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
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
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

    public static final String SDK_VERSION = System.getProperty("ballerina.version");

    public static final String EXPR_SCHEME = "expr";
    
    public static final List<String> BALLERINA_KEYWORDS = SyntaxInfo.keywords();

    public static final Set<SyntaxKind> QUALIFIER_KINDS = Set.of(SyntaxKind.SERVICE_KEYWORD,
            SyntaxKind.CLIENT_KEYWORD, SyntaxKind.ISOLATED_KEYWORD, SyntaxKind.TRANSACTIONAL_KEYWORD,
            SyntaxKind.PUBLIC_KEYWORD, SyntaxKind.PRIVATE_KEYWORD);

    private static final Pattern TYPE_NAME_DECOMPOSE_PATTERN = Pattern.compile("([\\w_.]*)/([\\w._]*):([\\w.-]*)");

    static {
        BALLERINA_HOME = System.getProperty("ballerina.home");
        String onlineCompilation = System.getProperty("ls.compilation.online");
        COMPILE_OFFLINE = !Boolean.parseBoolean(onlineCompilation);
        BALLERINA_CMD = BALLERINA_HOME + File.separator + "bin" + File.separator + "bal" +
                (SystemUtils.IS_OS_WINDOWS ? ".bat" : "");
    }

    private CommonUtil() {
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
            builder.append(" as ").append(alias);
        }
        builder.append(SEMI_COLON_SYMBOL_KEY).append(CommonUtil.LINE_SEPARATOR);

        return Collections.singletonList(new TextEdit(new Range(start, start), builder.toString()));
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
     * Given a prefix and visible symbols, this method will return a type name by appending a number to the end.
     *
     * @param prefix             Type name prefix
     * @param visibleSymbolNames Visible symbols
     * @return Generated type name
     */
    public static String generateTypeName(String prefix, Set<String> visibleSymbolNames) {
        String typeName = prefix;
        int idx = 0;
        while (visibleSymbolNames.contains(typeName)) {
            idx++;
            typeName = typeName + idx;
        }

        return typeName;
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
        if (arg.isEmpty() || !SyntaxInfo.isIdentifier(arg)) {
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
     * Escapes the escape characters present in an identifier.
     *
     * @param identifier Identifier
     * @return The identifier with escape characters escaped
     */
    public static String escapeEscapeCharsInIdentifier(String identifier) {
        return identifier.replaceAll("\\\\", "\\\\\\\\");
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
     * Check if the provided union type is a union of members of provided type desc kind.
     *
     * @param typeSymbol Union type symbol
     * @param typeDescKind    Type desc kind
     * @return true if provided union contains members of provided type desc kind
     */
    public static boolean isUnionOfType(TypeSymbol typeSymbol, TypeDescKind typeDescKind) {
        return typeSymbol.typeKind() == TypeDescKind.UNION &&
                ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors().stream()
                        .map(CommonUtil::getRawType)
                        .map(TypeSymbol::typeKind)
                        .allMatch(kind -> kind == typeDescKind);
    }

    /**
     * Given a type symbol, this method will get the record type symbols in the provided type. i.e. the provided type
     * can be a record or a union of records for this to work.
     *
     * @param typeSymbol Type symbol from which record types need to be extracted.
     * @return List of record type symbols, wrapped with a raw type and broader type container.
     */
    public static List<RawTypeSymbolWrapper<RecordTypeSymbol>> getRecordTypeSymbols(TypeSymbol typeSymbol) {
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);
        if (rawType.typeKind() == TypeDescKind.RECORD) {
            return Collections.singletonList(RawTypeSymbolWrapper.from(typeSymbol, (RecordTypeSymbol) rawType));
        }
        if (rawType.typeKind() == TypeDescKind.UNION) {
            // This will only consider the record type members and disregard other types
            return ((UnionTypeSymbol) rawType).memberTypeDescriptors().stream()
                    .filter(tSymbol -> CommonUtil.getRawType(tSymbol).typeKind() == TypeDescKind.RECORD)
                    .map(tSymbol -> {
                        RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) CommonUtil.getRawType(tSymbol);
                        return RawTypeSymbolWrapper.from(tSymbol, recordTypeSymbol);
                    }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Get the completion item label for a given package.
     *
     * @param pkg {@link Package} package info to evaluate
     * @return {@link String} label computed
     */
    public static String getPackageLabel(LSPackageLoader.PackageInfo pkg) {
        String orgName = "";
        if (pkg.packageOrg().value() != null && !pkg.packageOrg().value().equals(Names.ANON_ORG.getValue())) {
            orgName = pkg.packageOrg().value() + "/";
        }

        return orgName + pkg.packageName().value();
    }

    /**
     * Returns the type name (derived from signature) with version infromation removed.
     *
     * @param context    Context
     * @param typeSymbol Type symbol
     * @return Signature
     */
    public static String getModifiedTypeName(DocumentServiceContext context, TypeSymbol typeSymbol) {
        String typeSignature = typeSymbol.signature();
        return getModifiedSignature(context, typeSignature);
    }

    /**
     * Given a signature, this method will remove the version information from the signature.
     *
     * @param context   Context
     * @param signature Signature to be modified.
     * @return Modified signature
     */
    public static String getModifiedSignature(DocumentServiceContext context, String signature) {
        Matcher matcher = TYPE_NAME_DECOMPOSE_PATTERN.matcher(signature);
        while (matcher.find()) {
            String orgName = matcher.group(1);
            String moduleName = matcher.group(2);
            String matchedString = matcher.group();
            String modulePrefix = ModuleOperationUtil.getModulePrefix(context, orgName, moduleName);
            String replaceText = modulePrefix.isEmpty() ? matchedString + Names.VERSION_SEPARATOR : matchedString;
            signature = signature.replace(replaceText, modulePrefix);
        }

        return signature;
    }

    /**
     * Escape a given value.
     *
     * @param value to be escape
     * @return {@link String}
     */
    public static String escapeReservedKeyword(String value) {
        if (SyntaxInfo.isKeyword(value)) {
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

    /**
     * Check whether the completion item type belongs to the types list passed.
     *
     * @param lsCItem   Completion item
     * @param typesList List of types
     * @return {@link Boolean}
     */
    public static boolean isCompletionItemOfType(LSCompletionItem lsCItem, List<TypeDescKind> typesList) {

        if (lsCItem.getType() != LSCompletionItem.CompletionItemType.SYMBOL) {
            return false;
        }

        Symbol symbol = ((SymbolCompletionItem) lsCItem).getSymbol().orElse(null);
        Optional<TypeSymbol> typeDesc = SymbolUtil.getTypeDescriptor(symbol);

        if (typeDesc.isPresent()) {
            TypeSymbol rawType = CommonUtil.getRawType(typeDesc.get());
            return typesList.contains(rawType.typeKind());
        }
        return false;
    }

    /**
     * Given a node (currentNode) start looking up the parent ladder until the given predicate is satisfied.
     *
     * @param currentNode Node to start looking
     * @param predicate   to be satisfied
     * @return {@link Optional}
     */
    public static Optional<Node> getMatchingNode(Node currentNode, Predicate<Node> predicate) {
        Node evalNode = currentNode;
        while (evalNode != null && !predicate.test(evalNode)) {
            evalNode = evalNode.parent();
        }

        return Optional.ofNullable(evalNode);
    }

    /**
     * Check if a given offset is with in the range of a given node.
     *
     * @param node
     * @param offset
     * @return
     */
    public static boolean isWithInRange(Node node, int offset) {
        return node.textRange().startOffset() <= offset && offset <= node.textRange().endOffset();
    }

    /**
     * Get the list of function arguments from the invokable symbol.
     *
     * @param symbol Invokable symbol to extract the arguments
     * @param ctx    Lang Server Operation context
     * @return {@link List} List of arguments
     */
    public static List<String> getFuncArguments(FunctionSymbol symbol, BallerinaCompletionContext ctx) {
        List<ParameterSymbol> params = CommonUtil.getFunctionParameters(symbol, ctx);
        return params.stream().map(param -> getFunctionParamaterSyntax(param, ctx).orElse(""))
                .collect(Collectors.toList());
    }

    /**
     * Get the function parameter syntax given the parameter symbol.
     *
     * @param param parameter symbol.
     * @param ctx   Lang Server Operation context
     * @return {@link Optional<String>} Type and name syntax string.
     */
    public static Optional<String> getFunctionParamaterSyntax(ParameterSymbol param, BallerinaCompletionContext ctx) {

        if (param.paramKind() == ParameterKind.REST) {
            ArrayTypeSymbol typeSymbol = (ArrayTypeSymbol) param.typeDescriptor();
            return Optional.of(CommonUtil.getModifiedTypeName(ctx, typeSymbol.memberTypeDescriptor())
                    + (param.getName().isEmpty() ? "" : "... "
                    + param.getName().get()));
        }

        if (param.typeDescriptor().typeKind() == TypeDescKind.COMPILATION_ERROR) {
            // Invalid parameters are ignored, but empty string is used to indicate there's a parameter
            return Optional.empty();
        } else {
            return Optional.of(CommonUtil.getModifiedTypeName(ctx, param.typeDescriptor()) +
                    (param.getName().isEmpty() ? "" : " " + param.getName().get()));
        }
    }

    /**
     * Get the list of function parameters from the invokable symbol.
     *
     * @param symbol Invokable symbol to extract the parameters
     * @param ctx    Lang Server Operation context
     * @return {@link List<ParameterSymbol> } list of parameter symbols.
     */
    public static List<ParameterSymbol> getFunctionParameters(FunctionSymbol symbol, BallerinaCompletionContext ctx) {
        boolean skipFirstParam = skipFirstParam(ctx, symbol);
        FunctionTypeSymbol functionTypeDesc = symbol.typeDescriptor();
        Optional<ParameterSymbol> restParam = functionTypeDesc.restParam();
        List<ParameterSymbol> parameterDefs = new ArrayList<>();

        if (functionTypeDesc.params().isPresent()) {
            List<ParameterSymbol> params = functionTypeDesc.params().get();
            if (skipFirstParam) {
                if (params.size() > 1) {
                    parameterDefs.addAll(params.subList(1, params.size()));
                }
            } else {
                parameterDefs.addAll(params);
            }
        }
        restParam.ifPresent(parameterDefs::add);
        return parameterDefs;
    }

    /**
     * Whether we skip the first parameter being included as a label in the signature.
     * When showing a lang lib invokable symbol over DOT(invocation) we do not show the first param, but when we
     * showing the invocation over package of the langlib with the COLON we show the first param.
     * <p>
     * When the langlib function is retrieved from the Semantic API, those functions are filtered where the first param
     * type not being same as the langlib type. Hence we need to chek whether the function is from a langlib.
     *
     * @param context        context
     * @param functionSymbol invokable symbol
     * @return {@link Boolean} whether we show the first param or not
     */
    public static boolean skipFirstParam(BallerinaCompletionContext context, FunctionSymbol functionSymbol) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        return CommonUtil.isLangLib(functionSymbol.getModule().get().id())
                && nodeAtCursor.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE;
    }
}

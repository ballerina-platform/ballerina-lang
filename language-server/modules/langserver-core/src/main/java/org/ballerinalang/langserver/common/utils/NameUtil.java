/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.TextRange;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.PositionedOperationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Carries a set of utilities used for operations on names of variables, types, parameters, visible symbols.
 *
 * @since 2201.1.1
 */
public class NameUtil {

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
        while (visibleSymbolNames.contains(typeName) || CommonUtil.BALLERINA_KEYWORDS.contains(typeName)) {
            idx++;
            typeName = prefix + idx;
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
     * Returns the type name (derived from signature) with version information removed.
     *
     * @param context    Context
     * @param typeSymbol Type symbol
     * @return Signature
     */
    public static String getModifiedTypeName(DocumentServiceContext context, TypeSymbol typeSymbol) {
        String typeSignature = typeSymbol.signature();
        return CommonUtil.getModifiedSignature(context, typeSignature);
    }

    /**
     * Returns the record type name.
     *
     * @param context Context
     * @param wrapper A wrapper containing record type symbol and broader type symbol
     * @return Record type
     */
    public static String getRecordTypeName(DocumentServiceContext context,
                                           RawTypeSymbolWrapper<RecordTypeSymbol> wrapper) {
        if (wrapper.getRawType().getName().isPresent()) {
            return NameUtil.getModifiedTypeName(context, wrapper.getRawType());
        } else if (wrapper.getBroaderType().getName().isPresent()) {
            return NameUtil.getModifiedTypeName(context, wrapper.getBroaderType());
        } else {
            return wrapper.getRawType().signature();
        }
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

    private static String generateVariableName(int suffix, String name, Set<String> names) {
        names.addAll(CommonUtil.BALLERINA_KEYWORDS);
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
}

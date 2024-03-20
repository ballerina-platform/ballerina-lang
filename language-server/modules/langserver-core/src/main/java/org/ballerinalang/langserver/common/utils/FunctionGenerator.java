/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Module;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.codeaction.CodeActionModuleId;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Function generator utilities.
 */
public class FunctionGenerator {

    public static final Pattern FULLY_QUALIFIED_MODULE_ID_PATTERN =
            Pattern.compile("([\\w]+)\\/([\\w.]+):([^:]+):([\\w]+)[\\|]?");

    /**
     * Returns signature of the provided type.
     *
     * @param importsAcceptor imports acceptor
     * @param typeDescriptor  {@link BLangNode}
     * @param context         {@link DocumentServiceContext}
     * @return return type signature
     */
    public static String generateTypeSignature(ImportsAcceptor importsAcceptor,
                                               TypeSymbol typeDescriptor, DocumentServiceContext context) {
        return processModuleIDsInText(importsAcceptor, typeDescriptor.signature(), context);
    }

    /**
     * Returns imports processed of the type text.
     *
     * @param importsAcceptor imports acceptor
     * @param text            generated type text
     * @param context         {@link DocumentServiceContext}
     * @return return type signature
     */
    public static String processModuleIDsInText(ImportsAcceptor importsAcceptor, String text,
                                                DocumentServiceContext context) {
        Module module = context.workspace().module(context.filePath()).orElseThrow();
        String currentOrg = module.packageInstance().descriptor().org().value();
        String currentModule = module.descriptor().name().toString();
        String currentVersion = module.packageInstance().descriptor().version().value().toString();

        StringBuilder newText = new StringBuilder();
        ModuleID currentModuleID = CodeActionModuleId.from(currentOrg, currentModule, currentVersion);
        Matcher matcher = FULLY_QUALIFIED_MODULE_ID_PATTERN.matcher(text);
        int nextStart = 0;
        // Matching Fully-Qualified-Module-IDs (eg.`abc/mod1:1.0.0`)
        // Purpose is to transform `int|abc/mod1:1.0.0:Person` into `int|mod1:Person` or `int|Person`
        // base on the current module id and identifying the potential imports required.
        while (matcher.find()) {
            // Append up-to start of the match
            newText.append(text, nextStart, matcher.start(1));
            // Append module prefix(empty when in same module) and identify imports
            ModuleID moduleID = CodeActionModuleId.from(matcher.group(1), matcher.group(2), matcher.group(3));
            newText.append(ModuleUtil.getModulePrefix(importsAcceptor, currentModuleID, moduleID, context));
            // Update next-start position
            nextStart = matcher.end(3) + 1;
        }
        // Append the remaining
        if (nextStart != 0) {
            newText.append(text.substring(nextStart));
        }
        return newText.length() > 0 ? newText.toString() : text;
    }

    /**
     * Given a text like "ballerina/module1:0.1.0:Response", this will output "module1:Response" after filtering the
     * version and org information from the full qualified module name.
     *
     * @param text Text to be processed
     * @return Processed text
     */
    public static String processModuleIDsInText(String text) {
        StringBuilder newText = new StringBuilder();
        Matcher matcher = FULLY_QUALIFIED_MODULE_ID_PATTERN.matcher(text);
        int nextStart = 0;
        while (matcher.find()) {
            // Append up-to start of the match
            newText.append(text, nextStart, matcher.start(1));

            String modPart = matcher.group(2);
            int last = modPart.lastIndexOf(".");
            if (last != -1) {
                modPart = modPart.substring(last + 1);
            }

            String typeName = matcher.group(4);

            newText.append(modPart);
            newText.append(":");
            newText.append(typeName);
            // Update next-start position
            nextStart = matcher.end(4);
        }
        // Append the remaining
        if (nextStart != 0 && nextStart < text.length()) {
            newText.append(text.substring(nextStart));
        }
        return newText.length() > 0 ? newText.toString() : text;
    }

    /**
     * Generates a function with the provided parameters.
     *
     * @param context          Document service context
     * @param newLineAtStart   Whether to add an additional newline at the beginning of the function.
     * @param functionName     Name of the created function
     * @param args             Function arguments as a string list
     * @param returnTypeSymbol return type of the function
     * @return Created function as a string
     */
    public static String generateFunction(DocumentServiceContext context, boolean newLineAtStart, String functionName,
                                          List<String> args, TypeSymbol returnTypeSymbol) {
        return generateFunction(context, newLineAtStart, functionName, args, returnTypeSymbol, false);
    }

    /**
     * Generates a function with the provided parameters.
     *
     * @param context          Document service context
     * @param newLineAtEnd     Whether to add an additional newline at the end of the function.
     * @param functionName     Name of the created function
     * @param args             Function arguments as a string list
     * @param returnTypeSymbol return type of the function
     * @param isolated         Whether the created function should be prefixed with isolated qualifier
     * @return Created function as a string
     */
    public static String generateFunction(DocumentServiceContext context, boolean newLineAtEnd, String functionName,
                                          List<String> args, TypeSymbol returnTypeSymbol, boolean isolated) {
        String returnType = null;
        if (returnTypeSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR
                && returnTypeSymbol.typeKind() != TypeDescKind.NIL) {
            returnType = FunctionGenerator.getReturnTypeAsString(context, returnTypeSymbol.signature());
        }

        String returnsClause = "";
        String returnStmt = "";
        if (returnType != null) {
            // returns clause
            returnsClause = "returns " + returnType;
            // return statement
            Optional<String> defaultReturnValue = DefaultValueGenerationUtil.getDefaultValueForType(returnTypeSymbol);
            if (defaultReturnValue.isPresent()) {
                returnStmt = "return " + defaultReturnValue.get() + CommonKeys.SEMI_COLON_SYMBOL_KEY;
            }
        }

        return generateFunction(functionName, args, returnsClause, returnStmt, newLineAtEnd, isolated, "");
    }

    /**
     * Generate a function provided the function name, args, return clause and return statement.
     *
     * @param functionName  Function name
     * @param args          Function parameters
     * @param returnsClause Returns clause
     * @param returnStmt    Return statement
     * @param newLineAtEnd  Whether to add a new line at the end of the function
     * @param isolated      Whether the created function should be prefixed with isolated qualifier
     * @return Created function
     */
    public static String generateFunction(String functionName, List<String> args, String returnsClause,
                                          String returnStmt, boolean newLineAtEnd, boolean isolated,
                                          String funcBodyExcludingRetStmt) {
        // padding
        int padding = 4;
        String paddingStr = StringUtils.repeat(" ", padding);

        // body
        String body = "";

        if (!funcBodyExcludingRetStmt.isEmpty()) {
            body += funcBodyExcludingRetStmt;
        }

        if (!returnStmt.isEmpty()) {
            body += paddingStr + returnStmt + CommonUtil.LINE_SEPARATOR;
        } else if (funcBodyExcludingRetStmt.isEmpty()) {
            body += paddingStr + CommonUtil.LINE_SEPARATOR;
        }

        StringBuilder fnBuilder = new StringBuilder();
        if (!functionName.isEmpty()) {
            fnBuilder.append(CommonUtil.LINE_SEPARATOR).append(CommonUtil.LINE_SEPARATOR);
        }

        if (isolated) {
            fnBuilder.append("isolated ");
        }

        fnBuilder.append("function").append(" ").append(functionName)
                .append(CommonKeys.OPEN_PARENTHESES_KEY)
                .append(String.join(", ", args))
                .append(CommonKeys.CLOSE_PARENTHESES_KEY);

        if (!returnsClause.isEmpty()) {
            fnBuilder.append(" ").append(returnsClause);
        }

        fnBuilder.append(" ").append(CommonKeys.OPEN_BRACE_KEY)
                .append(CommonUtil.LINE_SEPARATOR)
                .append(body)
                .append(CommonKeys.CLOSE_BRACE_KEY);

        if (!functionName.isEmpty() && newLineAtEnd) {
            fnBuilder.append(CommonUtil.LINE_SEPARATOR);
        }

        return fnBuilder.toString();
    }

    /**
     * Generates a unique function name based on the provided function prefix and the list of visible symbols.
     * The function name is generated in such a way that it does not conflict with any of the visible symbol names.
     *
     * @param functionPrefix The prefix to be used for the function name.
     * @param visibleSymbols The list of visible symbols in the current scope.
     * @return A unique function name.
     */
    public static String generateFunctionName(String functionPrefix, List<Symbol> visibleSymbols) {
        Set<String> visibleSymbolNames = visibleSymbols.stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        return NameUtil.generateTypeName(functionPrefix, visibleSymbolNames);
    }

    /**
     * Converts the provided type signature which is to be used as the return type (ex: in a function definition).
     *
     * @param context   Context
     * @param signature Signature of the return type symbol to be converted to a string
     * @return Return type as string
     */
    public static String getReturnTypeAsString(DocumentServiceContext context, String signature) {
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        return processModuleIDsInText(importsAcceptor, signature, context);
    }

    /**
     * Converts the provided type symbol to a type name which will be used as a type of a function parameter.
     *
     * @param context    Context
     * @param typeSymbol Type symbol to be converted to a string
     * @return Type as a string
     */
    public static String getParameterTypeAsString(DocumentServiceContext context, TypeSymbol typeSymbol) {
        // Unknown types are considered as 'any' when generating parameters of a function
        if (typeSymbol == null || typeSymbol.typeKind() == TypeDescKind.COMPILATION_ERROR) {
            return TypeDescKind.ANY.getName();
        }

        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        return processModuleIDsInText(importsAcceptor, typeSymbol.signature(), context);
    }
}

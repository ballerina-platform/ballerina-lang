/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.invoker.classload;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.utils.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses a type to find exported types and add required implicit imports.
 */
public class TypeSignatureParser {
    /**
     * The regular expression used to parse the exported type.
     * Parsing format: org name    /   module name                 : version   :   type name
     * Formatted Vers: [ identifier/ ] identifier (. identifier)*  : [ {0-9.}* : ] identifier
     * Reg exp groups: (eg: ballerina/module1.module2.module3:2.2:type)
     * [0] exported type match (ballerina/module1.module2.module3:2.2:type)
     * [1] org name with slash (ballerina/)
     * [2] all module names sep with dot (module1.module2.module3)
     * [3] first module name (module1)
     * [4] last module name (module)
     * [5] type (type)
     */
    protected static final Pattern EXPORTED_NAME =
            Pattern.compile("" +
                    "([a-zA-Z0-9_$']+/)?" + // org name
                    "(([a-zA-Z0-9_']*)(?:\\.([a-zA-Z0-9_']*))*):" + // module name
                    "(?:[0-9.]*:)?" + // version
                    "([a-zA-Z0-9_']+)" // type name
            );

    private final ImportProcessor importProcessor;

    private final AtomicReference<String> exportedType;
    private final Set<String> implicitImportPrefixes;

    public TypeSignatureParser(ImportProcessor importProcessor) {
        this.exportedType = new AtomicReference<>("any|error");
        this.implicitImportPrefixes = new HashSet<>();
        this.importProcessor = importProcessor;
    }

    /**
     * Formats the type signature so that it can be used as a typedef.
     * Required implicit imports are added in this. Also empty <> are fixed.
     *
     * @param typeSymbol Type symbol to parse.
     */
    public void process(TypeSymbol typeSymbol) {
        String exportFormattedType = EXPORTED_NAME.matcher(typeSymbol.signature())
                .replaceAll((this::processExportedType));
        String xmlNeverFormattedType = exportFormattedType.replace("xml<>", "xml<never>");
        String anyDataFormattedType = xmlNeverFormattedType.replace("anydata...;", "");
        this.exportedType.set(anyDataFormattedType);
    }

    /**
     * Formats the type signature so that it can be used as a typedef. For example, int will be formatted to int.
     * ballerina/abc:1.0:pqr will be converted to 'imp1:pqr and an import added as import ballerina/abc.pqr as 'imp1.
     *
     * @param matchResult Match result of a type in format.
     */
    private String processExportedType(MatchResult matchResult) {
        // Find required information to create import
        String orgName = StringUtils.quoted(matchResult.group(1));
        String[] rawModuleNames = matchResult.group(2).split("\\.");
        String defaultPrefix = StringUtils.quoted(matchResult.group(4));
        // TODO: Check if type name is visible
        String typeName = matchResult.group(5);

        String moduleName = Arrays.stream(rawModuleNames)
                .map(StringUtils::quoted)
                .collect(Collectors.joining("."));

        // If org name is anonymous, this is a declared type.
        if (orgName.equals("'$anon/")) {
            return typeName;
        }

        // Create import snippet and find prefix using processor
        String fullModuleName = orgName + moduleName;
        try {
            String importPrefix = importProcessor.processImplicitImport(fullModuleName, defaultPrefix);
            implicitImportPrefixes.add(importPrefix);
            return String.format("%s:%s", importPrefix, typeName);
        } catch (InvokerException e) {
            throw new RuntimeException(e);
        }
    }

    public String getExportedType() {
        return exportedType.get();
    }

    public Collection<String> getImplicitImportPrefixes() {
        return implicitImportPrefixes;
    }
}

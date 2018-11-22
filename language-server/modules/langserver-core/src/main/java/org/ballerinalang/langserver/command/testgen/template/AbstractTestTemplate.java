/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.command.testgen.template;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This class provides shared functionalities across Ballerina Test Templates.
 *
 * @since 0.985.0
 */
public abstract class AbstractTestTemplate implements TestTemplate {
    protected static final String DEFAULT_IP = "0.0.0.0";
    protected static final String DEFAULT_PORT = "9092";
    protected static final String HTTP = "http://";
    protected static final String HTTPS = "https://";
    protected static final String WS = "ws://";
    protected static final String WSS = "wss://";
    protected BLangPackage builtTestFile;
    protected List<Pair<String, String>> imports;

    public AbstractTestTemplate(BLangPackage builtTestFile) {
        this.builtTestFile = builtTestFile;
        this.imports = new ArrayList<>();
        if (builtTestFile != null) {
            builtTestFile.testablePkgs.forEach(testablePkg -> testablePkg.getCompilationUnits().forEach(
                    unit -> unit.getTopLevelNodes().forEach(node -> {
                        //TODO: a dirty hack to retrieve imports of a test package, remove this when fixed
                        if (node instanceof BLangImportPackage) {
                            BLangImportPackage pkg = (BLangImportPackage) node;
                            String orgName = pkg.orgName.value;
                            String alias = pkg.alias.value;
                            this.imports.add(new ImmutablePair<>(orgName, alias));
                        }
                    })
            ));
        }
    }

    /**
     * Uppercase case the first letter of this string.
     *
     * @param name name to be converted
     * @return converted string
     */
    protected static String upperCaseFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1);
    }

    /**
     * Lowercase the first letter of this string.
     *
     * @param name name to be converted
     * @return converted string
     */
    protected static String lowerCaseFirstLetter(String name) {
        return name.substring(0, 1).toLowerCase(Locale.getDefault()) + name.substring(1);
    }

    /**
     * Returns True if import is non existent, False otherwise.
     *
     * @param orgName package organization name
     * @param alias   package alias
     * @return True if import is non existent, False otherwise.
     */
    protected boolean isNonExistImport(String orgName, String alias) {
        return imports.stream().noneMatch(pair -> (pair.getLeft().equals(orgName) && pair.getRight().equals(alias)));
    }

    /**
     * Returns a conflict free global variable name for the proposed variable name.
     *
     * @param name variable name
     * @return suggested name
     */
    protected String getSafeGlobalVariableName(String name) {
        List<String> names = builtTestFile.getGlobalVariables().stream()
                .map(variable -> variable.name.value)
                .collect(Collectors.toList());
        builtTestFile.testablePkgs.stream().findAny().ifPresent(
                testablePkg -> names.addAll(testablePkg.getGlobalVariables().stream()
                                                    .map(variable -> variable.name.value)
                                                    .collect(Collectors.toList()))
        );
        return getSafeName(name, names);
    }

    /**
     * Returns a conflict free function name for the proposed function name.
     *
     * @param name function name
     * @return suggested name
     */
    protected String getSafeFunctionName(String name) {
        List<String> names = builtTestFile.getFunctions().stream()
                .map(function -> function.name.value)
                .collect(Collectors.toList());
        builtTestFile.testablePkgs.stream().findAny().ifPresent(
                testablePkg -> names.addAll(testablePkg.getFunctions().stream()
                                                    .map(function -> function.name.value)
                                                    .collect(Collectors.toList()))
        );
        return getSafeName(name, names);
    }

    /**
     * Returns a conflict free service name for the proposed service name.
     *
     * @param name service name
     * @return suggested name
     */
    protected String getSafeServiceName(String name) {
        List<String> names = builtTestFile.getServices().stream()
                .map(service -> service.name.value)
                .collect(Collectors.toList());
        builtTestFile.testablePkgs.stream().findAny().ifPresent(
                testablePkg -> names.addAll(testablePkg.getServices().stream()
                                                    .map(service -> service.name.value)
                                                    .collect(Collectors.toList()))
        );
        return getSafeName(name, names);
    }

    private String getSafeName(String name, List<String> nodeNames) {
        boolean loop = true;
        int counter = 1;
        String prefix = "";
        while (loop) {
            loop = false;
            for (String nodeName : nodeNames) {
                if (nodeName.equals(name + prefix)) {
                    prefix = "" + (++counter);
                    loop = true;
                    break;
                }
            }
        }
        return name + prefix;
    }
}

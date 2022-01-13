/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a module information in ballerina.
 * <p>
 * Allows convenient transformation of ImportDeclarationNode node model representation for org-name, module-name,
 * version and alias.
 *
 * @since 2.0.0
 */
public class CodeActionModuleId implements ModuleID {

    private static final String ORG_SEPARATOR = "/";
    private final String orgName;
    private final String moduleName;
    private final String version;
    private final String alias;

    private CodeActionModuleId(String orgName, String moduleName, String alias, String version) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.alias = alias;
        this.version = version;
    }

    public static CodeActionModuleId from(String orgName, String moduleName, String version) {
        List<String> names = Arrays.stream(moduleName.split("\\.")).collect(Collectors.toList());
        String alias = moduleName.equals(".") ? moduleName : names.get(names.size() - 1);
        return new CodeActionModuleId(orgName, moduleName, alias, version);
    }

    public static CodeActionModuleId from(String orgName, String moduleName, String alias, String version) {
        return new CodeActionModuleId(orgName, moduleName, alias, version);
    }

    public static CodeActionModuleId from(ImportDeclarationNode importPkg) {
        String orgName = importPkg.orgName().isPresent() ? importPkg.orgName().get().orgName() + ORG_SEPARATOR : "";
        StringBuilder pkgNameBuilder = new StringBuilder();
        // Need to add separators (".") manually
        String pkgName = importPkg.moduleName().stream().map(Token::text).collect(Collectors.joining("."));
        pkgName = pkgNameBuilder.append(pkgName).toString();
        String alias = importPkg.prefix().isEmpty() ? "" : importPkg.prefix().get().prefix().text();
        return new CodeActionModuleId(orgName, pkgName, alias, "");
    }

    @Override
    public String orgName() {
        return orgName;
    }

    /**
     * Get the Package name of this module ID.
     *
     * @return Package name
     */
    @Override
    public String packageName() {
        // TODO: Implement this method.
        throw new UnsupportedOperationException("Not yet implemented");
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
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CodeActionModuleId that = (CodeActionModuleId) o;
        return orgName.equals(that.orgName) &&
                moduleName.equals(that.moduleName) &&
                version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgName, moduleName, version);
    }
}

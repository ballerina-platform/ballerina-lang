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

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;

/**
 * Represents a module information in ballerina.
 * <p>
 * Allows convenient transformation of ImportDeclarationNode node model representation for org-name, module-name,
 * version and alias.
 *
 * @since 2.0.0
 */
public class CodeActionModuleId {
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

    public static CodeActionModuleId from(ImportDeclarationNode importPkg) {
        String orgName = importPkg.orgName().isPresent() ? importPkg.orgName().get().orgName() + ORG_SEPARATOR : "";
        StringBuilder pkgNameBuilder = new StringBuilder();
        importPkg.moduleName().forEach(pkgNameBuilder::append);
        String pkgName = pkgNameBuilder.toString();
        String alias = importPkg.prefix().isEmpty() ? "" : importPkg.prefix().get().prefix().text();
        return new CodeActionModuleId(orgName, pkgName, alias, "");
    }

    public String orgName() {
        return orgName;
    }

    public String moduleName() {
        return moduleName;
    }

    public String version() {
        return version;
    }

    public String alias() {
        return alias;
    }
}

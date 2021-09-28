/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.internal.treegen.model.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Contains data required to generate a Java class for a syntax tree node.
 *
 * @since 1.3.0
 */
public class TreeNodeClass {
    private static final String INTERNAL_NODE_CLASS_NAME_PREFIX = "ST";

    private final String packageName;
    private final boolean isAbstract;
    private final String superClassName;
    private final List<ImportClass> imports;
    private final List<Field> fields;
    private final String syntaxKind;

    private final String externalClassName;
    private final String internalClassName;
    private final String tsClassName;

    private final String externalSuperClassName;
    private final String internalSuperClassName;
    private final String tsSuperClassName;

    public TreeNodeClass(String packageName,
                         String className,
                         boolean isAbstract,
                         String superClassName,
                         List<Field> fields,
                         String syntaxKind) {
        this.packageName = packageName;
        this.isAbstract = isAbstract;
        this.superClassName = superClassName;
        this.imports = new ArrayList<>();
        this.fields = fields;
        this.syntaxKind = syntaxKind;

        this.externalClassName = className;
        this.internalClassName = INTERNAL_NODE_CLASS_NAME_PREFIX + className;
        this.tsClassName = className.replaceAll("Node", "");

        this.externalSuperClassName = superClassName;
        this.internalSuperClassName = INTERNAL_NODE_CLASS_NAME_PREFIX + superClassName;
        this.tsSuperClassName = "Node".equals(superClassName)
                ? ("ST" + superClassName)
                : superClassName.replaceAll("Node", "");
    }

    public String packageName() {
        return packageName;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public String superClassName() {
        return superClassName;
    }

    public List<ImportClass> imports() {
        return imports;
    }

    public List<Field> fields() {
        return fields;
    }

    public String syntaxKind() {
        return syntaxKind;
    }

    public String externalClassName() {
        return externalClassName;
    }

    public String internalClassName() {
        return internalClassName;
    }

    public String externalSuperClassName() {
        return externalSuperClassName;
    }

    public String internalSuperClassName() {
        return internalSuperClassName;
    }

    public String camelCaseExternalClassName() {
        String firstChar = externalClassName.substring(0, 1);
        String firstCharLowercase = firstChar.toLowerCase(Locale.ENGLISH);
        return externalClassName.replaceFirst(firstChar, firstCharLowercase);
    }

    public boolean optionalFieldExists() {
        return fields.stream().anyMatch(Field::isOptional);
    }

    public boolean extendFromNode() {
        return "Node".equals(superClassName);
    }

    public void addImports(List<String> classNameList) {
        for (String className : classNameList) {
            imports.add(new ImportClass(className));
        }
        Collections.sort(imports);
    }

    public String tsClassName() {
        return tsClassName;
    }

    public String tsSuperClassName() {
        return tsSuperClassName;
    }
}

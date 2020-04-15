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

/**
 * Contains data required to generate syntax node visitor classes.
 *
 * @since 1.3.0
 */
public class TreeNodeVisitorClass {
    private final String packageName;
    private final String className;
    private final String superClassName;
    private final List<ImportClass> imports;
    private final List<TreeNodeClass> nodes;

    public TreeNodeVisitorClass(String packageName,
                                String className,
                                String superClassName,
                                List<String> importClassList,
                                List<TreeNodeClass> nodes) {
        this.packageName = packageName;
        this.className = className;
        this.superClassName = superClassName;
        this.imports = getImports(importClassList);
        this.nodes = nodes;
    }

    public String packageName() {
        return packageName;
    }

    public String className() {
        return className;
    }

    public String superClassName() {
        return superClassName;
    }

    public List<ImportClass> imports() {
        return imports;
    }

    public List<TreeNodeClass> nodes() {
        return nodes;
    }

    private List<ImportClass> getImports(List<String> classNameList) {
        List<ImportClass> imports = new ArrayList<>();
        for (String className : classNameList) {
            imports.add(new ImportClass(className));
        }
        Collections.sort(imports);
        return imports;
    }
}

/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.generator.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Package documentation.
 */
public class ModuleDoc {
    public final String description;
    public final String summary;
    public final Map<String, SyntaxTree> syntaxTreeMap;
    public final SemanticModel semanticModel;
    public final List<Path> resources;
    public final boolean isDefault;

    /**
     * Constructor.
     *
     * @param moduleMd md content of the Module.md.
     * @param resources       resources of this package.
     * @param syntaxTreeMap a hash map of bal file names and syntax trees.
     * @param semanticModel the semantic model
     * @param isDefault whether this is the default module (not a sub-module)
     * @throws IOException on error.
     */
    public ModuleDoc(String moduleMd, List<Path> resources, Map<String, SyntaxTree> syntaxTreeMap,
                     SemanticModel semanticModel, boolean isDefault) throws IOException {
        this.description = moduleMd;
        this.summary = BallerinaDocUtils.getSummary(moduleMd);
        this.resources = resources;
        this.syntaxTreeMap = syntaxTreeMap;
        this.semanticModel = semanticModel;
        this.isDefault = isDefault;
    }
}

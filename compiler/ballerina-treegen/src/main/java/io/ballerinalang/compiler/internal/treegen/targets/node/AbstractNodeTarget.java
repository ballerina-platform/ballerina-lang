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
package io.ballerinalang.compiler.internal.treegen.targets.node;

import io.ballerinalang.compiler.internal.treegen.TreeGenConfig;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxNode;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxNodeMetadata;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxTree;
import io.ballerinalang.compiler.internal.treegen.model.template.TreeNodeClass;
import io.ballerinalang.compiler.internal.treegen.targets.SourceText;
import io.ballerinalang.compiler.internal.treegen.targets.Target;

import java.util.HashMap;
import java.util.List;

/**
 * The class {@code AbstractNodeTarget} represent a generic entity that converts a
 * given {@code SyntaxTree} into a list of classes.
 * <p>
 * Each class represents a node in the syntax tree.
 *
 * @since 1.3.0
 */
public abstract class AbstractNodeTarget extends Target {

    public AbstractNodeTarget(TreeGenConfig config) {
        super(config);
    }

    protected abstract String getPackageName();

    protected abstract String getOutputDir();

    protected abstract String getClassName(TreeNodeClass treeNodeClass);

    protected abstract List<String> getImportClasses(SyntaxNode syntaxNode);

    @Override
    public List<SourceText> execute(SyntaxTree syntaxTree, HashMap<String, SyntaxNodeMetadata> nodeMetadataMap) {
        return syntaxTree.nodes()
                .stream()
                .map(syntaxNode -> generateNodeClass(syntaxNode, nodeMetadataMap))
                .map(treeNodeClass -> getSourceText(treeNodeClass, getOutputDir(), getClassName(treeNodeClass)))
                .toList();
    }

    private TreeNodeClass generateNodeClass(SyntaxNode syntaxNode, HashMap<String,
            SyntaxNodeMetadata> nodeMetadataMap) {
        List<String> importClassList = getImportClasses(syntaxNode);
        return convertToTreeNodeClass(syntaxNode, getPackageName(), importClassList, nodeMetadataMap);
    }
}

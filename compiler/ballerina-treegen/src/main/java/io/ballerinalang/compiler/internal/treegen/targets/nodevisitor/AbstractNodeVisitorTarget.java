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
package io.ballerinalang.compiler.internal.treegen.targets.nodevisitor;

import io.ballerinalang.compiler.internal.treegen.TreeGenConfig;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxTree;
import io.ballerinalang.compiler.internal.treegen.model.template.TreeNodeClass;
import io.ballerinalang.compiler.internal.treegen.model.template.TreeNodeVisitorClass;
import io.ballerinalang.compiler.internal.treegen.targets.SourceText;
import io.ballerinalang.compiler.internal.treegen.targets.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class {@code AbstractNodeVisitorTarget} represent a generic entity that converts a
 * given {@code SyntaxTree} into a node visitor.
 *
 * @since 1.3.0
 */
public abstract class AbstractNodeVisitorTarget extends Target {

    static final String TREE_MODIFIER_CN = "TreeModifier";
    static final String NODE_TRANSFORMER_CN = "NodeTransformer";
    static final String INTERNAL_TREE_MODIFIER_CN = "STTreeModifier";
    static final String INTERNAL_NODE_FACTORY_CN = "STNodeFactory";
    static final String INTERNAL_NODE_TRANSFORMER_CN = "STNodeTransformer";
    static final String INTERNAL_BASE_NODE_FACTORY_CN = "STAbstractNodeFactory";
    public static final String INTERNAL_NODE_VISITOR_CN = "STNodeVisitor";
    static final String EXTERNAL_NODE_FACTORY_CN = "NodeFactory";
    static final String EXTERNAL_BASE_NODE_FACTORY_CN = "AbstractNodeFactory";
    public static final String EXTERNAL_NODE_VISITOR_CN = "NodeVisitor";

    AbstractNodeVisitorTarget(TreeGenConfig config) {
        super(config);
    }

    @Override
    public List<SourceText> execute(SyntaxTree syntaxTree) {
        TreeNodeVisitorClass treeNodeVisitorClass = generateNodeVisitorClass(syntaxTree);
        return Collections.singletonList(
                getSourceText(treeNodeVisitorClass, getOutputDir(), getClassName()));
    }

    private TreeNodeVisitorClass generateNodeVisitorClass(SyntaxTree syntaxTree) {
        return new TreeNodeVisitorClass(getPackageName(), getClassName(),
                getSuperClassName(), getImportClasses(), generateNodeClasses(syntaxTree));
    }

    private List<TreeNodeClass> generateNodeClasses(SyntaxTree syntaxTree) {
        return syntaxTree.nodes()
                .stream()
                .map(syntaxNode -> convertToTreeNodeClass(syntaxNode,
                        getPackageName(), new ArrayList<>()))
                .collect(Collectors.toList());
    }

    protected abstract String getPackageName();

    protected abstract String getOutputDir();

    protected abstract String getClassName();

    protected abstract String getSuperClassName();

    protected abstract List<String> getImportClasses();
}

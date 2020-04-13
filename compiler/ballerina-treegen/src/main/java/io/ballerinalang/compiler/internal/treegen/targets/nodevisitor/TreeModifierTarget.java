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

import java.util.ArrayList;
import java.util.List;

import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.EXTERNAL_NODE_OUTPUT_DIR_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.EXTERNAL_NODE_PACKAGE_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.EXTERNAL_TREE_MODIFIER_TEMPLATE_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.INTERNAL_NODE_PACKAGE_KEY;

/**
 * Generates a syntax tree transformer java class for the external syntax tree.
 *
 * @since 1.3.0
 */
public class TreeModifierTarget extends AbstractNodeVisitorTarget {

    public TreeModifierTarget(TreeGenConfig config) {
        super(config);
    }

    @Override
    protected String getPackageName() {
        return config.getOrThrow(EXTERNAL_NODE_PACKAGE_KEY);
    }

    @Override
    protected String getOutputDir() {
        return config.getOrThrow(EXTERNAL_NODE_OUTPUT_DIR_KEY);
    }

    @Override
    protected String getClassName() {
        return TREE_MODIFIER_CN;
    }

    @Override
    protected String getSuperClassName() {
        return NODE_TRANSFORMER_CN;
    }

    protected @Override
    List<String> getImportClasses() {
        List<String> imports = new ArrayList<>();
        imports.add(getClassFQN(config.getOrThrow(INTERNAL_NODE_PACKAGE_KEY), INTERNAL_BASE_NODE_CN));
        imports.add(getClassFQN(config.getOrThrow(INTERNAL_NODE_PACKAGE_KEY), INTERNAL_NODE_FACTORY_CN));
        return imports;
    }

    @Override
    protected String getTemplateName() {
        return config.getOrThrow(EXTERNAL_TREE_MODIFIER_TEMPLATE_KEY);
    }
}

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

import java.util.Collections;
import java.util.List;

import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.INTERNAL_NODE_OUTPUT_DIR_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.INTERNAL_NODE_PACKAGE_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.INTERNAL_NODE_VISITOR_TEMPLATE_KEY;

/**
 * Generates a node visitor java class for the external syntax tree.
 *
 * @since 1.3.0
 */
public class InternalNodeVisitorTarget extends AbstractNodeVisitorTarget {

    public InternalNodeVisitorTarget(TreeGenConfig config) {
        super(config);
    }

    @Override
    protected String getPackageName() {
        return config.getOrThrow(INTERNAL_NODE_PACKAGE_KEY);
    }

    @Override
    protected String getOutputDir() {
        return config.getOrThrow(INTERNAL_NODE_OUTPUT_DIR_KEY);
    }

    @Override
    protected String getClassName() {
        return INTERNAL_NODE_VISITOR_CN;
    }

    @Override
    protected String getSuperClassName() {
        return null;
    }

    @Override
    protected List<String> getImportClasses() {
        return Collections.emptyList();
    }

    @Override
    protected String getTemplateName() {
        return config.getOrThrow(INTERNAL_NODE_VISITOR_TEMPLATE_KEY);
    }
}

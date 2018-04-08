/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.completions.util.positioning.resolvers;

import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Package node scope position resolver.
 */
public class PackageNodeScopeResolver extends CursorPositionResolver {
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, BLangNode node, TreeVisitor treeVisitor,
                                      LSServiceOperationContext completionContext) {
        // TODO: Finalize the implementation
        return false;
    }
}

/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor to find the base url of endpoint.
 *
 * @since 2.0.0
 */
public class ResourceFinder extends NodeVisitor {

    List<Resource> resources = new ArrayList<>();

    public List<Resource> getResources() {

        return resources;
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {

        if (functionDefinitionNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
            StringBuilder name = new StringBuilder();
            for (Node node : functionDefinitionNode.relativeResourcePath()) {
                name.append(node.toString());
            }
            resources.add(new Resource(name.toString(), functionDefinitionNode.lineRange()));
        }
    }
}

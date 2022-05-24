/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.semver.checker.util;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Syntax tree API related utilities.
 *
 * @since 2201.2.0
 */
public class SyntaxTreeUtils {

    /**
     * Returns the node typename of the given {@link SyntaxKind} instance.
     *
     * @param nodeKind node kind
     * @return node typename
     */
    public static String getNodeKindName(SyntaxKind nodeKind) {
        return nodeKind.name().toLowerCase(Locale.ROOT).toLowerCase(Locale.getDefault()).replaceAll("_", " ");
    }

    /**
     * Returns the service identifier of the given service node.
     *
     * @param serviceNode service declaration syntax node
     * @return the service identifier
     */
    public static String getServiceName(ServiceDeclarationNode serviceNode) {
        // Todo: refactor once the service identifier support is added to the language side
        if (serviceNode.absoluteResourcePath() != null && !serviceNode.absoluteResourcePath().isEmpty()) {
            return serviceNode.absoluteResourcePath().stream()
                    .map(Node::toSourceCode)
                    .collect(Collectors.joining("_"));
        } else if (serviceNode.typeDescriptor().isPresent()) {
            return serviceNode.typeDescriptor().get().toSourceCode();
        } else {
            return String.valueOf(serviceNode.hashCode());
        }
    }
}

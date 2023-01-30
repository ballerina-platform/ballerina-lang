/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator.generators.entity.nodevisitors;

import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Visitor class for TypeDefinition nodes.
 *
 * @since 2201.4.1
 */
public class TypeDefinitionNodeVisitor extends NodeVisitor {

    private final Map<String, RecordTypeDescriptorNode> recordTypeDescNodes = new HashMap<>();

    public Map<String, RecordTypeDescriptorNode> getRecordTypeDescNodes() {
        return recordTypeDescNodes;
    }

    @Override
    public void visit(TypeDefinitionNode typeDefinitionNode) {
        if (typeDefinitionNode.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
            recordTypeDescNodes.put(typeDefinitionNode.typeName().text(),
                    (RecordTypeDescriptorNode) typeDefinitionNode.typeDescriptor());
        }
    }
}

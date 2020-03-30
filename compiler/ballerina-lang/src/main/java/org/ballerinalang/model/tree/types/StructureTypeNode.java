/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.tree.types;

import org.ballerinalang.model.tree.SimpleVariableNode;

import java.util.List;

/**
 * {@code StructureTypeNode} represents a structure type node in Ballerina.
 *
 * @since 0.971.0
 */
public interface StructureTypeNode extends ReferenceTypeNode {

    boolean getIsAnonymous();

    boolean getIsLocal();
    
    List<? extends SimpleVariableNode> getFields();

    void addField(SimpleVariableNode field);

    /**
     * Get the list of types that are referenced by this type.
     *
     * @return list of types that are referenced by this type.
     */
    List<? extends TypeNode> getTypeReferences();

    /**
     * Add a type reference.
     *
     * @param field Type that is referenced by this type.
     */
    void addTypeReference(TypeNode field);
}

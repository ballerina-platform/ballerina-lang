/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.model.tree;

import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;

import java.util.List;

/**
 * Represents a record variable node.
 *
 * Example:
 *      type Person record {
 *          string name;
 *          boolean married;
 *          !...
 *      };
 *
 *      Person {name, married} = {name: "Peter", married: true};
 *
 * @since 0.985.0
 */
public interface RecordVariableNode extends VariableNode, AnnotatableNode, DocumentableNode, TopLevelNode {

    List<? extends BLangRecordVariable.BLangRecordVariableKeyValueNode> getVariables();

    /**
     * Interface for key and value of a record variable.
     */
    interface BLangRecordVariableKeyValueNode {

        IdentifierNode getKey();

        VariableNode getValue();
    }

}

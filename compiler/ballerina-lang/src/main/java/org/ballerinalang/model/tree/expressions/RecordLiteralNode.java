/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.tree.expressions;

import org.ballerinalang.model.tree.Node;

import java.util.List;

/**
 * {key1:value1, key2:value2, string`key${n}`:value3, ...}.
 *
 * @since 0.94
 */
public interface RecordLiteralNode extends ExpressionNode {

    List<? extends RecordField> getFields();

    /**
     * @since 1.2.0
     */
    interface RecordField extends Node {
        boolean isKeyValueField();
    }

    /**
     * @since 0.94
     */
    interface RecordKeyValueFieldNode extends RecordField {

        ExpressionNode getKey();

        ExpressionNode getValue();
    }

    /**
     * @since 1.2.0
     */
    interface RecordVarNameFieldNode extends RecordField, SimpleVariableReferenceNode {

    }
}

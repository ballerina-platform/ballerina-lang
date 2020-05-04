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
package org.ballerinalang.model.tree.expressions;

import org.ballerinalang.model.tree.ActionNode;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;

import java.util.List;

/**
 * This represents the wait for all expression node.
 *
 * @since 0.985
 */
public interface WaitForAllExpressionNode extends ExpressionNode, ActionNode {

    List<BLangWaitForAllExpr.BLangWaitKeyValue> getKeyValuePairs();

    /**
     * This represents the key-value node of wait for all expression.
     * @since 0.985
     */
    interface WaitKeyValueNode {

        BLangIdentifier getKey();

        BLangExpression getValue();
    }

}


/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.ballerinalang.compiler.tree.expressions;


import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.FieldBasedAccessNode;

/**
 * Implementation of {@link FieldBasedAccessNode}.
 *
 * @since 0.94
 */
public class BLangFieldBasedAccess extends BLangVariableReference implements FieldBasedAccessNode {

    public IdentifierNode fieldName;

    public ExpressionNode expressionNode;

    @Override
    public ExpressionNode getExpression() {
        return expressionNode;
    }

    @Override
    public IdentifierNode getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return expressionNode.toString() + "." + fieldName.getValue();
    }
}

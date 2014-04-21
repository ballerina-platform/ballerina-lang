/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.executor.conditon.compare.instance_of;


import org.wso2.siddhi.core.executor.conditon.compare.CompareConditionExecutor;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

public class InstanceOfCompareConditionExecutor extends CompareConditionExecutor {

    public InstanceOfCompareConditionExecutor(ExpressionExecutor leftExpressionExecutor,
                                              ExpressionExecutor rightExpressionExecutor) {
        super(leftExpressionExecutor, rightExpressionExecutor);
    }

    @Override
    protected boolean process(Object left, Object right) {
        Attribute.Type type = (Attribute.Type) right;
        switch (type) {
            case STRING:
                return left instanceof String;
            case INT:
                return left instanceof Integer;
            case LONG:
                return left instanceof Long;
            case FLOAT:
                return left instanceof Float;
            case DOUBLE:
                return left instanceof Double;
            case BOOL:
                return left instanceof Boolean;
        }
        return false;
    }
}

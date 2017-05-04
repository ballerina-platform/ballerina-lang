/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.table.record;


import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.condition.Compare;

/**
 * Antlr Visitor interface for Siddhi conditions.
 */
public interface ConditionVisitor {

    /*And*/
    void beginVisitAnd();

    void endVisitAnd();

    void beginVisitAndLeftOperand();

    void endVisitAndLeftOperand();

    void beginVisitAndRightOperand();

    void endVisitAndRightOperand();

    /*Or*/
    void beginVisitOr();

    void endVisitOr();

    void beginVisitOrLeftOperand();

    void endVisitOrLeftOperand();

    void beginVisitOrRightOperand();

    void endVisitOrRightOperand();

    /*Not*/
    void beginVisitNot();

    void endVisitNot();

    /*Compare*/
    void beginVisitCompare(Compare.Operator operator);

    void endVisitCompare(Compare.Operator operator);

    void beginVisitCompareLeftOperand(Compare.Operator operator);

    void endVisitCompareLeftOperand(Compare.Operator operator);

    void beginVisitCompareRightOperand(Compare.Operator operator);

    void endVisitCompareRightOperand(Compare.Operator operator);

    /*IsNull*/
    void beginVisitIsNull(String streamId);

    void endVisitIsNull(String streamId);

    /*In*/
    void beginVisitIn(String storeId);

    void endVisitIn(String storeId);

    /*Constant*/
    void beginVisitConstant(Object value, Attribute.Type type);

    void endVisitConstant(Object value, Attribute.Type type);

    /*Math*/
    void beginVisitMath(MathOperator mathOperator);

    void endVisitMath(MathOperator mathOperator);

    void beginVisitMathLeftOperand(MathOperator mathOperator);

    void endVisitMathLeftOperand(MathOperator mathOperator);

    void beginVisitMathRightOperand(MathOperator mathOperator);

    void endVisitMathRightOperand(MathOperator mathOperator);

    /*AttributeFunction*/
    void beginVisitAttributeFunction(String namespace, String functionName);

    void endVisitAttributeFunction(String namespace, String functionName);

    void beginVisitParameterAttributeFunction(int index);

    void endVisitParameterAttributeFunction(int index);

    /*Variable*/
    void beginVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type);

    void endVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type);

    /*Variable*/
    void beginVisitStoreVariable(String storeId, String attributeName, Attribute.Type type);

    void endVisitStoreVariable(String storeId, String attributeName, Attribute.Type type);

    /**
     * Math operator enums.
     */
    enum MathOperator {
        ADD,
        DIVIDE,
        MULTIPLY,
        SUBTRACT,
        MOD
    }

}

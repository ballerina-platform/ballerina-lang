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
 * Implementation of Antlr Visitor implementation for conditions.
 */
public class BaseConditionVisitor implements ConditionVisitor {


    @Override
    public void beginVisitAnd() {

    }

    @Override
    public void endVisitAnd() {

    }

    @Override
    public void beginVisitAndLeftOperand() {

    }

    @Override
    public void endVisitAndLeftOperand() {

    }

    @Override
    public void beginVisitAndRightOperand() {

    }

    @Override
    public void endVisitAndRightOperand() {

    }

    @Override
    public void beginVisitOr() {

    }

    @Override
    public void endVisitOr() {

    }

    @Override
    public void beginVisitOrLeftOperand() {

    }

    @Override
    public void endVisitOrLeftOperand() {

    }

    @Override
    public void beginVisitOrRightOperand() {

    }

    @Override
    public void endVisitOrRightOperand() {

    }

    @Override
    public void beginVisitNot() {

    }

    @Override
    public void endVisitNot() {

    }

    @Override
    public void beginVisitCompare(Compare.Operator operator) {

    }

    @Override
    public void endVisitCompare(Compare.Operator operator) {

    }

    @Override
    public void beginVisitCompareLeftOperand(Compare.Operator operator) {

    }

    @Override
    public void endVisitCompareLeftOperand(Compare.Operator operator) {

    }

    @Override
    public void beginVisitCompareRightOperand(Compare.Operator operator) {

    }

    @Override
    public void endVisitCompareRightOperand(Compare.Operator operator) {

    }

    @Override
    public void beginVisitIsNull(String streamId) {

    }

    @Override
    public void endVisitIsNull(String streamId) {

    }

    @Override
    public void beginVisitIn(String storeId) {

    }

    @Override
    public void endVisitIn(String storeId) {

    }

    @Override
    public void beginVisitConstant(Object value, Attribute.Type type) {

    }

    @Override
    public void endVisitConstant(Object value, Attribute.Type type) {

    }

    @Override
    public void beginVisitMath(MathOperator mathOperator) {

    }

    @Override
    public void endVisitMath(MathOperator mathOperator) {

    }

    @Override
    public void beginVisitMathLeftOperand(MathOperator mathOperator) {

    }

    @Override
    public void endVisitMathLeftOperand(MathOperator mathOperator) {

    }

    @Override
    public void beginVisitMathRightOperand(MathOperator mathOperator) {

    }

    @Override
    public void endVisitMathRightOperand(MathOperator mathOperator) {

    }

    @Override
    public void beginVisitAttributeFunction(String namespace, String functionName) {

    }

    @Override
    public void endVisitAttributeFunction(String namespace, String functionName) {

    }

    @Override
    public void beginVisitParameterAttributeFunction(int index) {

    }

    @Override
    public void endVisitParameterAttributeFunction(int index) {

    }

    @Override
    public void beginVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type) {

    }

    @Override
    public void endVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type) {

    }

    @Override
    public void beginVisitStoreVariable(String storeId, String attributeName, Attribute.Type type) {

    }

    @Override
    public void endVisitStoreVariable(String storeId, String attributeName, Attribute.Type type) {

    }
}

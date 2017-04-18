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
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.extension.table.rdbms;

import org.wso2.siddhi.core.table.record.BaseConditionVisitor;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSQueryConfigurationEntry;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.io.IOException;

public class RDBMSConditionVisitor extends BaseConditionVisitor {

    private static final String WHITESPACE = " ";
    private static final String SQL_AND = "AND";
    private static final String SQL_OR = "OR";
    private static final String SQL_NOT = "NOT";
    private static final String SQL_COMPARE_LESS_THAN = "<";
    private static final String SQL_COMPARE_GREATER_THAN = ">";
    private static final String SQL_COMPARE_LESS_THAN_EQUAL = "<=";
    private static final String SQL_COMPARE_GREATER_THAN_EQUAL = ">=";
    private static final String SQL_COMPARE_EQUAL = "==";
    private static final String SQL_COMPARE_NOT_EQUAL = "!=";
    private static final String SQL_MATH_ADD = "+";
    private static final String SQL_MATH_DIVIDE = "/";
    private static final String SQL_MATH_MULTIPLY = "*";
    private static final String SQL_MATH_SUBTRACT = "-";
    private static final String SQL_MATH_MOD = "%";

    private static final String STREAM_VAR_PREFIX = "?";

    private StringBuilder query;
    private RDBMSQueryConfigurationEntry queryConfig;

    private RDBMSConditionVisitor() throws IOException {
        //preventing initialization
    }

    public RDBMSConditionVisitor(RDBMSQueryConfigurationEntry entry) {
        this.query = new StringBuilder();
        this.queryConfig = entry;
    }

    public String returnCondition() {
        return this.query.toString();
    }

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
        query.append(WHITESPACE).append(SQL_AND).append(WHITESPACE);
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
        query.append(WHITESPACE).append(SQL_OR).append(WHITESPACE);
    }

    @Override
    public void endVisitOrRightOperand() {

    }

    @Override
    public void beginVisitNot() {
        query.append(WHITESPACE).append(SQL_NOT).append(WHITESPACE);
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
        query.append(WHITESPACE);
        switch (operator) {
            case EQUAL:
                query.append(SQL_COMPARE_EQUAL);
                break;
            case GREATER_THAN:
                query.append(SQL_COMPARE_GREATER_THAN);
                break;
            case GREATER_THAN_EQUAL:
                query.append(SQL_COMPARE_GREATER_THAN_EQUAL);
                break;
            case LESS_THAN:
                query.append(SQL_COMPARE_LESS_THAN);
                break;
            case LESS_THAN_EQUAL:
                query.append(SQL_COMPARE_LESS_THAN_EQUAL);
                break;
            case NOT_EQUAL:
                query.append(SQL_COMPARE_NOT_EQUAL);
                break;
        }
        query.append(WHITESPACE);
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
        query.append(WHITESPACE);
        switch (mathOperator) {
            case ADD:
                query.append(SQL_MATH_ADD);
                break;
            case DIVIDE:
                query.append(SQL_MATH_DIVIDE);
                break;
            case MOD:
                query.append(SQL_MATH_MOD);
                break;
            case MULTIPLY:
                query.append(SQL_MATH_MULTIPLY);
                break;
            case SUBTRACT:
                query.append(SQL_MATH_SUBTRACT);
                break;
        }
        query.append(WHITESPACE);
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
        query.append(STREAM_VAR_PREFIX).append(id);
    }

    @Override
    public void endVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type) {

    }

    @Override
    public void beginVisitStoreVariable(String StoreId, String attributeName, Attribute.Type type) {

    }

    @Override
    public void endVisitStoreVariable(String StoreId, String attributeName, Attribute.Type type) {

    }
}

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
import org.wso2.siddhi.extension.table.rdbms.exception.RDBMSTableException;
import org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants;
import org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableUtils;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class RDBMSConditionVisitor extends BaseConditionVisitor {

    private static final String WHITESPACE = " ";
    private static final String OPEN_PARENTHESIS = "(";
    private static final String CLOSE_PARENTHESIS = ")";

    private static final String SQL_AND = "AND";
    private static final String SQL_OR = "OR";
    private static final String SQL_NOT = "NOT";
    private static final String SQL_IN = "IN";
    private static final String SQL_IS_NULL = "IS NULL";
    private static final String SQL_COMPARE_LESS_THAN = "<";
    private static final String SQL_COMPARE_GREATER_THAN = ">";
    private static final String SQL_COMPARE_LESS_THAN_EQUAL = "<=";
    private static final String SQL_COMPARE_GREATER_THAN_EQUAL = ">=";
    private static final String SQL_COMPARE_EQUAL = "=";
    private static final String SQL_COMPARE_NOT_EQUAL = "!=";   // "<>" ?
    private static final String SQL_MATH_ADD = "+";
    private static final String SQL_MATH_DIVIDE = "/";
    private static final String SQL_MATH_MULTIPLY = "*";
    private static final String SQL_MATH_SUBTRACT = "-";
    private static final String SQL_MATH_MOD = "%";

    private StringBuilder query;
    private String finalCompiledCondition;
    private RDBMSQueryConfigurationEntry queryConfig;

    private Map<String, Attribute.Type> streamTypeMap;
    private SortedMap<Integer, Attribute> streamVariablePositions;

    private RDBMSConditionVisitor() throws IOException {
        //preventing initialization
    }

    public RDBMSConditionVisitor(RDBMSQueryConfigurationEntry entry) {
        this.query = new StringBuilder();
        this.queryConfig = entry;
        this.streamTypeMap = new HashMap<>();
        this.streamVariablePositions = new TreeMap<>();
    }

    public String returnCondition() {
        //return this.query.toString().trim();
    }

    public Map<String, Attribute.Type> getStreamTypeMap() {
        return streamTypeMap;
    }

    @Override
    public void beginVisitAnd() {
        query.append(OPEN_PARENTHESIS);
    }

    @Override
    public void endVisitAnd() {
        query.append(CLOSE_PARENTHESIS);
    }

    @Override
    public void beginVisitAndLeftOperand() {
        //Not applicable
    }

    @Override
    public void endVisitAndLeftOperand() {
        //Not applicable
    }

    @Override
    public void beginVisitAndRightOperand() {
        query.append(SQL_AND).append(WHITESPACE);
    }

    @Override
    public void endVisitAndRightOperand() {
        //Not applicable
    }

    @Override
    public void beginVisitOr() {
        query.append(OPEN_PARENTHESIS);
    }

    @Override
    public void endVisitOr() {
        query.append(CLOSE_PARENTHESIS);
    }

    @Override
    public void beginVisitOrLeftOperand() {
        //Not applicable
    }

    @Override
    public void endVisitOrLeftOperand() {
        //Not applicable
    }

    @Override
    public void beginVisitOrRightOperand() {
        query.append(SQL_OR).append(WHITESPACE);
    }

    @Override
    public void endVisitOrRightOperand() {
        //Not applicable
    }

    @Override
    public void beginVisitNot() {
        query.append(SQL_NOT).append(WHITESPACE);
    }

    @Override
    public void endVisitNot() {
        //Not applicable
    }

    @Override
    public void beginVisitCompare(Compare.Operator operator) {
        query.append(OPEN_PARENTHESIS);
    }

    @Override
    public void endVisitCompare(Compare.Operator operator) {
        query.append(CLOSE_PARENTHESIS);
    }

    @Override
    public void beginVisitCompareLeftOperand(Compare.Operator operator) {
        //Not applicable
    }

    @Override
    public void endVisitCompareLeftOperand(Compare.Operator operator) {
        //Not applicable
    }

    @Override
    public void beginVisitCompareRightOperand(Compare.Operator operator) {
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
        //Not applicable
    }

    @Override
    public void beginVisitIsNull(String streamId) {
        query.append(SQL_IS_NULL).append(WHITESPACE);
    }

    @Override
    public void endVisitIsNull(String streamId) {
        //Not applicable
    }

    @Override
    public void beginVisitIn(String storeId) {
        query.append(SQL_IN).append(WHITESPACE);
    }

    @Override
    public void endVisitIn(String storeId) {
        //Not applicable
    }

    @Override
    public void beginVisitConstant(Object value, Attribute.Type type) {
        // TODO rewrite with map for later processing
        if (value != null && type != Attribute.Type.OBJECT) {
            if (type == Attribute.Type.STRING) {
                query.append("'").append(value.toString()).append("'").append(WHITESPACE);
            }
            query.append(value.toString()).append(WHITESPACE);
        } else if (value == null) {
            throw new RDBMSTableException("A defined constant has a null value. Please check your query and try again.");
        } else {
            throw new RDBMSTableException("The RDBMS Event table does not support constants of type Object.");
        }
    }

    @Override
    public void endVisitConstant(Object value, Attribute.Type type) {
        //Not applicable
    }

    @Override
    public void beginVisitMath(MathOperator mathOperator) {
        query.append(OPEN_PARENTHESIS);
    }

    @Override
    public void endVisitMath(MathOperator mathOperator) {
        query.append(CLOSE_PARENTHESIS);
    }

    @Override
    public void beginVisitMathLeftOperand(MathOperator mathOperator) {
        //Not applicable
    }

    @Override
    public void endVisitMathLeftOperand(MathOperator mathOperator) {
        //Not applicable
    }

    @Override
    public void beginVisitMathRightOperand(MathOperator mathOperator) {
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
        //Not applicable
    }

    @Override
    public void beginVisitAttributeFunction(String namespace, String functionName) {
        if (RDBMSTableUtils.isEmpty(namespace)) {
            query.append(functionName).append(WHITESPACE);
        } else {
            throw new RDBMSTableException("The RDBMS Event table does not support function namespaces, but namespace '"
                    + namespace + "' was specified. Please use functions supported by the defined RDBMS data store.");
        }
    }

    @Override
    public void endVisitAttributeFunction(String namespace, String functionName) {
        //Not applicable
    }

    @Override
    public void beginVisitParameterAttributeFunction(int index) {
        //TODO
    }

    @Override
    public void endVisitParameterAttributeFunction(int index) {
        //TODO
    }

    @Override
    public void beginVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type) {
        query.append(RDBMSTableUtils.encodeStreamVariable(id)).append(WHITESPACE);
        this.streamTypeMap.put(id, type);
    }

    @Override
    public void endVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type) {
        //Not applicable
    }

    @Override
    public void beginVisitStoreVariable(String storeId, String attributeName, Attribute.Type type) {
        query.append(RDBMSTableConstants.TABLE_NAME_PLACEHOLDER).append(".").append(attributeName).append(WHITESPACE);
    }

    @Override
    public void endVisitStoreVariable(String storeId, String attributeName, Attribute.Type type) {
        //Not applicable
    }

    private void parameterizeCondition() {
        String unParametrizedCondition = this.query.toString();
        String output = this.query.toString();
//        for (Map.Entry<String, Attribute.Type> fields : this.streamTypeMap.entrySet()) {
//            String boxedField = RDBMSTableUtils.encodeStreamVariable(fields.getKey());
//            this.streamVariablePositions.put(unParametrizedCondition.indexOf(boxedField),
//                    new Attribute(fields.getKey(), fields.getValue()));
//            output = output.replace(boxedField, "?");
//        }
//        this.finalCompiledCondition = output;

        for(){

        }

    }

}

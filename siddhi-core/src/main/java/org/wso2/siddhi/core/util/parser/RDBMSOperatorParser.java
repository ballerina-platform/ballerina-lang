/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.*;
import org.wso2.siddhi.core.table.rdbms.DBConfiguration;
import org.wso2.siddhi.core.table.rdbms.ExecutionInfo;
import org.wso2.siddhi.core.table.rdbms.RDBMSEventAdaptorConstants;
import org.wso2.siddhi.core.table.rdbms.RDBMSOperator;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RDBMSOperatorParser {

    public static Operator parse(DBConfiguration dbConfiguration, Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors,
                                 Map<String, EventTable> eventTableMap, int matchingStreamIndex, AbstractDefinition candidateDefinition, long withinTime) {


        ExecutionInfo executionInfo = dbConfiguration.getExecutionInfo();
        Map<String, String> elementMappings = dbConfiguration.getElementMappings();
        String fullTableName = dbConfiguration.getFullTableName();
        List<Attribute> conditionAttributeList = new ArrayList<Attribute>();
        List<Attribute> updateConditionAttributeList = new ArrayList<Attribute>();
        updateConditionAttributeList.addAll(executionInfo.getInsertQueryColumnOrder());

        int candidateEventPosition = 0;
        ExpressionExecutor expressionExecutor = null;

        StringBuilder conditionBuilder = new StringBuilder(" ");

        MetaStateEvent metaStateEvent = null;
        if (metaComplexEvent instanceof MetaStreamEvent) {
            metaStateEvent = new MetaStateEvent(1);
            metaStateEvent.addEvent(((MetaStreamEvent) metaComplexEvent));
        } else {

            MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents();

            //for join
            for (; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
                if (candidateEventPosition != matchingStreamIndex && metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(candidateDefinition)) {
                    metaStateEvent = ((MetaStateEvent) metaComplexEvent);
                    break;
                }
            }

            if (metaStateEvent == null) {
                metaStateEvent = new MetaStateEvent(metaStreamEvents.length + 1);
                for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
                    metaStateEvent.addEvent(metaStreamEvent);
                }
            }
        }


        if (expression instanceof Compare) {
            Expression rightExpression = ((Compare) expression).getRightExpression();
            Expression leftExpression = ((Compare) expression).getLeftExpression();

            if (leftExpression instanceof Variable) {
                String attributeName = ((Variable) leftExpression).getAttributeName();
                conditionBuilder.append(attributeName).append(" ");
            }

            if (((Compare) expression).getOperator().equals(Compare.Operator.EQUAL)) {
                conditionBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_EQUAL)).append(" ");
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.GREATER_THAN)) {
                conditionBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_GREATER_THAN)).append(" ");
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.LESS_THAN)) {
                conditionBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_LESS_THAN)).append(" ");
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.GREATER_THAN_EQUAL)) {
                conditionBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_GREATER_THAN_EQUAL)).append(" ");
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.LESS_THAN_EQUAL)) {
                conditionBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_LESS_THAN_EQUAL)).append(" ");
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.NOT_EQUAL)) {
                conditionBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_NOT_EQUAL)).append(" ");
            }


            if (rightExpression instanceof Variable) {
                expressionExecutor = ExpressionParser.parseExpression(rightExpression,
                        metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
                conditionBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants
                        .EVENT_TABLE_RDBMS_QUESTION_MARK)).append(" ");
                String attributeName = ((Variable) rightExpression).getAttributeName();
                conditionAttributeList.add(getAttribute(dbConfiguration, attributeName));
                updateConditionAttributeList.add(getAttribute(dbConfiguration, attributeName));

            } else if (rightExpression instanceof Constant) {
                String value = ((StringConstant) rightExpression).getValue();
                conditionBuilder.append(value);
            }
        }

        //Constructing query to delete a table row
        String deleteTableRowQuery = dbConfiguration.constructQuery(fullTableName, elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_DELETE_TABLE), null, null, null, null, conditionBuilder);
        executionInfo.setPreparedDeleteStatement(deleteTableRowQuery);
        executionInfo.setDeleteQueryColumnOrder(conditionAttributeList);

        StringBuilder columnValues = getUpdateQueryAttributes(executionInfo, dbConfiguration.getElementMappings());

        String updateTableRowQuery = dbConfiguration.constructQuery(fullTableName, elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_UPDATE_TABLE), null, null, null, columnValues, conditionBuilder);
        executionInfo.setPreparedUpdateStatement(updateTableRowQuery);
        executionInfo.setUpdateQueryColumnOrder(updateConditionAttributeList);

        String selectTableRowQuery = dbConfiguration.constructQuery(fullTableName, elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_SELECT_TABLE), null, null, null, null, conditionBuilder);
        executionInfo.setPreparedSelectTableStatement(selectTableRowQuery);
        executionInfo.setConditionQueryColumnOrder(conditionAttributeList);

        String isTableRowExistentQuery = dbConfiguration.constructQuery(fullTableName, elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_TABLE_ROW_EXIST), null, null, null, null, conditionBuilder);
        executionInfo.setPreparedTableRowExistenceCheckStatement(isTableRowExistentQuery);
        executionInfo.setConditionQueryColumnOrder(conditionAttributeList);


        return new RDBMSOperator(expressionExecutor, dbConfiguration);
    }


    private static Attribute getAttribute(DBConfiguration dbConfiguration, String attributeName) {
        for (Attribute attribute : dbConfiguration.getAttributeList()) {
            if (attribute.getName().equals(attributeName)) {
                return attribute;
            }
        }
        //not-possible to happen
        return null;
    }

    private static StringBuilder getUpdateQueryAttributes(ExecutionInfo executionInfo, Map<String, String> elementMappings) {

        //Constructing (eg: information = ?  , latitude = ?) type values : column_values
        StringBuilder columnValues = new StringBuilder("");
        boolean appendComma = false;
        for (Attribute at : executionInfo.getInsertQueryColumnOrder()) {
            if (appendComma) {
                columnValues.append(" ").append(elementMappings.get(RDBMSEventAdaptorConstants
                        .EVENT_TABLE_RDBMS_COMMA)).append(" ");
            }
            columnValues.append(at.getName());
            columnValues.append(" ").append(elementMappings.get(RDBMSEventAdaptorConstants
                    .ADAPTOR_GENERIC_RDBMS_EQUAL)).append(" ")
                    .append(elementMappings.get(RDBMSEventAdaptorConstants
                            .EVENT_TABLE_RDBMS_QUESTION_MARK)).append(" ");
            appendComma = true;
        }
        return columnValues;
    }

}

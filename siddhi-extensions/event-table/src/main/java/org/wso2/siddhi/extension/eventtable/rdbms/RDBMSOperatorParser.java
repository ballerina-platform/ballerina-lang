/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.siddhi.extension.eventtable.rdbms;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.extension.eventtable.cache.CachingTable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.And;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.condition.Or;
import org.wso2.siddhi.query.api.expression.constant.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RDBMSOperatorParser {

    private static final Logger log = Logger.getLogger(RDBMSOperatorParser.class);

    private RDBMSOperatorParser() {
    }

    /**
     * Method that constructs the Operator for RDBMS related operations
     *
     * @param dbHandler                   dbHandler
     * @param expression                  Expression
     * @param metaComplexEvent            MetaComplexEvent
     * @param executionPlanContext        ExecutionPlanContext
     * @param variableExpressionExecutors list of VariableExpressionExecutor
     * @param eventTableMap               EventTable map
     * @param matchingStreamIndex         matching stream index
     * @param candidateDefinition         candidate definition
     * @param withinTime                  within time
     * @param cachingTable                caching table
     * @return Operator
     */
    public static Operator parse(DBHandler dbHandler, Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors,
                                 Map<String, EventTable> eventTableMap, int matchingStreamIndex, AbstractDefinition candidateDefinition, long withinTime, CachingTable cachingTable) {


        ExecutionInfo executionInfo = dbHandler.getExecutionInfoInstance();
        Map<String, String> elementMappings = dbHandler.getElementMappings();
        String tableName = dbHandler.getTableName();
        List<Attribute> conditionAttributeList = new ArrayList<Attribute>();
        List<Attribute> updateConditionAttributeList = new ArrayList<Attribute>();
        if (metaComplexEvent instanceof MetaStreamEvent && ((MetaStreamEvent) metaComplexEvent).getOutputData() != null) {
            updateConditionAttributeList.addAll(((MetaStreamEvent) metaComplexEvent).getOutputData());
        } else {
            updateConditionAttributeList.addAll(executionInfo.getInsertQueryColumnOrder());
        }

        List<ExpressionExecutor> expressionExecutorList = new ArrayList<ExpressionExecutor>();

        StringBuilder conditionBuilder = new StringBuilder(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);

        MetaStateEvent metaStateEvent = null;
        if (metaComplexEvent instanceof MetaStreamEvent) {
            metaStateEvent = new MetaStateEvent(1);
            metaStateEvent.addEvent(((MetaStreamEvent) metaComplexEvent));
            matchingStreamIndex = 0;
        } else {
            metaStateEvent = ((MetaStateEvent) metaComplexEvent);
        }

        Map<String, Boolean> isTableStreamMap = new HashMap<String, Boolean>();

        MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
        for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
            String referenceId = metaStreamEvent.getInputReferenceId();
            AbstractDefinition abstractDefinition = metaStreamEvent.getInputDefinitions().get(0);
            if (!abstractDefinition.getId().trim().equals("")) {
                if (abstractDefinition instanceof TableDefinition) {
                    isTableStreamMap.put(abstractDefinition.getId(), true);
                    if (referenceId != null) {
                        isTableStreamMap.put(referenceId, true);
                    }
                } else {
                    isTableStreamMap.put(abstractDefinition.getId(), false);
                    if (referenceId != null) {
                        isTableStreamMap.put(referenceId, false);
                    }
                }
            }

        }

        if (candidateDefinition instanceof TableDefinition) {
            isTableStreamMap.put(candidateDefinition.getId(), true);
        }

        buildConditionQuery(isTableStreamMap, expression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaComplexEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, executionInfo);

        //Constructing query to delete a table row
        String deleteTableRowQuery = dbHandler.constructQuery(tableName, elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_DELETE_TABLE), null, null, null, null, conditionBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Adding SQL Prepared Statement for execution plan " + executionPlanContext.getName() + " : " + deleteTableRowQuery);
        }
        executionInfo.setPreparedDeleteStatement(deleteTableRowQuery);
        executionInfo.setDeleteQueryColumnOrder(conditionAttributeList);

        //Constructing query to update a table row
        StringBuilder updateColumnValues = getUpdateQueryAttributes(updateConditionAttributeList, dbHandler.getElementMappings());
        String updateTableRowQuery = dbHandler.constructQuery(tableName, elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_UPDATE_TABLE), null, null, null, updateColumnValues, conditionBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Adding SQL Prepared Statement for execution plan " + executionPlanContext.getName() + " : " + updateTableRowQuery);
        }
        executionInfo.setPreparedUpdateStatement(updateTableRowQuery);
        updateConditionAttributeList.addAll(conditionAttributeList);
        executionInfo.setUpdateQueryColumnOrder(updateConditionAttributeList);

        //Constructing query to select table rows
        String selectTableRowQuery = dbHandler.constructQuery(tableName, elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_SELECT_TABLE), null, null, null, null, conditionBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Adding SQL Prepared Statement for execution plan " + executionPlanContext.getName() + " : " + selectTableRowQuery);
        }
        executionInfo.setPreparedSelectTableStatement(selectTableRowQuery);
        executionInfo.setConditionQueryColumnOrder(conditionAttributeList);

        //Constructing query to check for existence
        String isTableRowExistentQuery = dbHandler.constructQuery(tableName, elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_TABLE_ROW_EXIST), null, null, null, null, conditionBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Adding SQL Prepared Statement for execution plan " + executionPlanContext.getName() + " : " + isTableRowExistentQuery);
        }
        executionInfo.setPreparedTableRowExistenceCheckStatement(isTableRowExistentQuery);
        executionInfo.setConditionQueryColumnOrder(conditionAttributeList);

        Operator inMemoryEventTableOperator = parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, candidateDefinition, withinTime, cachingTable);
        return new RDBMSOperator(executionInfo, expressionExecutorList, dbHandler, inMemoryEventTableOperator, metaStateEvent.getMetaStreamEvent(matchingStreamIndex).getLastInputDefinition().getAttributeList().size());
    }


    /**
     * Method called to get the attribute object for attribute name
     */
    private static Attribute getAttribute(DBHandler dbHandler, String attributeName) {
        for (Attribute attribute : dbHandler.getAttributeList()) {
            if (attribute.getName().equals(attributeName)) {
                return attribute;
            }
        }
        //not-possible to happen
        return null;
    }

    /**
     * Method which constructs the update query string
     */
    private static StringBuilder getUpdateQueryAttributes(List<Attribute> updateConditionAttributeList, Map<String, String> elementMappings) {

        //Constructing (eg: information = ?  , latitude = ?) type values : column_values
        StringBuilder columnValues = new StringBuilder(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
        boolean appendComma = false;
        for (Attribute at : updateConditionAttributeList) {
            if (appendComma) {
                columnValues.append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER).append(elementMappings.get(RDBMSEventTableConstants
                        .EVENT_TABLE_RDBMS_COMMA)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            }
            columnValues.append(at.getName());
            columnValues.append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER).append(elementMappings.get(RDBMSEventTableConstants
                    .EVENT_TABLE_GENERIC_RDBMS_EQUAL)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER)
                    .append(elementMappings.get(RDBMSEventTableConstants
                            .EVENT_TABLE_RDBMS_QUESTION_MARK)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            appendComma = true;
        }
        return columnValues;
    }


    /**
     * Create necessary executors based on the Siddhi query in recursive manner
     */
    private static void buildConditionQuery(Map<String, Boolean> isTableStreamMap, Expression expression, StringBuilder conditionBuilder, List<Attribute> conditionAttributeList, List<ExpressionExecutor> expressionExecutorList, DBHandler dbHandler, Map<String, String> elementMappings, MetaComplexEvent metaStateEvent, int matchingStreamIndex,
                                            Map<String, EventTable> eventTableMap, List<VariableExpressionExecutor> variableExpressionExecutors,
                                            ExecutionPlanContext executionPlanContext, ExecutionInfo executionInfo) {


        if (expression instanceof And) {
            Expression leftExpression = ((And) expression).getLeftExpression();
            buildConditionQuery(isTableStreamMap, leftExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, executionInfo);
            conditionBuilder.append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER).append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_AND)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            Expression rightExpression = ((And) expression).getRightExpression();
            buildConditionQuery(isTableStreamMap, rightExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, executionInfo);
        } else if (expression instanceof Or) {
            Expression leftExpression = ((Or) expression).getLeftExpression();
            buildConditionQuery(isTableStreamMap, leftExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, executionInfo);
            conditionBuilder.append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER).append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_OR)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            Expression rightExpression = ((Or) expression).getRightExpression();
            buildConditionQuery(isTableStreamMap, rightExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, executionInfo);
        } else if (expression instanceof Compare) {
            Expression leftExpression = ((Compare) expression).getLeftExpression();
            Expression rightExpression = ((Compare) expression).getRightExpression();

            boolean isLeftExpressionEventTable = false;

            if (leftExpression instanceof Variable) {
                String streamId = ((Variable) leftExpression).getStreamId();
                if (streamId == null && (rightExpression instanceof Constant)) {
                    setEventTableVariableAttribute(leftExpression, conditionBuilder);
                    isLeftExpressionEventTable = true;
                } else if (streamId != null) {
                    Boolean isEvenTable = isTableStreamMap.get(streamId);
                    if (isEvenTable != null) {
                        if (isEvenTable) {
                            setEventTableVariableAttribute(leftExpression, conditionBuilder);
                            isLeftExpressionEventTable = true;
                        } else {
                            setExpressionExecutor(rightExpression, leftExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext);
                            isLeftExpressionEventTable = false;
                        }
                    }
                } else {
                    setExpressionExecutor(rightExpression, leftExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext);
                    isLeftExpressionEventTable = false;
                }
            } else if (leftExpression instanceof Constant) {
                setConstantValue(leftExpression, conditionBuilder);
                isLeftExpressionEventTable = false;

            }

            if (((Compare) expression).getOperator().equals(Compare.Operator.EQUAL)) {
                executionInfo.setIsBloomFilterCompatible(true);
                conditionBuilder.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_EQUAL)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.GREATER_THAN)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_GREATER_THAN)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.LESS_THAN)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_LESS_THAN)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.GREATER_THAN_EQUAL)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_GREATER_THAN_EQUAL)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.LESS_THAN_EQUAL)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_LESS_THAN_EQUAL)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.NOT_EQUAL)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_NOT_EQUAL)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            }

            if (isLeftExpressionEventTable) {
                if (rightExpression instanceof Variable) {
                    setExpressionExecutor(leftExpression, rightExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext);
                } else if (rightExpression instanceof Constant) {
                    setConstantValue(rightExpression, conditionBuilder);
                }
            } else {
                setEventTableVariableAttribute(rightExpression, conditionBuilder);
            }

        }
    }

    private static void setEventTableVariableAttribute(Expression expression, StringBuilder conditionBuilder) {
        String attributeName = ((Variable) expression).getAttributeName();
        conditionBuilder.append(attributeName).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
    }

    private static void setExpressionExecutor(Expression eventTableExpression, Expression expression, StringBuilder conditionBuilder, List<Attribute> conditionAttributeList, List<ExpressionExecutor> expressionExecutorList, DBHandler dbHandler, Map<String, String> elementMappings, MetaComplexEvent metaStateEvent, int matchingStreamIndex,
                                              Map<String, EventTable> eventTableMap, List<VariableExpressionExecutor> variableExpressionExecutors,
                                              ExecutionPlanContext executionPlanContext) {
        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
        conditionBuilder.append(elementMappings.get(RDBMSEventTableConstants
                .EVENT_TABLE_RDBMS_QUESTION_MARK)).append(RDBMSEventTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
        String attributeName = ((Variable) eventTableExpression).getAttributeName();
        conditionAttributeList.add(getAttribute(dbHandler, attributeName));
        expressionExecutorList.add(expressionExecutor);
    }

    private static void setConstantValue(Expression expression, StringBuilder conditionBuilder) {
        if (expression instanceof StringConstant) {
            String value = ((StringConstant) expression).getValue();
            conditionBuilder.append("'").append(value).append("'");
        } else if (expression instanceof IntConstant) {
            int value = ((IntConstant) expression).getValue();
            conditionBuilder.append(value);
        } else if (expression instanceof FloatConstant) {
            float value = ((FloatConstant) expression).getValue();
            conditionBuilder.append(value);
        } else if (expression instanceof LongConstant) {
            long value = ((LongConstant) expression).getValue();
            conditionBuilder.append(value);
        } else if (expression instanceof DoubleConstant) {
            double value = ((DoubleConstant) expression).getValue();
            conditionBuilder.append(value);
        } else if (expression instanceof BoolConstant) {
            boolean value = ((BoolConstant) expression).getValue();
            conditionBuilder.append(value ? RDBMSEventTableConstants.BOOLEAN_LITERAL_TRUE : RDBMSEventTableConstants.BOOLEAN_LITERAL_FALSE);
        }
    }

    /**
     * Method which creates the operator to perform event table cache related operations
     *
     * @param expression                  Expression
     * @param metaComplexEvent            MetaComplexEvent
     * @param executionPlanContext        ExecutionPlanContext
     * @param variableExpressionExecutors List of VariableExpressionExecutor
     * @param eventTableMap               EventTable Map
     * @param matchingStreamIndex         matching stream index
     * @param candidateDefinition         candidate definition
     * @param withinTime                  within time
     * @param cachingTable                caching table
     * @return Operator
     */
    public static Operator parse(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors,
                                 Map<String, EventTable> eventTableMap, int matchingStreamIndex, AbstractDefinition candidateDefinition, long withinTime, CachingTable cachingTable) {

        int candidateEventPosition = 0;
        int size = 0;

        MetaStreamEvent eventTableStreamEvent = new MetaStreamEvent();
        eventTableStreamEvent.setTableEvent(true);
        eventTableStreamEvent.addInputDefinition(candidateDefinition);
        for (Attribute attribute : candidateDefinition.getAttributeList()) {
            eventTableStreamEvent.addOutputData(attribute);
        }

        MetaStateEvent metaStateEvent = null;
        if (metaComplexEvent instanceof MetaStreamEvent) {
            metaStateEvent = new MetaStateEvent(2);
            metaStateEvent.addEvent(((MetaStreamEvent) metaComplexEvent));
            metaStateEvent.addEvent(eventTableStreamEvent);
            candidateEventPosition = 1;
            matchingStreamIndex = 0;
            size = 2;
        } else {

            MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents();

            //for join
            for (; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
                if (candidateEventPosition != matchingStreamIndex && metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(candidateDefinition)) {
                    metaStateEvent = ((MetaStateEvent) metaComplexEvent);
                    size = metaStreamEvents.length;
                    break;
                }
            }

            if (metaStateEvent == null) {
                metaStateEvent = new MetaStateEvent(metaStreamEvents.length + 1);
                for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
                    metaStateEvent.addEvent(metaStreamEvent);
                }
                metaStateEvent.addEvent(eventTableStreamEvent);
                candidateEventPosition = metaStreamEvents.length;
                size = metaStreamEvents.length + 1;
            }
        }

        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);

        return new CacheInMemoryOperator(expressionExecutor, candidateEventPosition, matchingStreamIndex, size, withinTime, cachingTable, metaStateEvent.getMetaStreamEvent(matchingStreamIndex).getLastInputDefinition().getAttributeList().size());
    }


}

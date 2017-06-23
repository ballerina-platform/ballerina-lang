/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.table.rdbms;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.extension.table.cache.CachingTable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.And;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.condition.IsNull;
import org.wso2.siddhi.query.api.expression.condition.Not;
import org.wso2.siddhi.query.api.expression.condition.Or;
import org.wso2.siddhi.query.api.expression.constant.BoolConstant;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.expression.constant.DoubleConstant;
import org.wso2.siddhi.query.api.expression.constant.FloatConstant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.LongConstant;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;

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
     * @param matchingMetaInfoHolder      MetaComplexEvent and details
     * @param siddhiAppContext        SiddhiAppContext
     * @param variableExpressionExecutors list of VariableExpressionExecutor
     * @param tableMap               Table map
     * @param cachingTable                caching table
     * @return Operator
     */
    public static Operator parse(DBHandler dbHandler, Expression expression, MatchingMetaInfoHolder
            matchingMetaInfoHolder, SiddhiAppContext siddhiAppContext, List<VariableExpressionExecutor>
            variableExpressionExecutors, Map<String, Table> tableMap, TableDefinition tableDefinition,
                                 CachingTable cachingTable, String queryName) {


        ExecutionInfo executionInfo = dbHandler.getExecutionInfoInstance();
        Map<String, String> elementMappings = dbHandler.getElementMappings();
        String tableName = dbHandler.getTableName();
        List<Attribute> conditionAttributeList = new ArrayList<Attribute>();

        List<Attribute> updateConditionAttributeList = new ArrayList<Attribute>();
        updateConditionAttributeList.addAll(matchingMetaInfoHolder.getMatchingStreamDefinition().getAttributeList());

        List<ExpressionExecutor> expressionExecutorList = new ArrayList<ExpressionExecutor>();

        StringBuilder conditionBuilder = new StringBuilder(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);

        Map<String, Boolean> isTableStreamMap = new HashMap<String, Boolean>();

        for (MetaStreamEvent metaStreamEvent : matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvents()) {
            String referenceId = metaStreamEvent.getInputReferenceId();
            AbstractDefinition abstractDefinition = metaStreamEvent.getLastInputDefinition();
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

        buildConditionQuery(isTableStreamMap, expression, conditionBuilder, conditionAttributeList,
                expressionExecutorList, dbHandler, elementMappings, matchingMetaInfoHolder.getMetaStateEvent(),
                matchingMetaInfoHolder.getMatchingStreamEventIndex(), tableMap, variableExpressionExecutors,
                siddhiAppContext, executionInfo, queryName);

        //Constructing query to delete a table row
        String deleteTableRowQuery = dbHandler.constructQuery(tableName, elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_DELETE_TABLE), null, null, null, null, conditionBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Adding SQL Prepared Statement for siddhi app " + siddhiAppContext.getName() + " : " +
                    deleteTableRowQuery);
        }
        executionInfo.setPreparedDeleteStatement(deleteTableRowQuery);
        executionInfo.setDeleteQueryColumnOrder(conditionAttributeList);

        //Constructing query to update a table row
        StringBuilder updateColumnValues = getUpdateQueryAttributes(updateConditionAttributeList, dbHandler.getElementMappings());
        String updateTableRowQuery = dbHandler.constructQuery(tableName, elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_UPDATE_TABLE), null, null, null, updateColumnValues, conditionBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Adding SQL Prepared Statement for siddhi app " + siddhiAppContext.getName() + " : " +
                    updateTableRowQuery);
        }
        executionInfo.setPreparedUpdateStatement(updateTableRowQuery);
        updateConditionAttributeList.addAll(conditionAttributeList);
        executionInfo.setUpdateQueryColumnOrder(updateConditionAttributeList);

        //Constructing query to select table rows
        String selectTableRowQuery = dbHandler.constructQuery(tableName, elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_SELECT_TABLE), null, null, null, null, conditionBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Adding SQL Prepared Statement for siddhi app " + siddhiAppContext.getName() + " : " +
                    selectTableRowQuery);
        }
        executionInfo.setPreparedSelectTableStatement(selectTableRowQuery);
        executionInfo.setConditionQueryColumnOrder(conditionAttributeList);

        //Constructing query to check for existence
        String isTableRowExistentQuery = dbHandler.constructQuery(tableName, elementMappings.get
                (RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_TABLE_ROW_EXIST), null, null, null, null,
                conditionBuilder);
        if (log.isDebugEnabled()) {
            log.debug("Adding SQL Prepared Statement for siddhi app " + siddhiAppContext.getName() + " : " +
                    isTableRowExistentQuery);
        }
        executionInfo.setPreparedTableRowExistenceCheckStatement(isTableRowExistentQuery);
        executionInfo.setConditionQueryColumnOrder(conditionAttributeList);

        Operator inMemoryTableOperator = null;
        if (cachingTable != null) {
            inMemoryTableOperator = OperatorParser.constructOperator(cachingTable.getCacheList(), expression,
                    matchingMetaInfoHolder,
                    siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
        }
        return new RDBMSOperator(executionInfo, expressionExecutorList, dbHandler, inMemoryTableOperator,
                matchingMetaInfoHolder.getMatchingStreamDefinition().getAttributeList().size());
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
    private static StringBuilder getUpdateQueryAttributes(List<Attribute> updateConditionAttributeList, Map<String,
            String> elementMappings) {

        //Constructing (eg: information = ?  , latitude = ?) type values : column_values
        StringBuilder columnValues = new StringBuilder(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
        boolean appendComma = false;
        for (Attribute at : updateConditionAttributeList) {
            if (appendComma) {
                columnValues.append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER).append(elementMappings.get(RDBMSTableConstants
                        .EVENT_TABLE_RDBMS_COMMA)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            }
            columnValues.append(at.getName());
            columnValues.append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER).append(elementMappings.get(RDBMSTableConstants
                    .EVENT_TABLE_GENERIC_RDBMS_EQUAL)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER)
                    .append(elementMappings.get(RDBMSTableConstants
                            .EVENT_TABLE_RDBMS_QUESTION_MARK)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            appendComma = true;
        }
        return columnValues;
    }


    /**
     * Create necessary executors based on the Siddhi query in recursive manner
     */
    private static void buildConditionQuery(Map<String, Boolean> isTableStreamMap, Expression expression,
                                            StringBuilder conditionBuilder, List<Attribute> conditionAttributeList,
                                            List<ExpressionExecutor> expressionExecutorList, DBHandler dbHandler,
                                            Map<String, String> elementMappings, MetaComplexEvent metaStateEvent, int
                                                    matchingStreamIndex,
                                            Map<String, Table> tableMap, List<VariableExpressionExecutor>
                                                    variableExpressionExecutors,
                                            SiddhiAppContext siddhiAppContext, ExecutionInfo executionInfo,
                                            String queryName) {


        if (expression instanceof And) {
            Expression leftExpression = ((And) expression).getLeftExpression();
            buildConditionQuery(isTableStreamMap, leftExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, tableMap, variableExpressionExecutors, siddhiAppContext, executionInfo, queryName);
            conditionBuilder.append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER).append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_AND)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            Expression rightExpression = ((And) expression).getRightExpression();
            buildConditionQuery(isTableStreamMap, rightExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, tableMap, variableExpressionExecutors, siddhiAppContext, executionInfo, queryName);
        } else if (expression instanceof Or) {
            Expression leftExpression = ((Or) expression).getLeftExpression();
            buildConditionQuery(isTableStreamMap, leftExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, tableMap, variableExpressionExecutors, siddhiAppContext, executionInfo, queryName);
            conditionBuilder.append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER).append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_OR)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            Expression rightExpression = ((Or) expression).getRightExpression();
            buildConditionQuery(isTableStreamMap, rightExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, tableMap, variableExpressionExecutors, siddhiAppContext, executionInfo, queryName);
        }  else if (expression instanceof Not) {
            Expression rightExpression = ((Not) expression).getExpression();
            conditionBuilder.append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER)
                    .append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_NOT))
                    .append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            buildConditionQuery(isTableStreamMap, rightExpression, conditionBuilder, conditionAttributeList,
                    expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex,
                    tableMap, variableExpressionExecutors, siddhiAppContext, executionInfo, queryName);
        } else if (expression instanceof IsNull) {
            Expression leftExpression = ((IsNull) expression).getExpression();
            String streamId = ((Variable) leftExpression).getStreamId();
            if (streamId != null) {
                Boolean isEvenTable = isTableStreamMap.get(streamId);
                if (isEvenTable != null) {
                    if (isEvenTable) {
                        setTableVariableAttribute(leftExpression, conditionBuilder);
                        conditionBuilder
                                .append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_IS_NULL))
                                .append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
                    }
                }
            }
        } else if (expression instanceof Compare) {
            Expression leftExpression = ((Compare) expression).getLeftExpression();
            Expression rightExpression = ((Compare) expression).getRightExpression();

            boolean isLeftExpressionTable = false;

            if (leftExpression instanceof Variable) {
                String streamId = ((Variable) leftExpression).getStreamId();
                if (streamId == null && (rightExpression instanceof Constant)) {
                    setTableVariableAttribute(leftExpression, conditionBuilder);
                    isLeftExpressionTable = true;
                } else if (streamId != null) {
                    Boolean isEvenTable = isTableStreamMap.get(streamId);
                    if (isEvenTable != null) {
                        if (isEvenTable) {
                            setTableVariableAttribute(leftExpression, conditionBuilder);
                            isLeftExpressionTable = true;
                        } else {
                            setExpressionExecutor(rightExpression, leftExpression, conditionBuilder,
                                    conditionAttributeList, expressionExecutorList, dbHandler, elementMappings,
                                    metaStateEvent, matchingStreamIndex, tableMap, variableExpressionExecutors,
                                    siddhiAppContext, queryName);
                            isLeftExpressionTable = false;
                        }
                    }
                } else {
                    setExpressionExecutor(rightExpression, leftExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, tableMap, variableExpressionExecutors, siddhiAppContext, queryName);
                    isLeftExpressionTable = false;
                }
            } else if (leftExpression instanceof Constant) {
                setConstantValue(leftExpression, conditionBuilder);
                isLeftExpressionTable = false;
            }

            if (((Compare) expression).getOperator().equals(Compare.Operator.EQUAL)) {
                executionInfo.setIsBloomFilterCompatible(true);
                conditionBuilder.append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_EQUAL)
                ).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.GREATER_THAN)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_GREATER_THAN)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.LESS_THAN)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_LESS_THAN)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.GREATER_THAN_EQUAL)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_GREATER_THAN_EQUAL)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.LESS_THAN_EQUAL)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_LESS_THAN_EQUAL)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            } else if (((Compare) expression).getOperator().equals(Compare.Operator.NOT_EQUAL)) {
                executionInfo.setIsBloomFilterCompatible(false);
                conditionBuilder.append(elementMappings.get(RDBMSTableConstants.EVENT_TABLE_GENERIC_RDBMS_NOT_EQUAL)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
            }

            if (isLeftExpressionTable) {
                if (rightExpression instanceof Variable) {
                    setExpressionExecutor(leftExpression, rightExpression, conditionBuilder, conditionAttributeList, expressionExecutorList, dbHandler, elementMappings, metaStateEvent, matchingStreamIndex, tableMap, variableExpressionExecutors, siddhiAppContext, queryName);
                } else if (rightExpression instanceof Constant) {
                    setConstantValue(rightExpression, conditionBuilder);
                }
            } else {
                setTableVariableAttribute(rightExpression, conditionBuilder);
            }

        }
    }

    private static void setTableVariableAttribute(Expression expression, StringBuilder conditionBuilder) {
        String attributeName = ((Variable) expression).getAttributeName();
        conditionBuilder.append(attributeName).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
    }

    private static void setExpressionExecutor(Expression tableExpression, Expression expression, StringBuilder
            conditionBuilder, List<Attribute> conditionAttributeList, List<ExpressionExecutor>
            expressionExecutorList, DBHandler dbHandler, Map<String, String> elementMappings, MetaComplexEvent
            metaStateEvent, int matchingStreamIndex,
                                              Map<String, Table> tableMap, List<VariableExpressionExecutor>
                                                      variableExpressionExecutors,
                                              SiddhiAppContext siddhiAppContext, String queryName) {
        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                metaStateEvent, matchingStreamIndex, tableMap, variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
        conditionBuilder.append(elementMappings.get(RDBMSTableConstants
                .EVENT_TABLE_RDBMS_QUESTION_MARK)).append(RDBMSTableConstants.EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER);
        String attributeName = ((Variable) tableExpression).getAttributeName();
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
            conditionBuilder.append(value ? RDBMSTableConstants.BOOLEAN_LITERAL_TRUE : RDBMSTableConstants.BOOLEAN_LITERAL_FALSE);
        }
    }

}

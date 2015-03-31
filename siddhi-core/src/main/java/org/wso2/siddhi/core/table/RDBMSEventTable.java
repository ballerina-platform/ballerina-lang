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


package org.wso2.siddhi.core.table;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.exception.CannotLoadConfigurationException;
import org.wso2.siddhi.core.exception.EventTableConnectionException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.table.cache.CachingTable;
import org.wso2.siddhi.core.table.rdbms.DBConfiguration;
import org.wso2.siddhi.core.table.rdbms.DBQueryHelper;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.RDBMSOperatorParser;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RDBMSEventTable implements EventTable {

    private final TableDefinition tableDefinition;
    private DBConfiguration dbConfiguration;
    private final String dataSourceName;
    private CachingTable cachedTable;

    private Logger log = Logger.getLogger(RDBMSEventTable.class);

    public RDBMSEventTable(TableDefinition tableDefinition, ExecutionPlanContext executionPlanContext) throws CannotLoadConfigurationException, EventTableConnectionException {
        this.tableDefinition = tableDefinition;

        Annotation fromAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_FROM,
                tableDefinition.getAnnotations());
        dataSourceName = fromAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_DATASOURCE_ID);
        String tableName = fromAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_TABLE_NAME);
        DataSource dataSource = executionPlanContext.getSiddhiContext().getSiddhiDataSource(dataSourceName);
        List<Attribute> attributeList = tableDefinition.getAttributeList();

        if (dataSourceName == null || tableName == null) {
            log.error("Invalid query specified. Required properties (datasourceName or/and tableName) not found ");
            return;
        }

        String cacheType = fromAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_CACHE);
        String cacheSizeInString = fromAnnotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_CACHE_SIZE);

        if (cacheType != null && cacheSizeInString != null) {
            cachedTable = new CachingTable(tableName, cacheType, cacheSizeInString, executionPlanContext, tableDefinition);
        }

        try {
            DBQueryHelper.loadConfiguration();
            this.dbConfiguration = new DBConfiguration(dataSource, tableName, attributeList);

            if (dataSource.getConnection() == null) {
                throw new EventTableConnectionException("Error while making connection to database");
            }

        } catch (CannotLoadConfigurationException e) {
            throw new CannotLoadConfigurationException("Error while loading the rdbms configuration file", e);
        } catch (SQLException e) {
            throw new EventTableConnectionException("Error while making connection to database", e);
        }
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        dbConfiguration.addEvent(addingEventChunk);
    }

    @Override
    public void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Operator operator) {
        operator.delete(deletingEventChunk, null);
    }

    @Override
    public void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Operator operator, int[] mappingPosition) {
        operator.update(updatingEventChunk, null, null);
    }

    @Override
    public boolean contains(ComplexEvent matchingEvent, Finder finder) {
        return finder.contains(matchingEvent, null);
    }

    @Override
    public Operator constructOperator(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return RDBMSOperatorParser.parse(dbConfiguration, expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime);
    }


    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, null, null);
    }

    @Override
    public Finder constructFinder(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return RDBMSOperatorParser.parse(dbConfiguration, expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, tableDefinition, withinTime);
    }


}

/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.siddhi.core.query.table.util;

import org.ballerinalang.siddhi.annotation.Example;
import org.ballerinalang.siddhi.annotation.Extension;
import org.ballerinalang.siddhi.core.exception.ConnectionUnavailableException;
import org.ballerinalang.siddhi.core.table.record.AbstractRecordTable;
import org.ballerinalang.siddhi.core.table.record.ExpressionBuilder;
import org.ballerinalang.siddhi.core.table.record.RecordIterator;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledExpression;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.query.api.annotation.Element;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;
import org.ballerinalang.siddhi.query.api.util.AnnotationHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.siddhi.core.util.SiddhiConstants.ANNOTATION_STORE;

/**
 * Custom store for testing purposes.
 */
@Extension(
        name = "test",
        namespace = "store",
        description = "Using this implementation a testing for store extension can be done.",
        examples = {
                @Example(
                        syntax = "@store(type='test')" +
                                "define table testTable (symbol string, price int, volume float); ",
                        description = "The above syntax initializes a test type store."
                )
        }
)
public class TestStore extends AbstractRecordTable {
    public static Map<String, String> systemConfigs;

    @Override
    protected void init(TableDefinition tableDefinition, ConfigReader configReader) {
        systemConfigs = new HashMap<>();
        systemConfigs = AnnotationHelper.getAnnotation(ANNOTATION_STORE, tableDefinition.getAnnotations())
                .getElements().stream().collect(Collectors.toMap(
                        Element::getKey, Element::getValue
                ));
    }

    @Override
    protected void add(List<Object[]> records) throws ConnectionUnavailableException {
        //Not Applicable
    }

    @Override
    protected RecordIterator<Object[]> find(Map<String, Object> findConditionParameterMap,
                                            CompiledCondition compiledCondition)
            throws ConnectionUnavailableException {
        //Not Applicable
        return null;
    }

    @Override
    protected boolean contains(Map<String, Object> containsConditionParameterMap,
                               CompiledCondition compiledCondition) throws ConnectionUnavailableException {
        //Not Applicable
        return false;
    }

    @Override
    protected void delete(List<Map<String, Object>> deleteConditionParameterMaps,
                          CompiledCondition compiledCondition) throws ConnectionUnavailableException {
        //Not Applicable
    }


    @Override
    protected void update(CompiledCondition updateCondition,
                          List<Map<String, Object>> updateConditionParameterMaps,
                          Map<String, CompiledExpression> updateSetExpressions,
                          List<Map<String, Object>> updateSetParameterMaps) throws ConnectionUnavailableException {
        //Not Applicable
    }

    /**
     * Try updating the records if they exist else add the records.
     *
     * @param updateCondition              the compiledCondition against which records should be matched for update
     * @param updateConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition based on which the records will be updated
     * @param updateSetExpressions         the set of updates mappings and related complied expressions
     * @param updateSetParameterMaps       map of matching StreamVariable Ids and their values corresponding to the
     *                                     update set
     * @param addingRecords                the values for adding new records if the update condition did not match
     */
    @Override
    protected void updateOrAdd(CompiledCondition updateCondition,
                               List<Map<String, Object>> updateConditionParameterMaps,
                               Map<String, CompiledExpression> updateSetExpressions,
                               List<Map<String, Object>> updateSetParameterMaps,
                               List<Object[]> addingRecords)
            throws ConnectionUnavailableException {
        //Not Applicable
    }

    @Override
    protected CompiledCondition compileCondition(ExpressionBuilder expressionBuilder) {
        return null;    //not implemented
    }

    /**
     * Compiles the expression in a set clause.
     *
     * @param expressionBuilder helps visiting the conditions in order to compile the condition
     * @return compiled expression that can be used for matching events in find, contains, delete, update and
     * updateOrAdd
     */
    @Override
    protected CompiledExpression compileSetAttribute(ExpressionBuilder expressionBuilder) {
        return null;
    }

    @Override
    protected void connect() throws ConnectionUnavailableException {
        //Not Applicable
    }

    @Override
    protected void disconnect() {
        //Not Applicable
    }

    @Override
    protected void destroy() {
        //Not Applicable
    }
}

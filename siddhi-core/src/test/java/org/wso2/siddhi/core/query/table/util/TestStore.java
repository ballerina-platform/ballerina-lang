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
package org.wso2.siddhi.core.query.table.util;

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.table.record.AbstractRecordTable;
import org.wso2.siddhi.core.table.record.ConditionBuilder;
import org.wso2.siddhi.core.table.record.RecordIterator;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.siddhi.core.util.SiddhiConstants.ANNOTATION_STORE;

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
    public static Map<String, String> storeConfigs;

    @Override
    protected void init(TableDefinition tableDefinition, ConfigReader configReader) {
        storeConfigs = new HashMap<>();
        storeConfigs = AnnotationHelper.getAnnotation(ANNOTATION_STORE, tableDefinition.getAnnotations())
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
                                            CompiledCondition compiledCondition) throws ConnectionUnavailableException {
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
    protected void update(List<Map<String, Object>> updateConditionParameterMaps, CompiledCondition compiledCondition,
                          List<Map<String, Object>> updateValues) throws ConnectionUnavailableException {
        //Not Applicable
    }

    @Override
    protected void updateOrAdd(List<Map<String, Object>> updateConditionParameterMaps,
                               CompiledCondition compiledCondition, List<Map<String, Object>> updateValues,
                               List<Object[]> addingRecords) throws ConnectionUnavailableException {
        //Not Applicable
    }

    @Override
    protected CompiledCondition compileCondition(ConditionBuilder conditionBuilder) {
        //Not Applicable
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

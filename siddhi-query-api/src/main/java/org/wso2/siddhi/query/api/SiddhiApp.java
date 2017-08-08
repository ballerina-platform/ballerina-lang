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
package org.wso2.siddhi.query.api;

import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;
import org.wso2.siddhi.query.api.definition.WindowDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.api.util.SiddhiConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Siddhi siddhi app
 */
public class SiddhiApp {

    private Map<String, StreamDefinition> streamDefinitionMap = new HashMap<String, StreamDefinition>();
    private Map<String, TableDefinition> tableDefinitionMap = new HashMap<String, TableDefinition>();
    private Map<String, WindowDefinition> windowDefinitionMap = new HashMap<String, WindowDefinition>();
    private Map<String, TriggerDefinition> triggerDefinitionMap = new HashMap<String, TriggerDefinition>();
    private Map<String, AggregationDefinition> aggregationDefinitionMap = new HashMap<String, AggregationDefinition>();
    private List<ExecutionElement> executionElementList = new ArrayList<ExecutionElement>();
    private List<String> executionElementNameList = new ArrayList<String>();
    private List<Annotation> annotations = new ArrayList<Annotation>();
    private Map<String, FunctionDefinition> functionDefinitionMap = new HashMap<String, FunctionDefinition>();

    public SiddhiApp(String name) {
        annotations.add(Annotation.annotation("info").element("name", name));
    }

    public SiddhiApp(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public SiddhiApp() {
    }

    public static SiddhiApp siddhiApp(String name) {
        return new SiddhiApp(name);
    }

    public static SiddhiApp siddhiApp() {
        return new SiddhiApp();
    }

    public Map<String, FunctionDefinition> getFunctionDefinitionMap() {
        return functionDefinitionMap;
    }

    public SiddhiApp defineStream(StreamDefinition streamDefinition) {
        if (streamDefinition == null) {
            throw new SiddhiAppValidationException("Stream Definition should not be null");
        } else if (streamDefinition.getId() == null) {
            throw new SiddhiAppValidationException("Stream Id should not be null for Stream Definition");
        }
        checkDuplicateDefinition(streamDefinition);
        this.streamDefinitionMap.put(streamDefinition.getId(), streamDefinition);
        return this;
    }

    public SiddhiApp defineAggregation(AggregationDefinition aggregationDefinition) {
        if (aggregationDefinition == null) {
            throw new SiddhiAppValidationException("Aggregation Definition should not be null");
        }
        if (aggregationDefinition.getId() == null) {
            throw new SiddhiAppValidationException("Aggregation Id should not be null for Aggregation Definition");
        }
        checkDuplicateDefinition(aggregationDefinition);
        this.aggregationDefinitionMap.put(aggregationDefinition.getId(), aggregationDefinition);

        return this;
    }

    public SiddhiApp defineTable(TableDefinition tableDefinition) {
        if (tableDefinition == null) {
            throw new SiddhiAppValidationException("Table Definition should not be null");
        } else if (tableDefinition.getId() == null) {
            throw new SiddhiAppValidationException("Table Id should not be null for Table Definition");
        }
        checkDuplicateDefinition(tableDefinition);
        this.tableDefinitionMap.put(tableDefinition.getId(), tableDefinition);
        return this;
    }

    public SiddhiApp defineWindow(WindowDefinition windowDefinition) {
        if (windowDefinition == null) {
            throw new SiddhiAppValidationException("Window Definition should not be null");
        } else if (windowDefinition.getId() == null) {
            throw new SiddhiAppValidationException("Window Id should not be null for Window Definition");
        }
        checkDuplicateDefinition(windowDefinition);
        this.windowDefinitionMap.put(windowDefinition.getId(), windowDefinition);
        return this;
    }

    public SiddhiApp defineTrigger(TriggerDefinition triggerDefinition) {
        if (triggerDefinition == null) {
            throw new SiddhiAppValidationException("Trigger Definition should not be null");
        } else if (triggerDefinition.getId() == null) {
            throw new SiddhiAppValidationException("Trigger Id should not be null for Trigger Definition");
        }
        StreamDefinition streamDefinition = StreamDefinition.id(triggerDefinition.getId()).attribute(SiddhiConstants
                .TRIGGERED_TIME, Attribute.Type.LONG);
        try {
            checkDuplicateDefinition(streamDefinition);
        } catch (DuplicateDefinitionException e) {
            throw new DuplicateDefinitionException("Trigger '" + triggerDefinition.getId() + "' cannot be defined as," +
                    " " + e.getMessage(), e);
        }
        if (triggerDefinitionMap.containsKey(triggerDefinition.getId())) {
            throw new DuplicateDefinitionException("Trigger Definition with same Id '" +
                    triggerDefinition.getId() + "' already exist '" + triggerDefinitionMap.get(triggerDefinition
                    .getId()) +
                    "', hence cannot add '" + triggerDefinition + "'");
        }
        this.triggerDefinitionMap.put(triggerDefinition.getId(), triggerDefinition);
        this.streamDefinitionMap.put(streamDefinition.getId(), streamDefinition);
        return this;
    }

    private void checkDuplicateDefinition(AbstractDefinition definition) {
        TableDefinition existingTableDefinition = tableDefinitionMap.get(definition.getId());
        if (existingTableDefinition != null && (!existingTableDefinition.equals(definition) || definition instanceof
                StreamDefinition)) {
            throw new DuplicateDefinitionException("Table Definition with same Stream Id '" +
                    definition.getId() + "' already exist : " + existingTableDefinition +
                    ", hence cannot add " + definition);
        }
        StreamDefinition existingStreamDefinition = streamDefinitionMap.get(definition.getId());
        if (existingStreamDefinition != null && (!existingStreamDefinition.equals(definition) || definition
                instanceof TableDefinition)) {
            throw new DuplicateDefinitionException("Stream Definition with same Stream Id '" +
                    definition.getId() + "' already exist : " + existingStreamDefinition +
                    ", hence cannot add " + definition);
        }
        WindowDefinition existingWindowDefinition = windowDefinitionMap.get(definition.getId());
        if (existingWindowDefinition != null && (!existingWindowDefinition.equals(definition) || definition
                instanceof WindowDefinition)) {
            throw new DuplicateDefinitionException("Stream Definition with same Window Id '" +
                    definition.getId() + "' already exist : " + existingWindowDefinition +
                    ", hence cannot add " + definition);
        }
        AggregationDefinition existingAggregationDefinition = aggregationDefinitionMap.get(definition.getId());
        if (existingAggregationDefinition != null
                && (!existingAggregationDefinition.equals(definition) || definition instanceof AggregationDefinition)) {
            throw new DuplicateDefinitionException("Aggregate Definition with same Aggregate Id '" + definition.getId()
                    + "' already exist : " + existingAggregationDefinition + ", hence cannot add " + definition);
        }
    }

    public SiddhiApp addQuery(Query query) {
        if (query == null) {
            throw new SiddhiAppValidationException("Query should not be null");
        }
        String name = null;
        Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_INFO, SiddhiConstants
                .ANNOTATION_ELEMENT_NAME, query.getAnnotations());
        if (element != null) {
            name = element.getValue();
        }
        if (name != null && executionElementNameList.contains(name)) {
throw new SiddhiAppValidationException(
                    "Cannot add Query as another Execution Element already uses " + "its name=" + name);
        }
        executionElementNameList.add(name);
        this.executionElementList.add(query);
        return this;
    }

    public SiddhiApp addPartition(Partition partition) {
        if (partition == null) {
            throw new SiddhiAppValidationException("Partition should not be null");
        }
        String name = null;
        Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_INFO, SiddhiConstants
                .ANNOTATION_ELEMENT_NAME, partition.getAnnotations());
        if (element != null) {
            name = element.getValue();
        }
        if (name != null && executionElementNameList.contains(name)) {
throw new SiddhiAppValidationException(
                    "Cannot add Partition as another Execution Element already " + "uses its name=" + name);
        }
        executionElementNameList.add(name);
        this.executionElementList.add(partition);
        return this;
    }

    public SiddhiApp annotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<ExecutionElement> getExecutionElementList() {
        return executionElementList;
    }

    public Map<String, StreamDefinition> getStreamDefinitionMap() {
        return streamDefinitionMap;
    }

    public Map<String, TableDefinition> getTableDefinitionMap() {
        return tableDefinitionMap;
    }

    public Map<String, TriggerDefinition> getTriggerDefinitionMap() {
        return triggerDefinitionMap;
    }

    public Map<String, WindowDefinition> getWindowDefinitionMap() {
        return windowDefinitionMap;
    }

    public Map<String, AggregationDefinition> getAggregationDefinitionMap() {
        return aggregationDefinitionMap;
    }

    @Override
    public String toString() {
return "SiddhiApp{" + "streamDefinitionMap=" + streamDefinitionMap + ", tableDefinitionMap="
                + tableDefinitionMap + ", windowDefinitionMap=" + windowDefinitionMap + ", aggregationDefinitionMap="
                + aggregationDefinitionMap + ", executionElementList=" + executionElementList
                + ", executionElementNameList=" + executionElementNameList + ", annotations=" + annotations + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SiddhiApp)) {
            return false;
        }

        SiddhiApp that = (SiddhiApp) o;

        if (annotations != null ? !annotations.equals(that.annotations) : that.annotations != null) {
            return false;
        }
        if (executionElementList != null ? !executionElementList.equals(that.executionElementList) : that
                .executionElementList != null) {
            return false;
        }
        if (executionElementNameList != null ? !executionElementNameList.equals(that.executionElementNameList) : that
                .executionElementNameList != null) {
            return false;
        }
        if (streamDefinitionMap != null ? !streamDefinitionMap.equals(that.streamDefinitionMap) : that
                .streamDefinitionMap != null) {
            return false;
        }
        if (tableDefinitionMap != null ? !tableDefinitionMap.equals(that.tableDefinitionMap) : that
                .tableDefinitionMap != null) {
            return false;
        }
        if (aggregationDefinitionMap != null ? !aggregationDefinitionMap.equals(that.aggregationDefinitionMap)
                : that.aggregationDefinitionMap != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = streamDefinitionMap != null ? streamDefinitionMap.hashCode() : 0;
        result = 31 * result + (tableDefinitionMap != null ? tableDefinitionMap.hashCode() : 0);
        result = 31 * result + (aggregationDefinitionMap != null ? aggregationDefinitionMap.hashCode() : 0);
        result = 31 * result + (executionElementList != null ? executionElementList.hashCode() : 0);
        result = 31 * result + (executionElementNameList != null ? executionElementNameList.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        return result;
    }

    public void defineFunction(FunctionDefinition functionDefinition) {
        if (functionDefinition == null) {
            throw new SiddhiAppValidationException("Function Definition should not be null");
        } else if (functionDefinition.getId() == null) {
            throw new SiddhiAppValidationException("Function Id should not be null for Function Definition");
        } else if (functionDefinition.getReturnType() == null) {
            throw new SiddhiAppValidationException("Return type should not be null for Function Definition");
        } else if (functionDefinition.getBody() == null) {
            throw new SiddhiAppValidationException("Body should not be null for Function Definition");
        } else if (functionDefinition.getLanguage() == null) {
            throw new SiddhiAppValidationException("Language should not be null for Function Definition");
        }
        checkDuplicateFunctionExist(functionDefinition);
        this.functionDefinitionMap.put(functionDefinition.getId(), functionDefinition);
    }

    private void checkDuplicateFunctionExist(FunctionDefinition functionDefinition) {
        if (this.functionDefinitionMap.get(functionDefinition.getId()) != null) {
            throw new DuplicateDefinitionException("The function definition with the same id exists " +
                    functionDefinition.getId());
        }
    }


}

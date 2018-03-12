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

package org.ballerinalang.siddhi.query.api.execution.partition;

import org.ballerinalang.siddhi.query.api.SiddhiElement;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.annotation.Element;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;
import org.ballerinalang.siddhi.query.api.execution.ExecutionElement;
import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.util.AnnotationHelper;
import org.ballerinalang.siddhi.query.api.util.SiddhiConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@linkplain org.ballerinalang.siddhi.query.api.execution.partition.Partition} class is used to represent
 * the definition of a partition for a Siddhi instance.
 */
public class Partition implements ExecutionElement, SiddhiElement {

    private static final long serialVersionUID = 1L;
    private Map<String, PartitionType> partitionTypeMap = new HashMap<String, PartitionType>();
    private List<Query> queryList = new ArrayList<Query>();
    private List<String> queryNameList = new ArrayList<String>();
    private List<Annotation> annotations = new ArrayList<Annotation>();
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public static Partition partition() {
        return new Partition();
    }

    public static RangePartitionType.RangePartitionProperty range(String partitionKey, Expression condition) {
        return new RangePartitionType.RangePartitionProperty(partitionKey, condition);
    }

    public Map<String, PartitionType> getPartitionTypeMap() {
        return partitionTypeMap;
    }

    public Partition with(String streamId, Expression expression) {
        ValuePartitionType valuePartitionType = new ValuePartitionType(streamId, expression);
        addPartitionType(valuePartitionType);
        return this;
    }

    public Partition with(String streamId, RangePartitionType.RangePartitionProperty... rangePartitionProperties) {
        PartitionType rangePartitionType = new RangePartitionType(streamId, rangePartitionProperties);
        addPartitionType(rangePartitionType);
        return this;
    }

    public Partition with(PartitionType partitionType) {
        addPartitionType(partitionType);
        return this;
    }

    public Partition addQuery(Query query) {
        if (query == null) {
            throw new SiddhiAppValidationException("Query should not be null");
        }
        String name = null;
        Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_INFO, SiddhiConstants
                .ANNOTATION_ELEMENT_NAME, query.getAnnotations());
        if (element != null) {
            name = element.getValue();
        }
        if (name != null && queryNameList.contains(name)) {
            throw new SiddhiAppValidationException("Cannot add Query as another Execution Element already uses " +
                    "its name=" + name + " within the same Partition",
                    element.getQueryContextStartIndex(), element.getQueryContextEndIndex());
        }
        queryNameList.add(name);
        this.queryList.add(query);
        return this;
    }

    private void addPartitionType(PartitionType partitionType) {
        String partitionedStream = partitionType.getStreamId();
        if (partitionTypeMap.containsKey(partitionedStream)) {
            throw new SiddhiAppValidationException("Duplicate partition for Stream " + partitionedStream + "!, "
                    + partitionType.toString() + " cannot be added as " + partitionTypeMap.get(partitionType
                    .getStreamId()) + " already exist.", partitionType.getQueryContextStartIndex(),
                    partitionType.getQueryContextEndIndex());
        }
        partitionTypeMap.put(partitionType.getStreamId(), partitionType);
    }

    public List<Query> getQueryList() {
        return queryList;
    }

    public Partition annotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return "Partition{" +
                "partitionTypeMap=" + partitionTypeMap +
                ", queryList=" + queryList +
                ", annotations=" + annotations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Partition)) {
            return false;
        }

        Partition partition = (Partition) o;

        if (annotations != null ? !annotations.equals(partition.annotations) : partition.annotations != null) {
            return false;
        }
        if (partitionTypeMap != null ? !partitionTypeMap.equals(partition.partitionTypeMap) : partition
                .partitionTypeMap != null) {
            return false;
        }
        if (queryList != null ? !queryList.equals(partition.queryList) : partition.queryList != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = partitionTypeMap != null ? partitionTypeMap.hashCode() : 0;
        result = 31 * result + (queryList != null ? queryList.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        return result;
    }

    @Override
    public int[] getQueryContextStartIndex() {
        return queryContextStartIndex;
    }

    @Override
    public void setQueryContextStartIndex(int[] lineAndColumn) {
        queryContextStartIndex = lineAndColumn;
    }

    @Override
    public int[] getQueryContextEndIndex() {
        return queryContextEndIndex;
    }

    @Override
    public void setQueryContextEndIndex(int[] lineAndColumn) {
        queryContextEndIndex = lineAndColumn;
    }
}

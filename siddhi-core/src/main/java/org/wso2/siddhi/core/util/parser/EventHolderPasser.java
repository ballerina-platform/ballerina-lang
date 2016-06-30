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

package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.table.holder.EventHolder;
import org.wso2.siddhi.core.table.holder.ListEventHolder;
import org.wso2.siddhi.core.table.holder.PrimaryKeyEventHolder;
import org.wso2.siddhi.core.table.holder.PrimaryKeyIndexEventHolder;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suho on 5/26/16.
 */
public class EventHolderPasser {
    public static EventHolder parse(AbstractDefinition tableDefinition, StreamEventPool tableStreamEventPool) {
        ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();

        // indexes.
        Annotation indexByAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_INDEX_BY,
                tableDefinition.getAnnotations());
        if (indexByAnnotation != null) {
            if (indexByAnnotation.getElements().size() > 1) {
                throw new OperationNotSupportedException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation contains " +
                        indexByAnnotation.getElements().size() +
                        " elements, Siddhi in-memory table only supports indexing based on a single attribute");
            }
            if (indexByAnnotation.getElements().size() == 0) {
                throw new ExecutionPlanValidationException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation contains "
                        + indexByAnnotation.getElements().size() + " element");
            }
            String indexAttributesString = indexByAnnotation.getElements().get(0).getValue();
            //todo fix indexing annotation
            String[] indexAttributes = indexAttributesString.split(",");
            if (indexAttributes.length > 1) {
                int indexPosition = tableDefinition.getAttributePosition(indexAttributes[0].trim());
                Map<String, Integer> indexMetaData = new HashMap<String, Integer>();
                for (int i = 1; i == indexAttributes.length - 1; i++) {
                    indexMetaData.put(indexAttributes[i].trim(), tableDefinition.getAttributePosition(indexAttributes[i].trim()));
                }
                return new PrimaryKeyIndexEventHolder(tableStreamEventPool, eventConverter, indexPosition, indexAttributes[0].trim(), indexMetaData);

            } else {
                int indexPosition = tableDefinition.getAttributePosition(indexAttributes[0].trim());
                return new PrimaryKeyEventHolder(tableStreamEventPool, eventConverter, indexPosition, indexAttributes[0].trim());

            }
        } else {
            return new ListEventHolder(tableStreamEventPool, eventConverter);
        }
    }
}

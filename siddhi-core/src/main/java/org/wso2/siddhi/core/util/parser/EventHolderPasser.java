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

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.table.holder.EventHolder;
import org.wso2.siddhi.core.table.holder.IndexEventHolder;
import org.wso2.siddhi.core.table.holder.ListEventHolder;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to parse {@link EventHolder}
 */
public class EventHolderPasser {
    private static final Logger log = Logger.getLogger(EventHolderPasser.class);

    public static EventHolder parse(AbstractDefinition tableDefinition, StreamEventPool tableStreamEventPool) {
        ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();

        String primaryKeyAttribute = null;
        int primaryKeyPosition = -1;

        Map<String, Integer> indexMetaData = new HashMap<String, Integer>();

        // primaryKey.
        Annotation primaryKeyAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_PRIMARY_KEY,
                                                                         tableDefinition.getAnnotations());
        if (primaryKeyAnnotation != null) {
            if (primaryKeyAnnotation.getElements().size() > 1) {
                throw new OperationNotSupportedException(SiddhiConstants.ANNOTATION_PRIMARY_KEY + " annotation " +
                                                                 "contains " +
                                                                 primaryKeyAnnotation.getElements().size() +
                                                                 " elements, Siddhi in-memory table only supports indexing based on a single attribute, " +
                                                                 "at '" + tableDefinition.getId() + "'");
            }
            if (primaryKeyAnnotation.getElements().size() == 0) {
                throw new SiddhiAppValidationException(SiddhiConstants.ANNOTATION_PRIMARY_KEY + " annotation " +
                                                                   "contains "
                                                                   + primaryKeyAnnotation.getElements().size() + " element, at '" + tableDefinition.getId() + "'");
            }
            primaryKeyAttribute = primaryKeyAnnotation.getElements().get(0).getValue().trim();
            primaryKeyPosition = tableDefinition.getAttributePosition(primaryKeyAttribute);

        }

        // indexes.
        Annotation indexAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_INDEX,
                                                                    tableDefinition.getAnnotations());
        if (indexAnnotation != null) {
            if (indexAnnotation.getElements().size() == 0) {
                throw new SiddhiAppValidationException(SiddhiConstants.ANNOTATION_INDEX + " annotation contains "
                                                                   + indexAnnotation.getElements().size() + " element");
            }
            for (Element element : indexAnnotation.getElements()) {
                Integer previousValue = indexMetaData.put(element.getValue().trim(), tableDefinition
                        .getAttributePosition(element.getValue().trim()));
                if (previousValue != null) {
                    throw new SiddhiAppCreationException("Multiple " + SiddhiConstants.ANNOTATION_INDEX + " " +
                                                                     "annotations defined with same attribute '" + element.getValue().trim() + "', at '" +
                                                                     tableDefinition.getId() + "'");
                }
            }
        }

        // not support indexBy.
        Annotation indexByAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_INDEX_BY,
                                                                      tableDefinition.getAnnotations());
        if (indexByAnnotation != null) {
            throw new OperationNotSupportedException(SiddhiConstants.ANNOTATION_INDEX_BY + " annotation is not " +
                                                             "supported anymore, please use @PrimaryKey or @Index annotations instead," +
                                                             " at '" + tableDefinition.getId() + "'");
        }

        if (primaryKeyAttribute != null || indexMetaData.size() > 0) {
            boolean isNumeric = false;
            if (primaryKeyAttribute != null) {
                Attribute.Type type = tableDefinition.getAttributeType(primaryKeyAttribute);
                if (type == Attribute.Type.DOUBLE || type == Attribute.Type.FLOAT || type == Attribute.Type.INT ||
                        type == Attribute.Type.LONG) {
                    isNumeric = true;
                }
            }
            return new IndexEventHolder(tableStreamEventPool, eventConverter, primaryKeyPosition, primaryKeyAttribute,
                                        isNumeric, indexMetaData);
        } else {
            return new ListEventHolder(tableStreamEventPool, eventConverter);
        }
    }
}

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

package org.ballerinalang.siddhi.core.util.parser;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.ballerinalang.siddhi.core.exception.OperationNotSupportedException;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.table.holder.EventHolder;
import org.ballerinalang.siddhi.core.table.holder.IndexEventHolder;
import org.ballerinalang.siddhi.core.table.holder.ListEventHolder;
import org.ballerinalang.siddhi.core.table.holder.PrimaryKeyReferenceHolder;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.annotation.Element;
import org.ballerinalang.siddhi.query.api.definition.AbstractDefinition;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;
import org.ballerinalang.siddhi.query.api.util.AnnotationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to parse {@link EventHolder}.
 */
public class EventHolderPasser {
    private static final Logger log = LoggerFactory.getLogger(EventHolderPasser.class);

    public static EventHolder parse(AbstractDefinition tableDefinition, StreamEventPool tableStreamEventPool,
                                    SiddhiAppContext siddhiAppContext) {
        ZeroStreamEventConverter eventConverter = new ZeroStreamEventConverter();

        PrimaryKeyReferenceHolder[] primaryKeyReferenceHolders = null;

        Map<String, Integer> indexMetaData = new HashMap<String, Integer>();

        // primaryKey.
        Annotation primaryKeyAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_PRIMARY_KEY,
                tableDefinition.getAnnotations());
        if (primaryKeyAnnotation != null) {
            if (primaryKeyAnnotation.getElements().size() == 0) {
                throw new SiddhiAppValidationException(SiddhiConstants.ANNOTATION_PRIMARY_KEY + " annotation " +
                        "contains " + primaryKeyAnnotation.getElements().size() + " element, at '" +
                        tableDefinition.getId() + "'");
            }
            primaryKeyReferenceHolders = primaryKeyAnnotation.getElements().stream()
                    .map(element -> element.getValue().trim())
                    .map(key -> new PrimaryKeyReferenceHolder(key, tableDefinition.getAttributePosition(key)))
                    .toArray(PrimaryKeyReferenceHolder[]::new);
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
                            tableDefinition.getId() + "'", indexAnnotation.getQueryContextStartIndex(),
                            indexAnnotation.getQueryContextEndIndex());
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

        if (primaryKeyReferenceHolders != null || indexMetaData.size() > 0) {
            boolean isNumeric = false;
            if (primaryKeyReferenceHolders != null) {
                if (primaryKeyReferenceHolders.length == 1) {
                    Attribute.Type type = tableDefinition.getAttributeType(
                            primaryKeyReferenceHolders[0].getPrimaryKeyAttribute());
                    if (type == Attribute.Type.DOUBLE || type == Attribute.Type.FLOAT || type == Attribute.Type.INT ||
                            type == Attribute.Type.LONG) {
                        isNumeric = true;
                    }
                }

            }
            return new IndexEventHolder(tableStreamEventPool, eventConverter, primaryKeyReferenceHolders, isNumeric,
                    indexMetaData, tableDefinition, siddhiAppContext);
        } else {
            return new ListEventHolder(tableStreamEventPool, eventConverter);
        }
    }


}

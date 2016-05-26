package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.table.holder.EventHolder;
import org.wso2.siddhi.core.table.holder.ListEventHolder;
import org.wso2.siddhi.core.table.holder.PrimaryKeyEventHolder;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

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
            String indexAttribute = indexByAnnotation.getElements().get(0).getValue();
            int indexPosition = tableDefinition.getAttributePosition(indexAttribute);
            return new PrimaryKeyEventHolder(tableStreamEventPool, eventConverter, indexPosition, indexAttribute);
        } else {
            return new ListEventHolder(tableStreamEventPool, eventConverter);
        }
    }
}

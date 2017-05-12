package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.UUID;

public class AggregationRuntime {
    private final ExecutionPlanContext executionPlanContext;
    private AggregationDefinition aggregationDefinition;
    private StreamRuntime streamRuntime;
    private MetaComplexEvent metaComplexEvent;
    private String aggregatorID;

    private IncrementalExecutor incrementalExecutor;

    public AggregationRuntime(AggregationDefinition aggregationDefinition, ExecutionPlanContext executionPlanContext,
                              StreamRuntime streamRuntime, MetaComplexEvent metaComplexEvent) {
        this.aggregationDefinition = aggregationDefinition;
        this.executionPlanContext = executionPlanContext;
        this.metaComplexEvent = metaComplexEvent;
        this.streamRuntime = streamRuntime;

        setId();
    }

    public void setIncrementalExecutor(IncrementalExecutor incrementalExecutor) {
        this.incrementalExecutor = incrementalExecutor;
    }

    public IncrementalExecutor getIncrementalExecutor() {
        return this.incrementalExecutor;
    }

    private void setId() {
        try {
            Element element = AnnotationHelper.getAnnotationElement("info", "name", aggregationDefinition.getAnnotations());
            if (element != null) {
                aggregatorID = element.getValue();

            }
        } catch (DuplicateAnnotationException e) {
            throw new DuplicateAnnotationException(e.getMessage() + " for the same Aggregator " + aggregationDefinition.toString());
        }
        if (aggregatorID == null) {
            aggregatorID = UUID.randomUUID().toString();
        }
    }

    public String getAggregatorID() {
        return aggregatorID;
    }
}

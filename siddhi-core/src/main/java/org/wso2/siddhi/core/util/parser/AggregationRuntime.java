package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;

public class AggregationRuntime {
    private final ExecutionPlanContext executionPlanContext;
    private AggregationDefinition aggregationDefinition;
    private StreamRuntime streamRuntime;
    private MetaComplexEvent metaComplexEvent;

    private IncrementalExecutor incrementalExecutor;

    public AggregationRuntime(AggregationDefinition aggregationDefinition, ExecutionPlanContext executionPlanContext,
                              StreamRuntime streamRuntime, MetaComplexEvent metaComplexEvent) {
        this.aggregationDefinition = aggregationDefinition;
        this.executionPlanContext = executionPlanContext;
        this.metaComplexEvent = metaComplexEvent;
        this.streamRuntime = streamRuntime;
    }

    public void setIncrementalExecutor(IncrementalExecutor incrementalExecutor) {
        this.incrementalExecutor = incrementalExecutor;
    }

    public IncrementalExecutor getIncrementalExecutor() {
        return this.incrementalExecutor;
    }
}

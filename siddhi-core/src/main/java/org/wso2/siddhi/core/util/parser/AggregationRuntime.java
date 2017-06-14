package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.ExecuteStreamReceiver;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.Executor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;

public class AggregationRuntime {
    private final ExecutionPlanContext executionPlanContext;
    private AggregationDefinition aggregationDefinition;
    private StreamRuntime streamRuntime;
    private MetaComplexEvent metaComplexEvent;
    private ExecuteStreamReceiver executeStreamReceiver;

    private Executor entryValveExecutor;

    public AggregationRuntime(AggregationDefinition aggregationDefinition, ExecutionPlanContext executionPlanContext,
                              StreamRuntime streamRuntime, MetaComplexEvent metaComplexEvent, ExecuteStreamReceiver executeStreamReceiver) {
        this.aggregationDefinition = aggregationDefinition;
        this.executionPlanContext = executionPlanContext;
        this.metaComplexEvent = metaComplexEvent;
        this.streamRuntime = streamRuntime;
        this.executeStreamReceiver = executeStreamReceiver;
    }

    public void setExecutor(Executor executor) {
        this.entryValveExecutor = executor;
    }

    public Executor getExecutor() {
        return this.entryValveExecutor;
    }

    public StreamRuntime getStreamRuntime() {
        return streamRuntime;
    }

    public ExecuteStreamReceiver getExecuteStreamReceiver() {
        return executeStreamReceiver;
    }
}

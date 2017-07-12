package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecuteStreamReceiver;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.Executor;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;

public class AggregationRuntime {
    private StreamRuntime streamRuntime;
    private IncrementalExecuteStreamReceiver incrementalExecuteStreamReceiver;

    private Executor entryValveExecutor;

    public AggregationRuntime(AggregationDefinition aggregationDefinition, SiddhiAppContext siddhiAppContext,
                              StreamRuntime streamRuntime, MetaComplexEvent metaComplexEvent, IncrementalExecuteStreamReceiver incrementalExecuteStreamReceiver) {
        this.streamRuntime = streamRuntime;
        this.incrementalExecuteStreamReceiver = incrementalExecuteStreamReceiver;
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

    public IncrementalExecuteStreamReceiver getIncrementalExecuteStreamReceiver() {
        return incrementalExecuteStreamReceiver;
    }
}

package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveExecutor;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;

public class AggregationRuntime {
    private final AggregationDefinition aggregationDefinition;
    private final SiddhiAppContext siddhiAppContext;
    private SingleStreamRuntime singleStreamRuntime;
    private IncrementalExecutor incrementalExecutorChain;
    private EntryValveExecutor entryValveExecutor;

    public AggregationRuntime(AggregationDefinition aggregationDefinition, SiddhiAppContext siddhiAppContext,
                              SingleStreamRuntime singleStreamRuntime, IncrementalExecutor incrementalExecutorChain,
                              EntryValveExecutor entryValveExecutor) {
        this.aggregationDefinition = aggregationDefinition;
        this.siddhiAppContext = siddhiAppContext;
        this.singleStreamRuntime = singleStreamRuntime;
        this.incrementalExecutorChain = incrementalExecutorChain;
        this.entryValveExecutor = entryValveExecutor;
    }

    public AggregationDefinition getAggregationDefinition() {
        return aggregationDefinition;
    }

    public SiddhiAppContext getSiddhiAppContext() {
        return siddhiAppContext;
    }

    public SingleStreamRuntime getSingleStreamRuntime() {
        return singleStreamRuntime;
    }

    public IncrementalExecutor getIncrementalExecutorChain() {
        return incrementalExecutorChain;
    }

    public EntryValveExecutor getEntryValveExecutor() {
        return entryValveExecutor;
    }
}

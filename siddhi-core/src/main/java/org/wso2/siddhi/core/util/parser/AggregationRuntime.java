package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveExecutor;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;

public class AggregationRuntime {
    private final AggregationDefinition aggregationDefinition;
    private final SiddhiAppContext siddhiAppContext;
    private SingleStreamRuntime singleStreamRuntime;
    private EntryValveExecutor entryValveExecutor;

    public AggregationRuntime(AggregationDefinition aggregationDefinition, SiddhiAppContext siddhiAppContext,
                              SingleStreamRuntime singleStreamRuntime, EntryValveExecutor entryValveExecutor) {
        this.aggregationDefinition = aggregationDefinition;
        this.siddhiAppContext = siddhiAppContext;
        this.singleStreamRuntime = singleStreamRuntime;
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

    public EntryValveExecutor getEntryValveExecutor() {
        return entryValveExecutor;
    }
}

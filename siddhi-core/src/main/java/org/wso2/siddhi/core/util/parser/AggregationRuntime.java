package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveExecutor;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;

import java.util.HashMap;
import java.util.Map;

public class AggregationRuntime {
    private final AggregationDefinition aggregationDefinition;
    private final Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap;
    private final HashMap<TimePeriod.Duration, Table> aggregationTables;
    private final SiddhiAppContext siddhiAppContext;
    private SingleStreamRuntime singleStreamRuntime;
    private IncrementalExecutor incrementalExecutorChain;
    private EntryValveExecutor entryValveExecutor;

    public AggregationRuntime(AggregationDefinition aggregationDefinition,
                              Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
                              HashMap<TimePeriod.Duration, Table> aggregationTables,
                              SingleStreamRuntime singleStreamRuntime, IncrementalExecutor incrementalExecutorChain,
                              EntryValveExecutor entryValveExecutor, SiddhiAppContext siddhiAppContext) {
        this.aggregationDefinition = aggregationDefinition;
        this.incrementalExecutorMap = incrementalExecutorMap;
        this.aggregationTables = aggregationTables;
        this.siddhiAppContext = siddhiAppContext;
        this.singleStreamRuntime = singleStreamRuntime;
        this.incrementalExecutorChain = incrementalExecutorChain;
        this.entryValveExecutor = entryValveExecutor;
    }


    public Map<TimePeriod.Duration, IncrementalExecutor> getIncrementalExecutorMap() {
        return incrementalExecutorMap;
    }

    public HashMap<TimePeriod.Duration, Table> getAggregationTables() {
        return aggregationTables;
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

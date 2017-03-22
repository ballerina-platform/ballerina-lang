package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryIncrementalStore implements IncrementalStore {
    private ConcurrentMap<String, List<FunctionExecutor>> incrementalStore = null;

    public InMemoryIncrementalStore(List<FunctionExecutor> baseFunctionExecutors) {
        incrementalStore = new ConcurrentHashMap<>();
        for(FunctionExecutor executor : baseFunctionExecutors){
            incrementalStore.putIfAbsent(executor)
        }
    }

    @Override
    public void add(Object data, AttributeAggregator aggregator) {

    }

    @Override
    public Object get(AttributeAggregator aggregator) {
        return null;
    }

    @Override
    public void reset(AttributeAggregator aggregator) {

    }
}

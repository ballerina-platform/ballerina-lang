package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryIncrementalStore implements IncrementalStore {
    private ConcurrentMap<String, List<FunctionExecutor>> incrementalStore = null;

    public InMemoryIncrementalStore(List<FunctionExecutor> baseFunctionExecutors) { // TODO: 5/11/17 ExpressionExecutors?
        incrementalStore = new ConcurrentHashMap<>();
        for(FunctionExecutor executor : baseFunctionExecutors){
           // incrementalStore.putIfAbsent(executor);
        }
    }

    @Override
    public void add(long timestamp, Object data) {

    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public void reset() {

    }
}

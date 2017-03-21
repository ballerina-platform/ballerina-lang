package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIncrementalStore implements IncrementalStore {
    private ConcurrentHashMap<String, List<AttributeAggregator>> incrementalStore = null;

    public InMemoryIncrementalStore(List<Expression> baseCalculators) {
        // incrementalStore consists of a list of AttributeAggregator hashed by attribute name
        // TODO: 3/20/17 : can two attribute have the same attribute name
        for(Expression incrementalAggregator : baseCalculators){
           
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

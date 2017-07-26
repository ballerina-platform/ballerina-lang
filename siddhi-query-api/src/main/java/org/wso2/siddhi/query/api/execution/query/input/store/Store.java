package org.wso2.siddhi.query.api.execution.query.input.store;

import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Store
 */
public class Store extends BasicSingleInputStream implements InputStore {

    private static final long serialVersionUID = 1L;

    protected Store(String streamId) {
        super(streamId);
    }

    protected Store(String storeReferenceId, String storeId) {
        super(storeReferenceId, storeId);
    }

    public InputStore on(Expression onCondition, Within within, Expression per) {
        return new AggregationInputStore(this, onCondition, within, per);
    }

    public InputStore on(Expression onCondition) {
        return new ConditionInputStore(this, onCondition);
    }

    public InputStore on(Within within, Expression per) {
        return new AggregationInputStore(this, within, per);
    }

    @Override
    public String getStoreReferenceId() {
        return streamReferenceId;
    }

    @Override
    public String getStoreId() {
        return streamId;
    }

    @Override
    public String toString() {
        return "Store{" +
                "isInnerStream=" + isInnerStream +
                ", streamId='" + streamId + '\'' +
                ", streamReferenceId='" + streamReferenceId + '\'' +
                ", streamHandlers=" + streamHandlers +
                ", windowPosition=" + windowPosition +
                '}';
    }


}

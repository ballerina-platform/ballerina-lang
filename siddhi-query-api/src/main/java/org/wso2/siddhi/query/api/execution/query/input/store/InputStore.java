package org.wso2.siddhi.query.api.execution.query.input.store;

import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Input Store
 */
public interface InputStore {

    static Store store(String storeId) {
        return new Store(storeId);
    }

    static Store store(String storeReferenceId, String storeId) {
        return new Store(storeReferenceId, storeId);
    }

    static InputStore store(String storeReferenceId, String storeId, Within within, Expression per) {
        return new AggregationInputStore(storeReferenceId, storeId, within, per);
    }

    static InputStore store(String storeId, Within within, Expression per) {
        return new AggregationInputStore(storeId, within, per);
    }

    String getStoreReferenceId();

    String getStoreId();

}

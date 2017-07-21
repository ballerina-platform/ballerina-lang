package org.wso2.siddhi.query.api.execution.query.input.store;

import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;

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

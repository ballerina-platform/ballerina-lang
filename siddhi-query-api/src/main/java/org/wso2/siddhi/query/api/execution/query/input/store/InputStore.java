package org.wso2.siddhi.query.api.execution.query.input.store;

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

    String getStoreReferenceId();

    String getStoreId();

}

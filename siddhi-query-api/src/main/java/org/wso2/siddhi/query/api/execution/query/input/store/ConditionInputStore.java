package org.wso2.siddhi.query.api.execution.query.input.store;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Condition Input Store
 */
public class ConditionInputStore implements InputStore {

    protected final Store store;
    protected Expression onCondition = null;

    protected ConditionInputStore(Store store, Expression onCondition) {
        this.store = store;
        this.onCondition = onCondition;
    }

    @Override
    public String getStoreReferenceId() {
        return store.getStoreReferenceId();
    }

    @Override
    public String getStoreId() {
        return store.getStoreId();
    }

    public Store getStore() {
        return store;
    }

    public Expression getOnCondition() {
        return onCondition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConditionInputStore that = (ConditionInputStore) o;

        if (store != null ? !store.equals(that.store) : that.store != null) {
            return false;
        }
        return onCondition != null ? onCondition.equals(that.onCondition) : that.onCondition == null;
    }

    @Override
    public int hashCode() {
        int result = store != null ? store.hashCode() : 0;
        result = 31 * result + (onCondition != null ? onCondition.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConditionInputStore{" +
                "store=" + store +
                ", onCondition=" + onCondition +
                '}';
    }
}

package org.wso2.siddhi.query.api.execution.query.input.store;

import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Aggregation Input Store
 */
public class AggregationInputStore implements InputStore {

    private final String storeId;
    private final Within within;
    private final Expression per;
    private String storeReferenceId = null;

    public AggregationInputStore(String storeReferenceId, String storeId, Within within, Expression per) {

        this.storeReferenceId = storeReferenceId;
        this.storeId = storeId;
        this.within = within;
        this.per = per;
    }

    public AggregationInputStore(String storeId, Within within, Expression per) {

        this.storeId = storeId;
        this.within = within;
        this.per = per;
    }

    public String getStoreReferenceId() {
        return storeReferenceId;
    }

    public String getStoreId() {
        return storeId;
    }

    public Within getWithin() {
        return within;
    }

    public Expression getPer() {
        return per;
    }

    @Override
    public String toString() {
        return "AggregationInputStore{" +
                "storeId='" + storeId + '\'' +
                ", within=" + within +
                ", per=" + per +
                ", storeReferenceId='" + storeReferenceId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AggregationInputStore that = (AggregationInputStore) o;

        if (storeId != null ? !storeId.equals(that.storeId) : that.storeId != null) {
            return false;
        }
        if (within != null ? !within.equals(that.within) : that.within != null) {
            return false;
        }
        if (per != null ? !per.equals(that.per) : that.per != null) {
            return false;
        }
        return storeReferenceId != null ? storeReferenceId.equals(that.storeReferenceId) :
                that.storeReferenceId == null;
    }

    @Override
    public int hashCode() {
        int result = storeId != null ? storeId.hashCode() : 0;
        result = 31 * result + (within != null ? within.hashCode() : 0);
        result = 31 * result + (per != null ? per.hashCode() : 0);
        result = 31 * result + (storeReferenceId != null ? storeReferenceId.hashCode() : 0);
        return result;
    }
}

package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Represents an `order by` clause in the query pipeline that sorts frames based on keys and directions.
 */
public class OrderByClause implements PipelineStage {
    private final BFunctionPointer orderKeyFunction;
    private final Environment env;

    /**
     * Constructor for the OrderByClause.
     *
     * @param env              The runtime environment.
     * @param orderKeyFunction The function to extract order keys and directions.
     */
    public OrderByClause(Environment env, BFunctionPointer orderKeyFunction) {
        this.orderKeyFunction = orderKeyFunction;
        this.env = env;
    }

    public static OrderByClause initOrderByClause(Environment env, BFunctionPointer orderKeyFunction) {
        return new OrderByClause(env, orderKeyFunction);
    }

    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        return inputStream.peek(frame -> {
            BMap<BString, Object> record = frame.getRecord();
            Object result = orderKeyFunction.call(env.getRuntime(), record);
            if (result instanceof BError error) {
                throw new QueryException(error);
            }
        }).sorted(getComparator());
    }

    /**
     * Returns a comparator based on the `$orderKey$` and `$orderDirection$`.
     */
    private Comparator<Frame> getComparator() {
        return (frame1, frame2) -> {
            BMap<BString, Object> record1 = frame1.getRecord();
            BMap<BString, Object> record2 = frame2.getRecord();

            if (record1 instanceof BError) {
                throw (BError) record1;
            }

            if (record2 instanceof BError) {
                throw (BError) record2;
            }

            BArray orderKey1Array = (BArray) record1.get(StringUtils.fromString("$orderKey$"));
            BArray orderKey2Array = (BArray) record2.get(StringUtils.fromString("$orderKey$"));
            BArray orderDirectionArray = (BArray) record1.get(StringUtils.fromString("$orderDirection$"));

            int size = orderKey1Array.size();
            for (int i = 0; i < size; i++) {
                Object key1 = orderKey1Array.getRefValue(i);
                Object key2 = orderKey2Array.getRefValue(i);

                boolean ascending = orderDirectionArray.getBoolean(i);
                int comparison = compareValues(key1, key2, ascending);

                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        };
    }

    private int compareValues(Object key1, Object key2, boolean ascending) {
        if (key1 == null || key2 == null) {
            return handleNullComparison(key1, key2);
        }

        if ((key1 instanceof Double && ((Double) key1).isNaN()) &&
                (key2 instanceof Double && ((Double) key2).isNaN())) {
            return handleNaNComparison((Double) key1, (Double) key2, ascending);
        }

        if ((key1 instanceof Double && ((Double) key1).isNaN()) ||
                (key2 instanceof Double && ((Double) key2).isNaN())) {
            return handleMixedNaNNullComparison(key1, key2, ascending);
        }

        if (key1 instanceof BArray && key2 instanceof BArray) {
            return compareLists((BArray) key1, (BArray) key2, ascending);
        }

        Comparable<?> comparableKey1 = toComparable(key1);
        Comparable<?> comparableKey2 = toComparable(key2);

        if (comparableKey1 == null || comparableKey2 == null) {
            throw new IllegalStateException("Keys must be comparable: " + key1 + ", " + key2);
        }

        @SuppressWarnings("unchecked")
        int comparison = ((Comparable<Object>) comparableKey1).compareTo(comparableKey2);

        return ascending ? comparison : -comparison;
    }

    /**
     * Handles comparison when either or both keys are `null`.
     */
    private int handleNullComparison(Object key1, Object key2) {
        if (key1 == null && key2 == null) {
            return 0;
        }
        if (key1 == null) {
            return 1;
        }
        return -1;
    }

    /**
     * Ensures NaN is always greater than null.
     */
    private int handleMixedNaNNullComparison(Object key1, Object key2, boolean ascending) {
        boolean isKey1NaN = key1 instanceof Double && ((Double) key1).isNaN();
        boolean isKey2NaN = key2 instanceof Double && ((Double) key2).isNaN();

        if (key1 == null && isKey2NaN) {
            return -1; // Null < NaN
        }
        if (isKey1NaN && key2 == null) {
            return 1; // NaN > Null
        }

        assert key1 instanceof Double;
        assert key2 instanceof Double;

        return handleNaNComparison((Double) key1, (Double) key2, ascending);
    }

    /**
     * Handles NaN values: they should always be treated as less than any number.
     */
    private int handleNaNComparison(Double key1, Double key2, boolean ascending) {
        boolean isKey1NaN = key1.isNaN();
        boolean isKey2NaN = key2.isNaN();

        if (isKey1NaN && isKey2NaN) {
            return 0; // Both are NaN, consider equal
        }
        if (isKey1NaN) {
            return 1; // NaN should be less than any number
        }
        if (isKey2NaN) {
            return -1;
        }

        return ascending ? Double.compare(key1, key2) : Double.compare(key2, key1);
    }

    /**
     * Converts supported types to Comparable objects.
     */
    private Comparable<?> toComparable(Object key) {
        if (key == null) {
            return null;
        }
        if (key instanceof Integer || key instanceof Long || key instanceof Double) {
            return (Comparable<?>) key;
        }
        if (key instanceof BDecimal bDecimal) {
            return bDecimal.decimalValue();
        }
        if (key instanceof BString bString) {
            return bString.getValue();
        }
        if (key instanceof String stringValue) {
            return stringValue;
        }
        if (key instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (key instanceof Float floatValue) {
            return handleFloatComparison(floatValue);
        }
        return (Comparable<?>) key;
    }

    /**
     * Handles float comparison, ensuring NaN is handled properly.
     */
    private Comparable<?> handleFloatComparison(Float key) {
        if (Float.isNaN(key)) {
            return null;
        }
        return key;
    }

    /**
     * Compare two lists element-wise.
     */
    private int compareLists(BArray list1, BArray list2, boolean ascending) {
        int size1 = list1.size();
        int size2 = list2.size();

        for (int i = 0; i < Math.min(size1, size2); i++) {
            Object item1 = list1.getRefValue(i);
            Object item2 = list2.getRefValue(i);

            int comparison = compareValues(item1, item2, ascending);
            if (comparison != 0) {
                return comparison;
            }
        }

        return Integer.compare(size1, size2) * (ascending ? 1 : -1);
    }
}

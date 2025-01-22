package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an `order by` clause in the query pipeline that sorts frames based on keys and directions.
 */
public class OrderByClause implements PipelineStage {
    private final BFunctionPointer orderKeyFunction;
    private final Environment env;
    private List<Frame> orderedFrames = null;

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
        if (orderedFrames == null) {
            List<Frame> frames = new ArrayList<>();

            // Collect all frames and apply the order key function
            inputStream.forEach(frame -> {
                try {
                    BMap<BString, Object> record = frame.getRecord();
                    orderKeyFunction.call(env.getRuntime(), record);
                    frames.add(frame);
                } catch (Exception e) {
                    throw new RuntimeException("Error in order by clause processing.", e);
                }
            });

            // Sort the frames using the comparator
            orderedFrames = frames.stream()
                    .sorted(getComparator())
                    .collect(Collectors.toList());
        }

        return orderedFrames.stream();
    }

    /**
     * Returns a comparator based on the `$orderKey$` and `$orderDirection$`.
     */
    private Comparator<Frame> getComparator() {
        return (frame1, frame2) -> {
            BMap<BString, Object> record1 = frame1.getRecord();
            BMap<BString, Object> record2 = frame2.getRecord();

            BArray orderKey1Array = (BArray) record1.get(StringUtils.fromString("$orderKey$"));
            BArray orderKey2Array = (BArray) record2.get(StringUtils.fromString("$orderKey$"));

            BArray orderDirectionArray = (BArray) record1.get(StringUtils.fromString("$orderDirection$"));

            if (orderKey1Array == null || orderKey2Array == null || orderDirectionArray == null) {
                throw new IllegalStateException("$orderKey$ and $orderDirection$ must not be null.");
            }

            int size = orderKey1Array.size();

            for (int i = 0; i < size; i++) {
                Object key1 = orderKey1Array.getRefValue(i);
                Object key2 = orderKey2Array.getRefValue(i);

                boolean ascending = orderDirectionArray.getBoolean(i);

                Comparable<Object> comparableKey1 = toComparable(key1);
                Comparable<Object> comparableKey2 = toComparable(key2);

                if (comparableKey1 == null || comparableKey2 == null) {
                    throw new IllegalStateException("Keys must be comparable: " + key1 + ", " + key2);
                }

                int comparison = ascending
                        ? comparableKey1.compareTo(comparableKey2)
                        : comparableKey2.compareTo(comparableKey1);

                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        };
    }

    private Comparable<Object> toComparable(Object key) {
        if (key instanceof Integer || key instanceof Long || key instanceof Double || key instanceof String) {
            return (Comparable<Object>) key;
        }

        if (key instanceof BDecimal) {
            return (Comparable<Object>) (Object)((BDecimal) key).intValue();
        }

        if (key instanceof BString) {
            return (Comparable<Object>) (Object)key.toString();
        }

        return null;
    }
}

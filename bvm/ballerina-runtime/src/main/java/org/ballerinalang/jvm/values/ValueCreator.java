package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.util.exceptions.BallerinaException;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@code ValueCreator} is an API that will be implemented by all the module init classed from jvm codegen.
 * This provides facility for creating runtime record, object, error values.
 *
 * @since 0.995.0
 */
public abstract class ValueCreator {

    private static final Map<String, ValueCreator> runtimeValueCreators = new HashMap<>();

    public static void addValueCreator(String key, ValueCreator valueCreater) {
        if (runtimeValueCreators.containsKey(key)) {
            throw new BallerinaException("Value creator object already available");
        }

        runtimeValueCreators.put(key, valueCreater);
    }

    public static ValueCreator getValueCreator(String key) {
        if (!runtimeValueCreators.containsKey(key)) {
            throw new BallerinaException("Value creator object is not available");
        }

        return runtimeValueCreators.get(key);
    }

    public abstract MapValue<String, Object> createRecordValue(String recordTypeName);

    public abstract ObjectValue createObjectValue(String objectTypeName);
}

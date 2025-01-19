package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

public class CollectionUtil {
    public static BArray createArray(Stream<Frame> strm, BArray arr) {
        Object[] tmpArr = strm
                .map(frame -> frame.getRecord())
                .toArray();

        arr = ValueCreator.createArrayValue(tmpArr, TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY));
        return arr;
    }
}

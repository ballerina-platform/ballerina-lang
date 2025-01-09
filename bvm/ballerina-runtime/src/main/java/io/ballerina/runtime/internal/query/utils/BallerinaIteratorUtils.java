package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.values.BCollection;
import io.ballerina.runtime.api.values.BIterator;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BallerinaIteratorUtils {

    public static <T> Stream<T> toStream(BCollection collection) {
        Iterator<T> javaIterator = getIterator(collection);

        return StreamSupport.stream(((Iterable<T>) () -> javaIterator).spliterator(), false);
    }

    public static <T> Iterator<T> getIterator(Object collection) {
        if (collection instanceof BCollection) {
            BIterator<?> iterator = ((BCollection) collection).getIterator();

            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public T next() {
                    return (T) iterator.next();
                }
            };
        } else {
            throw new UnsupportedOperationException("Unsupported collection type: " + collection.getClass().getName());
        }
    }
}

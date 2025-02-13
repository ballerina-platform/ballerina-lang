package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.*;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BallerinaIteratorUtils {
    private static final BString VALUE_FIELD = StringUtils.fromString("value");

    /**
     * Converts a Ballerina collection to a Java stream.
     *
     * @param collection The Ballerina collection.
     * @param <T>        The type of elements in the collection.
     * @return A Java Stream of elements.
     */
    public static <T> Stream<Frame> toStream(Object collection) {
        Iterator<T> javaIterator = getIterator(collection);
        return StreamSupport.stream(((Iterable<T>) () -> javaIterator).spliterator(), false)
                .map(element -> Frame.create(VALUE_FIELD, element));
    }

    /**
     * Returns a Java iterator for a given Ballerina collection.
     *
     * @param collection The Ballerina collection.
     * @param <T>        The type of elements in the collection.
     * @return A Java Iterator for the collection.
     */
    public static <T> Iterator<T> getIterator(Object collection) {
        switch (collection) {
            case BCollection bCollection -> {
                BIterator<?> iterator = bCollection.getIterator();

                return new Iterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public T next() {
                        return (T) iterator.next();
                    }
                };
            }
            case BString bString -> {
                BIterator<?> iterator = bString.getIterator();

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
            }
            case BStream bStream -> {
                BObject iteratorObj = bStream.getIteratorObj();
                BIterator<?> iterator = (BIterator<?>) iteratorObj;

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
            }
            case null, default ->
                    throw new UnsupportedOperationException("Unsupported collection type: " + collection.getClass().getName());
        }
    }
}

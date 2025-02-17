package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.Environment;
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
    public static <T> Stream<Frame> toStream(Environment env, Object collection) {
        Iterator<T> javaIterator = getIterator(env, collection);
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
    public static <T> Iterator<T> getIterator(Environment env,Object collection) {
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

                return new Iterator<T>() {
                    private Object nextValue = null;
                    private boolean hasMore = true; // Tracks whether more elements exist

                    @Override
                    public boolean hasNext() {
                        if (!hasMore) {
                            return false;
                        }

                        try {
                            Object result = env.getRuntime().callMethod(iteratorObj, "next", null);
                            if (result instanceof BError) {
                                hasMore = false;
                                return false;
                            }
                            if (result instanceof BMap<?, ?> record) {
                                nextValue = record.get(StringUtils.fromString("value"));
                                if (nextValue == null) {
                                    hasMore = false;
                                    return false;
                                }
                                return true;
                            }
                            hasMore = false;
                            return false;
                        } catch (Exception e) {
                            hasMore = false;
                            return false;
                        }
                    }

                    @Override
                    public T next() {
                        if (nextValue == null && !hasNext()) {
                            throw new RuntimeException("No more elements in BStream");
                        }
                        T returnValue = (T) nextValue;
                        nextValue = null; // Reset for the next iteration
                        return returnValue;
                    }
                };
            }
            case null, default ->
                    throw new UnsupportedOperationException("Unsupported collection type: " + collection.getClass().getName());
        }
    }
}

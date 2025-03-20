package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.*;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BallerinaIteratorUtils {
    private static final BString VALUE_FIELD = StringUtils.fromString("value");

    /**
     * Converts a Ballerina collection to a Java stream.
     *
     * @param env        The Ballerina runtime environment.
     * @param javaIterator The Java iterator.
     * @param <T>        The type of elements in the collection.
     * @return A Java Stream of elements.
     */
    public static <T> Stream<Frame> toStream(Environment env, Iterator<T> javaIterator) throws ErrorValue {
        return StreamSupport.stream(((Iterable<T>) () -> javaIterator).spliterator(), false)
                .map(element -> Frame.create(VALUE_FIELD, element));
    }

    /**
     * Returns a Java iterator for a given Ballerina collection.
     *
     * @param env        The Ballerina runtime environment.
     * @param collection The Ballerina collection.
     * @param <T>        The type of elements in the collection.
     * @return A Java Iterator for the collection.
     */
    public static <T> Iterator<T> getIterator(Environment env, Object collection) {
        try {
            switch (collection) {
                case BMap<?, ?> bMap -> {
                    BIterator<?> iterator = bMap.getIterator();
                    return createJavaMapIterator(iterator);
                }
                case BTable table -> {
                    BIterator<?> iterator = table.getIterator();
                    return createJavaTableIterator(iterator);
                }
                case BCollection bCollection -> {
                    BIterator<?> iterator = bCollection.getIterator();
                    return createJavaIterator(iterator);
                }
                case BString bString -> {
                    BIterator<?> iterator = bString.getIterator();
                    return createJavaIterator(iterator);
                }
                case BStream bStream -> {
                    BObject iteratorObj = bStream.getIteratorObj();
                    return new BStreamIterator<>(env, iteratorObj);
                }
                case BObject bObject -> {
                    Object iteratorObj = env.getRuntime().callMethod(bObject, "iterator", null);
                    if (iteratorObj instanceof BObject iteratorInstance) {
                        return new BStreamIterator<>(env, iteratorInstance);
                    }
                    throw prepareCompleteEarlyError(new UnsupportedOperationException(
                            "Unsupported iterable object: " + bObject.getType()));
                }
                case null, default -> {
                    throw prepareCompleteEarlyError(new UnsupportedOperationException(
                            "Unsupported collection type: " + collection.getClass().getName()));
                }
            }
        } catch (BError e) {
            throw DistinctQueryErrorCreator.createDistinctError(e);
        }
    }

    private static <T> Iterator<T> createJavaMapIterator(BIterator<?> iterator) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                BArray keyValueTuple = (BArray) iterator.next();
                return (T) keyValueTuple.get(1);
            }
        };
    }

    /**
     * Creates a Java `Iterator` from a Ballerina `BIterator` for table.
     *
     * @param iterator The Ballerina iterator.
     * @param <T>      The type of elements.
     * @return A Java `Iterator<T>`.
     */
    private static <T> Iterator<T> createJavaTableIterator(BIterator<?> iterator) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                BArray keyValueTuple = (BArray) iterator.next();
                return (T) keyValueTuple.get(1);
            }
        };
    }

    /**
     * Creates a Java `Iterator` from a Ballerina `BIterator`.
     *
     * @param ballerinaIterator The Ballerina iterator.
     * @param <T>               The type of elements.
     * @return A Java `Iterator<T>`.
     */
    private static <T> Iterator<T> createJavaIterator(BIterator<?> ballerinaIterator) throws ErrorValue {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return ballerinaIterator.hasNext();
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                return (T) ballerinaIterator.next();
            }
        };
    }

    /**
     * A custom iterator for handling Ballerina `stream` and `iterator()` objects.
     */
    private static class BStreamIterator<T> implements Iterator<T> {
        private final Environment env;
        private final BObject iteratorObj;
        private Object nextValue = null;
        private boolean hasMore = true;

        public BStreamIterator(Environment env, BObject iteratorObj) {
            this.env = env;
            this.iteratorObj = iteratorObj;
        }

        @Override
        public boolean hasNext() {
            if (!hasMore) {
                return false;
            }

            Object result = env.getRuntime().callMethod(iteratorObj, "next", null);
            if (result instanceof BError) {
                hasMore = false;
                throw new QueryErrorValue(((BError) result).getErrorMessage());
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
        }

        @Override
        public T next() {
            if (nextValue == null && !hasNext()) {
                throw new RuntimeException("No more elements in BStream");
            }
            T returnValue = (T) nextValue;
            nextValue = null;
            return returnValue;
        }
    }

    /**
     * Converts an exception into a Ballerina `CompleteEarlyError`.
     *
     * @param err The exception.
     * @return A `BError` representing `CompleteEarlyError`.
     */
    public static BError prepareCompleteEarlyError(Exception err) {
        return ErrorCreator.createError(StringUtils.fromString("CompleteEarlyError"), err);
    }
}

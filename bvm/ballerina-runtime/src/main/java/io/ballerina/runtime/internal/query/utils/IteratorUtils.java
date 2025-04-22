/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.IterableValue;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.ballerina.runtime.internal.query.utils.QueryConstants.VALUE_FIELD;

/**
 * contains util methods for get java iterators for ballerina iterators.
 *
 * @since 2201.13.0
 */
public class IteratorUtils {

    /**
     * Converts a Ballerina collection to a Java stream.
     *
     * @param javaIterator The Java iterator.
     * @param <T>          The type of elements in the collection.
     * @return A Java Stream of elements.
     */
    public static <T> Stream<Frame> toStream(Iterator<T> javaIterator) throws ErrorValue {
        return StreamSupport.stream(((Iterable<T>) () -> javaIterator).spliterator(), false)
                .map(element -> Frame.create(VALUE_FIELD, element));
    }

    /**
     * Returns a Java iterator for a given Ballerina collection.
     *
     * @param env        The Ballerina runtime environment.
     * @param collection The Ballerina collection.
     * @return A Java Iterator for the collection.
     */
    public static Iterator<?> getIterator(Environment env, Object collection) {
        try {
            switch (collection) {
                case IterableValue iterable -> {
                    return iterable.getJavaIterator();
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
                    throw new QueryException("Unsupported collection type");
                }
                default -> throw new QueryException("Unsupported collection type");
            }
        } catch (BError e) {
            throw new QueryException(e);
        }
    }

    /**
     * A custom iterator for handling Ballerina `stream` and `iterator()` objects.
     * @param <T> Type of the iterator
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
            if (result instanceof BError error) {
                hasMore = false;
                throw new QueryException(error, true);
            }
            if (result instanceof BMap<?, ?> record) {
                nextValue = record.get(VALUE_FIELD);
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
            T returnValue = (T) nextValue;
            nextValue = null;
            return returnValue;
        }
    }
}

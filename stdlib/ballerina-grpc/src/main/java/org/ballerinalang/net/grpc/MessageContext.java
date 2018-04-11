/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc;

import io.grpc.Context;
import io.grpc.Metadata;

import java.util.Set;

/**
 * This class will preserve an instance the current connection context as thread local variable.
 *
 * @since 1.0.0
 */
public class MessageContext {
    public static final String MESSAGE_CONTEXT_KEY = "MessageContext";
    public static final Context.Key<MessageContext> DATA_KEY = Context.key("MessageContext");
    private Metadata contextMetadata;

    /**
     * Attaches an empty message context to the provided gRPC {@code Context}.
     *
     * @throws IllegalStateException  if an ambient context has already been attached to the
     * provided gRPC {@code Context}.
     */
    public static Context initialize(Context context) {
        if (context == null) {
            throw new RuntimeException("Context instance provided is null.");
        }
        if (DATA_KEY.get(context) != null) {
            throw new IllegalStateException("MessageContext has already been created in the scope of the current " +
                    "context");
        }
        return context.withValue(DATA_KEY, new MessageContext());
    }

    /**
     * Returns the message context attached to the current gRPC {@code Context}.
     *
     * @throws  IllegalStateException  if no ambient context is attached to the current gRPC {@code Context}.
     */
    public static MessageContext current() {
        if (DATA_KEY.get() == null) {
            throw new IllegalStateException("MessageContext has not yet been created in the scope of the current " +
                    "context");
        }
        return DATA_KEY.get();
    }

    /**
     * @return true if an {@code MessageContext} is attached to the current gRPC context.
     */
    public static boolean isPresent() {
        return DATA_KEY.get() != null;
    }

    public MessageContext() {
        this.contextMetadata = new Metadata();
    }

    /**
     * Copy constructor.
     */
    public MessageContext(MessageContext other) {
        this();
        this.contextMetadata.merge(other.contextMetadata);
    }

    /**
     * Similar to {@link #initialize(Context)}, {@code fork()} attaches a shallow clone of this {@code MessageContext}
     * to a provided gRPC {@code Context}. Use {@code fork()} when you want create a temporary context scope.
     */
    public Context fork(Context context) {
        return context.withValue(DATA_KEY, new MessageContext(this));
    }

    /**
     * Returns true if a value is defined for the given key.
     *
     * <p>This is done by linear search, so if it is followed by {@link #get} or {@link #getAll},
     * prefer calling them directly and checking the return value against {@code null}.
     */
    public boolean containsKey(Metadata.Key<?> key) {
        return contextMetadata.containsKey(key);
    }

    /**
     * Remove all values for the given key without returning them. This is a minor performance
     * optimization if you do not need the previous values.
     */
    public <T> void discardAll(Metadata.Key<T> key) {
        contextMetadata.discardAll(key);
    }

    /**
     * Returns the last message context entry added with the name 'name' parsed as T.
     *
     * @return the parsed metadata entry or null if there are none.
     */
    public <T> T get(Metadata.Key<T> key) {
        return contextMetadata.get(key);
    }

    /**
     * Returns all the ambient context entries named 'name', in the order they were received, parsed as T, or
     * null if there are none. The iterator is not guaranteed to be "live." It may or may not be
     * accurate if the ambient context is mutated.
     */
    public <T> Iterable<T> getAll(final Metadata.Key<T> key) {
        return contextMetadata.getAll(key);
    }

    /**
     * Returns set of all keys in store.
     *
     * @return unmodifiable Set of keys
     */
    public Set<String> keys() {
        return contextMetadata.keys();
    }

    /**
     * Adds the {@code key, value} pair. If {@code key} already has values, {@code value} is added to
     * the end. Duplicate values for the same key are permitted.
     *
     */
    public <T> void put(Metadata.Key<T> key, T value) {
        contextMetadata.put(key, value);
    }

    /**
     * Removes the first occurrence of {@code value} for {@code key}.
     *
     * @param key key for value
     * @param value value
     * @return {@code true} if {@code value} removed; {@code false} if {@code value} was not present
     *
     */
    public <T> boolean remove(Metadata.Key<T> key, T value) {
        return contextMetadata.remove(key, value);
    }

    /**
     * Remove all values for the given key. If there were no values, {@code null} is returned.
     */
    public <T> Iterable<T> removeAll(Metadata.Key<T> key) {
        return contextMetadata.removeAll(key);
    }
}

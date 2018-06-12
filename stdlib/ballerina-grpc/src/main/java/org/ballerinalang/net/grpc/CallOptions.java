/*
 * Copyright 2015, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.grpc;

import java.util.Arrays;
import javax.annotation.concurrent.Immutable;

/**
 * The collection of runtime options for a new RPC call.
 * <p>
 * <p>A field that is not set is {@code null}.
 */
@Immutable
public final class CallOptions {

    /**
     * A blank {@code CallOptions} that all fields are not set.
     */
    public static final CallOptions DEFAULT = new CallOptions();

    private Object[][] customOptions = new Object[0][2];

    /**
     * Key for a key-value pair. Uses reference equality.
     *
     * @param <T> types
     */
    public static final class Key<T> {

        private final String name;
        private final T defaultValue;

        private Key(String name, T defaultValue) {

            this.name = name;
            this.defaultValue = defaultValue;
        }

        /**
         * Returns the user supplied default value for this key.
         */
        public T getDefault() {

            return defaultValue;
        }

        @Override
        public String toString() {

            return name;
        }

        /**
         * Factory method for creating instances of {@link Key}.
         *
         * @param name         the name of Key.
         * @param defaultValue default value to return when value for key not set
         * @param <T>          Key type
         * @return Key object
         */
        public static <T> Key<T> of(String name, T defaultValue) {

            return new Key<T>(name, defaultValue);
        }
    }

    /**
     * Sets a custom option. Any existing value for the key is overwritten.
     *
     * @param key   The option key
     * @param value The option value.
     */
    public <T> CallOptions withOption(Key<T> key, T value) {

        CallOptions newOptions = new CallOptions(this);
        int existingIdx = -1;
        for (int i = 0; i < customOptions.length; i++) {
            if (key.equals(customOptions[i][0])) {
                existingIdx = i;
                break;
            }
        }

        newOptions.customOptions = new Object[customOptions.length + (existingIdx == -1 ? 1 : 0)][2];
        System.arraycopy(customOptions, 0, newOptions.customOptions, 0, customOptions.length);

        if (existingIdx == -1) {
            // Add a new option
            newOptions.customOptions[customOptions.length] = new Object[]{key, value};
        } else {
            // Replace an existing option
            newOptions.customOptions[existingIdx][1] = value;
        }

        return newOptions;
    }

    /**
     * Get the value for a custom option or its inherent default.
     *
     * @param key Key identifying option
     */
    @SuppressWarnings("unchecked")
    public <T> T getOption(Key<T> key) {

        for (int i = 0; i < customOptions.length; i++) {
            if (key.equals(customOptions[i][0])) {
                return (T) customOptions[i][1];
            }
        }
        return key.defaultValue;
    }

    private CallOptions() {

    }

    /**
     * Copy constructor.
     */
    private CallOptions(CallOptions other) {

        customOptions = other.customOptions;
    }

    @Override
    public String toString() {

        return "customOptions" + Arrays.deepToString(customOptions);
    }
}

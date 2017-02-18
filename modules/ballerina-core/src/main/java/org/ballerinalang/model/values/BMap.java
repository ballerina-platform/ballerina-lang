/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.values;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * {@code MapType} represents a map.
 * @param <BString> Key
 * @param <V> Value
 * @since 0.8.0
 */
public class BMap<BString, V extends BValue> implements BRefType {

    private int size;
    private static final int INITIAL_CAPACITY = 16;
    private static final int MAX_CAPACITY = 1 << 16;
    @SuppressWarnings("unchecked")
    private MapEntry<BString, V>[] values = new MapEntry[INITIAL_CAPACITY];

    /**
     * Retrieve the value for the given key from map.
     * @param key key used to get the value
     * @return value
     */
    public V get(BString key) {
        for (int i = 0; i < size; i++) {
            if (values[i] != null) {
                if (values[i].getKey().equals(key)) {
                    return values[i].getValue();
                }
            }
        }
        return null;
    }

    /**
     * Insert a key value pair into the map.
     * @param key key related to the value
     * @param value value related to the key
     */
    public void put(BString key, V value) {
        boolean insert = true;
        for (int i = 0; i < size; i++) {
            if (values[i].getKey().equals(key)) {
                values[i].setValue(value);
                insert = false;
            }
        }
        if (insert) {
            ensureCapacity();
            values[size++] = new MapEntry<>(key, value);
        }
    }

    private void ensureCapacity() {
        if (size == values.length) {
            int newSize = values.length * 2;
            if (newSize <= MAX_CAPACITY) {
                values = Arrays.copyOf(values, newSize);
            } else {
                throw new RuntimeException(" Map cannot exceed the maximum size");
            }
        }
    }

    /**
     * Get the size of the map.
     * @return returns the size of the map
     */
    public int size() {
        return size;
    }

    /**
     * Remove an item from the map.
     * @param key key of the item to be removed
     */
    public void remove(BString key) {
        for (int i = 0; i < size; i++) {
            if (values[i].getKey().equals(key)) {
                values[i] = null;
                size--;
                condenseArray(i);
            }
        }
    }

    private void condenseArray(int start) {
        System.arraycopy(values, start + 1, values, start, (values.length - start - 1));
    }

    /**
     * Retrieve the set of keys related to this map.
     * @return returns the set of keys
     */
    public Set<BString> keySet() {
        Set<BString> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            set.add(values[i].getKey());
        }
        return set;
    }

    /** Return true if this map is empty. */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        return null;
    }

    private class MapEntry<K, V> {
        private final K key;
        private V value;

        MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        K getKey() {
            return key;
        }

        V getValue() {
            return value;
        }

        void setValue(V value) {
            this.value = value;
        }
    }

}


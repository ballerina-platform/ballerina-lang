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

import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@code MapType} represents a map.
 * @param <K> Key
 * @param <V> Value
 * @since 0.8.0
 */
public class BMap<K, V extends BValue> extends BallerinaMessageDataSource implements BRefType, BCollection {


    @SuppressWarnings("unchecked")
    private LinkedHashMap<K, V> map;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private BType type = BTypes.typeMap;

    public BMap() {
        map =  new LinkedHashMap<>();
    }

    public BMap(BMapType type) {
        this.map = new LinkedHashMap<>();
        this.type = type;
    }

    /**
     * Retrieve the value for the given key from map.
     * A {@code BallerinaException} will be thrown if the key does not exists.
     * 
     * @param key key used to get the value
     * @return value
     */
    public V get(K key) {
        readLock.lock();
        try {
            if (!map.containsKey(key)) {
                throw new BallerinaException("cannot find key '" + key + "'");
            }
            return map.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Retrieve the value for the given key from map.
     * 
     * @param key key used to get the value
     * @param except flag indicating whether to throw an exception if the key does not exists
     * @return value
     */
    public V get(K key, boolean except) {
        readLock.lock();
        try {
            if (!map.containsKey(key) && except) {
                throw new BallerinaException("cannot find key '" + key + "'");
            }
            return map.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Insert a key value pair into the map.
     * @param key key related to the value
     * @param value value related to the key
     */
    public void put(K key, V value) {
        writeLock.lock();
        try {
            map.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Clear map entries.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Check existence of a key of a map.
     *
     * @param key key of the map item
     * @return returns boolean true if key exists
     */
    public boolean hasKey(K key) {
        return map.containsKey(key);
    }

    /**
     * Retrieve the internal map.
     * @return map
     */
    public LinkedHashMap<K, V> getMap() {
        return map;
    }

    /**
     * Get the size of the map.
     * @return returns the size of the map
     */
    public int size() {
        return map.size();
    }

    /**
     * Remove an item from the map.
     *
     * @param key key of the item to be removed
     * @return boolean to indicate whether given key is removed.
     */
    public boolean remove(K key) {
        boolean hasKey = map.containsKey(key);
        if (hasKey) {
            map.remove(key);
        }
        return hasKey;
    }

    /**
     * Retrieve the set of keys related to this map.
     * @return returns the set of keys
     */
    public Set<K> keySet() {
        return map.keySet();
    }

    /**Return true if this map is empty.
     *
     * @return Flag indicating whether the map is empty or not
     */
    public boolean isEmpty() {
        return map.size() == 0;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public String stringValue() {
        StringJoiner sj = new StringJoiner(", ", "{", "}");

        for (Iterator<Map.Entry<K, V>> i = map.entrySet().iterator(); i.hasNext();) {

            String key;
            String stringValue;

            Map.Entry<K, V> e = i.next();
            key = "\"" + (String) e.getKey() + "\"";
            V value = e.getValue();

            if (value == null) {
                stringValue = null;
            } else if (value instanceof BString) {
                stringValue = "\"" + value.stringValue() + "\"";
            } else {
                stringValue = value.stringValue();
            }

            sj.add(key + ":" + stringValue);
        }
        return sj.toString();

    }

    @Override
    public BType getType() {
        return this.type;
    }

    @Override
    public BValue copy() {
        BMap<K, BValue> newMap = BTypes.typeMap.getEmptyValue();
        for (Map.Entry<K, V> entry: map.entrySet()) {
            BValue value = entry.getValue();
            newMap.put(entry.getKey(), value == null ? null : value.copy());
        }
        return newMap;
    }

    @Override
    public String getMessageAsString() {
        return stringValue();
    }

    @Override
    public void serializeData(OutputStream outputStream) {
        try {
            outputStream.write(stringValue().getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new BallerinaException("Error occurred while serializing data", e);
        }
    }

    @Override
    public BIterator newIterator() {
        return new BMapIterator<>(this);
    }

    /**
     * {@code {@link BMapIterator}} provides iterator implementation for map values.
     *
     * @since 0.96.0
     */
    static class BMapIterator<K, V extends BValue> implements BIterator {

        BMap<K, V> collection;
        Iterator<Map.Entry<K, V>> iterator;

        BMapIterator(BMap<K, V> value) {
            collection = value;
            iterator = new LinkedHashMap<>(value.map).entrySet().iterator();
        }

        @Override
        public BValue[] getNext(int arity) {
            Map.Entry<K, V> next = iterator.next();
            if (arity == 1) {
                return new BValue[] {next.getValue()};
            }
            return new BValue[] {new BString((String) next.getKey()), next.getValue()};
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
    }
}


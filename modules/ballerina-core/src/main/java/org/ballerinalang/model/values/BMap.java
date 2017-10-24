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
public class BMap<K, V extends BValue> extends BallerinaMessageDataSource implements BRefType {


    @SuppressWarnings("unchecked")
    private LinkedHashMap<K, V> map;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public BMap() {
        map =  new LinkedHashMap<>();
    }

    /**
     * Output stream to write message out to the socket.
     */
    private OutputStream outputStream;

    /**
     * Retrieve the value for the given key from map.
     * @param key key used to get the value
     * @return value
     */
    public V get(K key) {
        readLock.lock();
        try {
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
     * Get the size of the map.
     * @return returns the size of the map
     */
    public int size() {
        return map.size();
    }

    /**
     * Remove an item from the map.
     * @param key key of the item to be removed
     */
    public void remove(K key) {
        map.remove(key);
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
        return BTypes.typeMap;
    }

    @Override
    public BValue copy() {
        BMap newMap = BTypes.typeMap.getEmptyValue();
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
    public void serializeData() {
        try {
            outputStream.write(stringValue().getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new BallerinaException("Error occurred while serializing data", e);
        }
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

}


/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.freeze.State;
import org.ballerinalang.jvm.freeze.Status;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.Flags;
import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.ballerinalang.jvm.freeze.Utils.handleInvalidUpdate;

/**
 * Structure that represents the mapping between key value pairs in ballerina.
 * A map cannot contain duplicate keys; each key can map to at most one value.
 * 
 * @since 0.995.0
 */
public class MapValue<K, V> extends LinkedHashMap<K, V> implements RefValue {

    private static final long serialVersionUID = 1L;
    private BType type;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private volatile Status freezeStatus = new Status(State.UNFROZEN);

    public MapValue(BType type) {
        super();
        this.type = type;
    }

    public MapValue() {
        super();
    }

    /**
     * Retrieve the value for the given key from map.
     * A null will be returned if the key does not exists.
     *
     * @param key key used to get the value
     * @return value associated with the key
     */
    @Override
    public V get(Object key) {
        readLock.lock();
        try {
            return super.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Retrieve the value for the given key from map.
     *
     * @param key key used to get the value
     * @param except flag indicating whether to throw an exception if the key does not exists
     * @return value associated with the key
     */
    public Object get(String key, boolean except) {
        readLock.lock();
        try {
            if (except && !containsKey(key)) {
                throw new BallerinaException(BallerinaErrorReasons.KEY_NOT_FOUND_ERROR,
                        "cannot find key '" + key + "'");
            }
            return super.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Insert a key value pair into the map.
     * 
     * @param key key related to the value
     * @param value value related to the key
     * @return
     */
    @Override
    public V put(K key, V value) {
        writeLock.lock();
        try {
            if (freezeStatus.getState() != State.UNFROZEN) {
                handleInvalidUpdate(freezeStatus.getState());
            }

            return super.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Clear map entries.
     */
    public void clear() {
        writeLock.lock();
        try {
            if (freezeStatus.getState() != State.UNFROZEN) {
                handleInvalidUpdate(freezeStatus.getState());
            }
            super.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Check existence of a key of a map.
     *
     * @param key key of the map item
     * @return returns boolean true if key exists
     */
    @Override
    public boolean containsKey(Object key) {
        readLock.lock();
        try {
            return super.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Remove an item from the map.
     *
     * @param key key of the item to be removed
     * @return boolean to indicate whether given key is removed.
     */
    @Override
    public V remove(Object key) {
        writeLock.lock();
        try {
            if (freezeStatus.getState() != State.UNFROZEN) {
                handleInvalidUpdate(freezeStatus.getState());
            }
            return super.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Retrieve the keys related to this map as an array.
     *
     * @return keys as an array
     */
    @SuppressWarnings("unchecked")
    public K[] getKeys() {
        readLock.lock();
        try {

            Set<K> keys = super.keySet();
            return (K[]) keys.toArray(new String[keys.size()]);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Retrieve the value in the map as an array.
     *
     * @return values as an array
     */
    public Collection<V> values() {
        readLock.lock();
        try {
            return super.values();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return number of key-value mappings in this map
     */
    @Override
    public int size() {
        readLock.lock();
        try {
            return super.size();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Return true if this map is empty.
     *
     * @return Flag indicating whether the map is empty or not
     */
    public boolean isEmpty() {
        readLock.lock();
        try {
            return super.size() == 0;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        readLock.lock();
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        try {
            switch (type.getTag()) {
                case TypeTags.OBJECT_TYPE_TAG:
                    for (Map.Entry<String, BField> field : ((BStructureType) this.type).getFields().entrySet()) {
                        if (!Flags.isFlagOn(field.getValue().flags, Flags.PUBLIC)) {
                            continue;
                        }
                        String fieldName = field.getKey();
                        Object fieldVal = get(fieldName);
                        sj.add(fieldName + ":" + getStringValue(fieldVal));
                    }
                    break;
                case TypeTags.JSON_TAG:
                    return getJSONString();
                default:
                    String keySeparator = type.getTag() == TypeTags.MAP_TAG ? "\"" : "";
                    for (Iterator<Map.Entry<K, V>> i = super.entrySet().iterator(); i.hasNext();) {
                        String key;
                        Map.Entry<K, V> e = i.next();
                        key = keySeparator + e.getKey() + keySeparator;
                        Object value = e.getValue();
                        sj.add(key + ":" + getStringValue(value));
                    }
                    break;
            }
            return sj.toString();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void stamp(BType type) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public Object copy(Map<RefValue, RefValue> refs) {
        readLock.lock();
        try {
            if (isFrozen()) {
                return this;
            }

            if (refs.containsKey(this)) {
                return refs.get(this);
            }

            MapValue<K, V> newMap = new MapValue<>(type);
            refs.put(this, newMap);
            for (Map.Entry<K, V> entry : super.entrySet()) {
                V value = entry.getValue();
                value = value instanceof RefValue ? (V) ((RefValue) value).copy(refs) : value;
                newMap.put(entry.getKey(), value);
            }
            return newMap;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public BType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isFrozen() {
        return freezeStatus.isFrozen();
    }

    // Private methods

    private String getStringValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof BString) {
            return "\"" + value.toString() + "\"";
        } else {
            return value.toString();
        }
    }

    private String getJSONString() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonGenerator(byteOut);
        try {
            // TODO
            // gen.serialize(this);
            gen.flush();
        } catch (IOException e) {
            throw new BallerinaException("Error in converting JSON to a string: " + e.getMessage(), e);
        }
        return new String(byteOut.toByteArray());
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.util.exceptions.BallerinaException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * An interface for table Value. Represents tables in Ballerina.
 * </p>
 * 
 * @param <K> the type of keys maintained by this table
 * @param <V> the type of mapped values
 *
 * @since 2.0.0
 */
public interface BTable<K, V> extends BRefValue, BCollection {

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no
     * mapping for the key.
     *
     * <p>
     * If this map permits null values, then a return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also possible that the map explicitly
     * maps the key to {@code null}. The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this map contains no
     *         mapping for the key
     */
    V get(Object key);

    /**
     * Associates the specified value with the specified key in this map (optional operation). If the map
     * previously contained a mapping for the key, the old value is replaced by the specified value. (A map
     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only if {@link #containsKey(Object)
     * m.containsKey(k)} would return <tt>true</tt>.)
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for
     *         <tt>key</tt>. (A <tt>null</tt> return can also indicate that the map previously associated
     *         <tt>null</tt> with <tt>key</tt>, if the implementation supports <tt>null</tt> values.)
     */
    V put(K key, V value);

    /**
     * Removes the mapping for a key from this map if it is present (optional operation). Returns the value
     * to which this map previously associated the key, or <tt>null</tt> if the map contained no mapping for
     * the key.
     *
     * <p>
     * If this map permits null values, then a return value of <tt>null</tt> does not <i>necessarily</i>
     * indicate that the map contained no mapping for the key; it's also possible that the map explicitly
     * mapped the key to <tt>null</tt>.
     *
     * <p>
     * The map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for
     *         <tt>key</tt>.
     */
    V remove(Object key);

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified key
     */
    boolean containsKey(Object key);

    /**
     * Returns a {@link Set} set of mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map
     */
    Set<Map.Entry<K, V>> entrySet();

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     *
     * @return a collection view of the values contained in this map
     */
    Collection<V> values();

    /**
     * Removes all of the mappings from this map (optional operation). The map will be empty after this call
     * returns.
     */
    void clear();

    /**
     * Returns the value to which the specified key is mapped. A {@link BallerinaException} will be thrown
     * if the key does not exists.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped
     * @throws BallerinaException if the key does not exists
     */
    V getOrThrow(Object key);

    /**
     * Returns the value for the given key from map. If the key does not exist, but there exists a filler
     * value for the expected type, a new value will be created and added and then returned. A {@link
     * BallerinaException} will be thrown if the key does not exists and a filler value does not exist.
     *
     * @param key key used to get the value
     * @return value associated with the key
     */
    V fillAndGet(Object key);

    /**
     * Returns the keys related to this map as an array.
     *
     * @return keys as an array
     */
    K[] getKeys();

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    int size();

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    boolean isEmpty();

    /**
     * Add native data to the MapValue.
     *
     * @param key  key to identify native value.
     * @param data value to be added.
     */
    void addNativeData(String key, Object data);

    /**
     * Get native data.
     *
     * @param key key to identify native value.
     * @return value for the given key.
     */
    Object getNativeData(String key);

    Long getIntValue(BString key);

    Double getFloatValue(BString key);

    BString getStringValue(BString key);

    Boolean getBooleanValue(BString key);

    BMap<?, ?> getMapValue(BString key);

    BObject getObjectValue(BString key);

    BArray getArrayValue(BString key);

    Type getIteratorNextReturnType();

    Object removeOrThrow(Object key);

    Type getKeyType();

    void add(V data);

    long getNextKey();

    V put(V data);
}

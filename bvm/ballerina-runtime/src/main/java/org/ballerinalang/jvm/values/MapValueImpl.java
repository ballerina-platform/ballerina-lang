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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.IteratorUtils;
import org.ballerinalang.jvm.JSONGenerator;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.MapUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.ballerinalang.jvm.JSONUtils.mergeJson;
import static org.ballerinalang.jvm.util.BLangConstants.MAP_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.MAP_KEY_NOT_FOUND_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.ballerinalang.jvm.util.exceptions.RuntimeErrors.INVALID_READONLY_VALUE_UPDATE;
import static org.ballerinalang.jvm.values.ReadOnlyUtils.handleInvalidUpdate;

/**
 * <p>
 * Structure that represents the mapping between key value pairs in ballerina.
 * A map cannot contain duplicate keys; each key can map to at most one value.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * @see MapValue
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @since 0.995.0
 */
public class MapValueImpl<K, V> extends LinkedHashMap<K, V> implements RefValue, CollectionValue, MapValue<K, V>,
        BMap<K, V> {

    private static final long serialVersionUID = 1L;
    private TypedescValue typedesc;
    private BType type;
    private final Map<String, Object> nativeData = new HashMap<>();
    private BType iteratorNextReturnType;

    public MapValueImpl(TypedescValue typedesc) {
        this(typedesc.getDescribingType());
        this.typedesc = typedesc;
    }

    public MapValueImpl(BType type) {
        super();
        this.type = type;
    }

    public MapValueImpl(BType type, MappingInitialValueEntry[] initialValues) {
        super();
        this.type = type;
        populateInitialValues(initialValues);
    }

    public MapValueImpl() {
        super();
        type = BTypes.typeMap;
    }

    public Long getIntValue(BString key) {
        return (Long) get(key);
    }

    public Double getFloatValue(BString key) {
        return (Double) get(key);
    }

    public BString getStringValue(BString key) {
        return (BString) get(key);
    }

    public Boolean getBooleanValue(BString key) {
        return (Boolean) get(key);
    }

    public MapValueImpl<?, ?> getMapValue(BString key) {
        return (MapValueImpl<?, ?>) get(key);
    }

    public ObjectValue getObjectValue(BString key) {
        return (ObjectValue) get(key);
    }

    public ArrayValue getArrayValue(BString key) {
        return (ArrayValue) get(key);
    }

    public long getDefaultableIntValue(BString key) {
        if (get(key) != null) {
            return getIntValue(key);
        }
        return 0;
    }

    /**
     * Retrieve the value for the given key from map.
     * A {@link BallerinaException} will be thrown if the key does not exists.
     *
     * @param key key used to get the value
     * @return value associated with the key
     */
    public V getOrThrow(Object key) {
        if (!containsKey(key)) {
            throw BallerinaErrors.createError(MAP_KEY_NOT_FOUND_ERROR, "cannot find key '" + key + "'");
        }
        return this.get(key);
    }

    /**
     * Retrieve the value for the given key from map. If the key does not exist, but there exists a filler value for
     * the expected type, a new value will be created and added and then returned.
     * A {@link BallerinaException} will be thrown if the key does not exists and a filler value does not exist.
     *
     * @param key key used to get the value
     * @return value associated with the key
     */
    public V fillAndGet(Object key) {
        if (containsKey(key)) {
            return this.get(key);
        }

        BType expectedType = null;

        // The type should be a record or map for filling read.
        if (this.type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            BRecordType recordType = (BRecordType) this.type;
            Map fields = recordType.getFields();
            if (fields.containsKey(key.toString())) {
                expectedType = ((BField) fields.get(key.toString())).type;
            } else {
                if (recordType.sealed) {
                    // Panic if this record type does not contain a key by the specified name.
                    throw BallerinaErrors.createError(MAP_KEY_NOT_FOUND_ERROR, "cannot find key '" + key + "'");
                }
                expectedType = recordType.restFieldType;
            }
        } else {
            expectedType = ((BMapType) this.type).getConstrainedType();
        }

        if (!TypeChecker.hasFillerValue(expectedType)) {
            // Panic if the field does not have a filler value.
            throw BallerinaErrors.createError(MAP_KEY_NOT_FOUND_ERROR, "cannot find key '" + key + "'");
        }

        Object value = expectedType.getZeroValue();
        this.put((K) key, (V) value);
        return (V) value;
    }

    @Override
    public Object merge(MapValue v2, boolean checkMergeability) {
        return merge((MapValueImpl) v2, checkMergeability);
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    @Override
    public V put(K key, V value) {
        if (!type.isReadOnly()) {
            return putValue(key, value);
        }

        String errMessage = "";
        switch (getType().getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                errMessage = "Invalid update of record field: ";
                break;
            case TypeTags.MAP_TAG:
                errMessage = "Invalid map insertion: ";
                break;
        }
        throw BallerinaErrors.createError(getModulePrefixedReason(MAP_LANG_LIB, INVALID_UPDATE_ERROR_IDENTIFIER),
                                          errMessage + BLangExceptionHelper.getErrorMessage(
                                                  INVALID_READONLY_VALUE_UPDATE));
    }

    protected void populateInitialValues(MappingInitialValueEntry[] initialValues) {
        for (MappingInitialValueEntry initialValue : initialValues) {
            if (initialValue.isKeyValueEntry()) {
                MappingInitialValueEntry.KeyValueEntry keyValueEntry =
                        (MappingInitialValueEntry.KeyValueEntry) initialValue;
                populateInitialValue((K) keyValueEntry.key, (V) keyValueEntry.value);
                continue;
            }

            MapValueImpl<K, V> values =
                    (MapValueImpl<K, V>) ((MappingInitialValueEntry.SpreadFieldEntry) initialValue).values;
            for (Map.Entry<K, V> entry : values.entrySet()) {
                populateInitialValue(entry.getKey(), entry.getValue());
            }
        }
    }

    public void populateInitialValue(K key, V value) {
        if (type.getTag() == TypeTags.MAP_TAG) {
            MapUtils.handleInherentTypeViolatingMapUpdate(value, (BMapType) type);
        } else {
            BString fieldName = (BString) key;
            MapUtils.handleInherentTypeViolatingRecordUpdate(this, fieldName, value, (BRecordType) type, true);
        }

        putValue(key, value);
    }

    /**
     * Clear map entries.
     */
    public void clear() {
        validateFreezeStatus();
        super.clear();
    }

    protected void validateFreezeStatus() {
        if (!type.isReadOnly()) {
            return;
        }
        handleInvalidUpdate(MAP_LANG_LIB);
    }

    /**
     * Check existence of a key of a map.
     *
     * @param key key of the map item
     * @return returns boolean true if key exists
     */
    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
           return false;
        }

        MapValueImpl<?, ?> mapValue = (MapValueImpl<?, ?>) o;

        if (mapValue.type.getTag() != this.type.getTag()) {
            return false;
        }

        if (this.entrySet().size() != mapValue.entrySet().size()) {
            return false;
        }

        return entrySet().equals(mapValue.entrySet());
    }

    /**
     * Returns the hash code value.
     *
     * @return returns hashcode value
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Remove an item from the map.
     *
     * @param key key of the item to be removed
     * @return boolean to indicate whether given key is removed.
     */
    @Override
    public V remove(Object key) {
        validateFreezeStatus();
        return super.remove(key);
    }

    /**
     * Retrieve the keys related to this map as an array.
     *
     * @return keys as an array
     */
    @SuppressWarnings("unchecked")
    public K[] getKeys() {
        Set<K> keys = super.keySet();
        return (K[]) (keys.toArray(new BString[keys.size()]));
    }

    /**
     * Retrieve the value in the map as an array.
     *
     * @return values as an array
     */
    public Collection<V> values() {
        return super.values();
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return number of key-value mappings in this map
     */
    @Override
    public int size() {
        return super.size();
    }

    /**
     * Return true if this map is empty.
     *
     * @return Flag indicating whether the map is empty or not
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public String toString() {
        return stringValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        MapValueImpl<K, V> newMap = new MapValueImpl<>(type);
        refs.put(this, newMap);
        for (Map.Entry<K, V> entry : this.entrySet()) {
            V value = entry.getValue();
            value = value instanceof RefValue ? (V) ((RefValue) value).copy(refs) : value;
            newMap.put(entry.getKey(), value);
        }
        return newMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        MapValueImpl<K, V> copy = (MapValueImpl<K, V>) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }

    @Override
    public String stringValue() {
        StringJoiner sj = new StringJoiner(" ");
        for (Map.Entry<K, V> kvEntry : this.entrySet()) {
            K key = kvEntry.getKey();
            V value = kvEntry.getValue();
            sj.add(key + "=" + StringUtils.getStringValue(value));
        }
        return sj.toString();
    }

    @Override
    public BType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        if (isFrozen()) {
            return;
        }

        this.type = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.type);

        this.values().forEach(val -> {
            if (val instanceof RefValue) {
                ((RefValue) val).freezeDirect();
            }
        });
    }

    public String getJSONString() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        JSONGenerator gen = new JSONGenerator(byteOut);
        try {
            gen.serialize(this);
            gen.flush();
        } catch (IOException e) {
            throw new BallerinaException("Error in converting JSON to a string: " + e.getMessage(), e);
        }
        return new String(byteOut.toByteArray());
    }

    @Override
    public IteratorValue getIterator() {
        return new MapIterator<>(new LinkedHashSet<>(this.entrySet()).iterator());
    }

    /**
     * {@link MapIterator} iteration provider for ballerina maps.
     *
     * @since 0.995.0
     */
    static class MapIterator<K, V> implements IteratorValue {

        Iterator<Map.Entry<K, V>> iterator;

        MapIterator(Iterator<Map.Entry<K, V>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Object next() {
            Map.Entry<?, ?> next = iterator.next();
            Object value = next.getValue();

            List<BType> types = new LinkedList<>();
            types.add(BTypes.typeString);
            types.add(TypeChecker.getType(value));
            BTupleType tupleType = new BTupleType(types);

            TupleValueImpl tuple = new TupleValueImpl(tupleType);
            tuple.add(0, next.getKey());
            tuple.add(1, value);
            return tuple;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
    }

    /**
     * Add native data to the MapValue.
     *
     * @param key key to identify native value.
     * @param data value to be added.
     */
    public void addNativeData(String key, Object data) {
        nativeData.put(key, data);
    }

    /**
     * Get native data.
     * @param key key to identify native value.
     * @return value for the given key.
     */
    public Object getNativeData(String key) {
        return nativeData.get(key);
    }

    @Override
    public TypedescValue getTypedesc() {
        return typedesc;
    }

    /**
     * Method to retrieve whole native data map.
     *
     * @return nativeData map
     */
    public Map<String, Object> getNativeDataMap() {
        return this.nativeData;
    }

    private void initializeIteratorNextReturnType() {
        BType type;
        if (this.type.getTag() == BTypes.typeMap.getTag()) {
            BMapType mapType = (BMapType) this.type;
            type = mapType.getConstrainedType();
        } else {
            BRecordType recordType = (BRecordType) this.type;
            LinkedHashSet<BType> types = recordType.getFields().values().stream().map(bField -> bField.type)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (recordType.restFieldType != null) {
                types.add(recordType.restFieldType);
            }
            if (types.size() == 1) {
                type = types.iterator().next();
            } else {
                type = new BUnionType(new ArrayList<>(types));
            }
        }
        iteratorNextReturnType = IteratorUtils.createIteratorNextReturnType(type);
    }

    public BType getIteratorNextReturnType() {
        if (iteratorNextReturnType == null) {
            initializeIteratorNextReturnType();
        }

        return iteratorNextReturnType;
    }
    /*
     * Below are a set of convenient methods that handle map related operations.
     * This makes it easier to extend the operations without affecting the
     * common behaviors such as error handling.
     */
    protected V putValue(K key, V value) {
        return super.put(key, value);
    }

    private Object merge(MapValueImpl v2, boolean checkMergeability) {
        if (checkMergeability) {
            ErrorValue errorIfUnmergeable = JSONUtils.getErrorIfUnmergeable(this, v2, new ArrayList<>());
            if (errorIfUnmergeable != null) {
                return errorIfUnmergeable;
            }
        }

        MapValue<BString, Object> m1 = (MapValue<BString, Object>) this;
        MapValue<BString, Object> m2 = (MapValue<BString, Object>) v2;

        for (Map.Entry<BString, Object> entry : m2.entrySet()) {
            BString key = entry.getKey();

            if (!m1.containsKey(key)) {
                m1.put(key, entry.getValue());
                continue;
            }

            // Set checkMergeability to false to avoid rechecking mergeability.
            // Since write locks are acquired, the initial check should suffice, and merging will always succeed.
            m1.put(key, mergeJson(m1.get(key), entry.getValue(), false));
        }

        return this;
    }
}

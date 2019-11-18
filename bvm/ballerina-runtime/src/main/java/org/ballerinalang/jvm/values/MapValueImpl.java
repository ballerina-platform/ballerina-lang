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
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.JSONGenerator;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BLangFreezeException;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;
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

import static org.ballerinalang.jvm.JSONUtils.mergeJson;
import static org.ballerinalang.jvm.TypeConverter.getConvertibleTypes;
import static org.ballerinalang.jvm.util.BLangConstants.MAP_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.MAP_KEY_NOT_FOUND_ERROR;
import static org.ballerinalang.jvm.values.freeze.FreezeUtils.handleInvalidUpdate;

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
    private BType type;

    private static final String PERIOD = ".";
    private static final String UNDERSCORE = "_";
    private static final String SLASH = "/";

    private volatile Status freezeStatus = new Status(State.UNFROZEN);
    private final Map<String, Object> nativeData = new HashMap<>();

    public MapValueImpl(BType type) {
        super();
        this.type = type;
    }

    public MapValueImpl() {
        super();
        type = BTypes.typeMap;
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
        return super.get(key);
    }

    public Long getIntValue(String key) {
        return (Long) get(key);
    }

    public Double getFloatValue(String key) {
        return (Double) get(key);
    }

    public String getStringValue(String key) {
        return (String) get(key);
    }

    public Boolean getBooleanValue(String key) {
        return (Boolean) get(key);
    }

    public MapValueImpl<?, ?> getMapValue(String key) {
        return (MapValueImpl<?, ?>) get(key);
    }

    public ObjectValue getObjectValue(String key) {
        return (ObjectValue) get(key);
    }

    public ArrayValue getArrayValue(String key) {
        return (ArrayValue) get(key);
    }

    public long getDefaultableIntValue(String key) {
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
            if (fields.containsKey(key)) {
                expectedType = ((BField) fields.get(key)).type;
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
        checkFreezeStatus();
        return putValue(key, value);
    }
    
    /**
     * Check the freeze status of the current map value for updates. If its frozen,
     * then a {@link ErrorValue} will be thrown.
     */
    protected void checkFreezeStatus() {
        try {
            if (freezeStatus.getState() == State.UNFROZEN) {
                return;
            }
            handleInvalidUpdate(freezeStatus.getState(), MAP_LANG_LIB);
        } catch (BLangFreezeException e) {
            // we would only reach here for record or map, not for object
            String errMessage = "";
            switch (getType().getTag()) {
                case TypeTags.RECORD_TYPE_TAG:
                    errMessage = "Invalid update of record field: ";
                    break;
                case TypeTags.MAP_TAG:
                    errMessage = "Invalid map insertion: ";
                    break;
            }
            throw BallerinaErrors.createError(e.getMessage(), errMessage + e.getDetail());
        }
    }

    /**
     * Clear map entries.
     */
    public void clear() {
        if (freezeStatus.getState() != State.UNFROZEN) {
            handleInvalidUpdate(freezeStatus.getState(), MAP_LANG_LIB);
        }
        super.clear();
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

    /**
     * Returns the hash code value for map value object.
     *
     * @return returns hashcode value.
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
        if (freezeStatus.getState() != State.UNFROZEN) {
            handleInvalidUpdate(freezeStatus.getState(), MAP_LANG_LIB);
        }
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
        return (K[]) keys.toArray(new String[keys.size()]);
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
        return stringValue(null);
    }

    @Override
    public String stringValue(Strand strand) {
        StringJoiner sj = new StringJoiner(" ");
        for (Map.Entry<K, V> kvEntry : this.entrySet()) {
            K key = kvEntry.getKey();
            V value = kvEntry.getValue();
            sj.add(key + "=" + StringUtils.getStringValue(strand, value));
        }
        return sj.toString();
    }

    @Override
    public BType getType() {
        return type;
    }

    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {
        TypeValuePair typeValuePair = new TypeValuePair(this, type);
        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR,
                                         BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE,
                                                                              this.type));
        }
        unresolvedValues.add(typeValuePair);
        if (type.getTag() == TypeTags.JSON_TAG) {
            type = TypeConverter.resolveMatchingTypeForUnion(this, type);
            this.stamp(type, unresolvedValues);
        } else if (type.getTag() == TypeTags.MAP_TAG) {
            for (Map.Entry valueEntry : this.entrySet()) {
                Object value = valueEntry.getValue();
                BType constraintType = ((BMapType) type).getConstrainedType();
                if (value instanceof RefValue) {
                    ((RefValue) value).stamp(constraintType, unresolvedValues);
                } else if (!TypeChecker.checkIsType(value, constraintType)) {
                    // Has to be a numeric conversion.
                    this.put((K) valueEntry.getKey(),
                             (V) TypeConverter.convertValues(getConvertibleTypes(value, constraintType).get(0), value));
                }
            }
        } else if (type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            BRecordType recordType = (BRecordType) type;
            MapValueImpl<String, Object> recordWithDefaults = (MapValueImpl<String, Object>)
                    BallerinaValues.createRecordValue(recordType.getPackage(), recordType.getName());

            for (Map.Entry valueEntry : recordWithDefaults.entrySet()) {
                Object fieldName = valueEntry.getKey();
                if (!this.containsKey(fieldName)) {
                    this.put((K) fieldName, (V) valueEntry.getValue());
                }
            }

            BType restFieldType = recordType.restFieldType;
            Map<String, BType> targetTypeField = new HashMap<>();
            for (BField field : recordType.getFields().values()) {
                targetTypeField.put(field.getFieldName(), field.getFieldType());
            }

            for (Map.Entry valueEntry : this.entrySet()) {
                String fieldName = valueEntry.getKey().toString();
                Object value = valueEntry.getValue();
                BType bType = targetTypeField.getOrDefault(fieldName, restFieldType);
                if (value instanceof RefValue) {
                    ((RefValue) value).stamp(bType, unresolvedValues);
                } else if (!TypeChecker.checkIsType(value, bType)) {
                    // Has to be a numeric conversion.
                    this.put((K) fieldName,
                             (V) TypeConverter.convertValues(getConvertibleTypes(value, bType).get(0), value));
                }
            }
        } else if (type.getTag() == TypeTags.UNION_TAG) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (TypeChecker.checkIsLikeType(this, memberType)) {
                    this.stamp(memberType, unresolvedValues);
                    if (memberType.getTag() == TypeTags.ANYDATA_TAG) {
                        type = TypeConverter.resolveMatchingTypeForUnion(this, memberType);
                    } else {
                        type = memberType;
                    }
                    break;
                }
            }
        } else if (type.getTag() == TypeTags.ANYDATA_TAG) {
            type = TypeConverter.resolveMatchingTypeForUnion(this, type);
            this.stamp(type, unresolvedValues);
        }

        this.type = type;
        unresolvedValues.remove(typeValuePair);
    }

    private String getPackageForValueCreator(BPackage bPackage) {
        if (PERIOD.equals(bPackage.toString())) {
            return PERIOD;
        }

        String org = bPackage.org;
        String name = bPackage.name;
        return org.replace(PERIOD, UNDERSCORE).concat(SLASH).concat(name.replace(PERIOD, UNDERSCORE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void attemptFreeze(Status freezeStatus) {
        if (this.type.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            throw new BLangFreezeException("'freeze()' not allowed on '" + getType() + "'");
        }

        if (FreezeUtils.isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
            this.values().forEach(val -> {
                if (val instanceof RefValue) {
                    ((RefValue) val).attemptFreeze(freezeStatus);
                }
            });
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        if (isFrozen()) {
            return;
        }
        this.freezeStatus.setFrozen();
        this.values().forEach(val -> {
            if (val instanceof RefValue) {
                ((RefValue) val).freezeDirect();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isFrozen() {
        return freezeStatus.isFrozen();
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

            ArrayValue tuple = new ArrayValue(tupleType);
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

    /**
     * Method to retrieve whole native data map.
     *
     * @return nativeData map
     */
    public Map<String, Object> getNativeDataMap() {
        return this.nativeData;
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

        MapValue<String, Object> m1 = (MapValue<String, Object>) this;
        MapValue<String, Object> m2 = (MapValue<String, Object>) v2;

        for (Map.Entry<String, Object> entry : m2.entrySet()) {
            String key = entry.getKey();

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

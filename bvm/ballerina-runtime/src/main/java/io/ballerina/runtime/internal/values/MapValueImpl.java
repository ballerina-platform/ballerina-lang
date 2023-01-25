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
package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.CycleUtils;
import io.ballerina.runtime.internal.IteratorUtils;
import io.ballerina.runtime.internal.JsonGenerator;
import io.ballerina.runtime.internal.JsonInternalUtils;
import io.ballerina.runtime.internal.MapUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BField;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

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

import static io.ballerina.runtime.api.constants.RuntimeConstants.MAP_LANG_LIB;
import static io.ballerina.runtime.api.utils.TypeUtils.getReferredType;
import static io.ballerina.runtime.internal.JsonInternalUtils.mergeJson;
import static io.ballerina.runtime.internal.ValueUtils.createSingletonTypedesc;
import static io.ballerina.runtime.internal.ValueUtils.getTypedescValue;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.MAP_KEY_NOT_FOUND_ERROR;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.INVALID_READONLY_VALUE_UPDATE;
import static io.ballerina.runtime.internal.values.ReadOnlyUtils.handleInvalidUpdate;

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
    private BTypedesc typedesc;
    private Type type;
    private Type referredType;
    private final Map<String, Object> nativeData = new HashMap<>();
    private Type iteratorNextReturnType;

    public MapValueImpl(TypedescValue typedesc) {
        this(typedesc.getDescribingType());
        if (!type.isReadOnly()) {
            this.typedesc = typedesc;
        }
    }

    public MapValueImpl(Type type) {
        super();
        this.type = type;
        this.referredType = getReferredType(type);
        this.typedesc = getTypedescValue(type, this);
    }

    public MapValueImpl(Type type, BMapInitialValueEntry[] initialValues) {
        super();
        this.type = type;
        this.referredType = getReferredType(type);
        populateInitialValues(initialValues);
        if (!type.isReadOnly()) {
            this.typedesc = new TypedescValueImpl(type);
        }
    }

    public MapValueImpl() {
        super();
        type = PredefinedTypes.TYPE_MAP;
        this.referredType = this.type;
        this.typedesc = getTypedescValue(type, this);
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

    public BMap<?, ?> getMapValue(BString key) {
        return (BMap<?, ?>) get(key);
    }

    public BObject getObjectValue(BString key) {
        return (BObject) get(key);
    }

    public BArray getArrayValue(BString key) {
        return (BArray) get(key);
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
            throw ErrorCreator.createError(MAP_KEY_NOT_FOUND_ERROR,
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.KEY_NOT_FOUND_ERROR, key));
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

        Type expectedType = null;

        // The type should be a record or map for filling read.
        if (this.referredType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            BRecordType recordType = (BRecordType) this.referredType;
            Map fields = recordType.getFields();
            if (fields.containsKey(key.toString())) {
                expectedType = ((BField) fields.get(key.toString())).getFieldType();
            } else {
                if (recordType.sealed) {
                    // Panic if this record type does not contain a key by the specified name.
                    throw ErrorCreator.createError(MAP_KEY_NOT_FOUND_ERROR,
                            BLangExceptionHelper.getErrorDetails(RuntimeErrors.KEY_NOT_FOUND_ERROR, key));
                }
                expectedType = recordType.restFieldType;
            }
        } else {
            expectedType = ((BMapType) this.referredType).getConstrainedType();
        }

        if (!TypeChecker.hasFillerValue(expectedType)) {
            // Panic if the field does not have a filler value.
            throw ErrorCreator.createError(MAP_KEY_NOT_FOUND_ERROR,
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.KEY_NOT_FOUND_ERROR, key));
        }

        Object value = expectedType.getZeroValue();
        this.put((K) key, (V) value);
        return (V) value;
    }

    @Override
    public Object merge(BMap v2, boolean checkMergeability) {
        if (checkMergeability) {
            BError errorIfUnmergeable = JsonInternalUtils.getErrorIfUnmergeable(this, v2, new ArrayList<>());
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
        throw ErrorCreator.createError(getModulePrefixedReason(MAP_LANG_LIB, INVALID_UPDATE_ERROR_IDENTIFIER),
                                       StringUtils
                                                .fromString(errMessage).concat(BLangExceptionHelper.getErrorMessage(
                                                  INVALID_READONLY_VALUE_UPDATE)));
    }

    protected void populateInitialValues(BMapInitialValueEntry[] initialValues) {
        for (BMapInitialValueEntry initialValue : initialValues) {
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
        if (referredType.getTag() == TypeTags.MAP_TAG) {
            MapUtils.handleInherentTypeViolatingMapUpdate(value, (BMapType) referredType);
            putValue(key, value);
        } else {
            BString fieldName = (BString) key;
            if (MapUtils.handleInherentTypeViolatingRecordUpdate(this, fieldName, value, (BRecordType) referredType,
                    true)) {
                putValue(key, value);
            }
        }

        if (this.type.isReadOnly()) {
            this.typedesc = createSingletonTypedesc(this);
        }
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

        if (mapValue.referredType.getTag() != this.referredType.getTag()) {
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
        BString[] keyArr = new BString[keys.size()];
        int i = 0;
        for (K key : keys) {
            keyArr[i] = (BString) key;
            i++;
        }
        return (K[]) keyArr;
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
        return stringValue(null);
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
    public String stringValue(BLink parent) {
        StringJoiner sj = new StringJoiner(",");
        for (Map.Entry<K, V> kvEntry : this.entrySet()) {
            K key = kvEntry.getKey();
            V value = kvEntry.getValue();
            if (value == null) {
                sj.add("\"" + key + "\":null");
            } else {
                Type type = TypeChecker.getType(value);
                CycleUtils.Node mapParent = new CycleUtils.Node(this, parent);
                switch (type.getTag()) {
                    case TypeTags.STRING_TAG:
                    case TypeTags.XML_TAG:
                    case TypeTags.XML_ELEMENT_TAG:
                    case TypeTags.XML_ATTRIBUTES_TAG:
                    case TypeTags.XML_COMMENT_TAG:
                    case TypeTags.XML_PI_TAG:
                    case TypeTags.XMLNS_TAG:
                    case TypeTags.XML_TEXT_TAG:
                        sj.add("\"" + key + "\":" + ((BValue) value).informalStringValue(mapParent));
                        break;
                    default:
                        sj.add("\"" + key + "\":" + StringUtils.getStringValue(value, mapParent));
                        break;
                }
            }
        }
        return "{" + sj.toString() + "}";
    }

    @Override
    public String expressionStringValue(BLink parent) {
        CycleUtils.Node node = new CycleUtils.Node(this , parent);
        StringJoiner sj = new StringJoiner(",");
        for (Map.Entry<K, V> kvEntry : this.entrySet()) {
            K key = kvEntry.getKey();
            V value = kvEntry.getValue();
            CycleUtils.Node mapParent = new CycleUtils.Node(this, node);
            sj.add("\"" + key + "\":" + StringUtils.getExpressionStringValue(value, mapParent));
        }
        return "{" + sj.toString() + "}";
    }

    @Override
    public Type getType() {
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
        this.referredType = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.referredType);

        this.values().forEach(val -> {
            if (val instanceof RefValue) {
                ((RefValue) val).freezeDirect();
            }
        });
        this.typedesc = createSingletonTypedesc(this);
    }

    public String getJSONString() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonGenerator(byteOut);
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

            List<Type> types = new LinkedList<>();
            types.add(PredefinedTypes.TYPE_STRING);
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
    public BTypedesc getTypedesc() {
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
        Type type;
        if (this.referredType.getTag() == PredefinedTypes.TYPE_MAP.getTag()) {
            BMapType mapType = (BMapType) this.referredType;
            type = mapType.getConstrainedType();
        } else {
            BRecordType recordType = (BRecordType) this.referredType;
            LinkedHashSet<Type> types = recordType.getFields().values().stream().map(Field::getFieldType)
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

    public Type getIteratorNextReturnType() {
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
}

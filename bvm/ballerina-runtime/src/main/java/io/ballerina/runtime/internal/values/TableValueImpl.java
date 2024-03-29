/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.CycleUtils;
import io.ballerina.runtime.internal.IteratorUtils;
import io.ballerina.runtime.internal.TableUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BUnionType;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.ballerina.runtime.api.constants.RuntimeConstants.TABLE_LANG_LIB;
import static io.ballerina.runtime.api.utils.TypeUtils.getImpliedType;
import static io.ballerina.runtime.internal.TypeChecker.isEqual;
import static io.ballerina.runtime.internal.ValueUtils.getTypedescValue;
import static io.ballerina.runtime.internal.errors.ErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.errors.ErrorReasons.OPERATION_NOT_SUPPORTED_ERROR;
import static io.ballerina.runtime.internal.errors.ErrorReasons.TABLE_HAS_A_VALUE_FOR_KEY_ERROR;
import static io.ballerina.runtime.internal.errors.ErrorReasons.TABLE_KEY_NOT_FOUND_ERROR;
import static io.ballerina.runtime.internal.errors.ErrorReasons.getModulePrefixedReason;
import static io.ballerina.runtime.internal.util.StringUtils.getExpressionStringVal;
import static io.ballerina.runtime.internal.util.StringUtils.getStringVal;

/**
 * The runtime representation of table.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @since 1.3.0
 */
public class TableValueImpl<K, V> implements TableValue<K, V> {

    private Type type;
    private TableType tableType;
    private Type iteratorNextReturnType;
    private ConcurrentHashMap<Long, List<Map.Entry<K, V>>> entries;
    private LinkedHashMap<Long, List<V>> values;
    private String[] fieldNames;
    private ValueHolder valueHolder;
    private long maxIntKey = 0;

    //These are required to achieve the iterator behavior
    private Map<Long, K> indexToKeyMap;
    private Map<K, Long> keyToIndexMap;
    private Map<K, V> keyValues;
    private long noOfAddedEntries = 0;

    private boolean nextKeySupported;

    private final Map<String, Object> nativeData = new HashMap<>();
    private BTypedesc typedesc;

    public TableValueImpl(TableType tableType) {
        this.type = this.tableType = tableType;

        this.entries = new ConcurrentHashMap<>();
        this.values = new LinkedHashMap<>();
        this.keyToIndexMap = new LinkedHashMap<>();
        this.indexToKeyMap = new TreeMap<>();
        this.fieldNames = tableType.getFieldNames();
        this.keyValues = new LinkedHashMap<>();
        if (tableType.getFieldNames().length > 0) {
            this.valueHolder = new KeyHashValueHolder();
        } else {
            this.valueHolder = new ValueHolder();
        }
    }

    public TableValueImpl(Type type, ArrayValue data, ArrayValue fieldNames) {
        this((TableType) TypeUtils.getImpliedType(type), data, fieldNames);
        this.type = type;
    }

    public TableValueImpl(TableType tableType, ArrayValue data, ArrayValue fieldNames) {
        this(tableType);
        if (this.fieldNames == null) {
            this.fieldNames = fieldNames.getStringArray();
        }

        addData(data);
    }

    // TODO: Might be unnecessary after fixing issue lang/#36721
    public TableValueImpl(TableType tableType, ArrayValue fieldNames) {
        this(tableType);
        this.fieldNames = fieldNames.getStringArray();

        if (this.fieldNames.length > 0) {
            this.valueHolder = new KeyHashValueHolder();
        } else {
            this.valueHolder = new ValueHolder();
        }
    }

    private void addData(ArrayValue data) {
        BIterator itr = data.getIterator();
        while (itr.hasNext()) {
            Object next = itr.next();
            valueHolder.addData((V) next);
        }
    }

    @Override
    public IteratorValue getIterator() {
        return new TableIterator();
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        TableValueImpl<K, V> clone = new TableValueImpl<>(tableType);
        clone.type = type;

        if (fieldNames != null) {
            clone.fieldNames = fieldNames;
        }

        IteratorValue itr = getIterator();
        while (itr.hasNext()) {
            TupleValueImpl tupleValue = (TupleValueImpl) itr.next();
            Object value = tupleValue.get(1);
            value = value instanceof BRefValue ? ((BRefValue) value).copy(refs) : value;
            clone.add((V) value);
        }

       return clone;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        TableValueImpl<K, V> copy = (TableValueImpl<K, V>) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }

    protected void handleFrozenTableValue() {
        synchronized (this) {
            if (this.tableType.isReadOnly()) {
                ReadOnlyUtils.handleInvalidUpdate(TABLE_LANG_LIB);
            }
        }
    }

    @Override
    public BTypedesc getTypedesc() {
        if (this.typedesc == null) {
            this.typedesc = getTypedescValue(type, this);
        }
        return typedesc;
    }

    @Override
    public V get(Object key) {
        return valueHolder.getData((K) key);
    }

    //Generates the key from the given data
    public V put(V value) {
        handleFrozenTableValue();
        return valueHolder.putData(value);
    }

    @Override
    public V put(K key, V value) {
        handleFrozenTableValue();
        return valueHolder.putData(key, value);
    }

    @Override
    public void add(V data) {
        handleFrozenTableValue();
        valueHolder.addData(data);
    }

    @Override
    public V remove(Object key) {
        handleFrozenTableValue();
        return valueHolder.remove((K) key);
    }

    @Override
    public boolean containsKey(Object key) {
        return valueHolder.containsKey((K) key);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        LinkedHashSet<Map.Entry<K, V>> entrySet = new LinkedHashSet<>();
        for (List<Map.Entry<K, V>> entry: entries.values()) {
            entrySet.addAll(entry);
        }
        return new LinkedHashSet(entries.values());
    }

    @Override
    public Collection<V> values() {
        ArrayList<V> newValues = new ArrayList<>();
        Set<Long> keys = values.keySet();
        for (long key: keys) {
            newValues.addAll(values.get(key));
        }
        return newValues;
    }

    @Override
    public void clear() {
        handleFrozenTableValue();
        entries.clear();
        values.clear();
        keyToIndexMap.clear();
        indexToKeyMap.clear();
        noOfAddedEntries = 0;
    }

    @Override
    public V getOrThrow(Object key) {
        if (!containsKey(key)) {
            throw ErrorCreator.createError(TABLE_KEY_NOT_FOUND_ERROR,
                    ErrorHelper.getErrorDetails(ErrorCodes.KEY_NOT_FOUND_ERROR, key));
        }
        return this.get(key);
    }

    public V removeOrThrow(Object key) {
        handleFrozenTableValue();
        if (!containsKey(key)) {
            throw ErrorCreator.createError(TABLE_KEY_NOT_FOUND_ERROR,
                    ErrorHelper.getErrorDetails(ErrorCodes.KEY_NOT_FOUND_ERROR, key));
        }
        return this.remove(key);
    }

    public long getNextKey() {
        if (!nextKeySupported) {
            throw ErrorCreator.createError(OPERATION_NOT_SUPPORTED_ERROR,
                                           StringUtils
                                                    .fromString("Defined key sequence is not supported with nextKey(). "
                                                                        + "The key sequence should only have an " +
                                                                           "Integer field."));
        }
        return indexToKeyMap.size() == 0 ? 0 : (this.maxIntKey + 1);
    }

    public Type getKeyType() {
        return this.valueHolder.getKeyType();
    }

    @Override
    public V fillAndGet(Object key) {
        if (containsKey(key)) {
            return this.get(key);
        }

        Type expectedType = (this.tableType).getConstrainedType();

        if (!TypeChecker.hasFillerValue(expectedType)) {
            // Panic if the field does not have a filler value.
            throw ErrorCreator.createError(TABLE_KEY_NOT_FOUND_ERROR,
                    ErrorHelper.getErrorDetails(ErrorCodes.KEY_NOT_FOUND_ERROR, key));
        }

        Object value = expectedType.getZeroValue();
        this.put((K) key, (V) value);
        return (V) value;
    }

    @Override
    public K[] getKeys() {
        return (K[]) indexToKeyMap.values().toArray();
    }

    @Override
    public int size() {
        return values().size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public void addNativeData(String key, Object data) {
        nativeData.put(key, data);
    }

    @Override
    public Object getNativeData(String key) {
        return nativeData.get(key);
    }

    @Override
    public void freezeDirect() {
        if (isFrozen()) {
            return;
        }

        this.tableType = (BTableType) ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.tableType);
        this.type = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.type);

        //we know that values are always BRefValues
        this.values().forEach(val -> ((BRefValue) val).freezeDirect());
        this.typedesc = null;
    }

    public String stringValue(BLink parent) {
        Iterator<Map.Entry<Long, List<V>>> itr = values.entrySet().iterator();
        return createStringValueDataEntry(itr, parent);
    }

    @Override
    public String informalStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public String expressionStringValue(BLink parent) {
        Iterator<Map.Entry<Long, List<V>>> itr = values.entrySet().iterator();
        return createExpressionStringValueDataEntry(itr, parent);
    }

    private String createStringValueDataEntry(Iterator<Map.Entry<Long, List<V>>> itr, BLink parent) {
        StringJoiner sj = new StringJoiner(",");
        while (itr.hasNext()) {
            Map.Entry<Long, List<V>> struct = itr.next();
            if (struct.getValue().size() > 1) {
                for (V data: struct.getValue()) {
                    sj.add(getStringVal(data,
                            new CycleUtils.Node(this, parent)));
                }
            } else {
                sj.add(getStringVal(struct.getValue().get(0),
                        new CycleUtils.Node(this, parent)));
            }
        }
        return "[" + sj + "]";
    }

    private String createExpressionStringValueDataEntry(Iterator<Map.Entry<Long, List<V>>> itr, BLink parent) {
        StringJoiner sj = new StringJoiner(",");
        StringJoiner keyJoiner = new StringJoiner(",");
        String[] keysList = tableType.getFieldNames();
        for (int i = 0; i < keysList.length; i++) {
            keyJoiner.add(keysList[i]);
        }
        while (itr.hasNext()) {
            Map.Entry<Long, List<V>> struct = itr.next();
            if (struct.getValue().size() > 1) {
                for (V data: struct.getValue()) {
                    sj.add(getExpressionStringVal(data, new CycleUtils.Node(this, parent)));
                }
            } else {
                sj.add(getExpressionStringVal(struct.getValue().get(0), new CycleUtils.Node(this, parent)));
            }
        }
        return "table key(" + keyJoiner + ") [" + sj + "]";
    }

    private Type getTableConstraintField(Type constraintType, String fieldName) {
        switch (constraintType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                Map<String, Field> fieldList = ((BRecordType) constraintType).getFields();
                return fieldList.get(fieldName).getFieldType();
            case TypeTags.MAP_TAG:
                return ((BMapType) constraintType).getConstrainedType();
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((BIntersectionType) constraintType).getEffectiveType();
                return getTableConstraintField(effectiveType, fieldName);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                Type refType = ((BTypeReferenceType) constraintType).getReferredType();
                return getTableConstraintField(refType, fieldName);
            case TypeTags.UNION_TAG:
                HashSet<Type> possibleTypes = new HashSet<>();
                for (Type memberType : ((BUnionType) constraintType).getMemberTypes()) {
                    possibleTypes.add(getTableConstraintField(memberType, fieldName));
                }
                if (possibleTypes.size() == 1) {
                    return possibleTypes.iterator().next();
                } else if (possibleTypes.size() > 1) {
                    return new BUnionType(new ArrayList<>(possibleTypes));
                }
                break;
        }
        //cannot reach here. constraint should be a subtype of map<any|error>
        return null;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    public Type getIteratorNextReturnType() {
        if (iteratorNextReturnType == null) {
            iteratorNextReturnType = IteratorUtils.createIteratorNextReturnType(tableType.getConstrainedType());
        }

        return iteratorNextReturnType;
    }

    /**
     * Check whether the given table value is equal to the current value.
     *
     * @param o the value to check equality with
     * @param visitedValues the values that have already been visited
     * @return true if the current value is equal to the given value
     */
    @Override
    public boolean equals(Object o, Set<ValuePair> visitedValues) {
        ValuePair compValuePair = new ValuePair(this, o);
        for (ValuePair valuePair : visitedValues) {
            if (valuePair.equals(compValuePair)) {
                return true;
            }
        }
        visitedValues.add(compValuePair);

        if (!(o instanceof TableValueImpl<?, ?> table)) {
            return false;
        }
        if (this.size() != table.size()) {
            return false;
        }

        boolean isLhsKeyedTable =
                ((BTableType) getImpliedType(this.getType())).getFieldNames().length > 0;
        boolean isRhsKeyedTable =
                ((BTableType) getImpliedType(table.getType())).getFieldNames().length > 0;
        Object[] lhsTableValues = this.values().toArray();
        Object[] rhsTableValues = table.values().toArray();
        if (isLhsKeyedTable != isRhsKeyedTable) {
            return false;
        }
        for (int i = 0; i < lhsTableValues.length; i++) {
            if (!isEqual(lhsTableValues[i], rhsTableValues[i], visitedValues)) {
                return false;
            }
        }
        return true;
    }

    private class TableIterator implements IteratorValue {
        private long cursor;

        TableIterator() {
            this.cursor = 0;
        }

        @Override
        public Object next() {
            if (indexToKeyMap.containsKey(cursor)) {
                K key = indexToKeyMap.get(cursor);
                V value = keyValues.get(key);

                List<Type> types = new ArrayList<>();
                types.add(TypeChecker.getType(key));
                types.add(TypeChecker.getType(value));
                BTupleType tupleType = new BTupleType(types);

                TupleValueImpl tuple = new TupleValueImpl(tupleType);
                tuple.add(0, key);
                tuple.add(1, value);
                cursor++;
                return tuple;
            } else {
                cursor++;
                return next();
            }
        }

        @Override
        public boolean hasNext() {
           return cursor < noOfAddedEntries && values.size() != 0;
        }
    }

    private class ValueHolder {

        public void addData(V data) {
            putData(data);
        }

        public V getData(K key) {
            throw ErrorCreator.createError(TABLE_KEY_NOT_FOUND_ERROR,
                    ErrorHelper.getErrorDetails(ErrorCodes.KEY_NOT_FOUND_ERROR, key));
        }

        public V putData(K key, V data) {
            throw ErrorCreator.createError(TABLE_KEY_NOT_FOUND_ERROR,
                    ErrorHelper.getErrorDetails(ErrorCodes.KEY_NOT_FOUND_ERROR, key));
        }

        public V putData(V data) {
            checkInherentTypeViolation((MapValue) data, tableType);

            ArrayList<V> newData = new ArrayList<>();
            newData.add(data);

            Map.Entry<K, V> entry = new AbstractMap.SimpleEntry(data, data);
            List<Map.Entry<K, V>> entryList = new ArrayList<>();
            entryList.add(entry);
            UUID uuid = UUID.randomUUID();
            Long hash = (long) uuid.hashCode();
            updateIndexKeyMappings(hash, (K) data, data);
            entries.put(hash, entryList);
            values.put(hash, newData);
            return data;
        }

        public V remove(K key) {
            throw ErrorCreator.createError(TABLE_KEY_NOT_FOUND_ERROR,
                    ErrorHelper.getErrorDetails(ErrorCodes.KEY_NOT_FOUND_ERROR, key));
        }

        public boolean containsKey(K key) {
            return false;
        }

        public Type getKeyType() {
            throw ErrorCreator.createError(TABLE_KEY_NOT_FOUND_ERROR,
                                           StringUtils.fromString("keys are not defined"));
        }
    }

    private class KeyHashValueHolder extends ValueHolder {
        private DefaultKeyWrapper keyWrapper;
        private Type keyType;

        public KeyHashValueHolder() {
            super();
            if (fieldNames.length > 1) {
                keyWrapper = new MultiKeyWrapper();
            } else {
                keyWrapper = new DefaultKeyWrapper();
            }
        }

        public void addData(V data) {
            MapValue dataMap = (MapValue) data;
            checkInherentTypeViolation(dataMap, tableType);
            K key = this.keyWrapper.wrapKey(dataMap);

            if (containsKey((K) key)) {
                throw ErrorCreator.createError(TABLE_HAS_A_VALUE_FOR_KEY_ERROR,
                        ErrorHelper.getErrorDetails(ErrorCodes.TABLE_HAS_A_VALUE_FOR_KEY, key));
            }

            if (nextKeySupported && (indexToKeyMap.size() == 0 || maxIntKey < TypeChecker.anyToInt(key))) {
                maxIntKey = ((Long) TypeChecker.anyToInt(key)).intValue();
            }

            Map.Entry<K, V> entry = new AbstractMap.SimpleEntry(key, data);
            Long hash = TableUtils.hash(key, null);

            if (entries.containsKey(hash)) {
                updateIndexKeyMappings(hash, key, data);
                List<Map.Entry<K, V>> extEntries = entries.get(hash);
                extEntries.add(entry);
                List<V> extValues = values.get(hash);
                extValues.add(data);
                return;
            }

            putNewData(key, data, entry, hash);
        }

        public V getData(K key) {
            List<Map.Entry<K, V>> entryList = entries.get(TableUtils.hash(key, null));
            if (entryList == null) {
                return null;
            }
            for (Map.Entry<K, V> entry: entryList) {
                if (isEqual(key, entry.getKey())) {
                    return entry.getValue();
                }
            }
            return null;
        }

        public V putData(K key, V data) {
            Map.Entry<K, V> entry = new AbstractMap.SimpleEntry(key, data);
            Object actualKey = this.keyWrapper.wrapKey((MapValue) data);
            Long actualHash = TableUtils.hash(actualKey, null);
            Long hash = TableUtils.hash(key, null);

            if (!hash.equals(actualHash)) {
                throw ErrorCreator.createError(TABLE_KEY_NOT_FOUND_ERROR,
                        ErrorHelper.getErrorDetails(ErrorCodes.KEY_NOT_FOUND_IN_VALUE, key, data));
            }

            if (entries.containsKey(hash)) {
                return updateExistingEntry(key, data, entry, hash);
            }
            return putNewData(key, data, entry, hash);
        }

        private V putNewData(K key, V value, Map.Entry<K, V> entry, Long hash) {
            List<V> data = new ArrayList<>();
            data.add(value);
            updateIndexKeyMappings(hash, key, value);
            List<Map.Entry<K, V>> entryList = new ArrayList<>();
            entryList.add(entry);
            entries.put(hash, entryList);
            values.put(hash, data);
            return data.get(0);
        }

        public V putData(V data) {
            MapValue dataMap = (MapValue) data;
            checkInherentTypeViolation(dataMap, tableType);
            K key = this.keyWrapper.wrapKey(dataMap);
            Map.Entry<K, V> entry = new AbstractMap.SimpleEntry<>(key, data);
            Long hash = TableUtils.hash(key, null);

            if (entries.containsKey(hash)) {
                return updateExistingEntry(key, data, entry, hash);
            }
            return putNewData((K) key, data, entry, hash);
        }

        private V updateExistingEntry(K key, V data, Map.Entry<K, V> entry, Long hash) {
            updateIndexKeyMappings(hash, key, data);
            List<V> valueList = values.get(hash);
            List<Map.Entry<K, V>> entryList = entries.get(hash);
            for (Map.Entry<K, V> extEntry: entryList) {
                // Handle hash-collided entries
                if (TypeChecker.isEqual(key, extEntry.getKey())) {
                    entryList.remove(extEntry);
                    valueList.remove(extEntry.getValue());
                    break;
                }
            }
            entryList.add(entry);
            values.get(hash).add(data);
            return data;
        }

        public V remove(K key) {
            keyValues.remove(key);
            Long hash = TableUtils.hash(key, null);
            List<Map.Entry<K, V>> entryList = entries.get(hash);
            if (entryList != null && entryList.size() > 1) {
                for (Map.Entry<K, V> entry: entryList) {
                    if (isEqual(key, entry.getKey())) {
                        List<V> valueList = values.get(hash);
                        valueList.remove(entry.getValue());
                        entryList.remove(entry);
                        Long index = keyToIndexMap.remove(key);
                        indexToKeyMap.remove(index);
                        if (index != null && index == noOfAddedEntries - 1) {
                            noOfAddedEntries--;
                        }
                        return entry.getValue();
                    }
                }
            }
            if (entryList != null) {
                Long index = keyToIndexMap.remove(entryList.get(0).getKey());
                indexToKeyMap.remove(index);
                if (index != null && index == noOfAddedEntries - 1) {
                    noOfAddedEntries--;
                }
            }
            entries.remove(hash);
            List<V> removedValue = values.remove(hash);
            if (removedValue == null) {
                return null;
            }
            return removedValue.get(0);
        }

        public boolean containsKey(K key) {
            if (entries.containsKey(TableUtils.hash(key, null))) {
                List<Map.Entry<K, V>> entryList = entries.get(TableUtils.hash(key, null));
                for (Map.Entry<K, V> entry: entryList) {
                    if (isEqual(entry.getKey(), key)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Type getKeyType() {
            return keyType;
        }

        private class DefaultKeyWrapper {

            public DefaultKeyWrapper() {
                if (fieldNames.length == 1) {
                    keyType = getTableConstraintField(tableType.getConstrainedType(), fieldNames[0]);
                    if (keyType != null && TypeUtils.getImpliedType(keyType).getTag() == TypeTags.INT_TAG) {
                        nextKeySupported = true;
                    }
                }
            }

            public K wrapKey(MapValue data) {
                return (K) data.get(StringUtils.fromString(fieldNames[0]));
            }
        }

        private class MultiKeyWrapper extends DefaultKeyWrapper {

            public MultiKeyWrapper() {
                super();
                List<Type> keyTypes = new ArrayList<>();
                Type constraintType = TypeUtils.getImpliedType(tableType.getConstrainedType());
                if (constraintType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    BRecordType recordType = (BRecordType) constraintType;
                    Arrays.stream(fieldNames)
                            .forEach(field -> keyTypes.add(recordType.getFields().get(field).getFieldType()));
                } else if (constraintType.getTag() == TypeTags.MAP_TAG) {
                    BMapType mapType = (BMapType) constraintType;
                    Arrays.stream(fieldNames).forEach(field -> keyTypes.add(mapType.getConstrainedType()));
                }
                keyType = new BTupleType(keyTypes);
            }

            public K wrapKey(MapValue data) {
                TupleValueImpl arr = (TupleValueImpl) ValueCreator
                        .createTupleValue((BTupleType) keyType);
                for (int i = 0; i < fieldNames.length; i++) {
                    arr.add(i, data.get(StringUtils.fromString(fieldNames[i])));
                }
                return (K) arr;
            }
        }
    }

    // This method updates the indexes and the order required by the iterators
    private void updateIndexKeyMappings(Long hash, K key, V value) {
        if (entries.containsKey(hash)) {
            List<Map.Entry<K, V>> entryList = entries.get(hash);
            for (Map.Entry<K, V> entry: entryList) {
                if (TypeChecker.isEqual(entry.getKey(), key)) {
                    long index = keyToIndexMap.remove(entry.getKey());
                    keyToIndexMap.put(key, index);
                    indexToKeyMap.put(index, key);
                    keyValues.remove(entry.getKey());
                    keyValues.put(key, value);
                    return;
                }
            }
        }
        keyToIndexMap.put(key, noOfAddedEntries);
        indexToKeyMap.put(noOfAddedEntries, key);
        keyValues.put(key, value);
        noOfAddedEntries++;
    }

    // This method checks for inherent table type violation
    private void checkInherentTypeViolation(MapValue dataMap, TableType type) {
        if (!TypeChecker.checkIsType(dataMap.getType(), type.getConstrainedType())) {
            BString reason = getModulePrefixedReason(TABLE_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER);
            BString detail = StringUtils.fromString("value type '" + dataMap.getType() + "' inconsistent with the " +
                                                            "inherent table type '" + type + "'");
            throw ErrorCreator.createError(reason, detail);
        }
    }

    @Override
    public String toString() {
        return stringValue(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableValueImpl<?, ?> tableValue = (TableValueImpl<?, ?>) o;

        if (tableValue.tableType.getTag() != this.tableType.getTag()) {
            return false;
        }

        if (this.entrySet().size() != tableValue.entrySet().size()) {
            return false;
        }

        return entrySet().equals(tableValue.entrySet());
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public Long getIntValue(BString key) {
        return (Long) get(key);
    }

    @Override
    public Double getFloatValue(BString key) {
        return (Double) get(key);
    }

    @Override
    public BString getStringValue(BString key) {
        return (BString) get(key);
    }

    @Override
    public Boolean getBooleanValue(BString key) {
        return (Boolean) get(key);
    }

    @Override
    public BMap<?, ?> getMapValue(BString key) {
        return (BMap<?, ?>) get(key);
    }

    @Override
    public BObject getObjectValue(BString key) {
        return (BObject) get(key);
    }

    @Override
    public BArray getArrayValue(BString key) {
        return (BArray) get(key);
    }
}

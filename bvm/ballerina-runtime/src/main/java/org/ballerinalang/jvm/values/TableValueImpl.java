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

package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.IteratorUtils;
import org.ballerinalang.jvm.TableUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.api.BIterator;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.TABLE_HAS_A_VALUE_FOR_KEY_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.TABLE_KEY_NOT_FOUND_ERROR;

/**
 * The runtime representation of table.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @since 1.3.0
 */
public class TableValueImpl<K, V> implements TableValue<K, V> {

    private BTableType type;
    private volatile Status freezeStatus = new Status(State.UNFROZEN);
    private BType iteratorNextReturnType;
    private LinkedHashMap<Integer, Map.Entry<K, V>> entries;
    private LinkedHashMap<Integer, V> values;
    private LinkedHashMap<Integer, K> keys;
    private String[] fieldNames = null;
    private ValueHolder valueHolder;

    public TableValueImpl(BTableType type) {
        this.type = type;
        this.entries = new LinkedHashMap<>();
        this.keys = new LinkedHashMap<>();
        this.values = new LinkedHashMap<>();
        this.fieldNames = type.getFieldNames();
        if (type.getFieldNames() != null) {
            this.valueHolder = new KeyHashValueHolder();
        } else {
            this.valueHolder = new ValueHolder();
        }
    }

    public TableValueImpl(BTableType type, ArrayValue data, ArrayValue fieldNames) {
        this(type);
        if (this.fieldNames == null) {
            this.fieldNames = fieldNames.getStringArray();
        }

        addData(data);
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
        return new TableIterator<K, V>(entries.values().iterator());
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public V get(Object key) {
        return valueHolder.getData((K) key);
    }

    @Override
    public V put(K key, V value) {
        return valueHolder.putData(key, value);
    }

    @Override
    public void add(V data) {
        valueHolder.addData(data);
    }

    @Override
    public V remove(Object key) {
        return valueHolder.remove((K) key);
    }

    @Override
    public boolean containsKey(Object key) {
        return valueHolder.containsKey((K) key);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new LinkedHashSet<>(entries.values());
    }

    @Override
    public Collection<V> values() {
        return values.values();
    }

    @Override
    public void clear() {
        entries.clear();
        keys.clear();
        values.clear();
    }

    @Override
    public V getOrThrow(Object key) {
        if (!containsKey(key)) {
            throw BallerinaErrors.createError(TABLE_KEY_NOT_FOUND_ERROR, "cannot find key '" + key + "'");
        }
        return this.get(key);
    }

    public V removeOrThrow(Object key) {
        if (!containsKey(key)) {
            throw BallerinaErrors.createError(TABLE_KEY_NOT_FOUND_ERROR, "cannot find key '" + key + "'");
        }
        return this.remove(key);
    }

    @Override
    public V fillAndGet(Object key) {
        return null;
    }

    @Override
    public K[] getKeys() {
        return (K[]) keys.values().toArray(new Object[]{});
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean isFrozen() {
        return freezeStatus.isFrozen();
    }

    @Override
    public void attemptFreeze(Status freezeStatus) {
        if (FreezeUtils.isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
            this.values().forEach(val -> {
                if (val instanceof RefValue) {
                    ((RefValue) val).attemptFreeze(freezeStatus);
                }
            });
        }
    }

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

    public String stringValue() {
        Iterator<Map.Entry<Integer, Map.Entry<K, V>>> itr = entries.entrySet().iterator();
        return createStringValueDataEntry(itr);
    }

    private String createStringValueDataEntry(Iterator<Map.Entry<Integer, Map.Entry<K, V>>> itr) {
        StringJoiner sj = new StringJoiner("\n");
        while (itr.hasNext()) {
            Map.Entry<Integer, Map.Entry<K, V>> struct = itr.next();
            sj.add(struct.getValue().getValue().toString());
        }
        return sj.toString();
    }

    @Override
    public BType getType() {
        return this.type;
    }

    public BType getIteratorNextReturnType() {
        if (iteratorNextReturnType == null) {
            iteratorNextReturnType = IteratorUtils.createIteratorNextReturnType(type.getConstrainedType());
        }

        return iteratorNextReturnType;
    }

    static class TableIterator<K, V> implements IteratorValue {

        Iterator<Map.Entry<K, V>> iterator;

        TableIterator(Iterator<Map.Entry<K, V>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Object next() {
            Map.Entry<K, V> next = iterator.next();
            V value = next.getValue();
            K key = next.getKey();

            List<BType> types = new LinkedList<>();
            types.add(TypeChecker.getType(key));
            types.add(TypeChecker.getType(value));
            BTupleType tupleType = new BTupleType(types);

            TupleValueImpl tuple = new TupleValueImpl(tupleType);
            tuple.add(0, key);
            tuple.add(1, value);
            return tuple;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
    }

    private class ValueHolder {

        public void addData(V data) {
            Map.Entry<K, V> entry = new AbstractMap.SimpleEntry(data, data);
            UUID uuid = UUID.randomUUID();
            entries.put(uuid.hashCode(), entry);
            values.put(uuid.hashCode(), data);
        }

        public V getData(K key) {
            throw BallerinaErrors.createError(TABLE_KEY_NOT_FOUND_ERROR, "cannot find key '" + key + "'");
        }

        public V putData(K key, V data) {
            throw BallerinaErrors.createError(TABLE_KEY_NOT_FOUND_ERROR, "cannot find key '" + key + "'");
        }

        public V remove(K key) {
            throw BallerinaErrors.createError(TABLE_KEY_NOT_FOUND_ERROR, "cannot find key '" + key + "'");
        }

        public boolean containsKey(K key) {
            return false;
        }
    }

    private class KeyHashValueHolder extends ValueHolder {
        private DefaultKeyWrapper keyWrapper;
        private List<BType> keyTypes;

        public KeyHashValueHolder() {
            super();
            keyTypes = new ArrayList<>();
            if (fieldNames.length > 1) {
                populateKeyTypes();
                keyWrapper = new MultiKeyWrapper();
            } else {
                keyWrapper = new DefaultKeyWrapper();
            }
        }

        private void populateKeyTypes() {
            BType constraintType = type.getConstrainedType();
            if (constraintType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                BRecordType recordType = (BRecordType) constraintType;
                Arrays.stream(fieldNames).forEach(field -> keyTypes.add(recordType.getFields().get(field).type));
            } else if (constraintType.getTag() == TypeTags.MAP_TAG) {
                BMapType mapType = (BMapType) constraintType;
                Arrays.stream(fieldNames).forEach(field -> keyTypes.add(mapType.getConstrainedType()));
            }
        }

        public void addData(V data) {
            MapValue dataMap = (MapValue) data;
            Object key = this.keyWrapper.wrapKey(dataMap);

            if (containsKey((K) key)) {
                throw BallerinaErrors.createError(TABLE_HAS_A_VALUE_FOR_KEY_ERROR, "A value found for key '" +
                        key + "'");
            }

            Map.Entry<K, V> entry = new AbstractMap.SimpleEntry(key, data);
            Integer hash = TableUtils.hash(key);
            keys.put(hash, (K) key);
            values.put(hash, (V) data);
            entries.put(hash, entry);
        }

        public V getData(K key) {
            return values.get(TableUtils.hash(key));
        }

        public V putData(K key, V data) {
            Map.Entry<K, V> entry = new AbstractMap.SimpleEntry<>(key, data);
            Object acutalKey = this.keyWrapper.wrapKey((MapValue) data);
            Integer actualHash = TableUtils.hash(acutalKey);
            Integer hash = TableUtils.hash(key);

            if (!hash.equals(actualHash)) {
                throw BallerinaErrors.createError(TABLE_KEY_NOT_FOUND_ERROR, "The key '" +
                        key + "' not found in value " + data.toString());
            }

            entries.put(hash, entry);
            keys.put(hash, key);
            return values.put(hash, data);
        }

        public V remove(K key) {
            Integer hash = TableUtils.hash(key);
            entries.remove(hash);
            keys.remove(hash);
            return values.remove(hash);
        }

        public boolean containsKey(K key) {
            return keys.containsKey(TableUtils.hash(key));
        }

        private class DefaultKeyWrapper {
            public Object wrapKey(MapValue data) {
                return data.get(fieldNames[0]);
            }
        }

        private class MultiKeyWrapper extends DefaultKeyWrapper {
            public Object wrapKey(MapValue data) {
                TupleValueImpl arr = (TupleValueImpl) BValueCreator
                        .createTupleValue(new BTupleType(keyTypes));
                for (int i = 0; i < fieldNames.length; i++) {
                    arr.add(i, data.get(fieldNames[i]));
                }
                return arr;
            }
        }
    }
}

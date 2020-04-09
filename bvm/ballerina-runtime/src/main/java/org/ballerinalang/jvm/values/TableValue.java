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
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.api.BIterator;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.TABLE_HAS_A_VALUE_FOR_KEY_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.TABLE_KEY_NOT_FOUND_ERROR;

public class TableValue<K, V> implements BTable<K, V> {

    private BTableType type;
    private volatile Status freezeStatus = new Status(State.UNFROZEN);
    private BType iteratorNextReturnType;
    private LinkedHashMap<K, V> hashtable;
    private ValueAdder valueAdder;

    public TableValue(BTableType type) {
        this.type = type;
        this.hashtable = new LinkedHashMap<>();
        if (type.getFieldNames() != null) {
            this.valueAdder = new KeyHashValueAdder(hashtable, type.getFieldNames());
        } else {
            this.valueAdder = new ValueAdder(hashtable);
        }
    }

    public TableValue(BTableType type, ArrayValue data) {
        this(type);
        addData(data);
    }

    private void addData(ArrayValue data) {
        BIterator itr = data.getIterator();
        while (itr.hasNext()) {
            Object next = itr.next();
            valueAdder.addData((V) next);
        }
    }


    @Override
    public IteratorValue getIterator() {
        return new TableIterator<K, V>(new LinkedHashMap<K, V>(hashtable).entrySet().iterator());
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
        return hashtable.get(key);
    }

    @Override
    public V put(K key, V value) {
        if (containsKey(key)) {
            throw BallerinaErrors.createError(TABLE_HAS_A_VALUE_FOR_KEY_ERROR, "A value found for key '" + key + "'");
        }
        return hashtable.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return hashtable.remove(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return hashtable.containsKey(key);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return hashtable.entrySet();
    }

    @Override
    public Collection<V> values() {
        return hashtable.values();
    }

    @Override
    public void clear() {
        hashtable.clear();
    }

    @Override
    public V getOrThrow(Object key) {
        if (!containsKey(key)) {
            throw BallerinaErrors.createError(TABLE_KEY_NOT_FOUND_ERROR, "cannot find key '" + key + "'");
        }
        return this.get(key);
    }

    @Override
    public V fillAndGet(Object key) {
        return null;
    }

    @Override
    public K[] getKeys() {
        return (K[]) hashtable.keySet().toArray();
    }

    @Override
    public int size() {
        return hashtable.size();
    }

    @Override
    public boolean isEmpty() {
        return hashtable.isEmpty();
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
        Iterator<Map.Entry<K, V>> itr = hashtable.entrySet().iterator();
        return createStringValueDataEntry(itr);
    }

    private String createStringValueDataEntry(Iterator<Map.Entry<K, V>> itr) {
        StringJoiner sj = new StringJoiner("\n");
        while (itr.hasNext()) {
            Map.Entry<K, V> struct = itr.next();
            sj.add(struct.getValue().toString());
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

    private class ValueAdder {
        protected LinkedHashMap<K, V> hashtable;

        public ValueAdder(LinkedHashMap<K, V> hashtable) {
            this.hashtable = hashtable;
        }

        public void addData(V data) {
            this.hashtable.put((K) data, data);
        }
    }

    private class KeyHashValueAdder extends ValueAdder {
        private String[] fieldNames;

        public KeyHashValueAdder(LinkedHashMap<K, V> hashtable, String[] fieldNames) {
            super(hashtable);
            this.fieldNames = fieldNames;
        }

        public void addData(V data) {
            MapValue dataMap = (MapValue) data;
            ArrayValueImpl arr = (ArrayValueImpl) BValueCreator
                    .createArrayValue(new BArrayType(BTypes.typeAny, fieldNames.length));
            for (int i = 0; i < fieldNames.length; i++) {
                arr.add(i, dataMap.get(fieldNames[i]));
            }
            this.hashtable.put((K) arr, data);
        }
    }
}

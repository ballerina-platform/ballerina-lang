/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.StructField;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implantation of {@link org.ballerinalang.connector.api.Struct}
 *
 * @since 0.965.0
 */
public class StructImpl extends AnnotatableNode implements Struct {

    private BStruct value;
    private BStructType type;
    private Map<String, StructFieldImpl> structFields = new LinkedHashMap<>();
    private int[] indexes = new int[] {-1, -1, -1, -1, -1, -1};

    StructImpl(BStruct value) {
        this.value = value;
        type = value.getType();
        Arrays.stream(value.getType().getStructFields()).forEach(sf -> {
            final StructFieldImpl structField = new StructFieldImpl(sf.fieldName,
                    sf.fieldType.getSuperType().getTag());
            setIndex(structField, indexes);
            structFields.put(sf.fieldName, structField);
        });
    }

    @Override
    public String getName() {
        return type.getName();
    }

    @Override
    public String getPackage() {
        return type.getPackagePath();
    }

    @Override
    public StructFieldImpl[] getFields() {
        return structFields.values().toArray(new StructFieldImpl[0]);
    }

    @Override
    public long getIntField(String fieldName) {
        return value.getIntField(getFieldIndex(fieldName));
    }

    @Override
    public double getFloatField(String fieldName) {
        return value.getFloatField(getFieldIndex(fieldName));
    }

    @Override
    public String getStringField(String fieldName) {
        return value.getStringField(getFieldIndex(fieldName));
    }

    @Override
    public boolean getBooleanField(String fieldName) {
        return value.getBooleanField(getFieldIndex(fieldName)) == 1;
    }

    @Override
    public Struct getStructField(String fieldName) {
        final BStruct refField = (BStruct) value.getRefField(getFieldIndex(fieldName));
        if (refField == null) {
            return null;
        }
        return new StructImpl(refField);
    }

    @Override
    public Value[] getArrayField(String fieldName) {
        final BNewArray refField = (BNewArray) value.getRefField(getFieldIndex(fieldName));
        if (refField == null) {
            return null;
        }
        List<Value> list = new ArrayList<>();
        final BIterator bIterator = refField.newIterator();
        while (bIterator.hasNext()) {
            list.add(ValueImpl.createValue(bIterator.getNext(1)[0]));
        }
        return list.toArray(new Value[0]);
    }

    @Override
    public BValue getVMValue() {
        return this.value;
    }

    @Override
    public Map<String, Value> getMapField(String fieldName) {
        final BMap refField = (BMap) value.getRefField(getFieldIndex(fieldName));
        if (refField == null) {
            return null;
        }
        Map<String, Value> valueMap = new LinkedHashMap<>();
        final BIterator bIterator = refField.newIterator();
        while (bIterator.hasNext()) {
            final BValue[] next = bIterator.getNext(2);
            valueMap.put(next[0].stringValue(), ValueImpl.createValue(next[1]));
        }
        return valueMap;
    }

    @Override
    public Value getTypeField(String fieldName) {
        final BTypeDescValue refField = (BTypeDescValue) value.getRefField(getFieldIndex(fieldName));
        if (refField == null) {
            return null;
        }
        return ValueImpl.createValue(refField);
    }

    @Override
    public Value getRefField(String fieldName) {
        final BValue refField = value.getRefField(getFieldIndex(fieldName));

        if (refField == null) {
            return null;
        }
        return ValueImpl.createValue(refField);
    }

    private int getFieldIndex(String fieldName) {
        final StructFieldImpl structField = structFields.get(fieldName);
        if (structField == null) {
            throw new RuntimeException(fieldName + " not found");
        }
        return structField.index;
    }

    @Override
    public List<Annotation> getAnnotationList(String pkgPath, String name) {
        String key = pkgPath + ":" + name;
        return annotationMap.get(key);
    }

    @Override
    public void addNativeData(String key, Object data) {
        value.addNativeData(key, data);
    }

    @Override
    public Object getNativeData(String key) {
        return value.getNativeData(key);
    }

    @Override
    public String getAnnotationEntryKey() {
        return getName();
    }

    /**
     * Implementation of {@link StructFieldImpl}
     *
     * @since 0.965.0
     */
    static class StructFieldImpl implements StructField {

        private final String name;
        private final Value.Type field;
        protected int index;

        StructFieldImpl(String name, int tag) {
            this.name = name;
            field = Value.Type.getType(tag);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Value.Type getFieldType() {
            return field;
        }
    }

    private static void setIndex(StructFieldImpl structField, int[] currentIndex) {
        switch (structField.field.getTag()) {
            case TypeTags.INT_TAG:
                structField.index = ++currentIndex[0];
                break;
            case TypeTags.FLOAT_TAG:
                structField.index = ++currentIndex[1];
                break;
            case TypeTags.STRING_TAG:
                structField.index = ++currentIndex[2];
                break;
            case TypeTags.BOOLEAN_TAG:
                structField.index = ++currentIndex[3];
                break;
            case TypeTags.BLOB_TAG:
                structField.index = ++currentIndex[4];
                break;
            default:
                structField.index = ++currentIndex[5];
        }
    }
}

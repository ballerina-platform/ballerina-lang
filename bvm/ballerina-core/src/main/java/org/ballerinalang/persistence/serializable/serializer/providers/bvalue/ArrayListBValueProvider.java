/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence.serializable.serializer.providers.bvalue;

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.serializer.BValueDeserializer;
import org.ballerinalang.persistence.serializable.serializer.BValueSerializer;
import org.ballerinalang.persistence.serializable.serializer.JsonSerializerConst;
import org.ballerinalang.persistence.serializable.serializer.SerializationBValueProvider;

import java.util.ArrayList;

/**
 * Provide mapping between {@link Class} and {@link BValue} representation of it.
 */
public class ArrayListBValueProvider implements SerializationBValueProvider<ArrayList> {
    @Override
    public String typeName() {
        return getType().getName();
    }

    @Override
    public Class<?> getType() {
        return ArrayList.class;
    }

    @Override
    public BValue toBValue(ArrayList list, BValueSerializer serializer) {
        BRefValueArray array = new BRefValueArray(BTypes.typeAny);
        for (Object item : list) {
            array.append((BRefType) serializer.toBValue(item, null));
        }
        BMap<String, BValue> bMap = BValueProviderHelper.wrap(typeName(), array);
        bMap.put(JsonSerializerConst.LENGTH_TAG, new BInteger(list.size()));
        return bMap;
    }

    @Override
    public ArrayList toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
        if (bValue instanceof BMap) {
            @SuppressWarnings("unchecked")
            BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
            BRefValueArray array = (BRefValueArray) BValueProviderHelper.getPayload(wrapper);
            BInteger length = (BInteger) wrapper.get(JsonSerializerConst.LENGTH_TAG);
            ArrayList arrayList = new ArrayList((int) length.intValue());

            for (int i = 0; i < array.size(); i++) {
                arrayList.add(array.get(i));
            }
            return arrayList;
        }
        throw BValueProviderHelper.deserializationIncorrectType(bValue, typeName());
    }
}

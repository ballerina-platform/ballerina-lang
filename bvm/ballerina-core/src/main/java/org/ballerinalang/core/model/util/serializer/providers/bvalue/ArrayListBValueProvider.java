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
package org.ballerinalang.core.model.util.serializer.providers.bvalue;

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.util.serializer.BPacket;
import org.ballerinalang.core.model.util.serializer.BValueDeserializer;
import org.ballerinalang.core.model.util.serializer.BValueSerializer;
import org.ballerinalang.core.model.util.serializer.JsonSerializerConst;
import org.ballerinalang.core.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;

import java.util.ArrayList;

/**
 * Provide mapping between {@link Class} and {@link BValue} representation of it.
 *
 * @since 0.982.0
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
    public BPacket toBValue(ArrayList list, BValueSerializer serializer) {
        BValueArray array = new BValueArray(new BArrayType(BTypes.typeAny));
        for (Object item : list) {
            array.append((BRefType) serializer.toBValue(item, null));
        }
        return BPacket
                .from(typeName(), array)
                .put(JsonSerializerConst.LENGTH_TAG, new BInteger(list.size()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
        BInteger length = (BInteger) packet.get(JsonSerializerConst.LENGTH_TAG);
        BValueArray array = (BValueArray) packet.getValue();
        ArrayList arrayList = new ArrayList((int) length.intValue());

        int i = 0;
        for (; i < array.size(); i++) {
            arrayList.add(bValueDeserializer.deserialize(array.getRefValue(i), null));
        }
        for (; i < length.intValue(); i++) {
            arrayList.add(i, null);
        }
        return arrayList;
    }
}

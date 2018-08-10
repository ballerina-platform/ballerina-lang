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

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.serializer.BTypes;
import org.ballerinalang.persistence.serializable.serializer.BValueDeserializer;
import org.ballerinalang.persistence.serializable.serializer.BValueSerializer;
import org.ballerinalang.persistence.serializable.serializer.JsonSerializerConst;
import org.ballerinalang.persistence.serializable.serializer.SerializationBValueProvider;

/**
 * Provide mapping between {@link BRefValueArray} and {@link BValue} representation of it.
 */
public class BRefValueArrayBValueProvider implements SerializationBValueProvider {

    private static final String ARRAY_TYPE = "arrayType";

    @Override
    public String typeName() {
        return BRefValueArray.class.getSimpleName();
    }

    @Override
    public Class<?> getType() {
        return BRefValueArray.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BValue toBValue(Object object, BValueSerializer serializer) {
        if (object instanceof BRefValueArray) {
            BRefValueArray array = (BRefValueArray) object;

            BRefType[] newArray = new BRefType[Long.valueOf(array.size()).intValue()];
            for (int i = 0; i < array.size(); i++) {
                newArray[i] = (BRefType) serializer.toBValue(array.get(i), Object.class);
            }
            BValue wrapped = BValueProviderHelper.wrap(typeName(), new BRefValueArray(newArray, array.getType()));
            ((BMap) wrapped).put(ARRAY_TYPE, new BString(array.getType().toString()));
            return wrapped;
        }
        throw BValueProviderHelper.incorrectObjectType(object, typeName());
    }

    @Override
    public Object toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
        if (bValue instanceof BMap) {
            @SuppressWarnings("unchecked")
            BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
            if (BValueProviderHelper.isWrapperOfType(wrapper, typeName())) {
                BRefValueArray refValueArray = (BRefValueArray) wrapper.get(JsonSerializerConst.PAYLOAD_TAG);
                BRefType[] newArray = new BRefType[Long.valueOf(refValueArray.size()).intValue()];
                for (int i = 0; i < newArray.length; i++) {
                    newArray[i] = (BRefType) bValueDeserializer.deserialize(refValueArray.get(i), BRefType.class);
                }
                BString arrayType = (BString) wrapper.get(ARRAY_TYPE);
                BType bType = BTypes.fromString(arrayType.stringValue());

                return new BRefValueArray(newArray, bType);
            }
        }
        throw BValueProviderHelper.deserializationIncorrectType(bValue, typeName());
    }
}

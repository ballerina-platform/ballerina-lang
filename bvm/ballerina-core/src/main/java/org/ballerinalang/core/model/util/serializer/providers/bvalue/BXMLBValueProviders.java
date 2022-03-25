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
import org.ballerinalang.core.model.util.XMLUtils;
import org.ballerinalang.core.model.util.serializer.BPacket;
import org.ballerinalang.core.model.util.serializer.BValueDeserializer;
import org.ballerinalang.core.model.util.serializer.BValueSerializer;
import org.ballerinalang.core.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.core.model.values.BXMLItem;
import org.ballerinalang.core.model.values.BXMLQName;
import org.ballerinalang.core.model.values.BXMLSequence;

/**
 * Collection of {@link SerializationBValueProvider}s to support
 * Ballerina XML types.
 *
 * @since 0.982.0
 */
public class BXMLBValueProviders {
    private BXMLBValueProviders() {
    }

    /**
     * Convert {@link BXMLItem} into {@link BValue} object and back to facilitate serialization.
     */
    public static class BXMLItemBValueProvider implements SerializationBValueProvider<BXMLItem> {

        @Override
        public String typeName() {
            return getType().getSimpleName();
        }

        @Override
        public Class<?> getType() {
            return BXMLItem.class;
        }

        @Override
        public BPacket toBValue(BXMLItem object, BValueSerializer serializer) {
            return BPacket.from(typeName(), new BString(object.toString()));
        }

        @Override
        public BXMLItem toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            String s = packet.getValue().stringValue();
            return (BXMLItem) XMLUtils.parse(s);
        }
    }

    /**
     * Convert {@link BXMLSequence} into {@link BValue} object and back to facilitate serialization.
     */
    public static class BXMLSequenceBValueProvider implements SerializationBValueProvider<BXMLSequence> {

        @Override
        public String typeName() {
            return getType().getSimpleName();
        }

        @Override
        public Class<?> getType() {
            return BXMLSequence.class;
        }

        @Override
        public BPacket toBValue(BXMLSequence bxmlSequence, BValueSerializer serializer) {
            BRefType[] serializedItems = new BRefType[(int) bxmlSequence.size()];
            for (int i = 0; i < bxmlSequence.size(); i++) {
                BRefType value = (BRefType) serializer.toBValue(bxmlSequence.value().getRefValue(i), null);
                serializedItems[i] = value;
            }
            BValueArray array = new BValueArray(serializedItems, new BArrayType(BTypes.typeAny));
            return BPacket.from(typeName(), array);
        }

        @Override
        public BXMLSequence toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            BValueArray srcArray = (BValueArray) packet.getValue();
            BRefType[] values = new BRefType[(int) srcArray.size()];
            for (int i = 0; i < srcArray.size(); i++) {
                values[i] = (BXML<?>) bValueDeserializer.deserialize(srcArray.getRefValue(i), Object.class);
            }
            BValueArray xmlItemArray = new BValueArray(values, new BArrayType(BTypes.typeXML));
            return new BXMLSequence(xmlItemArray);
        }
    }

    /**
     * Convert {@link BXMLQName} into {@link BValue} object and back to facilitate serialization.
     */
    public static class BXMLQNameBValueProvider implements SerializationBValueProvider<BXMLQName> {
        static final String LOCAL = "LOCAL";
        static final String PREFIX = "PREFIX";

        @Override
        public String typeName() {
            return getType().getSimpleName();
        }

        @Override
        public Class<?> getType() {
            return BXMLQName.class;
        }

        @Override
        public BPacket toBValue(BXMLQName bxmlqName, BValueSerializer serializer) {
            return BPacket.from(typeName(), new BString(bxmlqName.getUri()))
                    .put(LOCAL, new BString(bxmlqName.getLocalName()))
                    .put(PREFIX, new BString(bxmlqName.getPrefix()));
        }

        @Override
        public BXMLQName toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            String uri = packet.getValue().stringValue();
            String local = packet.get(LOCAL).stringValue();
            String prefix = packet.get(PREFIX).stringValue();
            return new BXMLQName(local, uri, prefix);
        }
    }
}

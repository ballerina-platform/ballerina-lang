/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.State;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.TypedescValueImpl;
import io.ballerina.runtime.internal.values.ValueCreator;

import java.util.Map;
import java.util.Set;

/**
 * Class @{@link ValueUtils} provides utils to create Ballerina Values.
 *
 * @since 2.0.0
 */
public class ValueUtils {

    /**
     * Create a record value using the given package id and record type name.
     *
     * @param packageId      the package id that the record type resides.
     * @param recordTypeName name of the record type.
     * @return value of the record.
     */
    public static BMap<BString, Object> createRecordValue(Module packageId, String recordTypeName) {
        io.ballerina.runtime.internal.values.ValueCreator valueCreator =
                io.ballerina.runtime.internal.values.ValueCreator.getValueCreator(ValueCreator.
                        getLookupKey(packageId));
        return valueCreator.createRecordValue(recordTypeName);
    }

    /**
     * Create a record value that populates record fields using the given package id, record type name and a map of
     * field names and associated values for fields.
     *
     * @param packageId      the package id that the record type resides.
     * @param recordTypeName name of the record type.
     * @param valueMap       values to be used for fields when creating the record.
     * @return value of the populated record.
     */
    public static BMap<BString, Object> createRecordValue(Module packageId, String recordTypeName,
                                                          Map<String, Object> valueMap) {
        BMap<BString, Object> record = createRecordValue(packageId, recordTypeName);
        for (Map.Entry<String, Object> fieldEntry : valueMap.entrySet()) {
            Object val = fieldEntry.getValue();
            if (val instanceof String) {
                val = StringUtils.fromString((String) val);
            }
            record.populateInitialValue(StringUtils.fromString(fieldEntry.getKey()), val);
        }

        return record;
    }

    /**
     * Populate a runtime record value with given field values.
     *
     * @param record which needs to get populated
     * @param values field values of the record.
     * @return value of the record.
     */
    public static BMap<BString, Object> createRecordValue(BMap<BString, Object> record, Object... values) {
        BRecordType recordType = (BRecordType) record.getType();
        MapValue<BString, Object> mapValue = new MapValueImpl<>(recordType);
        int i = 0;
        for (Map.Entry<String, Field> fieldEntry : recordType.getFields().entrySet()) {
            Object value = values[i++];
            if (SymbolFlags.isFlagOn(fieldEntry.getValue().getFlags(), SymbolFlags.OPTIONAL) && value == null) {
                continue;
            }

            mapValue.put(StringUtils.fromString(fieldEntry.getKey()), value instanceof String ?
                    StringUtils.fromString((String) value) : value);
        }
        return mapValue;
    }

    /**
     * Create an object value using the given package id and object type name.
     *
     * @param packageId      the package id that the object type resides.
     * @param objectTypeName name of the object type.
     * @param fieldValues    values to be used for fields when creating the object value instance.
     * @return value of the object.
     */
    public static BObject createObjectValue(Module packageId, String objectTypeName, Object... fieldValues) {
        Strand currentStrand = Scheduler.getStrandNoException();
        // This method duplicates the createObjectValue with referencing the issue in runtime API getting strand
        io.ballerina.runtime.internal.values.ValueCreator
                valueCreator =  io.ballerina.runtime.internal.values.ValueCreator.getValueCreator(ValueCreator
                .getLookupKey(packageId));

        // Here the variables are initialized with default values
        Scheduler scheduler = null;
        State prevState = State.RUNNABLE;
        boolean prevBlockedOnExtern = false;
        BObject objectValue;

        try {
            // Check for non-blocking call
            if (currentStrand != null) {
                scheduler = currentStrand.scheduler;
                prevBlockedOnExtern = currentStrand.blockedOnExtern;
                prevState = currentStrand.getState();
                currentStrand.blockedOnExtern = false;
                currentStrand.setState(State.RUNNABLE);
            }
            objectValue = valueCreator.createObjectValue(objectTypeName, scheduler, currentStrand,
                                                         null, fieldValues);
        } finally {
            if (currentStrand != null) {
                currentStrand.blockedOnExtern = prevBlockedOnExtern;
                currentStrand.setState(prevState);
            }
        }
        return objectValue;
    }

    private ValueUtils() {
    }

    /**
     * Provide the readonly Xml Value that is equivalent to a given string value.
     *
     * @param value string value
     * @return immutable Xml value
     */
    public static BXml createReadOnlyXmlValue(String value) {
        BXml xml = TypeConverter.stringToXml(value);
        xml.freezeDirect();
        return xml;
    }

    /**
     * Provide the Typedesc Value with the singleton type with a value.
     * @param value Ballerina value
     * @return typedesc with singleton type
     */
    public static BTypedesc createSingletonTypedesc(BValue value) {
        return io.ballerina.runtime.api.creators.ValueCreator
                .createTypedescValue(TypeCreator.createFiniteType(value.toString(), Set.of(value), 0));
    }

    /**
     * Provide the Typedesc Value depending on the immutability of a value.
     * @param type Ballerina value
     * @return typedesc with the suitable type
     */
    public static BTypedesc getTypedescValue(Type type, BValue value) {
        if (type.isReadOnly()) {
            return createSingletonTypedesc(value);
        }
        return new TypedescValueImpl(type);
    }

}

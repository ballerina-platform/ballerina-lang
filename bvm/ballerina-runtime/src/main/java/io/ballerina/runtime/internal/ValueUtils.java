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
import io.ballerina.runtime.api.values.BError;
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
     * Create a record value using the given package ID and record type name.
     *
     * @param packageId      the package ID where the record type is defined.
     * @param recordTypeName name of the record type.
     * @return               value of the record.
     */
    public static BMap<BString, Object> createRecordValue(Module packageId, String recordTypeName) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(ValueCreator.getLookupKey(packageId, false));
        try {
            return valueCreator.createRecordValue(recordTypeName);
        } catch (BError e) {
            // If record type definition not found, get it from test module.
            String testLookupKey = ValueCreator.getLookupKey(packageId, true);
            if (ValueCreator.containsValueCreator(testLookupKey)) {
                return ValueCreator.getValueCreator(testLookupKey).createRecordValue(recordTypeName);
            }
            throw e;
        }
    }

    /**
     * Create a record value that populates record fields using the given package ID, record type name and a map of
     * field names and associated values for the fields.
     *
     * @param packageId      the package ID where the record type is defined.
     * @param recordTypeName name of the record type.
     * @param valueMap       values to be used for fields when creating the record.
     * @return               value of the populated record.
     */
    public static BMap<BString, Object> createRecordValue(Module packageId, String recordTypeName,
                                                          Map<String, Object> valueMap) {
        BMap<BString, Object> recordValue = createRecordValue(packageId, recordTypeName);
        for (Map.Entry<String, Object> fieldEntry : valueMap.entrySet()) {
            Object val = fieldEntry.getValue();
            // TODO: Remove the following String to BString conversion.
            if (val instanceof String) {
                val = StringUtils.fromString((String) val);
            }
            recordValue.populateInitialValue(StringUtils.fromString(fieldEntry.getKey()), val);
        }
        return recordValue;
    }

    /**
     * Create a record value that populates record fields using the given package ID, record type name and
     * a {@link BMap} of field names and associated values for the fields.
     *
     * @param packageId      the package ID where the record type is defined.
     * @param recordTypeName name of the record type.
     * @param valueMap       {@link BMap} of fields and values to initialize the record.
     * @return               value of the populated record.
     */
    public static BMap<BString, Object> createRecordValue(Module packageId, String recordTypeName,
                                                          BMap<BString, Object> valueMap) {
        BMap<BString, Object> recordValue = createRecordValue(packageId, recordTypeName);
        for (Map.Entry<BString, Object> fieldEntry : valueMap.entrySet()) {
            recordValue.populateInitialValue(fieldEntry.getKey(), fieldEntry.getValue());
        }
        return recordValue;
    }

    /**
     * Populate a runtime record value with given field values.
     *
     * @param recordValue record value which needs to get populated.
     * @param values      field values of the record.
     * @return            value of the record.
     */
    public static BMap<BString, Object> createRecordValue(BMap<BString, Object> recordValue, Object... values) {
        BRecordType recordType = (BRecordType) recordValue.getType();
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
     * Create an object value using the given package ID and object type name.
     *
     * @param packageId      the package ID that the object type resides.
     * @param objectTypeName name of the object type.
     * @param fieldValues    values to be used for fields when creating the object value instance.
     * @return               value of the object.
     */
    public static BObject createObjectValue(Module packageId, String objectTypeName, Object... fieldValues) {
        Strand currentStrand = Scheduler.getStrandNoException();
        Object[] fields = new Object[fieldValues.length];
        // Adding boolean values for each arg
        for (int i = 0, j = 0; i < fieldValues.length; i++) {
            fields[j++] = fieldValues[i];
        }
        return createObjectValue(currentStrand, packageId, objectTypeName, fields);
    }

    /**
     * Create object value with strand, package ID, object type name and given field values.
     *
     * @param currentStrand   current strand.
     * @param packageId       the package ID that the object type resides.
     * @param objectTypeName  name of the object type.
     * @param fieldValues     values to be used for fields when creating the object value instance.
     * @return                value of the object.
     */
    public static BObject createObjectValue(Strand currentStrand, Module packageId, String objectTypeName,
                                             Object[] fieldValues) {

        // This method duplicates the createObjectValue with referencing the issue in runtime API getting strand
        io.ballerina.runtime.internal.values.ValueCreator
                valueCreator =  io.ballerina.runtime.internal.values.ValueCreator.getValueCreator(ValueCreator
                .getLookupKey(packageId, false));

        // Here the variables are initialized with default values
        Scheduler scheduler = null;
        State prevState = State.RUNNABLE;
        boolean prevBlockedOnExtern = false;

        try {
            // Check for non-blocking call
            if (currentStrand != null) {
                scheduler = currentStrand.scheduler;
                prevBlockedOnExtern = currentStrand.blockedOnExtern;
                prevState = currentStrand.getState();
                currentStrand.blockedOnExtern = false;
                currentStrand.setState(State.RUNNABLE);
            }
            try {
                return valueCreator.createObjectValue(objectTypeName, scheduler, currentStrand,
                        null, fieldValues);
            } catch (BError e) {
                // If object type definition not found, get it from test module.
                String testLookupKey = ValueCreator.getLookupKey(packageId, true);
                if (ValueCreator.containsValueCreator(testLookupKey)) {
                    valueCreator = ValueCreator.getValueCreator(testLookupKey);
                    return valueCreator.createObjectValue(objectTypeName, scheduler, currentStrand,
                            null, fieldValues);
                }
                throw e;
            }
        } finally {
            if (currentStrand != null) {
                currentStrand.blockedOnExtern = prevBlockedOnExtern;
                currentStrand.setState(prevState);
            }
        }
    }

    private ValueUtils() {
    }

    /**
     * Provide the readonly Xml Value that is equivalent to a given string value.
     *
     * @param value string value
     * @return      immutable Xml value
     */
    public static BXml createReadOnlyXmlValue(String value) {
        BXml xml = TypeConverter.stringToXml(value);
        xml.freezeDirect();
        return xml;
    }

    /**
     * Provide the Typedesc Value with the singleton type with a value.
     * @param value Ballerina value
     * @return      typedesc with singleton type
     */
    public static BTypedesc createSingletonTypedesc(BValue value) {
        return io.ballerina.runtime.api.creators.ValueCreator
                .createTypedescValue(TypeCreator.createFiniteType(null, Set.of(value), 0));
    }

    /**
     * Provide the Typedesc Value depending on the immutability of a value.
     * @param type Ballerina value
     * @return     typedesc with the suitable type
     */
    public static BTypedesc getTypedescValue(Type type, BValue value) {
        if (type.isReadOnly()) {
            return createSingletonTypedesc(value);
        }
        return new TypedescValueImpl(type);
    }

    /**
     * Provide the Typedesc Value depending on the immutability of a value.
     * @param readOnly Indicates that the type is a subtype of readonly
     * @param value Ballerina value
     * @param inherentType Inherent type of the value
     * @return     typedesc with the suitable type
     */
    public static BTypedesc getTypedescValue(Boolean readOnly, BValue value, TypedescValueImpl inherentType) {
        if (readOnly) {
            TypedescValueImpl typedesc = (TypedescValueImpl) createSingletonTypedesc(value);
            typedesc.annotations = inherentType.annotations;
            return typedesc;
        }
        return inherentType;
    }
}

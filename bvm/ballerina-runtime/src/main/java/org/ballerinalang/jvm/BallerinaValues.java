/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.State;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.ValueCreator;
import org.ballerinalang.jvm.values.api.BString;

import java.util.Map;
import java.util.Map.Entry;

/**
 * The factory utility class that creates runtime values from given package and type names.
 *
 * @since 0.995.0
 */
public class BallerinaValues {

    /**
     * Method that creates a runtime record value using the given package id and record type name.
     *
     * @param packageId the package id that the record type resides.
     * @param recordTypeName name of the record type.
     * @return value of the record.
     */
    public static MapValue<BString, Object> createRecordValue(BPackage packageId, String recordTypeName) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(packageId.toString());
        return valueCreator.createRecordValue(recordTypeName);
    }

    /**
     * Method that populates record fields using the given package id, record type name and a map of
     * field names and associated values for fields.
     *
     * @param packageId the package id that the record type resides.
     * @param recordTypeName name of the record type.
     * @param valueMap values to be used for fields when creating the record.
     * @return value of the populated record.
     */
    public static MapValue<BString, Object> createRecordValue(BPackage packageId, String recordTypeName,
                                                              Map<String, Object> valueMap) {
        MapValue<BString, Object> record = createRecordValue(packageId, recordTypeName);
        for (Entry<String, Object> fieldEntry : valueMap.entrySet()) {
            Object val = fieldEntry.getValue();
            if (val instanceof String) {
                val = StringUtils.fromString((String) val);
            }
            record.put(StringUtils.fromString(fieldEntry.getKey()), val);
        }

        return record;
    }

    /**
     * Method that creates a runtime object value using the given package id and object type name.
     *
     * @param packageId the package id that the object type resides.
     * @param objectTypeName name of the object type.
     * @param fieldValues values to be used for fields when creating the object value instance.
     * @return value of the object.
     */
    @Deprecated
    public static ObjectValue createObjectValue(BPackage packageId, String objectTypeName, Object... fieldValues) {
        return createObjectValue(packageId, objectTypeName, getStrand(), fieldValues);
    }

    private static ObjectValue createObjectValue(BPackage packageId, String objectTypeName, Strand currentStrand,
                                                 Object... fieldValues) {
        // This method duplicates the createObjectValue with referencing the issue in runtime API getting strand
        ValueCreator valueCreator = ValueCreator.getValueCreator(packageId.toString());
        Object[] fields = new Object[fieldValues.length * 2];

        // Here the variables are initialized with default values
        Scheduler scheduler = null;
        State prevState = State.RUNNABLE;
        boolean prevBlockedOnExtern = false;
        ObjectValue objectValue;

        // Adding boolean values for each arg
        for (int i = 0, j = 0; i < fieldValues.length; i++) {
            fields[j++] = fieldValues[i];
            fields[j++] = true;
        }
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
                    null, fields);
        } finally {
            if (currentStrand != null) {
                currentStrand.blockedOnExtern = prevBlockedOnExtern;
                currentStrand.setState(prevState);
            }
        }
        return objectValue;
    }

    private static Strand getStrand() {
        try {
            return Scheduler.getStrand();
        } catch (Exception ex) {
            // Ignore : issue #22871 is opened to fix this
        }
        return null;
    }

    /**
     * Method to populate a runtime record value with given field values.
     *
     * @param record which needs to get populated
     * @param values field values of the record.
     * @return value of the record.
     */
    public static MapValue<BString, Object> createRecord(MapValue<BString, Object> record, Object... values) {
        BRecordType recordType = (BRecordType) record.getType();
        MapValue<BString, Object> mapValue = new MapValueImpl<>(recordType);
        int i = 0;
        for (Map.Entry<String, BField> fieldEntry : recordType.getFields().entrySet()) {
            Object value = values[i++];
            if (Flags.isFlagOn(fieldEntry.getValue().flags, Flags.OPTIONAL) && value == null) {
                continue;
            }

            mapValue.put(StringUtils.fromString(fieldEntry.getKey()), value instanceof String ?
                    StringUtils.fromString((String) value) : value);
        }
        return mapValue;
    }
}

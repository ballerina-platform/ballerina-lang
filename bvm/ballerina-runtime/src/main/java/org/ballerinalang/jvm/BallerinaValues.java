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

import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.ValueCreator;

import java.util.Map;
import java.util.Map.Entry;

/**
 * The factory utility class that creates runtime values from given package and type names.
 *
 * @since 0.995.0
 */
public class BallerinaValues {

    /**
     * Method that creates a runtime record value using the given package name and record type name.
     *
     * @deprecated
     * Use {@link BallerinaValues#createRecordValue(BPackage, String)} instead.
     *
     * @param pkgName the name of the package that the record type resides.
     * @param recordTypeName name of the record type.
     * @return value of the record.
     */
    @Deprecated
    public static MapValue<String, Object> createRecordValue(String pkgName, String recordTypeName) {
        throw new UnsupportedOperationException("This method is not supported now");
    }

    /**
     * Overloaded method that creates a runtime record value using the given package id and record type name.
     *
     * @param bPackage the package id that the record type resides.
     * @param recordTypeName name of the record type.
     * @return value of the record.
     */
    public static MapValue<String, Object> createRecordValue(BPackage bPackage, String recordTypeName) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(bPackage.toString());
        return valueCreator.createRecordValue(recordTypeName);
    }

    /**
     * Method that populates record fields using the given package name, record type name and a map of field names
     * and associated values for fields.
     *
     * @deprecated
     * Use {@link BallerinaValues#createRecordValue(BPackage, String, Map)} instead.
     *
     * @param pkgName the name of the package that the record type resides.
     * @param recordTypeName name of the record type.
     * @param valueMap values to be used for fields when creating the record.
     * @return value of the populated record.
     */
    @Deprecated
    public static MapValue<String, Object> createRecordValue(String pkgName, String recordTypeName,
                                                             Map<String, Object> valueMap) {
        throw new UnsupportedOperationException("This method is not supported now");
    }

    /**
     * Overloaded method that populates record fields using the given package id, record type name and a map of
     * field names and associated values for fields.
     *
     * @param bPackage the package id that the record type resides.
     * @param recordTypeName name of the record type.
     * @param valueMap values to be used for fields when creating the record.
     * @return value of the populated record.
     */
    public static MapValue<String, Object> createRecordValue(BPackage bPackage, String recordTypeName,
                                                             Map<String, Object> valueMap) {
        MapValue<String, Object> record = createRecordValue(bPackage, recordTypeName);
        for (Entry<String, Object> fieldEntry : valueMap.entrySet()) {
            record.put(fieldEntry.getKey(), fieldEntry.getValue());
        }

        return record;
    }

    /**
     * Method that creates a runtime object value using the given package name and object type name.
     *
     * @deprecated
     * Use {@link BallerinaValues#createObjectValue(BPackage, String, Object...)} instead.
     *
     * @param pkgName the name of the package that the object type resides.
     * @param objectTypeName name of the object type.
     * @param fieldValues values to be used for fields when creating the object value instance.
     * @return value of the object.
     */
    @Deprecated
    public static ObjectValue createObjectValue(String pkgName, String objectTypeName, Object... fieldValues) {
        throw new UnsupportedOperationException("This method is not supported now");
    }

    /**
     * Overloaded method that creates a runtime object value using the given package id and object type name.
     *
     * @param bPackage the package id that the object type resides.
     * @param objectTypeName name of the object type.
     * @param fieldValues values to be used for fields when creating the object value instance.
     * @return value of the object.
     */
    public static ObjectValue createObjectValue(BPackage bPackage, String objectTypeName, Object... fieldValues) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(bPackage.toString());
        Object[] fields = new Object[fieldValues.length * 2];

        // Adding boolean values for each arg
        for (int i = 0, j = 0; i < fieldValues.length; i++) {
            fields[j++] = fieldValues[i];
            fields[j++] = true;
        }
        //passing scheduler, strand and properties as null for the moment, but better to expose them via this method
        return valueCreator.createObjectValue(objectTypeName, null, null, null, fields);
    }

    /**
     * Method to populate a runtime record value with given field values.
     *
     * @param record which needs to get populated
     * @param values field values of the record.
     * @return value of the record.
     */
    public static MapValue<String, Object> createRecord(MapValue<String, Object> record, Object... values) {
        BRecordType recordType = (BRecordType) record.getType();
        MapValue<String, Object> mapValue = new MapValueImpl<>(recordType);
        int i = 0;
        for (Map.Entry<String, BField> fieldEntry : recordType.getFields().entrySet()) {
            mapValue.put(fieldEntry.getKey(), values[i++]);
        }
        return mapValue;
    }
}

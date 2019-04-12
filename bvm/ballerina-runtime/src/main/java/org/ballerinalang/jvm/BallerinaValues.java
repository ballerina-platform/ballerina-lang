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

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.ValueCreator;

/**
 * The factory utility class that creates runtime values from given package and type names.
 *
 * @since 0.995.0
 */
public class BallerinaValues {

    /**
     * Method that creates a runtime record value using the given package name and record type name.
     *
     * @param pkgName the name of the package that the record type resides.
     * @param recordTypeName name of the record type.
     * @return value of the record.
     */
    public static MapValue<String, Object> createRecordValue(String pkgName, String recordTypeName) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(pkgName);
        return valueCreator.createRecordValue(recordTypeName);
    }

    /**
     * Method that creates a runtime object value using the given package name and object type name.
     *
     * @param pkgName the name of the package that the record type resides.
     * @param objectTypeName name of the object type.
     * @return value of the object.
     */
    public static ObjectValue createObjectValue(String pkgName, String objectTypeName) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(pkgName);
        return valueCreator.createObjectValue(objectTypeName);
    }
}

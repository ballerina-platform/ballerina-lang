/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.testerina.utils;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.HashMap;

/**
 * This class contains a set of utility methods required for testerina runtime api @{@link ValueCreator} testing.
 *
 * @since 2201.1.0
 */
public class RuntimeApi {

    private static final Module objectModule = new Module("testorg", "runtime_api", "1");
    private static final Module recordModule = new Module("testorg", "runtime_api", "1");
    private static Module errorModule = new Module("testorg", "runtime_api", "1");


    public static BMap<BString, Object> getRecord(BString recordName) {
        HashMap<String, Object> address = new HashMap<>();
        address.put("city", StringUtils.fromString("Colombo"));
        address.put("country", StringUtils.fromString("Sri Lanka"));
        address.put("postalCode", 10250);
        return ValueCreator.createRecordValue(recordModule, recordName.getValue(), address);
    }

    public static BMap<BString, Object> getTestRecord(BString recordName) {
        HashMap<String, Object> address = new HashMap<>();
        address.put("city", StringUtils.fromString("Kandy"));
        address.put("country", StringUtils.fromString("Sri Lanka"));
        address.put("postalCode", 10250);
        return ValueCreator.createRecordValue(recordModule, recordName.getValue(), address);
    }

    public static BObject getObject(BString objectName) {
        BMap<BString, Object> address = getRecord(StringUtils.fromString("Address"));
        return ValueCreator.createObjectValue(objectModule, objectName.getValue(), StringUtils.fromString("Waruna"),
                14, address);
    }

    public static BObject getTestObject(BString objectName) {
        BMap<BString, Object> address = getTestRecord(StringUtils.fromString("TestAddress"));
        return ValueCreator.createObjectValue(objectModule, objectName.getValue(), StringUtils.fromString("Waruna"),
                14,  address);
    }

    public static BError getError(BString errorName) {
        BMap<BString, Object> errorDetails = ValueCreator.createMapValue();
        errorDetails.put(StringUtils.fromString("cause"), StringUtils.fromString("Person age cannot be negative"));
        return ErrorCreator.createError(errorModule, errorName.getValue(), StringUtils.fromString("Invalid age"),
                ErrorCreator.createError(StringUtils.fromString("Invalid data given")),
                errorDetails);
    }
}

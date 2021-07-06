/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.jvm.runtime.api.tests;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

/**
 * This class contains a set of utility methods required for runtime api @{@link ValueCreator} testing.
 *
 * @since 2.0.0
 */
public class Values {

    private static Module objectModule = new Module("testorg", "runtime_api.objects", "1.0.0");
    private static Module recordModule = new Module("testorg", "runtime_api.records", "1.0.0");

    public static BMap<BString, Object> getRecord(BString recordName) {
        HashMap<String, Object> address = new HashMap<>();
        address.put("city", StringUtils.fromString("Nugegoda"));
        address.put("country", StringUtils.fromString("Sri Lanka"));
        address.put("postalCode", 10250);
        return ValueCreator.createRecordValue(recordModule, recordName.getValue(), address);
    }

    public static BObject getObject(BString objectName) {
        BMap<BString, Object> address = getRecord(StringUtils.fromString("Address"));
        return ValueCreator.createObjectValue(objectModule, objectName.getValue(), StringUtils.fromString("Waruna"),
                                              14, address);
    }

    public static BArray getParamNames(BObject object, BString methodName) {
        RemoteMethodType remoteType = getRemoteMethod(object, methodName);
        if (remoteType == null) {
            return ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING, 0), 0);
        }
        String[] paramNames = remoteType.getParamNames();
        int len = paramNames.length;
        BString[] params = new BString[len];
        for (int i = 0; i < len; i++) {
            params[i] = StringUtils.fromString(paramNames[i]);
        }
        return ValueCreator.createArrayValue(params);
    }

    public static BArray getParamDefaultability(BObject object, BString methodName) {
        RemoteMethodType remoteType = getRemoteMethod(object, methodName);
        if (remoteType == null) {
            return ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_BOOLEAN, 0), 0);
        }
        Boolean[] paramDefaultability = remoteType.getParamDefaultability();
        int len = paramDefaultability.length;
        boolean[] values = new boolean[len];
        for (int i = 0; i < len; i++) {
            values[i] = paramDefaultability[i];
        }
        return ValueCreator.createArrayValue(values);
    }

    private static RemoteMethodType getRemoteMethod(BObject object, BString methodName) {
        ObjectType objectType = object.getType();
        Optional<MethodType> funcType = Arrays.stream(objectType.getMethods())
                .filter(r -> r.getName().equals(methodName.getValue())).findAny();
        return funcType.isEmpty() ? null : (RemoteMethodType) funcType.get();
    }

}

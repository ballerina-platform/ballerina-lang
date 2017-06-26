/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.nativeimpl.lang.time;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Contains utility methods for time related functions.
 */
public abstract class AbstractTimeFunction extends AbstractNativeFunction {

    public static final String TIME_PACKAGE = "ballerina.lang.time";
    public static final String STRUCT_TYPE_TIME = "Time";
    public static final String STRUCT_TYPE_TIMEZONE = "Timezone";

    private StructInfo timeStructInfo;
    private StructInfo zoneStructInfo;

    BStruct createCurrentTime(Context context) {
        long currentTime = Instant.now().toEpochMilli();
        BStruct currentTimezone = createCurrentTimeZone(context);
        return createBStruct(getTimeStructInfo(context), currentTime, currentTimezone);
    }

    BStruct createCurrentTimeZone(Context context) {
        //Get zone id
        ZoneId zoneId = ZoneId.systemDefault();
        String zoneIdName = zoneId.toString();
        //Get offset in seconds
        ZoneOffset o = OffsetDateTime.now().getOffset();
        int offset = o.getTotalSeconds();
        return createBStruct(getTimeZoneStructInfo(context), zoneIdName, offset);
    }

    private StructInfo getTimeZoneStructInfo(Context context) {
        StructInfo result = zoneStructInfo;
        if (result == null) {
            PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(TIME_PACKAGE);
            zoneStructInfo = timePackageInfo.getStructInfo(STRUCT_TYPE_TIMEZONE);
        }
        return zoneStructInfo;
    }

    private StructInfo getTimeStructInfo(Context context) {
        StructInfo result = timeStructInfo;
        if (result == null) {
            PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(TIME_PACKAGE);
            timeStructInfo = timePackageInfo.getStructInfo(STRUCT_TYPE_TIME);
        }
        return timeStructInfo;
    }

    private BStruct createBStruct(StructInfo structInfo, Object... values) {
        BStruct bStruct = new BStruct(structInfo.getType());
        bStruct.setFieldTypes(structInfo.getFieldTypes());
        bStruct.init(structInfo.getFieldCount());

        int longRegIndex = -1;
        int stringRegIndex = -1;
        int refRegIndex = -1;
        for (int i = 0; i < structInfo.getFieldTypes().length; i++) {
            BType paramType = structInfo.getFieldTypes()[i];
            if (values.length < i + 1) {
                break;
            }
            switch (paramType.getTag()) {
            case TypeTags.INT_TAG:
                if (values[i] != null) {
                    if (values[i] instanceof Integer) {
                        bStruct.setIntField(++longRegIndex, (Integer) values[i]);
                    } else if (values[i] instanceof Long) {
                        bStruct.setIntField(++longRegIndex, (Long) values[i]);
                    }
                }
                break;
            case TypeTags.STRING_TAG:
                if (values[i] != null && values[i] instanceof String) {
                    bStruct.setStringField(++stringRegIndex, (String) values[i]);
                }
                break;
            default:
                if (values[i] != null && (values[i] instanceof BRefType)) {
                    bStruct.setRefField(++refRegIndex, (BRefType) values[i]);
                }
            }
        }
        return bStruct;
    }
}

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
package org.ballerinalang.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Date;
import java.util.TimeZone;

/**
 * A util class for handling common functions across native implementation.
 *
 * @since 0.95.4
 */
public class Utils {

    public static final String PACKAGE_TIME = "ballerina.time";
    public static final String STRUCT_TYPE_TIME = "Time";
    public static final String STRUCT_TYPE_TIMEZONE = "Timezone";

    public static BStruct createTimeZone(StructInfo timezoneStructInfo, String zoneIdValue) {
        String zoneIdName;
        try {
            ZoneId zoneId = ZoneId.of(zoneIdValue);
            zoneIdName = zoneId.toString();
            //Get offset in seconds
            TimeZone tz = TimeZone.getTimeZone(zoneId);
            int offsetInMills  = tz.getOffset(new Date().getTime());
            int offset = offsetInMills / 1000;
            return BLangVMStructs.createBStruct(timezoneStructInfo, zoneIdName, offset);
        } catch (ZoneRulesException e) {
            throw new BallerinaException("invalid timezone id: " + zoneIdValue);
        }
    }

    public static BStruct createTimeStruct(StructInfo timezoneStructInfo, StructInfo timeStructInfo, long millis,
            String zoneIdName) {
        BStruct timezone = Utils.createTimeZone(timezoneStructInfo, zoneIdName);
        return BLangVMStructs.createBStruct(timeStructInfo, millis, timezone);
    }

    public static StructInfo getTimeZoneStructInfo(Context context) {
        PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_TIME);
        if (timePackageInfo == null) {
            return null;
        }
        return timePackageInfo.getStructInfo(STRUCT_TYPE_TIMEZONE);
    }

    public static StructInfo getTimeStructInfo(Context context) {
        PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_TIME);
        if (timePackageInfo == null) {
            return null;
        }
        return timePackageInfo.getStructInfo(STRUCT_TYPE_TIME);
    }

    public static BStruct createConversionError(Context context, String msg) {
        return BLangVMErrors.createTypeConversionError(context, -1, msg);
    }
}

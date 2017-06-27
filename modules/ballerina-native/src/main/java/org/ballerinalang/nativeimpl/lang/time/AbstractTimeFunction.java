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
import org.ballerinalang.util.exceptions.BallerinaException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRulesException;
import java.util.Date;
import java.util.TimeZone;

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

    BStruct createTime(Context context, long timeValue, String zoneId) {
        BStruct timezone;
        if (zoneId.isEmpty()) {
            timezone = createCurrentTimeZone(context);
        } else {
            timezone = createTimeZone(context, zoneId);
        }
        return createBStruct(getTimeStructInfo(context), timeValue, timezone);
    }

    BStruct parseTime(Context context, String dateValue, String pattern) {
        long milliSeconds;
        BStruct timezone;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateValue, formatter);
            milliSeconds = zonedDateTime.toInstant().toEpochMilli();
            ZoneId zoneId = zonedDateTime.getZone();
            timezone = createTimeZone(context, zoneId);
        } catch (DateTimeParseException e) {
            throw new BallerinaException("parse date " + dateValue + " for the format " + pattern  + " failed ");
        } catch (IllegalArgumentException e) {
            throw new BallerinaException("invalid pattern for parsing " + pattern);
        }
        return createBStruct(getTimeStructInfo(context), milliSeconds, timezone);
    }

    String getFormattedtString(BStruct timeStruct, String pattern) {
        long timeData = timeStruct.getIntField(0);
        BStruct zoneData = (BStruct) timeStruct.getRefField(0);
        ZoneId zoneId;
        String formattedString;
        if (zoneData != null) {
            String zoneIdName = zoneData.getStringField(0);
            zoneId = ZoneId.of(zoneIdName);
        } else {
            zoneId = ZoneId.systemDefault();
        }
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
            formattedString = dateTimeFormatter.format(Instant.ofEpochMilli(timeData));
        } catch (IllegalArgumentException e) {
            throw new BallerinaException("invalid pattern for parsing " + pattern);
        }
        return formattedString;
    }

    String getDefaultString(BStruct timeStruct) {
        long timeData = timeStruct.getIntField(0);
        BStruct zoneData = (BStruct) timeStruct.getRefField(0);
        ZoneId zoneId;
        if (zoneData != null) {
            String zoneIdName = zoneData.getStringField(0);
            zoneId = ZoneId.of(zoneIdName);
        } else {
            zoneId = ZoneId.systemDefault();
        }
        ZonedDateTime dateTime = Instant.ofEpochMilli(timeData).atZone(zoneId);
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private BStruct createCurrentTimeZone(Context context) {
        //Get zone id
        ZoneId zoneId = ZoneId.systemDefault();
        String zoneIdName = zoneId.toString();
        //Get offset in seconds
        ZoneOffset o = OffsetDateTime.now().getOffset();
        int offset = o.getTotalSeconds();
        return createBStruct(getTimeZoneStructInfo(context), zoneIdName, offset);
    }

    private BStruct createTimeZone(Context context, String zoneIdValue) {
        String zoneIdName;
        try {
            ZoneId zoneId = ZoneId.of(zoneIdValue);
            zoneIdName = zoneId.toString();
            //Get offset in seconds
            TimeZone tz = TimeZone.getTimeZone(zoneId);
            int offsetInMills  = tz.getOffset(new Date().getTime());
            int offset = offsetInMills / 1000;
            return createBStruct(getTimeZoneStructInfo(context), zoneIdName, offset);
        } catch (ZoneRulesException e) {
            throw new BallerinaException("invalid zone id " + zoneIdValue);
        }

    }

    private BStruct createTimeZone(Context context, ZoneId zoneId) {
        //Get zone id
        String zoneIdName = zoneId.toString();
        //Get offset in seconds
        TimeZone tz = TimeZone.getTimeZone(zoneId);
        int offsetInMills  = tz.getOffset(new Date().getTime());
        int offset = offsetInMills / 1000;
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

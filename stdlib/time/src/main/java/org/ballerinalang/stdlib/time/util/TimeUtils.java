/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.time.util;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;

import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Date;
import java.util.TimeZone;

import static org.ballerinalang.util.BLangConstants.ORG_NAME_SEPARATOR;

/**
 * A util class for the time package's native implementation.
 *
 * @since 0.95.4
 */
public class TimeUtils {
    private static final String TIME_PACKAGE_PATH = "ballerina" + ORG_NAME_SEPARATOR + "time";
    private static final String STRUCT_TYPE_TIME = "Time";
    private static final String STRUCT_TYPE_TIMEZONE = "TimeZone";

    private static final String TIME_ERROR_CODE = "{ballerina/time}TimeError";

    public static MapValue<String, Object> createTimeZone(MapValue<String, Object> timeZoneRecord, String zoneIdValue) {
        ZoneId zoneId = getTimeZone(zoneIdValue);
        //Get offset in seconds
        TimeZone tz = TimeZone.getTimeZone(zoneId);
        int offsetInMills = tz.getOffset(new Date().getTime());
        long offset = offsetInMills / 1000;
        return BallerinaValues.createRecord(timeZoneRecord, zoneIdValue, offset);

    }

    public static ZoneId getTimeZone(String zoneIdValue) {
        try {
            return ZoneId.of(zoneIdValue);
        } catch (ZoneRulesException e) {
            throw TimeUtils.getTimeError("invalid timezone id: " + zoneIdValue);
        }
    }

    public static MapValue<String, Object> createTimeRecord(MapValue<String, Object> timeZoneRecord,
                                                            MapValue<String, Object> timeRecord, long millis,
                                                            String zoneIdName) {
        MapValue<String, Object> timezone = createTimeZone(timeZoneRecord, zoneIdName);
        return BallerinaValues.createRecord(timeRecord, millis, timezone);
    }

    public static MapValue<String, Object> getTimeZoneRecord() {
        return BallerinaValues.createRecordValue(TIME_PACKAGE_PATH, STRUCT_TYPE_TIMEZONE);
    }

    public static MapValue<String, Object> getTimeRecord() {
        return BallerinaValues.createRecordValue(TIME_PACKAGE_PATH, STRUCT_TYPE_TIME);
    }

    public static ErrorValue getTimeError(String message) {
        return BallerinaErrors.createError(TIME_ERROR_CODE, message);
    }
}

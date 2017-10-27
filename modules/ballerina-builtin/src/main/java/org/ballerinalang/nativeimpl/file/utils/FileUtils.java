/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.file.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import static org.ballerinalang.nativeimpl.builtin.timelib.AbstractTimeFunction.STRUCT_TYPE_TIME;
import static org.ballerinalang.nativeimpl.builtin.timelib.AbstractTimeFunction.STRUCT_TYPE_TIMEZONE;
import static org.ballerinalang.nativeimpl.builtin.timelib.AbstractTimeFunction.TIME_PACKAGE;
import static org.ballerinalang.nativeimpl.file.utils.Constants.ACCESS_DENIED_ERROR;
import static org.ballerinalang.nativeimpl.file.utils.Constants.FILE_PACKAGE;
import static org.ballerinalang.nativeimpl.file.utils.Constants.FILE_STRUCT;
import static org.ballerinalang.nativeimpl.file.utils.Constants.IO_ERROR;

/**
 * A util class for handling struct creation
 *
 * @since 0.94.1
 */
public class FileUtils {

    public static BStruct createFileStruct(Context context, String path) {
        PackageInfo filePkg = context.getProgramFile().getPackageInfo(FILE_PACKAGE);
        StructInfo fileInfo = filePkg.getStructInfo(FILE_STRUCT);
        return BLangVMStructs.createBStruct(fileInfo, path);
    }

    public static BStruct createAccessDeniedError(Context context, String msg) {
        PackageInfo filePkg = context.getProgramFile().getPackageInfo(FILE_PACKAGE);
        StructInfo accessErrInfo = filePkg.getStructInfo(ACCESS_DENIED_ERROR);
        return BLangVMStructs.createBStruct(accessErrInfo, msg);
    }

    public static BStruct createIOError(Context context, String msg) {
        PackageInfo filePkg = context.getProgramFile().getPackageInfo(FILE_PACKAGE);
        StructInfo ioErrInfo = filePkg.getStructInfo(IO_ERROR);
        return BLangVMStructs.createBStruct(ioErrInfo, msg);
    }

    public static BStruct createTimeStruct(Context context, ZonedDateTime time, long millis) {
        BStruct timezone = createTimeZone(context, time.getZone());
        return BLangVMStructs.createBStruct(getTimeStructInfo(context), millis, timezone);
    }

    private static BStruct createTimeZone(Context context, ZoneId zoneId) {
        String zoneIdName = zoneId.toString();
        TimeZone tz = TimeZone.getTimeZone(zoneId);
        int offsetInMills = tz.getOffset(new Date().getTime());
        int offset = offsetInMills / 1000;
        return BLangVMStructs.createBStruct(getTimeZoneStructInfo(context), zoneIdName, offset);
    }

    private static StructInfo getTimeStructInfo(Context context) {
        PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(TIME_PACKAGE);
        return timePackageInfo.getStructInfo(STRUCT_TYPE_TIME);
    }

    private static StructInfo getTimeZoneStructInfo(Context context) {
        PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(TIME_PACKAGE);
        return timePackageInfo.getStructInfo(STRUCT_TYPE_TIMEZONE);
    }
}

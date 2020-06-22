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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.time.util;

import org.ballerinalang.jvm.types.BPackage;

import static org.ballerinalang.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
/**
 * Constants used in Ballerina Time library.
 *
 * @since 1.1.0
 */
public class Constants {
    private Constants() {}

    public static final String TIME_PACKAGE_VERSION = "1.0.0";
    public static final BPackage TIME_PACKAGE_ID = new BPackage(BALLERINA_BUILTIN_PKG_PREFIX, "time",
                                                                TIME_PACKAGE_VERSION);
    public static final String STRUCT_TYPE_TIME = "Time";
    public static final String STRUCT_TYPE_TIMEZONE = "TimeZone";

    public static final String TIME_ERROR = "TimeError";
    public static final String KEY_ZONED_DATETIME = "ZonedDateTime";
    public static final String TIME_FIELD = "time";
    public static final String ZONE_FIELD = "zone";
    public static final String ZONE_ID_FIELD = "id";

    public static final int MULTIPLIER_TO_NANO = 1000000;
}

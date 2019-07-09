/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.time.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.time.util.TimeUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Convert a Time to string in the given format.
 *
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "format"
)
public class Format extends AbstractTimeFunction {

    @Override
    public void execute(Context context) {
    }

    public static Object format(Strand strand, MapValue<String, Object> timeRecord, Object pattern) {
        try {
            if ("RFC_1123".equals(pattern.toString())) {
                ZonedDateTime zonedDateTime = getZonedDateTime(timeRecord);
                return zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
            } else {
                return getFormattedString(timeRecord, pattern.toString());
            }
        } catch (IllegalArgumentException e) {
            return TimeUtils.getTimeError("Invalid Pattern: " + pattern.toString());
        }
    }
}

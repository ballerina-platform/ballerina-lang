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
package org.ballerinalang.stdlib.time.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.time.util.TimeUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Create a time from the given time components.
 *
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "createTime"
)
public class CreateTime extends AbstractTimeFunction {

    @Override
    public void execute(Context context) {
        int years = (int) context.getIntArgument(0);
        int months = (int) context.getIntArgument(1);
        int dates = (int) context.getIntArgument(2);
        int hours = (int) context.getIntArgument(3);
        int minutes = (int) context.getIntArgument(4);
        int seconds = (int) context.getIntArgument(5);
        int milliSeconds = (int) context.getIntArgument(6);
        String zoneId = context.getStringArgument(0);
        try {
            BMap<String, BValue> timeVal = createDateTime(context, years, months, dates, hours, minutes, seconds,
                    milliSeconds, zoneId);
            context.setReturnValues(timeVal);
        } catch (BallerinaException e) {
            context.setReturnValues(TimeUtils.getTimeError(context, e.getMessage()));
        }
    }

    public static Object createTime(Strand strand, long years, long months, long dates, long hours, long minutes,
                                    long seconds, long milliSeconds, String zoneId) {
        try {
            return createDateTime((int) years, (int) months, (int) dates, (int) hours, (int) minutes, (int) seconds,
                                  (int) milliSeconds, zoneId);
        } catch (BallerinaException e) {
            return TimeUtils.getTimeError(e.getMessage());
        }
    }
}

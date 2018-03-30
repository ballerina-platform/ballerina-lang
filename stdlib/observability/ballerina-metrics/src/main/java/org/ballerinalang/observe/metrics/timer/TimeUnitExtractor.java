/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.observe.metrics.timer;

import org.ballerinalang.model.values.BEnumerator;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.concurrent.TimeUnit;

/**
 * This class extracts the {@link TimeUnit}.
 */
public class TimeUnitExtractor {

    private static TimeUnit timeUnit;

    public static TimeUnit getTimeUnit(BEnumerator timeUnitEnum) {
        switch (timeUnitEnum.getName()) {
            case "NANOSECONDS":
                timeUnit = timeUnit.NANOSECONDS;
                break;
            case "MICROSECONDS":
                timeUnit = timeUnit.MICROSECONDS;
                break;
            case "MILLISECONDS":
                timeUnit = timeUnit.MILLISECONDS;
                break;
            case "SECONDS":
                timeUnit = timeUnit.SECONDS;
                break;
            case "MINUTES":
                timeUnit = timeUnit.MINUTES;
                break;
            case "HOURS":
                timeUnit = timeUnit.HOURS;
                break;
            case "DAYS":
                timeUnit = timeUnit.DAYS;
                break;
            default:
                throw new BallerinaException("Unsupported base time unit " + timeUnitEnum + " for timer registration.");
        }
        return timeUnit;
    }
}
